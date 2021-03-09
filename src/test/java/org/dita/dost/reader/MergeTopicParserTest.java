/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.TestUtils;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;

import static javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.junit.Assert.assertEquals;

public class MergeTopicParserTest {

    final File resourceDir = TestUtils.getResourceDir(MergeTopicParserTest.class);
    private final File srcDir = new File(resourceDir, "src");
    private final File expDir = new File(resourceDir, "exp");

    private static SAXTransformerFactory stf;
    private MergeTopicParser parser;

    @BeforeClass
    public static void setupClass() {
        final TransformerFactory tf = TransformerFactory.newInstance();
        if (!tf.getFeature(SAXTransformerFactory.FEATURE)) {
            throw new RuntimeException("SAX transformation factory not supported");
        }
        stf = (SAXTransformerFactory) tf;
    }

    @Before
    public void setUp() throws IOException {
        final Job job = new Job(srcDir, new StreamStore(srcDir, new XMLUtils()));
        final TestUtils.TestLogger logger = new TestUtils.TestLogger();
        final MergeUtils util = new MergeUtils();
        util.setLogger(logger);
        util.setJob(job);
        parser = new MergeTopicParser(util);
        parser.setLogger(logger);
        parser.setJob(job);
    }

    @Test
    public void testParse() throws Exception {
        parse(parser, "test.xml");

        final Method method = MergeTopicParser.class.getDeclaredMethod("handleLocalHref", URI.class);
        method.setAccessible(true);
        assertEquals(new URI("images/test.jpg"), method.invoke(parser, new URI("images/test.jpg")));
    }

    @Test
    public void testParseSubdir() throws Exception {
        parse(parser, "subdir/test3.xml");
    }

    public void parse(MergeTopicParser parser, String file) throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setIgnoringComments(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document output = builder.newDocument();
        final TransformerHandler s = stf.newTransformerHandler();
        s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION, "yes");
        s.setResult(new DOMResult(output));
        parser.setContentHandler(s);
        parser.setLogger(new TestUtils.TestLogger());
        parser.setOutput(new File(srcDir, file));
        s.startDocument();
        parser.parse(file, srcDir.getAbsoluteFile());
        s.endDocument();
        assertXMLEqual(builder.parse(new File(expDir, file)), output);
    }

    @Test
    public void testHandleLocalHref() throws Exception {
        parser.setContentHandler(new DefaultHandler());
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

    @Test
    public void testHandleLocalHrefCacheStore() throws Exception {
        final File tmpDir = new File(resourceDir, "tmpRandom");
        final CacheStore store = new CacheStore(tmpDir, new XMLUtils());
        final Job job = new Job(srcDir, store);
        TestUtils.TestLogger logger = new TestUtils.TestLogger();
        final MergeUtils util = new MergeUtils();
        util.setLogger(logger);
        util.setJob(job);
        final MergeTopicParser parser = new MergeTopicParser(util);
        parser.setLogger(logger);
        parser.setJob(job);
        parser.setContentHandler(new DefaultHandler());
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document doc = builder.parse(new File(srcDir, "test.xml"));
        final URI uri = new File(tmpDir, "test.xml").toURI();
        store.writeDocument(doc, uri);

        parser.setOutput(new File(tmpDir, "test.xml"));
        final Method method = MergeTopicParser.class.getDeclaredMethod("handleLocalDita", URI.class, AttributesImpl.class);
        method.setAccessible(true);

        parser.parse("test.xml", tmpDir.getAbsoluteFile());
        final AttributesImpl attrs = new AttributesImpl();
        final URI val = (URI) method.invoke(parser, new URI("test.jpg"), attrs);
        final String original = attrs.getValue("ohref");
        assertEquals("test.jpg", original);
        assertEquals("#unique_7", val.toString());
        assertEquals("unique_7", util.getIdValue(new File(tmpDir, "test.jpg").toURI()));
    }

}
