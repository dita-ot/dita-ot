/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.*;
import org.dita.dost.invoker.ExtensibleAntInvoker;

import java.io.File;
import java.net.URI;

import static org.dita.dost.util.Constants.ANT_TEMP_DIR;
import static org.dita.dost.util.URLUtils.toFile;
import static org.dita.dost.util.URLUtils.toURI;

/**
 * File mapper that uses job configuration's {@link org.dita.dost.util.Job.FileInfo#result result}
 * to resolve output file. If {@link org.dita.dost.util.Job.FileInfo#result result} is not defined,
 * the original output filename is used.
 *
 * @since 3.0
 */
public class JobMapper implements FileNameMapper {

    private Job job;
    private String extension;

    public void setProject(Project project) {
        File tempDir = new File(project.getProperty(ANT_TEMP_DIR));
        if (!tempDir.isAbsolute()) {
            tempDir = new File(project.getBaseDir(), tempDir.getPath());
        }
        job = ExtensibleAntInvoker.getJob(tempDir, project);
    }

    @Override
    public void setFrom(String from) {
        // NOOP
    }

    @Override
    public void setTo(String extension) {
        // FIXME this should use an extra attribute `extension`, but Ant doesn't support it
        this.extension = extension.charAt(0) == '.' ? extension : ("." + extension);
    }

    @Override
    public String[] mapFileName(String sourceFileName) {
        final Job.FileInfo fi = job.getFileInfo(toURI(sourceFileName));
        final String res;
        if (fi.result == null) {
            res = sourceFileName;
        } else {
            final URI base = toURI(job.getProperty("user.input.dir.uri"));
            final URI rel = base.relativize(fi.result);
            res = toFile(rel).getPath();
        }
        return new String[]{extension != null ? (FilenameUtils.removeExtension(res) + extension) : res};
    }
}
