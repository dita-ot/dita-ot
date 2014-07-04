/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.exception;

import static org.dita.dost.util.Constants.*;

import org.xml.sax.Locator;
import org.xml.sax.SAXParseException;

/**
 * SAXExceptionWrapper which wrapper the SAXParseException.
 * 
 * @author wxzhang
 */
public final class SAXExceptionWrapper extends SAXParseException {

    /** Generated serial id. */
    private static final long serialVersionUID = -8266265000662519966L;
    /** Message & location. */
    String messageWithLocation;
    /** SAX parse exception. */
    SAXParseException saxParseException;
    /** Source file where the exception is thrown. */
    String sourceFile;

    /**
     * Constructor.
     * 
     * @param message message
     * @param locator locator
     */
    public SAXExceptionWrapper(final String message, final Locator locator) {
        super(message, locator);
    }

    /**
     * Constructor.
     * 
     * @param message message
     * @param locator locator
     * @param e Exception
     */
    public SAXExceptionWrapper(final String message, final Locator locator, final Exception e) {
        super(message, locator, e);
    }

    /**
     * Constructor.
     * 
     * @param message message
     * @param publicId public id
     * @param systemId systemId
     * @param lineNumber lineNumber
     * @param columnNumber columnNumber
     */
    public SAXExceptionWrapper(final String message, final String publicId,
            final String systemId, final int lineNumber, final int columnNumber) {
        super(message, publicId, systemId, lineNumber, columnNumber);
    }

    /**
     * Constructor.
     * 
     * @param message message
     * @param publicId publicId
     * @param systemId systemId
     * @param lineNumber lineNumber
     * @param columnNumber columnNumber
     * @param e Exception
     */
    public SAXExceptionWrapper(final String message, final String publicId,
            final String systemId, final int lineNumber, final int columnNumber, final Exception e) {
        super(message, publicId, systemId, lineNumber, columnNumber, e);
    }

    /**
     * Constructor.
     * 
     * @param file file
     * @param inner SAXParseException
     */
    public SAXExceptionWrapper(final String file, final SAXParseException inner) {
        super(inner.getMessage(), inner.getPublicId(), inner.getSystemId(), inner.getLineNumber(), inner.getColumnNumber(), inner.getException());
        saxParseException = inner;
        sourceFile = file;
    }

    /**
     * Retrieve the error message.
     * 
     * @return error message
     */
    @Override
    public String getMessage(){

        return sourceFile + " Line " + saxParseException.getLineNumber() + ":" + saxParseException.getMessage() + LINE_SEPARATOR;
    }

}
