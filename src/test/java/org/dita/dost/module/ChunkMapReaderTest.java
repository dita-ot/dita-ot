/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ChunkMapReaderTest {

  private final File resourceDir = TestUtils.getResourceDir(ChunkModuleTest.class);
  private File tempBaseDir;

  @BeforeEach
  public void setUp() throws Exception {
    tempBaseDir = TestUtils.createTempDir(getClass());
    TestUtils.copy(new File(resourceDir, "src"), tempBaseDir);
  }

  @AfterEach
  public void tearDown() throws Exception {
    TestUtils.forceDelete(tempBaseDir);
  }

  @Test
  public void testcase1() {
    test(
      "case1.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("three.dita", "three.dita")
        .put("four.dita", "four.dita")
        .put("ditabase.dita#two", "two.dita#two")
        .put("one.dita", "one.dita")
        .put("ditabase.dita#four", "four.dita#four")
        .put("two.dita", "two.dita")
        .put("ditabase.dita#one", "one.dita#one")
        .put("ditabase.dita#three", "three.dita#three")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testcase3() {
    test(
      "case3.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("three.dita", "three.dita")
        .put("child.dita", "child.dita")
        .put("ditabase.dita#three", "three.dita#three")
        .put("parent.dita", "parent.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testcase6() {
    test(
      "case6.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("three.dita", "three.dita")
        .put("four.dita", "four.dita")
        .put("ditabase.dita#two", "two.dita#two")
        .put("one.dita", "one.dita")
        .put("ditabase.dita#four", "four.dita#four")
        .put("two.dita", "two.dita")
        .put("ditabase.dita#one", "one.dita#one")
        .put("ditabase.dita#three", "three.dita#three")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testlink1() {
    test(
      "link1.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("ditabase.dita#topic", "topic.dita#topic")
        .put("t1.dita", "topic.dita#topic1")
        .put("t1.dita#topic1", "topic.dita#topic1")
        .put("sub/t3.dita", "sub/t3.dita")
        .put("topic.dita", "topic.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testFixChunk_map1() {
    test(
      "FixChunk_map1.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("parent1.dita", "Chunk0.dita#P1")
        .put("parent1.dita#P1", "Chunk0.dita#P1")
        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
        .put("Chunk0.dita", "Chunk0.dita")
        .build(),
      Collections.singletonMap("Chunk0.dita", "parent1.dita")
    );
  }

  @Test
  public void testFixChunk_map2() {
    test(
      "FixChunk_map2.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("ditabase.dita#Y", "Chunk0.dita#Y")
        .put("parent1.dita", "Chunk0.dita#P1")
        .put("parent1.dita#P1", "Chunk0.dita#P1")
        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
        .put("ditabase.dita#Y2", "Chunk0.dita#Y2")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
        .build(),
      Collections.singletonMap("Chunk0.dita", "parent1.dita")
    );
  }

  @Test
  public void testFixChunk_map3() {
    test(
      "FixChunk_map3.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("ditabase.dita#Y", "Chunk0.dita#Y")
        .put("ditabase.dita#X", "Chunk0.dita#X")
        .put("parent1.dita", "Chunk0.dita#P1")
        .put("ditabase.dita#Z", "Chunk0.dita#Z")
        .put("parent1.dita#P1", "Chunk0.dita#P1")
        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
        .put("ditabase.dita#Y2", "Chunk0.dita#Y2")
        .put("ditabase.dita#Z1", "Chunk0.dita#Z1")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
        .build(),
      Collections.singletonMap("Chunk0.dita", "parent1.dita")
    );
  }

  @Test
  public void testFixChunk_map4() {
    test(
      "FixChunk_map4.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("nested1.dita#N1a", "parentchunk.dita#N1a")
        .put("parent1.dita", "parentchunk.dita#P1")
        .put("parentchunk.dita", "parentchunk.dita")
        .put("parent1.dita#P1", "parentchunk.dita#P1")
        .put("nested1.dita#N1", "parentchunk.dita#N1")
        .put("nested1.dita", "parentchunk.dita#N1")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testFixChunk_map5() {
    test(
      "FixChunk_map5.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("child3.dita#C3", "parentchunk.dita#C3")
        .put("child1.dita#C1", "parentchunk.dita#C1")
        .put("parent1.dita", "parentchunk.dita#P1")
        .put("parentchunk.dita", "parentchunk.dita")
        .put("grandchild2.dita#GC2", "child2chunk.dita#GC2")
        .put("child3.dita", "parentchunk.dita#C3")
        .put("grandchild2.dita", "child2chunk.dita#GC2")
        .put("child2.dita#C2", "child2chunk.dita#C2")
        .put("child1.dita", "parentchunk.dita#C1")
        .put("grandchild3.dita#GC3", "parentchunk.dita#GC3")
        .put("parent1.dita#P1", "parentchunk.dita#P1")
        .put("grandchild3.dita", "parentchunk.dita#GC3")
        .put("child2.dita", "child2chunk.dita#C2")
        .put("child2chunk.dita", "child2chunk.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testFixChunk_map6() {
    test(
      "FixChunk_map6.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("nested1.dita#N1", "nestedchunk.dita#N1")
        .put("nestedchunk.dita", "nestedchunk.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testFixChunk_map7() {
    test(
      "FixChunk_map7.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("child1.dita", "child1.dita")
        .put("parent1.dita", "parent1.dita")
        .put("parent2.dita", "parent2.dita")
        .put("child2.dita", "child2.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testByTopic_map2() {
    test(
      "ByTopic_map2.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("nested1.dita#N1a", "N1a.dita#N1a")
        .put("N1.dita", "N1.dita")
        .put("N1a.dita", "N1a.dita")
        .put("nested1.dita#N1", "N1.dita#N1")
        .put("nested2.dita", "nested2.dita")
        .put("nested1.dita", "N1.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testByTopic_map4() {
    test(
      "ByTopic_map4.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("nested1.dita#N1a", "nest_split.dita#N1a")
        .put("nested2.dita#N2a", "nest_split.dita#N2a")
        .put("nested1.dita#N1", "nest_split.dita#N1")
        .put("nested2.dita", "nest_split.dita#N2")
        .put("nest_split.dita", "nest_split.dita")
        .put("nested1.dita", "nest_split.dita#N1")
        .put("nested2.dita#N2", "nest_split.dita#N2")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testByTopic_map6() {
    test(
      "ByTopic_map6.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("nested1.dita#N1a", "Chunk1.dita#N1a")
        .put("nested2.dita#N2a", "Chunk0.dita#N2a")
        .put("t1.dita", "Chunk1.dita#topic1")
        .put("Chunk1.dita", "Chunk1.dita")
        .put("t1.dita#topic1", "Chunk1.dita#topic1")
        .put("nested1.dita#N1", "Chunk0.dita#N1")
        .put("nested2.dita", "Chunk0.dita#N2")
        .put("nested1.dita", "Chunk0.dita#N1")
        .put("nested2.dita#N2", "Chunk0.dita#N2")
        .put("Chunk0.dita", "Chunk0.dita")
        .build(),
      TestUtils.<String, String>mapBuilder().put("Chunk1.dita", "t1.dita").put("Chunk0.dita", "nested1.dita").build()
    );
  }

  @Test
  public void testchunk_hogs_memory() {
    test(
      "chunk_hogs_memory.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("dita1.dita#C", "dita4.dita#C")
        .put("dita1.dita#AAA", "dita2.dita#AAA")
        .put("dita2.dita", "dita2.dita")
        .put("dita1.dita#B", "dita3.dita#B")
        .put("dita1.dita#D", "dita5.dita#D")
        .put("dita5.dita", "dita5.dita")
        .put("sub_dita2.dita#topicIDSUB", "dita2.dita#unique_0")
        .put("dita4.dita", "dita4.dita")
        .put("dita3.dita", "dita3.dita")
        .put("sub_dita2.dita", "dita2.dita#unique_0")
        .put("sub_dita1.dita#topicIDSUB", "dita2.dita#topicIDSUB")
        .put("sub_dita1.dita", "dita2.dita#topicIDSUB")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testAttribute_map2() {
    test(
      "Attribute_map2.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("parent1.dita", "Chunk0.dita#parent1")
        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
        .put("parent1.dita#parent1", "Chunk0.dita#parent1")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
        .build(),
      Collections.singletonMap("Chunk0.dita", "parent1.dita")
    );
  }

  @Test
  public void testAttribute_map3() {
    test(
      "Attribute_map3.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("ditabase.dita#Y", "Chunk0.dita#Y")
        .put("ditabase.dita#X", "Chunk0.dita#X")
        .put("parent1.dita", "Chunk0.dita#parent1")
        .put("ditabase.dita#Z", "Chunk0.dita#Z")
        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
        .put("ditabase.dita#Y2", "Chunk0.dita#Y2")
        .put("ditabase.dita#Z1", "Chunk0.dita#Z1")
        .put("parent1.dita#parent1", "Chunk0.dita#parent1")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
        .put("ditabase.dita", "Chunk0.dita#X")
        .build(),
      Collections.singletonMap("Chunk0.dita", "parent1.dita")
    );
  }

  @Test
  public void testAttribute_map4() {
    test(
      "Attribute_map4.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("parentchunk.dita", "parentchunk.dita")
        .put("nested1.dita", "nested1.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testAttribute_map5() {
    test(
      "Attribute_map5.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("child2.dita#topicmerge", "child2chunk.dita#topicmerge")
        .put("grandchild2.dita#grandchild2", "child2chunk.dita#grandchild2")
        .put("parent1.dita", "parentchunk.dita#parent1")
        .put("parentchunk.dita", "parentchunk.dita")
        .put("child3.dita#child3", "parentchunk.dita#child3")
        .put("parent1.dita#parent1", "parentchunk.dita#parent1")
        .put("child3.dita", "parentchunk.dita#child3")
        .put("grandchild2.dita", "child2chunk.dita#grandchild2")
        .put("child1.dita", "parentchunk.dita#child1")
        .put("grandchild3.dita#grandchild3", "parentchunk.dita#grandchild3")
        .put("grandchild3.dita", "parentchunk.dita#grandchild3")
        .put("child2.dita", "child2chunk.dita#topicmerge")
        .put("child2chunk.dita", "child2chunk.dita")
        .put("child1.dita#child1", "parentchunk.dita#child1")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testAttribute_map6() {
    test(
      "Attribute_map6.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("nested1.dita#N1", "nestedchunk.dita#N1")
        .put("nestedchunk.dita", "nestedchunk.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testAttribute_map7() {
    test(
      "Attribute_map7.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("child1.dita", "child1.dita")
        .put("parent1.dita", "parent1.dita")
        .put("parent2.dita", "parent2.dita")
        .put("child2.dita", "child2.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  @Test
  public void testAttribute_map8() {
    test(
      "Attribute_map8.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("ditabase.dita#Y", "Chunk0.dita#Y")
        .put("ditabase.dita#X", "Chunk0.dita#X")
        .put("parent1.dita", "Chunk0.dita#parent1")
        .put("ditabase.dita#Z", "Chunk0.dita#Z")
        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
        .put("ditabase.dita#Y2", "Chunk0.dita#Y2")
        .put("ditabase.dita#Z1", "Chunk0.dita#Z1")
        .put("parent1.dita#parent1", "Chunk0.dita#parent1")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
        .put("ditabase.dita", "Chunk0.dita#X")
        .build(),
      Collections.singletonMap("Chunk0.dita", "parent1.dita")
    );
  }

  @Test
  public void testAttribute_map9() {
    test(
      "Attribute_map9.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("Y.dita", "Chunk0.dita#Y")
        .put("Z.dita#Z1", "Chunk0.dita#Z1")
        .put("Y.dita#Y", "Chunk0.dita#Y")
        .put("Z.dita#Z", "Chunk0.dita#Z")
        .put("X.dita", "X.dita")
        .put("Z.dita", "Chunk0.dita#Z")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("Y.dita#Y1a", "Chunk0.dita#Y1a")
        .put("Y.dita#Y1", "Chunk0.dita#Y1")
        .build(),
      Collections.singletonMap("Chunk0.dita", "Y.dita")
    );
  }

  @Test
  public void testcopy_to1() {
    test(
      "copy_to1.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("Y1a.dita", "Y1a.dita")
        .put("Chunk16.dita", "Chunk16.dita")
        .put("Chunk2.dita", "Chunk2.dita")
        .put("Z.dita", "Z.dita")
        .put("Chunk8.dita", "Chunk8.dita")
        .put("documentY.dita#Z1", "Chunk9.dita#Z1")
        .put("Chunk7.dita", "Chunk7.dita")
        .put("Y2.dita", "Y2.dita")
        .put("documentY.dita#Y1a", "Chunk6.dita#Y1a")
        .put("Chunk1.dita", "Chunk1.dita")
        .put("ditabase.dita#Y1", "Chunk23.dita#Y1")
        .put("ditabase.dita#Y2", "Chunk23.dita#Y2")
        .put("Chunk10.dita", "Chunk10.dita")
        .put("documentX.dita#X", "Chunk10.dita#X")
        .put("documentX.dita#Y", "Chunk11.dita#Y")
        .put("documentX.dita#Z", "Chunk15.dita#Z")
        .put("topicYbranch.dita#Y2", "Y2.dita#Y2")
        .put("Chunk6.dita", "Chunk6.dita")
        .put("topicYbranch.dita#Y1", "Y1.dita#Y1")
        .put("Chunk3.dita", "Chunk3.dita")
        .put("Chunk17.dita", "Chunk17.dita")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("Chunk11.dita", "Chunk11.dita")
        .put("documentY.dita#Y1", "Chunk5.dita#Y1")
        .put("Chunk14.dita", "Chunk14.dita")
        .put("Y.dita", "Y.dita")
        .put("documentY.dita#Y2", "Chunk7.dita#Y2")
        .put("topicYbranch.dita#Y", "Chunk2.dita#Y")
        .put("Y1.dita", "Y1.dita")
        .put("Chunk9.dita", "Chunk9.dita")
        .put("topicX.dita#X", "Chunk0.dita#X")
        .put("Chunk19.dita", "Chunk19.dita")
        .put("ditabase.dita#Y", "Chunk23.dita#Y")
        .put("Chunk5.dita", "Chunk5.dita")
        .put("ditabase.dita#X", "Chunk23.dita#X")
        .put("ditabase.dita#Z", "Chunk23.dita#Z")
        .put("topicYbranch.dita#Y1a", "Y1a.dita#Y1a")
        .put("Chunk22.dita", "Chunk22.dita")
        .put("documentX.dita#Z1", "Chunk16.dita#Z1")
        .put("Chunk21.dita", "Chunk21.dita")
        .put("Chunk13.dita", "Chunk13.dita")
        .put("Chunk4.dita", "Chunk4.dita")
        .put("Chunk23.dita", "Chunk23.dita")
        .put("Chunk20.dita", "Chunk20.dita")
        .put("documentY.dita#Z", "Chunk8.dita#Z")
        .put("Chunk12.dita", "Chunk12.dita")
        .put("documentY.dita#Y", "Y.dita#Y")
        .put("X.dita", "X.dita")
        .put("documentY.dita#X", "Chunk4.dita#X")
        .put("Z1.dita", "Z1.dita")
        .put("Chunk18.dita", "Chunk18.dita")
        .put("Chunk15.dita", "Chunk15.dita")
        .put("documentX.dita#Y1a", "Chunk13.dita#Y1a")
        .put("ditabase.dita#Z1", "Chunk23.dita#Z1")
        .put("documentX.dita#Y1", "Chunk12.dita#Y1")
        .put("documentX.dita#Y2", "Chunk14.dita#Y2")
        .put("ditabase.dita#Y1a", "Chunk23.dita#Y1a")
        .put("topicY.dita#Y", "Chunk1.dita#Y")
        .build(),
      TestUtils
        .<String, String>mapBuilder()
        .put("Chunk23.dita", "Z.dita")
        .put("Chunk20.dita", "Y2.dita")
        .put("Chunk19.dita", "Y1a.dita")
        .put("Chunk5.dita", "Y1.dita")
        .put("Chunk12.dita", "Y1.dita")
        .put("Chunk16.dita", "Z1.dita")
        .put("Chunk2.dita", "topicYbranch.dita")
        .put("Chunk6.dita", "Y1a.dita")
        .put("Chunk3.dita", "document.dita")
        .put("Chunk22.dita", "Z1.dita")
        .put("Chunk17.dita", "Y.dita")
        .put("Chunk0.dita", "topicX.dita")
        .put("Chunk8.dita", "Z.dita")
        .put("Chunk11.dita", "Y.dita")
        .put("Chunk14.dita", "Y2.dita")
        .put("Chunk7.dita", "Y2.dita")
        .put("Chunk1.dita", "topicY.dita")
        .put("Chunk15.dita", "Z.dita")
        .put("Chunk18.dita", "Y1.dita")
        .put("Chunk10.dita", "documentX.dita")
        .put("Chunk9.dita", "Z1.dita")
        .put("Chunk21.dita", "Z.dita")
        .put("Chunk13.dita", "Y1a.dita")
        .put("Chunk4.dita", "documentY.dita")
        .build()
    );
  }

  @Test
  public void testcopy_to2() {
    test(
      "copy_to2.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("Y1a.dita", "Y1a.dita")
        .put("ditabase.dita#Y", "Chunk2.dita#Y")
        .put("Chunk5.dita", "Chunk5.dita")
        .put("ditabase.dita#X", "X.dita#X")
        .put("ditabase.dita#Z", "Chunk6.dita#Z")
        .put("Chunk2.dita", "Chunk2.dita")
        .put("Chunk6.dita", "Chunk6.dita")
        .put("X.dita", "X.dita")
        .put("Chunk3.dita", "Chunk3.dita")
        .put("Z1.dita", "Z1.dita")
        .put("Z.dita", "Z.dita")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("Y.dita", "Y.dita")
        .put("Chunk7.dita", "Chunk7.dita")
        .put("Y2.dita", "Y2.dita")
        .put("Chunk1.dita", "Chunk1.dita")
        .put("Y1.dita", "Y1.dita")
        .put("ditabase.dita#Y1", "Chunk3.dita#Y1")
        .put("ditabase.dita#Y2", "Chunk5.dita#Y2")
        .put("ditabase.dita#Z1", "Chunk7.dita#Z1")
        .put("topicX.dita#X", "Chunk0.dita#X")
        .put("Chunk4.dita", "Chunk4.dita")
        .put("ditabase.dita#Y1a", "Chunk4.dita#Y1a")
        .put("topicY.dita#Y", "Chunk1.dita#Y")
        .build(),
      TestUtils
        .<String, String>mapBuilder()
        .put("Chunk5.dita", "Y2.dita")
        .put("Chunk7.dita", "Z1.dita")
        .put("Chunk1.dita", "topicY.dita")
        .put("Chunk2.dita", "Y.dita")
        .put("Chunk6.dita", "Z.dita")
        .put("Chunk3.dita", "Y1.dita")
        .put("Chunk0.dita", "topicX.dita")
        .put("Chunk4.dita", "Y1a.dita")
        .build()
    );
  }

  @Test
  public void testByTopic_batseparate0() {
    test(
      "ByTopic_batseparate0.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("batfeeding.dita", "batfeeding.dita")
        .put("batcaring.dita", "batcaring.dita")
        .put("batcleaning.dita", "batcleaning.dita")
        .put("battytasks.dita#batcleaning", "batcleaning.dita#batcleaning")
        .put("battytasks.dita#batfeeding", "batfeeding.dita#batfeeding")
        .put("battytasks.dita#battytasks", "Chunk0.dita#battytasks")
        .put("battytasks.dita#batcaring", "batcaring.dita#batcaring")
        .put("Chunk0.dita", "Chunk0.dita")
        .build(),
      Collections.singletonMap("Chunk0.dita", "battytasks.dita")
    );
  }

  @Test
  public void testAttribute_map10() {
    test(
      "Attribute_map10.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("Y1a.dita", "Y1a.dita")
        .put("Z.dita#Z1", "Z1.dita#Z1")
        .put("Chunk2.dita", "Chunk2.dita")
        .put("X.dita", "Chunk0.dita")
        .put("Z1.dita", "Z1.dita")
        .put("Z.dita", "Chunk2.dita")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("Y.dita", "Chunk1.dita")
        .put("Y.dita#Y", "Chunk1.dita#Y")
        .put("Chunk1.dita", "Chunk1.dita")
        .put("X.dita#X", "Chunk0.dita#X")
        .put("Z.dita#Z", "Chunk2.dita#Z")
        .put("Y1.dita", "Y1.dita")
        .put("Y.dita#Y1a", "Y1a.dita#Y1a")
        .put("Y.dita#Y1", "Y1.dita#Y1")
        .build(),
      TestUtils
        .<String, String>mapBuilder()
        .put("Chunk1.dita", "Y.dita")
        .put("Chunk2.dita", "Z.dita")
        .put("Chunk0.dita", "X.dita")
        .build()
    );
  }

  @Test
  public void testAttribute_map11() {
    test(
      "Attribute_map11.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("Y.dita", "Chunk1.dita#Y")
        .put("Z.dita#Z1", "Chunk1.dita#Z1")
        .put("Y.dita#Y", "Chunk1.dita#Y")
        .put("Chunk1.dita", "Chunk1.dita")
        .put("X.dita#X", "Chunk0.dita#X")
        .put("Z.dita#Z", "Chunk1.dita#Z")
        .put("X.dita", "Chunk0.dita")
        .put("Z.dita", "Chunk1.dita#Z")
        .put("Chunk0.dita", "Chunk0.dita")
        .put("Y.dita#Y1a", "Chunk1.dita#Y1a")
        .put("Y.dita#Y1", "Chunk1.dita#Y1")
        .build(),
      TestUtils.<String, String>mapBuilder().put("Chunk1.dita", "Y.dita").put("Chunk0.dita", "X.dita").build()
    );
  }

  @Test
  public void testexternal_chunk() {
    test(
      "external_chunk.ditamap",
      TestUtils
        .<String, String>mapBuilder()
        .put("dita1.dita#ditatask111", "ditatask111.dita#ditatask111")
        .put("ditatask111.dita", "ditatask111.dita")
        .build(),
      Collections.emptyMap()
    );
  }

  private void test(final String testCase, final Map<String, String> change, final Map<String, String> conflict) {
    final String testName = FilenameUtils.getBaseName(testCase);
    final File tempDir = new File(tempBaseDir, testName);
    try {
      final ChunkMapReader mapReader = new ChunkMapReader();
      final TestUtils.CachingLogger logger = new TestUtils.CachingLogger(true);
      mapReader.setLogger(logger);
      final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
      mapReader.setJob(job);
      mapReader.setXmlUtils(new XMLUtils());
      mapReader.supportToNavigation(false);

      final URI path = job.getInputMap();
      final File mapFile = new File(tempDir, path.getPath());
      mapReader.read(mapFile);

      assertEquals(change, relativize(mapReader.getChangeTable(), tempDir));
      assertEquals(conflict, relativize(mapReader.getConflicTable(), tempDir));
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Map<String, String> relativize(Map<URI, URI> changeTable, File tempDir) {
    final URI t = tempDir.toURI();
    return changeTable
      .entrySet()
      .stream()
      .collect(Collectors.toMap(e -> t.relativize(e.getKey()).toString(), e -> t.relativize(e.getValue()).toString()));
  }
}
