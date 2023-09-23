/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.dita.dost.module.AbstractModuleTest;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.ChunkModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ChunkModuleOldTest extends AbstractModuleTest {

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of("combine"),
      Arguments.of("dita"),
      Arguments.of("link"),
      Arguments.of("uplevels"),
      Arguments.of("format"),
      Arguments.of("nested"),
      Arguments.of("scope"),
      Arguments.of("topicgroup"),
      Arguments.of("topichead"),
      Arguments.of("map")
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialFile(String testCase) {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelFile(String testCase) {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    chunkModule.setParallel(true);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialMemory(String testCase) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelMemory(String testCase) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    chunkModule.setParallel(true);
    test();
  }

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    final AbstractPipelineInput input = new PipelineHashIO();
    input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
    return input;
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new ChunkModule();
  }
}
