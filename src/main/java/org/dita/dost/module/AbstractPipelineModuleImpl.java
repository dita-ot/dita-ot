/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;

import java.util.function.Predicate;

/**
 * Abstract class for modules.
 */
public abstract class AbstractPipelineModuleImpl implements AbstractPipelineModule {

    protected DITAOTLogger logger;
    protected Job job;
    Predicate<FileInfo> fileInfoFilter;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

    abstract public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException;

    @Override
    public void setFileInfoFilter(Predicate<FileInfo> fileInfoFilter) {
        this.fileInfoFilter = fileInfoFilter;
    }
}
