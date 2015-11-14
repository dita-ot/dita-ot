/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.apache.commons.io.FileUtils.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.DITAOTAntLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is for converting charset and escaping
 * entities in html help component files.
 * 
 * @version 1.0 2010-09-30
 * 
 * @author Zhang Di Hua
 */
public final class ConvertLang extends Task {
    
    private static final String ATTRIBUTE_FORMAT_VALUE_WINDOWS = "windows";
    private static final String ATTRIBUTE_FORMAT_VALUE_HTML = "html";

    private static final String tag1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String tag2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>[OPTIONS]";
    private static final String tag3 = "&lt;?xml version=\"1.0\" encoding=\"utf-8\"?&gt;";

    private String basedir;

    private String outputdir;

    private String message;

    private String langcode;
    //charset map(e.g html = iso-8859-1)
    private final Map<String, String>charsetMap = new HashMap<>();
    //lang map(e.g ar- = 0x0c01 Arabic (EGYPT))
    private final Map<String, String>langMap = new HashMap<>();
    //entity map(e.g 38 = &amp;)
    private final Map<String, String>entityMap = new HashMap<>();


    private DITAOTLogger logger;

    /**
     * Executes the Ant task.
     */
    @Override
    public void execute(){
        logger = new DITAOTAntLogger(getProject());
        logger.info(message);

        //ensure outdir is absolute
        if (!new File(outputdir).isAbsolute()) {
            outputdir = new File(basedir, outputdir).getAbsolutePath();
        }

        //initialize language map
        createLangMap();
        //initialize entity map
        createEntityMap();
        //initialize charset map
        createCharsetMap();
        //change charset of html files
        convertHtmlCharset();
        //update entity and lang code
        updateAllEntitiesAndLangs();
    }

