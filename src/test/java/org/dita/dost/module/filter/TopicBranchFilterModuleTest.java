/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module.filter;

import static org.dita.dost.TestUtils.CachingLogger.Message.Level.ERROR;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;

public class TopicBranchFilterModuleTest extends TopicBranchFilterModule {

  private final File resourceDir = TestUtils.getResourceDir(TopicBranchFilterModuleTest.class);
  private final File expDir = new File(resourceDir, "exp");

  @TempDir
  private File tempDir;

  @BeforeEach
  public void setUp() throws Exception {
    TestUtils.copy(new File(resourceDir, "src"), tempDir);
  }

  @Test
  public void testDuplicateTopic() throws IOException {
    final TopicBranchFilterModule m = new TopicBranchFilterModule();
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.setInputDir(tempDir.toURI());
    job.addAll(getDuplicateTopicFileInfos());
    m.setJob(job);
    final CachingLogger logger = new CachingLogger();
    m.setLogger(logger);
    m.setXmlUtils(new XMLUtils());

    m.processMap(URI.create("test.ditamap"));

    assertXMLEqual(
      new InputSource(new File(expDir, "test.ditamap").toURI().toString()),
      new InputSource(new File(tempDir, "test.ditamap").toURI().toString())
    );
    assertXMLEqual(
      new InputSource(new File(expDir, "t1.xml").toURI().toString()),
      new InputSource(new File(tempDir, "t1.xml").toURI().toString())
    );
    assertXMLEqual(
      new InputSource(new File(expDir, "t1-1.xml").toURI().toString()),
      new InputSource(new File(tempDir, "t1-1.xml").toURI().toString())
    );
    assertEquals(getDuplicateTopicFileInfos(), new HashSet<>(job.getFileInfo()));
    assertEquals(0, logger.getMessages().stream().filter(msg -> msg.level == ERROR).count());
  }

  private Set<FileInfo> getDuplicateTopicFileInfos() {
    final Set<FileInfo> res = new HashSet<>();
    res.add(
      new FileInfo.Builder()
        .src(new File(tempDir, "test.ditamap").toURI())
        .result(new File(tempDir, "test.ditamap").toURI())
        .uri(URI.create("test.ditamap"))
        .format(ATTR_FORMAT_VALUE_DITAMAP)
        .build()
    );
    for (final String uri : Arrays.asList("test.ditaval", "test2.ditaval")) {
      res.add(
        new FileInfo.Builder()
          .src(new File(tempDir, uri).toURI())
          .result(new File(tempDir, uri).toURI())
          .uri(URI.create(uri))
          .format(ATTR_FORMAT_VALUE_DITAVAL)
          .build()
      );
    }
    for (final String uri : List.of("t1.xml")) {
      res.add(
        new FileInfo.Builder()
          .src(new File(tempDir, uri).toURI())
          .result(new File(tempDir, uri).toURI())
          .uri(URI.create(uri))
          .format(ATTR_FORMAT_VALUE_DITA)
          .build()
      );
    }
    res.add(
      new FileInfo.Builder()
        .src(new File(tempDir, "t1.xml").toURI())
        .result(new File(tempDir, "t1-1.xml").toURI())
        .uri(URI.create("t1-1.xml"))
        .format(ATTR_FORMAT_VALUE_DITA)
        .build()
    );
    return res;
  }
}
