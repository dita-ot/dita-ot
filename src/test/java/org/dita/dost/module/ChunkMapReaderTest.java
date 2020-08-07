/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ChunkMapReaderTest {

    private final File resourceDir = TestUtils.getResourceDir(ChunkModuleTest.class);
    private File tempBaseDir;

    @Before
    public void setUp() throws Exception {
        tempBaseDir = TestUtils.createTempDir(getClass());
        TestUtils.copy(new File(resourceDir, "src"), tempBaseDir);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempBaseDir);
    }

    @Test
    public void testunware_chunk_content() {
        test("unware_chunk_content.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("dita1.dita", "dita1.dita")
                        .put("one.dita#topicID", "two.dita#unique_2")
                        .put("one.dita", "two.dita#unique_2")
                        .put("two.dita", "two.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testunware_chunk_content2() {
        test("unware_chunk_content2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("one.dita#topicID", "Chunk0.dita#unique_3")
                        .put("one.dita", "Chunk0.dita#unique_3")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "one.dita")
                        .build()
        );
    }

    @Test
    public void testconflict_same_id() {
        test("conflict_same_id.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("t1.dita", "Chunk0.dita#topic1")
                        .put("t1.dita#topic1", "Chunk0.dita#topic1")
                        .put("t2.dita#topic1", "Chunk0.dita#unique_1")
                        .put("t2.dita", "Chunk0.dita#unique_1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "t1.dita")
                        .build()
        );
    }

    @Test
    public void testanchor1() {
        test("anchor1.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("reference1.dita#reference1", "Chunk0.dita#reference1")
                        .put("reference1.dita", "Chunk0.dita#reference1")
                        .put("task1.dita#task1", "Chunk0.dita#task1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("task1.dita", "Chunk0.dita#task1")
                        .put("concept1.dita#concept1r", "Chunk0.dita#concept1r")
                        .put("concept1.dita", "Chunk0.dita#concept1r")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "concept1.dita")
                        .build()
        );
    }

    @Test
    public void testanchor2() {
        test("anchor2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("reference1.dita#reference1", "Chunk1.dita#reference1")
                        .put("reference1.dita", "Chunk1.dita#reference1")
                        .put("Chunk1.dita", "Chunk1.dita")
                        .put("task1.dita#task1", "Chunk1.dita#task1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("task1.dita", "Chunk1.dita#task1")
                        .put("concept1.dita#concept1r", "Chunk0.dita#concept1r")
                        .put("concept1.dita", "Chunk0.dita#concept1r")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "task1.dita")
                        .put("Chunk0.dita", "concept1.dita")
                        .build()
        );
    }

    @Test
    public void testcase1() {
        test("case1.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("three.dita", "three.dita")
                        .put("four.dita", "four.dita")
                        .put("ditabase.dita#two", "two.dita#two")
                        .put("one.dita", "one.dita")
                        .put("ditabase.dita#four", "four.dita#four")
                        .put("two.dita", "two.dita")
                        .put("ditabase.dita#one", "one.dita#one")
                        .put("ditabase.dita#three", "three.dita#three")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testcase2() {
        test("case2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("case2.dita", "case2.dita")
                        .put("ditabase.dita#two", "case2.dita#two")
                        .put("ditabase.dita#four", "case2.dita#four")
                        .put("nested.dita", "case2.dita#nested")
                        .put("ditabase.dita#one", "case2.dita#one")
                        .put("nested.dita#nested", "case2.dita#nested")
                        .put("ditabase.dita#five", "case2.dita#five")
                        .put("ditabase.dita#three", "case2.dita#three")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testcase3() {
        test("case3.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("three.dita", "three.dita")
                        .put("child.dita", "child.dita")
                        .put("ditabase.dita#three", "three.dita#three")
                        .put("parent.dita", "parent.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testcase4() {
        test("case4.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("child.dita", "Chunk0.dita#nested")
                        .put("case4.dita", "Chunk0.dita#parent")
                        .put("child.dita#nested", "Chunk0.dita#nested")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("case4.dita#parent", "Chunk0.dita#parent")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "case4.dita")
                        .build()
        );
    }

    @Test
    public void testcase5() {
        test("case5.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("three.dita", "three.dita")
                        .put("four.dita", "four.dita")
                        .put("ditabase.dita#two", "two.dita#two")
                        .put("one.dita", "one.dita")
                        .put("ditabase.dita#four", "four.dita#four")
                        .put("two.dita", "two.dita")
                        .put("ditabase.dita#one", "one.dita#one")
                        .put("ditabase.dita#three", "three.dita#three")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testcase6() {
        test("case6.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("three.dita", "three.dita")
                        .put("four.dita", "four.dita")
                        .put("ditabase.dita#two", "two.dita#two")
                        .put("one.dita", "one.dita")
                        .put("ditabase.dita#four", "four.dita#four")
                        .put("two.dita", "two.dita")
                        .put("ditabase.dita#one", "one.dita#one")
                        .put("ditabase.dita#three", "three.dita#three")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testcase7() {
        test("case7.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("child.dita#child", "Chunk0.dita#child")
                        .put("child.dita", "Chunk0.dita#child")
                        .put("child2.dita#child2", "Chunk0.dita#child2")
                        .put("child2.dita", "Chunk0.dita#child2")
                        .put("parent.dita#parent", "Chunk0.dita#parent")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("parent.dita", "Chunk0.dita#parent")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent.dita")
                        .build()
        );
    }

    @Test
    public void testlink1() {
        test("link1.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("ditabase.dita#topic", "topic.dita#topic")
                        .put("t1.dita", "topic.dita#topic1")
                        .put("t1.dita#topic1", "topic.dita#topic1")
                        .put("sub/t3.dita", "sub/t3.dita")
                        .put("topic.dita", "topic.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testlink2() {
        test("link2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("t3.dita", "Chunk0.dita#topic3")
                        .put("ditabase.dita#topic", "Chunk0.dita#topic")
                        .put("ditabase.dita#task", "Chunk0.dita#task")
                        .put("ditabase.dita#concept", "Chunk0.dita#concept")
                        .put("ref.dita", "ref.dita")
                        .put("t3.dita#topic3", "Chunk0.dita#topic3")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("ditabase.dita", "Chunk0.dita#topic")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "t3.dita")
                        .build()
        );
    }

    @Test
    public void testFixChunk_map1() {
        test("FixChunk_map1.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("parent1.dita", "Chunk0.dita#P1")
                        .put("parent1.dita#P1", "Chunk0.dita#P1")
                        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent1.dita")
                        .build()
        );
    }

    @Test
    public void testFixChunk_map2() {
        test("FixChunk_map2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("ditabase.dita#Y", "Chunk0.dita#Y")
                        .put("parent1.dita", "Chunk0.dita#P1")
                        .put("parent1.dita#P1", "Chunk0.dita#P1")
                        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
                        .put("ditabase.dita#Y2", "Chunk0.dita#Y2")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent1.dita")
                        .build()
        );
    }

    @Test
    public void testFixChunk_map3() {
        test("FixChunk_map3.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent1.dita")
                        .build()
        );
    }

    @Test
    public void testFixChunk_map4() {
        test("FixChunk_map4.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested1.dita#N1a", "parentchunk.dita#N1a")
                        .put("parent1.dita", "parentchunk.dita#P1")
                        .put("parentchunk.dita", "parentchunk.dita")
                        .put("parent1.dita#P1", "parentchunk.dita#P1")
                        .put("nested1.dita#N1", "parentchunk.dita#N1")
                        .put("nested1.dita", "parentchunk.dita#N1")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testFixChunk_map5() {
        test("FixChunk_map5.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testFixChunk_map6() {
        test("FixChunk_map6.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested1.dita#N1", "nestedchunk.dita#N1")
                        .put("nestedchunk.dita", "nestedchunk.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testFixChunk_map7() {
        test("FixChunk_map7.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("child1.dita", "child1.dita")
                        .put("parent1.dita", "parent1.dita")
                        .put("parent2.dita", "parent2.dita")
                        .put("child2.dita", "child2.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testFixChunk_map8() {
        test("FixChunk_map8.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested2.dita", "nested2.dita")
                        .put("nested1.dita", "nested1.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testByTopic_map2() {
        test("ByTopic_map2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested1.dita#N1a", "N1a.dita#N1a")
                        .put("N1.dita", "N1.dita")
                        .put("N1a.dita", "N1a.dita")
                        .put("nested1.dita#N1", "N1.dita#N1")
                        .put("nested2.dita", "nested2.dita")
                        .put("nested1.dita", "N1.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testByTopic_map3() {
        test("ByTopic_map3.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("t1.dita", "t1.dita")
                        .put("nested1.dita", "nested1.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testByTopic_map4() {
        test("ByTopic_map4.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested1.dita#N1a", "nest_split.dita#N1a")
                        .put("nested2.dita#N2a", "nest_split.dita#N2a")
                        .put("nested1.dita#N1", "nest_split.dita#N1")
                        .put("nested2.dita", "nest_split.dita#N2")
                        .put("nest_split.dita", "nest_split.dita")
                        .put("nested1.dita", "nest_split.dita#N1")
                        .put("nested2.dita#N2", "nest_split.dita#N2")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testByTopic_map5() {
        test("ByTopic_map5.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("map5.dita", "map5.dita")
                        .put("nested1.dita#N1a", "map5.dita#N1a")
                        .put("t1.dita", "Chunk0.dita#topic1")
                        .put("t1.dita#topic1", "Chunk0.dita#topic1")
                        .put("nested1.dita#N1", "map5.dita#N1")
                        .put("nested1.dita", "map5.dita#N1")
                        .put("t2.dita#topic2", "Chunk0.dita#topic2")
                        .put("t2.dita", "Chunk0.dita#topic2")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "map5.dita")
                        .build()
        );
    }

    @Test
    public void testByTopic_map6() {
        test("ByTopic_map6.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "t1.dita")
                        .put("Chunk0.dita", "nested1.dita")
                        .build()
        );
    }

    @Test
    public void testByTopic_map7() {
        test("ByTopic_map7.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested4.dita#N1a", "Chunk1.dita#N1a")
                        .put("nested1.dita#N1a", "N1a.dita#N1a")
                        .put("Chunk1.dita", "Chunk1.dita")
                        .put("nested4.dita#N1", "Chunk0.dita#N1")
                        .put("N1.dita", "N1.dita")
                        .put("N1a.dita", "N1a.dita")
                        .put("nested1.dita#N1", "N1.dita#N1")
                        .put("nested4.dita", "Chunk0.dita")
                        .put("nested1.dita", "N1.dita")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "N1a.dita")
                        .put("Chunk0.dita", "N1.dita")
                        .build()
        );
    }

    @Test
    public void testtopicgroup_chunk() {
        test("topicgroup_chunk.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("Chunk19.dita", "Chunk23.dita#Chunk19")
                        .put("headkid.dita#topicID", "Chunk22.dita#topicID")
                        .put("groupkid.dita#topicID", "Chunk21.dita#topicID")
                        .put("Chunk18.dita#Chunk18", "Chunk22.dita#Chunk18")
                        .put("Chunk22.dita", "Chunk22.dita")
                        .put("Chunk8.dita", "Chunk8.dita")
                        .put("Chunk19.dita#Chunk19", "Chunk23.dita#Chunk19")
                        .put("Chunk7.dita", "Chunk7.dita")
                        .put("dita5.dita", "Chunk11.dita#topicID")
                        .put("Chunk1.dita", "Chunk1.dita")
                        .put("container.dita#topicID", "Chunk20.dita#topicID")
                        .put("dita8.dita", "Chunk15.dita#topicID")
                        .put("shortkid.dita", "Chunk23.dita#topicID")
                        .put("dita9.dita#topicID", "Chunk15.dita#unique_16")
                        .put("dita1.dita", "dita1.dita")
                        .put("Chunk10.dita", "Chunk10.dita")
                        .put("dita7.dita#topicID", "Chunk14.dita#topicID")
                        .put("Chunk21.dita", "Chunk21.dita")
                        .put("dita8.dita#topicID", "Chunk15.dita#topicID")
                        .put("Chunk13.dita", "Chunk14.dita")
                        .put("Chunk23.dita", "Chunk23.dita")
                        .put("Chunk20.dita", "Chunk20.dita")
                        .put("dita4.dita#topicID", "Chunk9.dita#topicID")
                        .put("dita3.dita#topicID", "Chunk8.dita#topicID")
                        .put("dita7.dita", "Chunk14.dita#topicID")
                        .put("dita6.dita#topicID", "Chunk11.dita#unique_12")
                        .put("dita1.dita#topicID", "Chunk6.dita#topicID")
                        .put("headkid.dita", "Chunk22.dita#topicID")
                        .put("dita5.dita#topicID", "Chunk11.dita#topicID")
                        .put("dita4.dita", "dita4.dita")
                        .put("Chunk6.dita", "Chunk6.dita")
                        .put("shortkid.dita#topicID", "Chunk23.dita#topicID")
                        .put("Chunk17.dita", "Chunk21.dita")
                        .put("dita2.dita#topicID", "Chunk7.dita#topicID")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("Chunk11.dita", "Chunk11.dita")
                        .put("dita2.dita", "dita2.dita")
                        .put("Chunk14.dita", "Chunk14.dita")
                        .put("Chunk18.dita", "Chunk22.dita#Chunk18")
                        .put("container.dita", "Chunk20.dita#topicID")
                        .put("Chunk15.dita", "Chunk15.dita")
                        .put("dita9.dita", "Chunk15.dita#unique_16")
                        .put("dita3.dita", "dita3.dita")
                        .put("Chunk9.dita", "Chunk9.dita")
                        .put("groupkid.dita", "Chunk21.dita#topicID")
                        .put("dita6.dita", "Chunk11.dita#unique_12")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk20.dita", "container.dita")
                        .put("Chunk23.dita", "Chunk19.dita")
                        .put("Chunk6.dita", "dita1.dita")
                        .put("Chunk22.dita", "Chunk18.dita")
                        .put("Chunk0.dita", "dita1.dita")
                        .put("Chunk8.dita", "dita3.dita")
                        .put("Chunk11.dita", "dita5.dita")
                        .put("Chunk14.dita", "Chunk13.dita")
                        .put("Chunk7.dita", "dita2.dita")
                        .put("Chunk1.dita", "dita2.dita")
                        .put("Chunk15.dita", "dita8.dita")
                        .put("Chunk10.dita", "dita5.dita")
                        .put("Chunk9.dita", "dita4.dita")
                        .put("Chunk21.dita", "Chunk17.dita")
                        .build()
        );
    }

    @Test
    public void testchunk_map_tocontent() {
        test("chunk_map_tocontent.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("chunk_map_tocontent/dita1.dita#topicID1", "chunk_map_tocontent/Chunk0.dita#topicID1")
                        .put("chunk_map_tocontent/Chunk0.dita", "chunk_map_tocontent/Chunk0.dita")
                        .put("chunk_map_tocontent/dita.xml", "chunk_map_tocontent/dita.xml")
                        .put("chunk_map_tocontent/sub_dita1.dita", "chunk_map_tocontent/Chunk0.dita#topicIDSUB")
                        .put("chunk_map_tocontent/sub_dita2.dita#topicIDSUB", "chunk_map_tocontent/Chunk1.dita#topicIDSUB")
                        .put("chunk_map_tocontent/dita2.dita", "chunk_map_tocontent/Chunk1.dita#topicID1")
                        .put("chunk_map_tocontent/dita2.dita#topicID1", "chunk_map_tocontent/Chunk1.dita#topicID1")
                        .put("chunk_map_tocontent/sub_dita1.dita#topicIDSUB", "chunk_map_tocontent/Chunk0.dita#topicIDSUB")
                        .put("chunk_map_tocontent/Chunk1.dita", "chunk_map_tocontent/Chunk1.dita")
                        .put("chunk_map_tocontent/sub_dita2.dita", "chunk_map_tocontent/Chunk1.dita#topicIDSUB")
                        .put("chunk_map_tocontent/dita1.dita", "chunk_map_tocontent/Chunk0.dita#topicID1")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("chunk_map_tocontent/Chunk0.dita", "chunk_map_tocontent/dita1.dita")
                        .put("chunk_map_tocontent/Chunk1.dita", "chunk_map_tocontent/dita2.dita")
                        .build()
        );
    }

    @Test
    public void testconflict_by_topic() {
        test("conflict_by_topic.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested4.dita#N1a", "Chunk1.dita#N1a")
                        .put("nested1.dita#N1a", "N1a.dita#N1a")
                        .put("Chunk1.dita", "Chunk1.dita")
                        .put("nested4.dita#N1", "Chunk0.dita#N1")
                        .put("N1.dita", "N1.dita")
                        .put("N1a.dita", "N1a.dita")
                        .put("nested1.dita#N1", "N1.dita#N1")
                        .put("nested4.dita", "Chunk0.dita")
                        .put("nested1.dita", "N1.dita")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "N1a.dita")
                        .put("Chunk0.dita", "N1.dita")
                        .build()
        );
    }

    @Test
    public void testchunk_hogs_memory() {
        test("chunk_hogs_memory.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testAttribute_map2() {
        test("Attribute_map2.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("parent1.dita", "Chunk0.dita#parent1")
                        .put("ditabase.dita#Y1", "Chunk0.dita#Y1")
                        .put("parent1.dita#parent1", "Chunk0.dita#parent1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("ditabase.dita#Y1a", "Chunk0.dita#Y1a")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent1.dita")
                        .build()
        );
    }

    @Test
    public void testAttribute_map3() {
        test("Attribute_map3.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent1.dita")
                        .build()
        );
    }

    @Test
    public void testAttribute_map4() {
        test("Attribute_map4.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("parentchunk.dita", "parentchunk.dita")
                        .put("nested1.dita", "nested1.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testAttribute_map5() {
        test("Attribute_map5.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testAttribute_map6() {
        test("Attribute_map6.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("nested1.dita#N1", "nestedchunk.dita#N1")
                        .put("nestedchunk.dita", "nestedchunk.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testAttribute_map7() {
        test("Attribute_map7.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("child1.dita", "child1.dita")
                        .put("parent1.dita", "parent1.dita")
                        .put("parent2.dita", "parent2.dita")
                        .put("child2.dita", "child2.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testAttribute_map8() {
        test("Attribute_map8.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "parent1.dita")
                        .build()
        );
    }

    @Test
    public void testAttribute_map9() {
        test("Attribute_map9.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("Y.dita", "Chunk0.dita#Y")
                        .put("Z.dita#Z1", "Chunk0.dita#Z1")
                        .put("Y.dita#Y", "Chunk0.dita#Y")
                        .put("Z.dita#Z", "Chunk0.dita#Z")
                        .put("X.dita", "X.dita")
                        .put("Z.dita", "Chunk0.dita#Z")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("Y.dita#Y1a", "Chunk0.dita#Y1a")
                        .put("Y.dita#Y1", "Chunk0.dita#Y1")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "Y.dita")
                        .build()
        );
    }

    @Test
    public void testcopy_to1() {
        test("copy_to1.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
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
        test("copy_to2.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
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
        test("ByTopic_batseparate0.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("batfeeding.dita", "batfeeding.dita")
                        .put("batcaring.dita", "batcaring.dita")
                        .put("batcleaning.dita", "batcleaning.dita")
                        .put("battytasks.dita#batcleaning", "batcleaning.dita#batcleaning")
                        .put("battytasks.dita#batfeeding", "batfeeding.dita#batfeeding")
                        .put("battytasks.dita#battytasks", "Chunk0.dita#battytasks")
                        .put("battytasks.dita#batcaring", "batcaring.dita#batcaring")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk0.dita", "battytasks.dita")
                        .build()
        );
    }

    @Test
    public void testchunk_duplicate_tocontent() {
        test("chunk_duplicate_tocontent.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("dita2.dita", "Chunk1.dita#topicID1")
                        .put("Chunk1.dita", "Chunk1.dita")
                        .put("sub_dita2.dita#topicIDSUB", "Chunk1.dita#topicIDSUB")
                        .put("dita2.dita#topicID1", "Chunk1.dita#topicID1")
                        .put("dita1.dita", "Chunk0.dita#topicID1")
                        .put("sub_dita2.dita", "Chunk1.dita#topicIDSUB")
                        .put("sub_dita1.dita#topicIDSUB", "Chunk0.dita#topicIDSUB")
                        .put("sub_dita1.dita", "Chunk0.dita#topicIDSUB")
                        .put("dita1.dita#topicID1", "Chunk0.dita#topicID1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "dita2.dita")
                        .put("Chunk0.dita", "dita1.dita")
                        .build()
        );
    }

    @Test
    public void testAttribute_map10() {
        test("Attribute_map10.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "Y.dita")
                        .put("Chunk2.dita", "Z.dita")
                        .put("Chunk0.dita", "X.dita")
                        .build()
        );
    }

    @Test
    public void testAttribute_map11() {
        test("Attribute_map11.ditamap",
                ImmutableMap.<String, String>builder()
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
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk1.dita", "Y.dita")
                        .put("Chunk0.dita", "X.dita")
                        .build()
        );
    }

    @Test
    public void testchunk_rewrite_tocontent() {
        test("chunk_rewrite_tocontent.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("dita4.dita#topicID", "Chunk3.dita#topicID")
                        .put("dita3.dita#topicID", "Chunk0.dita#unique_2")
                        .put("dita6.dita#topicID", "Chunk4.dita#unique_5")
                        .put("dita1.dita#topicID", "Chunk0.dita#topicID")
                        .put("dita5.dita#topicID", "Chunk4.dita#topicID")
                        .put("dita4.dita", "Chunk3.dita#topicID")
                        .put("Chunk3.dita", "Chunk3.dita")
                        .put("dita2.dita#topicID", "Chunk0.dita#unique_1")
                        .put("Chunk0.dita", "Chunk0.dita")
                        .put("dita2.dita", "Chunk0.dita#unique_1")
                        .put("dita5.dita", "Chunk4.dita#topicID")
                        .put("dita1.dita", "Chunk0.dita#topicID")
                        .put("dita3.dita", "Chunk0.dita#unique_2")
                        .put("dita6.dita", "Chunk4.dita#unique_5")
                        .put("Chunk4.dita", "Chunk4.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .put("Chunk3.dita", "dita4.dita")
                        .put("Chunk0.dita", "dita1.dita")
                        .put("Chunk4.dita", "dita5.dita")
                        .build()
        );
    }

    @Test
    public void testconflict_to_content() {
        test("conflict_to_content.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("map5.dita", "map5.dita")
                        .put("nested1.dita#N1a", "map1.dita#N1a")
                        .put("t1.dita", "map5.dita#topic1")
                        .put("t1.dita#topic1", "map5.dita#topic1")
                        .put("nested1.dita#N1", "map1.dita#N1")
                        .put("map1.dita", "map1.dita")
                        .put("nested1.dita", "map1.dita#N1")
                        .put("t2.dita#topic2", "map5.dita#topic2")
                        .put("t2.dita", "map5.dita#topic2")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
        );
    }

    @Test
    public void testexternal_chunk() {
        test("external_chunk.ditamap",
                ImmutableMap.<String, String>builder()
                        .put("dita1.dita#ditatask111", "ditatask111.dita#ditatask111")
                        .put("ditatask111.dita", "ditatask111.dita")
                        .build()
                ,
                ImmutableMap.<String, String>builder()
                        .build()
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
        return changeTable.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> t.relativize(e.getKey()).toString(),
                        e -> t.relativize(e.getValue()).toString()));
    }

}
