/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.exists;
import static java.util.stream.Collectors.toMap;
import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CONREF;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CONREFEND;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_COPY_TO;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_DITA_OT_ORIG_HREF;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_KEYSCOPE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_PROCESSING_ROLE;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.dita.dost.util.Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY;
import static org.dita.dost.util.Constants.INDEX_TYPE_ECLIPSEHELP;
import static org.dita.dost.util.Constants.MAP_TOPICREF;
import static org.dita.dost.util.Constants.SUBMAP;
import static org.dita.dost.util.Job.KEYDEF_LIST_FILE;
import static org.dita.dost.util.URLUtils.addSuffix;
import static org.dita.dost.util.URLUtils.setFragment;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.XMLUtils.close;
import static org.dita.dost.util.XMLUtils.getChildElements;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.KeyScopeSerializer;
import org.dita.dost.util.KeydefDeserializer;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.ConkeyrefFilter;
import org.dita.dost.writer.KeyrefPaser;
import org.dita.dost.writer.TopicFragmentFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Keyref ModuleElem.
 *
 */
final class KeyrefModule extends AbstractPipelineModuleImpl {

    private TempFileNameScheme tempFileNameScheme;
    /** Delayed conref utils. */
    private DelayConrefUtils delayConrefUtils;
    private String transtype;
    final Set<URI> normalProcessingRole = new HashSet<>();
    final Map<URI, Integer> usage = new HashMap<>();
    private TopicFragmentFilter topicFragmentFilter;
    private final XMLUtils xmlUtils = new XMLUtils();

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        try {
            tempFileNameScheme = (TempFileNameScheme) Class.forName(job.getProperty("temp-file-name-scheme")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        xmlUtils.setLogger(logger);
    }

    /**
     * Entry point of KeyrefModule.
     *
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {

        if (fileInfoFilter == null) {
            fileInfoFilter = f -> f.format == null || f.format.equals(ATTR_FORMAT_VALUE_DITA) || f.format.equals(ATTR_FORMAT_VALUE_DITAMAP);
        }
        final Collection<FileInfo> fis = job.getFileInfo(fileInfoFilter).stream()
                .filter(f -> f.hasKeyref)
                .collect(Collectors.toSet());
        if (!fis.isEmpty()) {
            try {
                final String cls = Optional
                        .ofNullable(job.getProperty("temp-file-name-scheme"))
                        .orElse(configuration.get("temp-file-name-scheme"));
                tempFileNameScheme = (GenMapAndTopicListModule.TempFileNameScheme) Class.forName(cls).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            tempFileNameScheme.setBaseDir(job.getInputDir());
            initFilters();

            final Document doc = readMap();
            final KeyScope rootScope = buildKeyScopes(doc);
            final List<ResolveTask> jobs = collectProcessingTopics(fis, rootScope, doc);
            writeMap(doc);

            transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
            delayConrefUtils = transtype.equals(INDEX_TYPE_ECLIPSEHELP) ? new DelayConrefUtils() : null;
            for (final ResolveTask r: jobs) {
                if (r.out != null) {
                    processFile(r);
                }
            }
            for (final ResolveTask r: jobs) {
                if (r.out == null) {
                    processFile(r);
                }
            }

            // Store job configuration updates
            for (final URI file: normalProcessingRole) {
                final FileInfo f = job.getFileInfo(file);
                if (f != null) {
                    f.isResourceOnly = false;
                    job.add(f);
                }
            }

            try {
                job.write();
                serializeKeyDefinitions(rootScope);
            } catch (final IOException e) {
                throw new DITAOTException("Failed to store job state: " + e.getMessage(), e);
            }
        }

        return null;
    }

    private KeyScope buildKeyScopes(Document document) throws DITAOTException {
    	final KeyScope filteredKeyScope = deserializeDefinitions();
    	
        final KeyrefReader reader = new KeyrefReader();
        reader.setLogger(logger);
        reader.setJob(job);
        final Job.FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
        final URI mapFile = in.uri;
        logger.info("Reading " + job.tempDirURI.resolve(mapFile).toString());
        reader.read(job.tempDirURI.resolve(mapFile), document);
        return mergeKeyScopes(reader.getKeyDefinition(), filteredKeyScope);
    }

    KeyScope mergeKeyScopes(KeyScope rootScope, KeyScope filteredKeyScope) {
        long ref1 = currentTimeMillis();
        final Map<String, Map<String, KeyDef>> filteredKeydef = new HashMap<>();
        readFilteredDefinitions(filteredKeyScope, filteredKeydef);
        long ref2 = currentTimeMillis();
        KeyScope keyScope = mergeDefinitions(rootScope, filteredKeydef);
        long ref3 = currentTimeMillis();
        logger.info(format("Reading filtered definitions: %d ms, merging: %d ms, total %d ms", ref2 - ref1, ref3 - ref2, ref3 - ref1));
        return keyScope;
    }
    
    private void readFilteredDefinitions(KeyScope filteredKeyScope, Map<String, Map<String, KeyDef>> filteredKeydef) {
    	if (filteredKeyScope != null) {
    		if (!filteredKeyScope.keySet().isEmpty()) {
        		filteredKeydef.put(filteredKeyScope.id, filteredKeyScope.keyDefinition);
        	}
        	
        	filteredKeyScope.childScopes.stream().forEach(scope -> readFilteredDefinitions(scope, filteredKeydef));
    	}
    }
    
    private KeyScope mergeDefinitions(KeyScope rootKeyScope, Map<String, Map<String, KeyDef>> filteredKeydef) {
    	if (filteredKeydef.containsKey(rootKeyScope.id)) {
    		for (Map.Entry<String, KeyDef> key : filteredKeydef.get(rootKeyScope.id).entrySet()) {
    			rootKeyScope.keyDefinition.put(key.getKey(), key.getValue());
        	}
    	}
    	
    	rootKeyScope.childScopes.stream().map(scope -> mergeDefinitions(scope, filteredKeydef)).collect(Collectors.toList());
    	return rootKeyScope;
    }

    private void initFilters() {
        topicFragmentFilter = new TopicFragmentFilter(ATTRIBUTE_NAME_CONREF, ATTRIBUTE_NAME_CONREFEND);
    }

    /** Collect topics for key reference processing and modify map to reflect new file names. */
    private List<ResolveTask> collectProcessingTopics(final Collection<FileInfo> fis, final KeyScope rootScope, final Document doc) {
    	long start = System.currentTimeMillis();
        final List<ResolveTask> res = new ArrayList<>();
        final FileInfo input = job.getFileInfo(fi -> fi.isInput).iterator().next();
        res.add(new ResolveTask(rootScope, input, null));
        // Collect topics from map and rewrite topicrefs for duplicates
        logger.info("The size of ResolveTask " + res.size());
        long beforeWalkMap = System.currentTimeMillis();
        logger.info("Time milis taken after getFileInfo " + (beforeWalkMap-start));
        
        walkMap(doc.getDocumentElement(), rootScope, res);
        long afterWalkMap = System.currentTimeMillis();
        logger.info("Time milis taken after walkMap " + (afterWalkMap-beforeWalkMap));
        // Collect topics not in map and map itself
        for (final FileInfo f: fis) {
            if (!usage.containsKey(f.uri)) {
                res.add(processTopic(f, rootScope, f.isResourceOnly));
            }
        }
        final List<ResolveTask> deduped = removeDuplicateResolveTargets(res);
        long afterRemoveDup = System.currentTimeMillis();
        
        logger.info("Time milis taken after removeDuplicateResolveTargets " + (afterRemoveDup-afterWalkMap));
        if (fileInfoFilter != null) {
            List<ResolveTask> adjustResourceRenames = adjustResourceRenames(deduped.stream()
                    .filter(rs -> fileInfoFilter.test(rs.in))
                    .collect(Collectors.toList()));
            long afteradjustResourceRenames = System.currentTimeMillis();
            logger.info("Time milis taken after adjustResourceRenames " + (afteradjustResourceRenames-afterRemoveDup));
            return adjustResourceRenames;
        } else {
        	List<ResolveTask> adjustResourceRenames = adjustResourceRenames(deduped);
        	long afteradjustResourceRenames = System.currentTimeMillis();
        	logger.info("Time milis taken after adjustResourceRenames " + (afteradjustResourceRenames-afterRemoveDup));
        	return adjustResourceRenames;
        }
    }

    /** Remove duplicate sources within the same scope */
    private List<ResolveTask> removeDuplicateResolveTargets(List<ResolveTask> renames) {
        return renames.stream()
                .collect(Collectors.groupingBy(
                        rt -> rt.scope,
                        Collectors.toMap(
                                rt -> rt.in.uri,
                                Function.identity(),
                                (rt1, rt2) -> rt1
                        )
                )).values().stream()
                .flatMap(m -> m.values().stream())
                .collect(Collectors.toList());
    }

    /** Adjust key targets per rewrites */
    List<ResolveTask> adjustResourceRenames(final List<ResolveTask> renames) {
        final Map<KeyScope, List<ResolveTask>> scopes = renames.stream().collect(Collectors.groupingBy(rt -> rt.scope));

        final List<ResolveTask> res = new ArrayList<>();
        for (final Map.Entry<KeyScope, List<ResolveTask>> group : scopes.entrySet()) {
            final KeyScope scope = group.getKey();
            final List<ResolveTask> tasks = group.getValue();
            final Map<URI, URI> rewrites = tasks.stream()
                    // FIXME this should be filtered out earlier
                    .filter(t -> t.out != null)
                    .collect(toMap(
                            t -> t.in.uri,
                            t -> t.out.uri
                    ));
            final KeyScope resScope = rewriteScopeTargets(scope, rewrites);
            tasks.stream().map(t -> new ResolveTask(resScope, t.in, t.out)).forEach(res::add);
        }

        return res;
    }

    /** ORIGINAL CODE
    KeyScope rewriteScopeTargets(KeyScope scope, Map<URI, URI> rewrites) {
        final Map<String, KeyDef> newKeys = new HashMap<>();
        for (Map.Entry<String, KeyDef> key : scope.keyDefinition.entrySet()) {
            final KeyDef oldKey = key.getValue();
            URI href = oldKey.href;
            if (href != null && rewrites.containsKey(stripFragment(href))) {
                href = setFragment(rewrites.get(stripFragment(href)), href.getFragment());
            }
            final KeyDef newKey = new KeyDef(oldKey.keys, href, oldKey.scope, oldKey.format, oldKey.source, oldKey.element, oldKey.isFiltered());
            newKeys.put(key.getKey(), newKey);
        }
        return new KeyScope(scope.id, scope.name,
                newKeys,
                scope.childScopes.stream()
                        .map(c -> rewriteScopeTargets(c, rewrites))
                        .collect(Collectors.toList()));
    } */
    
    KeyScope rewriteScopeTargets(KeyScope scope, Map<URI, URI> rewrites) {
        for (Map.Entry<String, KeyDef> key : scope.keyDefinition.entrySet()) {
            KeyDef oldKey = key.getValue();
            URI href = oldKey.href;
            if (href != null && rewrites.containsKey(stripFragment(href))) {
                href = setFragment(rewrites.get(stripFragment(href)), href.getFragment());
            }

            oldKey.href = href;
        }

        scope.childScopes.stream().map(c -> rewriteScopeTargets(c, rewrites)).collect(Collectors.toList());
        return scope;
    }


    /** Tuple class for key reference processing info. */
    static class ResolveTask {
        final KeyScope scope;
        final FileInfo in;
        final FileInfo out;
        ResolveTask(final KeyScope scope, final FileInfo in, final FileInfo out) {
            assert scope != null;
            this.scope = scope;
            assert in != null;
            this.in = in;
            this.out = out;
        }
    }

    /** Recursively walk map and process topics that have keyrefs. */
    void walkMap(final Element elem, final KeyScope scope, final List<ResolveTask> res) {
        List<KeyScope> ss = Collections.singletonList(scope);
        if (elem.getAttributeNode(ATTRIBUTE_NAME_KEYSCOPE) != null) {
            ss = new ArrayList<>();
            for (final String keyscope: elem.getAttribute(ATTRIBUTE_NAME_KEYSCOPE).trim().split("\\s+")) {
                final KeyScope s = scope.getChildScope(keyscope);
                assert s != null;
                if(s==null) {
                	Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
                    if (hrefNode == null) {
                        hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
                    }
                    if (hrefNode == null && SUBMAP.matches(elem)) {
                        hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_DITA_OT_ORIG_HREF);
                    }
                    
                	logger.warn("childscope is null! for " + hrefNode + "keyscope of " + keyscope);
                }else {
                	ss.add(s);
                }
            }
        }
        Attr hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
        if (hrefNode == null) {
            hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
        }
        if (hrefNode == null && SUBMAP.matches(elem)) {
            hrefNode = elem.getAttributeNode(ATTRIBUTE_NAME_DITA_OT_ORIG_HREF);
        }
        
        final boolean isResourceOnly = isResourceOnly(elem);
        for (final KeyScope s: ss) {
            if (hrefNode != null) {
                final URI href = stripFragment(job.getInputMap().resolve(hrefNode.getValue()));
                final FileInfo fi = job.getFileInfo(href);
                if (fi != null && fi.hasKeyref) {
                    final int count = usage.getOrDefault(fi.uri, 0);
                    final Optional<ResolveTask> existing = res.stream().filter(rt -> rt.in!=null&& rt.scope!=null && rt.scope.equals(s) && (rt.in.uri==null?"":rt.in.uri).equals(fi.uri)).findAny();
                    if (count != 0 && existing.isPresent()) {
                        final ResolveTask resolveTask = existing.get();
                        if (resolveTask.out != null) {
                            final URI value = tempFileNameScheme.generateTempFileName(resolveTask.out.result);
                            hrefNode.setValue(value.toString());
                        }
                    } else {
                        final ResolveTask resolveTask = processTopic(fi, s, isResourceOnly);
                        if(resolveTask==null) {
                    		logger.warn("resolveTask is null for " + href.toString());
                    	}else {
                    		if(resolveTask.scope==null) {
                    			logger.warn("resolveTask.scope is null for " + href.toString());
                    		}
                    		if(resolveTask.in==null) {
                    			logger.warn("resolveTask.in is null for " + href.toString());
                    		}else {
                    			if(resolveTask.in.uri==null) {
                    				if( resolveTask.in.file!=null)
                    					logger.warn("resolveTask.in.uri is null for " +  resolveTask.in.file.getPath());
                    				else
                    					logger.warn("resolveTask.in.uri and file is null!");
                    			}
                    		}
                    	}
                        
                        res.add(resolveTask);
                        final Integer used = usage.get(fi.uri);
                        if (used > 1) {
                        	long currentTimeMillis = System.currentTimeMillis();
                            final URI value = tempFileNameScheme.generateTempFileName(resolveTask.out.result);
                            long afterTimeMillis = System.currentTimeMillis();
                            logger.info("tempFileNameScheme.generateTempFileName taken " + (afterTimeMillis-currentTimeMillis));
                            hrefNode.setValue(value.toString());
                        }
                    }
                }
            }
            for (final Element child : getChildElements(elem, MAP_TOPICREF)) {
                walkMap(child, s, res);
            }
        }
    }

