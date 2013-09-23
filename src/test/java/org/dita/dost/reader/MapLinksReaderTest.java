package org.dita.dost.reader;

import static junit.framework.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.*;
import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.xml.resolver.tools.CatalogResolver;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.dita.dost.TestUtils;

public class MapLinksReaderTest {
    
    private static final File resourceDir = TestUtils.getResourceDir(MapLinksReaderTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static MapLinksReader reader;

    @BeforeClass
    public static void setUp() throws Exception {
        reader = new MapLinksReader();
        reader.setLogger(new TestUtils.TestLogger());
        reader.setMatch(new StringBuffer(ELEMENT_NAME_MAPLINKS)
                .append(SLASH).append(TOPIC_LINKPOOL.localName)
                .append(SLASH).append(TOPIC_LINKLIST.localName)
                .toString());

        reader.read(new File(srcDir, "maplinks.unordered").getAbsoluteFile());
    }

    @Test
    public void testRead() throws ParserConfigurationException, SAXException, IOException {
        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        db.setEntityResolver(new CatalogResolver());
        
        final Map<String, Document> expMap = readExpected(db);

        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreComments(true);

        final Map<String, Map<String, String>> mapSet = reader.getMapping();
        
        for (final Map.Entry<String, Map<String, String>> e: mapSet.entrySet()) {
            for (final Map.Entry<String, String> ee: e.getValue().entrySet()) {
                assertEquals(SHARP, ee.getKey());
                assertXMLEqual(expMap.get(e.getKey()),
                               db.parse(new InputSource(new StringReader(ee.getValue()))));
            }
        }
    }

    private Map<String, Document> readExpected(final DocumentBuilder db) throws SAXException, IOException {
        final Document expDoc = db.parse(new File(srcDir, "maplinks.unordered").toURI().toString());
        final Map<String, Document> expMap = new HashMap<String, Document>();
        final NodeList maplinks = expDoc.getElementsByTagName(ELEMENT_NAME_MAPLINKS);
        for (int i = 0; i < maplinks.getLength(); i++) {
            final Element m = (Element) maplinks.item(i);
            final Document d = expDoc.getImplementation().createDocument(null, m.getLocalName(), null);
            final NodeList cs = m.getChildNodes();
            for (int j = 0; j < cs.getLength(); j++) {
                final Node c = cs.item(j);
                if (c.getNodeType() == Node.ELEMENT_NODE) {
                    d.appendChild(d.importNode(c, true));
                }
            }
            expMap.put(new File(srcDir, m.getAttribute(ATTRIBUTE_NAME_HREF)).getAbsolutePath(), d);
        }
        return expMap;
    }

}
