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
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
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
 * MergeMapParser reads the ditamap file after preprocessing and merges
 * different files into one intermediate result. It calls MergeTopicParser
 * to process the topic file.
 * 
 * @author Zhang, Yuan Peng
 */
public final class MergeMapParser extends AbstractXMLReader {
	private XMLReader reader = null;
	private StringBuffer mapInfo = null;
	private MergeTopicParser topicParser = null;
	private MergeUtils util;
	private ContentImpl content;
	private String dirPath = null;
	private String tempdir = null;
	
	private Stack<String> processStack;
	private int processLevel = 0;

	/**
	 * Default Constructor.
	 */
	public MergeMapParser() {
		try{
			if(reader == null){
				reader = StringUtils.getXMLReader();
				reader.setContentHandler(this);
				reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			}
			if(mapInfo == null){
				mapInfo = new StringBuffer(Constants.INT_1024);
			}
			
			processStack = new Stack<String>();
			processLevel = 0;
			
			topicParser = new MergeTopicParser();
			topicParser.reset();
			content = new ContentImpl();
			util = MergeUtils.getInstance();
			util.reset();
		}catch (final Exception e){
			logger.logException(e);
		}
	}

	@Override
	public Content getContent() {
		content.setValue(mapInfo.append((StringBuffer)topicParser.getContent().getValue()));
		return content;
	}

	@Override
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
			
			final File input = new File(filename);
			dirPath = input.getParent();
			reader.setErrorHandler(new DITAOTXMLErrorHandler(filename));
			reader.parse(filename);
		}catch(final Exception e){
			logger.logException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (processLevel > 0) {
			String value = processStack.peek();
			if (processLevel == processStack.size()) {
				value = processStack.pop();
			}
			processLevel--;
			
			if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(value)) {
				return;
			}
		}
		mapInfo.append(Constants.LESS_THAN)
		.append(Constants.SLASH)
		.append(qName)
		.append(Constants.GREATER_THAN);
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (processStack.empty() || !Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processStack.peek())){
			mapInfo.append(StringUtils.escapeXML(ch, start, length));
		}
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		final String attrValue = atts.getValue(Constants.ATTRIBUTE_NAME_PROCESSING_ROLE);
	    if (attrValue != null) {
	        processStack.push(attrValue);
	        processLevel++;
	        if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(attrValue)) {
	        	// @processing-role='resource-only'
	        	return;
	        }
	    } else if (processLevel > 0) {
	        processLevel++;
	        if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processStack.peek())) {
	        	// Child of @processing-role='resource-only'
	        	return;
	        }
	    }
		
		mapInfo.append(Constants.LESS_THAN).append(qName);
		final String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		final int attsLen = atts.getLength();
		for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);
            if(Constants.ATTRIBUTE_NAME_HREF.equals(attQName) 
            		&& !StringUtils.isEmptyString(attValue)
            		&& classValue != null
            		&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF)!=-1){
                final String scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
        		final String formatValue = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
        		
        		if((scopeValue == null 
    					|| Constants.ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
    					&& (formatValue == null 
    							|| Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))){
        			final String ohref = attValue;
                    final String copyToValue = atts.getValue(Constants.ATTRIBUTE_NAME_COPY_TO);
                    if (!StringUtils.isEmptyString(copyToValue)) {
                        attValue = copyToValue;
                    }
    				if (util.isVisited(attValue)){
    					mapInfo.append(Constants.STRING_BLANK)
            			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(StringUtils.escapeXML(ohref)).append(Constants.QUOTATION);
    					
    					attValue = new StringBuffer(Constants.SHARP).append(util.getIdValue(attValue)).toString();
    				}else{
    					mapInfo.append(Constants.STRING_BLANK)
            			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(StringUtils.escapeXML(ohref)).append(Constants.QUOTATION);
    					    					
    					//parse the topic
    					final String fileId = topicParser.parse(attValue,dirPath);
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
	
	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		final String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
        mapInfo.append(Constants.LESS_THAN + Constants.QUESTION 
                + pi + Constants.QUESTION + Constants.GREATER_THAN);
	}

	@Override
	public void endDocument() throws SAXException {
		// read href dita topic list
		// compare visitedSet with the list
		// if list item not in visitedSet then call MergeTopicParser to parse it
		final Properties property = new Properties();
	    final File ditalist = new File(tempdir, Constants.FILE_NAME_DITA_LIST);
        final File xmlDitalist = new File(tempdir, Constants.FILE_NAME_DITA_LIST_XML);
        InputStream in = null;
        try{
	        if(xmlDitalist.exists()) {
	        	in = new FileInputStream(xmlDitalist);
	        	property.loadFromXML(in);
	        } else {
	        	in = new FileInputStream(ditalist);
	        	property.loadFromXML(in);
	        }
	        final String hrefTargetList = property.getProperty(Constants.HREF_TARGET_LIST);
	        String resourceOnlySet = property.getProperty(Constants.RESOURCE_ONLY_LIST);
	        resourceOnlySet = (resourceOnlySet == null ? "" : resourceOnlySet);
	        String skipTopicSet = property.getProperty(Constants.CHUNK_TOPIC_LIST);
	        skipTopicSet = (skipTopicSet == null ? "" : skipTopicSet);
	        String chunkedTopicSet = property.getProperty(Constants.CHUNKED_TOPIC_LIST);
	        chunkedTopicSet = (chunkedTopicSet == null ? "" : chunkedTopicSet);
			final StringTokenizer tokenizer = new StringTokenizer(hrefTargetList,Constants.COMMA);
			while(tokenizer.hasMoreElements())
			{
			    String element = (String)tokenizer.nextElement();
				if (!new File(dirPath).equals(new File(tempdir))) {
                    element = FileUtils.getRelativePathFromMap(new File(dirPath,"a.ditamap").getAbsolutePath(), new File(tempdir,element).getAbsolutePath());
                }
				if(!util.isVisited(element)){
					util.visit(element);
					if (!resourceOnlySet.contains(element) && (chunkedTopicSet.contains(element)
							|| !skipTopicSet.contains(element))){
						//ensure the file exists
						if(new File(dirPath, element).exists()){
							topicParser.parse(element, dirPath);
						}else{
							final String fileName = new File(dirPath, element).getAbsolutePath();
							final Properties prop = new Properties();
			            	prop.put("%1", fileName);
			            	logger.logWarn(MessageUtils.getMessage("DOTX008W", prop).toString());
						}
					}
						
				}
			}
        }catch (final Exception e){
        	logger.logException(e);
        } finally {
        	if (in != null) {
        		try {
        			in.close();
        		} catch (final IOException e) {
        			logger.logException(e);
        		}
        	}
        }
	}

}