    private void createLangMap() {

        final Properties entities = new Properties();
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/util/languages.properties");
            entities.load(in);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read language property file: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {}
            }
        }
        for (final Entry<Object, Object> e: entities.entrySet()) {
            langMap.put((String) e.getKey(), (String) e.getValue());
        }

    }

    private void createEntityMap(){

        final Properties entities = new Properties();
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/util/entities.properties");
            entities.load(in);
        } catch (final IOException e) {
            throw new RuntimeException("Failed to read entities property file: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {}
            }
        }
        for (final Entry<Object, Object> e: entities.entrySet()) {
            entityMap.put((String) e.getKey(), (String) e.getValue());
        }

    }

    private void createCharsetMap() {
        InputStream in = null;
        try {
            in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/util/codepages.xml");
            final DocumentBuilder builder = XMLUtils.getDocumentBuilder();
            final Document doc = builder.parse(in);
            final Element root = doc.getDocumentElement();
            final NodeList childNodes = root.getChildNodes();
            //search the node with langcode
            for(int i = 0; i< childNodes.getLength(); i++){
                final Node node = childNodes.item(i);
                //only for element node
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    final Element e = (Element)node;
                    final String lang = e.getAttribute(ATTRIBUTE_NAME_LANG);
                    //node found
                    if(langcode.equalsIgnoreCase(lang)||
                            lang.startsWith(langcode)){
                        //store the value into a map
                        //charsetMap = new HashMap<String, String>();
                        //iterate child nodes skip the 1st one
                        final NodeList subChild = e.getChildNodes();
                        for(int j = 0; j< subChild.getLength(); j++){
                            final Node subNode = subChild.item(j);
                            if(subNode.getNodeType() == Node.ELEMENT_NODE){
                                final Element elem = (Element)subNode;
                                final String format = elem.getAttribute(ATTRIBUTE_NAME_FORMAT);
                                final String charset = elem.getAttribute(ATTRIBUTE_NAME_CHARSET);
                                //store charset into map
                                charsetMap.put(format, charset);
                            }

                        }
                        break;
                    }
                }
            }
            //no matched charset is found set default value en-us
            if(charsetMap.size() == 0){
                charsetMap.put(ATTRIBUTE_FORMAT_VALUE_HTML, "iso-8859-1");
                charsetMap.put(ATTRIBUTE_FORMAT_VALUE_WINDOWS, "windows-1252");
            }
        } catch (final Exception e) {
            throw new RuntimeException("Failed to read charset configuration file: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {}
            }
        }
    }

    private String replaceXmlTag(final String source,final String tag){
        final int startPos = source.indexOf(tag);
        final int endPos = startPos + tag.length();
        return source.substring(0, startPos) + source.substring(endPos);
    }

    private void convertHtmlCharset() {
        final File outputDir = new File(outputdir);
        final File[] files = outputDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                //Recursive method
                convertCharset(file);
            }
        }
    }
    //Recursive method
    private void convertCharset(final File inputFile){
        if(inputFile.isDirectory()){
            final File[] files = inputFile.listFiles();
            if (files != null) {
                for (final File file : files) {
                    convertCharset(file);
                }
            }
        }else if(FileUtils.isHTMLFile(inputFile.getName())||
                FileUtils.isHHCFile(inputFile.getName())||
                FileUtils.isHHKFile(inputFile.getName())){

            final String fileName = inputFile.getAbsolutePath();
            final File outputFile = new File(fileName + FILE_EXTENSION_TEMP);
            log("Processing " + fileName, Project.MSG_INFO);
            BufferedReader reader = null;
            Writer writer = null;
            try {
                //prepare for the input and output
                final FileInputStream inputStream = new FileInputStream(inputFile);
                final InputStreamReader streamReader = new InputStreamReader(inputStream, UTF8);
                reader = new BufferedReader(streamReader);

                final FileOutputStream outputStream = new FileOutputStream(outputFile);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, UTF8);
                writer = new BufferedWriter(streamWriter);

                String value = reader.readLine();
                while(value != null){
                    //meta tag contains charset found
                    if(value.contains("<meta http-equiv") && value.contains("charset")){
                        final int insertPoint = value.indexOf("charset=") + "charset=".length();
                        final String subString = value.substring(0, insertPoint);
                        final int remainIndex = value.indexOf(UTF8) + UTF8.length();
                        final String remainString = value.substring(remainIndex);
                        //change the charset
                        final String newValue = subString + charsetMap.get(ATTRIBUTE_FORMAT_VALUE_HTML) + remainString;
                        //write into the output file
                        writer.write(newValue);
                        //add line break
                        writer.write(LINE_SEPARATOR);
                    }else{
                        if(value.contains(tag1)){
                            value = replaceXmlTag(value,tag1);
                        }else if(value.contains(tag2)){
                            value = replaceXmlTag(value,tag2);
                        }else if(value.contains(tag3)){
                            value = replaceXmlTag(value,tag3);
                        }

                        //other values
                        writer.write(value);
                        writer.write(LINE_SEPARATOR);
                    }
                    value = reader.readLine();
                }
            } catch (final FileNotFoundException e) {
                logger.error(e.getMessage(), e) ;
            } catch (final UnsupportedEncodingException e) {
            	throw new RuntimeException(e);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e) ;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        logger.error("Failed to close input stream: " + e.getMessage());
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (final IOException e) {
                        logger.error("Failed to close output stream: " + e.getMessage());
                    }
                }
            }
            try {
                deleteQuietly(inputFile);
                moveFile(outputFile, inputFile);
            } catch (final Exception e) {
                logger.error("Failed to replace " + inputFile + ": " + e.getMessage());
            }
        }
    }

    private void updateAllEntitiesAndLangs() {
        final File outputDir = new File(outputdir);
        final File[] files = outputDir.listFiles();
        if (files != null) {
            for (final File file : files) {
                //Recursive method
                updateEntityAndLang(file);
            }
        }
    }
    //Recursive method
    private void updateEntityAndLang(final File inputFile) {
        //directory case
        if(inputFile.isDirectory()){
            final File[] files = inputFile.listFiles();
            if (files != null) {
                for (final File file : files) {
                    updateEntityAndLang(file);
                }
            }
        }
        //html/hhc/hhk file case
        else if(FileUtils.isHTMLFile(inputFile.getName())||
                FileUtils.isHHCFile(inputFile.getName())||
                FileUtils.isHHKFile(inputFile.getName())){
            //do converting work
            convertEntityAndCharset(inputFile, ATTRIBUTE_FORMAT_VALUE_HTML);

        }
        //hhp file case
        else if(FileUtils.isHHPFile(inputFile.getName())){
            //do converting work
            convertEntityAndCharset(inputFile, ATTRIBUTE_FORMAT_VALUE_WINDOWS);
            //update language setting of hhp file
            final String fileName = inputFile.getAbsolutePath();
            final File outputFile = new File(fileName + FILE_EXTENSION_TEMP);
            //get new charset
            final String charset = charsetMap.get(ATTRIBUTE_FORMAT_VALUE_WINDOWS);
            BufferedReader reader = null;
            BufferedWriter writer = null;
            try {
                //prepare for the input and output
                final FileInputStream inputStream = new FileInputStream(inputFile);
                final InputStreamReader streamReader = new InputStreamReader(inputStream, charset);
                //wrapped into reader
                reader = new BufferedReader(streamReader);

                final FileOutputStream outputStream = new FileOutputStream(outputFile);

                //convert charset
                final OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, charset);
                //wrapped into writer
                writer = new BufferedWriter(streamWriter);

                String value = reader.readLine();
                while(value != null){
                    if(value.contains(tag1)){
                        value = replaceXmlTag(value,tag1);
                    }else if(value.contains(tag2)){
                        value = replaceXmlTag(value,tag2);
                    }else if(value.contains(tag3)){
                        value = replaceXmlTag(value,tag3);
                    }

                    //meta tag contains charset found
                    if(value.contains("Language=")){
                        String newValue = langMap.get(langcode);
                        if (newValue == null) {
                            newValue = langMap.get(langcode.split("-")[0]);
                        }
                        if (newValue != null) {
                            writer.write("Language=" + newValue);
                            writer.write(LINE_SEPARATOR);
                        } else {
                            throw new IllegalArgumentException("Unsupported language code '" + langcode + "', unable to map to a Locale ID.");
                        }

                    }else{
                        //other values
                        writer.write(value);
                        writer.write(LINE_SEPARATOR);
                    }
                    value = reader.readLine();
                }
            } catch (final FileNotFoundException e) {
                logger.error(e.getMessage(), e) ;
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e) ;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        logger.error("Failed to close input stream: " + e.getMessage());
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (final IOException e) {
                        logger.error("Failed to close output stream: " + e.getMessage());
                    }
                }
            }
            try {
                deleteQuietly(inputFile);
                moveFile(outputFile, inputFile);
            } catch (final Exception e) {
                logger.error("Failed to replace " + inputFile + ": " + e.getMessage());
            }
        }

    }

    private void convertEntityAndCharset(final File inputFile, final String format) {
        final String fileName = inputFile.getAbsolutePath();
        final File outputFile = new File(fileName + FILE_EXTENSION_TEMP);
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            //prepare for the input and output
            final FileInputStream inputStream = new FileInputStream(inputFile);
            final InputStreamReader streamReader = new InputStreamReader(inputStream, UTF8);
            //wrapped into reader
            reader = new BufferedReader(streamReader);

            final FileOutputStream outputStream = new FileOutputStream(outputFile);
            //get new charset
            final String charset = charsetMap.get(format);
            //convert charset
            final OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, charset);
            //wrapped into writer
            writer = new BufferedWriter(streamWriter);

            //read a character
            int charCode = reader.read();
            while(charCode != -1){
                final String key = String.valueOf(charCode);
                //Is an entity char
                if(entityMap.containsKey(key)){
                    //get related entity
                    final String value = entityMap.get(key);
                    //write entity into output file
                    writer.write(value);
                }else{
                    //normal process
                    writer.write(charCode);
                }
                charCode = reader.read();
            }
        } catch (final FileNotFoundException e) {
            logger.error(e.getMessage(), e) ;
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    logger.error("Failed to close input stream: " + e.getMessage());
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException e) {
                    logger.error("Failed to close output stream: " + e.getMessage());
                }
            }
        }
        try {
            deleteQuietly(inputFile);
            moveFile(outputFile, inputFile);
        } catch (final Exception e) {
            logger.error("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }

    public void setBasedir(final String basedir) {
        this.basedir = basedir;
    }

    public void setLangcode(final String langcode) {
        this.langcode = langcode;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setOutputdir(final String outputdir) {
        this.outputdir = outputdir;
    }

}
