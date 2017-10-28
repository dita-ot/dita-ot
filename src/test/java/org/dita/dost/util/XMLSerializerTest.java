/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.sax.TransformerHandler;


import org.dita.dost.TestUtils;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
import org.xml.sax.SAXException;
import org.junit.Test;

import static org.dita.dost.TestUtils.assertXMLEqual;

public class XMLSerializerTest {

    final File resourceDir = TestUtils.getResourceDir(XMLSerializerTest.class);
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

        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                new InputSource(new StringReader(buf.toString())));
    }

    @Test
    public void testEndDocument() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);

        serializer.writeStartDocument();
        serializer.writeStartElement("first");
        serializer.writeStartElement("second");
        serializer.writeStartElement("third");
        serializer.writeEndDocument();
        serializer.close();

        assertXMLEqual(new InputSource(new StringReader("<first><second><third/></second></first>")),
                new InputSource(new StringReader(buf.toString())));
    }

    @Test
    public void testCharactersString() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);

        serializer.writeStartDocument();
        serializer.writeStartElement("data");
        serializer.writeCharacters("first");
        serializer.writeEndDocument();
        serializer.close();

        assertXMLEqual(new InputSource(new StringReader("<data>first</data>")),
                new InputSource(new StringReader(buf.toString())));
    }

    @Test
    public void testCharactersArray() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);

        serializer.writeStartDocument();
        serializer.writeStartElement("data");
        final char[] first = "first".toCharArray();
        serializer.writeCharacters(first, 0, first.length);
        serializer.writeEndDocument();
        serializer.close();

        assertXMLEqual(new InputSource(new StringReader("<data>first</data>")),
                new InputSource(new StringReader(buf.toString())));
    }

    @Test
    public void testAttribute() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);
        serializer.getTransformerHandler().getTransformer().setOutputProperty("indent", "yes");

        serializer.writeStartDocument();
        serializer.writeStartElement("root");

        serializer.writeStartElement("att");
        serializer.writeAttribute("att", "value");
        serializer.writeEndElement();

        serializer.writeStartElement("att");
        serializer.writeAttribute("uri1", "ns1:att", "value");
        serializer.writeAttribute("uri2", "ns2:att", "value");
        serializer.writeAttribute("uri3", "ns3:att", "value");
        serializer.writeEndElement();

        serializer.writeEndDocument();
        serializer.close();

        final DocumentBuilder builder;
        try {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setIgnoringComments(true);
            builder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
        assertXMLEqual(builder.parse(new InputSource(new StringReader("<root>" +
                "<att att='value'/>" +
                "<att xmlns:ns1='uri1' xmlns:ns2='uri2' xmlns:ns3='uri3' ns1:att='value' ns2:att='value' ns3:att='value'/>" +
                "</root>"))),
                builder.parse(new InputSource(new StringReader(buf.toString()))));
    }

    @Test
    public void testNamespacePrefix() throws SAXException, IOException {
        final StringWriter buf = new StringWriter();
        final XMLSerializer serializer = XMLSerializer.newInstance(buf);
        serializer.getTransformerHandler().getTransformer().setOutputProperty("indent", "yes");

        serializer.writeStartDocument();
        serializer.writeStartElement("root");

        serializer.writeStartElement("uri1", "ns1:same-uri");
        serializer.writeStartElement("uri1", "ns1:same-uri");
        serializer.writeStartElement("uri1", "ns1:same-uri");
        serializer.writeEndElement();
        serializer.writeEndElement();
        serializer.writeEndElement();

        serializer.writeStartElement("uri1", "ns1:different-uri");
        serializer.writeStartElement("uri2", "ns1:different-uri");
        serializer.writeStartElement("uri1", "ns1:different-uri");
        serializer.writeEndElement();
        serializer.writeEndElement();
        serializer.writeEndElement();

        serializer.writeStartElement("uri1", "ns1:different-prefix");
        serializer.writeStartElement("uri1", "ns2:different-prefix");
        serializer.writeStartElement("uri1", "ns3:different-prefix");
        serializer.writeEndElement();
        serializer.writeEndElement();
        serializer.writeEndElement();

        serializer.writeStartElement("uri1", "ns1:different-prefix");
        serializer.writeStartElement("uri1", "ns2:different-prefix");
        serializer.writeStartElement("uri1", "ns1:different-prefix");
        serializer.writeEndElement();
        serializer.writeEndElement();
        serializer.writeEndElement();

        serializer.writeEndDocument();
        serializer.close();

        assertXMLEqual(new InputSource(new StringReader("<root>" +
                "<ns1:same-uri xmlns:ns1='uri1'>" +
                "<ns1:same-uri xmlns:ns1='uri1'>" +
                "<ns1:same-uri/>" +
                "</ns1:same-uri>" +
                "</ns1:same-uri>" +
                "<ns1:different-uri xmlns:ns1='uri1'>" +
                "<ns1:different-uri xmlns:ns1='uri2'>" +
                "<ns1:different-uri xmlns:ns1='uri1'/>" +
                "</ns1:different-uri>" +
                "</ns1:different-uri>" +
                "<ns1:different-prefix xmlns:ns1='uri1'>" +
                "<ns2:different-prefix xmlns:ns2='uri1'>" +
                "<ns3:different-prefix xmlns:ns3='uri1'/>" +
                "</ns2:different-prefix>" +
                "</ns1:different-prefix>" +
                "<ns1:different-prefix xmlns:ns1='uri1'>" +
                "<ns2:different-prefix xmlns:ns2='uri1'>" +
                "<ns1:different-prefix/>" +
                "</ns2:different-prefix>" +
                "</ns1:different-prefix>" +
                "</root>")),
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

        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                new InputSource(new StringReader(buf.toString())));
    }


}
