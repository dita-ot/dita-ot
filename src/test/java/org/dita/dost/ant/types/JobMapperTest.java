/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 David Bertalan
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant.types;

import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.Generate.NOT_GENERATEOUTTER;
import static org.dita.dost.util.Job.Generate.OLDSOLUTION;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.*;

public class JobMapperTest {

  private static File tempDir;
  private static Job job;
  private JobSourceSet jobSourceSet;
  private String extension;
  private static Project project;
  private Mapper mapper;
  private JobMapper jobMapper;
  private FileNameMapper fileNameMapper;

  @BeforeAll
  public static void setUpAll() throws Exception {
    tempDir = TestUtils.createTempDir(JobMapperTest.class);
    project = new Project();
    project.setBasedir(tempDir.getAbsolutePath());
    project.setUserProperty(ANT_TEMP_DIR, tempDir.getAbsolutePath());
    project.setUserProperty(INPUT_DIR_URI, tempDir.getAbsolutePath());
  }

  @BeforeEach
  public void setUp() throws Exception {
    job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    project.addReference(ANT_REFERENCE_JOB, job);
    jobSourceSet =
      new JobSourceSet() {
        @Override
        Job getJob() {
          return job;
        }
      };
    mapper = new Mapper(project);
    jobMapper = new JobMapper();
    jobMapper.setProject(project);
    mapper.add(jobMapper);
    fileNameMapper = mapper.getImplementation();
  }

