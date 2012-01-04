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
import static org.dita.dost.util.Job.*;

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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestGenMapAndTopicListModule {

    private static final File resourceDir = new File("test-stub", TestGenMapAndTopicListModule.class.getSimpleName());
    private static final File srcDir = new File(resourceDir, "src");
    
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(TestGenMapAndTopicListModule.class);

        final File inputDir = new File("maps");
        final File inputMap = new File(inputDir, "root-map-01.ditamap");
        final File outDir = new File(tempDir, "out");

        final PipelineHashIO pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_INPUTMAP, inputMap.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_BASEDIR, srcDir.getAbsolutePath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, inputDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, outDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_TEMPDIR, tempDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, "");
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_DITAEXT, ".xml");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_INDEXTYPE, "xhtml");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ENCODING, "en-US");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT, ".html");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE, "false");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, "1");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, "warn");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP, "false");
        //pipelineInput.setAttribute("ditalist", new File(tempDir, "dita.list").getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_MAPLINKS, new File(tempDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

        final AbstractFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        facade.execute("GenMapAndTopicList", pipelineInput);
    }

    @Test
    public void testTempContents() throws DITAOTException{
        assertTrue(new File(tempDir, HREF_TOPIC_LIST_FILE).exists());
        assertTrue(new File(tempDir, CODEREF_LIST_FILE).exists());
        assertTrue(new File(tempDir, CONREF_LIST_FILE).exists());
        assertTrue(new File(tempDir, CONREF_PUSH_LIST_FILE).exists());
        assertTrue(new File(tempDir, CONREF_TARGET_LIST_FILE).exists());
        assertTrue(new File(tempDir, COPYTO_SOURCE_LIST_FILE).exists());
        assertTrue(new File(tempDir, COPYTO_TARGET_TO_SOURCE_MAP_LIST_FILE).exists());
        assertTrue(new File(tempDir, FILE_NAME_DITA_LIST).exists());
        assertTrue(new File(tempDir, FILE_NAME_DITA_LIST_XML).exists());
        assertTrue(new File(tempDir, FLAG_IMAGE_LIST_FILE).exists());
        assertTrue(new File(tempDir, FULL_DITAMAP_LIST_FILE).exists());
        assertTrue(new File(tempDir, FULL_DITAMAP_TOPIC_LIST_FILE).exists());
        assertTrue(new File(tempDir, FULL_DITA_TOPIC_LIST_FILE).exists());
        assertTrue(new File(tempDir, HREF_DITA_TOPIC_LIST_FILE).exists());
        assertTrue(new File(tempDir, HREF_TARGET_LIST_FILE).exists());
        assertTrue(new File(tempDir, HTML_LIST_FILE).exists());
        assertTrue(new File(tempDir, IMAGE_LIST_FILE).exists());
        assertTrue(new File(tempDir, KEY_LIST_FILE).exists());
        assertTrue(new File(tempDir, KEYDEF_LIST_FILE).exists());
        assertTrue(new File(tempDir, KEYREF_LIST_FILE).exists());
        assertTrue(new File(tempDir, OUT_DITA_FILES_LIST_FILE ).exists());
        assertTrue(new File(tempDir, REL_FLAGIMAGE_LIST_FILE).exists());
        assertTrue(new File(tempDir, RESOURCE_ONLY_LIST_FILE).exists());
        assertTrue(new File(tempDir, CHUNK_TOPIC_LIST_FILE).exists());
        assertTrue(new File(tempDir, SUBSIDIARY_TARGET_LIST_FILE).exists());
        assertTrue(new File(tempDir, USER_INPUT_FILE_LIST_FILE).exists());
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

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
