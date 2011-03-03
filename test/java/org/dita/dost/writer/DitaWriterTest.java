/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.writer;

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
import org.dita.dost.util.OutputUtils;

public class DitaWriterTest {
    
    private static final File resourceDir = new File("test-stub", DitaWriterTest.class.getSimpleName());
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
        
        OutputUtils.setInputMapPathName(new File(srcDir, "main.ditamap").getAbsolutePath());
        final DitaWriter writer = new DitaWriter();
        writer.initXMLReader(srcDir.getAbsolutePath(), false, true);
        writer.setExtName(".xml");
        
        for (final String f: new String[] {"main.ditamap", "keyword.dita"}) {
            final Content content = new ContentImpl();
            content.setValue(tempDir.getAbsolutePath());
            writer.setContent(content);
            writer.write(srcDir.getAbsolutePath() + Constants.STICK + f);
        }
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
        assertEquals(TestUtils.readXmlToString(new File(expDir, "keyword.xml"), true, true),
                     TestUtils.readXmlToString(new File(tempDir, "keyword.xml"), true, true));
        assertEquals(TestUtils.readXmlToString(new File(expDir, "main.ditamap"), true, true),
                     TestUtils.readXmlToString(new File(tempDir, "main.ditamap"), true, true));
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    
    private class TestHandler implements ContentHandler {

        private File source;
        private final Map<String, Integer> counter = new HashMap<String, Integer>();
        private final Set<String> requiredProcessingInstructions = new HashSet<String>();
                
        void setSource(final File source) {
            this.source = source;
        }
        
        public void characters(char[] arg0, int arg1, int arg2) throws SAXException {
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

        public void endElement(String arg0, String arg1, String arg2) throws SAXException {
            // NOOP
        }

        public void endPrefixMapping(String arg0) throws SAXException {
            // NOOP
        }

        public void ignorableWhitespace(char[] arg0, int arg1, int arg2) throws SAXException {
            // NOOP
        }

        public void processingInstruction(String arg0, String arg1)
                throws SAXException {
            if (requiredProcessingInstructions.contains(arg0)) {
                requiredProcessingInstructions.remove(arg0);
            }
        }

        public void setDocumentLocator(Locator arg0) {
            // NOOP
        }

        public void skippedEntity(String arg0) throws SAXException {
            // NOOP
        }

        public void startDocument() throws SAXException {
            requiredProcessingInstructions.add("path2project");
            requiredProcessingInstructions.add("workdir");
        }

        public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
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

        public void startPrefixMapping(String arg0, String arg1) throws SAXException {
            // NOOP
        }
        
    }
}
