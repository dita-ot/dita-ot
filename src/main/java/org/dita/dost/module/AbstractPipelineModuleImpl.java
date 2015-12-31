/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job;

/**
 * Abstract class for modules.
 */
public abstract class AbstractPipelineModuleImpl implements AbstractPipelineModule {

    protected DITAOTLogger logger;
    protected Job job;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    @Override
    public void setJob(final Job job) {
        this.job = job;
    }
    
    abstract public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException;

}
