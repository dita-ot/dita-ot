/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class SaxCache {

    public interface SaxEvent {
        void write(ContentHandler handler) throws SAXException;
    }

    public record StartPrefixMappingEvent(String prefix, String uri) implements SaxEvent {

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.startPrefixMapping(prefix, uri);
        }
    }

    public record EndPrefixMappingEvent(String prefix) implements SaxEvent {

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.endPrefixMapping(prefix);
        }
    }

    public record StartElementEvent(String uri, String localName, String qName, Attributes atts) implements SaxEvent {
        public StartElementEvent(String uri, String localName, String qName, Attributes atts) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.atts = new AttributesImpl(atts);
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.startElement(uri, localName, qName, atts);
        }
    }

    public record EndElementEvent(String uri, String localName, String qName) implements SaxEvent {

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.endElement(uri, localName, qName);
        }
    }

    public record CharactersEvent(char[] ch, int start, int length) implements SaxEvent {
        public CharactersEvent(char[] ch, int start, int length) {
            final char[] copy = new char[length];
            System.arraycopy(ch, start, copy, 0, length);
            this.ch = copy;
            this.start = 0;
            this.length = length;
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.characters(ch, start, length);
        }
    }

    public record IgnorableWhitespaceEvent(char[] ch, int start, int length) implements SaxEvent {
        public IgnorableWhitespaceEvent(char[] ch, int start, int length) {
            final char[] copy = new char[length];
            System.arraycopy(ch, start, copy, 0, length);
            this.ch = copy;
            this.start = 0;
            this.length = length;
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.ignorableWhitespace(ch, start, length);
        }
    }

    public record ProcessingInstructionEvent(String target, String data) implements SaxEvent {

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.processingInstruction(target, data);
        }
    }
}
