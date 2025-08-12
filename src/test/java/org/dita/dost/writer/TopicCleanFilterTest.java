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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class TopicCleanFilterTest {

  private static TopicCleanFilter filter;

  @BeforeAll
  public static void setUpAll() {
    filter = new TopicCleanFilter();
  }

  public static Stream<Arguments> processingInstructionInputs() {
    return Stream.of(
      Arguments.of("path2project", "./", "", ""),
      Arguments.of("path2project", "../", "", ".." + File.separator),
      Arguments.of("path2project", "./", "../", ""),
      Arguments.of("path2project", "../", "../", ".." + File.separator),
      Arguments.of("path2project", "../", null, ".." + File.separator),
      Arguments.of("path2project-uri", "./", "", "./"),
      Arguments.of("path2project-uri", "../", "", "../"),
      Arguments.of("path2project-uri", "./", "../", "./"),
      Arguments.of("path2project-uri", "../", "../", "../"),
      Arguments.of("path2project-uri", "../", null, "../"),
      Arguments.of("path2rootmap-uri", "./", "", "./"),
      Arguments.of("path2rootmap-uri", "../", "", "./"),
      Arguments.of("path2rootmap-uri", "./", "../", "../"),
      Arguments.of("path2rootmap-uri", "../", "../", "../"),
      Arguments.of("path2rootmap-uri", "../", null, "data"),
      Arguments.of("other target", "passes", "through", "data")
    );
  }

  @ParameterizedTest(name = "{0}, src={1}, input={2}")
  @MethodSource("processingInstructionInputs")
  void processingInstruction(String target, String pathToRootDir, String pathToMapDir, String exp) throws SAXException {
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String expTarget, String data) {
          assertEquals(target, expTarget);
          assertEquals(
            Objects.requireNonNullElse(exp, ""),
            data,
            target + " \"" + pathToMapDir + "\" \"" + pathToRootDir + "\""
          );
        }
      }
    );

    filter.pathToMapDir = pathToMapDir;
    filter.pathToRootDir = pathToRootDir;
    filter.processingInstruction(target, "data");
  }

  @Nested
  class pathToProjectDir {

    private static File tempDir;
    private static Job job;

    @BeforeAll
    public static void setUpPathToProjectDir() throws IOException {
      tempDir = TestUtils.createTempDir(TopicCleanFilterTest.class);
      setUpTestFiles();
      job = new Job(tempDir, new CacheStore(tempDir, new XMLUtils()));
      filter.setJob(job);
    }

    private static void setUpTestFiles() throws IOException {
      String[] testedFilePaths = {
        "topic.dita",
        "dir/topic.dita",
        "dir/sub/topic.dita",
        "root/dir/sub/topic.dita",
        "root/dir/topic.dita",
        "root/topic.dita",
        "root.ditamap",
        "maps/root.ditamap",
        "root/root.ditamap",
      };
      for (String filePath : testedFilePaths) {
        Path path = Paths.get(tempDir.getPath(), filePath);
        Files.createDirectories(path.getParent());
        Files.createFile(path);
      }
    }

    @AfterEach
    void cleanUp() {
      job.getFileInfo().forEach(job::remove);
    }

    @AfterAll
    static void cleanUpSuite() throws IOException {
      TestUtils.forceDelete(tempDir);
    }

    public static Stream<Arguments> projectDirInputs() {
      return Stream.of(
        Arguments.of(NOT_GENERATEOUTTER, "topic.dita", "root.ditamap", "./", ""),
        Arguments.of(NOT_GENERATEOUTTER, "dir/topic.dita", "root.ditamap", "../", "../"),
        Arguments.of(NOT_GENERATEOUTTER, "dir/sub/topic.dita", "root.ditamap", "../../", "../../"),
        Arguments.of(NOT_GENERATEOUTTER, "dir/sub/topic.dita", "maps/root.ditamap", "../../maps/", "../../maps/"),
        Arguments.of(NOT_GENERATEOUTTER, "root/dir/sub/topic.dita", "root/ditamap", "../../", "../../"),
        Arguments.of(NOT_GENERATEOUTTER, "root/dir/topic.dita", "root/ditamap", "../", "../"),
        Arguments.of(OLDSOLUTION, "topic.dita", "root.ditamap", "./", ""),
        Arguments.of(OLDSOLUTION, "dir/topic.dita", "root.ditamap", "../", "../"),
        Arguments.of(OLDSOLUTION, "dir/sub/topic.dita", "root.ditamap", "../../", "../../"),
        Arguments.of(OLDSOLUTION, "dir/sub/topic.dita", "maps/root.ditamap", "../../", "../../maps/"),
        Arguments.of(OLDSOLUTION, "root/topic.dita", "root/ditamap", "./", ""),
        Arguments.of(OLDSOLUTION, "root/dir/sub/topic.dita", "root/ditamap", "../../", "../../"),
        Arguments.of(OLDSOLUTION, "root/dir/topic.dita", "root/ditamap", "../", "../")
      );
    }

    @ParameterizedTest(name = "{0} input={1} src={2}")
    @MethodSource("projectDirInputs")
    void calculatePathToProjectDirs(
      Job.Generate generate,
      String src,
      String input,
      String expPathToRootDir,
      String expPathToMapDir
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

      filter.startDocument();

      assertAll(
        () -> assertEquals(expPathToRootDir, filter.pathToRootDir, "pathToRootDir"),
        () -> assertEquals(expPathToMapDir, filter.pathToMapDir, "pathToMapDir")
      );
    }
  }
}
