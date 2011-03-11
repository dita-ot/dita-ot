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
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
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
final class ConrefPushModule implements AbstractPipelineModule {

    private DITAOTLogger logger;
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
	/**
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(AbstractPipelineInput)
	 * @param input input
	 * @return output
	 * @throws DITAOTException exception
	 */
	public AbstractPipelineOutput execute(final AbstractPipelineInput input)
			throws DITAOTException {
	    if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
		String tempDir = input.getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		final String basedir = input
		.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(basedir, tempDir).getAbsolutePath();
		}
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(final IOException e){
			logger.logException(e);
		}

		final Set<String> conrefpushlist = StringUtils.restoreSet(properties.getProperty(Constants.CONREF_PUSH_LIST));
		final ConrefPushReader reader = new ConrefPushReader();
		for(final String fileName:conrefpushlist){
			//FIXME: this reader calculate parent directory
			reader.read(new File(tempDir,fileName).getAbsolutePath());
		}
		
		final Set<Map.Entry<String, Hashtable<String, String>>> pushSet = (Set<Map.Entry<String, Hashtable<String,String>>>) reader.getContent().getCollection();
		final Iterator<Map.Entry<String, Hashtable<String,String>>> iter = pushSet.iterator();
		
		while(iter.hasNext()){
			final Map.Entry<String, Hashtable<String,String>> entry = iter.next();
			final ConrefPushParser parser = new ConrefPushParser();
			final Content content = new ContentImpl();
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
