/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
    private File basedir = null;
    private File tempdir = null;

    @Override
    public void execute() throws BuildException {

        File path=tempdir;
        if(!tempdir.isAbsolute()){
            if (basedir == null) {
                basedir = getProject().getBaseDir();
            }
            path=new File(basedir,tempdir.getPath());
        }
        DitaURIResolverFactory.setPath(path.getAbsolutePath());
        // If you wants to replace the default resolver
        // DitaURIResolverFactory.setURIResolver(/*? extends URIResolver*/
        // resolver);
    }

    /**
     * Set basedir.
     * @param basedir basedir
     */
    @Deprecated
    public void setBasedir(final File basedir) {
        this.basedir = basedir;
    }
    
    /**
     * Set tempdir.
     * @param tempdir tempdir
     */
    public void setTempdir(final File tempdir) {
        this.tempdir = tempdir;
    }

}
