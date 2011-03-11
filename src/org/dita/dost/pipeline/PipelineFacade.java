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
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.ModuleFactory;

/**
 * PipelineFacade implement AbstractFacade and control the constructing and excuting
 * of a module.
 * 
 * @author Lian, Li
 * 
 */
public final class PipelineFacade implements AbstractFacade {

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
    public AbstractPipelineOutput execute(String pipelineModuleName,
            AbstractPipelineInput input) throws DITAOTException {
        /* 
         * PipelineFacade just call the relevant single module now,
         * in the future can do more complex things here, like call several
         * modules. 
         */
        AbstractPipelineModule module = null;                
        ModuleFactory factory = ModuleFactory.instance();
        		
		module = factory.createModule(pipelineModuleName);
		
        if (module != null) {
            return module.execute(input);
        }

        return null;
    }

}
