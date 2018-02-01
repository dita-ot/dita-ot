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
 * Sets a property to the base name of a specified file, optionally minus a
 * suffix.
 *
 * This task can accept the following attributes:
 * <ul>
 * <li>file
 * <li>property
 * <li>suffix
 * </ul>
 * The <b>file</b> and <b>property</b> attributes are required. The
 * <b>suffix</b> attribute can be specified either with or without
 * the &quot;.&quot;, and the result will be the same (ie., the
 * returned file name will be minus the .suffix).
 * <p>
 * When this task executes, it will set the specified property to the
 * value of the last element in the specified file. If file is a
 * directory, the basename will be the last directory element. If file
 * is a full-path filename, the basename will be the simple file name.
 * If a suffix is specified, and the specified file ends in that suffix,
 * the basename will be the simple file name without the suffix.
 *
 *
 * @since Ant 1.5
 *
 */

public class UriBasenameTask extends Task {

    private URI file;
    private String property;
    private String suffix;

    /**
     * File or directory to get base name from
     */
    public void setFile(final URI file) {
        this.file = file;
    }

    /**
    * Property to set base name to.
    */
    public void setProperty(final String property) {
        this.property  = property;
    }

    /**
    * Suffix to remove from base name.
    */
    public void setSuffix(final String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void execute() throws BuildException {
        if (property == null) {
            throw new BuildException("property attribute required", getLocation());
        }
        if (file == null) {
            throw new BuildException("file attribute required", getLocation());
        }
        String value = getName(file.toString());
        if (suffix != null && value.endsWith(suffix)) {
            // if the suffix does not starts with a '.' and the
            // char preceding the suffix is a '.', we assume the user
            // wants to remove the '.' as well (see docs)
            int pos = value.length() - suffix.length();
            if (pos > 0 && suffix.charAt(0) != '.'
                && value.charAt(pos - 1) == '.') {
                pos--;
            }
            value = value.substring(0, pos);
        } else if (suffix != null && suffix.equals(".*") && value.indexOf('.') != -1) {
            value = value.substring(0, value.indexOf('.'));
        }
        getProject().setNewProperty(property, value);
    }

    private String getName(final String path) {
        final int i = path.lastIndexOf(URI_SEPARATOR);
        if (i != -1) {
            return path.substring(i + 1);
        } else {
            return path;
        }
    }

}

