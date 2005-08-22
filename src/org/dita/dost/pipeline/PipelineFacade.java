/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.ModuleFactory;

/**
 * PipelineFacade implement AbstractFacade and control the constructing and excuting
 * of a module.
 * 
 * @author Lian, Li
 * 
 */
public class PipelineFacade extends AbstractFacade {

    /**
     * Automatically generated constructor: PipelineFacade
     */
    public PipelineFacade() {
    }


    /**
     * @see org.dita.dost.pipeline.AbstractFacade#execute(java.lang.String, org.dita.dost.pipeline.AbstractPipelineInput)
     * 
     */
    public AbstractPipelineOutput execute(String pipelineModuleName,
            AbstractPipelineInput input) {
        // PipelineFacade just call the relevant single module
        // in the future can do more complex things here, like call several
        // modules
        AbstractPipelineModule module = null;
        AbstractPipelineOutput output = null;
        //((PipelineHashIO)input).setAttribute("tempDir",new String("temp"));

        module = ModuleFactory.instance().createModule(pipelineModuleName);

        if (module != null) {
            output = module.execute(input);
        }

        return output;
    }

}
