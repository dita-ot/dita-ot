/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static java.net.URI.create;
import static org.dita.dost.util.Constants.OS_NAME;
import static org.dita.dost.util.Constants.OS_NAME_WINDOWS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
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
  public void getCommonBase_unix() {
    if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
      assertEquals(
        create("file:/foo/bar/"),
        module.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/bar/b"))
      );
      assertEquals(create("file:/foo/"), module.getCommonBase(create("file:/foo/a"), create("file:/foo/bar/b")));
      assertEquals(create("file:/foo/"), module.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/b")));
      assertEquals(create("file:/foo/"), module.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/baz/b")));
      assertEquals(create("file:/"), module.getCommonBase(create("file:/foo/a/b/c"), create("file:/bar/b/c/d")));
      assertEquals(null, module.getCommonBase(create("file:/foo/bar/a"), create("https://example.com/baz/b")));
    }
  }

  @Test
  public void getCommonBase_windows() {
    if (OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
      assertEquals(create("file:/F:/bar/"), module.getCommonBase(create("file:/F:/bar/a"), create("file:/F:/bar/b")));
      assertEquals(create("file:/F:/"), module.getCommonBase(create("file:/F:/a"), create("file:/F:/bar/b")));
      assertEquals(create("file:/F:/"), module.getCommonBase(create("file:/F:/bar/a"), create("file:/F:/b")));
      assertEquals(create("file:/F:/"), module.getCommonBase(create("file:/F:/bar/a"), create("file:/F:/baz/b")));
      assertEquals(null, module.getCommonBase(create("file:/C:/a"), create("file:/D:/b")));
      assertEquals(null, module.getCommonBase(create("file:/f:/bar/a"), create("https://example.com/baz/b")));
    }
  }

  @Test
  public void getBaseDir() throws Exception {
    job.setInputDir(URI.create("file:/foo/bar/"));
    job.add(new Builder().uri(create("map.ditamap")).isInput(true).result(create("file:/foo/bar/map.ditamap")).build());
    job.add(new Builder().uri(create("topics/topic.dita")).result(create("file:/foo/bar/topics/topic.dita")).build());
    job.add(new Builder().uri(create("topics/null.dita")).build());
    job.add(new Builder().uri(create("topics/task.dita")).result(create("file:/foo/bar/topics/task.dita")).build());
    job.add(new Builder().uri(create("common/topic.dita")).result(create("file:/foo/bar/common/topic.dita")).build());

    assertEquals(create("file:/foo/bar/"), module.getBaseDir());
  }

  @Test
  public void getBaseDirExternal() throws Exception {
    job.setInputDir(URI.create("file:/foo/bar/"));
    job.add(new Builder().uri(create("map.ditamap")).isInput(true).result(create("file:/foo/bar/map.ditamap")).build());
    job.add(
      new Builder()
        .uri(create("topics/topic.dita"))
        .result(create("https://example.com/topics/bar/topics/topic.dita"))
        .build()
    );

    assertEquals(create("file:/foo/bar/"), module.getBaseDir());
  }

  @Test
  public void getBaseDirSubdir() throws Exception {
    job.setInputDir(URI.create("file:/foo/bar/maps/"));
    job.add(
      new Builder()
        .uri(create("maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/maps/map.ditamap"))
        .build()
    );
    job.add(new Builder().uri(create("topics/topic.dita")).result(create("file:/foo/bar/topics/topic.dita")).build());

    assertEquals(create("file:/foo/bar/"), module.getBaseDir());
  }

  @Test
  public void getBaseDirSupdir() throws Exception {
    job.setInputDir(URI.create("file:/foo/bar/maps/"));
    job.add(
      new Builder()
        .uri(create("maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/maps/map.ditamap"))
        .build()
    );
    job.add(new Builder().uri(create("topics/topic.dita")).result(create("file:/foo/bar/topic.dita")).build());

    assertEquals(create("file:/foo/bar/"), module.getBaseDir());
  }

  @Test
  public void getBaseDirResourceOnly() throws Exception {
    job.setInputDir(URI.create("file:/main/maps/"));
    job.add(
      new Builder()
        .uri(create("main/maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/main/maps/map.ditamap"))
        .build()
    );
    job.add(new Builder().uri(create("main/topics/topic.dita")).result(create("file:/main/topics/topic.dita")).build());
    job.add(
      new Builder()
        .uri(create("reuse/reuse.dita"))
        .result(create("file:/reuse/reuse.dita"))
        .isResourceOnly(true)
        .build()
    );

    assertEquals(create("file:/"), module.getBaseDir());
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
