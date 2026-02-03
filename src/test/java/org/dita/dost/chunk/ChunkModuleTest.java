/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import static org.dita.dost.TestUtils.CachingLogger.Message.Level.WARN;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.module.AbstractModuleTest;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ChunkModuleTest extends AbstractModuleTest {

  private int warningCount;

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of("Attribute_map10", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map5", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map6", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map8", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_batseparate0", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_map2", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_map4", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_map5", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_map7", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map4", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map5", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map6", Collections.emptyMap(), 0),
      Arguments.of("case1", Collections.emptyMap(), 0),
      Arguments.of("case3", Collections.emptyMap(), 0),
      Arguments.of("case5", Collections.emptyMap(), 0),
      Arguments.of("case6", Collections.emptyMap(), 0),
      Arguments.of("chunk_hogs_memory", Collections.emptyMap(), 0),
      Arguments.of("conflict_by_topic", Collections.emptyMap(), 0),
      Arguments.of("conflict_to_content", Collections.emptyMap(), 0),
      Arguments.of("copy_to1", Collections.emptyMap(), 0),
      Arguments.of("copy_to2", Collections.emptyMap(), 0),
      Arguments.of("external_chunk", Collections.emptyMap(), 0),
      Arguments.of("link1", Collections.emptyMap(), 0),
      Arguments.of("link2", Collections.emptyMap(), 0),
      Arguments.of("topicgroup_chunk", Collections.emptyMap(), 0),
      Arguments.of("with_non_dita", Collections.emptyMap(), 0)
    );
  }

  public static Stream<Arguments> dataSuccess() {
    return Stream.of(
      Arguments.of("Attribute_map11", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map2", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map3", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map4", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map7", Collections.emptyMap(), 0),
      Arguments.of("Attribute_map9", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_map3", Collections.emptyMap(), 0),
      Arguments.of("ByTopic_map6", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map1", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map2", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map3", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map7", Collections.emptyMap(), 0),
      Arguments.of("FixChunk_map8", Collections.emptyMap(), 0),
      Arguments.of("anchor1", Collections.emptyMap(), 0),
      Arguments.of("anchor2", Collections.emptyMap(), 0),
      Arguments.of("case2", Collections.emptyMap(), 0),
      Arguments.of("case4", Collections.emptyMap(), 0),
      Arguments.of("case7", Collections.emptyMap(), 0),
      Arguments.of("chunk_duplicate_tocontent", Collections.emptyMap(), 0),
      Arguments.of("chunk-combine-within-split", Collections.emptyMap(), 0),
      Arguments.of("chunk_map_tocontent", Collections.emptyMap(), 0),
      Arguments.of("chunk_rewrite_tocontent", Collections.emptyMap(), 0),
      Arguments.of("combine", Collections.emptyMap(), 1),
      Arguments.of("combine-empty-ditabase", Collections.emptyMap(), 0),
      Arguments.of("conflict_same_id", Collections.emptyMap(), 0),
      Arguments.of("dita", Collections.emptyMap(), 0),
      Arguments.of("duplicate", Collections.emptyMap(), 0),
      Arguments.of("format", Collections.emptyMap(), 0),
      Arguments.of("link", Collections.emptyMap(), 0),
      Arguments.of("managing-links", Collections.emptyMap(), 0),
      Arguments.of("managing-links-duplicates", Collections.emptyMap(), 0),
      Arguments.of("map", Collections.emptyMap(), 0),
      Arguments.of("multiple", Collections.emptyMap(), 0),
      Arguments.of("nested", Collections.emptyMap(), 0),
      Arguments.of("nested-select-branch", Collections.emptyMap(), 0),
      Arguments.of("nested-select-branch-compound", Collections.emptyMap(), 0),
      Arguments.of("nested-select-topic", Collections.emptyMap(), 0),
      Arguments.of("override", Map.of("root-chunk-override", "combine"), 0),
      Arguments.of("scope", Collections.emptyMap(), 0),
      Arguments.of("split", Collections.emptyMap(), 0),
      Arguments.of("split-dita", Collections.emptyMap(), 0),
      Arguments.of("split-empty-ditabase", Collections.emptyMap(), 0),
      Arguments.of("split-hierarchy", Collections.emptyMap(), 0),
      Arguments.of("split-map", Collections.emptyMap(), 0),
      Arguments.of("to_content_with_namespace_mathml", Collections.emptyMap(), 0),
      Arguments.of("to_content_with_namespace_xsi", Collections.emptyMap(), 0),
      Arguments.of("topicgroup", Collections.emptyMap(), 1),
      Arguments.of("topichead", Collections.emptyMap(), 1),
      Arguments.of("unware_chunk_content2", Collections.emptyMap(), 0),
      Arguments.of("uplevels", Collections.emptyMap(), 0),
      Arguments.of("uplevels-dir", Collections.emptyMap(), 0),
      Arguments.of("uplevels-parallel", Collections.emptyMap(), 0),
      Arguments.of("uplevels-root", Collections.emptyMap(), 0)
    );
  }

  @BeforeEach
  void setup() {
    this.logger = new TestUtils.CachingLogger(mode.equals(Configuration.Mode.STRICT));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialFile(String testCase, Map<String, String> params, int warningCount) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.params = params;
    this.warningCount = warningCount;
    job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelFile(String testCase, Map<String, String> params, int warningCount) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.params = params;
    this.warningCount = warningCount;
    job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    chunkModule.setParallel(true);
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialMemory(String testCase, Map<String, String> params, int warningCount) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.params = params;
    this.warningCount = warningCount;
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void parallelMemory(String testCase, Map<String, String> params, int warningCount) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    this.params = params;
    this.warningCount = warningCount;
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
  public void test() {
    initStore(job.getStore());
    super.test();
    final List<Message> warnings = logger.getMessages().stream().filter(m -> m.level == WARN).toList();
    warnings.forEach(m -> System.err.println(m.level + ": " + m.message));
    assertEquals(warningCount, warnings.size());
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new ChunkModule();
  }
}
