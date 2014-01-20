/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.IOException;

import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class extends AbstractReader, implement SAX's ContentHandler,
 * LexicalHandler, and EntityResolver.
 * 
 * @version 1.0 2005-06-24
 * 
 * @author Wu, Zhi Qiang
 */
public abstract class AbstractXMLReader implements AbstractReader,
ContentHandler, LexicalHandler, EntityResolver {

    /**
     * Sets the grammar pool on the parser. Note that this is a Xerces-specific
     * feature.
     * @param reader
     */
    public void setGrammarPool(final XMLReader reader) {
        try {
            reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", GrammarPoolManager.getGrammarPool());
            logger.info("Using Xerces grammar pool for DTD and schema caching.");
        } catch (final NoClassDefFoundError e) {
            logger.debug("Xerces not available, not using grammar caching");
        } catch (final SAXNotRecognizedException e) {
            e.printStackTrace();
            logger.warn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
        } catch (final SAXNotSupportedException e) {
            e.printStackTrace();
            logger.warn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
        }
    }

    protected DITAOTLogger logger;

    @Override
    public void read(final File filename) {
        // NOOP
    }

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setDocumentLocator(final Locator locator) {
        // NOOP
    }

    @Override
    public void startDocument() throws SAXException {
        // NOOP
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
        // NOOP
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

    @Override
    public void startDTD(final String name, final String publicId, final String systemId)
            throws SAXException {
        // NOOP
    }

    @Override
    public void endDTD() throws SAXException {
        // NOOP
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        // NOOP
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        // NOOP
    }

    @Override
    public void startCDATA() throws SAXException {
        // NOOP
    }

    @Override
    public void endCDATA() throws SAXException {
        // NOOP
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        // NOOP
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        return null;
    }

}
