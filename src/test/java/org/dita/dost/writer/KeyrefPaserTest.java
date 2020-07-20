/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.createTempDir;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;

import org.apache.commons.io.IOUtils;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.dita.dost.TestUtils;

public class KeyrefPaserTest {

    private static final File resourceDir = TestUtils.getResourceDir(KeyrefPaserTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    private KeyScope keyDefinition;
    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = createTempDir(KeyrefPaserTest.class);
        TestUtils.copy(srcDir, tempDir);

        keyDefinition = readKeyMap(Paths.get("keys.ditamap"));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    @Test
    public void testTopicWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File(tempDir, "a.xml").toURI());
        parser.write(new File(tempDir, "a.xml"));

        assertXMLEqual(new InputSource(new File(expDir, "a.xml").toURI().toString()),
                new InputSource(new File(tempDir, "a.xml").toURI().toString()));
    }
    
    @Test
    public void testTopicWriteSubdir() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(keyDefinition);
        final String tempSubDir = tempDir + File.separator + "subdir";
        parser.setCurrentFile(new File(tempSubDir, "subdirtopic.xml").toURI());
        parser.write(new File(tempSubDir, "subdirtopic.xml"));

        assertXMLEqual(new InputSource(new File(expDir + File.separator + "subdir", "subdirtopic.xml").toURI().toString()),
                new InputSource(new File(tempSubDir, "subdirtopic.xml").toURI().toString()));
    }

    @Test
    public void testFragment() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File(tempDir, "id.xml").toURI());
        parser.write(new File(tempDir, "id.xml"));

        assertXMLEqual(new InputSource(new File(expDir, "id.xml").toURI().toString()),
                new InputSource(new File(tempDir, "id.xml").toURI().toString()));
    }
    
    @Test
    public void testFallback() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File(tempDir, "fallback.xml").toURI());
        parser.write(new File(tempDir, "fallback.xml"));

        assertXMLEqual(new InputSource(new File(expDir, "fallback.xml").toURI().toString()),
                new InputSource(new File(tempDir, "fallback.xml").toURI().toString()));
    }

    @Test
    public void testMapWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File(tempDir, "b.ditamap").toURI());
        parser.write(new File(tempDir, "b.ditamap"));

        assertXMLEqual(new InputSource(new File(expDir, "b.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "b.ditamap").toURI().toString()));
    }

    @Test
    public void testUpLevelMapWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(readKeyMap(Paths.get("subdir", "c.ditamap")));
        parser.setCurrentFile(new File(tempDir, "subdir"+ File.separator +"c.ditamap").toURI());
        parser.write(new File(tempDir, "subdir"+ File.separator +"c.ditamap"));

        assertXMLEqual(new InputSource(new File(expDir, "subdir"+ File.separator +"c.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "subdir"+ File.separator +"c.ditamap").toURI().toString()));
    }

    @Test
    public void testMapWithKeyScopes() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File(tempDir, "d.ditamap").toURI());
        parser.write(new File(tempDir, "d.ditamap"));

        System.err.println(new File(expDir, "d.ditamap"));
        System.err.println(new File(tempDir, "d.ditamap"));
        assertXMLEqual(new InputSource(new File(expDir, "d.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "d.ditamap").toURI().toString()));
    }

    @Test
    public void testDomToSax() throws TransformerConfigurationException, SAXException, IOException, ParserConfigurationException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        
        assertXMLEqual(b.parse(new InputSource(new StringReader("<wrapper>bar qux quxx</wrapper>"))),
                domToSax(b.parse(new InputSource(new StringReader("<foo>bar <baz>qux</baz> quxx</foo>"))), false));
        assertXMLEqual(b.parse(new InputSource(new StringReader("<wrapper><foo>bar <baz>qux</baz> quxx</foo></wrapper>"))),
                domToSax(b.parse(new InputSource(new StringReader("<foo>bar <baz>qux</baz> quxx</foo>"))), true));
        
        assertXMLEqual(b.parse(new InputSource(new StringReader("<wrapper><foo class='- topic/linktext '>bar <baz class='- topic/linktext '>qux</baz> quxx</foo></wrapper>"))),
                domToSax(b.parse(new InputSource(new StringReader("<foo class='- map/linktext '>bar <baz class='- map/linktext '>qux</baz> quxx</foo>"))), true));
        
        assertXMLEqual(b.parse(new InputSource(new StringReader("<wrapper>bar <baz class='- topic/tm '>qux</baz> quxx</wrapper>"))),
                domToSax(b.parse(new InputSource(new StringReader("<foo>bar <baz class='- topic/tm '>qux</baz> quxx</foo>"))), false));
    }
    
    private Document domToSax(final Document doc, final boolean retain) throws TransformerConfigurationException, SAXException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final DOMResult r = new DOMResult();
        final SAXTransformerFactory f = ((SAXTransformerFactory) TransformerFactory.newInstance());
        final TransformerHandler h = f.newTransformerHandler();
        h.setResult(r);
        
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setContentHandler(h);
        
        final Method m = KeyrefPaser.class.getDeclaredMethod("domToSax", Element.class, boolean.class);
        m.setAccessible(true);
        
        h.startDocument();
        h.startElement("", "wrapper", "wrapper", new AttributesImpl());
        m.invoke(parser, doc.getDocumentElement(), retain);
        h.endElement("", "wrapper", "wrapper");
        h.endDocument();
        
        return (Document) r.getNode();
    }

    private KeyScope readKeyMap(final Path map) throws Exception {
        KeyrefReader reader = new KeyrefReader();
        final URI keyMapFile = tempDir.toPath().resolve(map).toUri();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final InputSource inputSource = new InputSource(keyMapFile.toString());
        final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        final Document document = documentBuilder.parse(inputSource);

        reader.read(keyMapFile, document);

        return reader.getKeyDefinition();
    }

}
