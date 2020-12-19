/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_CLASS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.helpers.AttributesImpl;


public class DitaClassTest {

    @Test
    public void testHashCode() {
        assertEquals(new DitaClass("- foo/bar baz/qux ").hashCode(), new DitaClass("- foo/bar baz/qux ").hashCode());
        assertEquals(new DitaClass("- foo/bar baz/qux ").hashCode(), new DitaClass("-  foo/bar  baz/qux  ").hashCode());
    }

    @Test
    public void testDitaClass() {
        new DitaClass("- foo/bar baz/qux ");
        try {
            new DitaClass(null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testEqualsObject() {
        assertTrue(new DitaClass("- foo/bar baz/qux ").equals(new DitaClass("- foo/bar baz/qux ")));
        assertTrue(new DitaClass("- foo/bar baz/qux ").equals(new DitaClass("-  foo/bar  baz/qux  ")));
    }

    @Test
    public void testToString() {
        assertEquals("- foo/bar baz/qux ", new DitaClass("- foo/bar baz/qux ").toString());
        assertEquals("- foo/bar baz/qux ", new DitaClass("-  foo/bar  baz/qux  ").toString());
    }

    @Test
    public void testLocalName() {
        assertEquals("qux", new DitaClass("- foo/bar baz/qux ").localName);
    }

    @Test
    public void testMatcher() {
        assertEquals(" baz/qux ", new DitaClass("- foo/bar baz/qux ").matcher);
    }

    @Test
    public void testMatchesDitaClass() {
        assertTrue(new DitaClass("- foo/bar ").matches(new DitaClass("- foo/bar baz/qux ")));
        assertTrue(new DitaClass("- foo/bar baz/qux ").matches(new DitaClass("- foo/bar baz/qux ")));
    }

    @Test
    public void testMatchesString() {
        assertTrue(new DitaClass("- foo/bar ").matches("- foo/bar baz/qux "));
        assertTrue(new DitaClass("- foo/bar baz/qux ").matches("- foo/bar baz/qux "));
    }

    @Test
    public void testMatchesAttributes() {
        final AttributesImpl atts = new AttributesImpl();
        atts.addAttribute("", ATTRIBUTE_NAME_CLASS, ATTRIBUTE_NAME_CLASS, "CDATA", "- foo/bar baz/qux ");
        assertTrue(new DitaClass("- foo/bar ").matches(atts));
        assertTrue(new DitaClass("- foo/bar baz/qux ").matches(atts));
        assertFalse(new DitaClass("- bar/baz ").matches(atts));
    }

    @Test
    public void testMatchesNode() throws ParserConfigurationException {
        final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final Element elem = doc.createElement("qux");
        elem.setAttribute(ATTRIBUTE_NAME_CLASS, "- foo/bar baz/qux ");
        assertTrue(new DitaClass("- foo/bar ").matches(elem));
        assertTrue(new DitaClass("- foo/bar baz/qux ").matches(elem));
        assertFalse(new DitaClass("- bar/baz ").matches(elem));
    }
    
    @Test
    public void testValidDitaClass() {
        assertTrue(new DitaClass("- topic/p ").isValid());
        assertTrue(new DitaClass("+ topic/p topic-d/domain_element ").isValid());
        assertTrue(new DitaClass("+ topic/p topic-d/domain_element   ex+d/x ").isValid());
        assertTrue(new DitaClass("- map/topicref ").isValid());
        assertTrue(new DitaClass("+ map/topicref map-d/domain_element ").isValid());
        assertFalse(new DitaClass("").isValid());
        assertFalse(new DitaClass("invalid syntax").isValid());
        assertFalse(new DitaClass("- close/but/invalid ").isValid());
        assertFalse(new DitaClass("- also\\invalid ").isValid());
    }

    @Test
    public void getInstance_same() {
        final DitaClass first = DitaClass.getInstance("- foo/bar ");
        final DitaClass second = DitaClass.getInstance("- foo/bar ");
        assertTrue(first == second);
    }

    @Test
    public void getInstance_differentWhitespace() {
        final DitaClass first = DitaClass.getInstance("- foo/bar ");
        final DitaClass second = DitaClass.getInstance("-  foo/bar  ");
        assertTrue(first == second);
    }

}
