/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

/**
 * Abstract class for Modules which contains the method that every module class
 * should implement.
 * 
 * @author Lian, Li
 * 
 */
public interface AbstractPipelineModule {

    /**
     * Start the process of this module with the input.
     * 
     * <p>{@link #setLogger(DITAOTLogger)} must be called before calling this method.</p>
     * 
     * @param input input
     * @return output
     * @throws DITAOTException DITAOTException
     */
    AbstractPipelineOutput execute(AbstractPipelineInput input)
            throws DITAOTException;

    /**
     * Set logger for module.
     * 
     * @param logger logger to use for log message
     */
    public void setLogger(DITAOTLogger logger);

}
