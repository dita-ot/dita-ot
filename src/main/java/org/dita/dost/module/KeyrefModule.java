/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.dita.dost.util.*;
import org.dita.dost.writer.TopicFragmentFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

    /** Delayed conref utils. */
    private DelayConrefUtils delayConrefUtils;
    private String transtype;
    final Set<URI> normalProcessingRole = new HashSet<>();
    final Map<URI, Integer> usage = new HashMap<>();
    private TopicFragmentFilter topicFragmentFilter;

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
        final Collection<FileInfo> fis = new HashSet(job.getFileInfo(new Filter() {
            @Override
            public boolean accept(final FileInfo f) {
                return f.hasKeyref;
            }
        }));
        if (!fis.isEmpty()) {
            initFilters();

            final Document doc = readMap();

            final KeyrefReader reader = new KeyrefReader();
            reader.setLogger(logger);
            final URI mapFile = job.getInputMap();
            logger.info("Reading " + job.tempDir.toURI().resolve(mapFile).toString());
            reader.read(job.tempDir.toURI().resolve(mapFile), doc);

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
    private List<ResolveTask> collectProcessingTopics(final Collection<FileInfo> fis, final KeyScope rootScope, final Document doc) throws DITAOTException {
        final List<ResolveTask> res = new ArrayList<>();
        // Collect topics from map and rewrite topicrefs for duplicates
        walkMap(doc.getDocumentElement(), rootScope, res);
        // Collect topics not in map and map itself
        for (final FileInfo f: fis) {
            if (!usage.containsKey(f.uri)) {
                res.add(processTopic(f, rootScope));
            }
        }
        return res;
    }

    /** Tuple class for key reference processing info. */
    private static class ResolveTask {
        final KeyScope scope;
        final FileInfo in;
        final FileInfo out;
        private ResolveTask(final KeyScope scope, final FileInfo in, final FileInfo out) {
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
        for (final KeyScope s: ss) {
            if (hrefNode != null) {
                final URI href = stripFragment(job.getInputMap().resolve(hrefNode.getValue()));
                final FileInfo fi = job.getFileInfo(href);
                if (fi != null && fi.hasKeyref) {
                    res.add(processTopic(fi, s));
                    final Integer used = usage.get(fi.uri);
                    if (used > 1) {
                        hrefNode.setValue(addSuffix(toURI(hrefNode.getValue()), "-" + (used - 1)).toString());
                    }
                }
            }
            for (final Element child : getChildElements(elem, MAP_TOPICREF)) {
                walkMap(child, s, res);
            }
        }
    }

    /**
     * Determine how topic is processed for key reference processing.
     *
     * @return key reference processing
     */
    private ResolveTask processTopic(final FileInfo f, final KeyScope scope) {
        final Integer used = usage.containsKey(f.uri) ? usage.get(f.uri) + 1 : 1;
        usage.put(f.uri, used);

        if (used > 1) {
            final URI out = addSuffix(f.uri, "-" + (used - 1));
            final FileInfo fo = new FileInfo.Builder(f).uri(out).build();
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
        conkeyrefFilter.setCurrentFile(r.in.file);
        conkeyrefFilter.setDelayConrefUtils(delayConrefUtils);
        filters.add(conkeyrefFilter);

        filters.add(topicFragmentFilter);

        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(logger);
        parser.setJob(job);
        parser.setKeyDefinition(r.scope);
        parser.setCurrentFile(r.in.file);
        filters.add(parser);

        try {
            logger.debug("Using " + (r.scope.name != null ? r.scope.name + " scope" : "root scope"));
            if (r.out != null) {
                logger.info("Processing " + job.tempDir.toURI().resolve(r.in.uri) +
                        " to " + job.tempDir.toURI().resolve(r.out.uri));
                XMLUtils.transform(new File(job.tempDir, r.in.file.getPath()),
                                   new File(job.tempDir, r.out.file.getPath()),
                                   filters);
            } else {
                logger.info("Processing " + job.tempDir.toURI().resolve(r.in.uri));
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
            in = new InputSource(job.tempDir.toURI().resolve(job.getInputMap()).toString());
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
            out = new StreamResult(new File(job.tempDir.toURI().resolve(job.getInputMap())));
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
