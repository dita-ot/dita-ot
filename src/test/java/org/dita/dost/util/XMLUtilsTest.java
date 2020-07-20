/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import net.sf.saxon.Configuration;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.SymbolicName;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.module.DelegatingCollationUriResolverTest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.junit.Assert.*;

public class XMLUtilsTest {

    private static final File resourceDir = TestUtils.getResourceDir(XMLUtilsTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;


    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(XMLUtilsTest.class);
    }

    @Test
    public void configureCollationResolvers() {
        final net.sf.saxon.Configuration configuration = new Configuration();
        XMLUtils.configureSaxonCollationResolvers(configuration);
        final CollationURIResolver collationURIResolver = configuration.getCollationURIResolver();
        assertTrue(collationURIResolver.getClass().isAssignableFrom(DelegatingCollationUriResolverTest.class));
    }

    @Test
    public void configureExtensions() {
        final net.sf.saxon.Configuration configuration = new Configuration();
        XMLUtils.configureSaxonExtensions(configuration);
        final SymbolicName.F functionName = new SymbolicName.F(new StructuredQName("x", "y", "z"), 0);
        assertTrue(configuration.getIntegratedFunctionLibrary().isAvailable(functionName));
    }

    @Test
    public void testGetPrefix() {
        assertEquals("", XMLUtils.getPrefix("foo"));
        assertEquals("bar", XMLUtils.getPrefix("bar:foo"));
        try {
            XMLUtils.getPrefix(null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testAddOrSetAttributeAttributesImplStringStringStringStringString() {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, "foo", "foo", "foo", "CDATA", "foo");
        assertEquals(1, atts.getLength());
        XMLUtils.addOrSetAttribute(atts, "bar", "bar", "bar", "CDATA", "bar");
        assertEquals(2, atts.getLength());
        XMLUtils.addOrSetAttribute(atts, "foo", "foo", "foo", "CDATA", "bar");
        assertEquals(2, atts.getLength());
    }

    @Test
    public void testAddOrSetAttributeAttributesImplStringString() {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, "foo", "foo");
        assertEquals(1, atts.getLength());
        XMLUtils.addOrSetAttribute(atts, "bar", "bar");
        assertEquals(2, atts.getLength());
        XMLUtils.addOrSetAttribute(atts, "foo", "bar");
        assertEquals(2, atts.getLength());
    }

    @Test
    public void testAddOrSetAttributeAttributesImplNode() throws ParserConfigurationException {
        final AttributesImpl atts = new AttributesImpl();
        final DOMImplementation dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        final Document doc = dom.createDocument(null, "foo", null);
        
        doc.getDocumentElement().setAttribute("foo", "foo");
        final Attr att = (Attr) doc.getDocumentElement().getAttributeNode("foo");
        XMLUtils.addOrSetAttribute(atts, att);

        final int i = atts.getIndex(NULL_NS_URI, "foo");
        assertEquals(NULL_NS_URI, atts.getURI(i));
        assertEquals("foo", atts.getQName(i));
        assertEquals("foo", atts.getLocalName(i));
        assertEquals("foo", atts.getValue(i));
        
        doc.getDocumentElement().setAttributeNS(XML_NS_URI, "xml:lang", "en");
        final Attr lang = (Attr) doc.getDocumentElement().getAttributeNodeNS(XML_NS_URI, "lang");
        XMLUtils.addOrSetAttribute(atts, lang);
        
        final int l = atts.getIndex(XML_NS_URI, "lang");
        assertEquals(XML_NS_URI, atts.getURI(l));
        assertEquals("xml:lang", atts.getQName(l));
        assertEquals("lang", atts.getLocalName(l));
        assertEquals("en", atts.getValue(l));
    }

    @Test
    public void testRemoveAttribute() {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, "foo", "foo", "foo", "CDATA", "foo");
        assertEquals(1, atts.getLength());
        XMLUtils.removeAttribute(atts, "foo");
        assertEquals(0, atts.getLength());
    }

    @Test
    public void testGetStringValue() throws ParserConfigurationException {
        final DOMImplementation dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().getDOMImplementation();
        final Document doc = dom.createDocument(null, "foo", null);
        
        final Element root = doc.getDocumentElement();
        root.appendChild(doc.createTextNode("foo"));
        assertEquals("foo", XMLUtils.getStringValue(root));
        root.appendChild(doc.createTextNode(" "));
        final Element nested = doc.createElement("ph");
        nested.appendChild(doc.createTextNode("nested"));
        root.appendChild(nested);
        root.appendChild(doc.createTextNode(" bar"));
        assertEquals("foo nested bar", XMLUtils.getStringValue(root));
    }
    
