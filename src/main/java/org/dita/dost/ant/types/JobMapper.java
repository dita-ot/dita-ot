/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant.types;

import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.*;
import org.dita.dost.ant.ExtensibleAntInvoker;
import org.dita.dost.util.Job;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

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

    private enum Type {
        TEMP, RESULT
    }

    private Type type = Type.RESULT;
    private Job job;
    private String extension;

    public void setProject(Project project) {
        job = ExtensibleAntInvoker.getJob(project);
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

    public void setExtension(String extension) {
        this.extension = extension.charAt(0) == '.' ? extension : ("." + extension);
    }

    public void setType(TypeAttribute attr) {
        type = Type.valueOf(attr.getValue().toUpperCase());
    }

    @Override
    public String[] mapFileName(String sourceFileName) {
        final URI uri = toURI(sourceFileName);
        Job.FileInfo fi = job.getFileInfo(uri);
        if (fi == null) {
            fi = job.getFileInfo(job.getInputDir().resolve(uri));
        }
        final String res;
        switch (type) {
            case TEMP:
                res = fi.file.getPath();
                break;
            case RESULT:
                if (fi.result == null) {
                    res = sourceFileName;
                } else {
                    final URI base = job.getInputDir();
                    final URI rel = base.relativize(fi.result);
                    res = toFile(rel).getPath();
                }
                break;
            default:
                throw new IllegalArgumentException();
        }
        return new String[]{extension != null ? (FilenameUtils.removeExtension(res) + extension) : res};
    }

    public static class TypeAttribute extends EnumeratedAttribute {
        @Override
        public String[] getValues() {
            return Arrays.stream(Type.values())
                    .map(t -> t.toString().toLowerCase())
                    .collect(Collectors.toList())
                    .toArray(new String[Type.values().length]);
        }
    }
}
