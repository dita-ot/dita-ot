/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ProfilingFilter;

/**
 * Filter module class.
 */
final class FilterModule extends AbstractPipelineModuleImpl {

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }

        final Collection<FileInfo> files = job.getFileInfo();
        
        final ProfilingFilter writer = new ProfilingFilter();
        writer.setLogger(logger);
        final FilterUtils filterUtils = parseFilterFile(input.getAttribute(ANT_INVOKER_PARAM_DITAVAL));
        writer.setFilterUtils(filterUtils);
        writer.setTranstype(input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE));
        for (final FileInfo f: files) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format) || ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
                final File file = new File(tempDir, f.file.getPath());
                logger.logInfo("Processing " + file.getAbsolutePath());
                try {
                    writer.write(file.getAbsoluteFile());
                    if (!writer.hasElementOutput()) {
                        logger.logInfo("All content in " + file.getAbsolutePath() + " was filtered out");
                        job.remove(f);
                        FileUtils.delete(file);
                    }
                } catch (final Exception e) {
                    logger.logError("Failed to profile " + file.getAbsolutePath() + ": " + e.getMessage());
                }
            }
        }

        try {
            job.write();
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }
        
        return null;
    }

    /**
     * Parse filter file. From GenMapAndTopicListModule.
     * 
     * @return configured filter utility
     */
    private FilterUtils parseFilterFile(final String ditavalFile) {
        
        final FilterUtils filterUtils = new FilterUtils();
        filterUtils.setLogger(logger);
        
        if (ditavalFile != null) {
            final DitaValReader ditaValReader = new DitaValReader();
            ditaValReader.setLogger(logger);
            ditaValReader.initXMLReader(true);

            ditaValReader.read(new File(ditavalFile).getAbsoluteFile());
            filterUtils.setFilterMap(ditaValReader.getFilterMap());
        } else {
            filterUtils.setFilterMap(null);
        }
        return filterUtils;
    }
    
}
