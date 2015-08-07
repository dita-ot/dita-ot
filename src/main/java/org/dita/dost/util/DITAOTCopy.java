/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

/**
 * Class description goes here.
 *
 * @author Wu, Zhi Qiang
 */
public final class DITAOTCopy extends Task {
    
    private String includes = null;
    private File includesFile = null;
    private String relativePaths = null;
    /** Destination directory */
    private File destDir = null;

    /**
     * Default Constructor.
     * 
     */
    public DITAOTCopy(){
    }

    /**
     * Set the copy files.
     * @param incld The includes to set.
     */
    public void setIncludes(final String incld) {
        includes = incld;
    }
    
    /**
     * Set the copy files list file.
     * @param includesFile list file for includes to set.
     */
    public void setIncludesfile(final File includesFile) {
        this.includesFile = includesFile;
    }

    /**
     * Set the destination directory.
     * @param destdir the destination directory.
     */
    public void setTodir(final File destdir) {
        destDir = destdir;
    }

    /**
     * Set the relative path from output directory.
     * @param relPaths the relative path .
     */
    public void setRelativePaths(final String relPaths) {
        if (!relPaths.trim().isEmpty()) {
            relativePaths = relPaths;
        }
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        if (includes == null && includesFile == null) {
            return;
        }
        if (destDir == null) {
            throw new BuildException("Destination directory not defined");
        }
        if (!destDir.exists()) {
            throw new BuildException("Destination directory " + destDir + " does not exists");
        }
        try {
            final FileUtils fileUtils = FileUtils.newFileUtils();
            final List<String> incs = getIncludes();
            if (relativePaths == null) {
                for (final String inc: incs) {
                    final File srcFile = new File(inc);
                    if (srcFile.exists()) {
                        final File destFile = new File(destDir, srcFile.getName());
                        fileUtils.copyFile(srcFile, destFile);
                    }
                }
            } else {
                for (final String inc: incs) {
                    final File srcFile = new File(inc);
                    File destFile = null;
                    for (final String rel: relativePaths.split(COMMA)) {
					    final File temp = new File(destDir, rel);
						if (temp.getName().equalsIgnoreCase(srcFile.getName())) {
							destFile = temp;
							break;
						}
					}
                    if (srcFile.exists() && destFile != null) {                      
                        fileUtils.copyFile(srcFile, destFile);
                    }
                }
            }
        } catch (final IOException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }

    private List<String> getIncludes() throws IOException {
        if (includes == null && includesFile == null) {
            return Collections.emptyList();
        }
        if (includesFile != null) {
            final List<String> res = new ArrayList<>();
            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(includesFile));
                String line = null;
                while ((line = r.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        res.add(line.trim());
                    }
                }
            } finally {
                if (r != null) {
                    r.close();
                }
            }
            return res;
        } else {
            return Arrays.asList(includes.split(COMMA));
        }
    }
    
}