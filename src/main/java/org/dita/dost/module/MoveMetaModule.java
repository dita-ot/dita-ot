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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Element;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.DitaMapMetaWriter;
import org.dita.dost.writer.DitaMetaWriter;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Cascades metadata from maps to topics and then from topics to maps.
 *
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
        final Collection<FileInfo> fis = new ArrayList<>();
        //for (final FileInfo f: job.getFileInfo()) {
        //    if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format)) {
        //        fis.add(f);
        //    }
        //}
        fis.add(job.getFileInfo(job.getInputMap()));
        if (!fis.isEmpty()) {
            // Push information from <topicmeta> in the map into the corresponding topics
            final MapMetaReader metaReader = new MapMetaReader();
            metaReader.setLogger(logger);
            for (final FileInfo f : fis) {
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
                for (final Entry<URI, Map<String, Element>> entry : mapSet.entrySet()) {
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
                for (final Map.Entry<URI, Map<String, Element>> entry : mapSet.entrySet()) {
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


            // Pull metadata (such as navtitle) into the map from the referenced topics
            final File styleFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));
            for (final FileInfo f : fis) {
                final File inputFile = new File(job.tempDir, f.file.getPath());
                final File tmp = new File(inputFile.getAbsolutePath() + ".tmp" + Long.toString(System.currentTimeMillis()));
                if (!tmp.getParentFile().exists() && !tmp.getParentFile().mkdirs()) {
                    throw new DITAOTException("Failed to create directory " + tmp.getParent());
                }
                logger.info("Processing " + inputFile.getAbsolutePath());
                logger.debug("Processing " + inputFile.getAbsolutePath() + " to " + tmp.getAbsolutePath());

                final Source source = new StreamSource(inputFile.toURI().toString());
                try {

                    logger.info("Loading stylesheet " + styleFile.getAbsolutePath());
                    final TransformerFactory tf = TransformerFactory.newInstance();
                    tf.setURIResolver(CatalogUtils.getCatalogResolver());
                    final Transformer t = tf.newTransformer(new StreamSource(styleFile));
                    if (Configuration.DEBUG) {
                        t.setURIResolver(new XMLUtils.DebugURIResolver(tf.getURIResolver()));
                    }
                    for (Map.Entry<String, String> e : input.getAttributes().entrySet()) {
                        logger.debug("Set parameter " + e.getKey() + " to '" + e.getValue() + "'");
                        t.setParameter(e.getKey(), e.getValue());
                    }

                    t.transform(source, new StreamResult(tmp));

                    logger.debug("Moving " + tmp.getAbsolutePath() + " to " + inputFile.getAbsolutePath());
                    if (!inputFile.delete()) {
                        throw new IOException("Failed to to delete input file " + inputFile.getAbsolutePath());
                    }
                    if (!tmp.renameTo(inputFile)) {
                        throw new IOException("Failed to to replace input file " + inputFile.getAbsolutePath());
                    }
                } catch (final TransformerConfigurationException e) {
                    throw new RuntimeException("Failed to compile stylesheet '" + styleFile.getAbsolutePath() + "': " + e.getMessage(), e);
                } catch (final Exception e) {
                    throw new DITAOTException("Failed to transform document: " + e.getMessage(), e);
                } finally {
                    try {
                        XMLUtils.close(source);
                    } catch (final IOException e) {
                        // NOOP
                    }
                    logger.debug("Remove " + tmp.getAbsolutePath());
                    FileUtils.delete(tmp);
                }
            }
        }

        return null;
    }
}
