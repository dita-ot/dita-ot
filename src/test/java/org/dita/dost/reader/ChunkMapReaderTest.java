/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.TestUtils.CachingLogger.Message.Level.ERROR;
import static org.dita.dost.TestUtils.mapBuilder;
import static org.dita.dost.TestUtils.parse;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.dita.dost.util.Constants.TOPIC_XREF;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.streams.Predicates;
import net.sf.saxon.s9api.streams.Steps;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
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

  @Test
  public void testRead() throws Exception {
    final Job job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    job.setInputDir(srcDir.toURI());
    job.setInputMap(URI.create("maps/gen.ditamap"));

    mapReader.setJob(job);

    TestUtils.copy(new File(srcDir, "gen.ditamap"), new File(tempDir, "maps" + File.separator + "gen.ditamap"));
    job.add(
      new Job.FileInfo.Builder()
        .src(new File(srcDir, "maps" + File.separator + "gen.ditamap").toURI())
        .uri(toURI("maps/gen.ditamap"))
        .isInput(true)
        .build()
    );
    for (final String srcFile : getSrcFiles()) {
      final URI dst = tempDir.toURI().resolve(srcFile);
      TestUtils.copy(new File(srcDir, "topic.dita"), new File(dst));
      job.add(new Job.FileInfo.Builder().src(new File(srcDir, srcFile).toURI()).uri(toURI(srcFile)).build());
    }

    mapReader.read(new File(tempDir, "maps" + File.separator + "gen.ditamap"));

    assertEquals(getActChangeTable(), mapReader.getChangeTable());
    assertEquals(getActConflictTable(), mapReader.getConflicTable());
  }

  @Test
  public void testMissingSource() throws Exception {
    final Job job = createJob("missing.ditamap", "2.dita");

    mapReader.setJob(job);

    mapReader.read(new File(tempDir, "missing.ditamap"));

    assertEquals(Map.of(prefixTemp("2.dita"), prefixTemp("2.dita")), mapReader.getChangeTable());
    assertEquals(Collections.emptyMap(), mapReader.getConflicTable());

    assertNull(job.getFileInfo(new URI("missing.dita")));
  }

  @Test
  public void testChunkFullMap() throws Exception {
    final Job job = createJob("map.ditamap", "1.dita", "2.dita", "3.dita");

    final CachingLogger logger = new CachingLogger();
    mapReader.setLogger(logger);
    mapReader.setJob(job);

    mapReader.read(new File(tempDir, "map.ditamap"));

    assertEquals(
      mapBuilder()
        .put(prefixTemp("map.dita"), prefixTemp("map.dita"))
        .put(prefixTemp("1.dita"), prefixTemp("map.dita#topic_qft_qwn_hv"))
        .put(prefixTemp("1.dita#topic_qft_qwn_hv"), prefixTemp("map.dita#topic_qft_qwn_hv"))
        .put(prefixTemp("2.dita"), prefixTemp("map.dita#unique_0"))
        .put(prefixTemp("2.dita#topic_qft_qwn_hv"), prefixTemp("map.dita#unique_0"))
        .put(prefixTemp("3.dita"), prefixTemp("map.dita#unique_1"))
        .put(prefixTemp("3.dita#topic_qft_qwn_hv"), prefixTemp("map.dita#unique_1"))
        .build(),
      mapReader.getChangeTable()
    );

    assertEquals(Collections.emptyMap(), mapReader.getConflicTable());
    assertEquals(0, logger.getMessages().stream().filter(msg -> msg.level == ERROR).count());
  }

  @Test
  public void testExistingGeneratedFile() throws Exception {
    final Job job = createJob("conflict.ditamap", "2.dita", "Chunk0.dita");

    final CachingLogger logger = new CachingLogger();
    mapReader.setLogger(logger);
    mapReader.setJob(job);

    mapReader.read(new File(tempDir, "conflict.ditamap"));

    assertEquals(
      mapBuilder()
        .put(prefixTemp("Chunk0.dita"), prefixTemp("Chunk0.dita"))
        .put(prefixTemp("Chunk2.dita"), prefixTemp("Chunk2.dita"))
        .put(prefixTemp("Chunk1.dita"), prefixTemp("Chunk2.dita"))
        .put(prefixTemp("2.dita"), prefixTemp("Chunk2.dita#topic_qft_qwn_hv"))
        .put(prefixTemp("2.dita#topic_qft_qwn_hv"), prefixTemp("Chunk2.dita#topic_qft_qwn_hv"))
        .build(),
      mapReader.getChangeTable()
    );

    assertEquals(
      mapBuilder().put(prefixTemp("Chunk2.dita"), prefixTemp("Chunk1.dita")).build(),
      mapReader.getConflicTable()
    );
    assertEquals(0, logger.getMessages().stream().filter(msg -> msg.level == ERROR).count());
  }

  private Job createJob(final String map, final String... topics) throws IOException {
    final Job job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    job.setInputDir(srcDir.toURI());
    job.setInputMap(URI.create(map));

    TestUtils.copy(new File(srcDir, map), new File(tempDir, map));
    job.add(
      new Job.FileInfo.Builder()
        .src(new File(srcDir, map).toURI())
        .uri(toURI(map))
        .result(new File(srcDir, map).toURI())
        .isInput(true)
        .build()
    );
    for (final String srcFile : topics) {
      final URI dst = tempDir.toURI().resolve(srcFile);
      TestUtils.copy(new File(srcDir, "topic.dita"), new File(dst));
      job.add(
        new Job.FileInfo.Builder()
          .src(new File(srcDir, srcFile).toURI())
          .uri(toURI(srcFile))
          .result(new File(srcDir, srcFile).toURI())
          .build()
      );
    }

    return job;
  }

  private List<String> getSrcFiles() {
    return Arrays.asList(
      "maps/0.dita",
      "maps/2.dita",
      "maps/3.dita",
      "maps/5.dita",
      "maps/6.dita",
      "maps/8.dita",
      "maps/9.dita",
      "maps/11.dita",
      "maps/12.dita",
      "maps/13.dita",
      "maps/14.dita",
      "16.dita",
      "maps/17.dita",
      "19.dita",
      "maps/20.dita",
      "22.dita",
      "maps/23.dita",
      "25.dita",
      "maps/26.dita",
      "27.dita",
      "maps/28.dita",
      "maps/topics/30.dita",
      "maps/31.dita",
      "maps/topics/33.dita",
      "maps/34.dita",
      "maps/topics/36.dita",
      "maps/37.dita",
      "maps/topics/39.dita",
      "maps/40.dita",
      "maps/topics/41.dita",
      "maps/42.dita",
      "topics/44.dita",
      "maps/45.dita",
      "topics/47.dita",
      "maps/48.dita",
      "topics/50.dita",
      "maps/51.dita",
      "topics/53.dita",
      "maps/54.dita",
      "topics/55.dita",
      "56.dita",
      "maps/58.dita",
      "59.dita",
      "maps/61.dita",
      "62.dita",
      "maps/64.dita",
      "65.dita",
      "maps/67.dita",
      "68.dita",
      "maps/69.dita",
      "70.dita",
      "72.dita",
      "73.dita",
      "75.dita",
      "76.dita",
      "78.dita",
      "79.dita",
      "81.dita",
      "82.dita",
      "83.dita",
      "84.dita",
      "maps/topics/86.dita",
      "87.dita",
      "maps/topics/89.dita",
      "90.dita",
      "maps/topics/92.dita",
      "93.dita",
      "maps/topics/95.dita",
      "96.dita",
      "maps/topics/97.dita",
      "98.dita",
      "topics/100.dita",
      "101.dita",
      "topics/103.dita",
      "104.dita",
      "topics/106.dita",
      "107.dita",
      "topics/109.dita",
      "110.dita",
      "topics/111.dita",
      "maps/topics/112.dita",
      "maps/114.dita",
      "maps/topics/115.dita",
      "maps/117.dita",
      "maps/topics/118.dita",
      "maps/120.dita",
      "maps/topics/121.dita",
      "maps/123.dita",
      "maps/topics/124.dita",
      "maps/125.dita",
      "maps/topics/126.dita",
      "128.dita",
      "maps/topics/129.dita",
      "131.dita",
      "maps/topics/132.dita",
      "134.dita",
      "maps/topics/135.dita",
      "137.dita",
      "maps/topics/138.dita",
      "139.dita",
      "maps/topics/140.dita",
      "maps/topics/142.dita",
      "maps/topics/143.dita",
      "maps/topics/145.dita",
      "maps/topics/146.dita",
      "maps/topics/148.dita",
      "maps/topics/149.dita",
      "maps/topics/151.dita",
      "maps/topics/152.dita",
      "maps/topics/153.dita",
      "maps/topics/154.dita",
      "topics/156.dita",
      "maps/topics/157.dita",
      "topics/159.dita",
      "maps/topics/160.dita",
      "topics/162.dita",
      "maps/topics/163.dita",
      "topics/165.dita",
      "maps/topics/166.dita",
      "topics/167.dita",
      "topics/168.dita",
      "maps/170.dita",
      "topics/171.dita",
      "maps/173.dita",
      "topics/174.dita",
      "maps/176.dita",
      "topics/177.dita",
      "maps/179.dita",
      "topics/180.dita",
      "maps/181.dita",
      "topics/182.dita",
      "184.dita",
      "topics/185.dita",
      "187.dita",
      "topics/188.dita",
      "190.dita",
      "topics/191.dita",
      "193.dita",
      "topics/194.dita",
      "195.dita",
      "topics/196.dita",
      "maps/topics/198.dita",
      "topics/199.dita",
      "maps/topics/201.dita",
      "topics/202.dita",
      "maps/topics/204.dita",
      "topics/205.dita",
      "maps/topics/207.dita",
      "topics/208.dita",
      "maps/topics/209.dita",
      "topics/210.dita",
      "topics/212.dita",
      "topics/213.dita",
      "topics/215.dita",
      "topics/216.dita",
      "topics/218.dita",
      "topics/219.dita",
      "topics/221.dita",
      "topics/222.dita",
      "topics/223.dita"
    );
  }

  private Map<URI, URI> getActConflictTable() {
    return TestUtils
      .<URI, URI>mapBuilder()
      .put(prefixTemp("maps/Chunk82.dita"), prefixTemp("topics/194.dita"))
      .put(prefixTemp("maps/Chunk88.dita"), prefixTemp("topics/208.dita"))
      .put(prefixTemp("maps/Chunk34.dita"), prefixTemp("82.dita"))
      .put(prefixTemp("maps/Chunk70.dita"), prefixTemp("maps/topics/166.dita"))
      .put(prefixTemp("maps/Chunk64.dita"), prefixTemp("maps/topics/152.dita"))
      .put(prefixTemp("maps/Chunk10.dita"), prefixTemp("maps/26.dita"))
      .put(prefixTemp("maps/Chunk94.dita"), prefixTemp("topics/222.dita"))
      .put(prefixTemp("maps/Chunk46.dita"), prefixTemp("110.dita"))
      .put(prefixTemp("maps/Chunk40.dita"), prefixTemp("96.dita"))
      .put(prefixTemp("maps/Chunk16.dita"), prefixTemp("maps/40.dita"))
      .put(prefixTemp("maps/Chunk4.dita"), prefixTemp("maps/12.dita"))
      .put(prefixTemp("maps/Chunk76.dita"), prefixTemp("topics/180.dita"))
      .put(prefixTemp("maps/Chunk22.dita"), prefixTemp("maps/54.dita"))
      .put(prefixTemp("maps/Chunk58.dita"), prefixTemp("maps/topics/138.dita"))
      .put(prefixTemp("maps/Chunk52.dita"), prefixTemp("maps/topics/124.dita"))
      .put(prefixTemp("maps/Chunk28.dita"), prefixTemp("68.dita"))
      .build();
  }

  private Map<URI, URI> getActChangeTable() {
    return TestUtils
      .<URI, URI>mapBuilder()
      .put(prefixTemp("maps/1.dita"), prefixTemp("maps/1.dita"))
      .put(prefixTemp("maps/0.dita"), prefixTemp("maps/1.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/0.dita#topic_qft_qwn_hv"), prefixTemp("maps/1.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/2.dita"), prefixTemp("maps/1.dita#unique_0"))
      .put(prefixTemp("maps/2.dita#topic_qft_qwn_hv"), prefixTemp("maps/1.dita#unique_0"))
      .put(prefixTemp("4.dita"), prefixTemp("4.dita"))
      .put(prefixTemp("maps/3.dita"), prefixTemp("4.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/3.dita#topic_qft_qwn_hv"), prefixTemp("4.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/5.dita"), prefixTemp("4.dita#unique_1"))
      .put(prefixTemp("maps/5.dita#topic_qft_qwn_hv"), prefixTemp("4.dita#unique_1"))
      .put(prefixTemp("maps/topics/7.dita"), prefixTemp("maps/topics/7.dita"))
      .put(prefixTemp("maps/6.dita"), prefixTemp("maps/topics/7.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/6.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/7.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/8.dita"), prefixTemp("maps/topics/7.dita#unique_2"))
      .put(prefixTemp("maps/8.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/7.dita#unique_2"))
      .put(prefixTemp("topics/10.dita"), prefixTemp("topics/10.dita"))
      .put(prefixTemp("maps/9.dita"), prefixTemp("topics/10.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/9.dita#topic_qft_qwn_hv"), prefixTemp("topics/10.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/11.dita"), prefixTemp("topics/10.dita#unique_3"))
      .put(prefixTemp("maps/11.dita#topic_qft_qwn_hv"), prefixTemp("topics/10.dita#unique_3"))
      .put(prefixTemp("maps/Chunk4.dita"), prefixTemp("maps/Chunk4.dita"))
      .put(prefixTemp("maps/12.dita"), prefixTemp("maps/Chunk4.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/12.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk4.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/13.dita"), prefixTemp("maps/Chunk4.dita#unique_5"))
      .put(prefixTemp("maps/13.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk4.dita#unique_5"))
      .put(prefixTemp("maps/15.dita"), prefixTemp("maps/15.dita"))
      .put(prefixTemp("maps/14.dita"), prefixTemp("maps/15.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/14.dita#topic_qft_qwn_hv"), prefixTemp("maps/15.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("16.dita"), prefixTemp("maps/15.dita#unique_6"))
      .put(prefixTemp("16.dita#topic_qft_qwn_hv"), prefixTemp("maps/15.dita#unique_6"))
      .put(prefixTemp("18.dita"), prefixTemp("18.dita"))
      .put(prefixTemp("maps/17.dita"), prefixTemp("18.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/17.dita#topic_qft_qwn_hv"), prefixTemp("18.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("19.dita"), prefixTemp("18.dita#unique_7"))
      .put(prefixTemp("19.dita#topic_qft_qwn_hv"), prefixTemp("18.dita#unique_7"))
      .put(prefixTemp("maps/topics/21.dita"), prefixTemp("maps/topics/21.dita"))
      .put(prefixTemp("maps/20.dita"), prefixTemp("maps/topics/21.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/20.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/21.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("22.dita"), prefixTemp("maps/topics/21.dita#unique_8"))
      .put(prefixTemp("22.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/21.dita#unique_8"))
      .put(prefixTemp("topics/24.dita"), prefixTemp("topics/24.dita"))
      .put(prefixTemp("maps/23.dita"), prefixTemp("topics/24.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/23.dita#topic_qft_qwn_hv"), prefixTemp("topics/24.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("25.dita"), prefixTemp("topics/24.dita#unique_9"))
      .put(prefixTemp("25.dita#topic_qft_qwn_hv"), prefixTemp("topics/24.dita#unique_9"))
      .put(prefixTemp("maps/Chunk10.dita"), prefixTemp("maps/Chunk10.dita"))
      .put(prefixTemp("maps/26.dita"), prefixTemp("maps/Chunk10.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/26.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk10.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("27.dita"), prefixTemp("maps/Chunk10.dita#unique_11"))
      .put(prefixTemp("27.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk10.dita#unique_11"))
      .put(prefixTemp("maps/29.dita"), prefixTemp("maps/29.dita"))
      .put(prefixTemp("maps/28.dita"), prefixTemp("maps/29.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/28.dita#topic_qft_qwn_hv"), prefixTemp("maps/29.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/30.dita"), prefixTemp("maps/29.dita#unique_12"))
      .put(prefixTemp("maps/topics/30.dita#topic_qft_qwn_hv"), prefixTemp("maps/29.dita#unique_12"))
      .put(prefixTemp("32.dita"), prefixTemp("32.dita"))
      .put(prefixTemp("maps/31.dita"), prefixTemp("32.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/31.dita#topic_qft_qwn_hv"), prefixTemp("32.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/33.dita"), prefixTemp("32.dita#unique_13"))
      .put(prefixTemp("maps/topics/33.dita#topic_qft_qwn_hv"), prefixTemp("32.dita#unique_13"))
      .put(prefixTemp("maps/topics/35.dita"), prefixTemp("maps/topics/35.dita"))
      .put(prefixTemp("maps/34.dita"), prefixTemp("maps/topics/35.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/34.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/35.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/36.dita"), prefixTemp("maps/topics/35.dita#unique_14"))
      .put(prefixTemp("maps/topics/36.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/35.dita#unique_14"))
      .put(prefixTemp("topics/38.dita"), prefixTemp("topics/38.dita"))
      .put(prefixTemp("maps/37.dita"), prefixTemp("topics/38.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/37.dita#topic_qft_qwn_hv"), prefixTemp("topics/38.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/39.dita"), prefixTemp("topics/38.dita#unique_15"))
      .put(prefixTemp("maps/topics/39.dita#topic_qft_qwn_hv"), prefixTemp("topics/38.dita#unique_15"))
      .put(prefixTemp("maps/Chunk16.dita"), prefixTemp("maps/Chunk16.dita"))
      .put(prefixTemp("maps/40.dita"), prefixTemp("maps/Chunk16.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/40.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk16.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/41.dita"), prefixTemp("maps/Chunk16.dita#unique_17"))
      .put(prefixTemp("maps/topics/41.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk16.dita#unique_17"))
      .put(prefixTemp("maps/43.dita"), prefixTemp("maps/43.dita"))
      .put(prefixTemp("maps/42.dita"), prefixTemp("maps/43.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/42.dita#topic_qft_qwn_hv"), prefixTemp("maps/43.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/44.dita"), prefixTemp("maps/43.dita#unique_18"))
      .put(prefixTemp("topics/44.dita#topic_qft_qwn_hv"), prefixTemp("maps/43.dita#unique_18"))
      .put(prefixTemp("46.dita"), prefixTemp("46.dita"))
      .put(prefixTemp("maps/45.dita"), prefixTemp("46.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/45.dita#topic_qft_qwn_hv"), prefixTemp("46.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/47.dita"), prefixTemp("46.dita#unique_19"))
      .put(prefixTemp("topics/47.dita#topic_qft_qwn_hv"), prefixTemp("46.dita#unique_19"))
      .put(prefixTemp("maps/topics/49.dita"), prefixTemp("maps/topics/49.dita"))
      .put(prefixTemp("maps/48.dita"), prefixTemp("maps/topics/49.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/48.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/49.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/50.dita"), prefixTemp("maps/topics/49.dita#unique_20"))
      .put(prefixTemp("topics/50.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/49.dita#unique_20"))
      .put(prefixTemp("topics/52.dita"), prefixTemp("topics/52.dita"))
      .put(prefixTemp("maps/51.dita"), prefixTemp("topics/52.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/51.dita#topic_qft_qwn_hv"), prefixTemp("topics/52.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/53.dita"), prefixTemp("topics/52.dita#unique_21"))
      .put(prefixTemp("topics/53.dita#topic_qft_qwn_hv"), prefixTemp("topics/52.dita#unique_21"))
      .put(prefixTemp("maps/Chunk22.dita"), prefixTemp("maps/Chunk22.dita"))
      .put(prefixTemp("maps/54.dita"), prefixTemp("maps/Chunk22.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/54.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk22.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/55.dita"), prefixTemp("maps/Chunk22.dita#unique_23"))
      .put(prefixTemp("topics/55.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk22.dita#unique_23"))
      .put(prefixTemp("maps/57.dita"), prefixTemp("maps/57.dita"))
      .put(prefixTemp("56.dita"), prefixTemp("maps/57.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("56.dita#topic_qft_qwn_hv"), prefixTemp("maps/57.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/58.dita"), prefixTemp("maps/57.dita#unique_24"))
      .put(prefixTemp("maps/58.dita#topic_qft_qwn_hv"), prefixTemp("maps/57.dita#unique_24"))
      .put(prefixTemp("60.dita"), prefixTemp("60.dita"))
      .put(prefixTemp("59.dita"), prefixTemp("60.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("59.dita#topic_qft_qwn_hv"), prefixTemp("60.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/61.dita"), prefixTemp("60.dita#unique_25"))
      .put(prefixTemp("maps/61.dita#topic_qft_qwn_hv"), prefixTemp("60.dita#unique_25"))
      .put(prefixTemp("maps/topics/63.dita"), prefixTemp("maps/topics/63.dita"))
      .put(prefixTemp("62.dita"), prefixTemp("maps/topics/63.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("62.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/63.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/64.dita"), prefixTemp("maps/topics/63.dita#unique_26"))
      .put(prefixTemp("maps/64.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/63.dita#unique_26"))
      .put(prefixTemp("topics/66.dita"), prefixTemp("topics/66.dita"))
      .put(prefixTemp("65.dita"), prefixTemp("topics/66.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("65.dita#topic_qft_qwn_hv"), prefixTemp("topics/66.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/67.dita"), prefixTemp("topics/66.dita#unique_27"))
      .put(prefixTemp("maps/67.dita#topic_qft_qwn_hv"), prefixTemp("topics/66.dita#unique_27"))
      .put(prefixTemp("maps/Chunk28.dita"), prefixTemp("maps/Chunk28.dita"))
      .put(prefixTemp("68.dita"), prefixTemp("maps/Chunk28.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("68.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk28.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/69.dita"), prefixTemp("maps/Chunk28.dita#unique_29"))
      .put(prefixTemp("maps/69.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk28.dita#unique_29"))
      .put(prefixTemp("maps/71.dita"), prefixTemp("maps/71.dita"))
      .put(prefixTemp("70.dita"), prefixTemp("maps/71.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("70.dita#topic_qft_qwn_hv"), prefixTemp("maps/71.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("72.dita"), prefixTemp("maps/71.dita#unique_30"))
      .put(prefixTemp("72.dita#topic_qft_qwn_hv"), prefixTemp("maps/71.dita#unique_30"))
      .put(prefixTemp("74.dita"), prefixTemp("74.dita"))
      .put(prefixTemp("73.dita"), prefixTemp("74.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("73.dita#topic_qft_qwn_hv"), prefixTemp("74.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("75.dita"), prefixTemp("74.dita#unique_31"))
      .put(prefixTemp("75.dita#topic_qft_qwn_hv"), prefixTemp("74.dita#unique_31"))
      .put(prefixTemp("maps/topics/77.dita"), prefixTemp("maps/topics/77.dita"))
      .put(prefixTemp("76.dita"), prefixTemp("maps/topics/77.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("76.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/77.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("78.dita"), prefixTemp("maps/topics/77.dita#unique_32"))
      .put(prefixTemp("78.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/77.dita#unique_32"))
      .put(prefixTemp("topics/80.dita"), prefixTemp("topics/80.dita"))
      .put(prefixTemp("79.dita"), prefixTemp("topics/80.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("79.dita#topic_qft_qwn_hv"), prefixTemp("topics/80.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("81.dita"), prefixTemp("topics/80.dita#unique_33"))
      .put(prefixTemp("81.dita#topic_qft_qwn_hv"), prefixTemp("topics/80.dita#unique_33"))
      .put(prefixTemp("maps/Chunk34.dita"), prefixTemp("maps/Chunk34.dita"))
      .put(prefixTemp("82.dita"), prefixTemp("maps/Chunk34.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("82.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk34.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("83.dita"), prefixTemp("maps/Chunk34.dita#unique_35"))
      .put(prefixTemp("83.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk34.dita#unique_35"))
      .put(prefixTemp("maps/85.dita"), prefixTemp("maps/85.dita"))
      .put(prefixTemp("84.dita"), prefixTemp("maps/85.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("84.dita#topic_qft_qwn_hv"), prefixTemp("maps/85.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/86.dita"), prefixTemp("maps/85.dita#unique_36"))
      .put(prefixTemp("maps/topics/86.dita#topic_qft_qwn_hv"), prefixTemp("maps/85.dita#unique_36"))
      .put(prefixTemp("88.dita"), prefixTemp("88.dita"))
      .put(prefixTemp("87.dita"), prefixTemp("88.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("87.dita#topic_qft_qwn_hv"), prefixTemp("88.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/89.dita"), prefixTemp("88.dita#unique_37"))
      .put(prefixTemp("maps/topics/89.dita#topic_qft_qwn_hv"), prefixTemp("88.dita#unique_37"))
      .put(prefixTemp("maps/topics/91.dita"), prefixTemp("maps/topics/91.dita"))
      .put(prefixTemp("90.dita"), prefixTemp("maps/topics/91.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("90.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/91.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/92.dita"), prefixTemp("maps/topics/91.dita#unique_38"))
      .put(prefixTemp("maps/topics/92.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/91.dita#unique_38"))
      .put(prefixTemp("topics/94.dita"), prefixTemp("topics/94.dita"))
      .put(prefixTemp("93.dita"), prefixTemp("topics/94.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("93.dita#topic_qft_qwn_hv"), prefixTemp("topics/94.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/95.dita"), prefixTemp("topics/94.dita#unique_39"))
      .put(prefixTemp("maps/topics/95.dita#topic_qft_qwn_hv"), prefixTemp("topics/94.dita#unique_39"))
      .put(prefixTemp("maps/Chunk40.dita"), prefixTemp("maps/Chunk40.dita"))
      .put(prefixTemp("96.dita"), prefixTemp("maps/Chunk40.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("96.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk40.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/97.dita"), prefixTemp("maps/Chunk40.dita#unique_41"))
      .put(prefixTemp("maps/topics/97.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk40.dita#unique_41"))
      .put(prefixTemp("maps/99.dita"), prefixTemp("maps/99.dita"))
      .put(prefixTemp("98.dita"), prefixTemp("maps/99.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("98.dita#topic_qft_qwn_hv"), prefixTemp("maps/99.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/100.dita"), prefixTemp("maps/99.dita#unique_42"))
      .put(prefixTemp("topics/100.dita#topic_qft_qwn_hv"), prefixTemp("maps/99.dita#unique_42"))
      .put(prefixTemp("102.dita"), prefixTemp("102.dita"))
      .put(prefixTemp("101.dita"), prefixTemp("102.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("101.dita#topic_qft_qwn_hv"), prefixTemp("102.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/103.dita"), prefixTemp("102.dita#unique_43"))
      .put(prefixTemp("topics/103.dita#topic_qft_qwn_hv"), prefixTemp("102.dita#unique_43"))
      .put(prefixTemp("maps/topics/105.dita"), prefixTemp("maps/topics/105.dita"))
      .put(prefixTemp("104.dita"), prefixTemp("maps/topics/105.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("104.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/105.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/106.dita"), prefixTemp("maps/topics/105.dita#unique_44"))
      .put(prefixTemp("topics/106.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/105.dita#unique_44"))
      .put(prefixTemp("topics/108.dita"), prefixTemp("topics/108.dita"))
      .put(prefixTemp("107.dita"), prefixTemp("topics/108.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("107.dita#topic_qft_qwn_hv"), prefixTemp("topics/108.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/109.dita"), prefixTemp("topics/108.dita#unique_45"))
      .put(prefixTemp("topics/109.dita#topic_qft_qwn_hv"), prefixTemp("topics/108.dita#unique_45"))
      .put(prefixTemp("maps/Chunk46.dita"), prefixTemp("maps/Chunk46.dita"))
      .put(prefixTemp("110.dita"), prefixTemp("maps/Chunk46.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("110.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk46.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/111.dita"), prefixTemp("maps/Chunk46.dita#unique_47"))
      .put(prefixTemp("topics/111.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk46.dita#unique_47"))
      .put(prefixTemp("maps/113.dita"), prefixTemp("maps/113.dita"))
      .put(prefixTemp("maps/topics/112.dita"), prefixTemp("maps/113.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/112.dita#topic_qft_qwn_hv"), prefixTemp("maps/113.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/114.dita"), prefixTemp("maps/113.dita#unique_48"))
      .put(prefixTemp("maps/114.dita#topic_qft_qwn_hv"), prefixTemp("maps/113.dita#unique_48"))
      .put(prefixTemp("116.dita"), prefixTemp("116.dita"))
      .put(prefixTemp("maps/topics/115.dita"), prefixTemp("116.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/115.dita#topic_qft_qwn_hv"), prefixTemp("116.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/117.dita"), prefixTemp("116.dita#unique_49"))
      .put(prefixTemp("maps/117.dita#topic_qft_qwn_hv"), prefixTemp("116.dita#unique_49"))
      .put(prefixTemp("maps/topics/119.dita"), prefixTemp("maps/topics/119.dita"))
      .put(prefixTemp("maps/topics/118.dita"), prefixTemp("maps/topics/119.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/118.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/119.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/120.dita"), prefixTemp("maps/topics/119.dita#unique_50"))
      .put(prefixTemp("maps/120.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/119.dita#unique_50"))
      .put(prefixTemp("topics/122.dita"), prefixTemp("topics/122.dita"))
      .put(prefixTemp("maps/topics/121.dita"), prefixTemp("topics/122.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/121.dita#topic_qft_qwn_hv"), prefixTemp("topics/122.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/123.dita"), prefixTemp("topics/122.dita#unique_51"))
      .put(prefixTemp("maps/123.dita#topic_qft_qwn_hv"), prefixTemp("topics/122.dita#unique_51"))
      .put(prefixTemp("maps/Chunk52.dita"), prefixTemp("maps/Chunk52.dita"))
      .put(prefixTemp("maps/topics/124.dita"), prefixTemp("maps/Chunk52.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/124.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk52.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/125.dita"), prefixTemp("maps/Chunk52.dita#unique_53"))
      .put(prefixTemp("maps/125.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk52.dita#unique_53"))
      .put(prefixTemp("maps/127.dita"), prefixTemp("maps/127.dita"))
      .put(prefixTemp("maps/topics/126.dita"), prefixTemp("maps/127.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/126.dita#topic_qft_qwn_hv"), prefixTemp("maps/127.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("128.dita"), prefixTemp("maps/127.dita#unique_54"))
      .put(prefixTemp("128.dita#topic_qft_qwn_hv"), prefixTemp("maps/127.dita#unique_54"))
      .put(prefixTemp("130.dita"), prefixTemp("130.dita"))
      .put(prefixTemp("maps/topics/129.dita"), prefixTemp("130.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/129.dita#topic_qft_qwn_hv"), prefixTemp("130.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("131.dita"), prefixTemp("130.dita#unique_55"))
      .put(prefixTemp("131.dita#topic_qft_qwn_hv"), prefixTemp("130.dita#unique_55"))
      .put(prefixTemp("maps/topics/133.dita"), prefixTemp("maps/topics/133.dita"))
      .put(prefixTemp("maps/topics/132.dita"), prefixTemp("maps/topics/133.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/132.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/133.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("134.dita"), prefixTemp("maps/topics/133.dita#unique_56"))
      .put(prefixTemp("134.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/133.dita#unique_56"))
      .put(prefixTemp("topics/136.dita"), prefixTemp("topics/136.dita"))
      .put(prefixTemp("maps/topics/135.dita"), prefixTemp("topics/136.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/135.dita#topic_qft_qwn_hv"), prefixTemp("topics/136.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("137.dita"), prefixTemp("topics/136.dita#unique_57"))
      .put(prefixTemp("137.dita#topic_qft_qwn_hv"), prefixTemp("topics/136.dita#unique_57"))
      .put(prefixTemp("maps/Chunk58.dita"), prefixTemp("maps/Chunk58.dita"))
      .put(prefixTemp("maps/topics/138.dita"), prefixTemp("maps/Chunk58.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/138.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk58.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("139.dita"), prefixTemp("maps/Chunk58.dita#unique_59"))
      .put(prefixTemp("139.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk58.dita#unique_59"))
      .put(prefixTemp("maps/141.dita"), prefixTemp("maps/141.dita"))
      .put(prefixTemp("maps/topics/140.dita"), prefixTemp("maps/141.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/140.dita#topic_qft_qwn_hv"), prefixTemp("maps/141.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/142.dita"), prefixTemp("maps/141.dita#unique_60"))
      .put(prefixTemp("maps/topics/142.dita#topic_qft_qwn_hv"), prefixTemp("maps/141.dita#unique_60"))
      .put(prefixTemp("144.dita"), prefixTemp("144.dita"))
      .put(prefixTemp("maps/topics/143.dita"), prefixTemp("144.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/143.dita#topic_qft_qwn_hv"), prefixTemp("144.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/145.dita"), prefixTemp("144.dita#unique_61"))
      .put(prefixTemp("maps/topics/145.dita#topic_qft_qwn_hv"), prefixTemp("144.dita#unique_61"))
      .put(prefixTemp("maps/topics/147.dita"), prefixTemp("maps/topics/147.dita"))
      .put(prefixTemp("maps/topics/146.dita"), prefixTemp("maps/topics/147.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/146.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/147.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/148.dita"), prefixTemp("maps/topics/147.dita#unique_62"))
      .put(prefixTemp("maps/topics/148.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/147.dita#unique_62"))
      .put(prefixTemp("topics/150.dita"), prefixTemp("topics/150.dita"))
      .put(prefixTemp("maps/topics/149.dita"), prefixTemp("topics/150.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/149.dita#topic_qft_qwn_hv"), prefixTemp("topics/150.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/151.dita"), prefixTemp("topics/150.dita#unique_63"))
      .put(prefixTemp("maps/topics/151.dita#topic_qft_qwn_hv"), prefixTemp("topics/150.dita#unique_63"))
      .put(prefixTemp("maps/Chunk64.dita"), prefixTemp("maps/Chunk64.dita"))
      .put(prefixTemp("maps/topics/152.dita"), prefixTemp("maps/Chunk64.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/152.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk64.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/153.dita"), prefixTemp("maps/Chunk64.dita#unique_65"))
      .put(prefixTemp("maps/topics/153.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk64.dita#unique_65"))
      .put(prefixTemp("maps/155.dita"), prefixTemp("maps/155.dita"))
      .put(prefixTemp("maps/topics/154.dita"), prefixTemp("maps/155.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/154.dita#topic_qft_qwn_hv"), prefixTemp("maps/155.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/156.dita"), prefixTemp("maps/155.dita#unique_66"))
      .put(prefixTemp("topics/156.dita#topic_qft_qwn_hv"), prefixTemp("maps/155.dita#unique_66"))
      .put(prefixTemp("158.dita"), prefixTemp("158.dita"))
      .put(prefixTemp("maps/topics/157.dita"), prefixTemp("158.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/157.dita#topic_qft_qwn_hv"), prefixTemp("158.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/159.dita"), prefixTemp("158.dita#unique_67"))
      .put(prefixTemp("topics/159.dita#topic_qft_qwn_hv"), prefixTemp("158.dita#unique_67"))
      .put(prefixTemp("maps/topics/161.dita"), prefixTemp("maps/topics/161.dita"))
      .put(prefixTemp("maps/topics/160.dita"), prefixTemp("maps/topics/161.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/160.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/161.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/162.dita"), prefixTemp("maps/topics/161.dita#unique_68"))
      .put(prefixTemp("topics/162.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/161.dita#unique_68"))
      .put(prefixTemp("topics/164.dita"), prefixTemp("topics/164.dita"))
      .put(prefixTemp("maps/topics/163.dita"), prefixTemp("topics/164.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/163.dita#topic_qft_qwn_hv"), prefixTemp("topics/164.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/165.dita"), prefixTemp("topics/164.dita#unique_69"))
      .put(prefixTemp("topics/165.dita#topic_qft_qwn_hv"), prefixTemp("topics/164.dita#unique_69"))
      .put(prefixTemp("maps/Chunk70.dita"), prefixTemp("maps/Chunk70.dita"))
      .put(prefixTemp("maps/topics/166.dita"), prefixTemp("maps/Chunk70.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/166.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk70.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/167.dita"), prefixTemp("maps/Chunk70.dita#unique_71"))
      .put(prefixTemp("topics/167.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk70.dita#unique_71"))
      .put(prefixTemp("maps/169.dita"), prefixTemp("maps/169.dita"))
      .put(prefixTemp("topics/168.dita"), prefixTemp("maps/169.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/168.dita#topic_qft_qwn_hv"), prefixTemp("maps/169.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/170.dita"), prefixTemp("maps/169.dita#unique_72"))
      .put(prefixTemp("maps/170.dita#topic_qft_qwn_hv"), prefixTemp("maps/169.dita#unique_72"))
      .put(prefixTemp("172.dita"), prefixTemp("172.dita"))
      .put(prefixTemp("topics/171.dita"), prefixTemp("172.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/171.dita#topic_qft_qwn_hv"), prefixTemp("172.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/173.dita"), prefixTemp("172.dita#unique_73"))
      .put(prefixTemp("maps/173.dita#topic_qft_qwn_hv"), prefixTemp("172.dita#unique_73"))
      .put(prefixTemp("maps/topics/175.dita"), prefixTemp("maps/topics/175.dita"))
      .put(prefixTemp("topics/174.dita"), prefixTemp("maps/topics/175.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/174.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/175.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/176.dita"), prefixTemp("maps/topics/175.dita#unique_74"))
      .put(prefixTemp("maps/176.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/175.dita#unique_74"))
      .put(prefixTemp("topics/178.dita"), prefixTemp("topics/178.dita"))
      .put(prefixTemp("topics/177.dita"), prefixTemp("topics/178.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/177.dita#topic_qft_qwn_hv"), prefixTemp("topics/178.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/179.dita"), prefixTemp("topics/178.dita#unique_75"))
      .put(prefixTemp("maps/179.dita#topic_qft_qwn_hv"), prefixTemp("topics/178.dita#unique_75"))
      .put(prefixTemp("maps/Chunk76.dita"), prefixTemp("maps/Chunk76.dita"))
      .put(prefixTemp("topics/180.dita"), prefixTemp("maps/Chunk76.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/180.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk76.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/181.dita"), prefixTemp("maps/Chunk76.dita#unique_77"))
      .put(prefixTemp("maps/181.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk76.dita#unique_77"))
      .put(prefixTemp("maps/183.dita"), prefixTemp("maps/183.dita"))
      .put(prefixTemp("topics/182.dita"), prefixTemp("maps/183.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/182.dita#topic_qft_qwn_hv"), prefixTemp("maps/183.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("184.dita"), prefixTemp("maps/183.dita#unique_78"))
      .put(prefixTemp("184.dita#topic_qft_qwn_hv"), prefixTemp("maps/183.dita#unique_78"))
      .put(prefixTemp("186.dita"), prefixTemp("186.dita"))
      .put(prefixTemp("topics/185.dita"), prefixTemp("186.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/185.dita#topic_qft_qwn_hv"), prefixTemp("186.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("187.dita"), prefixTemp("186.dita#unique_79"))
      .put(prefixTemp("187.dita#topic_qft_qwn_hv"), prefixTemp("186.dita#unique_79"))
      .put(prefixTemp("maps/topics/189.dita"), prefixTemp("maps/topics/189.dita"))
      .put(prefixTemp("topics/188.dita"), prefixTemp("maps/topics/189.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/188.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/189.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("190.dita"), prefixTemp("maps/topics/189.dita#unique_80"))
      .put(prefixTemp("190.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/189.dita#unique_80"))
      .put(prefixTemp("topics/192.dita"), prefixTemp("topics/192.dita"))
      .put(prefixTemp("topics/191.dita"), prefixTemp("topics/192.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/191.dita#topic_qft_qwn_hv"), prefixTemp("topics/192.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("193.dita"), prefixTemp("topics/192.dita#unique_81"))
      .put(prefixTemp("193.dita#topic_qft_qwn_hv"), prefixTemp("topics/192.dita#unique_81"))
      .put(prefixTemp("maps/Chunk82.dita"), prefixTemp("maps/Chunk82.dita"))
      .put(prefixTemp("topics/194.dita"), prefixTemp("maps/Chunk82.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/194.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk82.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("195.dita"), prefixTemp("maps/Chunk82.dita#unique_83"))
      .put(prefixTemp("195.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk82.dita#unique_83"))
      .put(prefixTemp("maps/197.dita"), prefixTemp("maps/197.dita"))
      .put(prefixTemp("topics/196.dita"), prefixTemp("maps/197.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/196.dita#topic_qft_qwn_hv"), prefixTemp("maps/197.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/198.dita"), prefixTemp("maps/197.dita#unique_84"))
      .put(prefixTemp("maps/topics/198.dita#topic_qft_qwn_hv"), prefixTemp("maps/197.dita#unique_84"))
      .put(prefixTemp("200.dita"), prefixTemp("200.dita"))
      .put(prefixTemp("topics/199.dita"), prefixTemp("200.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/199.dita#topic_qft_qwn_hv"), prefixTemp("200.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/201.dita"), prefixTemp("200.dita#unique_85"))
      .put(prefixTemp("maps/topics/201.dita#topic_qft_qwn_hv"), prefixTemp("200.dita#unique_85"))
      .put(prefixTemp("maps/topics/203.dita"), prefixTemp("maps/topics/203.dita"))
      .put(prefixTemp("topics/202.dita"), prefixTemp("maps/topics/203.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/202.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/203.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/204.dita"), prefixTemp("maps/topics/203.dita#unique_86"))
      .put(prefixTemp("maps/topics/204.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/203.dita#unique_86"))
      .put(prefixTemp("topics/206.dita"), prefixTemp("topics/206.dita"))
      .put(prefixTemp("topics/205.dita"), prefixTemp("topics/206.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/205.dita#topic_qft_qwn_hv"), prefixTemp("topics/206.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/207.dita"), prefixTemp("topics/206.dita#unique_87"))
      .put(prefixTemp("maps/topics/207.dita#topic_qft_qwn_hv"), prefixTemp("topics/206.dita#unique_87"))
      .put(prefixTemp("maps/Chunk88.dita"), prefixTemp("maps/Chunk88.dita"))
      .put(prefixTemp("topics/208.dita"), prefixTemp("maps/Chunk88.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/208.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk88.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("maps/topics/209.dita"), prefixTemp("maps/Chunk88.dita#unique_89"))
      .put(prefixTemp("maps/topics/209.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk88.dita#unique_89"))
      .put(prefixTemp("maps/211.dita"), prefixTemp("maps/211.dita"))
      .put(prefixTemp("topics/210.dita"), prefixTemp("maps/211.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/210.dita#topic_qft_qwn_hv"), prefixTemp("maps/211.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/212.dita"), prefixTemp("maps/211.dita#unique_90"))
      .put(prefixTemp("topics/212.dita#topic_qft_qwn_hv"), prefixTemp("maps/211.dita#unique_90"))
      .put(prefixTemp("214.dita"), prefixTemp("214.dita"))
      .put(prefixTemp("topics/213.dita"), prefixTemp("214.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/213.dita#topic_qft_qwn_hv"), prefixTemp("214.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/215.dita"), prefixTemp("214.dita#unique_91"))
      .put(prefixTemp("topics/215.dita#topic_qft_qwn_hv"), prefixTemp("214.dita#unique_91"))
      .put(prefixTemp("maps/topics/217.dita"), prefixTemp("maps/topics/217.dita"))
      .put(prefixTemp("topics/216.dita"), prefixTemp("maps/topics/217.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/216.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/217.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/218.dita"), prefixTemp("maps/topics/217.dita#unique_92"))
      .put(prefixTemp("topics/218.dita#topic_qft_qwn_hv"), prefixTemp("maps/topics/217.dita#unique_92"))
      .put(prefixTemp("topics/220.dita"), prefixTemp("topics/220.dita"))
      .put(prefixTemp("topics/219.dita"), prefixTemp("topics/220.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/219.dita#topic_qft_qwn_hv"), prefixTemp("topics/220.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/221.dita"), prefixTemp("topics/220.dita#unique_93"))
      .put(prefixTemp("topics/221.dita#topic_qft_qwn_hv"), prefixTemp("topics/220.dita#unique_93"))
      .put(prefixTemp("maps/Chunk94.dita"), prefixTemp("maps/Chunk94.dita"))
      .put(prefixTemp("topics/222.dita"), prefixTemp("maps/Chunk94.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/222.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk94.dita#topic_qft_qwn_hv"))
      .put(prefixTemp("topics/223.dita"), prefixTemp("maps/Chunk94.dita#unique_95"))
      .put(prefixTemp("topics/223.dita#topic_qft_qwn_hv"), prefixTemp("maps/Chunk94.dita#unique_95"))
      .build();
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
