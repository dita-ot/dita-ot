/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2008 All Rights Reserved.
 */
package org.dita.dost.invoker;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;

/**
 * Ant task for executing pipeline modules.
 * 
 * @author Deborah Pickett
 */
public final class ExtensibleAntInvoker extends Task {

    /** Pipeline. */
    private final PipelineFacade pipeline;
    /** Pipeline attributes and parameters */
    private final Map<String, String> attrs = new HashMap<String, String>();
    /** Nested params. */
    private final ArrayList<Param> pipelineParams;
    /** Nested modules. */
    private final ArrayList<Module> modules;

    /**
     * Constructor.
     */
    public ExtensibleAntInvoker() {
        super();
        pipeline = new PipelineFacade();
        pipelineParams = new ArrayList<Param>();
        modules = new ArrayList<Module>();
    }

    /**
     * Set message.
     * @param m message
     */
    public void setMessage(final String m) {
        attrs.put("message", m);
    }

    /**
     * Set input map.
     * @param inputmap input map file, may be relative or absolute
     */
    public void setInputmap(final String inputmap) {
        attrs.put(ANT_INVOKER_PARAM_INPUTMAP, inputmap);
    }

    /**
     * Set temporary directory.
     * @param tempdir temporary directory
     */
    public void setTempdir(final File tempdir) {
        attrs.put(ANT_INVOKER_PARAM_TEMPDIR, tempdir.getAbsolutePath());
    }

    /**
     * Handle nested parameters. Add the key/value to the pipeline hash, unless
     * the "if" attribute is set and refers to a unset property.
     * @return parameter
     */
    public Param createParam() {
        final Param p = new Param();
        pipelineParams.add(p);
        return p;
    }

    /**
     * Handle nested module elements.
     * 
     * @since 1.6
     */
    public void addConfiguredModule(final Module m) {
        modules.add(m);
    }
    
    /**
     * Execution point of this invoker.
     * @throws BuildException exception
     */
    @Override
    public void execute() throws BuildException {
        if (modules.isEmpty()) {
            throw new BuildException("Module must be specified");
        }
        if (attrs.get(ANT_INVOKER_PARAM_BASEDIR) == null) {
            attrs.put(ANT_INVOKER_PARAM_BASEDIR, getProject().getBaseDir().getAbsolutePath());
        }
        for (final Param p : pipelineParams) {
            if (!p.isValid()) {
                throw new BuildException("Incomplete parameter");
            }
            final String ifProperty = p.getIf();
            final String unlessProperty = p.getUnless();
            if ((ifProperty == null || getProject().getProperties().containsKey(ifProperty))
                    && (unlessProperty == null || !getProject().getProperties().containsKey(unlessProperty))) {
                attrs.put(p.getName(), p.getValue());
            }
        }

        final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
        logger.setTask(this);
        pipeline.setLogger(logger);
        try {
            for (final Module m: modules) {
                final PipelineHashIO pipelineInput = new PipelineHashIO();
                for (final Map.Entry<String, String> e: attrs.entrySet()) {
                    pipelineInput.setAttribute(e.getKey(), e.getValue());
                }
                for (final Param p : m.params) {
                    if (!p.isValid()) {
                        throw new BuildException("Incomplete parameter");
                    }
                    final String ifProperty = p.getIf();
                    final String unlessProperty = p.getUnless();
                    if ((ifProperty == null || getProject().getProperties().containsKey(ifProperty))
                            && (unlessProperty == null || !getProject().getProperties().containsKey(unlessProperty))) {
                        pipelineInput.setAttribute(p.getName(), p.getValue());
                    }
                }
                pipeline.execute(m.getImplementation(), pipelineInput);
            }
        } catch (final DITAOTException e) {
            throw new BuildException("Failed to run pipeline: " + e.getMessage(), e);
        }
    }
    
    /**
     * Nested pipeline module element configuration.
     * 
     * @since 1.6
     */
    public static class Module {
       
        public final List<Param> params = new ArrayList<Param>();
        private Class<? extends AbstractPipelineModule> cls;
        
        public void setClass(final Class<? extends AbstractPipelineModule> cls) {
            this.cls = cls;
        }
        
        public void addConfiguredParam(final Param p) {
            params.add(p);
        }
        
        public Class<? extends AbstractPipelineModule> getImplementation() {
            return cls;
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