/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

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
import org.dita.dost.reader.MapLinksReader;
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
     * execution point of MoveLinksModule.
     * @see org.dita.dost.module.AbstractPipelineModule#execute(org.dita.dost.pipeline.AbstractPipelineInput)
	 * @param input Input parameters and resources.
	 * @return null
	 * @throws DITAOTException exception
	 */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {

        String maplinksFile = ((PipelineHashIO)input).getAttribute(Constants.ANT_INVOKER_PARAM_MAPLINKS);
        MapLinksReader indexReader = new MapLinksReader();
		DitaLinksWriter indexInserter = new DitaLinksWriter();
		Set<Map.Entry<String, String>> mapSet;
		Iterator<Map.Entry<String, String>> i;
        
        indexReader.setMatch(new StringBuffer(Constants.ELEMENT_NAME_MAPLINKS)
                .append(Constants.SLASH).append(Constants.ELEMENT_NAME_LINKPOOL)
                .append(Constants.SLASH).append(Constants.ELEMENT_NAME_LINKLIST)
                .toString());
        
        indexReader.read(maplinksFile);
        mapSet = (Set<Map.Entry<String, String>>) indexReader.getContent().getCollection();

        i = mapSet.iterator();
        while (i.hasNext()) {
            Map.Entry<String, String> entry = i.next();
            content.setValue(entry.getValue());
            indexInserter.setContent(content);
            indexInserter.write((String) entry.getKey());
        }
        return null;
    }

}
