/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.File;

import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;


/**
 * @author Lian, Li
 * 
 */
public class CommandLineInvoker {

    public static void main(String[] args) {
        AbstractFacade facade = new PipelineFacade();
        PipelineHashIO pipelineInput = new PipelineHashIO();

        //pipelineInput.setAttribute("inputmap", "testcase" + File.separator
        //        + "dwDT\\langref\\ditaref-book.ditamap");
        
        pipelineInput.setAttribute("inputmap","sequence.ditamap");
        pipelineInput.setAttribute("basedir", "e:\\eclipse\\workspace\\DOST1.0\\testcase\\dwDT");

        facade.execute("GenMapAndTopicList", pipelineInput);
        //pipelineInput.setAttribute("ditaval", "testcase" + File.separator
        //        + "DOST\\new.ditaval");
        pipelineInput.setAttribute("ditalist", "output" + File.separator
                + "dita.list");
        pipelineInput.setAttribute("maplinks", "temp\\testcase" + File.separator
                + "dwDT\\langref\\maplinks.unordered");
        facade.execute("DebugAndFilter", pipelineInput);
        //facade.execute("MoveIndex", pipelineInput);
        //facade.execute("MoveLinks", pipelineInput);
    }
}
