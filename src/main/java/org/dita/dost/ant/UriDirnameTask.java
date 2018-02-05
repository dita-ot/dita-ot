/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import static org.dita.dost.util.Constants.*;

import java.net.URI;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Determines the directory name of the specified file.
 *
 * This task can accept the following attributes:
 * <ul>
 * <li>file
 * <li>property
 * </ul>
 * Both <b>file</b> and <b>property</b> are required.
 * <p>
 * When this task executes, it will set the specified property to the
 * value of the specified file up to, but not including, the last path
 * element. If file is a file, the directory will be the current
 * directory.
 */

public class UriDirnameTask extends Task {

    private URI file;
    private String property;

    /**
     * Path to take the dirname of.
     */
    public void setFile(final URI file) {
        this.file = file;
    }

    /**
     * The name of the property to set.
     */
    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public void execute() throws BuildException {
        if (property == null) {
            throw new BuildException("property attribute required", getLocation());
        }
        if (file == null) {
            throw new BuildException("file attribute required", getLocation());
        } else {
            String value = getParentDir(file.toString());
            getProject().setNewProperty(property, value);
        }
    }

    private String getParentDir(final String path) {
        String value = path;
        if (!value.endsWith(URI_SEPARATOR)) {
            final int i = value.lastIndexOf(URI_SEPARATOR);
            value = value.substring(0, i + 1);
        }
        return value;
    }
}
