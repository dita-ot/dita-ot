/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.util.List;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;

//RFE 2987769 Eclipse index-see

public abstract class AbstractExtendDitaWriter implements AbstractWriter, IExtendDitaWriter, IDitaTranstypeIndexWriter {

    protected PipelineHashIO pipelineHashMap = null;
    protected DITAOTLogger logger;
    protected Job job;
    /** List of indexterms */
    protected List<IndexTerm> termList = null;

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
