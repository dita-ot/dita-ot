package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

/**
 * Dummy pipeline module for testing. 
 * 
 * @author Jarno Elovirta
 */
public class DummyPipelineModule implements AbstractPipelineModule {
	
	public static final AbstractPipelineOutput exp = new AbstractPipelineOutput() {};
	
	/**
	 * @return always returns {@link #exp}
	 */
	@Override
	public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
		return exp;
	}
	
}