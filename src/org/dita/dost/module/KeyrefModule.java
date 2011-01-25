/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
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
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.KeyrefPaser;
/**
 * Keyref Module.
 *
 */
public class KeyrefModule implements AbstractPipelineModule {
	
	/**
	 * Entry point of KeyrefModule.
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		String tempDir = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		
		//Added by William on 2010-03-30 for bug:2978858 start
		//get basedir
		String baseDir = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		//Added by William on 2010-03-30 for bug:2978858 end
		
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(baseDir, tempDir).getAbsolutePath();
		}
		
		//Added by Alan Date:2009-08-04 --begin
		String ext = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAEXT);
		String extName = ext.startsWith(Constants.DOT) ? ext : (Constants.DOT + ext);
		//Added by Alan Date:2009-08-04 --end
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(Exception e){
			DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
			javaLogger.logException(e);
		}

		// maps of keyname and target 
		Map<String, String> keymap =new HashMap<String, String>();
		// store the key name defined in a map(keyed by ditamap file)
		Hashtable<String, HashSet<String>> maps = new Hashtable<String, HashSet<String>>();
		
		// get the key definitions from the dita.list, and the ditamap where it is defined
		// are not handle yet.
		String keylist = properties.getProperty(Constants.KEY_LIST);
		if(!StringUtils.isEmptyString(keylist)){
			Set<String> keys = StringUtils.restoreSet(keylist);
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
		//get files which have keyref attr
		Set<String> parseList = StringUtils.restoreSet(properties.getProperty(Constants.KEYREF_LIST));
		//Conref Module will change file's content, it is possible that tags with @keyref are copied in
		//while keyreflist is hard update with xslt.
		//bug:3056939
		Set<String> conrefList = StringUtils.restoreSet(properties.getProperty(Constants.CONREF_LIST));
		parseList.addAll(conrefList);
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
