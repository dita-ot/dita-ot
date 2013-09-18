/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
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
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.tools.ant.Project;

import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.MessageUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.XmlDeclaration;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;


/**
* This class is for converting charset and escaping
* entities in html help component files.
*   Version 1.0 of this class was written by Zhang Di Hua in 2010-09-30 
* 	Version 1.1 modified by Louis Lecaroz in 2013-09-16: Full UTF-8 decoding on 4 bytes managing 1111998 chars instead of 65535, with entities encoding 
* 		instead of question marks on unknown characters in the destination charset. This is a simplified decoding with limited checkings on malformed UTF-8 contents
*
* @version 1.1 2013-09-16
*
* @author Zhang Di Hua
*/
public final class ConvertLang extends Task {
    private static final String tag1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    private static final String tag2 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>[OPTIONS]";
    private static final String tag3 = "&lt;?xml version=\"1.0\" encoding=\"utf-8\"?&gt;";

    private String basedir;

    private String outputdir;

    private String message;

    private String langcode;
    //charset map(e.g html = iso-8859-1)
    private final Map<String, String>charsetMap = new HashMap<String, String>();
    //lang map(e.g ar- = 0x0c01 Arabic (EGYPT))
    private final Map<String, String>langMap = new HashMap<String, String>();
    //entity map(e.g 38 = &amp;)
    private final Map<String, String>entityMap = new HashMap<String, String>();


    private DITAOTLogger logger;

    /**
     * Executes the Ant task.
     */
    @Override
    public void execute(){
        logger = new DITAOTAntLogger(getProject());
        logger.logInfo(message);

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
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
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
        final StringBuilder sb = new StringBuilder();
        sb.append(source.substring(0,startPos)).append(source.substring(endPos));
        return sb.toString();
    }

    private void convertHtmlCharset() {
        final File outputDir = new File(outputdir);
        final File[] files = outputDir.listFiles();
        for (final File file : files) {
            //Recursive method
            convertCharset(file);
        }

    }

