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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.TopicMergeModule;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestTopicMergeModule {
	
	private final File resourceDir = new File("test-stub", TestTopicMergeModule.class.getSimpleName());
	private File tempDir;
	
	public TopicMergeModule module;
	
//	private AbstractFacade facade;
	
	private PipelineHashIO pipelineInput;
	final File ditalistfile = new File (resourceDir, "compare.xml");
	File tobecomparefile;
	
	@Before
	public void setUp() throws IOException {
		tempDir = TestUtils.createTempDir(getClass());
		
//		facade = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		
		final File inputDir = new File(resourceDir, "input");
		final File inputMap = new File(inputDir, "test.ditamap");

		final File temporaryDir = new File(tempDir, "temp");
		TestUtils.copy(new File(resourceDir, "temp"), temporaryDir);
		
		final File outDir = new File(tempDir, "out");
		tobecomparefile = new File(outDir, "tobecompared.xml");
		
		pipelineInput.setAttribute("inputmap", inputMap.getPath());
		pipelineInput.setAttribute("basedir", resourceDir.getPath());
		pipelineInput.setAttribute("inputdir", inputDir.getPath());
		pipelineInput.setAttribute("output", tobecomparefile.getPath());
		pipelineInput.setAttribute("outputdir", outDir.getPath());
		pipelineInput.setAttribute("tempDir", temporaryDir.getPath());
		pipelineInput.setAttribute("ditadir", "");
		pipelineInput.setAttribute("ditaext", ".xml");
		pipelineInput.setAttribute("indextype", "xhtml");
		pipelineInput.setAttribute("encoding", "en-US");
		pipelineInput.setAttribute("targetext", ".html");
		pipelineInput.setAttribute("validate", "false");
		pipelineInput.setAttribute("generatecopyouter", "1");
		pipelineInput.setAttribute("outercontrol", "warn");
		pipelineInput.setAttribute("onlytopicinmap", "false");
		pipelineInput.setAttribute("ditalist", new File(temporaryDir, "dita.list").getPath());
		pipelineInput.setAttribute("maplinks", new File(temporaryDir, "maplinks.unordered").getPath());
		
	}
	
	@Test
	public void testtopicmergemodule() throws DITAOTException, IOException
	{
		TopicMergeModule topicmergemodule = new TopicMergeModule();
        topicmergemodule.execute(pipelineInput);

		assertEquals(TestUtils.readFileToString(ditalistfile, true),
					 TestUtils.readFileToString(tobecomparefile, true));
    }

	@After
	public void tearDown() throws IOException {
		TestUtils.forceDelete(tempDir);
	}

}
