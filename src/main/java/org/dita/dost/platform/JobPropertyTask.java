/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.invoker.ExtensibleAntInvoker.getJob;

import java.io.File;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.util.Job;

/**
 * Read job configuration and set Ant properties.
 */
public final class JobPropertyTask extends Task {

    private File dir;

    @Override
    public void execute() throws BuildException {
        final Job job = getJob(dir, getProject());
        for (final Map.Entry<String, String> e: job.getProperties().entrySet()) {
            getProject().setProperty(e.getKey(), e.getValue());
        }
    }

    /**
     * Set directory where to read job configuration
     */
    public void setDir(final File dir) {
        this.dir = dir;
    }

}