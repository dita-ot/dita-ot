/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.writer.DitaLinksWriter;


/**
 * @author Zhang, Yuan Peng
 */
public class MoveLinksModule extends AbstractPipelineModule {
    private ContentImpl content;

    /**
     * 
     */
    public MoveLinksModule() {
        super();
        content = new ContentImpl();

    }

    /**
     * 
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) {

        String maplinksFile = ((PipelineHashIO)input).getAttribute("maplinks");
        MapIndexReader indexReader = new MapIndexReader();
        indexReader.setMatch("maplinks/linkpool");
        DitaLinksWriter indexInserter = new DitaLinksWriter();

        indexReader.read(maplinksFile);
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
