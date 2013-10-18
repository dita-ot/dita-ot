/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.ModuleFactory;
import org.dita.dost.util.Job;

/**
 * PipelineFacade implement AbstractFacade and control the constructing and excuting
 * of a module.
 * 
 * @author Lian, Li
 * 
 */
public final class PipelineFacade implements AbstractFacade {

    private DITAOTLogger logger;
    private Job job;
    private final ModuleFactory factory = ModuleFactory.instance();

    /**
     * Automatically generated constructor: PipelineFacade.
     */
    public PipelineFacade() {
    }


    /**
     * @see org.dita.dost.pipeline.AbstractFacade#execute(java.lang.String, org.dita.dost.pipeline.AbstractPipelineInput)
     * @param input input
     * @param pipelineModuleName pipelineModuleName
     * @return AbstractPipelineOutput
     * @throws DITAOTException DITAOTException
     */
    @Override
    public AbstractPipelineOutput execute(final String pipelineModuleName,
            final AbstractPipelineInput input) throws DITAOTException {
        /*
         * PipelineFacade just call the relevant single module now,
         * in the future can do more complex things here, like call several
         * modules.
         */
        final AbstractPipelineModule module = factory.createModule(pipelineModuleName);
        if (module != null) {
            module.setLogger(logger);
            module.setJob(job);
            return module.execute(input);
        }
        return null;
    }
    
    @Override
    public AbstractPipelineOutput execute(final Class<? extends AbstractPipelineModule> moduleClass,
            final AbstractPipelineInput input) throws DITAOTException {
        final AbstractPipelineModule module = factory.createModule(moduleClass);
        if (module != null) {
            module.setLogger(logger);
            module.setJob(job);
            return module.execute(input);
        }
        return null;
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineModule module,
            final AbstractPipelineInput input) throws DITAOTException {
        module.setLogger(logger);
        module.setJob(job);
        return module.execute(input);
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }
    
}
