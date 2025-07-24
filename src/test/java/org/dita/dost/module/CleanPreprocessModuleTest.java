/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class CleanPreprocessModuleTest {

  private CleanPreprocessModule module;
  private XMLUtils xmlUtils;
  private Job job;

  @TempDir
  public File tempDir;

  @BeforeEach
  public void setUp() throws IOException {
    module = new CleanPreprocessModule();
    xmlUtils = new XMLUtils();
    job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    module.setJob(job);
  }

  @Test
  public void RewriteRule_WhenStylesheetNotFound_ShouldThrowException() throws Exception {
    assertThrows(
      RuntimeException.class,
      () -> {
        module.setJob(job);
        module.setXmlUtils(xmlUtils);
        final TestLogger logger = new TestUtils.TestLogger(false);
        module.setLogger(logger);
        final Map<String, String> input = new HashMap<>();
        input.put("result.rewrite-rule.xsl", "abc.xsl");

        module.execute(input);
      }
    );
  }
}
