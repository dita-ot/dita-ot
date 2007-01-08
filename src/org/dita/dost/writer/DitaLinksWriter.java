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
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * DitaLinksWriter reads dita topic file and insert map links information into it.
 * 
 * @author Zhang, Yuan Peng
 * 
 */
public class DitaLinksWriter extends AbstractXMLWriter {
    private String firstMatchTopic;
    private boolean hasRelatedlinksTillNow;// whether there is <related-links> in thisfile
    private boolean hasWritten; //Eric

    private String indexEntries;
    private String lastMatchTopic;
    private int level; // level of the element
    private DITAOTJavaLogger logger;
    private int matchLevel; // the level of the topic to insert into
    private List matchList; // topic path that topicIdList need to match
    private boolean needResolveEntity;
    private OutputStreamWriter output;
    private XMLReader reader;
    private boolean startTopic; //whether to insert links at this topic
    private List topicIdList; // array list that is used to keep the hierarchy of topic id
    private boolean insideCDATA;
    private ArrayList topicSpecList;  //Eric


    /**
     * Default constructor of DitaLinksWriter class.
     */
    public DitaLinksWriter() {
        super();
        topicIdList = null;
        firstMatchTopic = null;
        hasRelatedlinksTillNow = false;
        hasWritten = false; //Eric
        indexEntries = null;
        lastMatchTopic = null;
        level = -1; //Eric
       // matchLevel = -1; //Eric
        matchList = null;
        needResolveEntity = false;
        output = null;
        startTopic = false;
        insideCDATA = false;
	topicSpecList = new ArrayList(); //Eric
        logger = new DITAOTJavaLogger();
        
        try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
            	StringUtils.initSaxDriver();
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
        } catch (Exception e) {
        	logger.logException(e);
        }

    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     * 
     */
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
	
//Commnented out the checkLink methods so that we use the same type of logic	
//for both DitaIndexWriter and DitaLinksWriter
//	private boolean checkLinkAtEnd(){ //Eric
//		if(!hasRelatedlinksTillNow){
//			if( level == -1){ //Eric
//				return true;
//			}
//		}
//		return false;
//	}
	
//	private boolean checkLinkAtStart(Attributes atts){
//		//if (!hasRelatedlinksTillNow && level > 1){
//		if (!hasRelatedlinksTillNow){
//			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).indexOf("topic/topic") != -1){
//				
////				
////					logger.logDebug("*****"+ atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) +"******");
//					if (level > 0){
//						level++;
//					}else if (level < 0){ 
//						logger.logDebug("***** LEVEL "+ level +"******");
////						This is equivalent to initializing level and increasing the veluw by one
//						// ---- topicLevel=0; ----
//						// ---- topicLevel++; ----
//						level = 1;
//					}else {
//						return false;
//					}
//					return false;  //Eric
//					
//				
//				
//			}
//		}
//		return true;
//	}
    
//  check whether the hierarchy of current node match the matchList
    private boolean checkMatch() {
        
        int matchSize = matchList.size();
        int ancestorSize = topicIdList.size();
        ListIterator matchIterator = matchList.listIterator();
        ListIterator ancestorIterator = topicIdList.listIterator(ancestorSize
                - matchSize);
        String match;
        String ancestor;
        
		if (matchList == null){
			return true;
		}
        
        while (matchIterator.hasNext()) {
            match = (String) matchIterator.next();
            ancestor = (String) ancestorIterator.next();
            if (!match.equals(ancestor)) {
                return false;
            }
        }
        return true;
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     * 
     */
    public void endCDATA() throws SAXException {
    	insideCDATA = false;
	    try{
	        output.write(Constants.CDATA_END);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     * 
     */
    public void endDocument() throws SAXException {
        //Reset the level back to it initial state once we finished processing the document.
    	level = -1;  //Eric
        try {
            output.flush();
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     * 
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    	if (topicSpecList.contains(localName)){//Eric
    		level--;
    	}
        if (!startTopic){
        	topicIdList.clear();
        }
        try {
             //Using the same type of logic that's used in DITAIndexWriter.
        	 if (!hasRelatedlinksTillNow && startTopic && !hasWritten && topicSpecList.contains(localName)) { //Eric
                 // if <prolog> don't exist
                output.write(Constants.RELATED_LINKS_HEAD);
                output.write(indexEntries);
                output.write(Constants.RELATED_LINKS_END);
                output.write(System.getProperty("line.separator"));
                hasRelatedlinksTillNow = true;
                 hasWritten = true;
            }
            output.write(Constants.LESS_THAN + Constants.SLASH + qName 
                    + Constants.GREATER_THAN);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     * 
     */
    public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}


    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     * 
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }


    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     * 
     */
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
    
    /**
     * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
     * 
     */
    public void setContent(Content content) {
        indexEntries = (String) content.getValue();
    }
    
    private void setMatch(String match) {
		int index = 0;
        matchList = new ArrayList(Constants.INT_16);
        
        firstMatchTopic = (match.indexOf(Constants.SLASH) != -1) ? match.substring(0, match.indexOf(Constants.SLASH)) : match;

        while (index != -1) {
            int end = match.indexOf(Constants.SLASH, index);
            if (end == -1) {
                matchList.add(match.substring(index));
                lastMatchTopic = match.substring(index);
                index = end;
            } else {
                matchList.add(match.substring(index, end));
                index = end + 1;
            }
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     * 
     */
    public void skippedEntity(String name) throws SAXException {
        try {
            output.write(StringUtils.getEntity(name));
        } catch (Exception e) {
        	logger.logException(e);
        }
    }
	
	/**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     * 
     */
    public void startCDATA() throws SAXException {
    	insideCDATA = true;
	    try{
	        output.write(Constants.CDATA_HEAD);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     * 
     */
    public void startDocument() throws SAXException {
        //Reset the level to -1 before we start processing the topics. //Eric
    	level = -1;
    }
    private boolean hasRelatedLinks(Attributes atts){ //Eric
		//check whether there is <prolog> in the current topic
		//if current element is <body> and there is no <prolog> before
		//then this topic has no <prolog> and return false
		if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null){ //Eric
			if (!hasRelatedlinksTillNow){
				if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).indexOf("topic/topic") != -1){

					if (level > 0){
						level++;
					}else if (level == -1){ 
						level = 1;
					}else {
						return false;
					}
					return false;  //Eric
					
				}
			}
		}
		return true;		
	}
    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     * 
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
		int attsLen = atts.getLength();
		// level++; //Eric

		//only care about adding releated links to topics. 
		if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null) {// Eric

			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).indexOf(
					"topic/topic") != -1) {

				if (!topicSpecList.contains(localName)) {
					topicSpecList.add(localName);
				}

				if (level > 0) {

					if (!hasRelatedLinks(atts) && startTopic && !hasWritten) { // Eric

						// if <related-links> don't exist
						try {
							output.write(Constants.RELATED_LINKS_HEAD);
							output.write(indexEntries);
							output.write(Constants.RELATED_LINKS_END);
							output.write(System.getProperty("line.separator"));
						} catch (Exception e) {
							if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null) {
								logger.logException(e);
							}
							hasRelatedlinksTillNow = true;
							hasWritten = true;
						}
					}

				} else if (level == -1) {
					//If the level is in the initialized state and we have hit our first
					//topic element. This is equivalient to the following steps.
					//level = 0;
					//level++;
					level = 1;
					
				} else {
					level++;
				}
			}
		}
		try {  //Eric

			// level++;;
			
			if (!startTopic
					&& !Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
				if (atts.getValue(Constants.ATTRIBUTE_NAME_ID) != null) {
					topicIdList.add(atts.getValue(Constants.ATTRIBUTE_NAME_ID));
				} else {
					topicIdList.add("null");
				}
				if (topicIdList.size() == matchList.size()) {
					startTopic = checkMatch();

					// added by Charlie at 10/21/2005
					// if (startTopic) {
					// matchLevel = level;
					// }
				}
			}
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
			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS).indexOf( //Eric
					" topic/related-links ") != -1
					&& startTopic) {
				hasRelatedlinksTillNow = true;
				hasWritten = true;  //Eric
				output.write(indexEntries);
			}

		} catch (Exception e) {
			if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS) != null) {
				logger.logException(e);
			}// prevent printing stack trace when meeting <dita> which has no
				// class attribute
		}
	}

	/**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     * 
     */
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

    /**
     * @see org.dita.dost.writer.AbstractWriter#write(java.lang.String)
     * 
     */
    public void write(String filename) {
		String file = null;
		String topic = null;
		File inputFile = null;
		File outputFile = null;
		FileOutputStream fileOutput = null;

        try {
            
            if(filename.lastIndexOf(Constants.SHARP)!=-1){
                file = filename.substring(0,filename.lastIndexOf(Constants.SHARP));
                topic = filename.substring(filename.lastIndexOf(Constants.SHARP)+1);
                setMatch(topic);
                startTopic = false;
                //matchLevel = matchList.size(); //Eric
            }else{
                file = filename;
                matchList = null;
                startTopic = true;
                //matchLevel = 1; //Eric
            }
            
            // ignore in-exists file
            if (file == null || !new File(file).exists()) {
            	return;
            }
            
        	needResolveEntity = true;
            hasRelatedlinksTillNow = false;
            hasWritten = false;
            topicIdList = new ArrayList(Constants.INT_16);
            inputFile = new File(file);
            outputFile = new File(file + Constants.FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, Constants.UTF8);

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
