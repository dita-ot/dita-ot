/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestDitaValReader {
	
	private final File resourceDir = new File("test-stub");
	private File tempDir;
	
	public DitaValReader reader;
	private final File baseDir = new File(resourceDir, "DITA-OT1.5");
	
	@Before
	public void setUp() throws Exception{
		tempDir = TestUtils.createTempDir(getClass());
		reader = new DitaValReader();
		
		PipelineFacade facade = new PipelineFacade();
		PipelineHashIO pipelineInput = new PipelineHashIO();
		
		final File inputDir = new File("DITAVAL");
		final File inputMap = new File(inputDir, "DITAVAL_testdata1.ditamap");
		final File outDir = new File(inputDir, "out");
		pipelineInput.setAttribute("inputmap", inputMap.getPath());
		pipelineInput.setAttribute("basedir", baseDir.getPath());
		pipelineInput.setAttribute("inputdir", inputDir.getPath());
		pipelineInput.setAttribute("outputdir", outDir.getAbsolutePath());
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
		pipelineInput.setAttribute("transtype", "xhtml");
		pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");
		facade.execute("GenMapAndTopicList", pipelineInput);
		
		
	}
	
	@Test
	public void testRead() throws DITAOTException{
		final File ditavalFile = new File(baseDir, "DITAVAL" + File.separator + "DITAVAL_1.ditaval");
		reader.read(ditavalFile.getAbsolutePath());
		HashMap<String, String> map = reader.getFilterMap();
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
	
	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}

}
