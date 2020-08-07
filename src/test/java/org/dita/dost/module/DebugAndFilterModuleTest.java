/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.dita.dost.util.Job.Generate.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.dita.dost.store.StreamStore;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
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
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job;

public class DebugAndFilterModuleTest {

    final File resourceDir = TestUtils.getResourceDir(DebugAndFilterModuleTest.class);
    private File tempDir;
    private final File ditaDir = new File("src" + File.separator + "main");
    private File tmpDir;
    private File inputDir;

    @BeforeClass
    public static void setUpClass() {
        CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
    }
    
    @Before
    public void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(getClass());

        inputDir = new File(resourceDir, "input");
        final File inputMap = new File(inputDir, "maps" + File.separator + "root-map-01.ditamap");
        final File outDir = new File(tempDir, "out");
        tmpDir = new File(tempDir, "temp");
        TestUtils.copy(new File(resourceDir, "temp"), tmpDir);
        final Job job = new Job(tmpDir, new StreamStore(tmpDir, new XMLUtils()));
        for (final Job.FileInfo fi: job.getFileInfo()) {
            job.add(new Job.FileInfo.Builder(fi).src(inputDir.toURI().resolve(fi.uri)).build());
        }
        job.setInputFile(inputMap.getAbsoluteFile().toURI());
        job.setGeneratecopyouter(NOT_GENERATEOUTTER);
        job.setOutputDir(outDir);
        job.setProperty(INPUT_DIR, inputDir.getAbsolutePath());
        job.setInputDir(inputDir.getAbsoluteFile().toURI());
        job.write();

        final PipelineHashIO pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute("inputmap", inputMap.getPath());
        pipelineInput.setAttribute("basedir", inputDir.getAbsolutePath());
        pipelineInput.setAttribute("inputdir", inputDir.getPath());
        pipelineInput.setAttribute("outputdir", outDir.getPath());
        pipelineInput.setAttribute("tempDir", tmpDir.getPath());
        pipelineInput.setAttribute("ditadir", ditaDir.getAbsolutePath());
        pipelineInput.setAttribute("indextype", "xhtml");
        pipelineInput.setAttribute("encoding", "en-US");
        pipelineInput.setAttribute("targetext", ".html");
        pipelineInput.setAttribute("validate", Boolean.FALSE.toString());
        pipelineInput.setAttribute("generatecopyouter", Integer.toString(NOT_GENERATEOUTTER.type));
        pipelineInput.setAttribute("outercontrol", "warn");
        pipelineInput.setAttribute("onlytopicinmap", Boolean.FALSE.toString());
        pipelineInput.setAttribute("ditalist", new File(tmpDir, "dita.list").getPath());
        pipelineInput.setAttribute("maplinks", new File(tmpDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "yes");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "xhtml");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATE_DEBUG_ATTR, Boolean.TRUE.toString());

        final DebugAndFilterModule module = new DebugAndFilterModule();
        module.setLogger(new TestUtils.TestLogger());
        module.setJob(job);
        module.setXmlUtils(new XMLUtils());
        module.setProcessingPipe(Collections.emptyList());
        
        module.execute(pipelineInput);
    }

    @Test
    public void testGeneratedFiles() throws SAXException, IOException {
        final File[] files = {
                new File("maps", "root-map-01.ditamap"),
                new File("topics", "target-topic-a.xml"),
                new File("topics", "target-topic-c.xml"),
                new File("topics", "xreffin-topic-1.xml"),
                // TODO test me somewhere else
//                new File("topics", "copy-to.xml"),
        };
        final Map<File, File> copyto = new HashMap<File, File>();
        copyto.put(new File("topics", "copy-to.xml"), new File("topics", "xreffin-topic-1.xml"));
        final TestHandler handler = new TestHandler();
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setEntityResolver(CatalogUtils.getCatalogResolver());
        parser.setContentHandler(handler);
        for (final File f: files) {
            InputStream in = null;
            try {
                in = new FileInputStream(new File(tmpDir, f.getPath()));
                handler.setSource(new File(inputDir, copyto.containsKey(f) ? copyto.get(f).getPath() : f.getPath()));
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
            requiredProcessingInstructions.add("workdir-uri");

        }

        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            final String xtrf = atts.getValue("xtrf");
            assertNotNull(xtrf);
            //assertEquals(source.getAbsoluteFile().toURI().toString(), xtrf);
            assertEquals(source.getAbsoluteFile().toURI().toString(), xtrf);
            final String xtrc = atts.getValue("xtrc");
            assertNotNull(xtrc);
            Integer c = counter.get(localName);
            c = c == null ? 1 : c + 1;
            counter.put(localName, c);
            assertTrue(xtrc.startsWith(localName + ":" + c + ";"));
        }

        public void startPrefixMapping(final String arg0, final String arg1) throws SAXException {
            // NOOP
        }

    }

}
