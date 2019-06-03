/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import java.io.File;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;

/**
 * AbstractReader defines the methods that every reader class should implement.
 *
 * @author Lian, Li
 *
 */
public interface AbstractReader {

    /**
     * Use reader to parse a document.
     *
     * @param filename absolute filename
     */
    void read(File filename) throws DITAOTException;

    /**
     * Set logger for module.
     *
     * @param logger logger to use for log message
     */
    void setLogger(DITAOTLogger logger);

    /**
     * Set job configuration for module
     *
     * @param job job configuration to use for processing
     */
    void setJob(Job job);

}
