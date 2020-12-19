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
import org.dita.dost.util.XMLUtils;

import java.util.List;
import java.util.function.Predicate;

/**
 * Abstract class for modules.
 */
public abstract class AbstractPipelineModuleImpl implements AbstractPipelineModule {

    protected DITAOTLogger logger;
    protected Job job;
    protected XMLUtils xmlUtils;
    protected boolean parallel;
    Predicate<FileInfo> fileInfoFilter;
    List<XmlFilterModule.FilterPair> filters;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

    @Override
    public void setXmlUtils(final XMLUtils xmlUtils) {
        this.xmlUtils = xmlUtils;
    }

    @Override
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
        return this.execute(input.getAttributes());
    }

    @Override
    public void setFileInfoFilter(Predicate<FileInfo> fileInfoFilter) {
        this.fileInfoFilter = fileInfoFilter;
    }

    @Override
    public void setProcessingPipe(List<XmlFilterModule.FilterPair> filters) {
        this.filters = filters;
    }

    public void setParallel(boolean parallel) {
        this.parallel = parallel;
    }
}
