/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;

/**
 * AbstractWriter defines the method every writer class should implement.
 * 
 * @author Lian, Li
 * 
 */
public interface AbstractWriter {

    /**
     * Set the result from reader to writer.
     * 
     * @param content container
     * 
     */
    void setContent(Content content);

    /**
     * Call the writer to write or rewrite the file.
     * 
     * @param filename system path to process, may contain a pseudo-fragment part
     * @throws DITAOTException DITAOTException
     */
    void write(String filename) throws DITAOTException;

    /**
     * Set logger for module.
     * 
     * @param logger logger to use for log message
     */
    public void setLogger(DITAOTLogger logger);

}
