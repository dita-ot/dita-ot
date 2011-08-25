/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
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
 */
public final class TopicIdParser implements ContentHandler {
    private boolean isFirstId = true;
    private StringBuffer firstId = null;

    /**
     * Default Constructor.
     *
     */
    public TopicIdParser(){
        this(null);
    }

    /**
     * Constructor.
     * @param result to store the topic id
     */
    public TopicIdParser(final StringBuffer result) {
        firstId = result;
    }

    public void setDocumentLocator(final Locator locator) {
        // NOOP
    }

    public void startDocument() throws SAXException {
        isFirstId = true;
    }

    public void endDocument() throws SAXException {
        // NOOP
    }

    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
        // NOOP
    }

    public void endPrefixMapping(final String prefix) throws SAXException {
        // NOOP
    }

    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        if (isFirstId){
            if (atts.getValue(ATTRIBUTE_NAME_ID)!=null){
                isFirstId = false;
                firstId.append(atts.getValue(ATTRIBUTE_NAME_ID));
            }
        }
    }

    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        // NOOP
    }

    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        // NOOP
    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        // NOOP
    }

    public void processingInstruction(final String target, final String data)
            throws SAXException {
        // NOOP
    }

    public void skippedEntity(final String name) throws SAXException {
        // NOOP
    }

}
