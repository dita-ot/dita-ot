/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.exception;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * SAXExceptionWrapper which wrapper the SAXParseException
 * @author wxzhang
 *
 */
public class SAXExceptionWrapper extends SAXParseException {

	String messageWithLocation=null;
	SAXParseException saxParseExcetpion =null;
	String sourceFile=null;
	/**
	 * @param message
	 * @param locator
	 */
	public SAXExceptionWrapper(String message, Locator locator) {
		super(message, locator);
	}

	/**
	 * @param message
	 * @param locator
	 * @param e
	 */
	public SAXExceptionWrapper(String message, Locator locator, Exception e) {
		super(message, locator, e);
	}

	/**
	 * @param message
	 * @param publicId
	 * @param systemId
	 * @param lineNumber
	 * @param columnNumber
	 */
	public SAXExceptionWrapper(String message, String publicId,
			String systemId, int lineNumber, int columnNumber) {
		super(message, publicId, systemId, lineNumber, columnNumber);
	}

	/**
	 * @param message
	 * @param publicId
	 * @param systemId
	 * @param lineNumber
	 * @param columnNumber
	 * @param e
	 */
	public SAXExceptionWrapper(String message, String publicId,
			String systemId, int lineNumber, int columnNumber, Exception e) {
		super(message, publicId, systemId, lineNumber, columnNumber, e);
	}
	
	public SAXExceptionWrapper(String file,SAXParseException inner){
		super(inner.getMessage(), inner.getPublicId(), inner.getSystemId(), inner.getLineNumber(), inner.getColumnNumber(), inner.getException());
		saxParseExcetpion=inner;
		sourceFile=file;
	}
	/**
	 * Retrieve the error message
	 * @param none
	 * @return String
	 */
	public String getMessage(){
		StringBuffer buff=new StringBuffer();
		buff.append(sourceFile + " ");
		buff.append("Line "+saxParseExcetpion.getLineNumber());
		buff.append(":");
		buff.append(saxParseExcetpion.getMessage());
		buff.append(System.getProperty("line.separator"));

		return buff.toString();
	}

}
