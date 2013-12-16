/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ImageMetadataFilter;

/**
 * Image metadata module.
 *
 */
final class ImageMetadataModule extends AbstractPipelineModuleImpl {

    /**
     * Constructor.
     */
    public ImageMetadataModule() {
        super();
    }

    /**
     * Entry point of image metadata Module.
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }

        final ImageMetadataFilter writer = new ImageMetadataFilter(new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR)), job);
        writer.setLogger(logger);
        for (final FileInfo f: job.getFileInfo()) {
            if (!f.isResourceOnly && (ATTR_FORMAT_VALUE_DITA.equals(f.format) || f.isChunked || f.isChunkedDitaMap)) {
                writer.write(new File(tempDir, f.file.getPath()).getAbsoluteFile());
            }
        }

        return null;
    }

}
