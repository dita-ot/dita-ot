/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */

package org.dita.dost.invoker;

import java.util.StringTokenizer;

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

    private final static String KEY_VALUE_PAIR_SEPARATOR = ";";

    private final static String KEY_VALUE_EQUAL_SIGN = "=";

    /**
     * @param module -
     *            The module to set.
     */
    public void setModule(String module) {

        pipelineInput.setAttribute("module", module);

    }

    /**
     * @param inputdita -
     *            The inputdita to set.
     */
    public void setInputdita(String inputdita) {
        pipelineInput.setAttribute("inputdita", inputdita);

    }

    /**
     * @param inputmap -
     *            The inputmap to set.
     */
    public void setInputmap(String inputmap) {
        pipelineInput.setAttribute("inputmap", inputmap);

    }

    /**
     * @param msg -
     *            The msg to set.
     */
    public void setMessage(String msg) {
        pipelineInput.setAttribute("message", msg);
    }

    /**
     * @param baseDir -
     *            base dir to set.
     */
    public void setBasedir(String baseDir) {
        pipelineInput.setAttribute("basedir", baseDir);
    }

    public void setTempdir(String tempdir) {
        pipelineInput.setAttribute("tempDir", tempdir);
    }

    /**
     * 
     * @param extParam
     * extended parameters string, key value pair string separated by ";"
     * eg. extparam="maplinks=XXXX;other=YYYY"
     */
    public void setExtparam(String extParam) {
        String keyValueStr = null;
        String attrName = null;
        String attrValue = null;
        StringTokenizer extParamStrTokenizer = new StringTokenizer(extParam,
                KEY_VALUE_PAIR_SEPARATOR);

        while (extParamStrTokenizer.hasMoreTokens()) {
            
            keyValueStr = extParamStrTokenizer.nextToken();
            int p = keyValueStr.indexOf(KEY_VALUE_EQUAL_SIGN);
            if (p <= 0) {
                System.out.println("error using pipeline extparam attribute: " + keyValueStr);
                return;                
            }
            attrName = keyValueStr.substring(0, p);
            attrValue = keyValueStr.substring(p + 1);
            if (null == attrName || null == attrValue) {
                System.out
                        .println("error using pipeline extparam attribute" + keyValueStr);
            }
            pipelineInput.setAttribute(attrName, attrValue);
            
        }

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