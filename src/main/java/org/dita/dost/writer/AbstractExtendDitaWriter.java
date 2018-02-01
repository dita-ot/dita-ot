/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import java.util.List;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;

//RFE 2987769 Eclipse index-see

public abstract class AbstractExtendDitaWriter implements AbstractWriter, IExtendDitaWriter, IDitaTranstypeIndexWriter {

    private PipelineHashIO pipelineHashMap = null;
    DITAOTLogger logger;
    private Job job;
    /** List of indexterms */
    List<IndexTerm> termList = null;

    // AbstractWriter methods

    public void setTermList(final List<IndexTerm> termList) {
        this.termList = termList;
    }

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

    // IExtendDitaWriter methods

    @Override
    public final PipelineHashIO getPipelineHashIO() {
        return pipelineHashMap;
    }

    @Override
    public final void setPipelineHashIO(final PipelineHashIO hashIO) {
        pipelineHashMap = hashIO;
    }

}
