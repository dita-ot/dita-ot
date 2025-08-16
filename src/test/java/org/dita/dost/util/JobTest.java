/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static java.net.URI.create;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.junit.jupiter.api.*;

public final class JobTest {

  private static final File resourceDir = TestUtils.getResourceDir(JobTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static File tempDir;
  private Job job;

  @BeforeAll
  public static void setUpAll() throws IOException {
    tempDir = TestUtils.createTempDir(JobTest.class);
    TestUtils.copy(srcDir, tempDir);
  }

  @BeforeEach
  public void setUp() throws IOException {
    job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
  }

  @AfterAll
  public static void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
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
  public void removePropertyExisting() {
    job.setProperty("foo", "bar");
    job.removeProperty("foo");
    assertNull(job.getProperty("foo"));
  }

  @Test
  public void removePropertyNonexistent() {
    job.removeProperty("foo");
    assertNull(job.getProperty("foo"));
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

  @Test
  public void getCommonBase_unix() {
    if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
      assertEquals(create("file:/foo/bar/"), job.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/bar/b")));
      assertEquals(create("file:/foo/"), job.getCommonBase(create("file:/foo/a"), create("file:/foo/bar/b")));
      assertEquals(create("file:/foo/"), job.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/b")));
      assertEquals(create("file:/foo/"), job.getCommonBase(create("file:/foo/bar/a"), create("file:/foo/baz/b")));
      assertEquals(create("file:/"), job.getCommonBase(create("file:/foo/a/b/c"), create("file:/bar/b/c/d")));
      assertEquals(null, job.getCommonBase(create("file:/foo/bar/a"), create("https://example.com/baz/b")));
    }
  }

  @Test
  public void getCommonBase_windows() {
    if (OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
      assertEquals(create("file:/F:/bar/"), job.getCommonBase(create("file:/F:/bar/a"), create("file:/F:/bar/b")));
      assertEquals(create("file:/F:/"), job.getCommonBase(create("file:/F:/a"), create("file:/F:/bar/b")));
      assertEquals(create("file:/F:/"), job.getCommonBase(create("file:/F:/bar/a"), create("file:/F:/b")));
      assertEquals(create("file:/F:/"), job.getCommonBase(create("file:/F:/bar/a"), create("file:/F:/baz/b")));
      assertEquals(null, job.getCommonBase(create("file:/C:/a"), create("file:/D:/b")));
      assertEquals(null, job.getCommonBase(create("file:/f:/bar/a"), create("https://example.com/baz/b")));
    }
  }

  @Test
  public void getResultBaseDir() {
    job.setInputDir(URI.create("file:/foo/bar/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create("file:/foo/bar/topics/topic.dita"))
        .build()
    );
    job.add(new Job.FileInfo.Builder().uri(create("topics/null.dita")).build());
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/task.dita"))
        .result(create("file:/foo/bar/topics/task.dita"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("common/topic.dita"))
        .result(create("file:/foo/bar/common/topic.dita"))
        .build()
    );

    assertEquals(create("file:/foo/bar/"), job.getResultBaseDir());
  }

  @Test
  public void getResultBaseDirUplevels() {
    job.setInputDir(URI.create("file:/foo/bar/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/common/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create("file:/foo/bar/topics/topic.dita"))
        .build()
    );
    job.add(new Job.FileInfo.Builder().uri(create("topics/null.dita")).build());
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/task.dita"))
        .result(create("file:/foo/bar/topics/task.dita"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("common/topic.dita"))
        .result(create("file:/foo/bar/common/topic.dita"))
        .build()
    );

    assertEquals(create("file:/foo/bar/"), job.getResultBaseDir());
  }

  @Test
  public void getResultBaseDirNormalFirstTime() {
    job.setInputDir(URI.create("file:/foo/bar/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create("file:/foo/bar/topics/topic.dita"))
        .build()
    );
    job.add(new Job.FileInfo.Builder().uri(create("topics/null.dita")).build());
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/task.dita"))
        .result(create("file:/foo/bar/topics/task.dita"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("common/topic.dita"))
        .result(create("file:/foo/bar/common/topic.dita"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("common/topic.dita"))
        .result(create("file:/foo/common/topic.dita"))
        .isResourceOnly(true)
        .build()
    );

    URI exp = create("file:/foo/bar/");
    assertEquals(exp, job.getResultBaseDirNormal());
    assertEquals(exp, create(job.getProperty(Job.FILE_SET_BASE_DIR_NORMAL)));
  }

  @Test
  public void getResultBaseDirNormalSecondTime() {
    job.setProperty(Job.FILE_SET_BASE_DIR_NORMAL, "someBaseDir");
    assertEquals(create("someBaseDir"), job.getResultBaseDirNormal());
  }

  @Test
  public void getBaseDirExternal() {
    job.setInputDir(URI.create("file:/foo/bar/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create("https://example.com/topics/bar/topics/topic.dita"))
        .build()
    );

    assertEquals(create("file:/foo/bar/"), job.getResultBaseDir());
  }

  @Test
  public void getBaseDirSubdir() {
    job.setInputDir(URI.create("file:/foo/bar/maps/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/maps/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create("file:/foo/bar/topics/topic.dita"))
        .build()
    );

    assertEquals(create("file:/foo/bar/"), job.getResultBaseDir());
  }

  @Test
  public void getResultBaseDirSiblingDir() {
    job.setInputDir(URI.create("file:/foo/bar/maps/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/maps/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create("file:/foo/bar/topics/topic.dita"))
        .build()
    );

    assertEquals(create("file:/foo/bar/"), job.getResultBaseDir());
  }

  @Test
  public void getBaseDirSupdir() {
    job.setInputDir(URI.create("file:/foo/bar/maps/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/foo/bar/maps/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder().uri(create("topics/topic.dita")).result(create("file:/foo/bar/topic.dita")).build()
    );

    assertEquals(create("file:/foo/bar/"), job.getResultBaseDir());
  }

  @Test
  public void getBaseDirResourceOnly() {
    job.getFileInfo().forEach(job::remove);

    job.setInputDir(URI.create("file:/main/maps/"));
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("main/maps/map.ditamap"))
        .isInput(true)
        .result(create("file:/main/maps/map.ditamap"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("main/topics/topic.dita"))
        .result(create("file:/main/topics/topic.dita"))
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("reuse/reuse.dita"))
        .result(create("file:/reuse/reuse.dita"))
        .isResourceOnly(true)
        .build()
    );

    assertEquals(create("file:/main/"), job.getResultBaseDir());
  }
}
