/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * MergeTopicParser reads topic file and transform the references to other dita
 * files into internal references. The parse result of MergeTopicParser will be
 * returned to MergeMapParser and serves as part of intermediate merged result.
 * 
 * @author Zhang, Yuan Peng
 */
public class MergeTopicParser extends AbstractXMLReader {
	private static StringBuffer topicInfo = null;
	private ContentImpl content;
	private String dirPath = null;
	private String filePath = null;
	private boolean isFirstTopicId = false;
	private DITAOTJavaLogger logger = null;

	private XMLReader reader = null;
	private String retId = null;
	private MergeUtils util;
	
	/**
	 * Default Constructor.
	 */
	public MergeTopicParser() {
		logger = new DITAOTJavaLogger();
		try{
			if(reader == null){
				reader = StringUtils.getXMLReader();
				reader.setContentHandler(this);
				reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			}
			synchronized(this) {
    			if(topicInfo == null){
    				topicInfo = new StringBuffer(Constants.INT_1024);
    			}
			}
			
			content = new ContentImpl();
			util = MergeUtils.getInstance();
		}catch (Exception e){
			logger.logException(e);
		}
	}
	/**
	 * reset.
	 */
	public void reset() {
		topicInfo.delete(0, topicInfo.length());
	}



	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		topicInfo.append(StringUtils.escapeXML(ch, start, length));
	}


	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// Skip redundant <dita> tags.
		if (Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
			return;
		}
		topicInfo.append(Constants.LESS_THAN)
		.append(Constants.SLASH)
		.append(qName)
		.append(Constants.GREATER_THAN);
	}

	
	/**
	 * @see org.dita.dost.reader.AbstractReader#getContent()
	 */
	public Content getContent() {
		content.setValue(topicInfo);
		return content;
	}



	/**
	 * @param classValue
	 * @param attValue
	 * @return
	 */
	private String handleID(String classValue, String attValue) {
		String retAttValue = attValue;
		if(classValue != null 
				&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC)!=-1){
			// Process the topic element id
			String value = filePath+Constants.SHARP+attValue;
			if(util.findId(value)){
				topicInfo.append(Constants.STRING_BLANK)
				.append("oid").append(Constants.EQUAL).append(Constants.QUOTATION)
				.append(StringUtils.escapeXML(attValue)).append(Constants.QUOTATION);
				retAttValue = util.getIdValue(value);
			}else{
				topicInfo.append(Constants.STRING_BLANK)
				.append("oid").append(Constants.EQUAL).append(Constants.QUOTATION)
				.append(StringUtils.escapeXML(attValue)).append(Constants.QUOTATION);
				retAttValue = util.addId(value);           			
			}
			if(isFirstTopicId){
				isFirstTopicId = false;
				retId = retAttValue;
				util.addId(filePath,retAttValue);
			}
		}
		return retAttValue;
	}


	/**
	 * @param sharpIndex
	 * @param attValue
	 * @return
	 */
	private String handleLocalDita(int sharpIndex, String attValue) {
		String fileId;
		String topicId;
		String pathFromMap;
		int slashIndex;
		int index;
		String retAttValue = attValue;
		if (sharpIndex != -1){ // href value refer to an id in a topic
			if(sharpIndex == 0){
				pathFromMap = filePath.replaceAll(Constants.DOUBLE_BACK_SLASH, Constants.SLASH);
			}else{
				pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue.substring(0,sharpIndex)).replaceAll(Constants.DOUBLE_BACK_SLASH, Constants.SLASH);
			}
			
			topicInfo.append(Constants.STRING_BLANK)
			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
			.append(pathFromMap)
			.append(attValue.substring(sharpIndex))
			.append(Constants.QUOTATION);
			
			topicId = attValue.substring(sharpIndex);
			slashIndex = topicId.indexOf(Constants.SLASH);
			index = attValue.indexOf(Constants.SLASH, sharpIndex);
			topicId = (slashIndex != -1) 
					? pathFromMap + topicId.substring(0, slashIndex)
					: pathFromMap + topicId;

			
			if(util.findId(topicId)){// topicId found 
				String prefix = Constants.SHARP + util.getIdValue(topicId);
				retAttValue = (index!=-1)? prefix + attValue.substring(index) : prefix;
			}else{//topicId not found
				String prefix = Constants.SHARP + util.addId(topicId);
				retAttValue = (index!=-1)? prefix + attValue.substring(index) : prefix;
			}

		}else{ // href value refer to a topic
			pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue);
			    					
			topicInfo.append(Constants.STRING_BLANK)
			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
			.append(pathFromMap)
			.append(Constants.QUOTATION);
			
			if(util.findId(pathFromMap)){
				retAttValue = Constants.SHARP + util.getIdValue(pathFromMap);
			}else{
				fileId = util.getFirstTopicId(pathFromMap, dirPath , false);
				if (util.findId(pathFromMap + Constants.SHARP + fileId)){
					util.addId(pathFromMap,util.getIdValue(pathFromMap + Constants.SHARP + fileId));
					retAttValue = Constants.SHARP + util.getIdValue(pathFromMap + Constants.SHARP + fileId);
				}else{
					retAttValue = Constants.SHARP + util.addId(pathFromMap);
					util.addId(pathFromMap + Constants.SHARP + fileId, util.getIdValue(pathFromMap));
				}
				
			}
		}
		return retAttValue;
	}

    /**
     * Parse the file to update id.
	 * @param filename filename
	 * @param dir file dir
	 * @return updated id
	 */
	public String parse(String filename,String dir){
		int index = filename.indexOf(Constants.SHARP);
		dirPath = dir;
		try{
			filePath = (index != -1) ? filename.substring(0,index):filename;
			reader.setErrorHandler(new DITAOTXMLErrorHandler(dir + File.separator + filePath));
			reader.parse(dir + File.separator + filePath);
			return retId;
		}catch (Exception e){
			logger.logException(e);
			return null;
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		isFirstTopicId = true;
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		//write the start element of topic parsing logic;
		String classValue = null;
		String scopeValue = null;
		String formatValue = null;
		int attsLen = atts.getLength();
		int sharpIndex;
		
		// Skip redundant <dita> tags.
		if (Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
			return;
		}
		
		topicInfo.append(Constants.LESS_THAN).append(qName);
		classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);		
		
		for (int i = 0; i < attsLen; i++) {
            String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);
            
            if(Constants.ATTRIBUTE_NAME_ID.equals(attQName)){           	
            	attValue = handleID(classValue, attValue);
            }
            
            if(classValue != null
            		&& Constants.ATTRIBUTE_NAME_HREF.equals(attQName) 
            		&& attValue != null
            		&& !Constants.STRING_EMPTY.equalsIgnoreCase(attValue)){
            	//If the element has valid @class attribute and current attribute
            	//is valid @href
            	
            	scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
        		formatValue = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
        		sharpIndex = attValue.indexOf(Constants.SHARP);
        		
//        		if (attValue.indexOf(Constants.SHARP) != -1){
//        			attValue = attValue.substring(0, attValue.indexOf(Constants.SHARP));
//        		}
        		
        		if((scopeValue == null 
    					|| Constants.ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
    					&& attValue.indexOf(Constants.COLON_DOUBLE_SLASH) == -1) {
        			//The scope for @href is local
        			
        			if((classValue.indexOf(Constants.ATTR_CLASS_VALUE_XREF) != -1
        					|| classValue.indexOf(Constants.ATTR_CLASS_VALUE_LINK) != -1)
        					&& (formatValue == null 
    							|| Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))){
        				//local xref or link that refers to dita file
        				
        				attValue = handleLocalDita(sharpIndex, attValue);
        			} else {
        				//local @href other than local xref and link that refers to dita file
        				
        				attValue = handleLocalHref(attValue);
        			}
    			}
        		
            }
            
            //output all attributes
            topicInfo.append(Constants.STRING_BLANK)
            		.append(attQName).append(Constants.EQUAL).append(Constants.QUOTATION)
            		.append(StringUtils.escapeXML(attValue)).append(Constants.QUOTATION);
        }
		topicInfo.append(Constants.GREATER_THAN);
	}



	private String handleLocalHref(String attValue) {
		String pathFromMap;
        pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue);
        return pathFromMap;
	}


	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
        topicInfo.append(Constants.LESS_THAN + Constants.QUESTION 
                + pi + Constants.QUESTION + Constants.GREATER_THAN);
	}
	

}