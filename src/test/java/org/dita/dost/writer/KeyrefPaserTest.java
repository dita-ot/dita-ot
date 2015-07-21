/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
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
import org.custommonkey.xmlunit.XMLUnit;
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
    private static final File resourceDir = TestUtils.getResourceDir(KeyrefPaserTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static CatalogResolver resolver;

    private static KeyScope keyDefinition;

    @BeforeClass
    public static void setUp() throws Exception {
        CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
        tempDir = TestUtils.createTempDir(KeyrefPaserTest.class);
        TestUtils.normalize(new File(srcDir, "a.xml"), new File(tempDir, "a.xml"));
        TestUtils.normalize(new File(srcDir, "b.ditamap"), new File(tempDir, "b.ditamap"));
        resolver = CatalogUtils.getCatalogResolver();

        TestUtils.resetXMLUnit();
        XMLUnit.setControlEntityResolver(resolver);
        XMLUnit.setTestEntityResolver(resolver);
        XMLUnit.setURIResolver(resolver);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);

        readKeyMap();
    }

    @Test
    public void testTopicWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File("a.xml"));
        parser.write(new File("a.xml"));

        assertXMLEqual(new InputSource(new File(expDir, "a.xml").toURI().toString()),
                new InputSource(new File(tempDir, "a.xml").toURI().toString()));
    }

    @Test
    public void testMapWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir));
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File("b.ditamap"));
        parser.write(new File("b.ditamap"));

        assertXMLEqual(new InputSource(new File(expDir, "b.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "b.ditamap").toURI().toString()));
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

    private static void readKeyMap() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final InputSource inputSource = new InputSource(new File(srcDir, "keys.ditamap").toURI().toString());
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
            final KeyDef keyDef = new KeyDef(elem.getAttribute("keys"), new URI(elem.getAttribute("href")), null, null, elem);
            keymap.put(keyDef.keys, keyDef);
        }
        keyDefinition = new KeyScope(keymap);
    }
    
}
