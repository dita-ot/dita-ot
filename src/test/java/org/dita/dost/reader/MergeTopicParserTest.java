/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static org.junit.Assert.*;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;
import org.custommonkey.xmlunit.XMLUnit;
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
        
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void testParse() throws Exception {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        parse(parser, "test.xml");

        final Method method = MergeTopicParser.class.getDeclaredMethod("handleLocalHref", URI.class);
        method.setAccessible(true);
        assertEquals(new URI("images/test.jpg"), method.invoke(parser, new URI("images/test.jpg")));
    }

    @Test
    public void testParseSubdir() throws Exception {
        parse(new MergeTopicParser(new MergeUtils()), "subdir/test3.xml");
    }

    public void parse(MergeTopicParser parser, String file) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final TransformerHandler s = stf.newTransformerHandler();
        s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION , "yes");
        s.setResult(new StreamResult(output));
        parser.setContentHandler(s);
        parser.setLogger(new TestUtils.TestLogger());
        parser.setOutput(new File(srcDir, file));
        s.startDocument();
        parser.parse(file, srcDir.getAbsoluteFile());
        s.endDocument();
        assertXMLEqual(new InputSource(new File(expDir, file).toURI().toString()),
                new InputSource(new ByteArrayInputStream(output.toByteArray())));
    }

    @Test
    public void testHandleLocalHref() throws Exception {
        final MergeTopicParser parser = new MergeTopicParser(new MergeUtils());
        parser.setContentHandler(new DefaultHandler());
        parser.setLogger(new TestUtils.TestLogger());
        parser.setOutput(new File(srcDir, "test.xml"));
        parser.parse("test.xml", srcDir.getAbsoluteFile());
        final Method method = MergeTopicParser.class.getDeclaredMethod("handleLocalHref", URI.class);
        method.setAccessible(true);
        
        parser.parse("test.xml", srcDir.getAbsoluteFile());
        assertEquals(new URI("test.jpg"), method.invoke(parser, new URI("test.jpg")));
        assertEquals(new URI("test.jpg#foo"), method.invoke(parser, new URI("test.jpg#foo")));
        assertEquals(new URI("#foo"), method.invoke(parser, new URI("#foo")));
        assertEquals(new URI("images/test.jpg"), method.invoke(parser, new URI("images/test.jpg")));
        assertEquals(new URI("images/test.jpg#foo"), method.invoke(parser, new URI("images/test.jpg#foo")));
        assertEquals(new URI("../test.jpg"), method.invoke(parser, new URI("../test.jpg")));
        
        parser.setOutput(new File(srcDir, "test.xml"));
        parser.parse("src" + File.separator + "test.xml", resourceDir.getAbsoluteFile());
        assertEquals(new URI("test.jpg"), method.invoke(parser, new URI("test.jpg")));
        assertEquals(new URI("images/test.jpg"), method.invoke(parser, new URI("images/test.jpg")));
        assertEquals(new URI("../test.jpg"), method.invoke(parser, new URI("../test.jpg")));
    }
    
}
