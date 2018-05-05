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

import java.util.*;

import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_OUTPUTCLASS;
import static org.dita.dost.util.Constants.PR_D_CODEBLOCK;

/**
 * Trim whitespace in codeblock elements.
 *
 * @since 3.1
 */
public final class NormalizeCodeblock extends AbstractXMLFilter {

    private Set<String> outputClass = new HashSet(Arrays.asList("normalize-space"));
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
        } else if (PR_D_CODEBLOCK.matches(atts) && hasStripWhitespace(atts.getValue(ATTRIBUTE_NAME_OUTPUTCLASS))) {
            depth = 1;
            super.startElement(uri, localName, qName, atts);
        } else {
            super.startElement(uri, localName, qName, atts);
        }
    }

    private boolean hasStripWhitespace(String value) {
        return value != null && Arrays.stream(value.split("\\s+")).anyMatch(cls -> outputClass.contains(cls));
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (depth > 0) {
            depth--;
            if (depth == 0) {
                for (final SAXEvent event : normalizeSpace(buf)) {
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

    private Collection<SAXEvent> normalizeSpace(Collection<SAXEvent> buf) {
        final StringBuilder merged = new StringBuilder();
        for (final SAXEvent event : buf) {
            if (event instanceof CharactersEvent) {
                final CharactersEvent e = (CharactersEvent) event;
                merged.append(e.ch, e.start, e.length);
            }
        }
        final int min = Arrays.stream(merged.toString().split("\n"))
                .filter(str -> !str.isEmpty())
                .mapToInt(this::countLeadingSpace)
                .min()
                .orElse(0);
        if (min == 0) {
            return buf;
        }
        final List<SAXEvent> res = new ArrayList<>(buf.size());
        boolean firstCharactersEvent = true;
        for (final SAXEvent event : buf) {
            if (event instanceof CharactersEvent) {
                final CharactersEvent e = (CharactersEvent) event;
                final char[] ch = stripLeadingSpace(firstCharactersEvent, min, e.ch, e.start, e.length);
                res.add(new CharactersEvent(ch, 0, ch.length));
                firstCharactersEvent = false;
            } else {
                res.add(event);
            }
        }
        return res;
    }

    char[] stripLeadingSpace(boolean first, int prefix, char[] ch, int start, int length) {
        final String str = first
                ? new String(ch, start + prefix, length - prefix)
                : new String(ch, start, length);
        return str
                .replaceAll("\n {" + prefix + "}", "\n")
                .toCharArray();
    }

    int countLeadingSpace(String ch) {
        return ch.replaceAll("^( *)\\S*", "$1").length();
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
