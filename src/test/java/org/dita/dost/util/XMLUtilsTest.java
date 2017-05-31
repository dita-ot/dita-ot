/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static javax.xml.XMLConstants.*;
import static org.junit.Assert.*;

import java.util.Deque;
import java.util.LinkedList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Attr;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.junit.Test;

public class XMLUtilsTest {

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

}
