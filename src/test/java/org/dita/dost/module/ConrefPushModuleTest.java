/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Configuration.Mode;
import org.dita.dost.util.Job;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConrefPushModuleTest extends AbstractModuleTest {

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of("conref_push", Mode.STRICT),
      Arguments.of("pushAfter_between_Specialization", Mode.STRICT),
      Arguments.of("pushAfter_with_crossRef", Mode.STRICT),
      Arguments.of("pushAfter_with_InvalidTarget", Mode.STRICT),
      Arguments.of("pushAfter_without_conref", Mode.STRICT),
      Arguments.of("pushBefore_between_Specialization", Mode.STRICT),
      Arguments.of("pushBefore_with_crossRef", Mode.STRICT),
      Arguments.of("pushBefore_with_InvalidTarget", Mode.LAX),
      Arguments.of("pushBefore_without_conref", Mode.STRICT),
      Arguments.of("pushReplace_between_Specialization", Mode.STRICT),
      Arguments.of("pushReplace_with_crossRef", Mode.STRICT),
      Arguments.of("pushReplace_with_InvalidTarget", Mode.LAX),
      Arguments.of("pushReplace_without_conref", Mode.STRICT),
      Arguments.of("simple_pushAfter", Mode.STRICT),
      Arguments.of("simple_pushBefore", Mode.STRICT),
      Arguments.of("simple_pushReplace", Mode.STRICT)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialFile(String testCase, Mode mode) {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.mode = mode;
    this.logger = new TestUtils.CachingLogger(mode.equals(Mode.STRICT));
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelFile(String testCase, Mode mode) {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.mode = mode;
    this.logger = new TestUtils.CachingLogger(mode.equals(Mode.STRICT));
    chunkModule.setParallel(true);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialMemory(String testCase, Mode mode) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.mode = mode;
    this.logger = new TestUtils.CachingLogger(mode.equals(Mode.STRICT));
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    chunkModule.setJob(job);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelMemory(String testCase, Mode mode) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.mode = mode;
    this.logger = new TestUtils.CachingLogger(mode.equals(Mode.STRICT));
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    chunkModule.setJob(job);
    chunkModule.setParallel(true);
    test();
  }

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    return new PipelineHashIO();
  }

  @Override
  protected AbstractPipelineModule getModule() {
    final ConrefPushModule conrefPushModule = new ConrefPushModule();
    conrefPushModule.setFileInfoFilter(fileInfo ->
      fileInfo.format.equals(ATTR_FORMAT_VALUE_DITA) ||
      fileInfo.format.equals(ATTR_FORMAT_VALUE_DITAMAP) &&
      fileInfo.isInput
    );
    return conrefPushModule;
  }
}
