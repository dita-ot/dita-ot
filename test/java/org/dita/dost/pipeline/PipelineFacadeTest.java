package org.dita.dost.pipeline;

import static org.junit.Assert.*;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModule;
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
