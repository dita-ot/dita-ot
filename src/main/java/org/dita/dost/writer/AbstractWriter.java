/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;

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
    public void setLogger(DITAOTLogger logger);

}
