/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import java.io.File;
import java.io.IOException;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This class extends AbstractReader, implement SAX's ContentHandler
 * and EntityResolver.
 *
 * @version 1.0 2005-06-24
 *
 * @author Wu, Zhi Qiang
 */
public abstract class AbstractXMLReader implements AbstractReader,
ContentHandler, EntityResolver {

    protected DITAOTLogger logger;
    protected Job job;

    @Override
    public void read(final File filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public final void setJob(final Job job) {
        this.job = job;
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
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        return null;
    }

}
