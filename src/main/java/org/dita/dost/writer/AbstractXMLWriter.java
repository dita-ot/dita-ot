/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import java.io.File;
import java.io.IOException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * AbstractXMLWriter class.
 *
 * @version 1.0 2005-6-28
 * @author Zhang, Yuan Peng
 */

abstract class AbstractXMLWriter implements AbstractWriter,
ContentHandler, EntityResolver {

    DITAOTLogger logger;
    Job job;


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
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        return null;
    }

    @Override
    public abstract void write(File filename) throws DITAOTException;

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

}
