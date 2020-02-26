/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import static java.util.stream.Collectors.toMap;
import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.*;
import org.dita.dost.writer.TopicFragmentFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.XMLFilter;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.writer.ConkeyrefFilter;
import org.dita.dost.writer.KeyrefPaser;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
        if (Files.exists((new File(job.tempDir, KEYDEF_LIST_FILE)).toPath())) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                SimpleModule module = new SimpleModule();
                module.addDeserializer(KeyDef.class, new KeyDefDeserializer());
                objectMapper.registerModule(module);
                return objectMapper.readValue(new FileInputStream(new File(job.tempDir, KEYDEF_LIST_FILE)),KeyScope.class);
            } catch (IOException e) {
                throw new DITAOTException("Couldn't build keyscope", e);
            }
        }

        final KeyrefReader reader = new KeyrefReader();
        reader.setLogger(logger);
        reader.setJob(job);
        final Job.FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
        final URI mapFile = in.uri;
        logger.info("Reading " + job.tempDirURI.resolve(mapFile).toString());
        reader.read(job.tempDirURI.resolve(mapFile), document);
        return reader.getKeyDefinition();
    }

    private void initFilters() {
        topicFragmentFilter = new TopicFragmentFilter(ATTRIBUTE_NAME_CONREF, ATTRIBUTE_NAME_CONREFEND);
    }

    /** Collect topics for key reference processing and modify map to reflect new file names. */
    private List<ResolveTask> collectProcessingTopics(final Collection<FileInfo> fis, final KeyScope rootScope, final Document doc) {
        final List<ResolveTask> res = new ArrayList<>();
        final FileInfo input = job.getFileInfo(fi -> fi.isInput).iterator().next();
        res.add(new ResolveTask(rootScope, input, null));
        // Collect topics from map and rewrite topicrefs for duplicates
        walkMap(doc.getDocumentElement(), rootScope, res);
        // Collect topics not in map and map itself
        for (final FileInfo f: fis) {
            if (!usage.containsKey(f.uri)) {
                res.add(processTopic(f, rootScope, f.isResourceOnly));
            }
        }

        final List<ResolveTask> deduped = removeDuplicateResolveTargets(res);
        if (fileInfoFilter != null) {
            return adjustResourceRenames(deduped.stream()
                    .filter(rs -> fileInfoFilter.test(rs.in))
                    .collect(Collectors.toList()));
        } else {
            return adjustResourceRenames(deduped);
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
                ss.add(s);
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
                    final Optional<ResolveTask> existing = res.stream().filter(rt -> rt.scope.equals(s) && rt.in.uri.equals(fi.uri)).findAny();
                    if (count != 0 && existing.isPresent()) {
                        final ResolveTask resolveTask = existing.get();
                        if (resolveTask.out != null) {
                            final URI value = tempFileNameScheme.generateTempFileName(resolveTask.out.result);
                            hrefNode.setValue(value.toString());
                        }
                    } else {
                        final ResolveTask resolveTask = processTopic(fi, s, isResourceOnly);
                        res.add(resolveTask);
                        final Integer used = usage.get(fi.uri);
                        if (used > 1) {
                            final URI value = tempFileNameScheme.generateTempFileName(resolveTask.out.result);
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
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(new File(job.tempDir, KEYDEF_LIST_FILE), rootScope);
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
