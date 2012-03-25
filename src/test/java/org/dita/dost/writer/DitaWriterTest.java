/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.OutputUtils;

public class DitaWriterTest {

    private static final File resourceDir = new File(TestUtils.testStub, DitaWriterTest.class.getSimpleName());
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException, SAXException {
        tempDir = TestUtils.createTempDir(DitaWriterTest.class);
        final Properties props = new Properties();
        props.put("keylist", "");
        OutputStream out = null;
        try {
            out = new FileOutputStream(new File(tempDir, "dita.list"));
            props.store(out, null);
        } finally {
            if (out != null) {
                out.close();
            }
        }
        final DitaWriter writer = new DitaWriter();
        writer.setLogger(new TestUtils.TestLogger());
        writer.initXMLReader(new File("src" + File.separator + "main").getAbsolutePath(), false, true);
        writer.setExtName(".xml");
        final FilterUtils fu = new FilterUtils();
        fu.setLogger(new TestUtils.TestLogger());
        writer.setFilterUtils(fu);
        final OutputUtils outputUtils = new OutputUtils();
        outputUtils.setInputMapPathName(new File(srcDir, "main.ditamap").getAbsolutePath());
        writer.setOutputUtils(outputUtils);

        for (final String f: new String[] {"main.ditamap", "keyword.dita"}) {
            writer.setTempDir(tempDir.getAbsolutePath());
            writer.write(srcDir.getAbsolutePath(), f);
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
            in = new FileInputStream(new File(tempDir, "keyword.xml"));
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
        clean(db.parse(new File(tempDir, "keyword.xml")));

        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);

        assertXMLEqual(clean(db.parse(new File(expDir, "keyword.xml"))),
                clean(db.parse(new File(tempDir, "keyword.xml"))));
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
                assertEquals(localName + ":" + c, xtrc);
            }
        }

        public void startPrefixMapping(final String arg0, final String arg1) throws SAXException {
            // NOOP
        }

    }
}
