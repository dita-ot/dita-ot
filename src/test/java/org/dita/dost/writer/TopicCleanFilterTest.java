/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.dita.dost.util.Job.Generate.NOT_GENERATEOUTTER;
import static org.dita.dost.util.Job.Generate.OLDSOLUTION;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import java.util.stream.Stream;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class TopicCleanFilterTest {

  private final TopicCleanFilter filter;
  private final Job job;

  TopicCleanFilterTest() throws IOException {
    filter = new TopicCleanFilter();
    final File tempDir = new File("").getAbsoluteFile();
    job = new Job(tempDir, new CacheStore(tempDir, new XMLUtils()));
  }

  @AfterEach
  void cleanUp() {
    job.getFileInfo().forEach(job::remove);
  }

  public static Stream<Arguments> projectDirInputs() {
    return Stream.of(
      Arguments.of(NOT_GENERATEOUTTER, "topic.dita", "root.ditamap", "./", ""),
      Arguments.of(NOT_GENERATEOUTTER, "dir/topic.dita", "root.ditamap", "../", "../"),
      Arguments.of(NOT_GENERATEOUTTER, "dir/sub/topic.dita", "root.ditamap", "../../", "../../"),
      Arguments.of(NOT_GENERATEOUTTER, "dir/sub/topic.dita", "maps/root.ditamap", "../../maps/", "../../maps/"),
      Arguments.of(NOT_GENERATEOUTTER, "root/dir/sub/topic.dita", "root/root.ditamap", "../../", "../../"),
      Arguments.of(NOT_GENERATEOUTTER, "root/dir/topic.dita", "root/root.ditamap", "../", "../"),
      Arguments.of(OLDSOLUTION, "topic.dita", "root.ditamap", "./", ""),
      Arguments.of(OLDSOLUTION, "dir/topic.dita", "root.ditamap", "../", "../"),
      Arguments.of(OLDSOLUTION, "dir/sub/topic.dita", "root.ditamap", "../../", "../../"),
      Arguments.of(OLDSOLUTION, "dir/sub/topic.dita", "maps/root.ditamap", "../../", "../../maps/"),
      Arguments.of(OLDSOLUTION, "root/topic.dita", "root/root.ditamap", "../", ""),
      Arguments.of(OLDSOLUTION, "root/dir/sub/topic.dita", "root/root.ditamap", "../../../", "../../"),
      Arguments.of(OLDSOLUTION, "root/dir/topic.dita", "root/root.ditamap", "../../", "../")
    );
  }

  @ParameterizedTest(name = "{0} input={1} src={2}")
  @MethodSource("projectDirInputs")
  void pathToProjectDir(
    Job.Generate generate,
    String src,
    String input,
    String expPathToRootDir,
    String expPathToMapDir
  ) throws SAXException {
    job.setGeneratecopyouter(generate);
    job.add(
      Job.FileInfo
        .builder()
        .src(URI.create("src:///Volume/src/").resolve(input))
        .format(ATTR_FORMAT_VALUE_DITAMAP)
        .isInput(true)
        .uri(URI.create(input))
        .result(URI.create(input))
        .build()
    );
    filter.setJob(job);
    filter.setFileInfo(
      Job.FileInfo
        .builder()
        .src(URI.create("src:///Volume/src/").resolve(src))
        .uri(URI.create(src))
        .result(URI.create(src))
        .build()
    );

    filter.startDocument();

    assertAll(
      () -> assertEquals(expPathToRootDir, filter.pathToRootDir, "pathToRootDir"),
      () -> assertEquals(expPathToMapDir, filter.pathToMapDir, "pathToMapDir")
    );
  }

  public static Stream<Arguments> commonBase() {
    return Stream.of(
      Arguments.of("topic.dita", "root.ditamap", "resource.dita", ""),
      Arguments.of("topic.dita", "root/root.ditamap", "resource.dita", ""),
      Arguments.of("root/topic.dita", "root.ditamap", "resource.dita", ""),
      Arguments.of("topics/topic.dita", "maps/root.ditamap", "resource.dita", ""),
      Arguments.of("root/topic.dita", "root/root.ditamap", "root/resource.dita", "root/"),
      Arguments.of("root/topic.dita", "root/root.ditamap", "resource.dita", "root/")
    );
  }

  @ParameterizedTest(name = "{0} input={1} normal={2} resource={3}")
  @MethodSource("commonBase")
  void commonBase(String input, String normal, String resourceOnly, String exp) {
    job.add(
      Job.FileInfo
        .builder()
        .src(URI.create("src:///Volume/src/").resolve(input))
        .format(ATTR_FORMAT_VALUE_DITAMAP)
        .isInput(true)
        .uri(URI.create(input))
        .result(URI.create(input))
        .build()
    );
    job.add(
      Job.FileInfo
        .builder()
        .src(URI.create("src:///Volume/src/").resolve(resourceOnly))
        .isResourceOnly(true)
        .uri(URI.create(resourceOnly))
        .result(URI.create(resourceOnly))
        .build()
    );
    job.add(
      Job.FileInfo
        .builder()
        .src(URI.create("src:///Volume/src/").resolve(normal))
        .uri(URI.create(normal))
        .result(URI.create(normal))
        .build()
    );
    filter.setJob(job);

    assertEquals(exp, job.getBaseDirNormal().toString(), "commonBase");
  }

  public static Stream<Arguments> processingInstructionInputs() {
    return Stream.of(
      Arguments.of("path2project", "topic.dita", "root.ditamap", ""),
      Arguments.of("path2project", "dir/topic.dita", "root.ditamap", ".." + File.separator),
      Arguments.of("path2project", "dir/sub/topic.dita", "root.ditamap", ".." + File.separator + ".." + File.separator),
      Arguments.of("path2project-uri", "topic.dita", "root.ditamap", "./"),
      Arguments.of("path2project-uri", "dir/topic.dita", "root.ditamap", "../"),
      Arguments.of("path2project-uri", "dir/sub/topic.dita", "root.ditamap", "../../"),
      Arguments.of("path2rootmap-uri", "topic.dita", "root.ditamap", "./"),
      Arguments.of("path2rootmap-uri", "dir/topic.dita", "root.ditamap", "../"),
      Arguments.of("path2rootmap-uri", "dir/sub/topic.dita", "root.ditamap", "../../"),
      Arguments.of("path2rootmap-uri", "topic.dita", "maps/root.ditamap", "maps/"),
      Arguments.of("path2rootmap-uri", "dir/topic.dita", "maps/root.ditamap", "../maps/"),
      Arguments.of("path2rootmap-uri", "dir/sub/topic.dita", "maps/root.ditamap", "../../maps/")
    );
  }

  @ParameterizedTest(name = "{0}, src={1}, input={2}")
  @MethodSource("processingInstructionInputs")
  void processingInstruction(String name, String src, String input, String exp) throws SAXException {
    job.add(
      Job.FileInfo
        .builder()
        .src(URI.create("file:///Volume/src/").resolve(input))
        .format(ATTR_FORMAT_VALUE_DITAMAP)
        .isInput(true)
        .uri(URI.create(input))
        .result(URI.create(input))
        .build()
    );
    filter.setJob(job);
    filter.setFileInfo(
      Job.FileInfo
        .builder()
        .src(URI.create("file:///Volume/src/").resolve(src))
        .uri(URI.create(src))
        .result(URI.create(src))
        .build()
    );
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String target, String data) {
          assertEquals(name, target);
          assertEquals(Objects.requireNonNullElse(exp, ""), data, name + " \"" + src + "\" \"" + input + "\"");
        }
      }
    );

    filter.startDocument();
    filter.processingInstruction(name, "target");
  }

  @Test
  void processingInstruction_other() throws SAXException {
    filter.setJob(job);
    filter.setFileInfo(
      Job.FileInfo
        .builder()
        .src(URI.create("file:///Volume/src/topic.dita"))
        .uri(URI.create("topic.dita"))
        .result(URI.create("topic.dita"))
        .build()
    );
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String target, String data) {
          assertEquals("target", target);
          assertEquals("data", data);
        }
      }
    );

    filter.startDocument();
    filter.processingInstruction("target", "data");
  }
}
