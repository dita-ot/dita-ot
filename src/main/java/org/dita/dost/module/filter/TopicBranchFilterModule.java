/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module.filter;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ProfilingFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.XMLFilter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.singletonList;
import static org.dita.dost.util.Constants.*;
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
public final class TopicBranchFilterModule extends AbstractBranchFilterModule {

    private static final String SKIP_FILTER = "skip-filter";
    private static final String BRANCH_COPY_TO = "filter-copy-to";

    /** Current map being processed, relative to temporary directory */
    private URI map;
    private final Set<URI> filtered = new HashSet<>();

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
        processMap(in.uri);

        addFlagImagesSetToProperties(job, relFlagImagesSet);

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
            doc = job.getStore().getDocument(currentFile);
        } catch (final IOException e) {
            logger.error("Failed to parse " + currentFile, e);
            return;
        }

        final SubjectScheme subjectSchemeMap = getSubjectScheme(doc.getDocumentElement());
        logger.debug("Filter topics and generate copies");
        generateCopies(doc.getDocumentElement(), Collections.emptyList(), subjectSchemeMap);
        logger.debug("Filter existing topics");
        filterTopics(doc.getDocumentElement(), Collections.emptyList(), subjectSchemeMap);

        logger.debug("Writing " + currentFile);
        try {
            job.getStore().writeDocument(doc, currentFile);
        } catch (final IOException e) {
            logger.error("Failed to serialize " + map.toString() + ": " + e.getMessage(), e);
        }
    }

    /** Copy and filter topics for branches. These topics have a new name and will be added to job configuration. */
    private void generateCopies(final Element topicref, final List<FilterUtils> filters,
                                final SubjectScheme subjectSchemeMap) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters, subjectSchemeMap);

        final String copyTo = topicref.getAttribute(BRANCH_COPY_TO);
        if (!copyTo.isEmpty()) {
            final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
            final URI srcUri = map.resolve(href);
            final URI srcAbsUri = job.tempDirURI.resolve(srcUri);
            final FileInfo srcFileInfo = job.getFileInfo(srcUri);
            if (srcFileInfo != null) {
                final URI dstUri = map.resolve(copyTo);
                final URI dstAbsUri = job.tempDirURI.resolve(dstUri);
                final FileInfo dstFileInfo = job.getFileInfo(dstAbsUri);
                if (dstFileInfo != null) {
                    final FileInfo updatedDstFileInfo = new FileInfo.Builder(dstFileInfo)
                            .addContentFields(srcFileInfo)
                            .build();
                    job.add(updatedDstFileInfo);
                }
                logger.info("Filtering " + srcAbsUri + " to " + dstAbsUri);
                final ProfilingFilter writer = new ProfilingFilter();
                writer.setLogger(logger);
                writer.setJob(job);
                writer.setFilterUtils(fs);
                writer.setCurrentFile(dstAbsUri);
                final List<XMLFilter> pipe = singletonList(writer);

                final File dstDirUri = new File(dstAbsUri.resolve("."));
                if (!dstDirUri.exists() && !dstDirUri.mkdirs()) {
                    logger.error("Failed to create directory " + dstDirUri);
                }
                try {
                    job.getStore().transform(srcAbsUri, dstAbsUri, pipe);
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
            generateCopies(child, fs, subjectSchemeMap);
        }
    }

    /** Modify and filter topics for branches. These files use an existing file name. */
    private void filterTopics(final Element topicref, final List<FilterUtils> filters,
                              final SubjectScheme subjectSchemeMap) {
        final List<FilterUtils> fs = combineFilterUtils(topicref, filters, subjectSchemeMap);

        final String href = topicref.getAttribute(ATTRIBUTE_NAME_HREF);
        final Attr skipFilter = topicref.getAttributeNode(SKIP_FILTER);
        final URI srcAbsUri = job.tempDirURI.resolve(map.resolve(href));
        if (!fs.isEmpty() && skipFilter == null
                && !filtered.contains(srcAbsUri)
                && !href.isEmpty()
                && !ATTR_SCOPE_VALUE_EXTERNAL.equals(topicref.getAttribute(ATTRIBUTE_NAME_SCOPE))) {
            final ProfilingFilter writer = new ProfilingFilter();
            writer.setLogger(logger);
            writer.setJob(job);
            writer.setFilterUtils(fs);
            writer.setCurrentFile(srcAbsUri);
            final List<XMLFilter> pipe = singletonList(writer);

            logger.info("Filtering " + srcAbsUri);
            try {
                job.getStore().transform(srcAbsUri, pipe);
            } catch (final DITAOTException e) {
                logger.error("Failed to filter " + srcAbsUri + ": " + e.getMessage(), e);
            }
            filtered.add(srcAbsUri);
        }
        if (skipFilter != null) {
            topicref.removeAttributeNode(skipFilter);
        }

        for (final Element child: getChildElements(topicref, MAP_TOPICREF)) {
            if (DITAVAREF_D_DITAVALREF.matches(child)) {
                continue;
            }
            filterTopics(child, fs, subjectSchemeMap);
        }
    }

}
