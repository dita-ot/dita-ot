/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import java.util.List;
import java.util.function.Predicate;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Configuration.Mode;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;

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
  public AbstractPipelineOutput execute(final AbstractPipelineInput input) {
    return exp;
  }

  public void setLogger(final DITAOTLogger logger) {
    // NOOP
  }

  @Override
  public void setJob(final Job job) {
    // Noop
  }

  @Override
  public void setXmlUtils(final XMLUtils xmlUtils) {
    // NOOP
  }

  @Override
  public void setFileInfoFilter(Predicate<FileInfo> fileInfoFilter) {
    // Noop
  }

  @Override
  public void setProcessingPipe(List<XmlFilterModule.FilterPair> pipe) {
    // Noop
  }

  @Override
  public void setParallel(final boolean parallel) {
    // Noop
  }

  @Override
  public void setProcessingMode(final Mode processingMode) {
    // Noop
  }
}
