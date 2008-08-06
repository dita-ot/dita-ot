package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.KeyrefPaser;

public class KeyrefModule implements AbstractPipelineModule {

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		String tempDir = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		Properties properties = new Properties();
		DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(tempDir).getAbsolutePath();
		}
		
		File ditafile = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
		File ditaxmlfile = new File(tempDir, Constants.FILE_NAME_DITA_LIST_XML);
		
		try{
		if(ditaxmlfile.exists()){
			properties.loadFromXML(new FileInputStream(ditaxmlfile));
		}else{
			properties.load(new FileInputStream(ditafile));
		}
		}catch (Exception e) {
			javaLogger.logException(e);
		}
		
		String[] parseList = ((String)properties.get(Constants.KEYREF_LIST)).split(Constants.COMMA);
		Map<String, String> keymap =new HashMap<String, String>();
		// get the key definitions from the dita.list, and the ditamap where it is defined
		// are not handle yet.
		if(!((String)properties.get(Constants.KEY_LIST)).equals(Constants.STRING_EMPTY)){
			String[] keys = ((String)properties.get(Constants.KEY_LIST)).split(Constants.COMMA);
			for(String key: keys){
				keymap.put(key.substring(0, key.indexOf(Constants.EQUAL)), 
						key.substring(key.indexOf(Constants.EQUAL)+1, key.indexOf("(")));
			}
		}
		Content content = new ContentImpl();
		content.setValue(keymap);
		for(String file: parseList){
			KeyrefPaser parser = new KeyrefPaser();
			parser.setContent(content);
			parser.setTempDir(tempDir);
			parser.write(file);
		}
		return null;
	}

}
