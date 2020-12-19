/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX Parser that handles topic id identification.
 *
 * @deprecated use DOM parsing instead
 */
@Deprecated
public final class TopicIdParser implements ContentHandler {
    private boolean isFirstId = true;
    private StringBuilder firstId = null;

    /**
     * Default Constructor.
     *
     */
    public TopicIdParser() {
        this(null);
    }

    /**
     * Constructor.
     * @param result to store the topic id
     */
    public TopicIdParser(final StringBuilder result) {
        firstId = result;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        // NOOP
    }

    @Override
    public void startDocument() throws SAXException {
        isFirstId = true;
    }

    @Override
    public void endDocument() throws SAXException {
        // NOOP
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
        // NOOP
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
        // NOOP
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        if (isFirstId) {
            if (atts.getValue(ATTRIBUTE_NAME_ID) != null) {
                isFirstId = false;
                firstId.append(atts.getValue(ATTRIBUTE_NAME_ID));
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        // NOOP
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        // NOOP
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        // NOOP
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        // NOOP
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
        // NOOP
    }

}
