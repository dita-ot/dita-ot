/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 David Bertalan
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant.types;

import static java.net.URI.create;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.Generate.NOT_GENERATEOUTTER;
import static org.dita.dost.util.Job.Generate.OLDSOLUTION;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileNameMapper;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
  public static void setUpClass() throws Exception {
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

  @Test
  public void testNormal() throws IOException {
    project.setUserProperty(INPUT_DIR_URI, tempDir.getAbsolutePath());
    job.setInputDir(tempDir.toURI());
    job.setGeneratecopyouter(NOT_GENERATEOUTTER);
    String ditamap = "map.ditamap";
    addMap(ditamap);
    addFiles();

    final String[] act = applyJobMapper();
    String[] exp = new String[] {
      "map.ditamap",
      "common" + File.separator + "topic.dita",
      "topics" + File.separator + "task.dita",
      "topics" + File.separator + "topic.dita",
      "topics/null.dita",
      "images" + File.separator + "image.gif",
    };
    Arrays.sort(act);
    Arrays.sort(exp);
    assertArrayEquals(exp, act, "Mismatch");
  }

  @Test
  public void testUplevels1() throws IOException {
    job.setInputDir(tempDir.toURI());
    job.setGeneratecopyouter(NOT_GENERATEOUTTER);
    String ditamap = "common/map.ditamap";
    addMap(ditamap);
    addFiles();

    String[] act = applyJobMapper();
    String[] exp = new String[] {
      "map.ditamap",
      "topic.dita",
      "topics/null.dita",
      ".." + File.separator + "topics" + File.separator + "task.dita",
      ".." + File.separator + "topics" + File.separator + "topic.dita",
      ".." + File.separator + "images" + File.separator + "image.gif",
    };
    Arrays.sort(act, Comparator.nullsLast(Comparator.naturalOrder()));
    Arrays.sort(exp, Comparator.nullsLast(Comparator.naturalOrder()));
    assertArrayEquals(exp, act, "Mismatch");
  }

  @Test
  public void testUplevels3() throws IOException {
    job.setInputDir(tempDir.toURI());
    job.setGeneratecopyouter(OLDSOLUTION);
    String ditamap = "common/map.ditamap";
    addMap(ditamap);
    addFiles();

    final String[] act = applyJobMapper();
    String[] exp = new String[] {
      "common" + File.separator + "map.ditamap",
      "common" + File.separator + "topic.dita",
      "topics" + File.separator + "task.dita",
      "topics" + File.separator + "topic.dita",
      "images" + File.separator + "image.gif",
      "topics/null.dita",
    };
    Arrays.sort(act);
    Arrays.sort(exp);
    assertArrayEquals(exp, act, "Mismatch");
  }

  private static void addMap(String ditamap) throws IOException {
    URI ditamapPath = tempDir.toURI().resolve(ditamap);
    File ditamapFile = new File(tempDir, ditamap);
    ditamapFile.getParentFile().mkdirs();
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

  private String[] applyJobMapper() {
    return jobSourceSet
      .stream()
      .map(elem -> fileNameMapper.mapFileName(elem.getName()))
      .flatMap(Arrays::stream)
      .toArray(String[]::new);
  }
}
