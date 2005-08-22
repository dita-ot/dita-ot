/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

/**
 * AbstractFacade defines the method of executing each module.
 * 
 * @author Lian, Li
 * 
 */
public abstract class AbstractFacade {

    /**
     * Excute the specified Java Module.
     * 
     * @param pipelineModule
     * @param input
     * @return AbstractPipelineOutput
     */
    public abstract AbstractPipelineOutput execute(String pipelineModule,
            AbstractPipelineInput input);
}
