/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying license.txt file for applicable licenses.

 */
package org.dita.dost.writer;

import org.dita.dost.pipeline.PipelineHashIO;

//RFE 2987769 Eclipse index-see
interface IExtendDitaWriter {

    PipelineHashIO getPipelineHashIO();

    void setPipelineHashIO(PipelineHashIO pipelineHashIO);

}
