/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.module.Content;

/**
 * @author Lian, Li
 * 
 */
public abstract class AbstractWriter {

    public abstract void setContent(Content content);

    public abstract void write(String filename);

}
