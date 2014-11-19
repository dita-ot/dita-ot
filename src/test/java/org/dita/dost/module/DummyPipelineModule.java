/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job;

/**
 * Dummy pipeline module for testing.
 * 
 * @author Jarno Elovirta
 */
public class DummyPipelineModule implements AbstractPipelineModule {

    public static final AbstractPipelineOutput exp = new AbstractPipelineOutput() {};

    /**
     * @return always returns {@link #exp}
     */
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        return exp;
    }

    public void setLogger(final DITAOTLogger logger) {
        // NOOP
    }

    @Override
    public void setJob(final Job job) {
        // Noop
    }

}