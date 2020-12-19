/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Abstract class for Modules which contains the method that every module class
 * should implement.
 *
 * @author Lian, Li
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
     * @deprecated implement {@link #execute(Map)} instead.
     */
    @Deprecated
    AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException;

    /**
     * Start the process of this module with the input.
     *
     * <p>{@link #setLogger(DITAOTLogger)} must be called before calling this method.</p>
     *
     * @param input input
     * @return output
     * @throws DITAOTException DITAOTException
     */
    default AbstractPipelineOutput execute(Map<String, String> input) throws DITAOTException {
        return execute(new PipelineHashIO(input));
    }

    /**
     * Set logger for module.
     *
     * @param logger logger to use for log message
     */
    void setLogger(DITAOTLogger logger);

    void setJob(Job job);

    void setXmlUtils(XMLUtils xmlUtils);

    void setFileInfoFilter(Predicate<FileInfo> fileInfoFilter);

    default void setProcessingPipe(List<XmlFilterModule.FilterPair> pipe) {
    }

    void setParallel(boolean parallel);
}
