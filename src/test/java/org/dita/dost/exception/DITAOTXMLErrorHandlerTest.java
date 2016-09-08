/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.exception;

import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.log.DITAOTLogger;

public class DITAOTXMLErrorHandlerTest {

    private final DITAOTLogger logger = new TestLogger();
    private final DITAOTXMLErrorHandler e = new DITAOTXMLErrorHandler("path", logger);
    private final SAXParseException se = new SAXParseException("message", "publicId", "systemId", 3, 1,
            new RuntimeException("msg"));

    @Test
    public void testDITAOTXMLErrorHandler() {
        new DITAOTXMLErrorHandler("path", logger);
        new DITAOTXMLErrorHandler(null, logger);
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
