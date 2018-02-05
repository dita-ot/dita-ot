/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.platform.Integrator;

/**
 * Task run by ant scripts, invoking Task.
 * @author Zhang, Yuan Peng
 */
public final class IntegratorTask extends Task {

    private File propertiesFile;
    private File ditaDir;

    @Override
    public void execute() throws BuildException {
        final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
        logger.setTarget(getOwningTarget());
        logger.setTask(this);
        final Integrator adaptee = new Integrator(ditaDir != null ? ditaDir : getProject().getBaseDir());
        adaptee.setLogger(logger);
        if (propertiesFile != null) {
            adaptee.setProperties(propertiesFile);
        }
        try {
            adaptee.execute();
        } catch (final Exception e) {
            throw new BuildException("Integration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Set the ditaDir.
     * @param ditaDir ditaDir
     */
    @Deprecated
    public void setDitadir(final File ditaDir) {
        if (!ditaDir.isAbsolute()) {
            throw new IllegalArgumentException("ditadir attribute value must be an absolute path: " + ditaDir);
        }
        this.ditaDir = ditaDir;
    }

    /**
     * Set the properties file.
     * @param propertiesFile propertiesFile
     */
    @Deprecated
    public void setProperties(final File propertiesFile) {
        this.propertiesFile = propertiesFile;

    }

}
