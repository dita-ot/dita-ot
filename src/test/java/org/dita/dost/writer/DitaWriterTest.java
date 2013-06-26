/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.helpers.AttributesImpl;

import org.w3c.dom.NodeList;

import org.w3c.dom.Element;

import org.w3c.dom.Document;

import org.custommonkey.xmlunit.XMLUnit;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.XMLUtils;

public class DitaWriterTest {

    private static final File resourceDir = TestUtils.getResourceDir(DitaWriterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException, SAXException {
        tempDir = TestUtils.createTempDir(DitaWriterTest.class);
        final DitaWriter writer = new DitaWriter();
        writer.setLogger(new TestUtils.TestLogger());
        writer.setTempDir(tempDir.getAbsoluteFile());
        writer.initXMLReader(new File("src" + File.separator + "main").getAbsoluteFile(), false, true);
        writer.setExtName(".dita");
        writer.setTranstype("xhtml");
        final FilterUtils fu = new FilterUtils();
        fu.setLogger(new TestUtils.TestLogger());
        writer.setFilterUtils(fu);
        writer.setDelayConrefUtils(new DelayConrefUtils());
        final OutputUtils outputUtils = new OutputUtils();
        outputUtils.setInputMapPathName(new File(srcDir, "main.ditamap"));
        writer.setOutputUtils(outputUtils);
        writer.setKeyDefinitions(Arrays.asList(new KeyDef("keydef", "keyword.dita", ATTR_SCOPE_VALUE_LOCAL, "main.ditamap")));
        
        FileUtils.copyFile(new File(srcDir, FILE_NAME_EXPORT_XML), new File(tempDir, FILE_NAME_EXPORT_XML));

        for (final String f: new String[] {"main.ditamap", "keyword.dita"}) {
            writer.write(srcDir.getAbsoluteFile(), f);
        }
        
        TestUtils.resetXMLUnit();
    }

    @Test
    public void testGeneratedTopic() throws SAXException, IOException {
        final TestHandler handler = new TestHandler();
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(handler);
        InputStream in = null;
        try {
            in = new FileInputStream(new File(tempDir, "keyword.dita"));
            handler.setSource(new File(srcDir, "keyword.dita"));
            parser.parse(new InputSource(in));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Test
    public void testGeneratedMap() throws SAXException, IOException {
        final TestHandler handler = new TestHandler();
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(handler);
        InputStream in = null;
        try {
            in = new FileInputStream(new File(tempDir, "main.ditamap"));
            handler.setSource(new File(srcDir, "main.ditamap"));
            parser.parse(new InputSource(in));
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }

    @Test
    public void testWrite() throws Exception {
        final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        clean(db.parse(new File(tempDir, "keyword.dita")));

        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
        XMLUnit.setIgnoreComments(true);

        assertXMLEqual(clean(db.parse(new File(expDir, "keyword.dita"))),
                clean(db.parse(new File(tempDir, "keyword.dita"))));
        assertXMLEqual(clean(db.parse(new File(expDir, "main.ditamap"))),
                clean(db.parse(new File(tempDir, "main.ditamap"))));
    }

    private Document clean(final Document d) {
        final NodeList elems = d.getElementsByTagName("*");
        for (int i = 0; i < elems.getLength(); i++) {
            final Element e = (Element) elems.item(i);
            for (final String a: new String[] {"class", "domains", "xtrf", "xtrc"}) {
                e.removeAttribute(a);
            }
        }
        return d;
    }
    
    @Test
    public void testReplaceHref() throws Exception {
        final Invoker w = new Invoker("replaceHREF", ATTRIBUTE_NAME_HREF, String.class, Attributes.class) {
            @Override
            public String invoke(final String value) throws Exception {
                final AttributesImpl atts = new AttributesImpl();
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_XREF.toString());
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_SCOPE, ATTR_SCOPE_VALUE_LOCAL);
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_FORMAT, ATTR_FORMAT_VALUE_DITA);
                XMLUtils.addOrSetAttribute(atts, attrName, value);
                return (String) method.invoke(writer, attrName, atts);    
            }
        };
        // same directory path
        assertEquals("foo +%25bar.dita", w.invoke("foo +%25bar.dita"));
        assertEquals("foo.dita#bar", w.invoke("foo.dita#bar"));
        // absolute same directory path
        assertEquals("foo.dita", w.invoke(new File(srcDir, "foo.dita").getAbsolutePath()));
        assertEquals("foo.dita#bar", w.invoke(new File(srcDir, "foo.dita").getAbsolutePath() + "#bar"));
        final File sub = new File(srcDir, "sub" + File.separator + "foo +%bar.dita").getAbsoluteFile();
        // absolute sub directory path
        assertEquals("sub/foo +%bar.dita", w.invoke(sub.getAbsolutePath()));
        assertEquals("sub/foo +%bar.dita#bar", w.invoke(sub.getAbsolutePath() + "#bar"));
        // absolute sub directory URI
        assertEquals("sub/foo%20+%25bar.dita", w.invoke(sub.toURI().toASCIIString()));
        assertEquals("sub/foo%20+%25bar.dita#bar", w.invoke(sub.toURI().toASCIIString() + "#bar"));
        // unsupported extension
        assertEquals("foo.dita", w.invoke("foo.bar"));
    }
    
    @Test
    public void testReplaceConref() throws Exception {
        final Invoker w = new Invoker("replaceCONREF", ATTRIBUTE_NAME_CONREF, Attributes.class) {
            @Override
            public String invoke(final String value) throws Exception {
                final AttributesImpl atts = new AttributesImpl();
                XMLUtils.addOrSetAttribute(atts, attrName, value);
                return (String) method.invoke(writer, atts);    
            }
        };
        // same directory path
        assertEquals("foo +%25bar.dita", w.invoke("foo +%25bar.dita"));
        assertEquals("foo.dita#bar", w.invoke("foo.dita#bar"));
        // absolute same directory path
        assertEquals("foo.dita", w.invoke(new File(srcDir, "foo.dita").getAbsolutePath()));
        assertEquals("foo.dita#bar", w.invoke(new File(srcDir, "foo.dita").getAbsolutePath() + "#bar"));
        final File sub = new File(srcDir, "sub" + File.separator + "foo +%bar.dita").getAbsoluteFile();
        // absolute sub directory path
        assertEquals("sub/foo +%bar.dita", w.invoke(sub.getAbsolutePath()));
        assertEquals("sub/foo +%bar.dita#bar", w.invoke(sub.getAbsolutePath() + "#bar"));
        // absolute sub directory URI
        assertEquals(srcDir.toURI().toASCIIString() + "sub/foo%20+%25bar.dita", w.invoke(sub.toURI().toASCIIString()));
        assertEquals(srcDir.toURI().toASCIIString() + "sub/foo%20+%25bar.dita#bar", w.invoke(sub.toURI().toASCIIString() + "#bar"));
        // unsupported extension
        assertEquals("foo.dita", w.invoke("foo.bar"));
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }


    private static class TestHandler implements ContentHandler {

        private File source;
        private final Map<String, Integer> counter = new HashMap<String, Integer>();
        private final Set<String> requiredProcessingInstructions = new HashSet<String>();

        void setSource(final File source) {
            this.source = source;
        }

        public void characters(final char[] arg0, final int arg1, final int arg2) throws SAXException {
            // NOOP
        }

        public void endDocument() throws SAXException {
            if (!requiredProcessingInstructions.isEmpty()) {
                for (final String pi: requiredProcessingInstructions) {
                    throw new AssertionError("Processing instruction " + pi + " not defined");
                }
            }
            counter.clear();
            requiredProcessingInstructions.clear();
        }

        public void endElement(final String arg0, final String arg1, final String arg2) throws SAXException {
            // NOOP
        }

        public void endPrefixMapping(final String arg0) throws SAXException {
            // NOOP
        }

        public void ignorableWhitespace(final char[] arg0, final int arg1, final int arg2) throws SAXException {
            // NOOP
        }

        public void processingInstruction(final String arg0, final String arg1)
                throws SAXException {
            if (requiredProcessingInstructions.contains(arg0)) {
                requiredProcessingInstructions.remove(arg0);
            }
        }

        public void setDocumentLocator(final Locator arg0) {
            // NOOP
        }

        public void skippedEntity(final String arg0) throws SAXException {
            // NOOP
        }

        public void startDocument() throws SAXException {
            requiredProcessingInstructions.add("path2project");
            requiredProcessingInstructions.add("workdir");
        }

        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            final String xtrf = atts.getValue("xtrf");
            if (xtrf != null) {
                assertEquals(source.getAbsolutePath(), xtrf);
            }
            final String xtrc = atts.getValue("xtrc");
            if (xtrc != null) {
                Integer c = counter.get(localName);
                c = c == null ? 1 : c + 1;
                counter.put(localName, c);
                assertTrue(xtrc.startsWith(localName + ":" + c + ";"));
            }
        }

        public void startPrefixMapping(final String arg0, final String arg1) throws SAXException {
            // NOOP
        }

    }
    
    private abstract class Invoker {
        final DitaWriter writer;
        final Method method;
        final String attrName;
        public Invoker(final String m, final String attrName, final Class<?>... args) throws Exception {
            writer = new DitaWriter();
            writer.setLogger(new TestUtils.TestLogger(false));
            writer.setExtName(".dita");
            final OutputUtils outputUtils = new OutputUtils();
            outputUtils.setInputMapPathName(new File(srcDir, "main.ditamap"));
            writer.setOutputUtils(outputUtils);        
            method = DitaWriter.class.getDeclaredMethod(m, args);
            method.setAccessible(true);
            this.attrName = attrName;
        }
        public abstract String invoke(final String value) throws Exception;
    }
    
}
