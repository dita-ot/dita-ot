package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.KeyrefReader;
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
		//Added by Alan Date:2009-08-04 --begin
		String ext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
		String extName = ext.startsWith(Constants.DOT) ? ext : (Constants.DOT + ext);
		//Added by Alan Date:2009-08-04 --end
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
		//get files which have keyref attr
		String[] parseList = ((String)properties.get(Constants.KEYREF_LIST)).split(Constants.COMMA);
		// maps of keyname and target 
		Map<String, String> keymap =new HashMap<String, String>();
		// store the key name defined in a map
		Hashtable<String, HashSet<String>> maps = new Hashtable<String, HashSet<String>>();
		
		// get the key definitions from the dita.list, and the ditamap where it is defined
		// are not handle yet.
		if(!((String)properties.get(Constants.KEY_LIST)).equals(Constants.STRING_EMPTY)){
			String[] keys = ((String)properties.get(Constants.KEY_LIST)).split(Constants.COMMA);
			for(String key: keys){
				keymap.put(key.substring(0, key.indexOf(Constants.EQUAL)), 
						key.substring(key.indexOf(Constants.EQUAL)+1, key.lastIndexOf("(")));
				// map file which define the keys
				String map = key.substring(key.lastIndexOf("(") + 1, key.lastIndexOf(")"));
				// put the keyname into corresponding map which defines it.
				//a map file can define many keys
				if(maps.containsKey(map)){
					maps.get(map).add(key.substring(0,key.indexOf(Constants.EQUAL)));
				}else{
					HashSet<String> set = new HashSet<String>();
					set.add(key.substring(0, key.indexOf(Constants.EQUAL)));
					maps.put(map, set);
				}
			}
		}
		KeyrefReader reader = new KeyrefReader();
		reader.setTempDir(tempDir);
		for(String mapFile: maps.keySet()){
			reader.setKeys(maps.get(mapFile));
			reader.read(mapFile);
		}		
		Content content = reader.getContent();
//		content.setValue(keymap);
		for(String file: parseList){
			KeyrefPaser parser = new KeyrefPaser();
			parser.setContent(content);
			parser.setTempDir(tempDir);
			parser.setKeyMap(keymap);		
			//Added by Alan Date:2009-08-04
			parser.setExtName(extName);
			parser.write(file);
			
		}
		return null;
	}

}
