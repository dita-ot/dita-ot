/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.XMLFilter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Map processes topics through XML filters. Filters are reused and should reset internal state on
 * {@code startDocument} event.
 */
public final class XmlFilterModule extends AbstractPipelineModuleImpl {

    private List<AbstractXMLFilter> pipe;
    private Filter<FileInfo> fileInfoFilter;

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
            final URI file = job.tempDir.toURI().resolve(f.uri);
            logger.info("Processing " + file);
            try {
                XMLUtils.transform(file, getProcessingPipe(file));
            } catch (final DITAOTException e) {
                logger.error("Failed to process XML filter: " + e.getMessage(), e);
            }
        }
        return null;
    }

    public void setProcessingPipe(final List<AbstractXMLFilter> pipe) {
        this.pipe = pipe;
    }

    /**
     * Get pipe line filters
     *
     * @param fileToParse absolute URI to current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        assert fileToParse.isAbsolute();
        final List<XMLFilter> res = new ArrayList<>();
        for (final AbstractXMLFilter f: pipe) {
            logger.debug("Configure filter " + f.getClass().getCanonicalName());
            f.setCurrentFile(fileToParse);
            f.setJob(job);
            f.setLogger(logger);
            res.add(f);
        }
        return res;
    }

    public void setFileInfoFilter(final Filter<FileInfo> fileInfoFilter) {
        this.fileInfoFilter = fileInfoFilter;
    }
}
