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
final class MoveLinksModule extends AbstractPipelineModuleImpl {

    /**
     * execution point of MoveLinksModule.
     * 
     * @param input input parameters and resources
     * @return always {@code null}
     * @throws DITAOTException if process fails
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final File maplinksFile = new File(input.getAttribute(ANT_INVOKER_PARAM_MAPLINKS));
        if (!maplinksFile.exists()) {
            return null;
        }
        
        final MapLinksReader indexReader = new MapLinksReader();
        indexReader.setLogger(logger);
        indexReader.setMatch(ELEMENT_NAME_MAPLINKS + SLASH + TOPIC_LINKPOOL.localName + SLASH + TOPIC_LINKLIST.localName);
        indexReader.read(maplinksFile.getAbsoluteFile());
        final Map<File, Map<String, String>> mapSet = indexReader.getMapping();
        
        if (!mapSet.isEmpty()) {
            final DitaLinksWriter indexInserter = new DitaLinksWriter();
            indexInserter.setLogger(logger);
            for (final Map.Entry<File, Map<String, String>> entry: mapSet.entrySet()) {
                logger.info("Processing " + entry.getKey());
                indexInserter.setLinks(entry.getValue());
                indexInserter.write(entry.getKey());
            }
        }
        return null;
    }

}
