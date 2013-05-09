/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.*;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import org.dita.dost.TestUtils;

public class KeyrefPaserTest {

    private static File tempDir;
    private static final File resourceDir = TestUtils.getResourceDir(KeyrefPaserTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static CatalogResolver resolver;

    private static Map<String, Element> keyDefinition;
    private final static Map<String, String> keymap = new HashMap<String, String>();

    @BeforeClass
    public static void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(KeyrefPaserTest.class);
        TestUtils.normalize(new File(srcDir, "a.xml"), new File(tempDir, "a.xml"));
        TestUtils.normalize(new File(srcDir, "b.ditamap"), new File(tempDir, "b.ditamap"));
        resolver = new CatalogResolver();

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
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File("a.xml"));
        parser.setTempDir(tempDir);
        parser.setKeyMap(keymap);
        parser.write(new File("a.xml").getPath());

        assertXMLEqual(new InputSource(new File(expDir, "a.xml").toURI().toString()),
                new InputSource(new File(tempDir, "a.xml").toURI().toString()));
    }

    @Test
    public void testMapWrite() throws Exception {
        final KeyrefPaser parser = new KeyrefPaser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setKeyDefinition(keyDefinition);
        parser.setCurrentFile(new File("b.ditamap"));
        parser.setTempDir(tempDir);
        parser.setKeyMap(keymap);
        parser.write(new File("b.ditamap").getPath());

        assertXMLEqual(new InputSource(new File(expDir, "b.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "b.ditamap").toURI().toString()));
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
        for (int i = 0; i < keydefs.getLength(); i++) {
            final Element keydef = (Element) keydefs.item(i);
            keymap.put(keydef.getAttribute("keys"), keydef.getAttribute("href"));
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.appendChild(doc.importNode(keydef, true));
            keys.put(keydef.getAttribute("keys"), keydef);
        }
        keyDefinition = Collections.unmodifiableMap(keys);
    }
    
}
