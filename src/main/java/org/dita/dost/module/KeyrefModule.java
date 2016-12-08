/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
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
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.writer.ConkeyrefFilter;
import org.dita.dost.writer.KeyrefPaser;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

/**
 * Keyref Module.
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

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        try {
            tempFileNameScheme = (TempFileNameScheme) getClass().forName(job.getProperty("temp-file-name-scheme")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());
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
        final Collection<FileInfo> fis = new HashSet(job.getFileInfo(new Filter<FileInfo>() {
            @Override
            public boolean accept(final FileInfo f) {
                return f.hasKeyref;
            }
        }));
        if (!fis.isEmpty()) {
            tempFileNameScheme.setBaseDir(job.getInputDir());
            initFilters();

            final Document doc = readMap();

            final KeyrefReader reader = new KeyrefReader();
            reader.setLogger(logger);
            final URI mapFile = job.getInputMap();
            logger.info("Reading " + job.tempDirURI.resolve(mapFile).toString());
            reader.read(job.tempDirURI.resolve(mapFile), doc);

            final KeyScope rootScope = reader.getKeyDefinition();
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
            } catch (final IOException e) {
                throw new DITAOTException("Failed to store job state: " + e.getMessage(), e);
            }
        }
        return null;
    }

    private void initFilters() {
        topicFragmentFilter = new TopicFragmentFilter(ATTRIBUTE_NAME_CONREF, ATTRIBUTE_NAME_CONREFEND);
    }

    /** Collect topics for key reference processing and modify map to reflect new file names. */
    // FIXME multple topirefs in a single scope result in redundant copies, allow duplicates inside scope
    private List<ResolveTask> collectProcessingTopics(final Collection<FileInfo> fis, final KeyScope rootScope, final Document doc) {
        final List<ResolveTask> res = new ArrayList<>();
        res.add(new ResolveTask(rootScope, job.getFileInfo(job.getInputMap()), null));
        // Collect topics from map and rewrite topicrefs for duplicates
        walkMap(doc.getDocumentElement(), rootScope, res);
        // Collect topics not in map and map itself
        for (final FileInfo f: fis) {
            if (!usage.containsKey(f.uri)) {
                res.add(processTopic(f, rootScope, f.isResourceOnly));
            }
        }
        return adjustResourceRenames(res);
    }

    List<ResolveTask> adjustResourceRenames(final List<ResolveTask> renames) {
        final Map<KeyScope, List<ResolveTask>> scopes = renames.stream().collect(Collectors.groupingBy(rt -> rt.scope));

        final List<ResolveTask> res = new ArrayList<>();
        for (final Map.Entry<KeyScope, List<ResolveTask>> group : scopes.entrySet()) {
            final KeyScope scope = group.getKey();
            final List<ResolveTask> tasks = group.getValue();
            final Map<URI, URI> rewrites = new HashMap<>();
            tasks.stream()
                    .filter(t -> t.out != null)
                    .forEach(t -> rewrites.put(t.in.uri, t.out.uri));
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
            final KeyDef newKey = new KeyDef(oldKey.keys, href, oldKey.scope, oldKey.format, oldKey.source, oldKey.element);
            newKeys.put(key.getKey(), newKey);
        }
        return new KeyScope(scope.name,
                newKeys,
                scope.childScopes.values().stream()
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
    private void walkMap(final Element elem, final KeyScope scope, final List<ResolveTask> res) {
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
        final boolean isResourceOnly = isResourceOnly(elem);
        for (final KeyScope s: ss) {
            if (hrefNode != null) {
                final URI href = stripFragment(job.getInputMap().resolve(hrefNode.getValue()));
                final FileInfo fi = job.getFileInfo(href);
                if (fi != null && fi.hasKeyref) {
                    final ResolveTask resolveTask = processTopic(fi, s, isResourceOnly);
                    res.add(resolveTask);
                    final Integer used = usage.get(fi.uri);
                    if (used > 1) {
                        final URI value = tempFileNameScheme.generateTempFileName(resolveTask.out.result);
                        hrefNode.setValue(value.toString());
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
                XMLUtils.transform(new File(job.tempDir, r.in.file.getPath()),
                                   new File(job.tempDir, r.out.file.getPath()),
                                   filters);
            } else {
                logger.info("Processing " + job.tempDirURI.resolve(r.in.uri));
                XMLUtils.transform(new File(job.tempDir, r.in.file.getPath()), filters);
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

    private Document readMap() throws DITAOTException {
        InputSource in = null;
        try {
            in = new InputSource(job.tempDirURI.resolve(job.getInputMap()).toString());
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
            out = new StreamResult(new File(job.tempDirURI.resolve(job.getInputMap())));
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
