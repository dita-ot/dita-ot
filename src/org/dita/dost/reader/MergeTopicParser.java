package org.dita.dost.reader;

import java.io.File;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 * MergeTopicParser reads topic file and transform the references to other dita
 * files into internal references. The parse result of MergeTopicParser will be
 * returned to MergeMapParser and serves as part of intermediate merged result.
 * 
 * @author Zhang, Yuan Peng
 */
public class MergeTopicParser extends AbstractXMLReader {

	private XMLReader reader = null;
	private static StringBuffer topicInfo = null;
	private String retId;
	private DITAOTJavaLogger logger = null;
	private MergeUtils util;
	private ContentImpl content;
	private boolean isFirstTopicId;
	private String filePath = null;
	private String dirPath = null;
	
	public MergeTopicParser() {
		try{
			if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
				//The default sax driver is set to xerces's sax driver
				System.setProperty(Constants.SAX_DRIVER_PROPERTY, Constants.SAX_DRIVER_DEFAULT_CLASS);
			}
			if(reader == null){
				reader = XMLReaderFactory.createXMLReader();
				reader.setContentHandler(this);
				reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
				reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
				reader.setFeature(Constants.FEATURE_VALIDATION, true); 
				reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
			}
			if(topicInfo == null){
				topicInfo = new StringBuffer(Constants.INT_1024);
			}
			
			content = new ContentImpl();
			util = MergeUtils.getInstance();
		}catch (Exception e){
			logger.logException(e);
		}
	}

	
	public Content getContent() {
		content.setValue(topicInfo);
		return content;
	}



	public void characters(char[] ch, int start, int length) throws SAXException {
		topicInfo.append(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		topicInfo.append(Constants.LESS_THAN)
		.append(Constants.SLASH)
		.append(qName)
		.append(Constants.GREATER_THAN);
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		//write the start element of topic parsing logic;
		String classValue = null;
		String scopeValue = null;
		String formatValue = null;
		String fileId = null;
		String topicId = null;
		String pathFromMap = null;
		int attsLen = atts.getLength();
		int index;
		int slashIndex;
		
		topicInfo.append(Constants.LESS_THAN).append(qName);
		classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);		
		
		for (int i = 0; i < attsLen; i++) {
            String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);
            
            if(Constants.ATTRIBUTE_NAME_ID.equals(attQName)){
            	
            	if(classValue != null 
            			&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC)!=-1){
            		// Process the topic element id
            		if(!util.findId(filePath+Constants.SHARP+attValue)){
            			topicInfo.append(Constants.STRING_BLANK)
            			.append("oid").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(attValue).append(Constants.QUOTATION);
            			attValue = util.addId(filePath+Constants.SHARP+attValue);
            		}else{
            			topicInfo.append(Constants.STRING_BLANK)
            			.append("oid").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(attValue).append(Constants.QUOTATION);
            			attValue = util.getIdValue(filePath+Constants.SHARP+attValue);
            		}
            		if(isFirstTopicId){
            			isFirstTopicId = false;
            			retId = attValue;
            			util.addId(filePath,attValue);
            		}
            	}
            }
            
            if((classValue.indexOf(Constants.ATTR_CLASS_VALUE_XREF) != -1
            		|| classValue.indexOf(Constants.ATTR_CLASS_VALUE_LINK) != -1)
            		&& Constants.ATTRIBUTE_NAME_HREF.equals(attQName) 
            		&& attValue != null
            		&& !Constants.STRING_EMPTY.equalsIgnoreCase(attValue)){
            	scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
        		formatValue = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
        		index = attValue.indexOf(Constants.SHARP);
        		
//        		if (attValue.indexOf(Constants.SHARP) != -1){
//        			attValue = attValue.substring(0, attValue.indexOf(Constants.SHARP));
//        		}
        		
        		if((scopeValue == null 
    					|| Constants.ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
    					&& (formatValue == null 
    							|| Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))){
        			if (index != -1){ // href value refer to an id in a topic
        				pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue.substring(0,index));
    					topicInfo.append(Constants.STRING_BLANK)
            			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(pathFromMap)
            			.append(attValue.substring(index))
            			.append(Constants.QUOTATION);
    					
    					topicId = attValue.substring(index);
    					slashIndex = topicId.indexOf(Constants.SLASH);
    					index = attValue.indexOf(Constants.SLASH, index);
    					if (slashIndex != -1){
    						topicId = pathFromMap + topicId.substring(0,slashIndex);
    					}else {
    						topicId = pathFromMap + topicId;
    					}
    					
    					if(util.findId(topicId)){// topicId found 
    						if(index!=-1){
    							attValue = Constants.SHARP + util.getIdValue(topicId)
    							+ attValue.substring(index);
    						} else{
    							attValue = Constants.SHARP + util.getIdValue(topicId);
    						}
    					}else{//topicId not found
    						if(index!=-1){
    							attValue = Constants.SHARP + util.addId(topicId)
    							+ attValue.substring(index);
    						} else{
    							attValue = Constants.SHARP + util.addId(topicId);
    						}
    					}

    				}else{ // href value refer to a topic
    					pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue);
    					    					
    					topicInfo.append(Constants.STRING_BLANK)
            			.append("ohref").append(Constants.EQUAL).append(Constants.QUOTATION)
            			.append(pathFromMap)
            			.append(Constants.QUOTATION);
    					
    					if(util.findId(pathFromMap)){
    						attValue = Constants.SHARP + util.getIdValue(pathFromMap);
    					}else{
    						fileId = util.getFirstTopicId(pathFromMap, dirPath);
    						if (util.findId(pathFromMap + Constants.SHARP + fileId)){
    							util.addId(pathFromMap,util.getIdValue(pathFromMap + Constants.SHARP + fileId));
    							attValue = Constants.SHARP + util.getIdValue(pathFromMap + Constants.SHARP + fileId);
    						}else{
    							attValue = Constants.SHARP + util.addId(pathFromMap);
    							util.addId(pathFromMap + Constants.SHARP + fileId, util.getIdValue(pathFromMap));
    						}
    						
    					}
    				}
    			}
        		
            }
            
            //output all attributes
            topicInfo.append(Constants.STRING_BLANK)
            		.append(attQName).append(Constants.EQUAL).append(Constants.QUOTATION)
            		.append(attValue).append(Constants.QUOTATION);
        }
		topicInfo.append(Constants.GREATER_THAN);
	}

	public void endDocument() throws SAXException {
		
	}

	public void startDocument() throws SAXException {
		isFirstTopicId = true;
	}

    public String parse(String filename,String dir){
		int index = filename.indexOf(Constants.SHARP);
		dirPath = dir;
		try{
			if (index != -1){
				filePath = filename.substring(0,index);
			}else{
				filePath = filename;
			}
			reader.parse(dir + File.separator + filePath);
			return retId;
		}catch (Exception e){
			logger.logException(e);
			return null;
		}
	}
	

}
