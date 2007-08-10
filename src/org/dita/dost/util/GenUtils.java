/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.dita.dost.log.DITAOTJavaLogger;

public class GenUtils {

	private static StringBuffer buffer;
	private static String outputFile;
	private static DITAOTJavaLogger logger = new DITAOTJavaLogger();
	
	public GenUtils() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public static void clear(){
		buffer = new StringBuffer();
		buffer.append(Constants.XML_HEAD);
	}
	
	public static void setOutput(String file){
		outputFile = file;
	}
	
	public static void startElement(String name){
		buffer.append("<").append(name).append(">");
	}
	
	public static void endElement(String name){
		buffer.append("</").append(name).append(">");
	}
	
	public static void addAttr(String name, String value){
		buffer.insert(buffer.lastIndexOf(">")," "+name+"=\""+value+"\"");
	}
	
	public static void addText(String text){
		buffer.append(text);
	}
	
	public static void flush(){
		OutputStreamWriter output = null;
		try{
			output = new OutputStreamWriter(
					new FileOutputStream(new File(outputFile)),Constants.UTF8);
			output.write(buffer.toString());
			output.flush();
		} catch (Exception e) {
        	logger.logException(e);
        }finally {
            try{
                output.close();
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
	}

}
