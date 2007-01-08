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
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.writer.DitaIndexWriter;

/**
 * MoveIndexModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different 
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public class MoveIndexModule implements AbstractPipelineModule {

    private ContentImpl content;
    private DITAOTJavaLogger logger = null;

    /**
     * Default constructor of MoveIndexModule class.
     */
    public MoveIndexModule() {
        super();
        content = new ContentImpl();
        logger = new DITAOTJavaLogger();

    }

    /** (non-Javadoc)
     * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
    	String mapFile;
    	Set mapSet;
		Iterator i;
		String targetFileName;
		MapIndexReader indexReader = new MapIndexReader();
		DitaIndexWriter indexInserter = new DitaIndexWriter();
		String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
    	String tempDir = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
    	String inputMap = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);
		File ditalist = null;
		Properties prop = new Properties();
		StringTokenizer st = null;
    	
		if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
		
		indexReader.setMatch(new StringBuffer(Constants.ELEMENT_NAME_TOPICREF)
        .append(Constants.SLASH).append(Constants.ELEMENT_NAME_TOPICMETA)
        .append(Constants.SLASH).append(Constants.ELEMENT_NAME_KEYWORDS).toString());
		
		ditalist = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
		try{
			prop.load(new FileInputStream(ditalist));
		}catch(IOException ioe){
			throw new DITAOTException(ioe);
		}
		
		st = new StringTokenizer(prop.getProperty(Constants.FULL_DITAMAP_LIST), Constants.COMMA);
		while(st.hasMoreTokens()){
			mapFile = new File(tempDir, st.nextToken()).getAbsolutePath();        	        
	        indexReader.read(mapFile);
		}		
		
        mapSet = (Set) indexReader.getContent().getCollection();

        i = mapSet.iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            targetFileName = (String) entry.getKey();
            targetFileName = targetFileName.indexOf(Constants.SHARP) != -1 
            				? targetFileName.substring(0, targetFileName.indexOf(Constants.SHARP))
            				: targetFileName;
            if (targetFileName.endsWith(Constants.FILE_EXTENSION_DITA) ||
                    targetFileName.endsWith(Constants.FILE_EXTENSION_XML)){
                content.setValue(entry.getValue());
                indexInserter.setContent(content);
                if (FileUtils.fileExists((String) entry.getKey())){
                    indexInserter.write((String) entry.getKey());
                }else{
                    logger.logError(" ERROR FILE DOES NOT EXIST " + (String) entry.getKey());
                }

            }
        }
        return null;
    }

}
