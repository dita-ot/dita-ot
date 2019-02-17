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

    public static class StartPrefixMappingEvent implements SaxEvent {
        public final String prefix;
        public final String uri;

        public StartPrefixMappingEvent(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.startPrefixMapping(prefix, uri);
        }
    }

    public static class EndPrefixMappingEvent implements SaxEvent {
        public final String prefix;

        public EndPrefixMappingEvent(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.endPrefixMapping(prefix);
        }
    }

    public static class StartElementEvent implements SaxEvent {
        public final String uri;
        public final String localName;
        public final String qName;
        public final Attributes atts;

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

    public static class EndElementEvent implements SaxEvent {
        public final String uri;
        public final String localName;
        public final String qName;

        public EndElementEvent(String uri, String localName, String qName) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.endElement(uri, localName, qName);
        }
    }

    public static class CharactersEvent implements SaxEvent {
        public final char[] ch;
        public final int start;
        public final int length;

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

    public static class IgnorableWhitespaceEvent implements SaxEvent {
        public final char[] ch;
        public final int start;
        public final int length;

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

    public static class ProcessingInstructionEvent implements SaxEvent {
        public final String target;
        public final String data;

        public ProcessingInstructionEvent(String target, String data) {
            this.target = target;
            this.data = data;
        }

        @Override
        public void write(ContentHandler handler) throws SAXException {
            handler.processingInstruction(target, data);
        }
    }
}
