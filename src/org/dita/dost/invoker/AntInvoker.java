/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;


/**
 * @author Lian, Li
 * 
 */
public class AntInvoker extends Task {

    private PipelineFacade pipeline;
    private PipelineHashIO pipelineInput;

    /**
     * @param module - The module to set.
     */
    public void setModule(String module) {

        pipelineInput.setAttribute("module", module);

    }

    /**
     * @param inputdita - The inputdita to set.
     */
    public void setInputdita(String inputdita) {
        pipelineInput.setAttribute("inputdita", inputdita);

    }

    /**
     * @param inputmap - The inputmap to set.
     */
    public void setInputmap(String inputmap) {
        pipelineInput.setAttribute("inputmap", inputmap);

    }

    /**
     * @param msg - The msg to set.
     */
    public void setMessage(String msg) {
        pipelineInput.setAttribute("message", msg);
    }

    /**
     * @param msg - The msg to set.
     */
    public void setOutput(String output) {
        pipelineInput.setAttribute("output", output);
    }

    /**
     * @param ditaval - The ditaval file name to set.
     */
    public void setDitaval(String ditaval) {
    	pipelineInput.setAttribute("ditaval", ditaval);
    }
    
    /**
     * @param maplinks - The maplinks.unordered file to set.
     */
    public void setMaplinks(String maplinks) {
    	pipelineInput.setAttribute("maplinks", maplinks);
    }
    
    /**
     * @param ditaList - dita.list file to set.
     */
    public void setDitalist(String ditaList) {
    	pipelineInput.setAttribute("ditalist", ditaList);
    }
    
    /**
     * @param baseDir - base dir to set.
     */
    public void setBasedir(String baseDir) {
    	pipelineInput.setAttribute("basedir", baseDir);
    }
    
    /**
     * 
     */
    public AntInvoker() {
        super();
        pipeline = new PipelineFacade();
        pipelineInput = new PipelineHashIO();
    }

    /**
     * 
     */
    public void execute() throws BuildException {

        System.out.println(pipelineInput.getAttribute("message"));
        pipeline.execute(pipelineInput.getAttribute("module"), pipelineInput);

    }
}
