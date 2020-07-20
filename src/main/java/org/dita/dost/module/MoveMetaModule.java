/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.DelegatingURIResolver;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.DitaMapMetaWriter;
import org.dita.dost.writer.DitaMetaWriter;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toFile;
import static org.dita.dost.util.XMLUtils.toErrorListener;

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
     * Entry point of MoveMetaModule.
     *
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        final Collection<FileInfo> fis = job.getFileInfo(fi -> fi.isInput);
        if (!fis.isEmpty()) {
            final Map<URI, Map<String, Element>> mapSet = getMapMetadata(fis);
            pushMetadata(mapSet);
            pullTopicMetadata(input, fis);
        }

        return null;
    }

    /**
     * Pull metadata from topics and push to maps.
     */
    private void pullTopicMetadata(final AbstractPipelineInput input, final Collection<FileInfo> fis) throws DITAOTException {
        // Pull metadata (such as navtitle) into the map from the referenced topics
        final File styleFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));
        logger.info("Loading stylesheet " + styleFile);
        final XsltExecutable xsltExecutable;
        try {
            xsltExecutable = xmlUtils.getProcessor().newXsltCompiler().compile(new StreamSource(styleFile));
        } catch (SaxonApiException e) {
            throw new RuntimeException("Failed to compile stylesheet '" + styleFile.toURI() + "': " + e.getMessage(), e);
        }

        for (final FileInfo f : fis) {
            final File inputFile = new File(job.tempDir, f.file.getPath());
            final File tmp = new File(inputFile.getAbsolutePath() + ".tmp" + Long.toString(System.currentTimeMillis()));
            logger.info("Processing " + inputFile.toURI());
            logger.debug("Processing " + inputFile.toURI() + " to " + tmp.toURI());

            try {
                final XsltTransformer transformer = xsltExecutable.load();
                transformer.setErrorListener(toErrorListener(logger));
                transformer.setURIResolver(new DelegatingURIResolver(CatalogUtils.getCatalogResolver(), job.getStore()));

                for (Entry<String, String> e : input.getAttributes().entrySet()) {
                    logger.debug("Set parameter " + e.getKey() + " to '" + e.getValue() + "'");
                    transformer.setParameter(new QName(e.getKey()), XdmItem.makeValue(e.getValue()));
                }

                final Source source = job.getStore().getSource(inputFile.toURI());
                transformer.setSource(source);
                final Destination result = job.getStore().getDestination(tmp.toURI());
                result.setDestinationBaseURI(inputFile.toURI());
                transformer.setDestination(result);
                transformer.transform();
            } catch (final UncheckedXPathException e) {
                throw new DITAOTException("Failed to transform document", e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final SaxonApiException e) {
                throw new DITAOTException("Failed to transform document: " + e.getMessage(), e);
            } catch (final Exception e) {
                throw new DITAOTException("Failed to transform document: " + e.getMessage(), e);
            }
            try {
                logger.debug("Moving " + tmp.toURI() + " to " + inputFile.toURI());
                job.getStore().move(tmp.toURI(), inputFile.toURI());
            } catch (final IOException e) {
                throw new DITAOTException("Failed to replace document: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Push information from topicmeta in the map into the corresponding topics and maps.
     */
    private void pushMetadata(final Map<URI, Map<String, Element>> mapSet) {
        if (!mapSet.isEmpty()) {
            //process map first
            final DitaMapMetaWriter mapInserter = new DitaMapMetaWriter();
            mapInserter.setLogger(logger);
            mapInserter.setJob(job);
            for (final Entry<URI, Map<String, Element>> entry : mapSet.entrySet()) {
                final URI key = stripFragment(entry.getKey());
                final FileInfo fi = job.getFileInfo(key);
                if (fi == null) {
                    logger.error("File " + new File(job.tempDir, key.getPath()) + " was not found.");
                    continue;
                }
                final URI targetFileName = job.tempDirURI.resolve(fi.uri);
                assert targetFileName.isAbsolute();
                if (fi.format != null && ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
                    mapInserter.setMetaTable(entry.getValue());
                    if (job.getStore().exists(targetFileName)) {
                        try {
                            mapInserter.read(toFile(targetFileName));
                        } catch (DITAOTException e) {
                            logger.error("Failed to read " + targetFileName + ": " + e.getMessage(), e);
                        }
                    } else {
                        logger.error("File " + targetFileName + " does not exist");
                    }
                }
            }
            //process topic
            final DitaMetaWriter topicInserter = new DitaMetaWriter();
            topicInserter.setLogger(logger);
            topicInserter.setJob(job);
            for (final Entry<URI, Map<String, Element>> entry : mapSet.entrySet()) {
                final URI key = stripFragment(entry.getKey());
                final FileInfo fi = job.getFileInfo(key);
                if (fi == null) {
                    logger.error("File " + new File(job.tempDir, key.getPath()) + " was not found.");
                    continue;
                }
                final URI targetFileName = job.tempDirURI.resolve(fi.uri);
                assert targetFileName.isAbsolute();
                if (fi.format == null || fi.format.equals(ATTR_FORMAT_VALUE_DITA)) {
                    final String topicid = entry.getKey().getFragment();
                    topicInserter.setTopicId(topicid);
                    topicInserter.setMetaTable(entry.getValue());
                    try {
                        topicInserter.read(toFile(targetFileName));
                    } catch (DITAOTException e) {
                        logger.error("Failed to read " + targetFileName + ": " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Read metadata from topicmeta elements in maps.
     */
    private Map<URI, Map<String, Element>> getMapMetadata(final Collection<FileInfo> fis) {
        final MapMetaReader metaReader = new MapMetaReader();
        metaReader.setLogger(logger);
        metaReader.setJob(job);
        for (final FileInfo f : fis) {
            final File mapFile = new File(job.tempDir, f.file.getPath());
            //FIXME: this reader gets the parent path of input file
            try {
                metaReader.read(mapFile);
            } catch (DITAOTException e) {
                logger.error("Failed to read " + mapFile + ": " + e.getMessage(), e);
            }
        }
        return metaReader.getMapping();
    }
}
