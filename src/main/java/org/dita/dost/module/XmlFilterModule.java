/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.XMLFilter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/**
 * Map processes topics through XML filters. Filters are reused and should reset internal state on
 * {@code startDocument} event.
 */
public final class XmlFilterModule extends AbstractPipelineModuleImpl {

    private final XMLUtils xmlUtils = new XMLUtils();
    private List<FilterPair> pipe;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        xmlUtils.setLogger(logger);
    }

    /**
     * Filter files through XML filters.
     * 
     * @param input Input parameters and resources.
     * @return always returns {@code null}
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(fileInfoFilter);
        for (final FileInfo f: fis) {
            final URI file = job.tempDirURI.resolve(f.uri);
            logger.info("Processing " + file);
            try {
                xmlUtils.transform(file, getProcessingPipe(f));
            } catch (final DITAOTException e) {
                logger.error("Failed to process XML filter: " + e.getMessage(), e);
            }
        }
        return null;
    }

    public void setProcessingPipe(final List<FilterPair> pipe) {
        this.pipe = pipe;
    }

    /**
     * Get pipe line filters
     *
     * @param fi current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final FileInfo fi) {
        final URI fileToParse = job.tempDirURI.resolve(fi.uri);
        assert fileToParse.isAbsolute();
        final List<XMLFilter> res = new ArrayList<>();
        for (final FilterPair p: pipe) {
            if (p.predicate.test(fi)) {
                final AbstractXMLFilter f = p.filter;
                logger.debug("Configure filter " + f.getClass().getCanonicalName());
                f.setCurrentFile(fileToParse);
                f.setJob(job);
                f.setLogger(logger);
                res.add(f);
            }
        }
        return res;
    }

    /**
     * SAX filter with file predicate.
     */
    public static class FilterPair {
        public final AbstractXMLFilter filter;
        public final Predicate<FileInfo> predicate;

        public FilterPair(final AbstractXMLFilter filter, final Predicate<FileInfo> fileInfoFilter) {
            this.filter = filter;
            this.predicate = fileInfoFilter;
        }
    }

}
