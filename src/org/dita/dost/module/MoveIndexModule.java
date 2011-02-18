/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.DitaIndexWriter;

/**
 * MoveIndexModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different 
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public class MoveIndexModule implements AbstractPipelineModule {

    private final ContentImpl content;
    private DITAOTJavaLogger logger = null;

    /**
     * Default constructor of MoveIndexModule class.
     */
    public MoveIndexModule() {
        super();
        content = new ContentImpl();
        logger = new DITAOTJavaLogger();

    }

    /**
	 * Entry point of MoveIndexModule.
	 * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
    	Set<Map.Entry<String, String>> mapSet;
		Iterator<Map.Entry<String, String>> i;
		String targetFileName;
		final MapIndexReader indexReader = new MapIndexReader();
		final DitaIndexWriter indexInserter = new DitaIndexWriter();
		final String baseDir = input.getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
    	String tempDir = input.getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
    	
		if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
    	   		
		indexReader.setMatch(new StringBuffer(Constants.ELEMENT_NAME_TOPICREF)
        .append(Constants.SLASH).append(Constants.ELEMENT_NAME_TOPICMETA)
        .append(Constants.SLASH).append(Constants.ELEMENT_NAME_KEYWORDS).toString());
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(final IOException e){
			throw new DITAOTException(e);
		}
		
		final Set<String> fullditamaplist = StringUtils.restoreSet(properties.getProperty(Constants.FULL_DITAMAP_LIST));
		for(final String fileName : fullditamaplist){
			//FIXME: this reader needs parent directory for further process
			indexReader.read(new File(tempDir, fileName).getAbsolutePath());  
		}
		
		mapSet = (Set<Map.Entry<String, String>>) indexReader.getContent().getCollection();
		i = mapSet.iterator();
        while (i.hasNext()) {
        	final Map.Entry<String, String> entry = i.next();
            targetFileName = entry.getKey();
            targetFileName = targetFileName.indexOf(Constants.SHARP) != -1 
            				? targetFileName.substring(0, targetFileName.indexOf(Constants.SHARP))
            				: targetFileName;
            if (targetFileName.endsWith(Constants.FILE_EXTENSION_DITA) ||
                    targetFileName.endsWith(Constants.FILE_EXTENSION_XML)){
                content.setValue(entry.getValue());
                indexInserter.setContent(content);
                if (FileUtils.fileExists(entry.getKey())){
                    indexInserter.write(entry.getKey());
                }else{
                    logger.logError(" ERROR FILE DOES NOT EXIST " + entry.getKey());
                }

            }
        }
        return null;
    }

}
