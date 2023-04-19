/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.exception;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.log.DITAOTLogger;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DITAOTXMLErrorHandlerTest {

  private final DITAOTLogger logger = new TestLogger();
  private final DITAOTXMLErrorHandler e = new DITAOTXMLErrorHandler("path", logger);
  private final SAXParseException se = new SAXParseException(
    "message",
    "publicId",
    "systemId",
    3,
    1,
    new RuntimeException("msg")
  );

  @Test
  public void testDITAOTXMLErrorHandler() {
    new DITAOTXMLErrorHandler("path", logger);
    new DITAOTXMLErrorHandler(null, logger);
  }

  @Test
  public void testError() {
    assertThrows(SAXExceptionWrapper.class, () -> e.error(se));
  }

  @Test
  public void testFatalError() {
    assertThrows(SAXExceptionWrapper.class, () -> e.fatalError(se));
  }

  @Test
  public void testWarning() throws SAXException {
    e.warning(se);
  }
}
