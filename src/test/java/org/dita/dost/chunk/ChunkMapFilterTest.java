/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.chunk;

import org.apache.commons.io.FilenameUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.util.Job;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.dita.dost.chunk.ChunkOperation.Operation.*;

public class ChunkMapFilterTest {

    final File resourceDir = TestUtils.getResourceDir("chunk");
    private File tempBaseDir;
    final File srcBaseDir = new File(resourceDir, "src");
    final File expBaseDir = new File(resourceDir, "exp");

    @Before
    public void setUp() throws Exception {
        tempBaseDir = TestUtils.createTempDir(getClass());
    }

    @Test
    public void testAttribute_map1() {
        new TestBuilder("Attribute_map1")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita")
                );
    }

    @Test
    public void testAttribute_map10() {
        new TestBuilder("Attribute_map10")
                .map("map10.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "X.dita#X", null),
                        chunk(BY_TOPIC, null, "Y.dita#Y", null,
                                chunk(BY_TOPIC, null, "Y.dita#Y1", null,
                                        chunk(BY_TOPIC, null, "Y.dita#Y1a", null))),
                        chunk(BY_TOPIC, null, "Z.dita#Z", null,
                                chunk(BY_TOPIC, null, "Z.dita#Z1", null))
                );
    }

    @Test
    public void testAttribute_map11() {
        new TestBuilder("Attribute_map11")
                .map("map11.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "X.dita#X", null),
                        chunk(TO_CONTENT, null, "Y.dita", "Y.dita",
                                chunk(null, SELECT_DOCUMENT, "Z.dita", null)),
                        chunk(BY_TOPIC, null, "Chunk1.dita#", null,
                                chunk(BY_TOPIC, null, "Chunk1.dita#Y", null,
                                        chunk(BY_TOPIC, null, "Chunk1.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "Chunk1.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "Chunk1.dita#Z", null,
                                                chunk(BY_TOPIC, null, "Chunk1.dita#Z1", null))))
                );
    }

    @Test
    public void testAttribute_map11_() {
        new TestBuilder("Attribute_map11_")
                .map("_map11.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "_X.dita#X", null),
                        chunk(TO_CONTENT, null, "_Y.dita", "_Y.dita",
                                chunk(null, SELECT_DOCUMENT, "_Z.dita", null)),
                        chunk(BY_TOPIC, null, "Chunk0.dita#", null,
                                chunk(BY_TOPIC, null, "Chunk0.dita#Y", null,
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Z", null,
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Z1", null))))
                );
    }

    @Test
    public void testAttribute_map2() {
        new TestBuilder("Attribute_map2")
                .map("map2.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, null, "ditabase.dita#Y1", null))
                );
    }

    @Test
    public void testAttribute_map3() {
        new TestBuilder("Attribute_map3")
                .map("map3.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, null, "ditabase.dita", null)),
                        chunk(BY_TOPIC, null, "Chunk0.dita#", null,
                                chunk(BY_TOPIC, null, "Chunk0.dita#parent1", null,
                                        chunk(BY_TOPIC, null, "Chunk0.dita#X", null),
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Y", null,
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Y1", null,
                                                        chunk(BY_TOPIC, null, "Chunk0.dita#Y1a", null))),
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Y2", null),
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Z", null,
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Z1", null))))
                );
    }

    @Test
    public void testAttribute_map4() {
        new TestBuilder("Attribute_map4")
                .map("map4.ditamap")
                .run()
                .assertEquals(

                );
    }

    @Test
    public void testAttribute_map5() {
        new TestBuilder("Attribute_map5")
                .map("map5.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parentchunk.dita",
                                chunk(null, null, "child1.dita", null),
                                chunk(null, null, "child3.dita", null,
                                        chunk(null, null, "grandchild3.dita", null))),
                        chunk(TO_CONTENT, null, "child2.dita", "child2chunk.dita",
                                chunk(null, null, "grandchild2.dita", null))
                );
    }

    @Test
    public void testAttribute_map6() {
        new TestBuilder("Attribute_map6")
                .map("map6.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, SELECT_TOPIC, "nested1.dita#N1", "nestedchunk.dita")
                );
    }

    @Test
    public void testAttribute_map7() {
        new TestBuilder("Attribute_map7")
                .map("map7.ditamap")
                .run()
                .assertEquals(

                );
    }

    @Test
    public void testAttribute_map8() {
        new TestBuilder("Attribute_map8")
                .map("map8.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, SELECT_BRANCH, "ditabase.dita", null))
                );
    }

    @Test
    public void testAttribute_map9() {
        new TestBuilder("Attribute_map9")
                .map("map9.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "Y.dita", "Y.dita",
                                chunk(null, SELECT_DOCUMENT, "Z.dita", null))
                );
    }

    @Test
    public void testByTopic_batseparate0() {
        new TestBuilder("ByTopic_batseparate0")
                .map("batseparate0.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "battytasks.dita#battytasks", "battytasks.dita#battytasks"),
                        // FIXME this should create a new chunk based on ID
                        chunk(TO_CONTENT, null, "battytasks.dita#batcaring", "battytasks.dita#batcaring"),
                        chunk(TO_CONTENT, null, "battytasks.dita#batfeeding", "battytasks.dita#batfeeding"),
                        chunk(TO_CONTENT, null, "battytasks.dita#batcleaning", "battytasks.dita#batcleaning")
                );
    }

    @Test
    public void testByTopic_map1() {
        new TestBuilder("ByTopic_map1")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "nested1.dita#N1", null,
                                chunk(BY_TOPIC, null, "nested1.dita#N1a", null)),
                        chunk(BY_TOPIC, null, "nested2.dita#N2", null,
                                chunk(BY_TOPIC, null, "nested2.dita#N2a", null))
                );
    }

    @Test
    public void testByTopic_map2() {
        new TestBuilder("ByTopic_map2")
                .map("map2.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "nested1.dita#N1", null,
                                chunk(BY_TOPIC, null, "nested1.dita#N1a", null))
                );
    }

    @Test
    public void testByTopic_map3() {
        new TestBuilder("ByTopic_map3")
                .map("map3.ditamap")
                .run()
                .assertEquals(

                );
    }

    @Test
    public void testByTopic_map4() {
        new TestBuilder("ByTopic_map4")
                .map("map4.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "nested1.dita", "nested1.dita",
                                chunk(null, null, "nested2.dita", null))
                );
    }

    @Test
    public void testByTopic_map5() {
        new TestBuilder("ByTopic_map5")
                .map("map5.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "map5.dita", "map5.dita",
                                chunk(null, null, "nested1.dita", null)),
                        chunk(TO_CONTENT, null, "t1.dita", "map5.dita",
                                chunk(null, null, "t2.dita", null))
                );
    }

    @Test
    public void testByTopic_map6() {
        new TestBuilder("ByTopic_map6")
                .map("map6.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "nested1.dita", "nested1.dita",
                                chunk(null, null, "nested2.dita", null)),
                        chunk(TO_CONTENT, null, "t1.dita", "t1.dita",
                                chunk(null, null, "nested1.dita#N1a", null))
                );
    }

    @Test
    public void testByTopic_map7() {
        new TestBuilder("ByTopic_map7")
                .map("map7.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "nested1.dita#N1", null,
                                chunk(BY_TOPIC, null, "nested1.dita#N1a", null)),
                        chunk(BY_TOPIC, null, "nested4.dita#N1", null,
                                chunk(BY_TOPIC, null, "nested4.dita#N1a", null))
                );
    }

    @Test
    public void testFixChunk_map1() {
        new TestBuilder("FixChunk_map1")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, SELECT_TOPIC, "ditabase.dita#Y1", null))
                );
    }

    @Test
    public void testFixChunk_map2() {
        new TestBuilder("FixChunk_map2")
                .map("map2.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, SELECT_BRANCH, "ditabase.dita#Y", null))
                );
    }

    @Test
    public void testFixChunk_map3() {
        new TestBuilder("FixChunk_map3")
                .map("map3.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, null, "ditabase.dita#Y1", null)),
                        chunk(BY_TOPIC, null, "Chunk0.dita#", null,
                                chunk(BY_TOPIC, null, "Chunk0.dita#P1", null,
                                        chunk(BY_TOPIC, null, "Chunk0.dita#X", null),
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Y", null,
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Y1", null,
                                                        chunk(BY_TOPIC, null, "Chunk0.dita#Y1a", null)),
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Y2", null)),
                                        chunk(BY_TOPIC, null, "Chunk0.dita#Z", null,
                                                chunk(BY_TOPIC, null, "Chunk0.dita#Z1", null))))
                );
    }

    @Test
    public void testFixChunk_map4() {
        new TestBuilder("FixChunk_map4")
                .map("map4.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parent1.dita",
                                chunk(null, null, "nested1.dita", null))
                );
    }

    @Test
    public void testFixChunk_map5() {
        new TestBuilder("FixChunk_map5")
                .map("map5.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent1.dita", "parentchunk.dita",
                                chunk(null, SELECT_BRANCH, "child1.dita", null),
                                chunk(null, null, "child3.dita", null,
                                        chunk(null, SELECT_BRANCH, "grandchild3.dita", null))),
                        chunk(TO_CONTENT, SELECT_BRANCH, "child2.dita", "child2chunk.dita",
                                chunk(null, null, "grandchild2.dita", null))
                );
    }

    @Test
    public void testFixChunk_map6() {
        new TestBuilder("FixChunk_map6")
                .map("map6.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, SELECT_TOPIC, "nested1.dita#N1", "nestedchunk.dita")
                );
    }

    @Test
    public void testFixChunk_map7() {
        new TestBuilder("FixChunk_map7")
                .map("map7.ditamap")
                .run()
                .assertEquals(

                );
    }

    @Test
    public void testFixChunk_map8() {
        new TestBuilder("FixChunk_map8")
                .map("map8.ditamap")
                .run()
                .assertEquals(

                );
    }

    @Test
    public void testanchor1() {
        new TestBuilder("anchor1")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "concept1.dita", "concept1.dita",
                                chunk(null, null, "reference1.dita", null),
                                chunk(null, null, "task1.dita", null))
                );
    }

    @Test
    public void testanchor2() {
        new TestBuilder("anchor2")
                .map("map2.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "concept1.dita", "concept1.dita",
                                chunk(null, null, "reference1.dita", null)),
                        chunk(TO_CONTENT, null, "task1.dita", "task1.dita",
                                chunk(null, null, "reference1.dita", null))
                );
    }

    @Test
    public void testcase1() {
        new TestBuilder("case1")
                .map("case1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, SELECT_TOPIC, "ditabase.dita#one", "ditabase.dita#one"),
                        // FIXME this should dst two.dita
                        chunk(TO_CONTENT, SELECT_TOPIC, "ditabase.dita#two", "ditabase.dita#two"),
                        chunk(TO_CONTENT, SELECT_TOPIC, "ditabase.dita#four", "ditabase.dita#four"),
                        chunk(TO_CONTENT, SELECT_TOPIC, "ditabase.dita#three", "ditabase.dita#three")
                );
    }

    @Test
    public void testcase2() {
        new TestBuilder("case2")
                .map("case2.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "case2.dita", "case2.dita",
                                chunk(null, null, "ditabase.dita#one", null,
                                        chunk(null, null, null, null,
                                                chunk(null, null, "nested.dita", null))))
                );
    }

    @Test
    public void testcase3() {
        new TestBuilder("case3")
                .map("case3.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null))
                );
    }

    @Test
    public void testcase4() {
        new TestBuilder("case4")
                .map("case4.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "Chunk0.dita", "Chunk0.dita",
                                chunk(null, null, "case4.dita", null,
                                        chunk(null, null, "child.dita", null)))
                );
    }

    @Test
    public void testcase5() {
        new TestBuilder("case5")
                .map("case5.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null)),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null)),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null)),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null))
                );
    }

    @Test
    public void testcase6() {
        new TestBuilder("case6")
                .map("case6.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null)),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null)),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null)),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#one", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#two", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#three", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#four", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#five", null))
                );
    }

    @Test
    public void testcase7() {
        new TestBuilder("case7")
                .map("case7.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "parent.dita", "parent.dita",
                                chunk(null, null, "child.dita", null),
                                chunk(null, null, "child2.dita", null),
                                chunk(null, null, "http://www.metadita.org", null))
                );
    }

    @Test
    public void testchunk_duplicate_tocontent() {
        new TestBuilder("chunk_duplicate_tocontent")
                .map("chunk_duplicate.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "dita1.dita", "dita1.dita",
                                chunk(null, null, "sub_dita1.dita", null)),
                        chunk(TO_CONTENT, null, "dita2.dita", "dita2.dita",
                                chunk(null, null, "sub_dita2.dita", null))
                );
    }

    @Test
    public void testchunk_hogs_memory() {
        new TestBuilder("chunk_hogs_memory")
                .map("map.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "dita1.dita#AAA", "dita1.dita#AAA",
                                chunk(null, null, "sub_dita1.dita", null,
                                        chunk(null, null, "sub_dita2.dita", null))),
                        chunk(TO_CONTENT, null, "dita1.dita#B", "dita1.dita#B"),
                        chunk(TO_CONTENT, null, "dita1.dita#C", "dita1.dita#C"),
                        chunk(TO_CONTENT, null, "dita1.dita#D", "dita1.dita#D")
                );
    }

    @Test
    public void testchunk_map_tocontent() {
        new TestBuilder("chunk_map_tocontent")
                .map("chunk_map_tocontent/map_chunk_source.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "chunk_map_tocontent/dita1.dita", "chunk_map_tocontent/dita1.dita",
                                chunk(null, null, "chunk_map_tocontent/sub_dita1.dita", null)),
                        chunk(TO_CONTENT, null, "chunk_map_tocontent/dita2.dita", "chunk_map_tocontent/dita2.dita",
                                chunk(null, null, "chunk_map_tocontent/sub_dita2.dita", null))
                );
    }

    @Test
    public void testchunk_rewrite_tocontent() {
        new TestBuilder("chunk_rewrite_tocontent")
                .map("chunk_rewrite_topicID.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "dita1.dita", "dita1.dita",
                                chunk(null, null, "dita2.dita", null),
                                chunk(null, null, "dita3.dita", null)),
                        chunk(TO_CONTENT, null, "dita4.dita", "dita4.dita"),
                        chunk(TO_CONTENT, null, "dita5.dita", "dita5.dita",
                                chunk(null, null, "dita6.dita", null))
                );
    }

    @Test
    public void testconflict_by_topic() {
        new TestBuilder("conflict_by_topic")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "nested1.dita#N1", null,
                                chunk(BY_TOPIC, null, "nested1.dita#N1a", null)),
                        chunk(BY_TOPIC, null, "nested4.dita#N1", null,
                                chunk(BY_TOPIC, null, "nested4.dita#N1a", null))
                );
    }

    @Test
    public void testconflict_same_id() {
        new TestBuilder("conflict_same_id")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "t1.dita", "t1.dita",
                                chunk(null, null, "t2.dita", null))
                );
    }

    @Test
    public void testconflict_to_content() {
        new TestBuilder("conflict_to_content")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "map1.dita", "map1.dita",
                                chunk(null, null, "nested1.dita", null)),
                        chunk(TO_CONTENT, null, "t1.dita", "map5.dita",
                                chunk(null, null, "t2.dita", null))
                );
    }

    @Test
    public void testcopy_to1() {
        new TestBuilder("copy_to1")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(TO_CONTENT, SELECT_BRANCH, "ditabase.dita#X", "document.dita"),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(TO_CONTENT, SELECT_DOCUMENT, "ditabase.dita#Z", "Z.dita")
                );
    }

    @Test
    public void testcopy_to2() {
        new TestBuilder("copy_to2")
                .map("map2.ditamap")
                .run()
                .assertEquals(
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null))),
                        chunk(BY_TOPIC, null, "ditabase.dita#", null,
                                chunk(BY_TOPIC, null, "ditabase.dita#X", null),
                                chunk(BY_TOPIC, null, "ditabase.dita#Y", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y1", null,
                                                chunk(BY_TOPIC, null, "ditabase.dita#Y1a", null)),
                                        chunk(BY_TOPIC, null, "ditabase.dita#Y2", null)),
                                chunk(BY_TOPIC, null, "ditabase.dita#Z", null,
                                        chunk(BY_TOPIC, null, "ditabase.dita#Z1", null)))
                );
    }

    @Test
    public void testexternal_chunk() {
        new TestBuilder("external_chunk")
                .map("test_chunk.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "dita1.dita#ditatask111", "dita1.dita#ditatask111",
                                chunk(null, null, "http://w3.ibm.com/", null))
                );
    }

    @Test
    public void testlink1() {
        new TestBuilder("link1")
                .map("map1.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "t1.dita", "t1.dita",
                                chunk(null, null, "ditabase.dita#topic", null))
                );
    }

    @Test
    public void testlink2() {
        new TestBuilder("link2")
                .map("map2.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "t3.dita", "t3.dita",
                                chunk(null, null, "ditabase.dita", null))
                );
    }

    // FIXME
    @Ignore
    @Test
    public void testtopicgroup_chunk() {
        new TestBuilder("topicgroup_chunk")
                .map("topicgroup_chunk.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "dita1.dita", "dita1.dita"),
                        chunk(TO_CONTENT, null, "dita2.dita", "dita2.dita",
                                chunk(null, null, "dita3.dita", null,
                                        chunk(null, null, "dita4.dita", null,
                                                chunk(null, null, "dita5.dita", null,
                                                        chunk(null, null, "dita6.dita", null))))),
                        chunk(TO_CONTENT, null, "dita1.dita", "dita1.dita"),
                        chunk(TO_CONTENT, null, "dita2.dita", "dita2.dita"),
                        chunk(TO_CONTENT, null, null, "Chunk0.dita",
                                chunk(null, null, "dita7.dita", null)),
                        chunk(TO_CONTENT, null, "dita8.dita", "dita8.dita",
                                chunk(null, null, "Chunk1.dita", null,
                                        chunk(null, null, "dita9.dita", null))),
                        chunk(TO_CONTENT, null, "container.dita", "container.dita"),
                        chunk(TO_CONTENT, null, null, "Chunk2.dita",
                                chunk(null, null, "groupkid.dita", null)),
                        chunk(TO_CONTENT, null, null, "Chunk3.dita",
                                chunk(null, null, "headkid.dita", null)),
                        chunk(TO_CONTENT, null, null, "Chunk4.dita",
                                chunk(null, null, "shortkid.dita", null))
                );
    }

    @Test
    public void testunware_chunk_content() {
        new TestBuilder("unware_chunk_content")
                .map("maplink.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "one.dita", "one.dita",
                                chunk(null, null, "one.dita", null,
                                        chunk(null, null, "one.dita", null,
                                                chunk(null, null, "one.dita", null))))
                );
    }

    @Test
    public void testunware_chunk_content2() {
        new TestBuilder("unware_chunk_content2")
                .map("maplink.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "one.dita", "one.dita",
                                chunk(null, null, "one.dita", null,
                                        chunk(null, null, "one.dita", null,
                                                chunk(null, null, "one.dita", null))))
                );
    }

    @Test
    public void testwith_non_dita() {
        new TestBuilder("with_non_dita")
                .map("withnondita.ditamap")
                .run()
                .assertEquals(
                        chunk(TO_CONTENT, null, "withnondita.dita", "withnondita.dita",
                                chunk(null, null, "thisisdita.dita", null,
                                        chunk(null, null, "thisistext.txt", null)))
                );
    }

    @Ignore
    @Test
    public void generate() throws IOException {
        final Path base = Paths.get("/Users/jelovirt/Work/dita-ot/out/test/resources/ChunkModuleTest/src");
        Files.list(base).sorted().forEach(dir -> {
            try {
                final Path map = Files.list(dir)
                        .filter(file -> file.getFileName().toString().endsWith(".ditamap"))
                        .findFirst()
                        .map(Path::getFileName)
                        .orElse(Paths.get(dir.getFileName() + ".ditamap"));
                System.out.println("@Test");
                System.out.println("public void test" + dir.getFileName() + "() {");
                System.out.println("    new TestBuilder(\"" + dir.getFileName() + "\")");
                System.out.println("            .map(\"" + map + "\")");
                System.out.println("            .run()");
                System.out.println("            .assertEquals(");
                final TestResult act = new TestBuilder(dir.getFileName().toString())
                        .map(map.toString())
                        .run();
                System.out.println(act.act.stream().map(ChunkOperation::toString).collect(Collectors.joining(",\n")));
                System.out.println("            );");
                System.out.println("}\n");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @After
    public void teardown() throws IOException {
        TestUtils.forceDelete(tempBaseDir);
    }

    private class TestBuilder {
        private String testCase;
        private String map;

        private TestBuilder(final String testCase) {
            this.testCase = testCase;
        }

        private TestBuilder map(final String map) {
            this.map = map;
            return this;
        }

        private TestResult run() {
            try {
                final String testName = FilenameUtils.getBaseName(testCase);
                final File srcDir = new File(srcBaseDir, testName);
                final File tempDir = new File(tempBaseDir, testName);
                final File expDir = new File(expBaseDir, testName);
                TestUtils.copy(srcDir, tempDir);

                final ChunkMapFilter mapFilter = new ChunkMapFilter();
                final CachingLogger logger = new CachingLogger(true);
                mapFilter.setLogger(logger);
                final Job job = new Job(tempDir);
                mapFilter.setJob(job);

                final File inputFile = new File(tempDir, map != null ? map : testCase);
                mapFilter.read(inputFile);

                final List<ChunkOperation> act = simplify(mapFilter.changes, tempDir.toURI());
//                assertEquals(Arrays.asList(exp), act);
//            compare(tempDir, expDir);

                logger.getMessages().stream()
                        .filter(m -> m.level == CachingLogger.Message.Level.ERROR)
                        .forEach(m -> System.err.println(m.level + ": " + m.message));
                return new TestResult(act);
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }

        private List<ChunkOperation> simplify(final List<ChunkOperation> changes, final URI tempDir) {
            return changes.stream()
                    .map(c -> new ChunkOperation(
                            c.operation,
                            c.select,
                            c.src != null ? tempDir.relativize(c.src) : null,
                            c.dst != null ? tempDir.relativize(c.dst) : null,
                            simplify(c.children, tempDir)
                    ))
                    .collect(Collectors.toList());
        }
    }

    private static class TestResult {
        public final List<ChunkOperation> act;

        private TestResult(List<ChunkOperation> act) {
            this.act = act;
        }

        private void assertEquals(final ChunkOperation... exp) {
            Assert.assertEquals(
                    Arrays.asList(exp).stream().map(c -> toCode(c, "")).collect(Collectors.joining(",\n")),
                    act.stream().map(c -> toCode(c, "")).collect(Collectors.joining(",\n"))
            );
        }

        public String toCode(final ChunkOperation c, final String indent) {
            final StringBuilder buf = new StringBuilder()
                    .append(indent)
                    .append("chunk(")
                    .append(c.operation)
                    .append(", ")
                    .append(c.select)
                    .append(", ")
                    .append(c.src != null ? ("\"" + c.src + "\"") : null)
                    .append(", ")
                    .append(c.dst != null ? ("\"" + c.dst + "\"") : null);
            for (ChunkOperation child : c.children) {
                buf.append(",\n").append(toCode(child, indent + "  "));
            }
            buf.append(')');
            return buf.toString();
        }
    }

    private ChunkOperation chunk(final ChunkOperation.Operation operation,
                                 final ChunkOperation.Operation select,
                                 final String src,
                                 final String dst,
                                 final ChunkOperation... children) {
        return new ChunkOperation(
                operation,
                select,
                src != null ? URI.create(src) : null,
                dst != null ? URI.create(dst) : null,
                Arrays.asList(children)
        );
    }

}
