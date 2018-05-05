/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collection;

import static org.dita.dost.util.Constants.PR_D_CODEBLOCK;

/**
 * Trim whitespace in codeblock elements.
 *
 * @since 3.1
 */
public final class NormalizeCodeblock extends AbstractXMLFilter {

    private int depth = 0;
    private Collection<SAXEvent> buf = new ArrayList<>();

//    /**
//     * Filter a start Namespace prefix mapping event.
//     *
//     * @param prefix The Namespace prefix.
//     * @param uri The Namespace URI.
//     * @exception org.xml.sax.SAXException The client may throw
//     *            an exception during processing.
//     */
//    public void startPrefixMapping (String prefix, String uri)
//            throws SAXException
//    {
//        if (contentHandler != null) {
//            contentHandler.startPrefixMapping(prefix, uri);
//        }
//    }
//
//
//    /**
//     * Filter an end Namespace prefix mapping event.
//     *
//     * @param prefix The Namespace prefix.
//     * @exception org.xml.sax.SAXException The client may throw
//     *            an exception during processing.
//     */
//    public void endPrefixMapping (String prefix)
//            throws SAXException
//    {
//        if (contentHandler != null) {
//            contentHandler.endPrefixMapping(prefix);
//        }
//    }


    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes atts)
            throws SAXException {
        if (depth > 0) {
            depth++;
            buf.add(new StartElementEvent(uri, localName, qName, atts));
        } else if (PR_D_CODEBLOCK.matches(atts)) {
            depth = 1;
            super.startElement(uri, localName, qName, atts);
        } else {
            super.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (depth > 0) {
            depth--;
            if (depth == 0) {
                for (final SAXEvent event : buf) {
                    if (event instanceof StartElementEvent) {
                        final StartElementEvent e = (StartElementEvent) event;
                        super.startElement(e.uri, e.localName, e.qName, e.atts);
                    } else if (event instanceof EndElementEvent) {
                        final EndElementEvent e = (EndElementEvent) event;
                        super.endElement(e.uri, e.localName, e.qName);
                    } else if (event instanceof CharactersEvent) {
                        final CharactersEvent e = (CharactersEvent) event;
                        super.characters(e.ch, e.start, e.length);
                    } else if (event instanceof ProcessingInstructionEvent) {
                        final ProcessingInstructionEvent e = (ProcessingInstructionEvent) event;
                        super.processingInstruction(e.target, e.data);
                    } else {
                        throw new IllegalArgumentException(event.getClass().getCanonicalName());
                    }
                }
                buf.clear();
                super.endElement(uri, localName, qName);
            } else {
                buf.add(new EndElementEvent(uri, localName, qName));
            }
        } else {
            super.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char ch[], int start, int length)
            throws SAXException {
        if (depth > 0) {
            buf.add(new CharactersEvent(ch, start, length));
        } else {
            super.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length)
            throws SAXException {
        if (depth > 0) {
            buf.add(new CharactersEvent(ch, start, length));
        } else {
            super.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (depth > 0) {
            buf.add(new ProcessingInstructionEvent(target, data));
        } else {
            super.processingInstruction(target, data);
        }
    }

    interface SAXEvent {
    }

    static class StartElementEvent implements SAXEvent {
        final String uri;
        final String localName;
        final String qName;
        final Attributes atts;

        StartElementEvent(String uri, String localName, String qName, Attributes atts) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
            this.atts = atts;
        }
    }

    static class EndElementEvent implements SAXEvent {
        final String uri;
        final String localName;
        final String qName;

        EndElementEvent(String uri, String localName, String qName) {
            this.uri = uri;
            this.localName = localName;
            this.qName = qName;
        }
    }

    static class CharactersEvent implements SAXEvent {
        final char[] ch;
        final int start;
        final int length;

        CharactersEvent(char ch[], int start, int length) {
            this.ch = ch;
            this.start = start;
            this.length = length;
        }
    }

    static class ProcessingInstructionEvent implements SAXEvent {
        final String target;
        final String data;

        ProcessingInstructionEvent(String target, String data) {
            this.target = target;
            this.data = data;
        }
    }

}