    @Test
    public void testAttributesBuilder() throws ParserConfigurationException {
        final XMLUtils.AttributesBuilder b = new XMLUtils.AttributesBuilder(); 
        assertEquals(0, b.build().getLength());
        
        b.add("foo", "bar");
        b.add("uri", "foo", "prefix:foo", "CDATA", "qux");
        final Attributes a = b.build();
        assertEquals("bar", a.getValue("foo"));
        assertEquals("qux", a.getValue("prefix:foo"));
        assertEquals(2, a.getLength());
        for (int i = 0; i < a.getLength(); i++) {
            if (a.getQName(i).equals("prefix:foo")) {
                assertEquals("uri", a.getURI(i));
                assertEquals("foo", a.getLocalName(i));
                assertEquals("prefix:foo", a.getQName(i));
                assertEquals("CDATA", a.getType(i));
                assertEquals("qux", a.getValue(i));
            }
        }
        
        b.add("foo", "quxx");
        final Attributes aa = b.build();
        assertEquals("quxx", aa.getValue("foo"));
        assertEquals(2, aa.getLength());
        
        final AttributesImpl ai = new AttributesImpl();
        ai.addAttribute(NULL_NS_URI, "baz", "baz", "CDATA", "all");
        b.addAll(ai);
        final Attributes aaa = b.build();
        assertEquals("all", aaa.getValue("baz"));
        assertEquals(3, aaa.getLength());

        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Attr domAttr = doc.createAttributeNS(XML_NS_URI, "xml:space");
        domAttr.setValue("preserve");
        b.add(domAttr);
        final Attributes a4 = b.build();
        for (int i = 0; i < a4.getLength(); i++) {
            if (a4.getQName(i).equals("xml:space")) {
                assertEquals(XML_NS_URI, a4.getURI(i));
                assertEquals("space", a4.getLocalName(i));
                assertEquals("xml:space", a4.getQName(i));
                assertEquals("preserve", a4.getValue(i));
            }
        }
    }


