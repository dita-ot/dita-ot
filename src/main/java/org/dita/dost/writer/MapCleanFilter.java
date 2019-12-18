/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.dita.dost.util.Constants.*;

public class MapCleanFilter extends AbstractXMLFilter {

    private enum Keep {
        RETAIN, DISCARD, DISCARD_BRANCH
    }

    private final Deque<Keep> stack = new ArrayDeque<>();

    @Override
    public void startDocument() throws SAXException {
        stack.clear();
        getContentHandler().startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        assert stack.isEmpty();
        getContentHandler().endDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (!stack.isEmpty() && stack.element() == Keep.DISCARD_BRANCH) {
            stack.addFirst(Keep.DISCARD_BRANCH);
        } else if (SUBMAP.matches(cls)) {
            stack.addFirst(Keep.DISCARD);
        } else if (DITA_OT_D_KEYDEF.matches(cls)) {
            stack.addFirst(Keep.DISCARD_BRANCH);
        } else {
            stack.addFirst(Keep.RETAIN);
        }

        if (stack.isEmpty() || stack.peekFirst() == Keep.RETAIN) {
            getContentHandler().startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (stack.removeFirst() == Keep.RETAIN) {
            getContentHandler().endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (stack.peekFirst() != Keep.DISCARD_BRANCH) {
            getContentHandler().characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (stack.peekFirst() != Keep.DISCARD_BRANCH) {
            getContentHandler().ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        if (stack.isEmpty() || stack.peekFirst() != Keep.DISCARD_BRANCH) {
            getContentHandler().processingInstruction(target, data);
        }
    }

    @Override
    public void skippedEntity(String name)
            throws SAXException {
        if (stack.peekFirst() != Keep.DISCARD_BRANCH) {
            getContentHandler().skippedEntity(name);
        }
    }

//    <xsl:template match="*[contains(@class, ' mapgroup-d/topicgroup ')]/*/*[contains(@class, ' topic/navtitle ')]">
//      <xsl:call-template name="output-message">
//        <xsl:with-param name="id" select="'DOTX072I'"/>
//      </xsl:call-template>
//    </xsl:template>

}
