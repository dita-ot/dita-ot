/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Filter;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.NormalizeTableFilter;
import org.dita.dost.writer.TopicFragmentFilter;
import org.xml.sax.XMLFilter;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_PROCESSING_MODE;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;

final class TopicFragmentModule extends AbstractPipelineModuleImpl {

    private Configuration.Mode processingMode;

    /**
     * Process topic files for same topic fragments identifiers.
     * 
     * @param input Input parameters and resources.
     * @return always returns {@code null}
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final String mode = input.getAttribute(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE);
        processingMode = mode != null ? Configuration.Mode.valueOf(mode.toUpperCase()) : Configuration.Mode.LAX;

        final Collection<FileInfo> fis = job.getFileInfo(new Filter() {
            @Override
            public boolean accept(final FileInfo f) {
                return ATTR_FORMAT_VALUE_DITA.equals(f.format);
            }
        });
        for (final FileInfo f: fis) {
            final URI file = job.tempDir.toURI().resolve(f.uri);
            logger.info("Processing " + file);
            try {
                XMLUtils.transform(file, getProcessingPipe(file));
            } catch (final DITAOTException e) {
                logger.error("Failed to process same topic fragment identifiers: " + e.getMessage(), e);
            }
        }
        return null;
    }

    /**
     * Get pipe line filters
     *
     * @param fileToParse absolute URI to current file being processed
     */
    private List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        assert fileToParse.isAbsolute();

        final List<XMLFilter> pipe = new ArrayList<>();

        final TopicFragmentFilter filter = new TopicFragmentFilter(ATTRIBUTE_NAME_HREF);
        pipe.add(filter);

        final NormalizeTableFilter normalizeFilter = new NormalizeTableFilter();
        normalizeFilter.setLogger(logger);
        normalizeFilter.setProcessingMode(processingMode);
        pipe.add(normalizeFilter);

        return pipe;
    }

}
