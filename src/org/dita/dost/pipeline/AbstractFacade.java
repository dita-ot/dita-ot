/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import org.dita.dost.exception.DITAOTException;

/**
 * AbstractFacade defines the method of executing each module.
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
}
