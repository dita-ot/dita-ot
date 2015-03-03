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
     */
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
     * @param pipelineModule pipeline module 
     * @param input input
     */
    AbstractPipelineOutput execute(AbstractPipelineModule module,
            AbstractPipelineInput input) throws DITAOTException;
        
    public void setLogger(DITAOTLogger logger);

    public void setJob(Job job);

}
