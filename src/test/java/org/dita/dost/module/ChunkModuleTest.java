/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

public class ChunkModuleTest extends AbstractModuleTest {

    @Override
    AbstractPipelineInput getAbstractPipelineInput() {
        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
        return input;
    }

    @Override
    AbstractPipelineModule getModule(final File tempDir) {
        return new ChunkModule();
    }

    @Test
    public void testCase1() {
        test("case1.ditamap");
    }

    @Test
    public void testCase2() {
        test("case2.ditamap");
    }

    @Test
    public void testCase3() {
        test("case3.ditamap");
    }

    @Test
    public void testCase4() {
        test("case4.ditamap");
    }

    @Test
    public void testCase5() {
        test("case5.ditamap");
    }

    @Test
    public void testCase6() {
        test("case6.ditamap");
    }

    @Test
    public void testCase7() {
        test("case7.ditamap");
    }

    @Test
    public void testConflictByTopic() {
        test("conflict_by_topic.ditamap");
    }

    @Test
    public void testCopyTo1() {
        test("copy_to1.ditamap");
    }

    @Test
    public void testCopyTo2() {
        test("copy_to2.ditamap");
    }

    @Test
    public void testConflictSameId() {
        test("conflict_same_id.ditamap");
    }

    @Test
    public void testConflictToContent() {
        test("conflict_to_content.ditamap");
    }

    @Test
    public void testExternalChunk() {
        test("external_chunk.ditamap");
    }

    @Test
    public void testLink1() {
        test("link1.ditamap");
    }

    @Test
    public void testLink2() {
        test("link2.ditamap");
    }

    @Test
    public void testAnchor1() {
        test("anchor1.ditamap");
    }

    @Test
    public void testAnchor2() {
        test("anchor2.ditamap");
    }

    @Test
    @Ignore
    // FIXME
    public void testAttributeMap1() {
        test("Attribute_map1.ditamap");
    }

    @Test
    public void testAttributeMap2() {
        test("Attribute_map2.ditamap");
    }

    @Test
    public void testAttributeMap3() {
        test("Attribute_map3.ditamap");
    }

    @Test
    public void testAttributeMap4() {
        test("Attribute_map4.ditamap");
    }

    @Test
    public void testAttributeMap5() {
        test("Attribute_map5.ditamap");
    }

    @Test
    public void testAttributeMap6() {
        test("Attribute_map6.ditamap");
    }

    @Test
    public void testAttributeMap7() {
        test("Attribute_map7.ditamap");
    }

    @Test
    public void testAttributeMap8() {
        test("Attribute_map8.ditamap");
    }

    @Test
    public void testAttributeMap9() {
        test("Attribute_map9.ditamap");
    }

    @Test
    public void testAttributeMap10() {
        test("Attribute_map10.ditamap");
    }

    @Test
    public void testAttributeMap11() {
        test("Attribute_map11.ditamap");
    }

    @Test
    public void testByTopicMap2() {
        test("ByTopic_map2.ditamap");
    }

    @Test
    public void testByTopicMap3() {
        test("ByTopic_map3.ditamap");
    }

    @Test
    public void testByTopicMap4() {
        test("ByTopic_map4.ditamap");
    }

    @Test
    public void testByTopicMap5() {
        test("ByTopic_map5.ditamap");
    }

    @Test
    public void testByTopicMap6() {
        test("ByTopic_map6.ditamap");
    }

    @Test
    public void testByTopicMap7() {
        test("ByTopic_map7.ditamap");
    }

    @Test
    public void testByTopicBatseparate0() {
        test("ByTopic_batseparate0.ditamap");
    }

    @Test
    public void testFixChunkMap1() {
        test("FixChunk_map1.ditamap");
    }

    @Test
    public void testFixChunkMap2() {
        test("FixChunk_map2.ditamap");
    }

    @Test
    public void testFixChunkMap3() {
        test("FixChunk_map3.ditamap");
    }

    @Test
    public void testFixChunkMap4() {
        test("FixChunk_map4.ditamap");
    }

    @Test
    public void testFixChunkMap5() {
        test("FixChunk_map5.ditamap");
    }

    @Test
    public void testFixChunkMap6() {
        test("FixChunk_map6.ditamap");
    }

    @Test
    public void testFixChunkMap7() {
        test("FixChunk_map7.ditamap");
    }

    @Test
    public void testFixChunkMap8() {
        test("FixChunk_map8.ditamap");
    }

    @Test
    public void testChunkDuplicateToContent() {
        test("chunk_duplicate_tocontent.ditamap");
    }

    @Test
    public void testChunkHogsMemory() {
        test("chunk_hogs_memory.ditamap");
    }

    @Test
    public void testChunkMapToContent() {
        test("chunk_map_tocontent.ditamap");
    }

    @Test
    public void testChunkRewriteToContent() {
        test("chunk_rewrite_tocontent.ditamap");
    }

    @Test
    public void testTopicgroupChunk() {
        test("topicgroup_chunk.ditamap");
    }

    @Test
    public void testUnwareChunkContent() {
        test("unware_chunk_content.ditamap");
    }

    @Test
    public void testUnwareChunkContent2() {
        test("unware_chunk_content2.ditamap");
    }
    
    @Test
    public void testWith_non_dita() {
        test("with_non_dita.ditamap");
    }

    @Test
    public void testto_content_with_namespace_mathml() {test("to_content_with_namespace_mathml.ditamap");}

    @Test

    public void testto_content_with_namespace_xsi() {test("to_content_with_namespace_xsi.ditamap");}

}
