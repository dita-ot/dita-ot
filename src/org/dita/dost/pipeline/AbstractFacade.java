/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

/**
 * @author Lian, Li
 * 
 */
public abstract class AbstractFacade {

    public abstract AbstractPipelineOutput execute(String pipelineModule,
            AbstractPipelineInput input);
}
