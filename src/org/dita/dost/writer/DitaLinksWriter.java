/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * DitaLinksWriter reads dita topic file and insert map links information into it.
 * 
 * @author Zhang, Yuan Peng
 * 
 */
public class DitaLinksWriter extends AbstractXMLWriter {
    private String curMatchTopic;
    private boolean firstTopic; //Eric

    private HashMap<String, String> indexEntries;
    private Set<String> topicSet;
    private DITAOTJavaLogger logger;
    private boolean needResolveEntity;
    private OutputStreamWriter output;
    private XMLReader reader;
    private Stack<String> topicIdStack; // array list that is used to keep the hierarchy of topic id
    private boolean insideCDATA;
    private ArrayList<String> topicSpecList;  //Eric


    /**
     * Default constructor of DitaLinksWriter class.
     */
    public DitaLinksWriter() {
        super();
        topicIdStack = null;
        curMatchTopic = null;
        firstTopic = true;
        indexEntries = null;
        topicSet = null;
        needResolveEntity = false;
        output = null;
        insideCDATA = false;
        topicSpecList = new ArrayList<String>(); //Eric
        logger = new DITAOTJavaLogger();
        
        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
            //Edited by william on 2009-11-8 for ampbug:2893664 start
			reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
			reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
			//Edited by william on 2009-11-8 for ampbug:2893664 end
        } catch (Exception e) {
        	logger.logException(e);
        }

    }

    @Override
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    	if(needResolveEntity){
    		try {
    			if(insideCDATA)
    				output.write(ch, start, length);
    			else
    				output.write(StringUtils.escapeXML(ch, start, length));
    		} catch (Exception e) {
    			logger.logException(e);
    		}
    	}
    }
	
    @Override
    public void endCDATA() throws SAXException {
    	insideCDATA = false;
	    try{
	        output.write(Constants.CDATA_END);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    	if (topicSpecList.contains(localName)){//Eric
    		// Remove the last topic id.
    		if (!topicIdStack.empty()) topicIdStack.pop();
    		if (firstTopic) firstTopic = false;
    	}
        try {
             //Using the same type of logic that's used in DITAIndexWriter.
        	if (curMatchTopic != null && topicSpecList.contains(localName)) {
                 // if <prolog> don't exist
                output.write(Constants.RELATED_LINKS_HEAD);
                output.write(indexEntries.get(curMatchTopic));
                output.write(Constants.RELATED_LINKS_END);
                output.write(System.getProperty("line.separator"));
                curMatchTopic = null;
            }
            output.write(Constants.LESS_THAN + Constants.SLASH + qName 
                    + Constants.GREATER_THAN);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    @Override
    public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}


    @Override
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }


    @Override
    public void processingInstruction(String target, String data)
            throws SAXException {
        String pi;
        try {
            pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
            output.write(Constants.LESS_THAN + Constants.QUESTION 
                    + pi + Constants.QUESTION + Constants.GREATER_THAN);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }
    
    @Override
    public void setContent(Content content) {
        indexEntries = (HashMap<String, String>)content.getValue();
        topicSet = indexEntries.keySet();
    }
    
    @Override
    public void skippedEntity(String name) throws SAXException {
        try {
            output.write(StringUtils.getEntity(name));
        } catch (Exception e) {
        	logger.logException(e);
        }
    }
	
    @Override
    public void startCDATA() throws SAXException {
    	insideCDATA = true;
	    try{
	        output.write(Constants.CDATA_HEAD);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

    @Override
    public void startDocument() throws SAXException {
    	topicIdStack.clear();
    	firstTopic = true;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
		int attsLen = atts.getLength();

		//only care about adding related links to topics. 
		if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null) {// Eric

			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).contains(" topic/topic ")) {

				if (!topicSpecList.contains(localName)) {
					topicSpecList.add(localName);
				}
				
				if (!Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
					if (atts.getValue(Constants.ATTRIBUTE_NAME_ID) != null) {
						topicIdStack.push(atts.getValue(Constants.ATTRIBUTE_NAME_ID));
					}
				}
				
				if (curMatchTopic != null && !firstTopic) {

					try {
						output.write(Constants.RELATED_LINKS_HEAD);
						output.write(indexEntries.get(curMatchTopic));
						output.write(Constants.RELATED_LINKS_END);
						output.write(System.getProperty("line.separator"));
						curMatchTopic = null;
					} catch (Exception e) {
						if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null) {
							logger.logException(e);
						}
					}
				}
				String t = StringUtils.assembleString(topicIdStack, Constants.SLASH);
				if (topicSet.contains(t)) {
					curMatchTopic = t;
				} else if (topicSet.contains(topicIdStack.peek())) {
					curMatchTopic = topicIdStack.peek();
				}
				if (firstTopic) firstTopic = false;
			}
		}
		try {  //Eric

			output.write(Constants.LESS_THAN + qName);
			for (int i = 0; i < attsLen; i++) {
				String attQName = atts.getQName(i);
				String attValue;
				attValue = atts.getValue(i);

				// replace '&' with '&amp;'
				// if (attValue.indexOf('&') > 0) {
				// attValue = StringUtils.replaceAll(attValue, "&", "&amp;");
				// }
				attValue = StringUtils.escapeXML(attValue);

				output.write(new StringBuffer().append(Constants.STRING_BLANK)
						.append(attQName).append(Constants.EQUAL).append(
								Constants.QUOTATION).append(attValue).append(
								Constants.QUOTATION).toString());  //Eric
			}
			output.write(Constants.GREATER_THAN);
			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS)!=null 
					&& atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).indexOf(" topic/related-links ") != -1
					&& curMatchTopic != null) {
				output.write(indexEntries.get(curMatchTopic));
				curMatchTopic = null;
			}

		} catch (Exception e) {
			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null) {
				logger.logException(e);
			}// prevent printing stack trace when meeting <dita> which has no
				// class attribute
		}
	}

    @Override
    public void startEntity(String name) throws SAXException {
		try {
           	needResolveEntity = StringUtils.checkEntity(name);
           	if(!needResolveEntity){
           		output.write(StringUtils.getEntity(name));
           	}
        } catch (Exception e) {
        	logger.logException(e);
        }
        
	}

    @Override
    public void write(String filename) {
		String file = null;
		File inputFile = null;
		File outputFile = null;
		FileOutputStream fileOutput = null;

        try {
        	
        	file = filename;
        	curMatchTopic = topicSet.contains(Constants.SHARP) ? Constants.SHARP : null;
            
            // ignore in-exists file
            if (file == null || !new File(file).exists()) {
            	return;
            }
            
        	needResolveEntity = true;
            topicIdStack = new Stack<String>();
            inputFile = new File(file);
            outputFile = new File(file + Constants.FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, Constants.UTF8);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(file));
            reader.parse(file);
            output.close();
            
            if(!inputFile.delete()){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());

            }
            if(!outputFile.renameTo(inputFile)){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
        } catch (Exception e) {
        	logger.logException(e);
        }finally {
            try {
            	if (fileOutput != null) {
            		fileOutput.close();
            	}
            }catch (Exception e) {
				logger.logException(e);
            }
        }
    }
}
