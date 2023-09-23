/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger.Message.Level;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.Store;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MapReaderModuleTest {

  File resourceDir = TestUtils.getResourceDir(getClass());
  File srcDir = new File(resourceDir, "src");
  File expDir = new File(resourceDir, "exp");

  private MapReaderModule reader;

  @TempDir
  private File baseTempDir;

  @BeforeEach
  public void setUp() {
    reader = new MapReaderModule();
    reader.setLogger(new TestUtils.TestLogger());
  }

  public static Stream<Arguments> data() {
    return Stream
      .of(
        DefaultTempFileScheme.class,
        FlattenTempFileScheme.class,
        FullPathTempFileScheme.class,
        HashTempFileScheme.class
      )
      .flatMap(cls ->
        Stream.of(
          Arguments.of(cls, true, cls.getSimpleName() + " in-memory"),
          Arguments.of(cls, false, cls.getSimpleName() + " disk")
        )
      );
  }

  @ParameterizedTest(name = "{2}")
  @MethodSource("data")
  public void execute(Class<? extends TempFileNameScheme> cls, boolean inMemory, String name)
    throws DITAOTException, IOException {
    final File tempDir = new File(baseTempDir, "temp");
    final File outDir = new File(baseTempDir, "out");
    final Map<String, String> input = new HashMap<>();
    input.put("inputmap", new File(srcDir, "root.ditamap").getAbsolutePath());
    input.put("validate", Boolean.toString(true));
    input.put("generatecopyouter", Integer.toString(1));
    input.put("outercontrol", "fail");
    input.put("outputdir", outDir.getAbsolutePath());
    final XMLUtils xmlUtils = new XMLUtils();
    final Store store = inMemory ? new StreamStore(tempDir, xmlUtils) : new CacheStore(tempDir, xmlUtils);
    final Job job = new Job(tempDir, store);
    job.setProperty("temp-file-name-scheme", cls.getCanonicalName());
    final TestUtils.CachingLogger logger = new TestUtils.CachingLogger();
    reader.setLogger(logger);
    reader.setJob(job);

    reader.execute(input);

    assertEquals(5, job.getFileInfo().size());
    for (Job.FileInfo fileInfo : job.getFileInfo()) {
      assertEquals(
        Objects.equals(fileInfo.format, "ditamap"),
        job.getStore().exists(tempDir.toURI().resolve(fileInfo.uri))
      );
      final URI src = srcDir.toURI().relativize(fileInfo.src);
      switch (src.toString()) {
        case "root.ditamap":
        case "submap.ditamap":
          assertEquals("ditamap", fileInfo.format);
          break;
        case "topic.dita":
        case "subtopic.dita":
          assertEquals(null, fileInfo.format);
          break;
        case "ext.pdf":
          assertEquals("pdf", fileInfo.format);
          break;
        default:
          throw new RuntimeException("Unmapped " + fileInfo.uri);
      }
    }
    assertFalse(logger.getMessages().stream().anyMatch(m -> m.level == Level.WARN));
    assertFalse(logger.getMessages().stream().anyMatch(m -> m.level == Level.ERROR));
  }

  @Test
  public void categorizeReferenceFileTopic() throws Exception {
    reader.categorizeReferenceFile(new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.dita")));
    assertEquals(0, reader.waitList.size());
  }

  @Test
  public void categorizeReferenceFileDitamap() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.ditamap"), ATTR_FORMAT_VALUE_DITAMAP)
    );
    assertEquals(1, reader.waitList.size());
  }

  @Test
  public void categorizeReferenceFileDitaval() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.ditaval"), ATTR_FORMAT_VALUE_DITAVAL)
    );
    assertEquals(1, reader.formatSet.size());
  }

  @Test
  public void categorizeReferenceFileImage() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.jpg"), ATTR_FORMAT_VALUE_IMAGE)
    );
    assertEquals(1, reader.formatSet.size());
  }
}