    private boolean isResourceOnly(final Element elem) {
        Node curr = elem;
        while (curr != null) {
            if (curr.getNodeType() == Node.ELEMENT_NODE) {
                final Attr processingRole = ((Element) curr).getAttributeNode(ATTRIBUTE_NAME_PROCESSING_ROLE);
                if (processingRole != null) {
                    return processingRole.getValue().equals(ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
                }
            }
            curr = curr.getParentNode();
        }
        return false;
    }

    /**
     * Determine how topic is processed for key reference processing.
     *
     * @return key reference processing
     */
    private ResolveTask processTopic(final FileInfo f, final KeyScope scope, final boolean isResourceOnly) {
        final int increment = isResourceOnly ? 0 : 1;
        final Integer used = usage.containsKey(f.uri) ? usage.get(f.uri) + increment : increment;
        usage.put(f.uri, used);

        if (used > 1) {
            final URI result = addSuffix(f.result, "-" + (used - 1));
            final URI out = tempFileNameScheme.generateTempFileName(result);
            final FileInfo fo = new FileInfo.Builder(f)
                    .uri(out)
                    .result(result)
                    .build();
            // TODO: Should this be added when content is actually generated?
            job.add(fo);
            return new ResolveTask(scope, f, fo);
        } else {
            return new ResolveTask(scope, f, null);
        }
    }

    /**
     * Process key references in a topic. Topic is stored with a new name if it's
     * been processed before.
     */
    private void processFile(final ResolveTask r) {
        final List<XMLFilter> filters = new ArrayList<>();

        final ConkeyrefFilter conkeyrefFilter = new ConkeyrefFilter();
        conkeyrefFilter.setLogger(logger);
        conkeyrefFilter.setJob(job);
        conkeyrefFilter.setKeyDefinitions(r.scope);
        conkeyrefFilter.setCurrentFile(job.tempDirURI.resolve(r.in.uri));
        conkeyrefFilter.setDelayConrefUtils(delayConrefUtils);
        filters.add(conkeyrefFilter);

        filters.add(topicFragmentFilter);

        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(logger);
        parser.setJob(job);
        parser.setKeyDefinition(r.scope);
        parser.setCurrentFile(job.tempDirURI.resolve(r.in.uri));
        filters.add(parser);

        try {
            logger.debug("Using " + (r.scope.name != null ? r.scope.name + " scope" : "root scope"));
            if (r.out != null) {
                logger.info("Processing " + job.tempDirURI.resolve(r.in.uri) +
                        " to " + job.tempDirURI.resolve(r.out.uri));
                xmlUtils.transform(new File(job.tempDir, r.in.file.getPath()),
                                   new File(job.tempDir, r.out.file.getPath()),
                                   filters);
            } else {
                logger.info("Processing " + job.tempDirURI.resolve(r.in.uri));
                xmlUtils.transform(new File(job.tempDir, r.in.file.getPath()), filters);
            }
            // validate resource-only list
            normalProcessingRole.addAll(parser.getNormalProcessingRoleTargets());
        } catch (final DITAOTException e) {
            logger.error("Failed to process key references: " + e.getMessage(), e);
        }
    }

    /**
     * Add key definition to job configuration
     *
     * @param keydefs key defintions to add
     */
    private void writeKeyDefinition(final Map<String, KeyDef> keydefs) {
        try {
            KeyDef.writeKeydef(new File(job.tempDir, KEYDEF_LIST_FILE), keydefs.values());
        } catch (final DITAOTException e) {
            logger.error("Failed to write key definition file: " + e.getMessage(), e);
        }
    }

    private void serializeKeyDefinitions(KeyScope rootScope) throws IOException {
        if (!exists((new File(job.tempDir, KEYDEF_LIST_FILE)).toPath())) {
            long ref1 = currentTimeMillis();
            ObjectWriter objectWriter = new KeyScopeSerializer().newSerializer();
            objectWriter.writeValue(new File(job.tempDir, KEYDEF_LIST_FILE), rootScope);
            logger.info(format("Serializing filtered keydefs: %d ms", currentTimeMillis()-ref1));
        }
    }
    
    private KeyScope deserializeDefinitions() throws DITAOTException {
    	KeyScope filteredKeyScope = null;
    	if ((new File(job.tempDir, KEYDEF_LIST_FILE)).toPath().toFile().exists()) {
            try {
                long ref1 = currentTimeMillis();
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleModule module = new SimpleModule();
                
                module.addDeserializer(KeyDef.class, new KeydefDeserializer());
                objectMapper.registerModule(module);
               
                filteredKeyScope = objectMapper.readValue(new FileInputStream(new File(job.tempDir, KEYDEF_LIST_FILE)),KeyScope.class);
                logger.info(format("Deserializing filtered keydefs: %d ms", currentTimeMillis()-ref1));
            } catch (IOException e) {
                throw new DITAOTException("Couldn't build keyscope", e);
            }
        }
    	return filteredKeyScope;
    }

    private Document readMap() throws DITAOTException {
        InputSource in = null;
        try {
            final FileInfo input = job.getFileInfo(fi -> fi.isInput).iterator().next();
            in = new InputSource(job.tempDirURI.resolve(input.uri).toString());
            return XMLUtils.getDocumentBuilder().parse(in);
        } catch (final Exception e) {
            throw new DITAOTException("Failed to parse map: " + e.getMessage(), e);
        } finally {
            try {
                close(in);
            } catch (IOException e) {
                logger.error("Failed to close input: " + e.getMessage(), e);
            }
        }
    }

    private void writeMap(final Document doc) throws DITAOTException {
        Result out = null;
        try {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
            out = new StreamResult(job.tempDirURI.resolve(in.uri).toString());
            transformer.transform(new DOMSource(doc), out);
        } catch (final TransformerConfigurationException e) {
            throw new RuntimeException(e);
        } catch (final TransformerException e) {
            throw new DITAOTException("Failed to write map: " + e.getMessageAndLocation(), e);
        } finally {
            try {
                close(out);
            } catch (IOException e) {
                logger.error("Failed to close result: " + e.getMessage(), e);
            }
        }
    }


}
