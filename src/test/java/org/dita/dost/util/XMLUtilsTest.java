/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.sf.saxon.Configuration;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.ErrorReporter;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.MessageListener2;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.SaxonApiUncheckedException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.sapling.Saplings;
import net.sf.saxon.trans.SymbolicName;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.trans.XmlProcessingException;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.module.DelegatingCollationUriResolverTest;
import org.dita.dost.util.Configuration.Mode;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

public class XMLUtilsTest {

  private static final File resourceDir = TestUtils.getResourceDir(XMLUtilsTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");
  private static File tempDir;

  @BeforeAll
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
    assertTrue(configuration.getIntegratedFunctionLibrary().isAvailable(functionName, 20));
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
    final Attr att = doc.getDocumentElement().getAttributeNode("foo");
    XMLUtils.addOrSetAttribute(atts, att);

    final int i = atts.getIndex(NULL_NS_URI, "foo");
    assertEquals(NULL_NS_URI, atts.getURI(i));
    assertEquals("foo", atts.getQName(i));
    assertEquals("foo", atts.getLocalName(i));
    assertEquals("foo", atts.getValue(i));

    doc.getDocumentElement().setAttributeNS(XML_NS_URI, "xml:lang", "en");
    final Attr lang = doc.getDocumentElement().getAttributeNodeNS(XML_NS_URI, "lang");
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
    final String input =
      "<this is test of char update for xml href=\" see link: http://www.ibm.com/download.php?abc=123&def=456\">'test' </test>";
    final String expected =
      "&lt;this is test of char update for xml href=&quot; see link: http://www.ibm.com/download.php?abc=123&amp;def=456&quot;&gt;&apos;test&apos; &lt;/test&gt;";
    result = XMLUtils.escapeXML(input);
    assertEquals(expected, result);
  }

  @Test
  public void testEscapeXMLCharArrayIntInt() {
    String result = null;
    final char[] input =
      "<this is test of char update for xml href=\" see link: http://www.ibm.com/download.php?abc=123&def=456\">'test' </test>".toCharArray();
    final String expected =
      "&lt;this is test of char update for xml href=&quot; see link: http://www.ibm.com/download.php?abc=123&amp;def=456&quot;&gt;&apos;test&apos; &lt;/test&gt;";
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
    classes.addFirst(TOPIC_TOPIC);
    assertFalse(XMLUtils.nonDitaContext(classes));
    classes.addFirst(TOPIC_BODY);
    assertFalse(XMLUtils.nonDitaContext(classes));
    classes.addFirst(TOPIC_FOREIGN);
    assertFalse(XMLUtils.nonDitaContext(classes));
    classes.addFirst(DitaClass.getInstance("nondita"));
    assertTrue(XMLUtils.nonDitaContext(classes));
    classes.addFirst(DitaClass.getInstance(""));
    assertTrue(XMLUtils.nonDitaContext(classes));
    classes.pop();
    classes.pop();
    classes.addFirst(DitaClass.getInstance("+ topic/xref foreign-d/ditaInForeign "));
    assertTrue(XMLUtils.nonDitaContext(classes));
    classes.addFirst(TOPIC_PH);
    assertFalse(XMLUtils.nonDitaContext(classes));
    classes.pop();
    classes.pop();
    classes.addFirst(TOPIC_UNKNOWN);
    assertTrue(XMLUtils.nonDitaContext(classes));
    classes.addFirst(null);
    assertTrue(XMLUtils.nonDitaContext(classes));
    classes.addFirst(null);
    assertTrue(XMLUtils.nonDitaContext(classes));
  }

