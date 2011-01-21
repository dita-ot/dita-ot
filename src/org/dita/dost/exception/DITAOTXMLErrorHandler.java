/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.exception;

import org.dita.dost.log.DITAOTJavaLogger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * DITAOTXMLErrorHandler to wrapper the SAXParseException and rethrow it to DITA-OT.
 * 
 * @author wxzhang
 */
public class DITAOTXMLErrorHandler implements ErrorHandler {
	
	/**
	 * The xml file where the error occured.
	 */
	private final String filePath;
	
	/**
	 * Constructor.
	 * @param file File
	 */
	public DITAOTXMLErrorHandler(final String file) {
		filePath = file;
	}

	/**
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 * @param saxException Exception
	 * @throws SAXException Exception
	 */
	public void error(final SAXParseException saxException) throws SAXException {
		throw new SAXExceptionWrapper(filePath, saxException);
		//throw new SAXParseException();
	}

	/**
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 * @param saxException Exception
	 * @throws SAXException Exception
	 */
	public void fatalError(final SAXParseException saxException) throws SAXException {
		throw new SAXExceptionWrapper(filePath, saxException);
	}

	/**
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 * @param saxException Exception
	 * @throws SAXException Exception
	 */
	public void warning(final SAXParseException saxException) throws SAXException {
		final DITAOTJavaLogger javalogger = new DITAOTJavaLogger();
		String msg = new SAXExceptionWrapper(filePath, saxException).getMessage();
		javalogger.logWarn(msg);
	}

}

