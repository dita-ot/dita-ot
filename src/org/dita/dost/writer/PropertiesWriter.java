/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;

/**
 * This class extends AbstractWriter, used to output content to properites file.
 * 
 * @version 1.0 2005-05-11
 * 
 * @author Wu, Zhi Qiang
 * @deprecated use {@link org.dita.dost.util.Job#write()} instead
 */
@Deprecated
public final class PropertiesWriter implements AbstractWriter {
    /** Properties used to output */
    private Properties prop = null;
    private DITAOTLogger logger;

    public void setContent(final Content content) {
        prop = (Properties) content.getValue();
    }

    public void write(final String filename) throws DITAOTException {
        FileOutputStream fileOutputStream = null;

        try {
            fileOutputStream = new FileOutputStream(filename);
            prop.store(fileOutputStream, null);
            fileOutputStream.flush();
            //Added by William on 2010-07-23 for bug:3033141 start
            fileOutputStream.close();
            //Added by William on 2010-07-23 for bug:3033141 end
        } catch (final Exception e) {
            throw new DITAOTException(e);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (final Exception e) {
                    throw new DITAOTException(e);
                }
            }
        }
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Write into xml file.
     * @param filename xml file name
     * @throws DITAOTException DITAOTException
     */
    public void writeToXML(final String filename) throws DITAOTException{
        FileOutputStream os=null;
        //new dita.xml file
        try{
            os=new FileOutputStream(filename);
            prop.storeToXML(os, null);
            //Added by William on 2010-07-23 for bug:3033141 start
            os.flush();
            os.close();
            //Added by William on 2010-07-23 for bug:3033141 end
        }catch(final IOException ioe){
            throw new DITAOTException(ioe);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (final IOException e) {
                    logger.logException(e);
                }
            }
        }
    }
}
