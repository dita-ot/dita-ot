/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module.filter;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.AbstractPipelineModuleImpl;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.ProfilingFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toFile;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.close;
import static org.dita.dost.util.XMLUtils.getChildElements;

/**
 * Branch filter module for topics.
 *
 * <p>Branch filtering is done with the following steps:</p>
 * <ol>
 *   <li>Copy and filter generated copy-to targets</li>
 *   <li>Filter topics that were not branch generated</li>
 * </ol>
 *
 * @since 2.5
 */
public final class TopicBranchFilterModule extends AbstractPipelineModuleImpl {

    private static final String SKIP_FILTER = "skip-filter";
    private static final String BRANCH_COPY_TO = "filter-copy-to";

    private final XMLUtils xmlUtils = new XMLUtils();
    private final DocumentBuilder builder;
    private final DitaValReader ditaValReader;
//    private final TempFileNameScheme tempFileNameScheme;
    private final Map<URI, FilterUtils> filterCache = new HashMap<>();
    /** Current map being processed, relative to temporary directory */
    private URI map;
    /** Absolute URI to map being processed. */
    protected URI currentFile;

    public TopicBranchFilterModule() {
        builder = XMLUtils.getDocumentBuilder();
        ditaValReader = new DitaValReader();
        ditaValReader.initXMLReader(true);
//        try {
//            tempFileNameScheme = (TempFileNameScheme) getClass().forName(configuration.get("temp-file-name-scheme")).newInstance();
//        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
//            throw new RuntimeException(e);
//        }
    }
    
    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        ditaValReader.setLogger(logger);
        xmlUtils.setLogger(logger);
    }

