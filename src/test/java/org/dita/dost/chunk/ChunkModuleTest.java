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
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.junit.Test;

import java.io.File;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

public class ChunkModuleTest extends AbstractModuleTest {
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

    @Test
    public void combine() {
        test("combine.ditamap");
    }

    @Test
    public void link() {
        test("link.ditamap");
    }

    @Test
    public void uplevels() {
        test("uplevels.ditamap");
    }

    @Test
    public void format() {
        test("format.ditamap");
    }

    @Test
    public void topicgroup() {
        test("topicgroup.ditamap");
    }

    @Test
    public void topichead() {
        test("topichead.ditamap");
    }

    @Test
    public void map() {
        test("map.ditamap");
    }
}
