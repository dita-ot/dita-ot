/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.ListReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.writer.DitaWriter;


/**
 * DebugAndFilterModule implement the second step in preprocess. It will insert debug
 * information into every dita files and filter out the information that is not 
 * necessary.
 * 
 * @author Zhang, Yuan Peng
 */
public class DebugAndFilterModule extends AbstractPipelineModule {

    /**
     * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
     * 
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
        String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
        String ditavalFile = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_DITAVAL);
        String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
        String inputDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_EXT_PARAM_INPUTDIR);
        String filePathPrefix = null;
        ListReader listReader = new ListReader();
        LinkedList parseList = null;
        Content content;
        DitaWriter fileWriter;
        
        if (!new File(inputDir).isAbsolute()) {
        	inputDir = new File(baseDir, inputDir).getAbsolutePath();
        }
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        if (ditavalFile != null && !new File(ditavalFile).isAbsolute()) {
			ditavalFile = new File(baseDir, ditavalFile).getAbsolutePath();
		}
        
        listReader.read(new File(tempDir, Constants.FILE_NAME_DITA_LIST).getAbsolutePath());
        parseList = (LinkedList) listReader.getContent()
                .getCollection();
        if (ditavalFile!=null){
            DitaValReader filterReader = new DitaValReader();
            filterReader.read(ditavalFile);
            content = filterReader.getContent();
        }else{
            content = new ContentImpl();
        }

        fileWriter = new DitaWriter();
        content.setValue(tempDir);
        fileWriter.setContent(content);
        
        if(inputDir != null){
            filePathPrefix = inputDir + Constants.STICK;
        }
        
        while (!parseList.isEmpty()) {
            /*
             * Usually the writer's argument for write() is used to pass in the
             * ouput file name. But in this case, the input file name is same as
             * output file name so we can use this argument to pass in the input
             * file name. "|" is used to separate the path information that is
             * not necessary to be kept (baseDir) and the path information that
             * need to be kept in the temp directory.
             */
        	fileWriter.write(
        			new StringBuffer().append(filePathPrefix)
        				.append((String) parseList.removeLast()).toString());
            
        }
        
        performCopytoTask(tempDir, listReader.getCopytoMap());

        return null;
    }

    /*
     * Execute copy-to task, generate copy-to targets base on sources
     */
	private void performCopytoTask(String tempDir, Map copytoMap) {
        Iterator iter = copytoMap.entrySet().iterator();
        while (iter.hasNext()) {
        	Map.Entry entry = (Map.Entry) iter.next();
        	String copytoTarget = (String) entry.getKey();
        	String copytoSource = (String) entry.getValue();        	
        	File srcFile = new File(tempDir, copytoSource);
        	File targetFile = new File(tempDir, copytoTarget);
        	
        	FileUtils.copyFile(srcFile, targetFile);
        }
	}

}
