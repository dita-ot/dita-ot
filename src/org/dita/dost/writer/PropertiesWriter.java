/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.FileOutputStream;
import java.util.Properties;

import org.dita.dost.module.Content;


/**
 * Class description goes here.
 * 
 * @version 1.0 2005-2-2
 * @author Charlie Wu
 */
public class PropertiesWriter extends AbstractWriter {
    private Properties prop = null;
    
    /* (non-Javadoc)
     * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
     */
    public void setContent(Content content) {        
        prop = (Properties) content.getObject();
    }

    /* (non-Javadoc)
     * @see org.dita.dost.writer.AbstractWriter#write(java.lang.String)
     */
    public void write(String filename) {        
        try {           
            prop.store(new FileOutputStream(filename), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
