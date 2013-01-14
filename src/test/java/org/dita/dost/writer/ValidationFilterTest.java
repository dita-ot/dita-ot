package org.dita.dost.writer;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.*;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.dita.dost.TestUtils;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ValidationFilterTest {

	@Test
	public void testXMLLang() throws SAXException {
		final List<String> res = new ArrayList<String>();
		final ValidationFilter f = new ValidationFilter();
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

}
