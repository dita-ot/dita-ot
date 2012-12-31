/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */

package org.dita.dost.invoker;

import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.StringUtils;

/**
 * The class that ant scripts invokes by the <pipeline> tag.
 * @author Lian, Li
 * @deprecated use {@link ExtensibleAntInvoker} instead
 */
@Deprecated
public final class AntInvoker extends Task {

    /**key value pair separator.*/
    private final static String KEY_VALUE_PAIR_SEPARATOR = ";";
    /**equal sign.*/
    private final static String KEY_VALUE_EQUAL_SIGN = "=";
    /**pipeline.*/
    private final PipelineFacade pipeline;
    /**hashIO.*/
    private final PipelineHashIO pipelineInput;

    /**
     * Defalut Constructor. Construct pipeline & input instance.
     */
    public AntInvoker() {
        super();
        pipeline = new PipelineFacade();
        pipelineInput = new PipelineHashIO();
    }

    /**
     * Set the "module" attribute for input.
     * @param module - The module to set.
     */
    public void setModule(final String module) {
        pipelineInput.setAttribute("module", module);
    }

    /**
     * Set the "inputdata" attribute for input.
     * @param inputdita - The inputdita to set.
     */
    public void setInputdita(final String inputdita) {
        pipelineInput.setAttribute("inputdita", inputdita);
    }

    /**
     * Set the "inputmap" attribute for input.
     * @param inputmap - The inputmap to set.
     */
    public void setInputmap(final String inputmap) {
        pipelineInput.setAttribute("inputmap", inputmap);
    }

    /**
     * Set the "message" attribute for input.
     * @param msg -  The msg to set.
     */
    public void setMessage(final String msg) {
        pipelineInput.setAttribute("message", msg);
    }

    /**
     * Set the "basedir" attribute for input.
     * @param baseDir - base dir to set.
     */
    public void setBasedir(final String baseDir) {
        pipelineInput.setAttribute("basedir", baseDir);
    }

    /**
     * Set the 'tempDir' attribute for input.
     * @param tempdir temp
     */
    public void setTempdir(final String tempdir) {
        pipelineInput.setAttribute("tempDir", tempdir);
    }

    /**
     * Set extra parameter values for input.
     * @param extParam extended parameters string, key value pair string separated by
     *            ";" eg. extparam="maplinks=XXXX;other=YYYY"
     */
    public void setExtparam(final String extParam) {
        String keyValueStr = null;
        String attrName = null;
        String attrValue = null;
        final StringTokenizer extParamStrTokenizer = new StringTokenizer(extParam,
                KEY_VALUE_PAIR_SEPARATOR);

        while (extParamStrTokenizer.hasMoreTokens()) {
            int p;
            keyValueStr = extParamStrTokenizer.nextToken();
            p = keyValueStr.indexOf(KEY_VALUE_EQUAL_SIGN);

            if (p <= 0) {
                String msg = null;
                final Properties params = new Properties();

                params.put("%1", keyValueStr);
                msg = MessageUtils.getInstance().getMessage("DOTJ006F", params).toString();
                throw new RuntimeException(msg);
            }

            attrName = keyValueStr.substring(0, p).trim();
            attrValue = keyValueStr.substring(p + 1).trim();

            if (StringUtils.isEmptyString(attrName) ||
                    StringUtils.isEmptyString(attrValue)) {
                String msg = null;
                final Properties params = new Properties();

                params.put("%1", keyValueStr);
                msg = MessageUtils.getInstance().getMessage("DOTJ006F", params).toString();
                throw new RuntimeException(msg);
            }

            pipelineInput.setAttribute(attrName, attrValue);
        }

    }

    /**
     * execution point of this invoker.
     * @throws BuildException Exception
     */
    @Override
    public void execute() throws BuildException {
        pipeline.setLogger(new DITAOTAntLogger(getProject()));
        try {
            pipeline.execute(pipelineInput.getAttribute("module"),
                    pipelineInput);
        } catch (final DITAOTException e) {
            throw new BuildException(e.getMessage(), e);
        }

    }
}