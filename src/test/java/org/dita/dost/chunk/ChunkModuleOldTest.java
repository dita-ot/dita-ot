/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import org.dita.dost.module.AbstractModuleTest;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.ChunkModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

@RunWith(Parameterized.class)
public class ChunkModuleOldTest extends AbstractModuleTest {
    @Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"combine.ditamap"},
                {"dita.ditamap"},
                {"link.ditamap"},
                {"uplevels.ditamap"},
                {"format.ditamap"},
                {"nested.ditamap"},
                {"scope.ditamap"},
                {"topicgroup.ditamap"},
                {"topichead.ditamap"},
                {"map.ditamap"}
        });
    }

    public ChunkModuleOldTest(final String testCase) {
        super(testCase);
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
