/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

/**
 * @author Lian, Li
 * 
 */
public abstract class AbstractPipelineModule {

    public abstract AbstractPipelineOutput execute(AbstractPipelineInput input);

}
