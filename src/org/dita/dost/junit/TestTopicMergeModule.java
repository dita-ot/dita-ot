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
	File ditalistfile=new File ("test-stub/TestTopicMergeModule/compare.xml");
	File tobecomparefile=new File ("test-stub/TestTopicMergeModule/out/tobecompared.xml");
	
	@BeforeClass
	public static void setUp(){
		
		
		
		facade = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		
		String baseDir = "test-stub/TestTopicMergeModule";
		String tempDir = "test-stub/TestTopicMergeModule/temp";
		String inputDir = "test-stub/TestTopicMergeModule/input";
		
		String inputMap = inputDir + "/test.ditamap";
		
		String outDir = "test-stub/TestTopicMergeModule/out";
		String outputmap=outDir+"/tobecompared.xml";
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
		pipelineInput.setAttribute("ditalist", tempDir + "/dita.list");
		pipelineInput.setAttribute("maplinks", tempDir + "/maplinks.unordered");
		
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
