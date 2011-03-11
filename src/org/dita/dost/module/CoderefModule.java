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
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Constants;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.CoderefResolver;
/**
 * Coderef Module class.
 *
 */
final class CoderefModule implements AbstractPipelineModule {
	/**
	 * Constructor.
	 */
	public CoderefModule() {
		super();
	}
	/**
	 * Entry point of Coderef Module.
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
	public AbstractPipelineOutput execute(final AbstractPipelineInput input)
			throws DITAOTException {
		final String baseDir = input.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String tempDir = input.getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        
    	Properties properties = null;
    	try{
    		properties = ListUtils.getDitaList();
    	}catch(final IOException e){
    		throw new DITAOTException(e);
    	}
    	
    	final Set<String> codereflist=StringUtils.restoreSet(properties.getProperty(Constants.CODEREF_LIST));		
		final CoderefResolver writer = new CoderefResolver();
		for (final String fileName : codereflist) {
			//FIXME:This writer deletes and renames files, have to 
			writer.write(new File(tempDir,fileName).getAbsolutePath()); 
		}
		
		return null;
	}

}
