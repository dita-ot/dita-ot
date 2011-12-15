/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Constants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestGenMapAndTopicListModule {

    private final File resourceDir = new File("test-stub");
    private File tempDir;

    private final File baseDir = new File(resourceDir, "DITA-OT1.5");

    @Before
    public void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(getClass());

        final File inputDir = new File("keyrefs", "maps_parallel_to_topics" + File.separator + "maps");
        final File inputMap = new File(inputDir, "root-map-01.ditamap");
        final File outDir = new File(tempDir, "out");

        final PipelineHashIO pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute("inputmap", inputMap.getPath());
        pipelineInput.setAttribute("basedir", baseDir.getAbsolutePath());
        pipelineInput.setAttribute("inputdir", inputDir.getPath());
        pipelineInput.setAttribute("outputdir", outDir.getPath());
        pipelineInput.setAttribute("tempDir", tempDir.getPath());
        pipelineInput.setAttribute("ditadir", "");
        pipelineInput.setAttribute("ditaext", ".xml");
        pipelineInput.setAttribute("indextype", "xhtml");
        pipelineInput.setAttribute("encoding", "en-US");
        pipelineInput.setAttribute("targetext", ".html");
        pipelineInput.setAttribute("validate", "false");
        pipelineInput.setAttribute("generatecopyouter", "1");
        pipelineInput.setAttribute("outercontrol", "warn");
        pipelineInput.setAttribute("onlytopicinmap", "false");
        pipelineInput.setAttribute("ditalist", new File(tempDir, "dita.list").getPath());
        pipelineInput.setAttribute("maplinks", new File(tempDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

        final AbstractFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        facade.execute("GenMapAndTopicList", pipelineInput);
    }

    @Test
    public void testTempContents() throws DITAOTException{
        assertTrue(new File(tempDir, "canditopics.list").exists());
        assertTrue(new File(tempDir, "coderef.list").exists());
        assertTrue(new File(tempDir, "conref.list").exists());
        assertTrue(new File(tempDir, "conrefpush.list").exists());
        assertTrue(new File(tempDir, "conreftargets.list").exists());
        assertTrue(new File(tempDir, "copytosource.list").exists());
        assertTrue(new File(tempDir, "copytotarget2sourcemap.list").exists());
        assertTrue(new File(tempDir, "dita.list").exists());
        assertTrue(new File(tempDir, "dita.xml.properties").exists());
        assertTrue(new File(tempDir, "flagimage.list").exists());
        assertTrue(new File(tempDir, "fullditamap.list").exists());
        assertTrue(new File(tempDir, "fullditamapandtopic.list").exists());
        assertTrue(new File(tempDir, "fullditatopic.list").exists());
        assertTrue(new File(tempDir, "hrefditatopic.list").exists());
        assertTrue(new File(tempDir, "hreftargets.list").exists());
        assertTrue(new File(tempDir, "html.list").exists());
        assertTrue(new File(tempDir, "image.list").exists());
        assertTrue(new File(tempDir, "key.list").exists());
        assertTrue(new File(tempDir, "keydef.xml").exists());
        assertTrue(new File(tempDir, "keyref.list").exists());
        assertTrue(new File(tempDir, "outditafiles.list").exists());
        assertTrue(new File(tempDir, "relflagimage.list").exists());
        assertTrue(new File(tempDir, "resourceonly.list").exists());
        assertTrue(new File(tempDir, "skipchunk.list").exists());
        assertTrue(new File(tempDir, "subtargets.list").exists());
        assertTrue(new File(tempDir, "usr.input.file.list").exists());
    }

    @Test
    public void testFileContent() throws Exception{
        final List<String> canditopicsList = readLines(new File(tempDir, "canditopics.list"));
        assertTrue(canditopicsList.contains("topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml"));
        assertTrue(canditopicsList.contains("topics" + UNIX_SEPARATOR + "target-topic-c.xml"));
        assertTrue(canditopicsList.contains("topics" + UNIX_SEPARATOR + "target-topic-a.xml"));

        final Properties ditaProps = readProperties(new File(tempDir, "dita.list"));
        final String[] expFullditamapandtopiclist = {
                "topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml",
                "maps" + UNIX_SEPARATOR + "root-map-01.ditamap",
                "topics" + UNIX_SEPARATOR + "target-topic-c.xml",
                "topics" + UNIX_SEPARATOR + "target-topic-a.xml" };
        final String[] actFullditamapandtopiclist = ditaProps.getProperty("fullditamapandtopiclist").split(",");
        Arrays.sort(expFullditamapandtopiclist);
        Arrays.sort(actFullditamapandtopiclist);
        assertArrayEquals(expFullditamapandtopiclist, actFullditamapandtopiclist);

        final List<String> fullditamapandtopicList = readLines(new File(tempDir, "fullditamapandtopic.list"));
        assertTrue(fullditamapandtopicList.contains("topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml"));
        assertTrue(fullditamapandtopicList.contains("topics" + UNIX_SEPARATOR + "target-topic-c.xml"));
        assertTrue(fullditamapandtopicList.contains("topics" + UNIX_SEPARATOR + "target-topic-a.xml"));
        assertTrue(fullditamapandtopicList.contains("maps" + UNIX_SEPARATOR + "root-map-01.ditamap"));

        final List<String> hrefditatopicList = readLines(new File(tempDir, "hrefditatopic.list"));
        assertTrue(hrefditatopicList.contains("topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml"));

        final List<String> hreftargetsList = readLines(new File(tempDir, "hreftargets.list"));
        assertTrue(hreftargetsList.contains("topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml"));
        assertTrue(hreftargetsList.contains("topics" + UNIX_SEPARATOR + "target-topic-c.xml"));
        assertTrue(hreftargetsList.contains("topics" + UNIX_SEPARATOR + "target-topic-a.xml"));

        final Properties keyProps = readProperties(new File(tempDir, "key.list"));
        assertEquals("topics" + UNIX_SEPARATOR + "target-topic-a.xml(maps" + UNIX_SEPARATOR + "root-map-01.ditamap)",
                keyProps.getProperty("target_topic_1"));
        assertEquals("topics" + UNIX_SEPARATOR + "target-topic-c.xml(maps" + UNIX_SEPARATOR + "root-map-01.ditamap)",
                keyProps.getProperty("target_topic_2"));

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();

        final Document document = builder.parse(new File(tempDir+ File.separator + "keydef.xml"));

        final Element elem = document.getDocumentElement();
        final NodeList nodeList = elem.getElementsByTagName("keydef");
        final String[]keys ={"target_topic_2","target_topic_1"};
        final String[]href ={"topics" + UNIX_SEPARATOR + "target-topic-c.xml","topics" + UNIX_SEPARATOR + "target-topic-a.xml"};
        final String[]source ={"maps" + UNIX_SEPARATOR + "root-map-01.ditamap","maps" + UNIX_SEPARATOR + "root-map-01.ditamap"};

        for(int i = 0; i< nodeList.getLength();i++){
            assertEquals(keys[i],
                    ((Element)nodeList.item(i)).getAttribute("keys"));
            assertEquals(href[i],
                    ((Element)nodeList.item(i)).getAttribute("href"));
            assertEquals(source[i],
                    ((Element)nodeList.item(i)).getAttribute("source"));
        }

        final List<String> keyrefList = readLines(new File(tempDir, "keyref.list"));
        assertTrue(keyrefList.contains("topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml"));

        final List<String> outditafilesList = readLines(new File(tempDir, "outditafiles.list"));
        assertTrue(outditafilesList.contains("topics" + UNIX_SEPARATOR + "xreffin-topic-1.xml"));
        assertTrue(outditafilesList.contains("topics" + UNIX_SEPARATOR + "target-topic-c.xml"));
        assertTrue(outditafilesList.contains("topics" + UNIX_SEPARATOR + "target-topic-a.xml"));

        final List<String> usrInputFileList = readLines(new File(tempDir, "usr.input.file.list"));
        assertTrue(usrInputFileList.contains("maps" + File.separator + "root-map-01.ditamap"));

    }
    
    private Properties readProperties(final File f)
            throws IOException, FileNotFoundException {
        final Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            p.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return p;
    }
    
    private List<String> readLines(final File f) throws IOException {
        final List<String> lines = new ArrayList<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lines;
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
