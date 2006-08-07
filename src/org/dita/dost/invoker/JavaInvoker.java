/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.File;

import org.dita.dost.exception.DITAOTException;
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
        
        pipelineInput.setAttribute("inputmap","samples\\hierarchy.ditamap");
        pipelineInput.setAttribute("basedir", "e:\\eclipse\\workspace\\DITA-OT13\\ant");
//        pipelineInput.setAttribute("inputdir", "d:\\temp\\DITA-OT\\test");
        pipelineInput.setAttribute("tempDir", "temp");
        pipelineInput.setAttribute("ditadir", "e:\\eclipse\\workspace\\DITA-OT13");

        try {
			
//			pipelineInput.setAttribute("ditaval", "d:\\temp\\DITA-OT\\test\\02.ditaval");
//			pipelineInput.setAttribute("ditalist", "temp" + File.separator
//			        + "dita.list");
//			pipelineInput.setAttribute("maplinks", "temp\\maplinks.unordered");
//			facade.execute("GenMapAndTopicList", pipelineInput);
			facade.execute("DebugAndFilter", pipelineInput);
			facade.execute("MoveIndex", pipelineInput);
			facade.execute("MoveLinks", pipelineInput);
		} catch (DITAOTException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
