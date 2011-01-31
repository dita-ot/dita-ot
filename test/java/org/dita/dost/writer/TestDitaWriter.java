/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.module.DebugAndFilterModule;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.TestDITAOTCopy;
import org.dita.dost.writer.DitaWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


public class TestDitaWriter {
	
	private final File resourceDir = new File("test-stub");
	private File tempDir;
	
	public DitaWriter writer;
	//get catalog file.
	//private String ditaDir = "C:/jia/DITA-OT1.5";
	
	private final File baseDir = new File(resourceDir, "DITA-OT1.5");
	//private String tempDir = "DITAVAL" + File.separator + "temp";
	private final File inputDir = new File("DITAVAL");
	private final File inputMap = new File(inputDir, "DITAVAL_testdata1.ditamap");
	private final File outDir = new File(tempDir, "out");
	private final File ditavalFile = new File(inputDir, "DITAVAL_1.ditaval");
	
	
	private PipelineHashIO pipelineInput;

	@Before
	public void setUp() throws Exception {
		tempDir = TestUtils.createTempDir(getClass());
		
		PipelineFacade facade = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		
		pipelineInput.setAttribute("inputmap", inputMap.getPath());
		pipelineInput.setAttribute("basedir", baseDir.getAbsolutePath());
		pipelineInput.setAttribute("inputdir", inputDir.getPath());
		pipelineInput.setAttribute("outputdir", outDir.getAbsolutePath());
		pipelineInput.setAttribute("tempDir", tempDir.getAbsolutePath());
		pipelineInput.setAttribute("ditadir", "");
		pipelineInput.setAttribute("ditaext", ".xml");
		pipelineInput.setAttribute("indextype", "xhtml");
		pipelineInput.setAttribute("encoding", "en-US");
		pipelineInput.setAttribute("targetext", ".html");
		pipelineInput.setAttribute("validate", "false");
		pipelineInput.setAttribute("generatecopyouter", "1");
		pipelineInput.setAttribute("outercontrol", "warn");
		pipelineInput.setAttribute("onlytopicinmap", "false");
		pipelineInput.setAttribute("ditalist", new File(tempDir, "dita.list").getAbsolutePath());
		pipelineInput.setAttribute("maplinks", new File(tempDir, "maplinks.unordered").getAbsolutePath());
		pipelineInput.setAttribute("transtype", "xhtml");
		pipelineInput.setAttribute("ditaval", ditavalFile.getPath());
		pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");
		
		
		
		facade.execute("GenMapAndTopicList", pipelineInput);
		
		String ditaDir = baseDir.getAbsolutePath();
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
        
        HashMap<String, String> map = filterReader.getFilterMap();
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
        content.setValue(tempDir.getAbsolutePath());
		writer.setContent(content);
		//C:\jia\DITA-OT1.5\DITAVAL|img.dita
		String filePathPrefix = new File(baseDir, inputDir.getPath()).getAbsolutePath() + Constants.STICK;
		String filePath = new File(baseDir, new File(inputDir, "keyword.dita").getPath()).getAbsolutePath();
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
			String filePath1 = new File(tempDir, "keyword.xml").getAbsolutePath();
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
	
	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}
	
}
