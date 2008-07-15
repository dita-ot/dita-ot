package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.ConrefPushParser;

public class ConrefPushModule implements AbstractPipelineModule {

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		// TODO Auto-generated method stub
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		Properties properties = null;
		DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
		ConrefPushReader reader = new ConrefPushReader();
		
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(tempDir).getAbsolutePath();
		}
		
		File ditafile = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
		File ditaxmlfile = new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
		
		try{
		if(ditaxmlfile.exists()){
			properties.load(new FileInputStream(ditaxmlfile));
		}else{
			properties.load(new FileInputStream(ditafile));
		}
		}catch (Exception e) {
			javaLogger.logException(e);
		}
		
		StringTokenizer parseList = new StringTokenizer(properties.getProperty("conrefpushlist"),Constants.COMMA);
		
		while (parseList.hasMoreElements()){
			String fileName = (String)parseList.nextElement();
			reader.read(new File(tempDir,fileName).getAbsolutePath());
		}
		HashSet<Map.Entry<String, Hashtable>> pushSet = (HashSet<Map.Entry<String, Hashtable>>) reader.getContent().getCollection();
		Iterator<Map.Entry<String, Hashtable>> iter = pushSet.iterator();
		
		while(iter.hasNext()){
			Map.Entry<String, Hashtable> entry = iter.next();
			ConrefPushParser parser = new ConrefPushParser();
			Content content = new ContentImpl();
			content.setValue(entry.getValue());
			parser.setContent(content);
			
			parser.write(entry.getKey());
		}
		
		return null;
	}

}
