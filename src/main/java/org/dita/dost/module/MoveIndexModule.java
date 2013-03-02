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
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapIndexReader;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.writer.DitaIndexWriter;

/**
 * MoveIndexModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
final class MoveIndexModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Entry point of MoveIndexModule.
     * 
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        
        Job job = null;
        try{
            job = new Job(tempDir);
        } catch(final IOException e) {
            throw new DITAOTException(e);
        }

        final MapIndexReader indexReader = new MapIndexReader();
        indexReader.setLogger(logger);
        indexReader.setMatch(new StringBuffer(MAP_TOPICREF.localName)
                .append(SLASH).append(MAP_TOPICMETA.localName)
                .append(SLASH).append(TOPIC_KEYWORDS.localName).toString());

        final Set<String> fullditamaplist = job.getSet(FULL_DITAMAP_LIST);
        for(final String fileName : fullditamaplist){
            //FIXME: this reader needs parent directory for further process
            indexReader.read(new File(tempDir, fileName).getAbsolutePath());
        }

        final Map<String, String> mapSet = indexReader.getMapping();
        
        final DitaIndexWriter indexInserter = new DitaIndexWriter();
        indexInserter.setLogger(logger);
        for (final Map.Entry<String, String> entry: mapSet.entrySet()) {
            String targetFileName = entry.getKey();
            targetFileName = targetFileName.indexOf(SHARP) != -1
                            ? targetFileName.substring(0, targetFileName.indexOf(SHARP))
                            : targetFileName;
            if (targetFileName.endsWith(FILE_EXTENSION_DITA) || targetFileName.endsWith(FILE_EXTENSION_XML)){
                final ContentImpl content = new ContentImpl();
                content.setValue(entry.getValue());
                indexInserter.setContent(content);
                if (FileUtils.fileExists(entry.getKey())) {
                    logger.logInfo("Processing " + targetFileName);
                    indexInserter.write(entry.getKey());
                } else {
                    logger.logError("File " + entry.getKey() + " does not exist");
                }

            }
        }
        return null;
    }

}
