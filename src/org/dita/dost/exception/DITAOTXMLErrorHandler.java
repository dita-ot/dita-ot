/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.exception;


import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.dita.dost.util.LogUtils;
import org.dita.dost.log.DITAOTJavaLogger;

/**
 * DITAOTXMLErrorHandler to wrapper the SAXParseException and rethrow it to DITA-OT
 * 
 * @author wxzhang
 *
 */
public class DITAOTXMLErrorHandler implements ErrorHandler {
	/*
	 * the xml file where the error occured.
	 */
	private String filePath=null;

	public DITAOTXMLErrorHandler(String file) {
		filePath=file;
	}
	

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
	 */
	public void error(SAXParseException saxException) throws SAXException {
		
		throw new SAXExceptionWrapper(filePath,saxException);
		//throw new SAXParseException();

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
	 */
	public void fatalError(SAXParseException saxException) throws SAXException {
		throw new SAXExceptionWrapper(filePath,saxException);
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
	 */
	public void warning(SAXParseException saxException) throws SAXException {
		DITAOTJavaLogger javalogger=new DITAOTJavaLogger();
		String msg=null;
		msg=new SAXExceptionWrapper(filePath,saxException).getMessage();
		javalogger.logWarn(msg);
	}

}

