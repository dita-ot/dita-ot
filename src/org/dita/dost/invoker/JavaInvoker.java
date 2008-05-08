/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.File;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;


/**
 * Invoke the process in Java and use java code to control building process.
 * 
 * @author Lian, Li
 * 
 */
public class JavaInvoker {
	private static DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	/**
	 * Remove all files in certain directory
	 * @param dir
	 * @author Marshall
	 */
	public static void removeFiles(String dir){
		File file = new File(dir);
		int size = file.listFiles().length;
		if(!(file.exists() && file.isDirectory())){
			return;
		}
		for(int i=0; i< size; i++){
			File f = file.listFiles()[i];
			f.deleteOnExit();
		}
	}

    /**
     * Automatically generated constructor for utility class
     */
    private JavaInvoker() {
    }
    
    
	/**
	 * The main flow of the process
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		AbstractFacade facade = new PipelineFacade();
		PipelineHashIO pipelineInput = new PipelineHashIO();
		//pipelineInput.setAttribute("inputmap", "testcase" + File.separator
		//        + "dwDT\\langref\\ditaref-book.ditamap");
        
		pipelineInput.setAttribute("inputmap","E:/DITA-OT-TMP/temp/hierarchy.ditamap");
		pipelineInput.setAttribute("basedir", "E:/DITA-OT-TMP");
		pipelineInput.setAttribute("inputdir", "E:/DITA-OT-TMP/test/testindextarget");
		pipelineInput.setAttribute("output", "E:/DITA-OT-TMP/temp/out");
		pipelineInput.setAttribute("tempDir", "E:/DITA-OT-TMP/temp");
		pipelineInput.setAttribute("ditadir", "E:/DITA-OT-TMP");
		pipelineInput.setAttribute("ditaext", ".xml");
		pipelineInput.setAttribute("indextype", "eclipsehelp");
		pipelineInput.setAttribute("encoding", "en-US");
		pipelineInput.setAttribute("targetext", ".html");
		try {
		
			//pipelineInput.setAttribute("ditaval", "d:\\temp\\DITA-OT\\test\\02.ditaval");
			//removeFiles("C:/testcase/tc5/temp");
			pipelineInput.setAttribute("ditalist", "temp" + File.separator
					+ "dita.list");
			pipelineInput.setAttribute("maplinks", "temp\\maplinks.unordered");
			//facade.execute("GenMapAndTopicList", pipelineInput);
			//facade.execute("DebugAndFilter", pipelineInput);
			facade.execute("IndexTermExtract", pipelineInput);
			//facade.execute("MoveMeta", pipelineInput);
		} catch (DITAOTException e) {
			// TODO Auto-generated catch block
			javaLogger.logException(e);
		}
	}
}