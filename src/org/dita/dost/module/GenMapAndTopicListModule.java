/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.PropertiesWriter;
import org.xml.sax.SAXException;

/**
 * This class extends AbstractPipelineModule, used to generate map and topic
 * list by parsing all the refered dita files.
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public class GenMapAndTopicListModule extends AbstractPipelineModule {
    /** Set of all dita files */
    private Set ditaSet = null;

    /** Set of all topic files */
    private Set fullTopicSet = null;

    /** Set of all map files */
    private Set fullMapSet = null;

    /** Set of topic files containing href */
    private Set hrefTopicSet = null;

    /** Set of map files containing href */
    private Set hrefMapSet = null;

    /** Set of dita files containing conref */
    private Set conrefSet = null;

    /** Set of all images */
    private Set imageSet = null;

    /** Set of all html files */
    private Set htmlSet = null;

    /** Set of all the href targets */
    private Set hrefTargetSet = null;

    /** List of files waiting for parsing */
    private List waitList = null;

    /** List of parsed files */
    private List doneList = null;
    
    /** Basedir for processing */
    private String baseDir = null;
    
    /** Tempdir for processing */
    private String tempDir = null;
    
    /** ditadir for processing */
    private String ditaDir = null;

    /**
     * Create a new instance and do the initialization.
     * 
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public GenMapAndTopicListModule() throws SAXException,
            ParserConfigurationException {
        ditaSet = new HashSet();
        fullTopicSet = new HashSet();
        fullMapSet = new HashSet();
        hrefTopicSet = new HashSet();
        hrefMapSet = new HashSet();
        conrefSet = new HashSet();
        imageSet = new HashSet();
        htmlSet = new HashSet();
        hrefTargetSet = new HashSet();
        waitList = new LinkedList();
        doneList = new LinkedList();
    }

    /**
     * Execute the module.
     * 
     * @param input
     * @return
     */
    public AbstractPipelineOutput execute(AbstractPipelineInput input) {
        try {
            String inputFile = ((PipelineHashIO) input)
                    .getAttribute(Constants.ANT_INVOKER_PARAM_INPUTMAP);
            baseDir = ((PipelineHashIO) input)
                    .getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
            tempDir = ((PipelineHashIO) input)
                    .getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
            ditaDir = ((PipelineHashIO) input)
                    .getAttribute(Constants.ANT_INVOKER_EXT_PARAM_DITADIR);

            // Initiate the waitingList
            addToWaitList(inputFile);

            // Add the input file into href targets
            if (inputFile.toLowerCase().endsWith(Constants.FILE_EXTENSION_DITA)
                    || inputFile.toLowerCase().endsWith(
                            Constants.FILE_EXTENSION_XML)) {
                hrefTargetSet.add(new File(inputFile).getPath());
            }

            // Parse all the file in the waitingList
            processWaitList();

            outputResult();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param baseDir
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    private void processWaitList() throws SAXException,
            ParserConfigurationException {
        GenListModuleReader reader = new GenListModuleReader();
        File fileToParse = null;

        reader.initXMLReader(ditaDir);

        while (!waitList.isEmpty()) {
            String currentFile = (String) waitList.remove(0);
            fileToParse = new File(baseDir, currentFile);

            if (fileToParse.exists()){
            	doneList.add(currentFile);
            	reader.setCurrentDir(new File(currentFile).getParent());
            	reader.parse(fileToParse);
            	saveParseResult(reader, currentFile);
            }else{
            	// TO DO: report the File Not Found error in the log system
            }
        }
    }

    /**
     * @param currentFile
     */
    private void saveParseResult(GenListModuleReader reader, String currentFile) {
        String lcasecf = currentFile.toLowerCase();
        Iterator iter = reader.getResult().iterator();

        while (iter.hasNext()) {
            String item = (String) iter.next();
            String lcaseItem = item.toLowerCase();

            addToWaitList(item);

            if (lcaseItem.endsWith(Constants.FILE_EXTENSION_JPG)
                    || lcaseItem.endsWith(Constants.FILE_EXTENSION_GIF)
                    || lcaseItem.endsWith(Constants.FILE_EXTENSION_EPS)) {
                imageSet.add(item);
            }

            if (lcaseItem.endsWith(Constants.FILE_EXTENSION_HTML)) {
                htmlSet.add(item);
            }
        }

        ditaSet.add(currentFile);
        hrefTargetSet.addAll(reader.getHrefTargets());

        if (reader.hasConRef()) {
            conrefSet.add(currentFile);
        }

        if (lcasecf.endsWith(Constants.FILE_EXTENSION_DITA)
                || lcasecf.endsWith(Constants.FILE_EXTENSION_XML)) {
            fullTopicSet.add(currentFile);
            if (reader.hasHref()) {
                hrefTopicSet.add(currentFile);
            }
        }

        if (lcasecf.endsWith(Constants.FILE_EXTENSION_DITAMAP)) {
            fullMapSet.add(currentFile);
            if (reader.hasHref()) {
                hrefMapSet.add(currentFile);
            }
        }
    }

    /**
     * Add the given file into the wait list, if it is a dita file and has not
     * been parsed.
     * 
     * @param fileName
     */
    private void addToWaitList(String fileName) {
    	String normalizedFileName = new File(fileName).getPath();
        String lcasefn = normalizedFileName.toLowerCase();
        boolean isDitaFile = lcasefn.endsWith(Constants.FILE_EXTENSION_DITA)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_XML)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_DITAMAP);

        if (!isDitaFile) {
            return;
        }
        
        if (doneList.contains(normalizedFileName) || waitList.contains(normalizedFileName)) {
            return;
        }
                
        waitList.add(normalizedFileName);
    }

    /**
     * @param tempDir
     */
    private void outputResult() {
        Properties prop = new Properties();
        PropertiesWriter writer = new PropertiesWriter();
        Content content = new ContentImpl();
        File dir = new File(tempDir);

        if (!dir.exists()) {
            dir.mkdirs();
        }

        /*
         * In ant, all the file separator should be slash, so we need to replace
         * all the back slash with slash.
         */
        prop.put(Constants.FULL_DITAMAP_TOPIC_LIST, StringUtils.assembleString(ditaSet,
                Constants.COMMA).replaceAll(Constants.DOUBLE_BACK_SLASH,
                Constants.SLASH));
        prop.put(Constants.FULL_DITA_TOPIC_LIST, StringUtils.assembleString(
                fullTopicSet, Constants.COMMA).replaceAll(
                Constants.DOUBLE_BACK_SLASH, Constants.SLASH));
        prop.put(Constants.FULL_DITAMAP_LIST, StringUtils.assembleString(
                fullMapSet, Constants.COMMA).replaceAll(
                Constants.DOUBLE_BACK_SLASH, Constants.SLASH));
        prop.put(Constants.HREF_DITA_TOPIC_LIST, StringUtils.assembleString(
                hrefTopicSet, Constants.COMMA).replaceAll(
                Constants.DOUBLE_BACK_SLASH, Constants.SLASH));
        prop.put(Constants.CONREF_LIST, StringUtils.assembleString(conrefSet,
                Constants.COMMA).replaceAll(Constants.DOUBLE_BACK_SLASH,
                Constants.SLASH));
        prop.put(Constants.IMAGE_LIST, StringUtils.assembleString(imageSet,
                Constants.COMMA).replaceAll(Constants.DOUBLE_BACK_SLASH,
                Constants.SLASH));
        prop.put(Constants.HTML_LIST, StringUtils.assembleString(htmlSet,
                Constants.COMMA).replaceAll(Constants.DOUBLE_BACK_SLASH,
                Constants.SLASH));
        prop.put(Constants.HREF_TARGET_LIST, StringUtils.assembleString(
                hrefTargetSet, Constants.COMMA).replaceAll(
                Constants.DOUBLE_BACK_SLASH, Constants.SLASH));

        content.setValue(prop);
        writer.setContent(content);
        writer.write(new StringBuffer(tempDir).append(Constants.FILE_SEPARATOR)
                .append(Constants.FILE_NAME_DITA_LIST).toString());
    }

}