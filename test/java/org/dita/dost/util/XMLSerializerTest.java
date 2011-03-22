/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.sax.TransformerHandler;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.util.XMLSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;
import org.junit.Test;

public class XMLSerializerTest {
    
    private final File resourceDir = new File("test-stub", XMLSerializerTest.class.getSimpleName());
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");
    
    @Test
    public void testSerializer() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);
        
        serializer.writeStartDocument();
        serializer.writeStartElement("topic");
        serializer.writeAttribute("class", "- topic/topic ");
        serializer.writeCharacters("\n" +
        		"foo" + "\n" +
        		"&<" + "\n" +
        		"bar" + "\n" +
        		"entity" + "\n");
        serializer.writeComment("foo & <");
        serializer.writeCharacters("\n");
        serializer.writeProcessingInstruction("foo", "bar & <");
        serializer.writeProcessingInstruction("baz", "");
        serializer.writeCharacters("\n" + "bar & <" + "\n");
        serializer.writeStartElement("empty");
        serializer.writeEndElement(); // empty
        serializer.writeStartElement("empty");
        serializer.writeAttribute("with", "attribute");
        serializer.writeEndElement(); // empty
        serializer.writeStartElement("parent");
        serializer.writeCharacters("foo");
        serializer.writeStartElement("child");
        serializer.writeCharacters("bar");
        serializer.writeEndElement(); // child
        serializer.writeCharacters("baz");
        serializer.writeEndElement(); // parent
        serializer.writeCharacters("\n");
        serializer.writeStartElement("http://example.com/bar", "foo");
        serializer.writeEndElement(); // foo
        serializer.writeStartElement("http://example.com/baz", "ns:foo");
        serializer.writeStartElement("http://example.com/baz", "ns:bar");
        serializer.writeEndElement(); // ns:bar
        serializer.writeEndElement(); // ns:foo
        serializer.writeCharacters("\n");
        serializer.writeEndElement(); // topic
        serializer.writeEndDocument();
        serializer.close();
        
        XMLUnit.setNormalizeWhitespace(false);
        XMLUnit.setIgnoreWhitespace(false);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(false);
        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                       new InputSource(new StringReader(buf.toString())));
    }

    @Test
    public void testTransformer() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);
        final TransformerHandler transformer = serializer.getTransformerHandler();
        
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(transformer);
        parser.setProperty("http://xml.org/sax/properties/lexical-handler", transformer);
        Reader in = null;
        try {
            in = new FileReader(new File(srcDir, "test.xml"));
            parser.parse(new InputSource(in));
        } finally {
            in.close();
        }
        
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                       new InputSource(new StringReader(buf.toString())));
    }

    
}
