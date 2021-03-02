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

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

@RunWith(Parameterized.class)
public class ChunkModuleTest extends AbstractModuleTest {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"case1"},
                {"case2"},
                {"case3"},
                {"case4"},
                {"case5"},
                {"case6"},
                {"case7"},
                {"conflict_by_topic"},
                {"copy_to1"},
                {"copy_to2"},
                {"conflict_same_id"},
                {"conflict_to_content"},
                {"external_chunk"},
                {"link1"},
                {"link2"},
                {"anchor1"},
                {"anchor2"},
//                {"Attribute_map1"},
                {"Attribute_map2"},
                {"Attribute_map3"},
                {"Attribute_map4"},
                {"Attribute_map5"},
                {"Attribute_map6"},
                {"Attribute_map7"},
                {"Attribute_map8"},
                {"Attribute_map9"},
                {"Attribute_map10"},
                {"Attribute_map11"},
                {"ByTopic_map2"},
                {"ByTopic_map3"},
                {"ByTopic_map4"},
                {"ByTopic_map5"},
                {"ByTopic_map6"},
                {"ByTopic_map7"},
                {"ByTopic_batseparate0"},
                {"FixChunk_map1"},
                {"FixChunk_map2"},
                {"FixChunk_map3"},
                {"FixChunk_map4"},
                {"FixChunk_map5"},
                {"FixChunk_map6"},
                {"FixChunk_map7"},
                {"FixChunk_map8"},
                {"chunk_duplicate_tocontent"},
                {"chunk_hogs_memory"},
                {"chunk_map_tocontent"},
                {"chunk_rewrite_tocontent"},
                {"topicgroup_chunk"},
                {"unware_chunk_content"},
                {"unware_chunk_content2"},
                {"with_non_dita"}
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
