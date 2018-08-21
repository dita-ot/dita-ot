/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import java.io.File;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;

/**
 * AbstractWriter defines the method every writer class should implement.
 *
 * @author Lian, Li
 *
 */
public interface AbstractWriter {

    /**
     * Call the writer to write or rewrite the file.
     *
     * @param filename system path to process
     * @throws DITAOTException DITAOTException
     */
    void write(File filename) throws DITAOTException;

    /**
     * Set logger for module.
     *
     * @param logger logger to use for log message
     */
    void setLogger(DITAOTLogger logger);

    /**
     * Set Job for module
     *
     * @param job Job configuration to use for processing
     */
    void setJob(Job job);

}
