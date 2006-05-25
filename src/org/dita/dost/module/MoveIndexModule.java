/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.writer.DitaIndexWriter;
import org.dita.dost.util.Constants;

/**
 * MoveIndexModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different 
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public class MoveIndexModule extends AbstractPipelineModule {

    private ContentImpl content;

    /**
     * Default constructor of MoveIndexModule class.
     */
    public MoveIndexModule() {
        super();
        content = new ContentImpl();

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
		
		if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
		
		mapFile = new File(tempDir, inputMap).getAbsolutePath();
		
        indexReader.setMatch(new StringBuffer(Constants.ELEMENT_NAME_TOPICREF)
                .append(Constants.SLASH).append(Constants.ELEMENT_NAME_TOPICMETA)
                .append(Constants.SLASH).append(Constants.ELEMENT_NAME_KEYWORDS).toString());
        
        indexReader.read(mapFile);
        mapSet = (Set) indexReader.getContent().getCollection();

        i = mapSet.iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            targetFileName = (String) entry.getKey();
            if (targetFileName.endsWith(Constants.FILE_EXTENSION_DITA) ||
                    targetFileName.endsWith(Constants.FILE_EXTENSION_XML)){
                content.setValue(entry.getValue());
                indexInserter.setContent(content);
                indexInserter.write((String) entry.getKey());
            }
        }
        return null;
    }

}
