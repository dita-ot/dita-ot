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
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.ConrefPushParser;
/**
 * Conref push module.
 * 
 *
 */
public class ConrefPushModule implements AbstractPipelineModule {

	/**
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(AbstractPipelineInput)
	 * @param input input
	 * @return output
	 * @throws DITAOTException exception
	 */
	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		String basedir = ((PipelineHashIO) input)
		.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(basedir, tempDir).getAbsolutePath();
		}
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(IOException e){
			DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
			javaLogger.logException(e);
		}

		Set<String> conrefpushlist = StringUtils.restoreSet(properties.getProperty(Constants.CONREF_PUSH_LIST));
		ConrefPushReader reader = new ConrefPushReader();
		for(String fileName:conrefpushlist){
			//FIXME: this reader calculate parent directory
			reader.read(new File(tempDir,fileName).getAbsolutePath());
		}
		
		Set<Map.Entry<String, Hashtable<String, String>>> pushSet = (Set<Map.Entry<String, Hashtable<String,String>>>) reader.getContent().getCollection();
		Iterator<Map.Entry<String, Hashtable<String,String>>> iter = pushSet.iterator();
		
		while(iter.hasNext()){
			Map.Entry<String, Hashtable<String,String>> entry = iter.next();
			ConrefPushParser parser = new ConrefPushParser();
			Content content = new ContentImpl();
			content.setValue(entry.getValue());
			parser.setContent(content);
			//pass the tempdir to ConrefPushParser
			parser.setTempDir(tempDir);
			//FIXME:This writer creates and renames files, have to 
			parser.write(entry.getKey());
		}
		
		return null;
	}

}
