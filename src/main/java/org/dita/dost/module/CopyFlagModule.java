/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_DITAOUTPUTDIR;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;

/**
 * Copy DITAVAL flag module.
 */
final class CopyFlagModule extends AbstractPipelineModuleImpl {

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(f -> f.isFlagImage);
        if (!fis.isEmpty()) {
            try {
                final FileUtils fileUtils = FileUtils.newFileUtils();
                final File outputDirFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_DITAOUTPUTDIR));
                if (!outputDirFile.exists()) {
                    outputDirFile.mkdir();
                }

                final URI outputDirURI = URLUtils.toURI(outputDirFile);
                for (final FileInfo f: fis) {
                    final File srcFile = new File(f.src);
                    final File destFile = new File(outputDirURI.resolve(f.uri));
                    if (srcFile.exists() && destFile != null && !destFile.exists()) {
                        fileUtils.copyFile(srcFile, destFile);
                    }
                }
            } catch (final IOException e) {
                    throw new BuildException(e.getMessage(), e);
            }
        }
        return null;
    }

}
