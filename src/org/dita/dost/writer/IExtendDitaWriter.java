package org.dita.dost.writer;

import org.dita.dost.pipeline.PipelineHashIO;

//RFE 2987769 Eclipse index-see
public interface IExtendDitaWriter {
	
	public PipelineHashIO getPipelineHashIO ();
	
	public void setPipelineHashIO (PipelineHashIO pipelineHashIO);

}