    //Recursive method
    private void convertCharset(final File inputFile){
        if(inputFile.isDirectory()){
            final File[] files = inputFile.listFiles();
            for (final File file : files) {
                convertCharset(file);
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

                // Due to the DITA-OT framework, input should XHTML, so, force the XML Parser for analyzing the file
                org.jsoup.nodes.Document doc = Jsoup.parse(inputStream, UTF8, "", Parser.xmlParser());
    
                // Remove XML declaration
                if(doc.childNodeSize()!=0 &&  doc.childNode(0) instanceof XmlDeclaration) doc.childNode(0).remove();
                // Set the correct charset in the meta tag
                doc.head().select("meta[http-equiv]");
        		final Elements result=doc.head().select("meta[http-equiv~=(?i)content-type]");
        		for(org.jsoup.nodes.Element entry: result) {
        			StringBuffer newContentAttributeValue=new StringBuffer();
        			for(String currentValue:entry.attr("content").split(";")) { 
        				if(newContentAttributeValue.length()>0) newContentAttributeValue.append(';');

        				String regEx="(?i)^(\\s*charset\\s*)=\\s*(.*)\\s*$";
        				newContentAttributeValue.append(currentValue.replaceAll(regEx, "$1="+charsetMap.get(ATTRIBUTE_FORMAT_VALUE_HTML)));

        			}
        			entry.attr("content", newContentAttributeValue.toString());
        		}
                //convert charset
                final FileOutputStream outputStream = new FileOutputStream(outputFile);
                final OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, UTF8);
                //wrapped into writer
                writer = new BufferedWriter(streamWriter);
        		writer.write(doc.toString());
            } catch (final FileNotFoundException e) {
                logger.logError(e.getMessage(), e) ;
            } catch (final UnsupportedEncodingException e) {
            	throw new RuntimeException(e);
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        logger.logError("Failed to close input stream: " + e.getMessage());
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (final IOException e) {
                        logger.logError("Failed to close output stream: " + e.getMessage());
                    }
                }
            }
            try {
                //delete old file
                if (!inputFile.delete()) {
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
                }
                //rename newly created file to the old file
                if (!outputFile.renameTo(inputFile)) {
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    private void updateAllEntitiesAndLangs() {
        final File outputDir = new File(outputdir);
        final File[] files = outputDir.listFiles();
        for (final File file : files) {
            //Recursive method
            updateEntityAndLang(file);
        }

    }
    //Recursive method
    private void updateEntityAndLang(final File inputFile) {
        //directory case
        if(inputFile.isDirectory()){
            final File[] files = inputFile.listFiles();
            for (final File file : files) {
                updateEntityAndLang(file);
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
                logger.logError(e.getMessage(), e) ;
            } catch (final UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        logger.logError("Failed to close input stream: " + e.getMessage());
                    }
                }
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (final IOException e) {
                        logger.logError("Failed to close output stream: " + e.getMessage());
                    }
                }
            }
            try {
                //delete old file
                if (!inputFile.delete()) {
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
                }
                //rename newly created file to the old file
                if (!outputFile.renameTo(inputFile)) {
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }

    }

    private void convertEntityAndCharset(final File inputFile, final String format) {
        final String fileName = inputFile.getAbsolutePath();
        final File outputFile = new File(fileName + FILE_EXTENSION_TEMP);
        BufferedInputStream reader= null;
        BufferedWriter writer = null;
        try {
            //prepare for the input and output
            final FileInputStream inputStream = new FileInputStream(inputFile); // Open in binary instead of UTF8 as we now have our own code to manage a full UTF8 decoding
            // final InputStreamReader streamReader = new InputStreamReader(inputStream, UTF8); 
            //wrapped into reader
            // reader = new BufferedReader(inputStream);
            reader=new BufferedInputStream(inputStream);

            final FileOutputStream outputStream = new FileOutputStream(outputFile);
            //get new charset
            final String charset = charsetMap.get(format);
            //convert charset
            final OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, charset);
            //wrapped into writer
            writer = new BufferedWriter(streamWriter);

            //read a character
            for(int byteCode = reader.read(); byteCode!=-1; byteCode = reader.read()) {
            	ByteArrayOutputStream readBytesList = new ByteArrayOutputStream(); // Used for converting bytes into string
            	readBytesList.write((byte) byteCode);
    /*
            	Le premier octet d’une séquence UTF-8 valide ne peut prendre que les valeurs hexadécimales 00 à 7F ou C2 à F4 :

            	    le premier octet hexadécimal 00 à 7F d’une séquence n’est suivi d’aucun octet de continuation ;
            	    le premier octet hexadécimal C2 à DF d’une séquence est toujours suivi d’un seul octet de continuation (chacun de valeur hexadécimale entre 80 et BF) ;
            	    le premier octet hexadécimal E0 à EF d’une séquence est toujours suivi de deux octets de continuation (chacun de valeur hexadécimale entre 80 et BF) ;
            	        cependant, si le premier octet d’une séquence prend la valeur hexadécimale E0, le premier octet de continuation est restreint à une valeur hexadécimale entre A0 et BF ;
            	        cependant, si le premier octet d’une séquence prend la valeur hexadécimale ED, le premier octet de continuation est restreint à une valeur hexadécimale entre 80 et 9F ;
            	    le premier octet hexadécimal F0 à F4 d’une séquence est toujours suivi de trois octets de continuation (chacun de valeur hexadécimale entre 80 et BF) ;
            	        cependant, si le premier octet d’une séquence prend la valeur hexadécimale F0, le premier octet de continuation est restreint à une valeur hexadécimale entre 90 et BF ;
            	        cependant, si le premier octet d’une séquence prend la valeur hexadécimale F4, le premier octet de continuation est restreint à une valeur hexadécimale entre 80 et 8F.
    */
            	int utfCount=-1;	//  Le premier octet d’une séquence UTF-8 valide ne peut prendre que les valeurs hexadécimales 00 à 7F ou C2 à F4 :
            	int codePoint=byteCode; // This is the UTF-8 code point value which will contain the numeric value based on the UTF-8 binary coding
            	for(
        			int initialUtfCount=(
            			utfCount=byteCode>=0x00  && byteCode<=0x7F?0:( // 7 Bits code, nothing to do, already done
            				byteCode>=0xC2 && byteCode<=0xDF?1:( // Sequence of one byte following the starting code
            					byteCode>=0xE0 && byteCode<=0xEF?2:( // Sequence of two byte following the starting code
            						byteCode>=0xF0 && byteCode<=0xF4?3:-1 // Sequence of three byte following the starting code
            					)
            				)
            			)
        			);
        			utfCount>0; utfCount--
        		)	{
            		
            		// First loop, initialize the codePoint with the first read byte contained in the byteCode after having removd the sequence code
            		if(initialUtfCount==utfCount)  codePoint=(byteCode&(0xFF>>(utfCount+2)))<<(initialUtfCount*6);
            		
            		int nextByteCode=reader.read();
            		if(nextByteCode==-1) break;
            		
            		codePoint|=(nextByteCode&0x3F)<<((utfCount-1)*6); // Add next values in the code point afterin having removed sequence code and shift the content at the right place
            		readBytesList.write((byte) nextByteCode); 
         		}
            	if(utfCount!=0) throw new CharConversionException(); // utfCount must be zero or this means an UTF-8 malformed string 
            	
                if(entityMap.containsKey(String.valueOf(codePoint))) {
                    //get related entity
                    final String value = entityMap.get(String.valueOf(codePoint));
                    //write entity into output file
                    writer.write(value);
                }else {        	
        			//checking if the unicode char read previously can be converted into the output charset or put its entity instead of a question mark
                	String originalString= readBytesList.toString(UTF8); // Convert read bytes/UTF-8 sequence into a UTF-16 internal java string        	
                	String decodedString = new String(originalString.getBytes(charset),charset);
    	        	if(format.equals(ATTRIBUTE_FORMAT_VALUE_HTML) && !originalString.equals(decodedString)) writer.write("&#"+String.valueOf(codePoint)+";"); // If the decoded string does not match to the original one, there is a matching issue in the destination charset and, let's put its entity value 
    	        	else writer.write(originalString); // correctly converted, put the corresponding string
                }
            }
        } catch (final FileNotFoundException e) {
            logger.logError(e.getMessage(), e) ;
        } catch (final UnsupportedEncodingException e) {
            logger.logError(e.getMessage(), e) ;
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    logger.logError("Failed to close input stream: " + e.getMessage());
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (final IOException e) {
                    logger.logError("Failed to close output stream: " + e.getMessage());
                }
            }
        }
        try {
            //delete old file
            if (!inputFile.delete()) {
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            //rename newly created file to the old file
            if (!outputFile.renameTo(inputFile)) {
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }


        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
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
