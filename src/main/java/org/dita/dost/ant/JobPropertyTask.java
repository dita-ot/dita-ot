/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import static org.dita.dost.ant.ExtensibleAntInvoker.getJob;

import java.io.File;
import java.util.Map;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.util.Job;

/**
 * Read job configuration and set Ant properties.
 */
public final class JobPropertyTask extends Task {

    @Override
    public void execute() throws BuildException {
        final Job job = getJob(getProject());
        for (final Map.Entry<String, String> e: job.getProperties().entrySet()) {
            getProject().setProperty(e.getKey(), e.getValue());
        }
    }

    /**
     * Set directory where to read job configuration
     */
    @Deprecated
    public void setDir(final File dir) {
        // NOOP
    }

}