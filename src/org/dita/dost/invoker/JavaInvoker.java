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
public class JavaInvoker {

    public static void main(String[] args) {
        AbstractFacade facade = new PipelineFacade();
        PipelineHashIO pipelineInput = new PipelineHashIO();

        //pipelineInput.setAttribute("inputmap", "testcase" + File.separator
        //        + "dwDT\\langref\\ditaref-book.ditamap");
        
        pipelineInput.setAttribute("inputmap","test.ditamap");
        pipelineInput.setAttribute("basedir", "e:\\eclipse\\workspace\\DITA-OT1.1\\test");

        facade.execute("GenMapAndTopicList", pipelineInput);
        //pipelineInput.setAttribute("ditaval", "testcase" + File.separator
        //        + "DOST\\new.ditaval");
        pipelineInput.setAttribute("ditalist", "temp" + File.separator
                + "dita.list");
        pipelineInput.setAttribute("maplinks", "temp\\maplinks.unordered");
        facade.execute("DebugAndFilter", pipelineInput);
        facade.execute("MoveIndex", pipelineInput);
        facade.execute("MoveLinks", pipelineInput);
    }
}
