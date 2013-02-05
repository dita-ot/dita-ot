/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job;
import org.dita.dost.writer.ImageMetadataFilter;

/**
 * Image metadata module.
 *
 */
final class ImageMetadataModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    /**
     * Constructor.
     */
    public ImageMetadataModule() {
        super();
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Entry point of Coderef Module.
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }

        Job job = null;
        try{
            job = new Job(tempDir);
        }catch(final IOException e){
            throw new DITAOTException(e);
        }

        final Set<String> codereflist=job.getSet(FULL_DITA_TOPIC_LIST);
        final ImageMetadataFilter writer = new ImageMetadataFilter(new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR)),
                                                                       tempDir,
                                                                       job.getProperty("uplevels"));
        writer.setLogger(logger);
        for (final String fileName: codereflist) {
            writer.write(new File(tempDir,fileName).getAbsolutePath());
        }

        return null;
    }

}
