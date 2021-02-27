/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.module.AbstractModuleTest;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.Store;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

@RunWith(Parameterized.class)
public class ChunkModuleTest extends AbstractModuleTest {
    @Parameters(name = "{0} {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"combine.ditamap", Collections.emptyMap()},
                {"override.ditamap", ImmutableMap.of("root-chunk-override", "combine")},
                {"dita.ditamap", Collections.emptyMap()},
                {"link.ditamap", Collections.emptyMap()},
                {"uplevels.ditamap", Collections.emptyMap()},
                {"format.ditamap", Collections.emptyMap()},
                {"nested.ditamap", Collections.emptyMap()},
                {"scope.ditamap", Collections.emptyMap()},
                {"topicgroup.ditamap", Collections.emptyMap()},
                {"topichead.ditamap", Collections.emptyMap()},
                {"multiple.ditamap", Collections.emptyMap()},
                {"map.ditamap", Collections.emptyMap()}
        });
    }

    public ChunkModuleTest(final String testCase, final Map<String, String> params) {
        super(testCase, params);
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
