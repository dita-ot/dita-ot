/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.resolver;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * URIResolverConfigTask to setup DitaURIResolverFactory.
 * @author Alan
 */
public final class URIResolverConfigTask extends Task {
    private String basedir = null;
    private String tempdir = null;
    /**
     * Construct a new instance of URIResolverConfigTask.
     */
    public URIResolverConfigTask() {
        // nop
    }

    @Override
    public void execute() throws BuildException {

        String path=tempdir;
        if(!new File(tempdir).isAbsolute()){
            path=new File(basedir,tempdir).getAbsolutePath();
        }
        DitaURIResolverFactory.setPath(path);
        // If you wants to replace the default resolver
        // DitaURIResolverFactory.setURIResolver(/*? extends URIResolver*/
        // resolver);
    }
    /**
     * Get basedir.
     * @return base dir
     */
    public String getBasedir() {
        return basedir;
    }
    /**
     * Set basedir.
     * @param basedir basedir
     */
    public void setBasedir(final String basedir) {
        this.basedir = basedir;
    }
    /**
     * Get tempdir.
     * @return temp dir
     */
    public String getTempdir() {
        return tempdir;
    }
    /**
     * Set tempdir.
     * @param tempdir tempdir
     */
    public void setTempdir(final String tempdir) {
        this.tempdir = tempdir;
    }

}
