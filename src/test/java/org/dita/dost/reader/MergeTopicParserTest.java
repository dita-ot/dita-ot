/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.reader;

import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;

import org.xml.sax.InputSource;
import org.dita.dost.TestUtils;
import org.dita.dost.util.MergeUtils;
import org.junit.Test;

public class MergeTopicParserTest {

    final File resourceDir = TestUtils.getResourceDir(MergeTopicParserTest.class);
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");

    private static SAXTransformerFactory stf;
    
    @BeforeClass
    public static void setup() {
        final TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(SAXTransformerFactory.FEATURE)) {
            throw new RuntimeException("SAX transformation factory not supported");
        }
        stf = (SAXTransformerFactory) tf;
    }

    @Test
    public void testParse() throws Exception {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final TransformerHandler s = stf.newTransformerHandler();
        s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION , "yes");
        s.setResult(new StreamResult(output));
        parser.setContentHandler(s);
        parser.setLogger(new TestUtils.TestLogger());
        s.startDocument();
        parser.parse("test.xml", srcDir.getAbsolutePath());
        s.endDocument();
        assertXMLEqual(new InputSource(new File(expDir, "test.xml").toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }

}
