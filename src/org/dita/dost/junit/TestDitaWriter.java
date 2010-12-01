package org.dita.dost.junit;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.module.DebugAndFilterModule;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.DitaWriter;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class TestDitaWriter {
	
	public static DitaWriter writer;
	//get catalog file.
	//private static String ditaDir = "C:/jia/DITA-OT1.5";
	
	private static String baseDir = "test-stub" + File.separator + "DITA-OT1.5";
	private static String tempDir = "DITAVAL" + File.separator + "temp";
	private static String inputDir = "DITAVAL";
	private static String inputMap = "DITAVAL" + File.separator + "DITAVAL_testdata1.ditamap";
	private static String outDir = "DITAVAL" + File.separator + "out";
	private static String ditavalFile = inputDir + File.separator + "DITAVAL_1.ditaval";
	
	
	private static PipelineHashIO pipelineInput;

	@BeforeClass
	public static void setUp() throws Exception {
		
		//Create the temp dir
		File dir = new File(baseDir, tempDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		PipelineFacade facade = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		
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
		pipelineInput.setAttribute("transtype", "xhtml");
		pipelineInput.setAttribute("ditaval", ditavalFile);
		
		
		
		facade.execute("GenMapAndTopicList", pipelineInput);
		
		String ditaDir = new File(baseDir, "").getAbsolutePath();
		DitaWriter.initXMLReader(ditaDir, false, true);
		
		writer = new DitaWriter();
	}
	
	@Test
	public void testWrite() throws DITAOTException{
		
        //ListReader listReader = new ListReader();
        
        String ditavalFile = pipelineInput.getAttribute(Constants.ANT_INVOKER_PARAM_DITAVAL);
        ditavalFile = new File(baseDir, ditavalFile).getAbsolutePath();
        DitaValReader filterReader = new DitaValReader();
        filterReader.read(ditavalFile);
        
        HashMap map = filterReader.getFilterMap();
		assertEquals("include", map.get("audience=Cindy"));
		assertEquals("flag", map.get("produt=p1"));
		assertEquals("exclude", map.get("product=ABase_ph"));
		assertEquals("include", map.get("product=AExtra_ph"));
		assertEquals("exclude", map.get("product=Another_ph"));
		assertEquals("flag", map.get("platform=Windows"));
		assertEquals("flag", map.get("platform=Linux"));
		assertEquals("exclude", map.get("keyword=key1"));
		assertEquals("flag", map.get("keyword=key2"));
		assertEquals("include", map.get("keyword=key3"));
		assertEquals("exclude", map.get("product=key1"));
		assertEquals("flag", map.get("product=key2"));
		assertEquals("include", map.get("product=key3"));
        
        Content content = new ContentImpl();
        String tempDir1 = new File(baseDir, tempDir).getAbsolutePath();
		content.setValue(tempDir1);
		writer.setContent(content);
		//C:\jia\DITA-OT1.5\DITAVAL|img.dita
		String filePathPrefix = new File(baseDir, inputDir).getAbsolutePath() + Constants.STICK;
		String filePath = new File(baseDir, inputDir + File.separator + "keyword.dita").getAbsolutePath();
		DebugAndFilterModule.extName = ".xml";
		writer.write(filePathPrefix + "keyword.dita");
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder builder;
		try {
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);	
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(filePath);
			Element elem = document.getDocumentElement();
			NodeList nodeList = elem.getElementsByTagName("keyword");
			String[] ids ={"prodname1", "prodname2", "prodname3"};
			String[] products = {"key1", "key2", "key3"};
			for(int i = 0; i<nodeList.getLength(); i++){
				assertEquals(ids[i],
						((Element)nodeList.item(i)).getAttribute("id"));
				assertEquals(products[i],
						((Element)nodeList.item(i)).getAttribute("product"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
		}
		
		try {
			builder = factory.newDocumentBuilder();
			String filePath1 = new File(baseDir, tempDir + File.separator + "keyword.xml").getAbsolutePath();
			Document document = builder.parse(filePath1);
			Element elem = document.getDocumentElement();
			NodeList nodeList = elem.getElementsByTagName("keyword");
			String[] ids ={"prodname2", "prodname3"};
			String[] products = {"key2", "key3"};
			for(int i = 0; i<nodeList.getLength(); i++){
				assertEquals(ids[i],
						((Element)nodeList.item(i)).getAttribute("id"));
				assertEquals(products[i],
						((Element)nodeList.item(i)).getAttribute("product"));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail();
			
		}
	}
}
