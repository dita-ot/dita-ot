/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

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

    /**
     * Automatically generated constructor: PipelineFacade.
     */
    public PipelineFacade() {
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
