package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.TopicMergeModule;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.junit.BeforeClass;
import org.junit.Test;


public class TestTopicMergeModule {
	public static TopicMergeModule module;
	
	private static AbstractFacade facade;
	
	private static PipelineHashIO pipelineInput;
	File ditalistfile=new File ("test-stub" + File.separator + "TestTopicMergeModule" + File.separator + "compare.xml");
	File tobecomparefile=new File ("test-stub" + File.separator + "TestTopicMergeModule" + File.separator + "out" + File.separator + "tobecompared.xml");
	
	@BeforeClass
	public static void setUp(){
		
		
		
		facade = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		
		String baseDir = "test-stub" + File.separator + "TestTopicMergeModule";
		String tempDir = "test-stub" + File.separator + "TestTopicMergeModule" + File.separator + "temp";
		String inputDir = "test-stub" + File.separator + "TestTopicMergeModule" + File.separator + "input";
		
		String inputMap = inputDir + File.separator + "test.ditamap";
		
		String outDir = "test-stub" + File.separator + "TestTopicMergeModule" + File.separator + "out";
		String outputmap=outDir + File.separator + "tobecompared.xml";
		//Create the temp dir
		File dir = new File(baseDir, tempDir);
		if(!dir.exists()){
			dir.mkdir();
		}
		
		pipelineInput.setAttribute("inputmap", inputMap);
		pipelineInput.setAttribute("basedir", baseDir);
		pipelineInput.setAttribute("inputdir", inputDir);
		pipelineInput.setAttribute("output", outputmap);
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
		
	}
	
	@Test
	public void testtopicmergemodule() throws DITAOTException, IOException
	{
		
		try
		{
				TopicMergeModule topicmergemodule=new TopicMergeModule();
		        topicmergemodule.execute(pipelineInput);
		}
		catch (Exception e) {
			e.printStackTrace();
		
	    }finally
	    {
	    	BufferedReader topicmergemodulebuf = new BufferedReader(new FileReader(ditalistfile));
	    	BufferedReader tobecomparedfilebuf = new BufferedReader(new FileReader(tobecomparefile));
	    	String str;
            String st1="";
            topicmergemodulebuf.readLine();
            while ((str = topicmergemodulebuf.readLine()) != null) 
            {      
            	st1=st1+str;
            }
          
            topicmergemodulebuf.close();
            String ste;
            String st2="";
            tobecomparedfilebuf.readLine();
            while ((ste = tobecomparedfilebuf.readLine()) != null) 
            {       
            	st2=st2+ste;
            }
          
            tobecomparedfilebuf.close();
           assertEquals(st1,st2);
	    }

    }
}
