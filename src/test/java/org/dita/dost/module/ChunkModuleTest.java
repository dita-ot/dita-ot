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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

@RunWith(Parameterized.class)
public class ChunkModuleTest extends AbstractModuleTest {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"case1.ditamap"},
                {"case2.ditamap"},
                {"case3.ditamap"},
                {"case4.ditamap"},
                {"case5.ditamap"},
                {"case6.ditamap"},
                {"case7.ditamap"},
                {"conflict_by_topic.ditamap"},
                {"copy_to1.ditamap"},
                {"copy_to2.ditamap"},
                {"conflict_same_id.ditamap"},
                {"conflict_to_content.ditamap"},
                {"external_chunk.ditamap"},
                {"link1.ditamap"},
                {"link2.ditamap"},
                {"anchor1.ditamap"},
                {"anchor2.ditamap"},
//                {"Attribute_map1.ditamap"},
                {"Attribute_map2.ditamap"},
                {"Attribute_map3.ditamap"},
                {"Attribute_map4.ditamap"},
                {"Attribute_map5.ditamap"},
                {"Attribute_map6.ditamap"},
                {"Attribute_map7.ditamap"},
                {"Attribute_map8.ditamap"},
                {"Attribute_map9.ditamap"},
                {"Attribute_map10.ditamap"},
                {"Attribute_map11.ditamap"},
                {"ByTopic_map2.ditamap"},
                {"ByTopic_map3.ditamap"},
                {"ByTopic_map4.ditamap"},
                {"ByTopic_map5.ditamap"},
                {"ByTopic_map6.ditamap"},
                {"ByTopic_map7.ditamap"},
                {"ByTopic_batseparate0.ditamap"},
                {"FixChunk_map1.ditamap"},
                {"FixChunk_map2.ditamap"},
                {"FixChunk_map3.ditamap"},
                {"FixChunk_map4.ditamap"},
                {"FixChunk_map5.ditamap"},
                {"FixChunk_map6.ditamap"},
                {"FixChunk_map7.ditamap"},
                {"FixChunk_map8.ditamap"},
                {"chunk_duplicate_tocontent.ditamap"},
                {"chunk_hogs_memory.ditamap"},
                {"chunk_map_tocontent.ditamap"},
                {"chunk_rewrite_tocontent.ditamap"},
                {"topicgroup_chunk.ditamap"},
                {"unware_chunk_content.ditamap"},
                {"unware_chunk_content2.ditamap"},
                {"with_non_dita.ditamap"}
        });
    }

    public ChunkModuleTest(final String testCase) {
        super(testCase, Collections.emptyMap());
    }

    @Override
    protected AbstractPipelineInput getAbstractPipelineInput() {
        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
        return input;
    }

    @Override
    protected AbstractPipelineModule getModule(final File tempDir) {
        return new ChunkModule();
    }
}
