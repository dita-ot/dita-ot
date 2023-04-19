/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.INPUT_DIR;
import static org.dita.dost.util.Constants.INPUT_DIR_URI;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public final class JobTest {

  private static final File resourceDir = TestUtils.getResourceDir(JobTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static File tempDir;
  private static Job job;

  @BeforeAll
  public static void setUp() throws IOException {
    tempDir = TestUtils.createTempDir(JobTest.class);
    TestUtils.copy(srcDir, tempDir);
    job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
  }

  @Test
  public void testGetProperty() {
    assertEquals("/foo/bar", job.getProperty(INPUT_DIR));
    assertEquals("file:/foo/bar", job.getProperty(INPUT_DIR_URI));
  }

  @Test
  public void testSetProperty() {
    job.setProperty("foo", "bar");
    assertEquals("bar", job.getProperty("foo"));
  }

  @Test
  public void testGetFileInfo() throws URISyntaxException {
    final URI relative = new URI("foo/bar.dita");
    final URI absolute = tempDir.toURI().resolve(relative);
    final Job.FileInfo fi = new Job.FileInfo.Builder().uri(relative).build();
    job.add(fi);
    assertEquals(fi, job.getFileInfo(relative));
    assertEquals(fi, job.getFileInfo(absolute));
    assertNull(job.getFileInfo((URI) null));
  }

  @Test
  public void testGetInputMap() {
    assertEquals(toURI("foo"), job.getInputMap());
  }

  @Test
  public void testGetValue() throws URISyntaxException {
    assertEquals(new URI("file:/foo/bar"), job.getInputDir());
  }

  @Test
  @Disabled
  public void write_performance_large() throws IOException {
    for (int i = 0; i < 60_000; i++) {
      job.add(
        Job.FileInfo
          .builder()
          .src(new File(tempDir, "topic_" + i + ".dita").toURI())
          .uri(new File("topic_" + i + ".dita").toURI())
          .result(new File(tempDir, "topic_" + i + ".html").toURI())
          .format("dita")
          .hasKeyref(true)
          .hasLink(true)
          .build()
      );
    }
    final long start = System.currentTimeMillis();
    job.write();
    final long end = System.currentTimeMillis();
    System.out.println(((end - start)) + " ms");
  }

  @AfterAll
  public static void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
