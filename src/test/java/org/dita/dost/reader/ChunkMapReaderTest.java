/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.TestUtils.parse;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.Constants.TOPIC_XREF;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Predicates;
import net.sf.saxon.s9api.streams.Steps;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChunkMapReaderTest {

  final File resourceDir = TestUtils.getResourceDir(ChunkMapReaderTest.class);
  final File srcDir = new File(resourceDir, "src");

  private File tempDir = null;
  private XMLUtils xmlUtils;
  private TestUtils.TestLogger logger;
  private ChunkMapReader mapReader;

  @BeforeEach
  public void setUp() throws Exception {
    tempDir = TestUtils.createTempDir(getClass());
    new File(tempDir, "maps").mkdirs();
    new File(tempDir, "topics").mkdirs();
    new File(tempDir, "maps" + File.separator + "topics").mkdirs();
    xmlUtils = new XMLUtils();
    logger = new TestUtils.TestLogger();
    mapReader = new ChunkMapReader();
    mapReader.setLogger(logger);
    mapReader.setXmlUtils(xmlUtils);
  }

  private URI prefixTemp(final String s) {
    return tempDir.toURI().resolve(s);
  }

  @AfterEach
  public void teardown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }

  @Test
  public void testReadSplitTopic() throws Exception {
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.setInputDir(srcDir.toURI());
    job.setInputMap(URI.create("chunkedMap.ditamap"));
    TestUtils.copy(new File(srcDir, "chunkedMap.ditamap"), new File(tempDir, "chunkedMap.ditamap"));
    job.add(
      new Job.FileInfo.Builder()
        .src(new File(srcDir, "chunkedMap.ditamap").toURI())
        .uri(toURI("chunkedMap.ditamap"))
        .isInput(true)
        .build()
    );
    String srcFile = "chunkedTopic.dita";
    final URI dst = tempDir.toURI().resolve(srcFile);
    TestUtils.copy(new File(srcDir, "chunkedTopic.dita"), new File(dst));
    job.add(new Job.FileInfo.Builder().src(new File(srcDir, srcFile).toURI()).uri(toURI(srcFile)).build());

    mapReader.setJob(job);

    mapReader.read(new File(tempDir, "chunkedMap.ditamap"));

    final XdmNode actWithFragment = parse(new File(tempDir, "subtopic2.dita"));
    assertTrue(
      actWithFragment
        .select(
          Steps.descendant(
            TOPIC_XREF.matcher().and(Predicates.attributeEq(ATTRIBUTE_NAME_HREF, "chunkedTopic.dita#subtopic3"))
          )
        )
        .exists()
    );

    final XdmNode actWithoutFragment = parse(new File(tempDir, "parentTopic.dita"));
    assertTrue(
      actWithoutFragment
        .select(
          Steps.descendant(
            TOPIC_XREF.matcher().and(Predicates.attributeEq(ATTRIBUTE_NAME_HREF, "chunkedTopic.dita#subtopic3"))
          )
        )
        .exists()
    );
  }

  @Test
  public void testReadRootChunkOverride() throws Exception {
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.setInputDir(srcDir.toURI());
    job.setInputMap(URI.create("mapNoChunk.ditamap"));

    mapReader.setRootChunkOverride("to-content");
    mapReader.setJob(job);

    TestUtils.copy(new File(srcDir, "mapNoChunk.ditamap"), new File(tempDir, "mapNoChunk.ditamap"));
    job.add(
      new Job.FileInfo.Builder()
        .src(new File(srcDir, "mapNoChunk.ditamap").toURI())
        .uri(toURI("mapNoChunk.ditamap"))
        .isInput(true)
        .build()
    );
    List<String> srcFiles = Arrays.asList("1.dita", "2.dita", "3.dita");
    for (final String srcFile : srcFiles) {
      final URI dst = tempDir.toURI().resolve(srcFile);
      TestUtils.copy(new File(srcDir, "topic.dita"), new File(dst));
      job.add(new Job.FileInfo.Builder().src(new File(srcDir, srcFile).toURI()).uri(toURI(srcFile)).build());
    }

    mapReader.read(new File(tempDir, "mapNoChunk.ditamap"));
    Set<URI> expected = new HashSet<>();
    expected.add(prefixTemp("1.dita"));
    expected.add(prefixTemp("2.dita"));
    expected.add(prefixTemp("3.dita"));
    assertEquals(expected, mapReader.getChunkTopicSet());
  }
}
