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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.GenMapAndTopicListModule;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
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
		pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");
		
		final AbstractFacade facade = new PipelineFacade();
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
		Properties properties = new Properties();
		properties.load(new FileInputStream(tempDir + File.separator + "canditopics.list"));
		assertTrue(properties.containsKey("topics" + File.separator + "xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-c.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-a.xml"));
		
		properties.load(new FileInputStream(tempDir+ File.separator + "dita.list"));
		final String[] expFullditamapandtopiclist = {
				"topics" + File.separator + "xreffin-topic-1.xml",
				"maps" + File.separator + "root-map-01.ditamap",
				"topics" + File.separator + "target-topic-c.xml",
				"topics" + File.separator + "target-topic-a.xml" };
		final String[] actFullditamapandtopiclist = properties.getProperty("fullditamapandtopiclist").split(",");
		Arrays.sort(expFullditamapandtopiclist);
		Arrays.sort(actFullditamapandtopiclist);
		assertArrayEquals(expFullditamapandtopiclist, actFullditamapandtopiclist);
		
		properties.load(new FileInputStream(tempDir+ File.separator + "fullditamapandtopic.list"));
		assertTrue(properties.containsKey("topics" + File.separator + "xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-c.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-a.xml"));
		assertTrue(properties.containsKey("maps" + File.separator + "root-map-01.ditamap"));
		
		properties.load(new FileInputStream(tempDir+ File.separator + "hrefditatopic.list"));
		assertTrue(properties.containsKey("topics" + File.separator + "xreffin-topic-1.xml"));
		
		properties.load(new FileInputStream(tempDir+ File.separator + "hreftargets.list"));
		assertTrue(properties.containsKey("topics" + File.separator + "xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-c.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-a.xml"));
		
		properties.load(new FileInputStream(tempDir+ File.separator + "key.list"));
		
		assertEquals("topics" + File.separator + "target-topic-a.xml(maps" + File.separator + "root-map-01.ditamap)",
				properties.getProperty("target_topic_1"));
		assertEquals("topics" + File.separator + "target-topic-c.xml(maps" + File.separator + "root-map-01.ditamap)",
				properties.getProperty("target_topic_2"));
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		Document document = builder.parse(new File(tempDir+ File.separator + "keydef.xml"));
		
		Element elem = document.getDocumentElement();
		NodeList nodeList = elem.getElementsByTagName("keydef");
		String[]keys ={"target_topic_2","target_topic_1"};
		String[]href ={"topics" + File.separator + "target-topic-c.xml","topics" + File.separator + "target-topic-a.xml"};
		String[]source ={"maps" + File.separator + "root-map-01.ditamap","maps" + File.separator + "root-map-01.ditamap"};
		
		for(int i = 0; i< nodeList.getLength();i++){
			assertEquals(keys[i],
					((Element)nodeList.item(i)).getAttribute("keys"));
			assertEquals(href[i],
					((Element)nodeList.item(i)).getAttribute("href"));
			assertEquals(source[i],
					((Element)nodeList.item(i)).getAttribute("source"));
		}
		
		properties.load(new FileInputStream(tempDir+ File.separator + "keyref.list"));
		assertTrue(properties.containsKey("topics" + File.separator + "xreffin-topic-1.xml"));
		
		properties.load(new FileInputStream(tempDir+ File.separator + "outditafiles.list"));
		assertTrue(properties.containsKey("topics" + File.separator + "xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-c.xml"));
		assertTrue(properties.containsKey("topics" + File.separator + "target-topic-a.xml"));
		
		properties.load(new FileInputStream(tempDir+ File.separator + "usr.input.file.list"));
		assertTrue(properties.containsKey("maps" + File.separator + "root-map-01.ditamap"));
		
	}

	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}

}
