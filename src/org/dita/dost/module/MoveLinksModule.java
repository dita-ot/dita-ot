/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.DitaLinksWriter;

/**
 * MoveLinksModule implements move links step in preprocess. It reads the map links
 * information from the temp file "maplinks.unordered" and move these information
 * to different corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public class MoveLinksModule implements AbstractPipelineModule {
    private ContentImpl content;


    /**
     * Default constructor of MoveLinksModule class.
     */
    public MoveLinksModule() {
        super();
        content = new ContentImpl();

    }


    /**
     * execution point of MoveLinksModule
     * @param input
     * @throws DITAOTException
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {

        String maplinksFile = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_MAPLINKS);
        MapIndexReader indexReader = new MapIndexReader();
		DitaLinksWriter indexInserter = new DitaLinksWriter();
		Set mapSet;
		Iterator i;
        
        indexReader.setMatch(new StringBuffer(Constants.ELEMENT_NAME_MAPLINKS)
                .append(Constants.SLASH).append(Constants.ELEMENT_NAME_LINKPOOL)
                .toString());
        
        indexReader.read(maplinksFile);
        mapSet = (Set) indexReader.getContent().getCollection();

        i = mapSet.iterator();
        while (i.hasNext()) {
            Map.Entry entry = (Map.Entry) i.next();
            content.setValue(entry.getValue());
            indexInserter.setContent(content);
            indexInserter.write((String) entry.getKey());
        }
        return null;
    }

}
