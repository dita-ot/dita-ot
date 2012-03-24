/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.dita.dost.log.DITAOTJavaLogger;

/**
 * GenUtils class writing xml contents into xml files.
 *
 * @since 1.4
 * @deprecated use {@link XMLSerializer} instead
 */
@Deprecated
public final class GenUtils {

    private static StringBuffer buffer;
    private static String outputFile;
    private static DITAOTJavaLogger logger = new DITAOTJavaLogger();
    /**
     * Constructor.
     */
    public GenUtils() {
        super();
    }
    /**
     * Clear up.
     */
    public static void clear(){
        buffer = new StringBuffer();
        buffer.append(XML_HEAD);
    }
    /**
     * Set output file.
     * @param file file
     */
    public static void setOutput(final String file){
        outputFile = file;
    }
    /**
     * Set start element.
     * @param name element name
     */
    public static void startElement(final String name){
        buffer.append("<").append(name).append(">");
    }
    /**
     * Set end element.
     * @param name element name
     */
    public static void endElement(final String name){
        buffer.append("</").append(name).append(">");
    }
    /**
     * Add attribute.
     * @param name attribute name
     * @param value attribute value
     */
    public static void addAttr(final String name, final String value){
        buffer.insert(buffer.lastIndexOf(">")," "+name+"=\""+value+"\"");
    }
    /**
     * Added text.
     * @param text text
     */
    public static void addText(final String text){
        buffer.append(text);
    }
    /**
     * write into file.
     */
    public static void flush(){
        OutputStreamWriter output = null;
        try{
            output = new OutputStreamWriter(
                    new FileOutputStream(new File(outputFile)),UTF8);
            output.write(buffer.toString());
            output.flush();
        } catch (final Exception e) {
            logger.logException(e);
        }finally {
            try{
                output.close();
            } catch (final Exception e) {
                logger.logException(e);
            }
        }
    }

}
