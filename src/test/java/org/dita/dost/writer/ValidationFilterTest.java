package org.dita.dost.writer;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.*;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.util.Configuration;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ValidationFilterTest {

	@Test
	public void testXMLLang() throws SAXException {
		final List<String> res = new ArrayList<String>();
		final ValidationFilter f = new ValidationFilter();
        f.setValidateMap(Collections.EMPTY_MAP);
        f.setProcessingMode(Configuration.Mode.LAX);
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
		final ValidationFilter f = new ValidationFilter();
        f.setValidateMap(Collections.EMPTY_MAP);
        f.setProcessingMode(Configuration.Mode.LAX);
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
				.build());
		f.startElement(NULL_NS_URI, TOPIC_XREF.localName, TOPIC_XREF.localName, new AttributesBuilder()
				.add(ATTRIBUTE_NAME_HREF, "http://example.com/valid/bar+baz:qux")
				.build());
		
		assertEquals(1, l.getMessages().size());
		assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
		assertEquals("http://example.com/foo/bar%20baz:qux", res.get(0));
		assertEquals("http://example.com/valid/bar+baz:qux", res.get(1));
	}
	
	@Test
    public void testId() throws SAXException, URISyntaxException {
        final ValidationFilter f = new ValidationFilter();
        f.setValidateMap(Collections.EMPTY_MAP);
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
        final ValidationFilter f = new ValidationFilter();
        f.setValidateMap(Collections.EMPTY_MAP);
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
	        final ValidationFilter f = new ValidationFilter();
            f.setValidateMap(Collections.EMPTY_MAP);
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
        final ValidationFilter f = new ValidationFilter();
        f.setValidateMap(Collections.EMPTY_MAP);
        f.setContentHandler(new DefaultHandler());
        final TestUtils.CachingLogger l = new TestUtils.CachingLogger();
        f.setLogger(l);
        
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(XML_NS_URI, ATTRIBUTE_NAME_DOMAINS, "a(props person jobrole)")
            .build());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(XML_NS_URI, "person", "jobrole(programmer)")
            .add(XML_NS_URI, "jobrole", "admin")
            .build());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(XML_NS_URI, "jobrole", "admin")
            .build());
        f.startElement(NULL_NS_URI, "x", "x", new AttributesBuilder()
            .add(XML_NS_URI, "person", "jobrole(programmer)")
            .build());
        
        assertEquals(1, l.getMessages().size());
        assertEquals(TestUtils.CachingLogger.Message.Level.ERROR, l.getMessages().get(0).level);
    }

}
