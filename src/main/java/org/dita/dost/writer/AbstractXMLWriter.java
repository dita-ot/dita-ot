/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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


    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
    }

    @Override
    public void endDocument() throws SAXException {
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
    }

    @Override
    public void endPrefixMapping(final String prefix) throws SAXException {
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
    }

    @Override
    public void startDocument() throws SAXException {
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
    }

    @Override
    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
    }

    @Override
    public void endCDATA() throws SAXException {
    }

    @Override
    public void endDTD() throws SAXException {
    }

    @Override
    public void endEntity(final String name) throws SAXException {
    }

    @Override
    public void startCDATA() throws SAXException {
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId)
            throws SAXException {
    }

    @Override
    public void startEntity(final String name) throws SAXException {
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        return null;
    }

    @Override
    public void setContent(final Content content) {
    }

    @Override
    public abstract void write(String filename) throws DITAOTException;

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

}
