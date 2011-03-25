/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.KeyrefPaser;
/**
 * Keyref Module.
 *
 */
final class KeyrefModule implements AbstractPipelineModule {
	
    private DITAOTLogger logger;
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
	/**
	 * Entry point of KeyrefModule.
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
	public AbstractPipelineOutput execute(final AbstractPipelineInput input)
			throws DITAOTException {
	    if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
		String tempDir = input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR);
		
		//Added by William on 2010-03-30 for bug:2978858 start
		//get basedir
		final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
		//Added by William on 2010-03-30 for bug:2978858 end
		
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(baseDir, tempDir).getAbsolutePath();
		}
		
		//Added by Alan Date:2009-08-04 --begin
		final String ext = input.getAttribute(ANT_INVOKER_PARAM_DITAEXT);
		final String extName = ext.startsWith(DOT) ? ext : (DOT + ext);
		//Added by Alan Date:2009-08-04 --end
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(final Exception e){
			logger.logException(e);
		}

		// maps of keyname and target 
		final Map<String, String> keymap =new HashMap<String, String>();
		// store the key name defined in a map(keyed by ditamap file)
		final Hashtable<String, HashSet<String>> maps = new Hashtable<String, HashSet<String>>();
		
		// get the key definitions from the dita.list, and the ditamap where it is defined
		// are not handle yet.
		final String keylist = properties.getProperty(KEY_LIST);
		if(!StringUtils.isEmptyString(keylist)){
			final Set<String> keys = StringUtils.restoreSet(keylist);
			for(final String key: keys){
				keymap.put(key.substring(0, key.indexOf(EQUAL)), 
						key.substring(key.indexOf(EQUAL)+1, key.lastIndexOf("(")));
				// map file which define the keys
				final String map = key.substring(key.lastIndexOf("(") + 1, key.lastIndexOf(")"));
				// put the keyname into corresponding map which defines it.
				//a map file can define many keys
				if(maps.containsKey(map)){
					maps.get(map).add(key.substring(0,key.indexOf(EQUAL)));
				}else{
					final HashSet<String> set = new HashSet<String>();
					set.add(key.substring(0, key.indexOf(EQUAL)));
					maps.put(map, set);
				}
			}
		}
		final KeyrefReader reader = new KeyrefReader();
		reader.setLogger(logger);
		reader.setTempDir(tempDir);
		for(final String mapFile: maps.keySet()){
			reader.setKeys(maps.get(mapFile));
			reader.read(mapFile);
		}		
		final Content content = reader.getContent();
		//get files which have keyref attr
		final Set<String> parseList = StringUtils.restoreSet(properties.getProperty(KEYREF_LIST));
		//Conref Module will change file's content, it is possible that tags with @keyref are copied in
		//while keyreflist is hard update with xslt.
		//bug:3056939
		final Set<String> conrefList = StringUtils.restoreSet(properties.getProperty(CONREF_LIST));
		parseList.addAll(conrefList);
		for(final String file: parseList){
			final KeyrefPaser parser = new KeyrefPaser();
			parser.setLogger(logger);
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
