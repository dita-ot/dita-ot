/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import org.dita.dost.module.Content;

/**
 * @author Lian, Li
 * 
 */
public abstract class AbstractReader {

    public abstract void read(String filename);

    public abstract Content getContent();

}
