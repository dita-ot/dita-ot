/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.writer.DitaIndexWriter;


/**
 * @author Zhang, Yuan Peng
 */
public class MoveIndexModule extends AbstractPipelineModule {

    private ContentImpl content;

    /**
     * 
     */
    public MoveIndexModule() {
        super();
        content = new ContentImpl();

    }

    /**
     * 
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) {

    	String tempDir = ((PipelineHashIO)input).getAttribute("tempDir");
        String mapFile = tempDir + File.separatorChar
                + ((PipelineHashIO) input).getAttribute("inputmap");
        MapIndexReader indexReader = new MapIndexReader();
        indexReader.setMatch("topicref/topicmeta/keywords");
        DitaIndexWriter indexInserter = new DitaIndexWriter();

        indexReader.read(mapFile);
        Set mapSet = (Set) indexReader.getContent().getCollection();

        Iterator i = mapSet.iterator();
        for (; i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            content.setObject(entry.getValue());
            indexInserter.setContent(content);
            indexInserter.write((String) entry.getKey());
        }
        return null;
    }

}
