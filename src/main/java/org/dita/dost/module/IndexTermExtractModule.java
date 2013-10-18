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
import java.util.Properties;
import java.util.Set;

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
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;
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

    /** The basedir of the input file for parsing */
    private String baseInputDir = null;

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
            logger.logError(e.getMessage(), e) ;
        }

        return null;
    }

    private void parseAndValidateInput(final AbstractPipelineInput input)
            throws DITAOTException {
        final String baseDir = input.getAttribute(ANT_INVOKER_PARAM_BASEDIR);
        final File tempDir = new File(input.getAttribute(ANT_INVOKER_PARAM_TEMPDIR));
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        String output = input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUT);
        if (!new File(output).isAbsolute()) {
            output = new File(baseDir, output).getAbsolutePath();
        }
        final String encoding = input.getAttribute(ANT_INVOKER_EXT_PARAM_ENCODING);
        final String indextype = input.getAttribute(ANT_INVOKER_EXT_PARAM_INDEXTYPE);
        final String indexclass = input.getAttribute(ANT_INVOKER_EXT_PARAM_INDEXCLASS);
        inputMap = input.getAttribute(ANT_INVOKER_PARAM_INPUTMAP);
        targetExt = input.getAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT);
        baseInputDir = tempDir.getAbsolutePath();

        /*
         * Parse topic list and ditamap list from the input dita.list file
         */
        topicList = new ArrayList<String>();
        for (final FileInfo f: job.getFileInfo()) {
            if ("dita".equals(f.format) && f.isActive && !f.isResourceOnly) {
                topicList.add(f.file.getPath());
            }
        }
        ditamapList = new ArrayList<String>();
        for (final FileInfo f: job.getFileInfo()) {
            if ("ditamap".equals(f.format) && f.isActive && !f.isResourceOnly) {
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
        
        xmlReader = StringUtils.getXMLReader();

        try {
            xmlReader.setContentHandler(handler);

            for (int i = 0; i < topicNum; i++) {
                String target;
                String targetPathFromMap;
                String targetPathFromMapWithoutExt;
                handler.reset();
                target = topicList.get(i);
                targetPathFromMap = FileUtils.getRelativeUnixPath(
                        inputMap, target);
                targetPathFromMapWithoutExt = targetPathFromMap
                        .substring(0, targetPathFromMap.lastIndexOf("."));
                handler.setTargetFile(new StringBuffer(
                        targetPathFromMapWithoutExt).append(targetExt)
                        .toString());

                try {
                    /*if(!new File(baseInputDir, target).exists()){
						logger.logWarn("Cannot find file "+ target);
						continue;
					}*/
                    inputStream = new FileInputStream(
                            new File(baseInputDir, target));
                    xmlReader.parse(new InputSource(inputStream));
                    inputStream.close();
                } catch (final Exception e) {
                    final StringBuffer buff=new StringBuffer();
                    String msg = null;
                    msg = MessageUtils.getInstance().getMessage("DOTJ013E", target).toString();
                    logger.logError(buff.append(msg).append(e.getMessage()).toString());
                }
            }

            xmlReader.setContentHandler(ditamapIndexTermReader);

            for (int j = 0; j < ditamapNum; j++) {
                final String ditamap = ditamapList.get(j);
                final String currentMapPathName = FileUtils.getRelativeUnixPath(
                        inputMap, ditamap);
                String mapPathFromInputMap = "";

                if (currentMapPathName.lastIndexOf(SLASH) != -1) {
                    mapPathFromInputMap = currentMapPathName.substring(0,
                            currentMapPathName.lastIndexOf(SLASH));
                }

                ditamapIndexTermReader.setMapPath(mapPathFromInputMap);
                try {
                    /*if(!new File(baseInputDir, ditamap).exists()){
						logger.logWarn("Cannot find file "+ ditamap);
						continue;
					}*/
                    inputStream = new FileInputStream(new File(baseInputDir,
                            ditamap));
                    xmlReader.parse(new InputSource(inputStream));
                    inputStream.close();
                } 	catch (final Exception e) {
                    String msg = null;
                    msg = MessageUtils.getInstance().getMessage("DOTJ013E", ditamap).toString();
                    logger.logError(msg, e) ;
                }
            }
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                    logger.logError(e.getMessage(), e) ;
                }

            }
        }
    }

}
