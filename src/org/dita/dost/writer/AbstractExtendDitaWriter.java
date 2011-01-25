/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.pipeline.PipelineHashIO;

//RFE 2987769 Eclipse index-see

public abstract class AbstractExtendDitaWriter implements IExtendDitaWriter {
	
	private PipelineHashIO pipelineHashMap = null;


	public PipelineHashIO getPipelineHashIO() {
		
		return pipelineHashMap;
	}


	public void setPipelineHashIO(PipelineHashIO hashIO) {
		pipelineHashMap = hashIO;
		
	}

}
