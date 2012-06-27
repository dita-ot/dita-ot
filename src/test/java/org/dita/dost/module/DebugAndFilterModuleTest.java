/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTFileLogger;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.util.Constants;
import org.dita.dost.util.OutputUtils;

public class DebugAndFilterModuleTest {

    final File resourceDir = TestUtils.getResourceDir(DebugAndFilterModuleTest.class);
    private File tempDir;
    private final File ditaDir = new File("src" + File.separator + "main");
    private File tmpDir;
    private File inputDir;

    @Before
    public void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(getClass());

        inputDir = new File(resourceDir, "input");
        final File inputMap = new File(inputDir, "maps" + File.separator + "root-map-01.ditamap");
        final File outDir = new File(tempDir, "out");
        tmpDir = new File(tempDir, "temp");
        TestUtils.copy(new File(resourceDir, "temp"), tmpDir);
        final File ditaList = new File(tmpDir, "dita.list");
        final Properties props = readProperties(ditaList);
        props.put("user.input.dir", inputDir.getAbsolutePath());
        writeProperties(props, ditaList, false);
        writeProperties(props, new File(tmpDir, "dita.xml.properties"), true);

        DITAOTFileLogger.getInstance().setLogDir(tmpDir.getAbsolutePath());
        DITAOTFileLogger.getInstance().setLogFile(DebugAndFilterModuleTest.class.getSimpleName() + ".log");
        DitaURIResolverFactory.setPath(tmpDir.getAbsolutePath());

        final PipelineHashIO pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute("inputmap", inputMap.getPath());
        pipelineInput.setAttribute("basedir", inputDir.getAbsolutePath());
        pipelineInput.setAttribute("inputdir", inputDir.getPath());
        pipelineInput.setAttribute("outputdir", outDir.getPath());
        pipelineInput.setAttribute("tempDir", tmpDir.getPath());
        pipelineInput.setAttribute("ditadir", ditaDir.getAbsolutePath());
        pipelineInput.setAttribute("ditaext", ".xml");
        pipelineInput.setAttribute("indextype", "xhtml");
        pipelineInput.setAttribute("encoding", "en-US");
        pipelineInput.setAttribute("targetext", ".html");
        pipelineInput.setAttribute("validate", "false");
        pipelineInput.setAttribute("generatecopyouter", "1");
        pipelineInput.setAttribute("outercontrol", "warn");
        pipelineInput.setAttribute("onlytopicinmap", "false");
        pipelineInput.setAttribute("ditalist", new File(tmpDir, "dita.list").getPath());
        pipelineInput.setAttribute("maplinks", new File(tmpDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "yes");

        final AbstractFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        facade.execute("DebugAndFilter", pipelineInput);
    }

    @Test
    public void testGeneratedFiles() throws SAXException, IOException {
        final File[] files = {
                new File("maps", "root-map-01.ditamap"),
                new File("topics", "target-topic-a.xml"),
                new File("topics", "target-topic-c.xml"),
                new File("topics", "xreffin-topic-1.xml"),
        };
        final TestHandler handler = new TestHandler();
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(handler);
        for (final File f: files) {
            InputStream in = null;
            try {
                in = new FileInputStream(new File(tmpDir, f.getPath()));
                handler.setSource(new File(inputDir, f.getPath()));
                parser.parse(new InputSource(in));
            } finally {
                if (in != null) {
                    in.close();
                }
            }
        }
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

    private Properties readProperties(final File ditaList) throws IOException {
        final Properties props = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(ditaList);
            props.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return props;
    }

    private void writeProperties(final Properties props, final File ditaList, final boolean isXML) throws IOException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(ditaList);
            if (isXML) {
                props.storeToXML(out, null);
            } else {
                props.store(out, null);
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
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
            assertNotNull(xtrf);
            assertEquals(source.getAbsolutePath(), xtrf);
            final String xtrc = atts.getValue("xtrc");
            assertNotNull(xtrc);
            Integer c = counter.get(localName);
            c = c == null ? 1 : c + 1;
            counter.put(localName, c);
            assertEquals(localName + ":" + c, xtrc);
        }

        public void startPrefixMapping(final String arg0, final String arg1) throws SAXException {
            // NOOP
        }

    }

}
