/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.IOException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * AbstractXMLWriter class.
 * 
 * @version 1.0 2005-6-28
 * @author Zhang, Yuan Peng
 */

abstract class AbstractXMLWriter implements AbstractWriter,
ContentHandler, LexicalHandler, EntityResolver {

    protected DITAOTLogger logger;


    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
    }

    public void endPrefixMapping(final String prefix) throws SAXException {
    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
    }

    public void processingInstruction(final String target, final String data)
            throws SAXException {
    }

    public void setDocumentLocator(final Locator locator) {
    }

    public void skippedEntity(final String name) throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
    }

    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
    }

    public void comment(final char[] ch, final int start, final int length) throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void endEntity(final String name) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void startDTD(final String name, final String publicId, final String systemId)
            throws SAXException {
    }

    public void startEntity(final String name) throws SAXException {
    }

    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        return null;
    }

    public void setContent(Content content) {
    }

    public abstract void write(String filename) throws DITAOTException;

    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

}
