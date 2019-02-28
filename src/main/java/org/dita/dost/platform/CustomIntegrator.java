/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import org.dita.dost.log.DITAOTLogger;

import java.io.File;

/**
 * Custom integration processor.
 *
 * @since 3.3
 */
public interface CustomIntegrator {
    /**
     * Set logger
     *
     * @param logger integration log messages
     */
    void setLogger(final DITAOTLogger logger);

    /**
     * Set DITA-OT installation base directory
     *
     * @param ditaDir absolute path to DITA-OT installation directory
     */
    void setDitaDir(final File ditaDir);

    /**
     * Process custom integration process.
     *
     * @throws Exception if integration process fails
     */
    void process() throws Exception;
}
