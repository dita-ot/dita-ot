package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.Constants;

import org.junit.BeforeClass;
import org.junit.Test;


public class TestDitaValReader {
	
	public static DitaValReader reader;
	private static String baseDir = "test-stub" + File.separator + "DITA-OT1.5";
	private static String tempDir = "DITAVAL" + File.separator + "temp";
	
	
	@BeforeClass
	public static void setUp() throws Exception{
		reader = new DitaValReader();
		//Create the temp dir
		File dir = new File(baseDir, tempDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		PipelineFacade facade = new PipelineFacade();
		PipelineHashIO pipelineInput = new PipelineHashIO();
		
		String inputDir = "DITAVAL";
		String inputMap = "DITAVAL" + File.separator + "DITAVAL_testdata1.ditamap";
		String outDir = "DITAVAL" + File.separator + "out";
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
		pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");
		facade.execute("GenMapAndTopicList", pipelineInput);
		
		
	}
	
	@Test
	public void testRead() throws DITAOTException{
		
		reader.read(baseDir+ File.separator + "DITAVAL" + File.separator + "DITAVAL_1.ditaval");		
		HashMap map = reader.getFilterMap();
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
	}
	
}
