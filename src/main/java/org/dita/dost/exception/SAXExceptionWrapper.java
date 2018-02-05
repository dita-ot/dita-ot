/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

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
    /** Message &amp; location. */
    String messageWithLocation;
    /** SAX parse exception. */
    private SAXParseException saxParseException;
    /** Source file where the exception is thrown. */
    private String sourceFile;

    /**
     * Constructor.
     *
     * @param message message
     * @param locator locator
     * @deprecated since 2.3
     */
    @Deprecated
    public SAXExceptionWrapper(final String message, final Locator locator) {
        super(message, locator);
    }

    /**
     * Constructor.
     *
     * @param message message
     * @param locator locator
     * @param e Exception
     * @deprecated since 2.3
     */
    @Deprecated
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
     * @deprecated since 2.3
     */
    @Deprecated
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
     * @deprecated since 2.3
     */
    @Deprecated
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
    public String getMessage() {

        return sourceFile + " Line " + saxParseException.getLineNumber() + ":" + saxParseException.getMessage() + LINE_SEPARATOR;
    }

}
