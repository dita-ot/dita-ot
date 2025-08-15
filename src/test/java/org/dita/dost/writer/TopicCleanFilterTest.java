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
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class TopicCleanFilterTest {

  private static File tempDir;
  private static TopicCleanFilter filter;
  private static Job job;

  @BeforeAll
  public static void setUpAll() throws IOException {
    tempDir = TestUtils.createTempDir(TopicCleanFilterTest.class);
    job = new Job(tempDir, new CacheStore(tempDir, new XMLUtils()));
    filter = new TopicCleanFilter();
    filter.setJob(job);
  }

  @AfterEach
  void tearDown() {
    job.getFileInfo().forEach(job::remove);
  }

  static Stream<Arguments> scenarios() {
    String table =
      """
      NG,  topic.dita,          root.ditamap,       ,            ./,           ./
      NG,  dir/topic.dita,      root.ditamap,       ../,         ../,          ../
      NG,  dir/sub/topic.dita,  root.ditamap,       ../../,      ../../,       ../../
      NG,  topic.dita,          maps/root.ditamap,  maps/,       maps/,        maps/
      NG,  dir/topic.dita,      maps/root.ditamap,  ../maps/,    ../maps/,     ../maps/
      NG,  dir/sub/topic.dita,  maps/root.ditamap,  ../../maps/, ../../maps/,  ../../maps/
      OLD, topic.dita,          root.ditamap,       ,            ./,           ./
      OLD, dir/topic.dita,      root.ditamap,       ../,         ../,          ../
      OLD, dir/sub/topic.dita,  root.ditamap,       ../../,      ../../,       ../../
      OLD, topic.dita,          maps/root.ditamap,  ,            ./,           maps/
      OLD, dir/topic.dita,      maps/root.ditamap,  ../,         ../,          ../maps/
      OLD, dir/sub/topic.dita,  maps/root.ditamap,  ../../,      ../../,       ../../maps/
      """;

    Map<String, Job.Generate> GEN = Map.of("NG", NOT_GENERATEOUTTER, "OLD", OLDSOLUTION);
    return table
      .lines()
      .map(line -> Arrays.stream(line.split(",", -1)).map(String::trim).toArray(String[]::new))
      .map(c -> Arguments.of(GEN.get(c[0]), c[1], c[2], c[3].replace("/", File.separator), c[4], c[5]));
  }

  @ParameterizedTest(name = "{0}, {1}, {2}")
  @MethodSource("scenarios")
  void processingInstruction(
    Job.Generate generate,
    String src,
    String input,
    String path2project,
    String path2projectUri,
    String path2rootmapUri
  ) throws SAXException {
    job.setGeneratecopyouter(generate);
    Job.FileInfo mapFileInfo = Job.FileInfo
      .builder()
      .src(URI.create("src:///Volume/src/").resolve(input))
      .format(ATTR_FORMAT_VALUE_DITAMAP)
      .isInput(true)
      .uri(URI.create(input))
      .result(tempDir.toURI().resolve(input))
      .build();
    Job.FileInfo srcFileInfo = Job.FileInfo
      .builder()
      .src(URI.create("src:///Volume/src/").resolve(src))
      .uri(URI.create(src))
      .result(tempDir.toURI().resolve(src))
      .build();
    job.add(mapFileInfo);
    job.add(srcFileInfo);
    filter.setFileInfo(srcFileInfo);
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String target, String data) {
          assertEquals(
            switch (target) {
              case "path2project" -> path2project;
              case "path2project-uri" -> path2projectUri;
              case "path2rootmap-uri" -> path2rootmapUri;
              default -> "";
            },
            data,
            target + " \"" + src + "\" \"" + input + "\""
          );
        }
      }
    );

    filter.startDocument();
    assertAll(
      () -> filter.processingInstruction("path2project", "target"),
      () -> filter.processingInstruction("path2project-uri", "target"),
      () -> filter.processingInstruction("path2rootmap-uri", "target")
    );
  }

  @Test
  void processingInstruction_other() throws SAXException {
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String target, String data) {
          assertEquals("target", target);
          assertEquals("data", data);
        }
      }
    );

    filter.processingInstruction("target", "data");
  }
}
