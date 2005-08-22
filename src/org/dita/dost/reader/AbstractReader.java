/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import org.dita.dost.module.Content;

/**
 * AbstractReader defines the methods that every reader class should implement.
 * 
 * @author Lian, Li
 * 
 */
public abstract class AbstractReader {

    /**
     * Use reader to parse a document.
     * 
     * @param filename
     */
    public abstract void read(String filename);

    /**
     * Get the result from reader after parsing.
     * 
     * @return Content
     */
    public abstract Content getContent();

}
