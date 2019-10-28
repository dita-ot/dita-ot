/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class IgnoringXmlFilter extends XMLFilterImpl {

    private final Deque<Boolean> include = new ArrayDeque<>();

    public IgnoringXmlFilter(final XMLReader parent) {
        super(parent);
    }

    public void startElement(final String uri, final String localName,
                             final String qName, final Attributes atts) throws SAXException {
        if (Objects.equals(uri, XmlReader.NS)) {
            include.push(true);
            super.startElement(uri, localName, qName, filterAttributes(atts));
        } else {
            include.push(false);
        }
    }

    private Attributes filterAttributes(final Attributes atts) {
        final int len = atts.getLength();
        if (len == 0) {
            return atts;
        }
        boolean allLocal = true;
        for (int i = 0; i < len; i++) {
            final String ns = atts.getURI(i);
            if (ns != null && !ns.isEmpty()) {
                allLocal = false;
                break;
            }
        }
        if (allLocal) {
            return atts;
        }
        AttributesImpl res = null;
        for (int i = 0; i < len; i++) {
            final String ns = atts.getURI(i);
            if (ns == null || ns.isEmpty()) {
                if (res == null) {
                    res = new AttributesImpl();
                }
                res.addAttribute(ns, atts.getLocalName(i), atts.getQName(i), atts.getType(i), atts.getValue(i));
            }
        }
        if (res == null) {
            return XMLUtils.EMPTY_ATTRIBUTES;
        }
        return res;
    }

    public void endElement(final String uri, final String localName,
                           final String qName) throws SAXException {
        if (include.pop()) {
            super.endElement(uri, localName, qName);
        }
    }

    public void characters(final char ch[], final int start, final int length) throws SAXException {
        if (include.peek()) {
            super.characters(ch, start, length);
        }
    }

    public void ignorableWhitespace(final char ch[], final int start, final int length) throws SAXException {
        if (include.peek()) {
            super.characters(ch, start, length);
        }
    }

    public void skippedEntity(final String name) throws SAXException {
        if (include.peek()) {
            super.skippedEntity(name);
        }
    }
}
