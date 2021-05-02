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
    @Parameters(name = "{0} {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"combine", Collections.emptyMap()},
                {"duplicate", Collections.emptyMap()},
                {"override", ImmutableMap.of("root-chunk-override", "combine")},
                {"dita", Collections.emptyMap()},
                {"link", Collections.emptyMap()},
                {"uplevels", Collections.emptyMap()},
                {"uplevels-dir", Collections.emptyMap()},
                {"uplevels-root", Collections.emptyMap()},
                {"uplevels-parallel", Collections.emptyMap()},
                {"format", Collections.emptyMap()},
                {"nested", Collections.emptyMap()},
                {"scope", Collections.emptyMap()},
                {"topicgroup", Collections.emptyMap()},
                {"topichead", Collections.emptyMap()},
                {"multiple", Collections.emptyMap()},
                {"map", Collections.emptyMap()}
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
