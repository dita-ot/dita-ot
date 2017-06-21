/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;

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

import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.dita.dost.TestUtils;
import org.dita.dost.util.CatalogUtils;

public class KeyrefPaserTest {

    private static File tempDir;
    private static File tempDirSubDir;
    private static final File resourceDir = TestUtils.getResourceDir(KeyrefPaserTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static CatalogResolver resolver;

    private static KeyScope keyDefinition;

    @BeforeClass
    public static void setUp() throws Exception {
        CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
        tempDir = TestUtils.createTempDir(KeyrefPaserTest.class);
        tempDirSubDir = new File(tempDir, "subdir");
        tempDirSubDir.mkdirs();
        TestUtils.normalize(new File(srcDir, "a.xml"), new File(tempDir, "a.xml"));
        TestUtils.normalize(new File(srcDir, "b.ditamap"), new File(tempDir, "b.ditamap"));
        TestUtils.normalize(new File(srcDir, "subdir" + File.separator + "c.ditamap"), new File(tempDir, "subdir" + File.separator + "c.ditamap"));
        TestUtils.normalize(new File(srcDir, "id.xml"), new File(tempDir, "id.xml"));
        TestUtils.normalize(new File(srcDir, "fallback.xml"), new File(tempDir, "fallback.xml"));
        resolver = CatalogUtils.getCatalogResolver();

        keyDefinition = readKeyMap(Paths.get("keys.ditamap"));
    }

    @Test
    public void testTopicWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File(tempDir, "a.xml").toURI());
        parser.write(new File(tempDir, "a.xml"));

        assertXMLEqual(new InputSource(new File(expDir, "a.xml").toURI().toString()),
                new InputSource(new File(tempDir, "a.xml").toURI().toString()));
    }

    @Test
    public void testFragment() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir));
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
        parser.setJob(new Job(tempDir));
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
        parser.setJob(new Job(tempDir));
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
        parser.setJob(new Job(tempDir));
        parser.setKeyDefinition(readKeyMap(Paths.get("subdir", "c.ditamap")));
        parser.setCurrentFile(new File(tempDir, "subdir"+ File.separator +"c.ditamap").toURI());
        parser.write(new File(tempDir, "subdir"+ File.separator +"c.ditamap"));

        assertXMLEqual(new InputSource(new File(expDir, "subdir"+ File.separator +"c.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "subdir"+ File.separator +"c.ditamap").toURI().toString()));
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
    

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    private static KeyScope readKeyMap(final Path map) throws Exception {
        final URI keyMapFile = srcDir.toPath().resolve(map).toUri();
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final InputSource inputSource = new InputSource(keyMapFile.toString());
        final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        documentBuilder.setEntityResolver(resolver);
        final Document document = documentBuilder.parse(inputSource);

        final Map<String, Element> keys = new HashMap<String, Element>();
        final NodeList keydefs = document.getElementsByTagName("keydef");
        final Map<String, KeyDef> keymap = new HashMap<>();
        for (int i = 0; i < keydefs.getLength(); i++) {
            final Element elem = (Element) keydefs.item(i);
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.appendChild(doc.importNode(elem, true));
            keys.put(elem.getAttribute("keys"), elem);
            final KeyDef keyDef = new KeyDef(elem.getAttribute("keys"), new URI(elem.getAttribute("href")),
                    null, null, tempDir.toPath().resolve(map).toUri(), elem);
            keymap.put(keyDef.keys, keyDef);
        }
        return new KeyScope(keymap);
    }
    
}
