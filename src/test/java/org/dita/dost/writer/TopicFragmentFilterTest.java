package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TopicFragmentFilterTest {

    private static File tempDir;
    private static final File resourceDir = TestUtils.getResourceDir(TopicFragmentFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    
    @Before
    public void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(KeyrefPaserTest.class);
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempDir);
    }

    @Test
    public void test() throws Exception {
        final TopicFragmentFilter f = new TopicFragmentFilter(ATTRIBUTE_NAME_HREF);
        f.setParent(SAXParserFactory.newInstance().newSAXParser().getXMLReader());
                
        final DOMResult dst = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(new SAXSource(f, new InputSource(new File(srcDir, "topic.dita").toURI().toString())), dst);
        
        final Document exp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new File(expDir, "topic.dita").toURI().toString()));
        assertXMLEqual(exp, (Document) dst.getNode());
    }

}
