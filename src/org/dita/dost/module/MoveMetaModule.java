/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.DitaMapMetaWriter;
import org.dita.dost.writer.DitaMetaWriter;

/**
 * MoveMetaModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different 
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
final class MoveMetaModule implements AbstractPipelineModule {

    private final ContentImpl content;
    private DITAOTLogger logger;

    /**
     * Default constructor of MoveMetaModule class.
     */
    public MoveMetaModule() {
        super();
        content = new ContentImpl();
    }
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
	 * Entry point of MoveMetaModule.
	 * 
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
		final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
    	String tempDir = input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR);
       	
		final MapMetaReader metaReader = new MapMetaReader();
		metaReader.setLogger(logger);
		final DitaMetaWriter topicInserter = new DitaMetaWriter();
		topicInserter.setLogger(logger);
		final DitaMapMetaWriter mapInserter = new DitaMapMetaWriter();
		mapInserter.setLogger(logger);
		
		if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(final IOException e){
			throw new DITAOTException(e);
		}
		
		final Set<String> fullditamaplist = StringUtils.restoreSet(properties.getProperty(FULL_DITAMAP_LIST));
		for(String mapFile:fullditamaplist){
			mapFile = new File(tempDir, mapFile).getAbsolutePath();
			//FIXME: this reader gets the parent path of input file
			metaReader.read(mapFile);
	        final File oldMap = new File(mapFile);
	        final File newMap = new File(mapFile+".temp");
	        if (newMap.exists()) {
	        	if (!oldMap.delete()) {
	        		final Properties p = new Properties();
	            	p.put("%1", oldMap.getPath());
	            	p.put("%2", newMap.getAbsolutePath()+".chunk");
	            	logger.logError(MessageUtils.getMessage("DOTJ009E", p).toString());
	        	}
	        	if (!newMap.renameTo(oldMap)) {
	        		final Properties p = new Properties();
	            	p.put("%1", oldMap.getPath());
	            	p.put("%2", newMap.getAbsolutePath()+".chunk");
	            	logger.logError(MessageUtils.getMessage("DOTJ009E", p).toString());
	        	}
	        }
		}
				
		final Set<?> mapSet = (Set<?>) metaReader.getContent().getCollection();
		String targetFileName = null;
		//process map first
		Iterator<?> i = mapSet.iterator();
        while (i.hasNext()) {
            final Map.Entry<?,?> entry = (Map.Entry<?,?>) i.next();
            targetFileName = (String) entry.getKey();
            targetFileName = targetFileName.indexOf(SHARP) != -1 
            				? targetFileName.substring(0, targetFileName.indexOf(SHARP))
            				: targetFileName;
            if (targetFileName.endsWith(FILE_EXTENSION_DITAMAP )) {       	
            	content.setValue(entry.getValue());
                mapInserter.setContent(content);
                if (FileUtils.fileExists((String) entry.getKey())){
                	mapInserter.write((String) entry.getKey());
                }else{
                    logger.logError(" ERROR FILE DOES NOT EXIST " + (String) entry.getKey());
                }
            	
            }
        }
		
		//process topic		
        i = mapSet.iterator();
		targetFileName = null;
        while (i.hasNext()) {
            final Map.Entry<?,?> entry = (Map.Entry<?,?>) i.next();
            targetFileName = (String) entry.getKey();
            targetFileName = targetFileName.indexOf(SHARP) != -1 
            				? targetFileName.substring(0, targetFileName.indexOf(SHARP))
            				: targetFileName;
            if (targetFileName.endsWith(FILE_EXTENSION_DITA) ||
                    targetFileName.endsWith(FILE_EXTENSION_XML)){
                content.setValue(entry.getValue());
                topicInserter.setContent(content);
                if (FileUtils.fileExists((String) entry.getKey())){
                	topicInserter.write((String) entry.getKey());
                }else{
                    logger.logError(" ERROR FILE DOES NOT EXIST " + (String) entry.getKey());
                }

            } 
        }
        return null;
    }
}
