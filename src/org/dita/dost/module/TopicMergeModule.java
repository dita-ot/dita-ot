package org.dita.dost.module;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MergeMapParser;
import org.dita.dost.util.Constants;

public class TopicMergeModule extends AbstractPipelineModule {

	public TopicMergeModule() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		// TODO Auto-generated method stub
		String ditaInput = ((PipelineHashIO) input)
		.getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);
		String style = ((PipelineHashIO) input)
		.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_STYLE);
		String out = ((PipelineHashIO) input)
		.getAttribute(Constants.ANT_INVOKER_EXT_PARAM_OUTPUT);
		OutputStreamWriter output = null;
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		
		if (ditaInput == null || !new File(ditaInput).exists()){
			logger.logError(MessageUtils.getMessage("DOTJ025E").toString());
			return null;
		}
		
		if ( out == null ){
			logger.logError(MessageUtils.getMessage("DOTJ026E").toString());
			return null;
		}

		MergeMapParser mapParser = new MergeMapParser();
		mapParser.read(ditaInput);
		String midResult = new StringBuffer(Constants.XML_HEAD).append("<dita-merge>")
			.append(((StringBuffer)mapParser.getContent().getValue())).append("</dita-merge>").toString();
		StringReader midStream = new StringReader(midResult);
		
		try{
			if (style != null){
				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer(new StreamSource(style));
				transformer.transform(new StreamSource(midStream), new StreamResult(out));
			}else{
				output = new OutputStreamWriter(new FileOutputStream(out),Constants.UTF8);
				output.write(midResult);
				output.flush();
				output.close();
			}
		}catch (Exception e){
			//use java logger to log the exception
			logger.logException(e);
		}
		
		return null;
	}

}
