/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertEquals;

public class NormalizeFilterTest {

    @Test
    public void testCascade() throws Exception {
        final NormalizeFilter f = new NormalizeFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) throws SAXException {
                assertEquals(ATTRIBUTE_CASCADE_VALUE_NOMERGE, attributes.getValue(ATTRIBUTE_NAME_CASCADE));
            }
        });
        f.startElement(NULL_NS_URI, MAP_MAP.localName, MAP_MAP.localName, new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, MAP_MAP.toString()).build());
    }

    @Test
    public void testDomains() throws Exception {
        final NormalizeFilter f = new NormalizeFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) throws SAXException {
                assertEquals("(topic hi-d) (topic ut-d) (topic indexing-d) (topic hazard-d) (topic abbrev-d) (topic pr-d) (topic sw-d) (topic ui-d) a(props audience) a(props deliveryTarget) a(props platform) a(props product) a(props otherprops)",
                        attributes.getValue(ATTRIBUTE_NAME_DOMAINS));
                assertEquals("@props/audience @props/deliveryTarget @props/platform @props/product @props/otherprops",
                        attributes.getValue(ATTRIBUTE_NAME_SPECIALIZATIONS));
            }
        });
        f.startElement(NULL_NS_URI, TOPIC_TOPIC.localName, TOPIC_TOPIC.localName, new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
                .add(ATTRIBUTE_NAME_DOMAINS, "(topic hi-d)\n" +
                        "(topic ut-d)\n" +
                        "(topic indexing-d)\n" +
                        "(topic hazard-d)\n" +
                        "(topic abbrev-d)\n" +
                        "(topic pr-d)\n" +
                        "(topic sw-d)\n" +
                        "(topic ui-d)\n" +
                        "a(props audience)\n" +
                        "a(props deliveryTarget)\n" +
                        "a(props platform)\n" +
                        "a(props product)\n" +
                        "a(props otherprops)    ")
                .build());
    }

    @Test
    public void testSpecializations() throws Exception {
        final NormalizeFilter f = new NormalizeFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) throws SAXException {
                assertEquals("a(props audience) a(props deliveryTarget) a(props platform) a(props product) a(props otherprops)",
                        attributes.getValue(ATTRIBUTE_NAME_DOMAINS));
                assertEquals("@props/audience @props/deliveryTarget @props/platform @props/product @props/otherprops",
                        attributes.getValue(ATTRIBUTE_NAME_SPECIALIZATIONS));
            }
        });
        f.startElement(NULL_NS_URI, TOPIC_TOPIC.localName, TOPIC_TOPIC.localName, new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
                .add(ATTRIBUTE_NAME_SPECIALIZATIONS, "  @props/audience\n" +
                        "@props/deliveryTarget\n" +
                        "@props/platform\n" +
                        "@props/product\n" +
                        "@props/otherprops  ")
                .build());
    }

    @Test
    public void testExistingCascade() throws Exception {
        final NormalizeFilter f = new NormalizeFilter();
        f.setLogger(new TestUtils.TestLogger());
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes) throws SAXException {
                assertEquals(ATTRIBUTE_CASCADE_VALUE_MERGE, attributes.getValue(ATTRIBUTE_NAME_CASCADE));
            }
        });
        f.startElement(NULL_NS_URI, MAP_MAP.localName, MAP_MAP.localName, new XMLUtils.AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, MAP_MAP.toString())
                .add(ATTRIBUTE_NAME_CASCADE, ATTRIBUTE_CASCADE_VALUE_MERGE).build());
    }

}