//    @Override
//    public void setJob(final Job job) {
//        super.setJob(job);
//        tempFileNameScheme.setBaseDir(job.getInputDir());
//    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        processMap(job.getInputMap());

        try {
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to serialize job configuration: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Process map for branch replication.
     */
    protected void processMap(final URI map) {
        assert !map.isAbsolute();
        this.map = map;
        currentFile = job.tempDirURI.resolve(map);

        logger.info("Processing " + currentFile);
        final Document doc;
        try {
            logger.debug("Reading " + currentFile);
            doc = builder.parse(new InputSource(currentFile.toString()));
        } catch (final SAXException | IOException e) {
            logger.error("Failed to parse " + currentFile, e);
            return;
        }

        logger.debug("Filter topics and generate copies");
        generateCopies(doc.getDocumentElement(), Collections.emptyList());
        logger.debug("Filter existing topics");
        filterTopics(doc.getDocumentElement(), Collections.emptyList());

        logger.debug("Writing " + currentFile);
        Result result = null;
        try {
            Transformer serializer = TransformerFactory.newInstance().newTransformer();
            result = new StreamResult(currentFile.toString());
            serializer.transform(new DOMSource(doc), result);
        } catch (final TransformerConfigurationException | TransformerFactoryConfigurationError e) {
            throw new RuntimeException(e);
        } catch (final TransformerException e) {
            logger.error("Failed to serialize " + map.toString() + ": " + e.getMessage(), e);
        } finally {
            try {
                close(result);
            } catch (final IOException e) {
                logger.error("Failed to close result stream for " + map.toString() + ": " + e.getMessage(), e);
            }
        }
    }

    private List<FilterUtils> combineFilterUtils(final Element topicref, final List<FilterUtils> filters) {
        final List<Element> ditavalRefs = getChildElements(topicref, DITAVAREF_D_DITAVALREF);
        assert ditavalRefs.size() <= 1;
        if (!ditavalRefs.isEmpty()) {
            List<FilterUtils> fs = new ArrayList<>(filters);
            final FilterUtils f = getFilterUtils(ditavalRefs.get(0));
            if (f != null) {
                fs = new ArrayList<>(fs);
                fs.add(f);
            }
            return fs;
        } else {
            return filters;
        }
    }

    /** Copy and filter topics for branches. These topics have a new name and will be added to job configuration. */
    private void generateCopies(final Element topicref, final List<FilterUtils> filters) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters);

        final String copyTo = topicref.getAttribute(BRANCH_COPY_TO);
        if (!copyTo.isEmpty()) {
            final URI dstUri = map.resolve(copyTo);
            final URI dstAbsUri = job.tempDirURI.resolve(dstUri);
            final FileInfo dstFileInfo = job.getFileInfo(dstAbsUri);
            final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
            final URI srcUri = map.resolve(href);
            final URI srcAbsUri = job.tempDirURI.resolve(srcUri);
            final FileInfo srcFileInfo = job.getFileInfo(srcUri);
            if (srcFileInfo != null) {
//                final FileInfo fi = new FileInfo.Builder(srcFileInfo).uri(dstUri).build();
//                 TODO: Maybe Job should be updated earlier?
//                job.add(fi);
                logger.info("Filtering " + srcAbsUri + " to " + dstAbsUri);
                final List<XMLFilter> pipe = new ArrayList<>();
                // TODO: replace multiple profiling filters with a merged filter utils
                for (final FilterUtils f : fs) {
                    final ProfilingFilter writer = new ProfilingFilter();
                    writer.setLogger(logger);
                    writer.setJob(job);
                    writer.setFilterUtils(f);
                    pipe.add(writer);
                }
                final File dstDirUri = new File(dstAbsUri.resolve("."));
                if (!dstDirUri.exists() && !dstDirUri.mkdirs()) {
                    logger.error("Failed to create directory " + dstDirUri);
                }
                try {
                    xmlUtils.transform(srcAbsUri,
                                       dstAbsUri,
                                       pipe);
                } catch (final DITAOTException e) {
                    logger.error("Failed to filter " + srcAbsUri + " to " + dstAbsUri + ": " + e.getMessage(), e);
                }
                topicref.setAttribute(ATTRIBUTE_NAME_HREF, copyTo);
                topicref.removeAttribute(BRANCH_COPY_TO);
                // disable filtering again
                topicref.setAttribute(SKIP_FILTER, Boolean.TRUE.toString());
            }
        }
        for (final Element child: getChildElements(topicref, MAP_TOPICREF)) {
            if (DITAVAREF_D_DITAVALREF.matches(child)) {
                continue;
            }
            generateCopies(child, fs);
        }
    }

    /** Modify and filter topics for branches. These files use an existing file name. */
    private void filterTopics(final Element topicref, final List<FilterUtils> filters) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters);

        final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
        final Attr skipFilter = topicref.getAttributeNode(SKIP_FILTER);
        if (!fs.isEmpty() && skipFilter == null
                && !href.isEmpty()
                && !ATTR_SCOPE_VALUE_EXTERNAL.equals(topicref.getAttribute(ATTRIBUTE_NAME_SCOPE))) {
            final List<XMLFilter> pipe = new ArrayList<>();
            // TODO: replace multiple profiling filters with a merged filter utils
            for (final FilterUtils f : fs) {
                final ProfilingFilter writer = new ProfilingFilter();
                writer.setLogger(logger);
                writer.setJob(job);
                writer.setFilterUtils(f);
                pipe.add(writer);
            }

            final URI srcAbsUri = job.tempDirURI.resolve(map.resolve(href));
            logger.info("Filtering " + srcAbsUri);
            try {
                xmlUtils.transform(toFile(srcAbsUri),
                        pipe);
            } catch (final DITAOTException e) {
                logger.error("Failed to filter " + srcAbsUri + ": " + e.getMessage(), e);
            }
        }
        if (skipFilter != null) {
            topicref.removeAttributeNode(skipFilter);
        }

        for (final Element child: getChildElements(topicref, MAP_TOPICREF)) {
            if (DITAVAREF_D_DITAVALREF.matches(child)) {
                continue;
            }
            filterTopics(child, fs);
        }
    }

    /**
     * Read and cache filter.
     **/
    private FilterUtils getFilterUtils(final Element ditavalRef) {
        final URI href = toURI(ditavalRef.getAttribute(ATTRIBUTE_NAME_HREF));
        final URI tmp = currentFile.resolve(href);
        final FileInfo fi = job.getFileInfo(tmp);
        final URI ditaval = fi.src;
        FilterUtils f = filterCache.get(ditaval);
        if (f == null) {
            ditaValReader.filterReset();
            ditaValReader.read(ditaval);
            Map<FilterUtils.FilterKey, FilterUtils.Action> filterMap = ditaValReader.getFilterMap();
            f = new FilterUtils(filterMap);
            f.setLogger(logger);
            filterCache.put(ditaval, f);
        }
        return f;
    }

}
