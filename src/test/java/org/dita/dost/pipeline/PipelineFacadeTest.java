/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.pipeline;

import static org.junit.Assert.*;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.DummyPipelineModule;
import org.junit.Test;

/**
 * Unit test for {@link PipelineFacade}
 * 
 * @author Jarno Elovirta
 */
public class PipelineFacadeTest {

    @Test
    public void testExecute() throws DITAOTException {
        final AbstractPipelineInput i = new PipelineHashIO();
        final AbstractFacade p = new PipelineFacade();
        final AbstractPipelineOutput act = p.execute("DummyPipeline", i);
        assertSame(DummyPipelineModule.exp, act);
    }

}
