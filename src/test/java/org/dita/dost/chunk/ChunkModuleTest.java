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
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.util.*;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.module.AbstractModuleTest;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ChunkModuleTest extends AbstractModuleTest {

  private final int warningCount;

  @Parameters(name = "{0} {1}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
      new Object[][] {
        { "combine", Collections.emptyMap(), 2 },
        { "duplicate", Collections.emptyMap(), 0 },
        { "override", ImmutableMap.of("root-chunk-override", "combine"), 0 },
        { "dita", Collections.emptyMap(), 0 },
        { "combine-empty-ditabase", Collections.emptyMap(), 0 },
        { "link", Collections.emptyMap(), 0 },
        { "uplevels", Collections.emptyMap(), 0 },
        { "uplevels-dir", Collections.emptyMap(), 0 },
        { "uplevels-root", Collections.emptyMap(), 0 },
        { "uplevels-parallel", Collections.emptyMap(), 0 },
        { "format", Collections.emptyMap(), 0 },
        { "nested", Collections.emptyMap(), 0 },
        { "scope", Collections.emptyMap(), 0 },
        { "topicgroup", Collections.emptyMap(), 1 },
        { "topichead", Collections.emptyMap(), 1 },
        { "multiple", Collections.emptyMap(), 0 },
        { "map", Collections.emptyMap(), 0 },
        { "split", Collections.emptyMap(), 0 },
        { "split-dita", Collections.emptyMap(), 0 },
        { "split-hierarchy", Collections.emptyMap(), 0 },
        { "split-empty-ditabase", Collections.emptyMap(), 0 },
        { "split-map", Collections.emptyMap(), 0 },
        { "chunk-combine-within-split", Collections.emptyMap(), 0 },
        { "managing-links", Collections.emptyMap(), 0 },
        { "managing-links-duplicates", Collections.emptyMap(), 0 },
      }
    );
  }

  public ChunkModuleTest(final String testCase, final Map<String, String> params, final int warningCount) {
    super(testCase, params);
    this.warningCount = warningCount;
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
  protected AbstractPipelineModule getModule(final File tempDir) {
    return new ChunkModule();
  }
}
