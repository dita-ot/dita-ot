/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

/**
 * Abstract class for Modules which contains the method that every module class should
 * implement. 
 * @author Lian, Li
 * 
 */
public abstract class AbstractPipelineModule {

    /**
     * Start the process of this module with the input.
     * 
     * @param input
     * @return
     * @author Lian, Li
     */
    public abstract AbstractPipelineOutput execute(AbstractPipelineInput input);

}