  @AfterAll
  public static void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }

  @Nested
  class to {

    @Test
    public void toEmptyThrowError() {
      jobMapper.setTo("");
      assertThat(jobMapper.getTo()).isEqualTo(".");
    }

    @Test
    public void toExtensionWithoutDot() {
      jobMapper.setTo("ext");
      assertThat(jobMapper.getTo()).isEqualTo(".ext");
    }

    @Test
    public void toExtensionWithDot() {
      jobMapper.setTo(".ext");
      assertThat(jobMapper.getTo()).isEqualTo(".ext");
    }

    @Test
    public void toFileName() {
      jobMapper.setTo("file.ext");
      assertThat(jobMapper.getTo()).isEqualTo("file.ext");
    }
  }

  @Nested
  class baseDir {

    @Test
    public void baseDirNormal() throws IOException {
      project.setUserProperty(INPUT_DIR_URI, tempDir.getAbsolutePath());
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(NOT_GENERATEOUTTER);
      String ditamap = "map.ditamap";
      addMap(ditamap);
      addFiles();

      final List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "map.ditamap",
        "common" + File.separator + "topic.dita",
        "topics" + File.separator + "task.dita",
        "topics" + File.separator + "topic.dita",
        "topics/null.dita",
        "images" + File.separator + "image.gif"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }

    @Test
    public void baseDirUplevels1() throws IOException {
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(NOT_GENERATEOUTTER);
      String ditamap = "common/map.ditamap";
      addMap(ditamap);
      addFiles();

      List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "map.ditamap",
        "topic.dita",
        "topics/null.dita",
        ".." + File.separator + "topics" + File.separator + "task.dita",
        ".." + File.separator + "topics" + File.separator + "topic.dita",
        ".." + File.separator + "images" + File.separator + "image.gif"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }

    @Test
    public void baseDirUplevels3() throws IOException {
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(OLDSOLUTION);
      String ditamap = "common/map.ditamap";
      addMap(ditamap);
      addFiles();

      final List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "common" + File.separator + "map.ditamap",
        "common" + File.separator + "topic.dita",
        "topics" + File.separator + "task.dita",
        "topics" + File.separator + "topic.dita",
        "images" + File.separator + "image.gif",
        "topics/null.dita"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }

    @Test
    public void baseDirUplevels3ResourceOnly() throws IOException {
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(OLDSOLUTION);
      String ditamap = "common/map.ditamap";
      addMap(ditamap);
      addFiles();
      job
        .getFileInfo()
        .stream()
        .filter(fileInfo -> !fileInfo.uri.toString().startsWith("common"))
        .forEach(fileInfo -> {
          job.remove(fileInfo);
          job.add(
            new Job.FileInfo.Builder()
              .uri(fileInfo.uri)
              .result(fileInfo.result)
              .isResourceOnly(true)
              .format("dita")
              .build()
          );
        });

      final List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "map.ditamap",
        "topic.dita",
        ".." + File.separator + "topics" + File.separator + "topic.dita",
        ".." + File.separator + "topics" + File.separator + "task.dita",
        ".." + File.separator + "images" + File.separator + "image.gif",
        "topics/null.dita"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }
  }

  @Nested
  class replace {

    @Test
    public void replaceExtension() throws IOException {
      project.setUserProperty(INPUT_DIR_URI, tempDir.getAbsolutePath());
      jobMapper.setTo("png");
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(NOT_GENERATEOUTTER);
      String ditamap = "map.ditamap";
      addMap(ditamap);
      addFiles();

      final List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "map.png",
        "common" + File.separator + "topic.png",
        "topics" + File.separator + "task.png",
        "topics" + File.separator + "topic.png",
        "topics/null.png",
        "images" + File.separator + "image.png"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }

    @Test
    public void replaceExtensionAndDot() throws IOException {
      project.setUserProperty(INPUT_DIR_URI, tempDir.getAbsolutePath());
      jobMapper.setTo(".png");
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(NOT_GENERATEOUTTER);
      String ditamap = "map.ditamap";
      addMap(ditamap);
      addFiles();

      final List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "map.png",
        "common" + File.separator + "topic.png",
        "topics" + File.separator + "task.png",
        "topics" + File.separator + "topic.png",
        "topics/null.png",
        "images" + File.separator + "image.png"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }

    @Test
    public void replaceFilename() throws IOException {
      project.setUserProperty(INPUT_DIR_URI, tempDir.getAbsolutePath());
      jobMapper.setTo("index.html");
      jobMapper.setFrom("map.ditamap");
      job.setInputDir(tempDir.toURI());
      job.setGeneratecopyouter(NOT_GENERATEOUTTER);
      String ditamap = "map.ditamap";
      addMap(ditamap);
      addFiles();

      final List<String> act = applyJobMapper();
      List<String> exp = Arrays.asList(
        "index.html",
        "common" + File.separator + "topic.dita",
        "topics" + File.separator + "task.dita",
        "topics" + File.separator + "topic.dita",
        "topics/null.dita",
        "images" + File.separator + "image.gif"
      );
      assertThat(act).containsExactlyInAnyOrderElementsOf(exp);
    }
  }

  private static void addMap(String ditamap) throws IOException {
    URI ditamapPath = tempDir.toURI().resolve(ditamap);
    File ditamapFile = new File(tempDir, ditamap);
    ditamapFile.mkdirs();
    ditamapFile.createNewFile();
    job.add(
      new Job.FileInfo.Builder()
        .uri(create(ditamap))
        .isInput(true)
        .src(ditamapPath)
        .result(ditamapPath)
        .format("ditamap")
        .build()
    );
  }

  private static void addFiles() {
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/topic.dita"))
        .result(create(tempDir.toURI() + "topics/topic.dita"))
        .format("dita")
        .build()
    );
    job.add(new Job.FileInfo.Builder().uri(create("topics/null.dita")).build());
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("topics/task.dita"))
        .result(create(tempDir.toURI() + "topics/task.dita"))
        .format("dita")
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("common/topic.dita"))
        .result(create(tempDir.toURI() + "common/topic.dita"))
        .format("dita")
        .build()
    );
    job.add(
      new Job.FileInfo.Builder()
        .uri(create("images/image.gif"))
        .result(create(tempDir.toURI() + "images/image.gif"))
        .format("image")
        .build()
    );
  }

  private List<String> applyJobMapper() {
    return jobSourceSet
      .stream()
      .map(elem -> fileNameMapper.mapFileName(elem.getName()))
      .flatMap(Arrays::stream)
      .toList();
  }
}
