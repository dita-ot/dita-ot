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
import org.dita.dost.util.Job;

/**
 * AbstractFacade defines the method of executing each module.
 * 
 * If a module makes modifications to the provided job configuration, it <em>must</em> serialize
 * the job configuration before finishing execution.
 * 
 * @author Lian, Li
 * 
 */
public interface AbstractFacade {

    /**
     * Excute the specified Java Module.
     * 
     * @param pipelineModule pipelineModule name
     * @param input input
     * @return AbstractPipelineOutput
     * @throws DITAOTException DITAOTException
     * @deprecated use {@link #execute(Class, AbstractPipelineInput)} instead. Deprecated since 2.3
     */
    @Deprecated
    AbstractPipelineOutput execute(String pipelineModule,
            AbstractPipelineInput input) throws DITAOTException;

    /**
     * Excute the specified Java Module.
     * 
     * @param pipelineModule pipeline module class 
     * @param input input
     * @return AbstractPipelineOutput
     * @throws DITAOTException DITAOTException
     * @since 1.6
     */
    AbstractPipelineOutput execute(Class<? extends AbstractPipelineModule> pipelineModule,
            AbstractPipelineInput input) throws DITAOTException;
    
    /**
     * Excute the specified Java Module.
     * 
     * @param module pipeline module
     * @param input input
     */
    AbstractPipelineOutput execute(AbstractPipelineModule module,
            AbstractPipelineInput input) throws DITAOTException;
        
    void setLogger(DITAOTLogger logger);

    void setJob(Job job);

}
