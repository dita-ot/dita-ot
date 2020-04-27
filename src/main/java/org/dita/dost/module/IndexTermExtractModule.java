/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitamapIndexTermReader;
import org.dita.dost.reader.IndexTermReader;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static org.dita.dost.util.Constants.*;

/**
 * This class extends AbstractPipelineModule, used to extract indexterm from
 * dita/ditamap files.
 *
 * @version 1.0 2005-04-30
 *
 * @author Wu, Zhi Qiang
 */
public class IndexTermExtractModule extends AbstractPipelineModuleImpl {
    /** The input map */
    private File inputMap = null;

    /** The extension of the target file */
    private String targetExt = null;

    /** The list of topics */
    private List<URI> topicList = null;

    /** The list of ditamap files */
    private List<URI> ditamapList = null;

    private IndexTermCollection indexTermCollection;

    /**
     * Create a default instance.
     */
    public IndexTermExtractModule() {
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        indexTermCollection = new IndexTermCollection();
        indexTermCollection.setLogger(logger);
        try {
            parseAndValidateInput(input);
            extractIndexTerm();
            indexTermCollection.sort();
            indexTermCollection.outputTerms();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }

        return null;
    }

    private void parseAndValidateInput(final AbstractPipelineInput input) {
        final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
        String output = input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUT);
        if (!new File(output).isAbsolute()) {
            output = new File(baseDir, output).getAbsolutePath();
        }
        final String encoding = input.getAttribute(ANT_INVOKER_EXT_PARAM_ENCODING);
        final String indextype = input.getAttribute(ANT_INVOKER_EXT_PARAM_INDEXTYPE);
        final String indexclass = input.getAttribute(ANT_INVOKER_EXT_PARAM_INDEXCLASS);
        final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
        inputMap = new File(job.tempDirURI.resolve(in.uri));
        targetExt = input.getAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT);

        /*
         * Parse topic list and ditamap list from the input dita.list file
         */
        topicList = new ArrayList<>();
        for (final FileInfo f: job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format) && !f.isResourceOnly) {
                topicList.add(job.tempDirURI.resolve(f.uri));
            }
        }
        ditamapList = new ArrayList<>();
        for (final FileInfo f: job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format) && !f.isResourceOnly) {
                ditamapList.add(job.tempDirURI.resolve(f.uri));
            }
        }

        final int lastIndexOfDot = output.lastIndexOf(".");
        final String outputRoot = (lastIndexOfDot == -1) ? output : output.substring(0,
                lastIndexOfDot);

        indexTermCollection.setOutputFileRoot(outputRoot);
        indexTermCollection.setIndexType(indextype);
        indexTermCollection.setIndexClass(indexclass);
        //RFE 2987769 Eclipse index-see
        indexTermCollection.setPipelineHashIO((PipelineHashIO) input);

        if (encoding != null && encoding.trim().length() > 0) {
            IndexTerm.setTermLocale(StringUtils.getLocale(encoding));
        }
    }

    private void extractIndexTerm() throws SAXException {
        final IndexTermReader handler = new IndexTermReader(indexTermCollection);
        handler.setLogger(logger);
        final DitamapIndexTermReader ditamapIndexTermReader = new DitamapIndexTermReader(indexTermCollection, true);
        ditamapIndexTermReader.setLogger(logger);

        final FileInfo fileInfo = job.getFileInfo(f -> f.isInput).iterator().next();
        final URI tempInputMap = job.tempDirURI.resolve(fileInfo.uri);
        for (final URI aTopicList : topicList) {
            URI target;
            String targetPathFromMap;
            String targetPathFromMapWithoutExt;
            handler.reset();
            target = aTopicList;
            targetPathFromMap = FileUtils.getRelativeUnixPath(
                    tempInputMap.toString(),
                    target.toString());
            targetPathFromMapWithoutExt = targetPathFromMap
                    .substring(0, targetPathFromMap.lastIndexOf("."));
            handler.setTargetFile(targetPathFromMapWithoutExt + targetExt);

            try {
                job.getStore().transform(target, handler);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                final StringBuilder buff = new StringBuilder();
                String msg;
                msg = MessageUtils.getMessage("DOTJ013E", target.toString()).toString();
                logger.error(buff.append(msg).append(e.getMessage()).toString());
            }
        }

        for (final URI ditamap : ditamapList) {
            final String currentMapPathName = FileUtils.getRelativeUnixPath(
                    tempInputMap.toString(), ditamap.toString());
            String mapPathFromInputMap = "";

            if (currentMapPathName.lastIndexOf(SLASH) != -1) {
                mapPathFromInputMap = currentMapPathName.substring(0,
                        currentMapPathName.lastIndexOf(SLASH));
            }

            ditamapIndexTermReader.setMapPath(mapPathFromInputMap);
            try {
                job.getStore().transform(ditamap, ditamapIndexTermReader);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                String msg;
                msg = MessageUtils.getMessage("DOTJ013E", ditamap.toString()).toString();
                logger.error(msg, e);
            }
        }
    }

}
