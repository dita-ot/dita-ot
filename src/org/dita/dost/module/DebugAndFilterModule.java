/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.LinkedList;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.ListReader;
import org.dita.dost.writer.DitaWriter;


/**
 * @author Zhang, Yuan Peng
 */
public class DebugAndFilterModule extends AbstractPipelineModule {

    /**
     * 
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) {
        String baseDir = ((PipelineHashIO) input).getAttribute("basedir");
        String ditavalFile = ((PipelineHashIO) input).getAttribute("ditaval");
        String tempDir = ((PipelineHashIO) input).getAttribute("tempDir");
        ListReader listReader = new ListReader();
        listReader.read(tempDir + File.separator + "dita.list");
        LinkedList parseList = (LinkedList) listReader.getContent()
                .getCollection();
        Content content;
        if (ditavalFile!=null){
            DitaValReader filterReader = new DitaValReader();
            filterReader.read(ditavalFile);
            content = filterReader.getContent();
        }else{
            content = new ContentImpl();
        }

        DitaWriter fileWriter = new DitaWriter();
        content.setObject(tempDir);
        fileWriter.setContent(content);
        
        if(baseDir!=null){
            while (!parseList.isEmpty()) {
                /*
                 * Usually the writer's argument for write() is used to pass in the
                 * ouput file name. But in this case, the input file name is same as
                 * output file name so we can use this argument to pass in the input
                 * file name
                 */
                fileWriter.write(baseDir+'|'+
                        (String) parseList.removeLast());
            }
        }else{
            while (!parseList.isEmpty()) {
                /*
                 * Usually the writer's argument for write() is used to pass in the
                 * ouput file name. But in this case, the input file name is same as
                 * output file name so we can use this argument to pass in the input
                 * file name
                 */
                fileWriter.write((String) parseList.removeLast());
            }
        }

        return null;
    }

}
