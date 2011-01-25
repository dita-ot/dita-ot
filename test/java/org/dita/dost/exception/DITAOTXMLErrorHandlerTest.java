/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.exception;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DITAOTXMLErrorHandlerTest {

	private final DITAOTXMLErrorHandler e = new DITAOTXMLErrorHandler("path");
	private final SAXParseException se = new SAXParseException("message", "publicId", "systemId", 3, 1,
			new RuntimeException("msg"));

	@Test
	public void testDITAOTXMLErrorHandler() {
		new DITAOTXMLErrorHandler("path");
		new DITAOTXMLErrorHandler(null);
	}

	@Test(expected = SAXExceptionWrapper.class)
	public void testError() throws SAXException {
		e.error(se);
	}

	@Test(expected = SAXExceptionWrapper.class)
	public void testFatalError() throws SAXException {
		e.fatalError(se);
	}

	@Test
	public void testWarning() throws SAXException {
		e.warning(se);
	}

}
