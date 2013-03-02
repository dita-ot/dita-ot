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
import org.dita.dost.module.Content;
import org.dita.dost.pipeline.PipelineHashIO;

//RFE 2987769 Eclipse index-see

public abstract class AbstractExtendDitaWriter implements AbstractWriter, IExtendDitaWriter, IDitaTranstypeIndexWriter {

    protected PipelineHashIO pipelineHashMap = null;
    protected DITAOTLogger logger;
    /** List of indexterms */
    protected List<IndexTerm> termList = null;

    // AbstractWriter methods

    /**
     * Set the content for output.
     * 
     * @param content The content to output
     */
    @Override
    public final void setContent(final Content content) {
        termList = (List<IndexTerm>) content.getCollection();
    }

    @Override
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
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
