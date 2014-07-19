/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.w3c.dom.Element;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.DitaMapMetaWriter;
import org.dita.dost.writer.DitaMetaWriter;

/**
 * MoveMetaModule implement the move meta step in preprocess. It cascades metadata
 * in maps and collects metadata for topics. The collected metadata is then inserted
 * into maps and topics.
 *
 * @author Zhang, Yuan Peng
 */
final class MoveMetaModule extends AbstractPipelineModuleImpl {

    /**
     * Default constructor of MoveMetaModule class.
     */
    public MoveMetaModule() {
        super();
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
        final Collection<FileInfo> fis = new ArrayList<FileInfo>(); 
        //for (final FileInfo f: job.getFileInfo()) {
        //    if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
        //        fis.add(f);
        //    }
        //}
        fis.add(job.getFileInfo(toURI(job.getInputMap())));
        if (!fis.isEmpty()) {
            final MapMetaReader metaReader = new MapMetaReader();
            metaReader.setLogger(logger);
            for (final FileInfo f: fis) {
                final File mapFile = new File(job.tempDir, f.file.getPath());
                logger.info("Processing " + mapFile);
                //FIXME: this reader gets the parent path of input file
                metaReader.read(mapFile);
            }
            final Map<URI, Map<String, Element>> mapSet = metaReader.getMapping();
            
            if (!mapSet.isEmpty()) {
                //process map first
                final DitaMapMetaWriter mapInserter = new DitaMapMetaWriter();
                mapInserter.setLogger(logger);
                mapInserter.setJob(job);
                for (final Entry<URI, Map<String, Element>> entry: mapSet.entrySet()) {
                    final URI targetFileName = entry.getKey();
                    if (targetFileName.getPath().endsWith(FILE_EXTENSION_DITAMAP)) {
                        mapInserter.setMetaTable(entry.getValue());
                        if (toFile(targetFileName).exists()) {
                            logger.info("Processing " + targetFileName);
                            mapInserter.read(toFile(targetFileName));
                        } else {
                            logger.error("File " + targetFileName + " does not exist");
                        }
        
                    }
                }
        
                //process topic
                final DitaMetaWriter topicInserter = new DitaMetaWriter();
                topicInserter.setLogger(logger);
                topicInserter.setJob(job);
                for (final Map.Entry<URI, Map<String, Element>> entry: mapSet.entrySet()) {
                    final URI targetFileName = entry.getKey();
                    if (targetFileName.getPath().endsWith(FILE_EXTENSION_DITA) || targetFileName.getPath().endsWith(FILE_EXTENSION_XML)) {
                        topicInserter.setMetaTable(entry.getValue());
                        if (toFile(targetFileName).exists()) {
                            logger.info("Processing " + targetFileName);
                            topicInserter.read(toFile(targetFileName));
                        } else {
                            logger.error("File " + targetFileName + " does not exist");
                        }
        
                    }
                }
            }
        }
        return null;
    }
}
