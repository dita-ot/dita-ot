package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.GenMapAndTopicListModule;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestGenMapAndTopicListModule {
	
	public static GenMapAndTopicListModule module;
	
	private static AbstractFacade facade;
	
	private static PipelineHashIO pipelineInput;
	
	//public static ConrefPushReader reader;
	
	@BeforeClass
	public static void setUp(){
		
		
		
		facade = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		
		String baseDir = "test-stub" + File.separator + "DITA-OT1.5";
		String tempDir = "keyrefs" + File.separator + "maps_parallel_to_topics" + File.separator + "temp";
		String inputDir = "keyrefs" + File.separator + "maps_parallel_to_topics" + File.separator + "maps";
		
		String inputMap = inputDir +  File.separator + "root-map-01.ditamap";
		String outDir = "keyrefs" + File.separator + "maps_parallel_to_topics" + File.separator + "out";
		
		//Create the temp dir
		File dir = new File(baseDir, tempDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		pipelineInput.setAttribute("inputmap", inputMap);
		pipelineInput.setAttribute("basedir", baseDir);
		pipelineInput.setAttribute("inputdir", inputDir);
		pipelineInput.setAttribute("outputdir", outDir);
		pipelineInput.setAttribute("tempDir", tempDir);
		pipelineInput.setAttribute("ditadir", "");
		pipelineInput.setAttribute("ditaext", ".xml");
		pipelineInput.setAttribute("indextype", "xhtml");
		pipelineInput.setAttribute("encoding", "en-US");
		pipelineInput.setAttribute("targetext", ".html");
		pipelineInput.setAttribute("validate", "false");
		pipelineInput.setAttribute("generatecopyouter", "1");
		pipelineInput.setAttribute("outercontrol", "warn");
		pipelineInput.setAttribute("onlytopicinmap", "false");
		pipelineInput.setAttribute("ditalist", tempDir + File.separator + "dita.list");
		pipelineInput.setAttribute("maplinks", tempDir + File.separator + "maplinks.unordered");
		pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");
		
	}
	
	@Test
	public void testExecute() throws DITAOTException{
		try {
			String baseDir = "test-stub" + File.separator + "DITA-OT1.5";
			String tempDir = pipelineInput.getAttribute("tempDir");
			
			
			facade.execute("GenMapAndTopicList", pipelineInput);
			tempDir = new File(baseDir, tempDir).getAbsolutePath();
			
			assertTrue(FileUtils.fileExists(tempDir));
			
			//File existence test
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "canditopics.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "coderef.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "conref.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "conrefpush.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "conreftargets.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "copytosource.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "copytotarget2sourcemap.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "dita.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "dita.xml.properties"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "flagimage.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "fullditamap.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "fullditamapandtopic.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "fullditatopic.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "hrefditatopic.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "hreftargets.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "html.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "image.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "key.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "keydef.xml"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "keyref.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "outditafiles.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "relflagimage.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "resourceonly.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "skipchunk.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "subtargets.list"));
			assertTrue(FileUtils.fileExists(tempDir + File.separator + "usr.input.file.list"));
			
			
			
			/*DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();*/
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Test
	public void testFileContent() throws Exception{
		
		String baseDir = "test-stub" + File.separator + "DITA-OT1.5";
		String tempDir = pipelineInput.getAttribute("tempDir");
		tempDir = new File(baseDir, tempDir).getAbsolutePath();
		
		Properties properties = new Properties();
		properties.load(new FileInputStream(tempDir+ File.separator + "canditopics.list"));
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
}
