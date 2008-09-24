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
import java.io.FileInputStream;
import java.util.Properties;
import java.util.StringTokenizer;

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
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * MergeMapParser reads the ditamap file after preprocessing and merges
 * different files into one intermediate result. It calls MergeTopicParser
 * to process the topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public class MergeMapParser extends AbstractXMLReader {
	private XMLReader reader = null;
	private StringBuffer mapInfo = null;
	private MergeTopicParser topicParser = null;
	private DITAOTJavaLogger logger = null;
	private MergeUtils util;
	private ContentImpl content;
	private String dirPath = null;
	private String tempdir = null;

	/**
	 * Default Constructor
	 */
	public MergeMapParser() {
		logger = new DITAOTJavaLogger();
		try{
			if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
				//The default sax driver is set to xerces's sax driver
				StringUtils.initSaxDriver();
			}
			if(reader == null){
				reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(this);
//				reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
				reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
//				reader.setFeature(Constants.FEATURE_VALIDATION, true); 
//				reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
			}
			if(mapInfo == null){
				mapInfo = new StringBuffer(Constants.INT_1024);
			}
			
			topicParser = new MergeTopicParser();
			content = new ContentImpl();
			util = MergeUtils.getInstance();
		}catch (Exception e){
			logger.logException(e);
		}
	}

	/**
	 * @see org.dita.dost.reader.AbstractReader#getContent()
	 */
	public Content getContent() {
		content.setValue(mapInfo.append((StringBuffer)topicParser.getContent().getValue()));
		return content;
	}

	/**
	 * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
	 */
	public void read(String ditaInput) {
		try{
			String filename;
			if(ditaInput.contains(Constants.STICK)){
				filename = ditaInput.substring(0, ditaInput.indexOf(Constants.STICK));
				tempdir = ditaInput.substring(ditaInput.indexOf(Constants.STICK)+1);
			}else{
				filename = ditaInput;
				tempdir = new File(filename).getParent();
			}
			
			File input = new File(filename);
			dirPath = input.getParent();
			reader.setErrorHandler(new DITAOTXMLErrorHandler(filename));
			reader.parse(filename);
		}catch(Exception e){
			logger.logException(e);
		}
	}

	
	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		mapInfo.append(Constants.LESS_THAN)
		.append(Constants.SLASH)
		.append(qName)
		.append(Constants.GREATER_THAN);
	}

	
	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		mapInfo.append(StringUtils.escapeXML(ch, start, length));
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		
		String scopeValue = null;
		String formatValue = null;
		String classValue = null;
		String fileId = null;
		int attsLen = atts.getLength();
		
		mapInfo.append(Constants.LESS_THAN).append(qName);
		classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		for (int i = 0; i < attsLen; i++) {
            String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);
            if(Constants.ATTRIBUTE_NAME_HREF.equals(attQName) 
            		&& !StringUtils.isEmptyString(attValue)
            		&& classValue != null
            		&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF)!=-1){
            	scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
        		formatValue = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
        		
//        		if (attValue.indexOf(Constants.SHARP) != -1){
//        			attValue = attValue.substring(0, attValue.indexOf(Constants.SHARP));
//        		}
        		
        		if((scopeValue == null 
    					|| Constants.ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
    					&& (formatValue == null 
    							|| Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))){
    				if (util.isVisited(attValue)){
    					mapInfo.append(Constants.STRING_BLANK)
            			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(StringUtils.escapeXML(attValue)).append(Constants.QUOTATION);
    					
//    					random = RandomUtils.getRandomNum();
//    					filename = attValue + "(" + Long.toString(random) + ")";
    					attValue = new StringBuffer(Constants.SHARP).append(util.getIdValue(attValue)).toString();
    					//parse the file but give it another file name
//    					topicParser.read(filename);
    				}else{
    					mapInfo.append(Constants.STRING_BLANK)
            			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(StringUtils.escapeXML(attValue)).append(Constants.QUOTATION);
    					    					
    					//parse the topic
    					fileId = topicParser.parse(attValue,dirPath);
    					util.visit(attValue);
    					attValue = new StringBuffer(Constants.SHARP).append(fileId).toString();
    				}
    			}
        		
            }
            
            //output all attributes
            mapInfo.append(Constants.STRING_BLANK)
            		.append(attQName).append(Constants.EQUAL).append(Constants.QUOTATION)
            		.append(StringUtils.escapeXML(attValue)).append(Constants.QUOTATION);
        }
		mapInfo.append(Constants.GREATER_THAN);
		
	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
        mapInfo.append(Constants.LESS_THAN + Constants.QUESTION 
                + pi + Constants.QUESTION + Constants.GREATER_THAN);
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		// read href dita topic list
		// compare visitedSet with the list
		// if list item not in visitedSet then call MergeTopicParser to parse it
		Properties property = new Properties();
	    File ditalist = new File(tempdir, Constants.FILE_NAME_DITA_LIST);
        File xmlDitalist = new File(tempdir, Constants.FILE_NAME_DITA_LIST_XML);
        try{
	        if(xmlDitalist.exists())
	        	property.loadFromXML(new FileInputStream(xmlDitalist));
	        else 
	        	property.loadFromXML(new FileInputStream(ditalist));
	        String hrefTargetList = property.getProperty("hreftargetslist");
			StringTokenizer tokenizer = new StringTokenizer(hrefTargetList,Constants.COMMA);
			String element = null;
			while(tokenizer.hasMoreElements())
			{
				element = (String)tokenizer.nextElement();
				if (!new File(dirPath).equals(new File(tempdir)))
					element = FileUtils.getRelativePathFromMap(new File(dirPath,"a.ditamap").getAbsolutePath(), new File(tempdir,element).getAbsolutePath());
				if(!util.isVisited(element)){
					util.visit(element);
					topicParser.parse(element, dirPath);
				}
			}
        }catch (Exception e){
        	logger.logException(e);
        }		
	}

}
