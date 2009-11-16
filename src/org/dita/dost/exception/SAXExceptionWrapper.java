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
 * SAXExceptionWrapper which wrapper the SAXParseException.
 * @author wxzhang
 *
 */
public class SAXExceptionWrapper extends SAXParseException {
	/** generated serial id. */
	private static final long serialVersionUID = -8266265000662519966L;
	/** message & location. */
	String messageWithLocation=null;
	/** saxParseExcetpion. */
	SAXParseException saxParseExcetpion =null;
	/** source file where the exception is thrown. */
	String sourceFile=null;
	/**Constructor.
	 * @param message message
	 * @param locator locator
	 */
	public SAXExceptionWrapper(String message, Locator locator) {
		super(message, locator);
	}

	/**Constructor.
	 * @param message message
	 * @param locator locator
	 * @param e Exception
	 */
	public SAXExceptionWrapper(String message, Locator locator, Exception e) {
		super(message, locator, e);
	}

	/**Constructor.
	 * @param message message
	 * @param publicId public id
	 * @param systemId systemId
	 * @param lineNumber lineNumber
	 * @param columnNumber columnNumber
	 */
	public SAXExceptionWrapper(String message, String publicId,
			String systemId, int lineNumber, int columnNumber) {
		super(message, publicId, systemId, lineNumber, columnNumber);
	}

	/**Constructor.
	 * @param message message
	 * @param publicId publicId
	 * @param systemId systemId
	 * @param lineNumber lineNumber
	 * @param columnNumber columnNumber
	 * @param e Exception
	 */
	public SAXExceptionWrapper(String message, String publicId,
			String systemId, int lineNumber, int columnNumber, Exception e) {
		super(message, publicId, systemId, lineNumber, columnNumber, e);
	}
	/**
	 * Constructor.
	 * @param file file
	 * @param inner SAXParseException
	 */
	public SAXExceptionWrapper(String file,SAXParseException inner){
		super(inner.getMessage(), inner.getPublicId(), inner.getSystemId(), inner.getLineNumber(), inner.getColumnNumber(), inner.getException());
		saxParseExcetpion=inner;
		sourceFile=file;
	}
	/**
	 * Retrieve the error message.
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
