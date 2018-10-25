/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.*;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.util.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ValidationFilterTest {

    private ValidationFilter f;

    @Before
    public void setUp() {
        f = new ValidationFilter();
        f.setValidateMap(Collections.emptyMap());
        f.setProcessingMode(Configuration.Mode.LAX);
        f.setCurrentFile(URI.create("file:/foo/bar.dita"));
    }

    @Test
    public void testXMLLang() throws SAXException {
        final List<String> res = new ArrayList<String>();
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                res.add(atts.getValue(XML_NS_URI, "lang"));
            }
        });
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);

        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(XML_NS_URI, "lang", "en_us")
            .build());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(XML_NS_URI, "lang", "en-GB")
            .build());

        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
        assertEquals("en-us", res.get(0));
        assertEquals("en-GB", res.get(1));
    }

    @Test
    public void testHref() throws SAXException, URISyntaxException {
        final List<String> res = new ArrayList<String>();
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                res.add(atts.getValue(ATTRIBUTE_NAME_HREF));
            }
        });
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);

        f.startElement(NULL_NS_URI, TOPIC_XREF.localName, TOPIC_XREF.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_HREF, "http://example.com/foo\\bar baz:qux")
                .add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL)
                .build());
        f.startElement(NULL_NS_URI, TOPIC_XREF.localName, TOPIC_XREF.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_HREF, "http://example.com/valid/bar+baz:qux")
                .add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL)
                .build());

        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
        assertEquals("http://example.com/foo/bar%20baz:qux", res.get(0));
        assertEquals("http://example.com/valid/bar+baz:qux", res.get(1));
    }
    
    @Test
    public void testConref() throws SAXException, URISyntaxException {
        final List<String> res = new ArrayList<String>();
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                res.add(atts.getValue(ATTRIBUTE_NAME_CONREF));
            }
        });
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);

        f.startElement(NULL_NS_URI, TOPIC_KEYWORD.localName, TOPIC_KEYWORD.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONREF, "sub\\backslash.dita#topic/back")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_KEYWORD.localName, TOPIC_KEYWORD.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CONREF, "sub/slash.dita#topic/valid")
                .build());

        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
        assertEquals("sub/backslash.dita#topic/back", res.get(0));
        assertEquals("sub/slash.dita#topic/valid", res.get(1));
    }

    @Test
    public void testScope() throws SAXException, URISyntaxException {
        final List<String> res = new ArrayList<String>();
        f.setContentHandler(new DefaultHandler() {
            @Override
            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
                res.add(atts.getValue(ATTRIBUTE_NAME_HREF));
            }
        });
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);

        f.startElement(NULL_NS_URI, TOPIC_XREF.localName, TOPIC_XREF.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_HREF, "http://example.com/broken")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_XREF.localName, TOPIC_XREF.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_HREF, "http://example.com/valid")
                .add(ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_EXTERNAL)
                .build());

        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.WARN, l.getMessages().get(0).level);
        assertEquals("http://example.com/broken", res.get(0));
    }

    @Test
    public void testId() throws SAXException, URISyntaxException {
        f.setContentHandler(new DefaultHandler());
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);
        
        f.startElement(NULL_NS_URI, TOPIC_TOPIC.localName, TOPIC_TOPIC.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
                .add(ATTRIBUTE_NAME_ID, "topic")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_P.toString())
                .add(ATTRIBUTE_NAME_ID, "first")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_P.toString())
                .add(ATTRIBUTE_NAME_ID, "second")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_P.toString())
                .add(ATTRIBUTE_NAME_ID, "first")
                .build());
        assertEquals(1, l.getMessages().size());
        f.startElement(NULL_NS_URI, TOPIC_TOPIC.localName, TOPIC_TOPIC.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString())
                .add(ATTRIBUTE_NAME_ID, "topic")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_CLASS, TOPIC_P.toString())
                .add(ATTRIBUTE_NAME_ID, "second")
                .build());
        
        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.WARN, l.getMessages().get(0).level);
    }

    @Test
    public void testKeys() throws SAXException, URISyntaxException {
        f.setContentHandler(new DefaultHandler());
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);
        
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_KEYS, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:@!$&'()*+,;=")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_KEYS, "foo bar baz")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_KEYS, " foo ")
                .build());
        assertEquals(0, l.getMessages().size());
        
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_KEYS, "foo/bar")
                .build());
        f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                .add(ATTRIBUTE_NAME_KEYS, "foo\u00E4bar")
                .build());
        
        assertEquals(2, l.getMessages().size());
        for (final Message m: l.getMessages()) {
            assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, m.level);            
        }
    }

       @Test
        public void testKeyscope() throws SAXException, URISyntaxException {
            f.setContentHandler(new DefaultHandler());
            final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
            f.setLogger(l);

            f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                    .add(ATTRIBUTE_NAME_KEYSCOPE, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~:@!$&'()*+,;=")
                    .build());
            f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                    .add(ATTRIBUTE_NAME_KEYSCOPE, "foo bar baz")
                    .build());
            f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                    .add(ATTRIBUTE_NAME_KEYSCOPE, " foo ")
                    .build());
            assertEquals(0, l.getMessages().size());

            f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                    .add(ATTRIBUTE_NAME_KEYSCOPE, "foo/bar")
                    .build());
            f.startElement(NULL_NS_URI, TOPIC_P.localName, TOPIC_P.localName, new AttributesBuilder()
                    .add(ATTRIBUTE_NAME_KEYSCOPE, "foo\u00E4bar")
                    .build());

            assertEquals(2, l.getMessages().size());
            for (final Message m: l.getMessages()) {
                assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, m.level);
            }
        }

    @Test
    public void testAttributeGeneralization() throws SAXException {
        f.setContentHandler(new DefaultHandler());
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);
        
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(ATTRIBUTE_NAME_DOMAINS, "a(props person jobrole)")
            .build());
        assertEquals(0, l.getMessages().size());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add("person", "jobrole(programmer)")
            .add("jobrole", "admin")
            .build());
        assertEquals(1, l.getMessages().size());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add("jobrole", "admin")
            .build());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add("person", "jobrole(programmer)")
            .build());
        
        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
    }

}
