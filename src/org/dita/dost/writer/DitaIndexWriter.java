/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Properties;

import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/*
 * Created on 2004-12-17
 */

/**
 * DitaIndexWriter reads dita topic file and insert the index information into it.
 * 
 * @author Zhang, Yuan Peng
 */
public class DitaIndexWriter extends AbstractXMLWriter {

    private String indexEntries;
    private XMLReader reader;
    private OutputStreamWriter output;
    private boolean hasPrologTillNow;// whether we have met <prolog> in this topic we want
    private boolean hasMetadataTillNow;// whether we have met <metadata> in <prolog> element
    private boolean needResolveEntity;
    private ArrayList matchList; // topic path that topicIdList need to match
    private ArrayList topicIdList; // array list that is used to keep the hierarchy of topic id
    private String lastMatchTopic;
    private String firstMatchTopic;
    private boolean startTopic; //whether to insert links at this topic
    private DITAOTJavaLogger logger;

    /**
     * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
     * 
     */
    public void setContent(Content content) {
        indexEntries = (String) content.getValue();
    }


    /**
     * Default constructor of DitaIndexWriter class.
     */
    public DitaIndexWriter() {
        super();
        topicIdList = new ArrayList(Constants.INT_16);
        firstMatchTopic = null;
        hasMetadataTillNow = false;
        hasPrologTillNow = false;
        indexEntries = null;
        lastMatchTopic = null;
        matchList = null;
        needResolveEntity = false;
        output = null;
        startTopic = false;
        logger = new DITAOTJavaLogger();
        
        try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_DEFAULT_CLASS);
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
            }else{
                file = filename;
                matchList = null;
                startTopic = true;
            }
        	needResolveEntity = true;
            hasPrologTillNow = false;
            hasMetadataTillNow = false;
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
            try{
                fileOutput.close();
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
    }
    private void setMatch(String match) {
		int index = 0;
        matchList = new ArrayList(Constants.INT_16);
        
        firstMatchTopic = (match.indexOf(Constants.SLASH) != -1) ? match.substring(0, match.indexOf('/')) : match;

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
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     * 
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    	if(needResolveEntity){
    		try {
            	output.write(ch, start, length);
        	} catch (Exception e) {
        		logger.logException(e);
        	}
    	}
    }

    /**
     * @see org.xml.sax.ContentHandler#endDocument()
     * 
     */
    public void endDocument() throws SAXException {

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
        if (!startTopic){
            topicIdList.remove(topicIdList.size() - 1);
        }
        try {
            if (!hasMetadataTillNow && Constants.ELEMENT_NAME_PROLOG.equals(qName) && startTopic) {
                output.write(Constants.META_HEAD);
                output.write(indexEntries);
                output.write(Constants.META_END);
                hasMetadataTillNow = true;
            }
            output.write(Constants.LESS_THAN + Constants.SLASH + qName
                    + Constants.GREATER_THAN);
        } catch (Exception e) {
        	logger.logException(e);
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
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     * 
     */
    public void skippedEntity(String name) throws SAXException {
        try {
            output.write(name);
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     * 
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    	int attsLen = atts.getLength();
    	
        try {
            if (!hasProlog(atts) && startTopic) {
                // if <prolog> don't exist
                output
                        .write(Constants.PROLOG_HEAD + Constants.META_HEAD);
                output.write(indexEntries);
                output.write(Constants.META_END + Constants.PROLOG_END);
            }
            if ( startTopic == false && Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(qName) == false){
                if (atts.getValue(Constants.ATTRIBUTE_NAME_ID) != null){
                    topicIdList.add(atts.getValue(Constants.ATTRIBUTE_NAME_ID));
                }else{
                    topicIdList.add("null");
                }
                if (topicIdList.size() == matchList.size()){
                    startTopic = checkMatch();
                }
            }

            if (!hasMetadata(qName) && startTopic) {
                output.write(Constants.META_HEAD);
                output.write(indexEntries);
                output.write(Constants.META_END);
                hasMetadataTillNow = true;
            }

            output.write(Constants.LESS_THAN + qName);
            for (int i = 0; i < attsLen; i++) {
                String attQName = atts.getQName(i);
                String attValue;
                attValue = atts.getValue(i);
                output.write(new StringBuffer().append(Constants.STRING_BLANK)
                		.append(attQName).append(Constants.EQUAL).append(Constants.QUOTATION)
                		.append(attValue).append(Constants.QUOTATION).toString());
            }
            output.write(Constants.GREATER_THAN);
            if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS)
                    .indexOf(" topic/metadata ") != -1 && startTopic) {
                hasMetadataTillNow = true;
                output.write(indexEntries);
            }
            if (atts.getValue(Constants.ATTRIBUTE_NAME_CLASS)
                    .indexOf(" topic/prolog ") != -1) {
                hasPrologTillNow = true;
            }
        } catch (Exception e) {
        	logger.logException(e);
        }
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     * 
     */
    public void endCDATA() throws SAXException {
	    try{
	        output.write(Constants.CDATA_END);
	    }catch(Exception e){
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
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     * 
     */
    public void startCDATA() throws SAXException {
	    try{
	        output.write(Constants.CDATA_HEAD);
	    }catch(Exception e){
	    	logger.logException(e);
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
	
	private boolean hasProlog(Attributes atts){
		//check whether there is <prolog> in the current topic
		//if current element is <body> and there is no <prolog> before
		//then this topic has no <prolog> and return false
		
		if (!hasPrologTillNow && atts.getValue(Constants.ATTRIBUTE_NAME_CLASS)
		        .indexOf("topic/body") != -1){
			return false;
		}
		return true;		
	}
	
	private boolean hasMetadata(String qName){
		//check whether there is <metadata> in <prolog> element
		//if there is <prolog> element and there is no <metadata> element before
		//and current element is <resourceid>, then there is no <metadata> in current
		//<prolog> element. return false.
		if(hasPrologTillNow && !hasMetadataTillNow && Constants.ELEMENT_NAME_RESOURCEID.equals(qName)){
			return false;
		}
		return true;
	}
}
