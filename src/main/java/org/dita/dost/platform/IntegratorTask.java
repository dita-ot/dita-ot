/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

import org.dita.dost.log.DITAOTAntLogger;

/**
 * Task run by ant scripts, invoking Task.
 * @author Zhang, Yuan Peng
 */
public final class IntegratorTask extends Task {

    private final Integrator adaptee;
    private File ditaDir;

    /**
     * Default Constructor.
     */
    public IntegratorTask() {
        adaptee = new Integrator();
    }

    @Override
    public void execute() throws BuildException {
        final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
        logger.setTarget(getOwningTarget());
        logger.setTask(this);
        adaptee.setLogger(logger);
        adaptee.setDitaDir(ditaDir != null ? ditaDir : getProject().getBaseDir());
        try {
            adaptee.execute();
        } catch (final Exception e) {
            throw new BuildException("Integration failed: " + e.getMessage());
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
        adaptee.setProperties(propertiesFile);
    }

    /**
     * Setter for strict/lax mode.
     * @param strict {@code true} for strict mode, {@code false} for lax mode
     */
    public void setStrict(final boolean strict) {
        adaptee.setStrict(strict);
    }

}