    @Test
    public void testEscapeXMLString() {
        String result = null;
        final String input = "<this is test of char update for xml href=\" see link: http://www.ibm.com/download.php?abc=123&def=456\">'test' </test>";
        final String expected = "&lt;this is test of char update for xml href=&quot; see link: http://www.ibm.com/download.php?abc=123&amp;def=456&quot;&gt;&apos;test&apos; &lt;/test&gt;";
        result = XMLUtils.escapeXML(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapeXMLCharArrayIntInt() {
        String result = null;
        final char[] input = "<this is test of char update for xml href=\" see link: http://www.ibm.com/download.php?abc=123&def=456\">'test' </test>".toCharArray();
        final String expected = "&lt;this is test of char update for xml href=&quot; see link: http://www.ibm.com/download.php?abc=123&amp;def=456&quot;&gt;&apos;test&apos; &lt;/test&gt;";
        result = XMLUtils.escapeXML(input, 0, input.length);
        assertEquals(expected, result);
    }
    
    @Test
    public void testNonDitaContext() {
        /* Queue assumes the following values:
         * <topic class="- topic/topic ">...
         *  <body class="- topic/body ">
         *   <foreign class="- topic/foreign ">
         *    <NONDITA class="nondita">
         *      <moreNonDita/>
         *    </NONDITA>
         *    <ditaInForeign class="- topic/xref foreign-d/ditaInForeign ">
         *      <ph class="- topic/ph "/>
         *    <unknown class="- topic/unknown ">
         *     <moreNonDita><more/></moreNonDita>
         *    </unknown></ditaInForeign></foreign></body></topic>
         */
        Deque<DitaClass> classes = new LinkedList<>();
        classes.addFirst(new DitaClass("- topic/topic "));
        assertFalse(XMLUtils.nonDitaContext(classes));
        classes.addFirst(new DitaClass("- topic/body "));
        assertFalse(XMLUtils.nonDitaContext(classes));
        classes.addFirst(new DitaClass("- topic/foreign "));
        assertFalse(XMLUtils.nonDitaContext(classes));
        classes.addFirst(new DitaClass("nondita"));
        assertTrue(XMLUtils.nonDitaContext(classes));
        classes.addFirst(new DitaClass(""));
        assertTrue(XMLUtils.nonDitaContext(classes));
        classes.pop();
        classes.pop();
        classes.addFirst(new DitaClass("+ topic/xref foreign-d/ditaInForeign "));
        assertTrue(XMLUtils.nonDitaContext(classes));
        classes.addFirst(new DitaClass("+ topic/ph "));
        assertFalse(XMLUtils.nonDitaContext(classes));
        classes.pop();
        classes.pop();
        classes.addFirst(new DitaClass("- topic/unknown "));
        assertTrue(XMLUtils.nonDitaContext(classes));
        classes.addFirst(null);
        assertTrue(XMLUtils.nonDitaContext(classes));
        classes.addFirst(null);
        assertTrue(XMLUtils.nonDitaContext(classes));
    }

    @Test
    public void withLogger() throws TransformerException {
        final String file = "<xsl:stylesheet version=\"2.0\" xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\">\n" +
                "  <xsl:template match='/'>\n" +
                "    <xsl:message>info</xsl:message>\n" +
                "    <xsl:message><info/></xsl:message>\n" +
                "  </xsl:template>\n" +
                "</xsl:stylesheet>";
        final Transformer base = TransformerFactory.newInstance().newTransformer(
                new StreamSource(new StringReader(file)));
        final CachingLogger logger = new CachingLogger();

        XMLUtils.withLogger(base, logger)
                .transform(
                        new StreamSource(new StringReader(file)),
                        new StreamResult(new ByteArrayOutputStream()));

        assertEquals(Arrays.asList(
                new Message(Message.Level.WARN, "info", null),
                new Message(Message.Level.WARN, "<info/>", null)
                ),
                logger.getMessages());
    }

//    @Test
//    public void transform() throws Exception {
//        copyFile(new File(srcDir, "test.dita"), new File(tempDir, "test.dita"));
//        final Job job = new Job(tempDir);
//        final URI src = new File(tempDir, "test.dita").toURI();
//
//        // two filters that assume processing order
//        final URI act = new File(tempDir, "order.dita").toURI();
//        job.transform(src, act, Arrays.asList(
//            (XMLFilter) new XMLFilterImpl() {
//                @Override
//                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
//                    getContentHandler().startElement(uri, localName + "_x", qName + "_x", atts);
//                }
//                @Override
//                public void endElement(final String uri, final String localName, final String qName) throws SAXException {
//                    getContentHandler().endElement(uri, localName + "_x", qName + "_x");
//                }
//            },
//            (XMLFilter) new XMLFilterImpl() {
//                @Override
//                public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
//                    getContentHandler().startElement(uri, localName + "_y", qName + "_y", atts);
//                }
//                @Override
//                public void endElement(final String uri, final String localName, final String qName) throws SAXException {
//                    getContentHandler().endElement(uri, localName + "_y", qName + "_y");
//                }
//            }));
//        TestUtils.assertXMLEqual(new InputSource(new File(expDir, "order.dita").toURI().toString()),
//                new InputSource(new File(tempDir, "order.dita").toURI().toString()));
//    }
//
//    @Test
//    public void transform_single() throws Exception {
//        copyFile(new File(srcDir, "test.dita"), new File(tempDir, "test.dita"));
//        final Job job = new Job(tempDir);
//        final URI src = new File(tempDir, "test.dita").toURI();
//
//        // single filter that prefixes each element name
//        final URI act = new File(tempDir, "single.dita").toURI();
//        job.transform(src, act, Arrays.asList((XMLFilter) new XMLFilterImpl() {
//            @Override
//            public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
//                getContentHandler().startElement(uri, localName + "_x", qName + "_x", atts);
//            }
//            @Override
//            public void endElement(final String uri, final String localName, final String qName) throws SAXException {
//                getContentHandler().endElement(uri, localName + "_x", qName + "_x");
//            }
//        }));
//        TestUtils.assertXMLEqual(new InputSource(new File(expDir, "single.dita").toURI().toString()),
//                       new InputSource(new File(tempDir, "single.dita").toURI().toString()));
//    }
//
//    @Test
//    public void transform_empty() throws Exception {
//        copyFile(new File(srcDir, "test.dita"), new File(tempDir, "test.dita"));
//        final Job job = new Job(tempDir);
//        final URI src = new File(tempDir, "test.dita").toURI();
//
//        // identity without a filter
//        final URI act = new File(tempDir, "identity.dita").toURI();
//        job.transform(src, act, Collections.EMPTY_LIST);
//        TestUtils.assertXMLEqual(new InputSource(new File(expDir, "identity.dita").toURI().toString()),
//                       new InputSource(new File(tempDir, "identity.dita").toURI().toString()));
//    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
