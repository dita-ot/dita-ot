/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.Map;
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

    private DITAOTLogger logger;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * execution point of MoveLinksModule.
     * 
     * @param input input parameters and resources
     * @return always {@code null}
     * @throws DITAOTException if process fails
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        
        final File maplinksFile = new File(input.getAttribute(ANT_INVOKER_PARAM_MAPLINKS));
        
        final MapLinksReader indexReader = new MapLinksReader();
        indexReader.setLogger(logger);
        indexReader.setMatch(new StringBuffer(ELEMENT_NAME_MAPLINKS)
                .append(SLASH).append(TOPIC_LINKPOOL.localName)
                .append(SLASH).append(TOPIC_LINKLIST.localName)
                .toString());
        indexReader.read(maplinksFile.getAbsolutePath());
        final Map<String, Map<String, String>> mapSet = indexReader.getMapping();
        
        final DitaLinksWriter indexInserter = new DitaLinksWriter();
        indexInserter.setLogger(logger);
        for (final Map.Entry<String, Map<String, String>> entry: mapSet.entrySet()) {
            logger.logInfo("Processing " + entry.getKey());
            final ContentImpl content = new ContentImpl();
            content.setValue(entry.getValue());
            indexInserter.setContent(content);
            indexInserter.write(entry.getKey());
        }
        return null;
    }

}
