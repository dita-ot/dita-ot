/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.exception;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * DITAOTXMLErrorHandler to wrapper the SAXParseException and rethrow it to DITA-OT.
 *
 * @author wxzhang
 */
public final class DITAOTXMLErrorHandler implements ErrorHandler {

  /**
   * The xml file where the error occured.
   */
  private final String filePath;
  private final DITAOTLogger logger;
  private final Configuration.Mode mode;

  /**
   * Constructor.
   * @param file File
   */
  public DITAOTXMLErrorHandler(final String file, final DITAOTLogger logger, final Configuration.Mode mode) {
    filePath = file;
    this.logger = logger;
    this.mode = mode;
  }

  /**
   * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
   * @param saxException Exception
   * @throws SAXException Exception
   */
  @Override
  public void error(final SAXParseException saxException) throws SAXException {
    final SAXExceptionWrapper ex = new SAXExceptionWrapper(filePath, saxException);
    if (mode == Configuration.Mode.STRICT) {
      throw ex;
    } else {
      logger.error(
        MessageUtils.getMessage("DOTJ088E", saxException.getMessage()).setLocation(saxException).toString(),
        saxException
      );
    }
  }

  /**
   * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
   * @param saxException Exception
   * @throws SAXException Exception
   */
  @Override
  public void fatalError(final SAXParseException saxException) throws SAXException {
    throw new SAXExceptionWrapper(filePath, saxException);
  }

  /**
   * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
   * @param saxException Exception
   * @throws SAXException Exception
   */
  @Override
  public void warning(final SAXParseException saxException) throws SAXException {
    final String msg = new SAXExceptionWrapper(filePath, saxException).getMessage();
    logger.warn(msg);
  }
}
