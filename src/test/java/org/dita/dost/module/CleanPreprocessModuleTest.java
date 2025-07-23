/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static java.net.URI.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  static Stream<Arguments> tempUplevelsProvider() {
    return Stream.of(
      Arguments.of(List.of("1.dit"), "file:/foo/bar/", "file:/foo/bar/", ""),
      Arguments.of(List.of("1.dita"), "file:/foo/", "file:/foo/bar/", "../"),
      Arguments.of(List.of("1.dita", "bar/2.dita"), "file:/foo/", "file:/foo/bar/", "../"),
      Arguments.of(List.of("1.dita", "bar/2.dita", "bar/baz/3.dita"), "file:/foo/", "file:/foo/bar/baz/", "../../"),
      Arguments.of(List.of("bar/baz/1.dita", "bar/2.dita", "bar/baz/3.dita"), "file:/foo/", "file:/foo/bar/baz/", "../")
    );
  }

  @ParameterizedTest
  @MethodSource("tempUplevelsProvider")
  void correctTestUplevels(List<String> uriResults, String inputDir, URI baseDir, String exp) {
    job.setInputDir(create(inputDir));
    for (String result : uriResults) {
      job.add(new Builder().uri(create(inputDir + result)).result(create(inputDir + result)).build());
    }
    String act = module.getTempUplevels(baseDir);
    assertEquals(exp, act);
  }
}
