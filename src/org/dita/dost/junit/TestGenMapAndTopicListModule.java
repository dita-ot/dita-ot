package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.GenMapAndTopicListModule;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
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
		
		String baseDir = "test-stub/DITA-OT1.5";
		String tempDir = "keyrefs/maps_parallel_to_topics/temp";
		String inputDir = "keyrefs/maps_parallel_to_topics/maps";
		
		String inputMap = inputDir + "/root-map-01.ditamap";
		String outDir = "keyrefs/maps_parallel_to_topics/out";
		
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
		pipelineInput.setAttribute("ditalist", tempDir + "/dita.list");
		pipelineInput.setAttribute("maplinks", tempDir + "/maplinks.unordered");
		
	}
	
	@Test
	public void testExecute() throws DITAOTException{
		try {
			String baseDir = "test-stub/DITA-OT1.5";
			String tempDir = pipelineInput.getAttribute("tempDir");
			
			
			facade.execute("GenMapAndTopicList", pipelineInput);
			tempDir = new File(baseDir, tempDir).getAbsolutePath();
			
			assertTrue(FileUtils.fileExists(tempDir));
			
			//File existence test
			assertTrue(FileUtils.fileExists(tempDir+"/canditopics.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/coderef.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/conref.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/conrefpush.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/conreftargets.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/copytosource.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/copytotarget2sourcemap.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/dita.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/dita.xml.properties"));
			assertTrue(FileUtils.fileExists(tempDir+"/flagimage.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/fullditamap.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/fullditamapandtopic.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/fullditatopic.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/hrefditatopic.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/hreftargets.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/html.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/image.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/key.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/keydef.xml"));
			assertTrue(FileUtils.fileExists(tempDir+"/keyref.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/outditafiles.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/relflagimage.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/resourceonly.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/skipchunk.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/subtargets.list"));
			assertTrue(FileUtils.fileExists(tempDir+"/usr.input.file.list"));
			
			
			
			/*DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();*/
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	@Test
	public void testFileContent() throws Exception{
		
		String baseDir = "test-stub/DITA-OT1.5";
		String tempDir = pipelineInput.getAttribute("tempDir");
		tempDir = new File(baseDir, tempDir).getAbsolutePath();
		
		Properties properties = new Properties();
		properties.load(new FileInputStream(tempDir+"/canditopics.list"));
		assertTrue(properties.containsKey("topics/xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics/target-topic-c.xml"));
		assertTrue(properties.containsKey("topics/target-topic-a.xml"));
		
		properties.load(new FileInputStream(tempDir+"/dita.list"));
		assertEquals("topics/xreffin-topic-1.xml,maps/root-map-01.ditamap,topics/target-topic-c.xml,topics/target-topic-a.xml", 
				properties.getProperty("fullditamapandtopiclist"));
		
		properties.load(new FileInputStream(tempDir+"/fullditamapandtopic.list"));
		assertTrue(properties.containsKey("topics/xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics/target-topic-c.xml"));
		assertTrue(properties.containsKey("topics/target-topic-a.xml"));
		assertTrue(properties.containsKey("maps/root-map-01.ditamap"));
		
		properties.load(new FileInputStream(tempDir+"/hrefditatopic.list"));
		assertTrue(properties.containsKey("topics/xreffin-topic-1.xml"));
		
		properties.load(new FileInputStream(tempDir+"/hreftargets.list"));
		assertTrue(properties.containsKey("topics/xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics/target-topic-c.xml"));
		assertTrue(properties.containsKey("topics/target-topic-a.xml"));
		
		properties.load(new FileInputStream(tempDir+"/key.list"));
		
		assertEquals("topics/target-topic-a.xml(maps/root-map-01.ditamap)",
				properties.getProperty("target_topic_1"));
		assertEquals("topics/target-topic-c.xml(maps/root-map-01.ditamap)",
				properties.getProperty("target_topic_2"));
		
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		
		Document document = builder.parse(new File(tempDir+"/keydef.xml"));
		
		Element elem = document.getDocumentElement();
		NodeList nodeList = elem.getElementsByTagName("keydef");
		String[]keys ={"target_topic_2","target_topic_1"};
		String[]href ={"..\\topics\\target-topic-c.xml","..\\topics\\target-topic-a.xml"};
		String[]source ={"root-map-01.ditamap","root-map-01.ditamap"};
		
		for(int i = 0; i< nodeList.getLength();i++){
			assertEquals(keys[i],
					((Element)nodeList.item(i)).getAttribute("keys"));
			assertEquals(href[i],
					((Element)nodeList.item(i)).getAttribute("href"));
			assertEquals(source[i],
					((Element)nodeList.item(i)).getAttribute("source"));
		}
		
		properties.load(new FileInputStream(tempDir+"/keyref.list"));
		assertTrue(properties.containsKey("topics/xreffin-topic-1.xml"));
		
		properties.load(new FileInputStream(tempDir+"/outditafiles.list"));
		assertTrue(properties.containsKey("topics/xreffin-topic-1.xml"));
		assertTrue(properties.containsKey("topics/target-topic-c.xml"));
		assertTrue(properties.containsKey("topics/target-topic-a.xml"));
		
		properties.load(new FileInputStream(tempDir+"/usr.input.file.list"));
		assertTrue(properties.containsKey("maps/root-map-01.ditamap"));
		
	}
}
