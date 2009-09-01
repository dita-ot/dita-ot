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
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.DitaMetaWriter;

/**
 * MoveMetaModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different 
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public class MoveMetaModule implements AbstractPipelineModule {

    private ContentImpl content;
    private DITAOTJavaLogger logger = null;

    /**
     * Default constructor of MoveMetaModule class.
     */
    public MoveMetaModule() {
        super();
        content = new ContentImpl();
        logger = new DITAOTJavaLogger();
    }

    /** (non-Javadoc)
     * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
		String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
    	String tempDir = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
       	
		MapMetaReader metaReader = new MapMetaReader();
		DitaMetaWriter inserter = new DitaMetaWriter();

		if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
		
		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(IOException e){
			throw new DITAOTException(e);
		}
		
		Set<String> fullditamaplist = StringUtils.restoreSet(properties.getProperty(Constants.FULL_DITAMAP_LIST));
		for(String mapFile:fullditamaplist){
			mapFile = new File(tempDir, mapFile).getAbsolutePath();
			//FIXME: this reader gets the parent path of input file
			metaReader.read(mapFile);
	        File oldMap = new File(mapFile);
	        File newMap = new File(mapFile+".temp");
	        if (newMap.exists()) {
	        	if (!oldMap.delete()) {
	        		Properties p = new Properties();
	            	p.put("%1", oldMap.getPath());
	            	p.put("%2", newMap.getAbsolutePath()+".chunk");
	            	logger.logError(MessageUtils.getMessage("DOTJ009E", p).toString());
	        	}
	        	if (!newMap.renameTo(oldMap)) {
	        		Properties p = new Properties();
	            	p.put("%1", oldMap.getPath());
	            	p.put("%2", newMap.getAbsolutePath()+".chunk");
	            	logger.logError(MessageUtils.getMessage("DOTJ009E", p).toString());
	        	}
	        }
		}
				
		Set<?> mapSet = (Set<?>) metaReader.getContent().getCollection();
        Iterator<?> i = mapSet.iterator();
		String targetFileName = null;
        while (i.hasNext()) {
            Map.Entry<?,?> entry = (Map.Entry<?,?>) i.next();
            targetFileName = (String) entry.getKey();
            targetFileName = targetFileName.indexOf(Constants.SHARP) != -1 
            				? targetFileName.substring(0, targetFileName.indexOf(Constants.SHARP))
            				: targetFileName;
            if (targetFileName.endsWith(Constants.FILE_EXTENSION_DITA) ||
                    targetFileName.endsWith(Constants.FILE_EXTENSION_XML)){
                content.setValue(entry.getValue());
                inserter.setContent(content);
                if (FileUtils.fileExists((String) entry.getKey())){
                    inserter.write((String) entry.getKey());
                }else{
                    logger.logError(" ERROR FILE DOES NOT EXIST " + (String) entry.getKey());
                }

            }
        }
        return null;
    }
}
