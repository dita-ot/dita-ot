/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2008 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.StringUtils;

/**
 * Extensible Ant Invoker.
 * 
 * @author Deborah Pickett
 */
public final class ExtensibleAntInvoker extends Task {

    /** Key value pair separator. */
    private final static String KEY_VALUE_PAIR_SEPARATOR = ";";
    /** Equal sign. */
    private final static String KEY_VALUE_EQUAL_SIGN = "=";

    /** Pipeline. */
    private final PipelineFacade pipeline;
    /** Pipeline input. */
    private final PipelineHashIO pipelineInput;
    /** Nested params. */
    private final ArrayList<Param> params;

    /**
     * Constructor.
     */
    public ExtensibleAntInvoker() {
        super();
        pipeline = new PipelineFacade();
        pipelineInput = new PipelineHashIO();
        params = new ArrayList<Param>();
    }

    /**
     * Set base directory.
     * @param s base directory
     */
    public void setBasedir(final String s) {
        pipelineInput.setAttribute("basedir", s);
    }

    /**
     * Get base directory
     * @return base directory
     */
    public String getBasedir() {
        return pipelineInput.getAttribute("basedir");
    }

    /**
     * Set module.
     * @param module module name
     */
    public void setModule(final String module) {
        pipelineInput.setAttribute("module", module);
    }

    /**
     * Get module.
     * @return module name
     */
    public String getModule() {
        return pipelineInput.getAttribute("module");
    }

    /**
     * Set message.
     * @param m message
     */
    public void setMessage(final String m) {
        pipelineInput.setAttribute("message", m);
    }

    /**
     * Get message.
     * @return message
     */
    public String getMessage() {
        return pipelineInput.getAttribute("message");
    }

    /**
     * Set input data.
     * @param inputdita input data file
     */
    public void setInputdita(final String inputdita) {
        pipelineInput.setAttribute("inputdita", inputdita);
    }

    /**
     * Set input map.
     * @param inputmap input map file
     */
    public void setInputmap(final String inputmap) {
        pipelineInput.setAttribute("inputmap", inputmap);
    }

    /**
     * Set temporary directory.
     * @param tempdir temporary directory
     */
    public void setTempdir(final String tempdir) {
        pipelineInput.setAttribute("tempDir", tempdir);
    }

    /**
     * Set extra parameter values for input.
     * 
     * Value is a key-value pair string separated by ";" e.g.
     * {@code maplinks=XXXX;other=YYYY}
     * 
     * @param extParam extended parameters string
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
                msg = MessageUtils.getMessage("DOTJ006F", params).toString();
                throw new RuntimeException(msg);
            }

            attrName = keyValueStr.substring(0, p).trim();
            attrValue = keyValueStr.substring(p + 1).trim();

            if (StringUtils.isEmptyString(attrName) ||
                    StringUtils.isEmptyString(attrValue)) {
                String msg = null;
                final Properties params = new Properties();

                params.put("%1", keyValueStr);
                msg = MessageUtils.getMessage("DOTJ006F", params).toString();
                throw new RuntimeException(msg);
            }

            pipelineInput.setAttribute(attrName, attrValue);
        }

    }

    /**
     * Handle nested parameters. Add the key/value to the pipeline hash, unless
     * the "if" attribute is set and refers to a unset property.
     * @return parameter
     */
    public Param createParam() {
        final Param p = new Param();
        params.add(p);
        return p;
    }

    /**
     * Execution point of this invoker.
     * @throws BuildException exception
     */
    @Override
    public void execute() throws BuildException {
        if (getModule() == null) {
            throw new BuildException("Module attribute must be specified");
        }
        /* Set basedir if not already set. */
        if (pipelineInput.getAttribute("basedir") == null) {
            pipelineInput.setAttribute("basedir", getProject().getBaseDir().getAbsolutePath());
        }
        /* Set params. */
        for (final Param p : params) {
            if (!p.isValid()) {
                throw new BuildException("Incomplete parameter");
            }
            // Check the "if" attribute.
            final String ifProperty = p.getIf();
            final String unlessProperty = p.getUnless();
            if ((ifProperty == null || getProject().getProperties().containsKey(ifProperty))
                    && (unlessProperty == null || !getProject().getProperties().containsKey(unlessProperty))) {
                pipelineInput.setAttribute(p.getName(), p.getValue());
            }
        }

        final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
        logger.setTask(this);
        pipeline.setLogger(logger);
        try {
            pipeline.execute(getModule(), pipelineInput);
        } catch (final DITAOTException e) {
            throw new BuildException("Failed to run pipeline: " + e.getMessage(), e);
        }
    }

    /** Nested parameters. */
    public static class Param {

        private String name;
        private String value;
        private String ifProperty;
        private String unlessProperty;

        /**
         * Get parameter name.
         * @return parameter name, {@code null} if not set
         */
        public String getName() {
            return name;
        }

        /**
         * Validate that all required attributes have been set.
         * @return isValid {@code true} is valid object, otherwise {@code false}
         */
        public boolean isValid() {
            return (name != null && value != null);
        }

        /**
         * Set parameter name.
         * @param s name
         */
        public void setName(final String s) {
            name = s;
        }

        /**
         * Get parameter value.
         * @return parameter value, {@code null} if not set
         */
        public String getValue() {
            return value;
        }

        /**
         * Set parameter value.
         * @param v parameter value
         */
        public void setExpression(final String v) {
            value = v;
        }

        /**
         * Set parameter value.
         * @param v parameter value
         */
        public void setValue(final String v) {
            value = v;
        }

        /**
         * Set parameter file value.
         * @param v parameter file value
         */
        public void setLocation(final File v) {
            value = v.getPath();
        }

        /**
         * Get if condition property name
         * @return if condition parameter name, {@code null} if not set
         */
        public String getIf() {
            return ifProperty;
        }

        /**
         * Set if condition parameter name
         * @param p parameter name
         */
        public void setIf(final String p) {
            ifProperty = p;
        }

        /**
         * Get unless condition property name
         * @return unless condition parameter name, {@code null} if not set
         */
        public String getUnless() {
            return unlessProperty;
        }

        /**
         * Set unless condition parameter name
         * @param p parameter name
         */
        public void setUnless(final String p) {
            unlessProperty = p;
        }

    }

}