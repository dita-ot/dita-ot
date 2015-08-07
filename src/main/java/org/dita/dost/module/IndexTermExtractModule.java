/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import org.dita.dost.util.XMLUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * This class extends AbstractPipelineModule, used to extract indexterm from
 * dita/ditamap files.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
final class IndexTermExtractModule extends AbstractPipelineModuleImpl {
    /** The input map */
    private String inputMap = null;

    /** The extension of the target file */
    private String targetExt = null;

    /** The list of topics */
    private List<String> topicList = null;

    /** The list of ditamap files */
    private List<String> ditamapList = null;

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
        indexTermCollection = IndexTermCollection.getInstantce();
        try {
            indexTermCollection.clear();
            parseAndValidateInput(input);
            extractIndexTerm();
            indexTermCollection.sort();
            indexTermCollection.outputTerms();
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
        inputMap = input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP);
        targetExt = input.getAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT);

        /*
         * Parse topic list and ditamap list from the input dita.list file
         */
        topicList = new ArrayList<>();
        for (final FileInfo f: job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITA.equals(f.format) && !f.isResourceOnly) {
                topicList.add(f.file.getPath());
            }
        }
        ditamapList = new ArrayList<>();
        for (final FileInfo f: job.getFileInfo()) {
            if (ATTR_FORMAT_VALUE_DITAMAP.equals(f.format) && !f.isResourceOnly) {
                ditamapList.add(f.file.getPath());
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
        final int topicNum = topicList.size();
        final int ditamapNum = ditamapList.size();
        FileInputStream inputStream = null;
        XMLReader xmlReader = null;
        final IndexTermReader handler = new IndexTermReader(indexTermCollection);
        handler.setLogger(logger);
        final DitamapIndexTermReader ditamapIndexTermReader = new DitamapIndexTermReader(indexTermCollection, true);
        ditamapIndexTermReader.setLogger(logger);
        
        xmlReader = XMLUtils.getXMLReader();

        try {
            xmlReader.setContentHandler(handler);

            for (String aTopicList : topicList) {
                String target;
                String targetPathFromMap;
                String targetPathFromMapWithoutExt;
                handler.reset();
                target = aTopicList;
                targetPathFromMap = FileUtils.getRelativeUnixPath(
                        inputMap, target);
                targetPathFromMapWithoutExt = targetPathFromMap
                        .substring(0, targetPathFromMap.lastIndexOf("."));
                handler.setTargetFile(targetPathFromMapWithoutExt + targetExt);

                try {
                    /*if(!new File(job.tempDir, target).exists()){
                        logger.logWarn("Cannot find file "+ target);
						continue;
					}*/
                    inputStream = new FileInputStream(
                            new File(job.tempDir, target));
                    xmlReader.parse(new InputSource(inputStream));
                    inputStream.close();
                } catch (final Exception e) {
                    final StringBuilder buff = new StringBuilder();
                    String msg = null;
                    msg = MessageUtils.getInstance().getMessage("DOTJ013E", target).toString();
                    logger.error(buff.append(msg).append(e.getMessage()).toString());
                }
            }

            xmlReader.setContentHandler(ditamapIndexTermReader);

            for (final String ditamap : ditamapList) {
                final String currentMapPathName = FileUtils.getRelativeUnixPath(
                        inputMap, ditamap);
                String mapPathFromInputMap = "";

                if (currentMapPathName.lastIndexOf(SLASH) != -1) {
                    mapPathFromInputMap = currentMapPathName.substring(0,
                            currentMapPathName.lastIndexOf(SLASH));
                }

                ditamapIndexTermReader.setMapPath(mapPathFromInputMap);
                try {
                    /*if(!new File(job.tempDir, ditamap).exists()){
                        logger.logWarn("Cannot find file "+ ditamap);
						continue;
					}*/
                    inputStream = new FileInputStream(new File(job.tempDir,
                            ditamap));
                    xmlReader.parse(new InputSource(inputStream));
                    inputStream.close();
                } catch (final Exception e) {
                    String msg = null;
                    msg = MessageUtils.getInstance().getMessage("DOTJ013E", ditamap).toString();
                    logger.error(msg, e);
                }
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }

            }
        }
    }

}
