/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.CoderefResolver;
/**
 * Coderef Module class.
 *
 */
final class CoderefModule extends AbstractPipelineModuleImpl {

    /**
     * Constructor.
     */
    public CoderefModule() {
        super();
    }

    /**
     * Entry point of Coderef Module.
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

        final CoderefResolver writer = new CoderefResolver();
        writer.setLogger(logger);
        for (final FileInfo f: job.getFileInfo()) {
            if (f.hasCoderef) {
                //FIXME:This writer deletes and renames files, have to
                writer.write(new File(tempDir, f.file.getPath()).getAbsoluteFile());
            }
        }

        return null;
    }

}