  @Test
  public void toMessageListener() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.LAX);

    final XdmNode msg = Saplings
      .doc()
      .withChild(Saplings.text("message "), Saplings.elem("debug"))
      .toXdmNode(new XMLUtils().getProcessor());

    listener.message(msg, null, false, null);

    final List<Message> act = logger.getMessages();
    assertEquals(1, act.size());
    assertEquals(new Message(Message.Level.INFO, "message <debug/>", null), act.get(0));
  }

  @Test
  public void toMessageListener_withLevelAndErrorCode() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.LAX);

    final XdmNode msg = Saplings
      .doc()
      .withChild(
        Saplings.pi("error-code", "DOTX037W"),
        Saplings.pi("level", "WARN"),
        Saplings.text("message "),
        Saplings.elem("debug")
      )
      .toXdmNode(new XMLUtils().getProcessor());

    listener.message(msg, null, false, null);

    final List<Message> act = logger.getMessages();
    assertEquals(1, act.size());
    assertEquals(new Message(Message.Level.WARN, "message <debug/>", null), act.get(0));
  }

  @Test
  public void toMessageListener_withErrorCode() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.LAX);

    final XdmNode msg = Saplings
      .doc()
      .withChild(Saplings.pi("error-code", "DOTX037W"), Saplings.text("message "), Saplings.elem("debug"))
      .toXdmNode(new XMLUtils().getProcessor());

    listener.message(msg, null, false, null);

    final List<Message> act = logger.getMessages();
    assertEquals(1, act.size());
    assertEquals(new Message(Message.Level.INFO, "message <debug/>", null), act.get(0));
  }

  @Test
  public void toMessageListener_withFatalErrorCode() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.LAX);

    final XdmNode msg = Saplings
      .doc()
      .withChild(
        Saplings.pi("error-code", "DOTX037W"),
        Saplings.pi("level", "WARN"),
        Saplings.text("message "),
        Saplings.elem("debug")
      )
      .toXdmNode(new XMLUtils().getProcessor());

    try {
      listener.message(msg, null, true, null);
      fail();
    } catch (SaxonApiUncheckedException e) {
      final TerminationException cause = (TerminationException) e.getCause();
      assertEquals("DOTX037W", cause.getErrorCodeQName().getLocalPart());
      assertEquals("message <debug/>", cause.getMessage());
    }
  }

  @Test
  public void toMessageListener_withLevel() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.LAX);

    final XdmNode msg = Saplings
      .doc()
      .withChild(Saplings.pi("level", "ERROR"), Saplings.text("message "), Saplings.elem("debug"))
      .toXdmNode(new XMLUtils().getProcessor());

    listener.message(msg, null, false, null);

    final List<Message> act = logger.getMessages();
    assertEquals(1, act.size());
    assertEquals(new Message(Message.Level.ERROR, "message <debug/>", null), act.get(0));
  }

  @Test
  public void toMessageListener_withLevel_strictMode() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.STRICT);

    final XdmNode msg = Saplings
      .doc()
      .withChild(Saplings.pi("level", "ERROR"), Saplings.text("message "), Saplings.elem("debug"))
      .toXdmNode(new XMLUtils().getProcessor());

    try {
      listener.message(msg, null, false, null);
      fail();
    } catch (UncheckedXPathException e) {
      assertEquals("message <debug/>", e.getMessage());
    } catch (Throwable e) {
      fail();
    }
  }

  @Test
  public void toMessageListenerProperElemsSerialization() throws SaxonApiException {
    final CachingLogger logger = new CachingLogger();
    final MessageListener2 listener = XMLUtils.toMessageListener(logger, Mode.LAX);

    final XdmNode msg = Saplings
      .doc()
      .withChild(Saplings.text("abc "), Saplings.elem("def").withChild(Saplings.elem("hij")))
      .toXdmNode(new XMLUtils().getProcessor());

    listener.message(msg, null, false, null);

    final List<Message> act = logger.getMessages();
    assertEquals(1, act.size());
    assertEquals("""
                abc <def>
                   <hij/>
                </def>""", act.get(0).message);
  }

  @Test
  public void toErrorReporter_message() {
    final CachingLogger logger = new CachingLogger();
    final ErrorReporter errorReporter = XMLUtils.toErrorReporter(logger);
    errorReporter.report(new XmlProcessingException(new XPathException("msg")));

    assertEquals(1, logger.getMessages().size());
    final Message message = logger.getMessages().get(0);
    assertEquals("msg", message.message);
    assertEquals(null, message.exception);
    assertEquals(Message.Level.ERROR, message.level);
  }

  @Test
  public void toErrorReporter_location() {
    final CachingLogger logger = new CachingLogger();
    final ErrorReporter errorReporter = XMLUtils.toErrorReporter(logger);
    final LocatorImpl loc = new LocatorImpl();
    loc.setLineNumber(1);
    loc.setColumnNumber(2);
    loc.setSystemId("foo:///bar");
    errorReporter.report(new XmlProcessingException(new XPathException("msg", null, Loc.makeFromSax(loc))));

    assertEquals(1, logger.getMessages().size());
    final Message message = logger.getMessages().get(0);
    assertEquals("msg", message.message);
    assertEquals(null, message.exception);
    assertEquals(Message.Level.ERROR, message.level);
  }

  @Test
  public void toErrorReporter_FileNotFoundException() {
    final CachingLogger logger = new CachingLogger();
    final ErrorReporter errorReporter = XMLUtils.toErrorReporter(logger);
    final LocatorImpl loc = new LocatorImpl();
    loc.setLineNumber(1);
    loc.setColumnNumber(2);
    loc.setSystemId("foo:///bar");
    final XPathException exception = new XPathException("msg", new FileNotFoundException("cause"));
    exception.setLocation(Loc.makeFromSax(loc));
    errorReporter.report(new XmlProcessingException(exception));

    assertEquals(1, logger.getMessages().size());
    final Message message = logger.getMessages().get(0);
    assertEquals("foo:///bar:1:2: msg", message.message);
    assertEquals(null, message.exception);
    assertEquals(Message.Level.WARN, message.level);
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

  @AfterAll
  public static void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
