/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ChunkModuleTest extends AbstractModuleTest {

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of("case1"),
      Arguments.of("case2"),
      Arguments.of("case3"),
      Arguments.of("case4"),
      Arguments.of("case5"),
      Arguments.of("case6"),
      Arguments.of("case7"),
      Arguments.of("conflict_by_topic"),
      Arguments.of("copy_to1"),
      Arguments.of("copy_to2"),
      Arguments.of("conflict_same_id"),
      Arguments.of("conflict_to_content"),
      Arguments.of("external_chunk"),
      Arguments.of("link1"),
      Arguments.of("link2"),
      Arguments.of("anchor1"),
      Arguments.of("anchor2"),
      //                {"Attribute_map1"},
      Arguments.of("Attribute_map2"),
      Arguments.of("Attribute_map3"),
      Arguments.of("Attribute_map4"),
      Arguments.of("Attribute_map5"),
      Arguments.of("Attribute_map6"),
      Arguments.of("Attribute_map7"),
      Arguments.of("Attribute_map8"),
      Arguments.of("Attribute_map9"),
      Arguments.of("Attribute_map10"),
      Arguments.of("Attribute_map11"),
      Arguments.of("ByTopic_map2"),
      Arguments.of("ByTopic_map3"),
      Arguments.of("ByTopic_map4"),
      Arguments.of("ByTopic_map5"),
      Arguments.of("ByTopic_map6"),
      Arguments.of("ByTopic_map7"),
      Arguments.of("ByTopic_batseparate0"),
      Arguments.of("FixChunk_map1"),
      Arguments.of("FixChunk_map2"),
      Arguments.of("FixChunk_map3"),
      Arguments.of("FixChunk_map4"),
      Arguments.of("FixChunk_map5"),
      Arguments.of("FixChunk_map6"),
      Arguments.of("FixChunk_map7"),
      Arguments.of("FixChunk_map8"),
      Arguments.of("chunk_duplicate_tocontent"),
      Arguments.of("chunk_hogs_memory"),
      Arguments.of("chunk_map_tocontent"),
      Arguments.of("chunk_rewrite_tocontent"),
      Arguments.of("topicgroup_chunk"),
      Arguments.of("unware_chunk_content"),
      Arguments.of("unware_chunk_content2"),
      Arguments.of("with_non_dita"),
      Arguments.of("to_content_with_namespace_mathml"),
      Arguments.of("to_content_with_namespace_xsi")
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialFile(String testCase) {
    this.testCase = testCase;
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelFile(String testCase) {
    this.testCase = testCase;
    chunkModule.setParallel(true);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialMemory(String testCase) throws IOException {
    this.testCase = testCase;
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    chunkModule.setJob(job);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelMemory(String testCase) throws IOException {
    this.testCase = testCase;
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    chunkModule.setJob(job);
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
  protected AbstractPipelineModule getModule(final File tempDir) {
    return new ChunkModule();
  }
}
