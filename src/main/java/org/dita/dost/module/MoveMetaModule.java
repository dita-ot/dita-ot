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
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.w3c.dom.Element;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.writer.DitaMapMetaWriter;
import org.dita.dost.writer.DitaMetaWriter;

/**
 * MoveMetaModule implement the move index step in preprocess. It reads the index
 * information from ditamap file and move these information to different
 * corresponding dita topic file.
 * 
 * @author Zhang, Yuan Peng
 */
final class MoveMetaModule implements AbstractPipelineModule {

    private final ContentImpl content;
    private DITAOTLogger logger;

    /**
     * Default constructor of MoveMetaModule class.
     */
    public MoveMetaModule() {
        super();
        content = new ContentImpl();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Entry point of MoveMetaModule.
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
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }

        final MapMetaReader metaReader = new MapMetaReader();
        metaReader.setLogger(logger);
        final Set<String> fullditamaplist = job.getSet(FULL_DITAMAP_LIST);
        for (String mapFile: fullditamaplist) {
            mapFile = new File(tempDir, mapFile).getAbsolutePath();
            logger.logInfo("Reading " + mapFile);
            //FIXME: this reader gets the parent path of input file
            metaReader.read(mapFile);
            final File oldMap = new File(mapFile);
            final File newMap = new File(mapFile+".temp");
            if (newMap.exists()) {
                if (!oldMap.delete()) {
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", oldMap.getPath(), newMap.getAbsolutePath()+".chunk").toString());
                }
                if (!newMap.renameTo(oldMap)) {
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", oldMap.getPath(), newMap.getAbsolutePath()+".chunk").toString());
                }
            }
        }

        final Map<String, Hashtable<String, Element>> mapSet = metaReader.getMapping();
        
        //process map first
        final DitaMapMetaWriter mapInserter = new DitaMapMetaWriter();
        mapInserter.setLogger(logger);
        for (final Entry<String, Hashtable<String, Element>> entry: mapSet.entrySet()) {
            String targetFileName = entry.getKey();
            targetFileName = targetFileName.indexOf(SHARP) != -1
                             ? targetFileName.substring(0, targetFileName.indexOf(SHARP))
                             : targetFileName;
            if (targetFileName.endsWith(FILE_EXTENSION_DITAMAP )) {
                content.setValue(entry.getValue());
                mapInserter.setContent(content);
                if (FileUtils.fileExists(entry.getKey())) {
                    logger.logInfo("Processing " + entry.getKey());
                    mapInserter.write(entry.getKey());
                } else {
                    logger.logError("File " + entry.getKey() + " does not exist");
                }

            }
        }

        //process topic
        final DitaMetaWriter topicInserter = new DitaMetaWriter();
        topicInserter.setLogger(logger);
        for (final Map.Entry<String, Hashtable<String, Element>> entry: mapSet.entrySet()) {
            String targetFileName = entry.getKey();
            targetFileName = targetFileName.indexOf(SHARP) != -1
                             ? targetFileName.substring(0, targetFileName.indexOf(SHARP))
                             : targetFileName;
            if (targetFileName.endsWith(FILE_EXTENSION_DITA) || targetFileName.endsWith(FILE_EXTENSION_XML)) {
                content.setValue(entry.getValue());
                topicInserter.setContent(content);
                if (FileUtils.fileExists(entry.getKey())) {
                    logger.logInfo("Processing " + entry.getKey());
                    topicInserter.write(entry.getKey());
                } else {
                    logger.logError("File " + entry.getKey() + " does not exist");
                }

            }
        }
        return null;
    }
}
