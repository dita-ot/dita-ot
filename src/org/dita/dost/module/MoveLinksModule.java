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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapLinksReader;
import org.dita.dost.writer.DitaLinksWriter;

/**
 * MoveLinksModule implements move links step in preprocess. It reads the map links
 * information from the temp file "maplinks.unordered" and move these information
 * to different corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
final class MoveLinksModule implements AbstractPipelineModule {
    private final ContentImpl content;

    private DITAOTLogger logger;

    /**
     * Default constructor of MoveLinksModule class.
     */
    public MoveLinksModule() {
        super();
        content = new ContentImpl();

    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * execution point of MoveLinksModule.
     * 
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final String maplinksFile = input.getAttribute(ANT_INVOKER_PARAM_MAPLINKS);
        final MapLinksReader indexReader = new MapLinksReader();
        indexReader.setLogger(logger);
        final DitaLinksWriter indexInserter = new DitaLinksWriter();
        indexInserter.setLogger(logger);
        Set<Map.Entry<String, String>> mapSet;
        Iterator<Map.Entry<String, String>> i;

        indexReader.setMatch(new StringBuffer(ELEMENT_NAME_MAPLINKS)
        .append(SLASH).append(TOPIC_LINKPOOL.localName)
        .append(SLASH).append(TOPIC_LINKLIST.localName)
        .toString());

        indexReader.read(maplinksFile);
        mapSet = (Set<Map.Entry<String, String>>) indexReader.getContent().getCollection();

        i = mapSet.iterator();
        while (i.hasNext()) {
            final Map.Entry<String, String> entry = i.next();
            content.setValue(entry.getValue());
            indexInserter.setContent(content);
            indexInserter.write(entry.getKey());
        }
        return null;
    }

}
