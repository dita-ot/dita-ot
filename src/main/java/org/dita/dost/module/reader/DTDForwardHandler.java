/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * Lexical handler to forward DTD declaration into processing instructions.
 */
final class DTDForwardHandler implements LexicalHandler {

    private final XMLReader parser;

    public DTDForwardHandler(XMLReader parser) {
        this.parser = parser;
    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId) throws SAXException {
        if (publicId != null && !publicId.isEmpty()) {
            parser.getContentHandler().processingInstruction("doctype-public", publicId);
        }
        if (systemId != null && !systemId.isEmpty()) {
            parser.getContentHandler().processingInstruction("doctype-system", systemId);
        }
    }

    @Override
    public void endDTD() throws SAXException {}

    @Override
    public void startEntity(String name) throws SAXException {}

    @Override
    public void endEntity(String name) throws SAXException {}

    @Override
    public void startCDATA() throws SAXException {}

    @Override
    public void endCDATA() throws SAXException {}

    @Override
    public void comment(char[] ch, int start, int length) throws SAXException {}
}
