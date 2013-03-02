/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;

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
     * @param filename filename
     */
    void read(String filename);

    /**
     * Get the result from reader after parsing.
     * 
     * @return Content
     */
    Content getContent();

    /**
     * Set logger for module.
     * 
     * @param logger logger to use for log message
     */
    public void setLogger(DITAOTLogger logger);

}
