/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * This class is for writing conref push contents into
 * specific files.
 *
 */
public final class ConrefPushParser extends AbstractXMLWriter {

	/**table containing conref push contents.*/
	private Hashtable<String, String> movetable = null;
	
	/**topicId keep the current topic id value.*/
	private String topicId = null;
	
	/**idStack keeps the history of topicId because topics can be nested.*/
	private Stack<String> idStack = null;
	/**parser.*/
	private XMLReader parser = null;
	/**output.*/
	private OutputStreamWriter output = null;
	//Added by william on 2009-11-8 for ampbug:2893664 start
	/**whether an entity needs to be resolved or not flag. */
	private boolean needResolveEntity = true;
	//Added by william on 2009-11-8 for ampbug:2893664 end
	
	/**topicSpecSet is used to store all kinds of names for elements which is 
	specialized from <topic>. It is useful in endElement(...) because we don't
	know the value of class attribute of the element when processing its end
	tag. That's why we need to store the element's name to the set when we first
	met it in startElement(...).*/
	private HashSet<String> topicSpecSet = null;
	
	/**boolean isReplaced show whether current content is replace
	because of "pushreplace" action in conref push. If the current
	content is replaced, the output will neglect it until isReplaced
	is turned off*/
	private boolean isReplaced = false;
	
	/**int level is used the count the level number to the element which
	is the starting point that is neglected because of "pushreplace" action
	The initial value of level is 0. It will add one if element level
	increases in startElement(....) and minus one if level decreases in 
	endElement(...). When it turns out to be 0 again, boolean isReplaced 
	needs to be turn off.*/
	private int level = 0;
	
	/**boolean hasPushafter show whether there is something we need to write
	after the current element. If so the counter levelForPushAfter should
	count the levels to make sure we insert the push content after the right
	end tag.*/
	private boolean hasPushafter = false;
	
	/**int levelForPushAfter is used to count the levels to the element which
	is the starting point for "pushafter" action. It will add one in startElement(...)
	and minus one in endElement(...). When it turns out to be 0 again, we
	should append the push content right after the current end tag.*/
	private int levelForPushAfter = 0;
	
	/**levelForPushAfterStack is used to store the history value of levelForPushAfter
	It is possible that we have pushafter action for both parent and child element.
	In this case, we need to push the parent's value of levelForPushAfter to Stack
	before initializing levelForPushAfter for child element. When we finished
	pushafter action for child element, we need to restore the original value for
	parent. As to "pushreplace" action, we don't need this because if we replaced the
	parent, the replacement of child is meaningless.*/
	private Stack<Integer> levelForPushAfterStack = null;
	
	/**contentForPushAfter is used to store the content that will push after the end
	tag of the element when levelForPushAfter is decreased to zero. This is useful
	to "pushafter" action because we don't know the value of id when processing the
	end tag of an element. That's why we need to store the content for push after
	into variable in startElement(...)*/
	private String contentForPushAfter = null;
	
	/**contentForPushAfterStack is used to store the history value of contentForPushAfter
	It is possible that we have pushafter action for both parent and child element.
	In this case, we need to push the parent's value of contentForPushAfter to Stack
	before getting value contentForPushAfter for child element from movetable. When we 
	finished pushafter action for child element, we need to restore the original value for
	parent. */
	private Stack<String> contentForPushAfterStack = null;
	
	/**if the pushcontent has @conref, it should be paid attention to it. Because the current 
	file may not contain any @conref attribute, it will not resolved by the conref.xsl,
	while it may contain @conref after pushing. So the dita.list file should be updated, if 
	the pushcontent has @conref.*/
	private boolean hasConref = false;
	/**tempDir.*/
	private String tempDir;
	/**
	 * Constructor.
	 */
	public ConrefPushParser(){
		topicSpecSet = new HashSet<String>();
		levelForPushAfterStack = new Stack<Integer>();
		contentForPushAfterStack = new Stack<String>();
		needResolveEntity = true;
		try{
			parser = StringUtils.getXMLReader();
			parser.setFeature(FEATURE_NAMESPACE_PREFIX, true);
			parser.setFeature(FEATURE_NAMESPACE, true);
			parser.setContentHandler(this);
			//Added by william on 2009-11-8 for ampbug:2893664 start
			parser.setProperty(LEXICAL_HANDLER_PROPERTY,this);
			parser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
			parser.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
			//Added by william on 2009-11-8 for ampbug:2893664 end
		}catch (Exception e) {
			logger.logException(e);
		}
	}
	/**
	 * @param content Content
	 */
	public void setContent(Content content) {
		movetable = (Hashtable<String, String>)content.getValue();
	}
	/**
	 * 
	 * @param tempDir tempDir
	 */
	public void setTempDir(String tempDir){
		this.tempDir = tempDir;
	}
	/**
	 * @param filename filename
	 * @throws DITAOTException exception
	 */
	public void write(String filename) throws DITAOTException {
		hasConref = false;
		isReplaced = false;
		hasPushafter = false;
		level = 0;
		levelForPushAfter = 0;
		idStack = new Stack<String>();
		topicSpecSet = new HashSet<String>();
		levelForPushAfterStack = new Stack<Integer>();
		contentForPushAfterStack = new Stack<String>();
		try {
			File inputFile = new File(filename);
			File outputFile = new File(filename+".cnrfpush");
			output = new OutputStreamWriter(new FileOutputStream(outputFile),UTF8);
			parser.parse(filename);
			if(!movetable.isEmpty()){
				Properties prop = new Properties();
				String key = null;
				Iterator<String> iterator = movetable.keySet().iterator();
				while(iterator.hasNext()){
					key = iterator.next();
					prop.setProperty("%1", key.substring(0, key.indexOf(STICK)));
					prop.setProperty("%2", filename);
					logger.logWarn(MessageUtils.getMessage("DOTJ043W", prop).toString());
				}
			}
			if(hasConref){
				updateList(filename);
			}
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
		}finally{
			try{
				output.close();
			}catch (Exception ex) {
				logger.logException(ex);
			}
		}
		
		
	}
	/**
	 * 
	 * @param filename filename
	 */
	private void updateList(String filename){
		Properties properties = new Properties();
		// dita.list file in temp directory, it is used to store the list properties.
		File ditaFile = new File(tempDir, FILE_NAME_DITA_LIST);
		// dita.xml.properties file in temp dicrectory, 
		// store the list properties as the dita.list in the form of xml.
		File ditaxmlFile = new File(tempDir, FILE_NAME_DITA_LIST_XML);
		// use ditaFile as the OutputStream, rewrite the dita.list file
		FileOutputStream output = null;
		// use ditaxmlFile as the outputStream, rewrite the dita.xml.properties file
		FileOutputStream xmloutput = null;
		// this is used to update the conref.list file.
		BufferedWriter bufferedWriter =null;
		InputStream in = null;
		try{
			if(ditaxmlFile.exists()) {
				in = new FileInputStream(ditaxmlFile);
				properties.loadFromXML(in);
			} else {
				in = new FileInputStream(ditaFile);
				properties.load(in);
			}
			
			String conreflist[] = properties.getProperty("conreflist").split(COMMA);
			// get the reletivePath from tempDir
			String reletivePath = filename.substring(FileUtils.removeRedundantNames(tempDir).length() + 1);
			for(String str: conreflist){
				if(str.equals(reletivePath)){
					return;
				}
			}
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer.append(properties.getProperty("conreflist")).append(COMMA).append(reletivePath);
			properties.setProperty("conreflist", stringBuffer.toString());
			try {
    			output = new FileOutputStream (new File(tempDir, FILE_NAME_DITA_LIST));
    			properties.store(output, null);
			} finally {
				if (output != null) {
					output.close();
				}
			}
			try {
    			xmloutput = new FileOutputStream(new File(tempDir, FILE_NAME_DITA_LIST_XML));
    			properties.storeToXML(xmloutput, null);
			} finally {
				if (xmloutput != null) {
					xmloutput.close();
				} 
			}
			try {
    			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(tempDir,"conref.list"))));
    			for(String str: conreflist){
    				bufferedWriter.append(str).append("\n");
    			}
    			bufferedWriter.append(reletivePath);
			} finally {
				if (bufferedWriter != null) {
					bufferedWriter.close();
				}
			}
		}catch (Exception e){
			logger.logException(e);
		} finally {
			if (in != null) {
				try {
	                in.close();
                } catch (IOException e) {
                	logger.logException(e);
                }
			}
		}

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (!isReplaced && needResolveEntity){
			try{
				output.write(StringUtils.escapeXML(ch, start, length));
			}catch (Exception e) {
				logger.logException(e);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		
		if(isReplaced){
			level--;
			if(level == 0){
				isReplaced = false;
			}
		}else{
			//write the end tag
			try{
				output.write(LESS_THAN);
				output.write(SLASH);
				output.write(name);
				output.write(GREATER_THAN);
			}catch (Exception e) {
				logger.logException(e);
			}
		}
		
		if(hasPushafter){
			levelForPushAfter--;
			if(levelForPushAfter == 0){
				//write the pushcontent after the end tag
				try{
					if(contentForPushAfter != null){
						output.write(contentForPushAfter);
					}
				}catch (Exception e) {
					logger.logException(e);
				}
				if(!levelForPushAfterStack.isEmpty() &&
						!contentForPushAfterStack.isEmpty()){
					levelForPushAfter = levelForPushAfterStack.pop().intValue();
					contentForPushAfter = contentForPushAfterStack.pop();
				}else{
					hasPushafter = false;
					//empty the contentForPushAfter since it is write to output
					contentForPushAfter = null; 
				}
			}
		}
		if(!idStack.isEmpty() && topicSpecSet.contains(name)){
			topicId = idStack.pop();
		}
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		if (!isReplaced) { 
            try {
            	String pi = (data != null) ? target + STRING_BLANK + data : target;
                output.write(LESS_THAN + QUESTION 
                        + pi + QUESTION + GREATER_THAN);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
	}
	/**
	 * 
	 * @param targetClassAttribute targetClassAttribute
	 * @param string string
	 * @return string
	 */
	private String replaceElementName(String targetClassAttribute, String string){
		InputSource inputSource = null;
		Document document = null;
		//add stub to serve as the root element
		string = "<stub>" + string + "</stub>";
		inputSource = new InputSource(new StringReader(string));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		Element element = null;
		NodeList nodeList = null;
		String targetElementName ;
		String type;
		StringBuffer stringBuffer = new StringBuffer();
		try {
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			document = documentBuilder.parse(inputSource);
			element = document.getDocumentElement();
			if(element.hasChildNodes()){
				nodeList = element.getChildNodes();
				for(int i =0;i<nodeList.getLength();i++){
					Node node = nodeList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE){
						Element elem = (Element) node;
						NodeList nList = null;
						String clazz = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
						// get type of the target element  
						type = targetClassAttribute.substring(INT_1, targetClassAttribute.indexOf("/")).trim();
						if(!clazz.equalsIgnoreCase(targetClassAttribute) && clazz.contains(targetClassAttribute)){
							// Specializing the pushing content is not handled here
							// but we can catch such a situation to emit a warning by comparing the class values.
							targetElementName = targetClassAttribute.substring(targetClassAttribute.indexOf("/") +1 ).trim();
							stringBuffer.append(LESS_THAN).append(targetElementName);
							NamedNodeMap namedNodeMap = elem.getAttributes();
							for(int t=0; t<namedNodeMap.getLength(); t++){
								//write the attributes to new generated element
								if(namedNodeMap.item(t).getNodeName().equals("conref") && namedNodeMap.item(t).getNodeValue().length()!=0){
									hasConref = true;
								}
								stringBuffer.append(STRING_BLANK).append(namedNodeMap.item(t).getNodeName()).
								append(EQUAL).append(QUOTATION+
										StringUtils.escapeXML(namedNodeMap.item(t).getNodeValue())
										+QUOTATION);
							}
							stringBuffer.append(GREATER_THAN);
							// process the child nodes of the current node
							nList = elem.getChildNodes();
							for(int j=0; j<nList.getLength(); j++){
								Node subNode = nList.item(j);
								if(subNode.getNodeType() == Node.ELEMENT_NODE){
									//replace the subElement Name 
									stringBuffer.append(replaceSubElementName(type, (Element)subNode));
								}
								if(subNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
									stringBuffer.append("<?").append(subNode.getNodeName()).append("?>");
								}
								if(subNode.getNodeType() == Node.TEXT_NODE){
									//stringBuffer.append(subNode.getNodeValue());
									//Added by William on 2009-06-30 for colname bug:2811358 start
									stringBuffer.append(StringUtils.escapeXML(subNode.getNodeValue()));
									//Added by William on 2009-06-30 for colname bug:2811358 start
								}
							}
							stringBuffer.append("</").append(targetElementName).append(GREATER_THAN);
						}else{
							stringBuffer.append(replaceSubElementName(STRING_BLANK, elem));
						}
					}
				}
				return stringBuffer.toString();
			}
			else {
				return string;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return string;
		}
	}
	/**
	 * 
	 * @param type pushtype
	 * @param elem element
	 * @return string
	 */
	private String replaceSubElementName(String type, Element elem){
		StringBuffer stringBuffer = new StringBuffer();
		String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
		String generalizedElemName = elem.getNodeName();
		if(classValue != null){
			if(classValue.contains(type) && !type.equals(STRING_BLANK)){
				generalizedElemName = classValue.substring(classValue.indexOf("/") +1 , classValue.indexOf(STRING_BLANK, classValue.indexOf("/"))).trim();
			}
		}
		stringBuffer.append(LESS_THAN).append(generalizedElemName);
		NamedNodeMap namedNodeMap = elem.getAttributes();
		for(int i=0; i<namedNodeMap.getLength(); i++){
			if(namedNodeMap.item(i).getNodeName().equals("conref") && namedNodeMap.item(i).getNodeValue().length()!=0){
				hasConref = true;
			}
			stringBuffer.append(STRING_BLANK).append(namedNodeMap.item(i).getNodeName()).
			append(EQUAL).append(QUOTATION+
			  StringUtils.escapeXML(namedNodeMap.item(i).getNodeValue())
			  +QUOTATION);
		}
		stringBuffer.append(GREATER_THAN);
		NodeList nodeList = elem.getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				// If the type of current node is ELEMENT_NODE, process current node.
				stringBuffer.append(replaceSubElementName(type, (Element)node));
			}
			if(node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
				stringBuffer.append("<?").append(node.getNodeName()).append("?>");
			}
			if(node.getNodeType() == Node.TEXT_NODE){
				//stringBuffer.append(node.getNodeValue());
				//Added by William on 2009-06-30 for colname bug:2811358 start
				stringBuffer.append(StringUtils.escapeXML(node.getNodeValue()));
				//Added by William on 2009-06-30 for colname bug:2811358 start
			}
		}
		stringBuffer.append("</").append(generalizedElemName).append(GREATER_THAN);
		return stringBuffer.toString();
	}

	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if(hasPushafter){
			levelForPushAfter ++;
		}
		if(isReplaced){
			level ++;
		}else{
			try{
				String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
				if (classValue != null && classValue.contains(ATTR_CLASS_VALUE_TOPIC)){
					if (!topicSpecSet.contains(name)){
						//add the element name to topicSpecSet if the element
						//is a topic specialization. This is used when push and pop
						//topic ids in a stack
						topicSpecSet.add(name);
					}
					String idValue = atts.getValue(ATTRIBUTE_NAME_ID);
					if (idValue != null){
						if (topicId != null){
							idStack.push(topicId);
						}				
						topicId = idValue;
					}
				}else if (atts.getValue(ATTRIBUTE_NAME_ID) != null){
					String idPath = SHARP+topicId+SLASH+atts.getValue(ATTRIBUTE_NAME_ID);
					String defaultidPath = SHARP+atts.getValue(ATTRIBUTE_NAME_ID);
					String containkey =null;
					//Added by William on 2009-10-10 for conrefPush bug:2872954 start
					//enable conref push at map level
					if(classValue != null && (classValue.contains(ATTR_CLASS_VALUE_TOPICREF)
						|| classValue.contains(ATTR_CLASS_VALUE_MAP))){
						String mapId = atts.getValue(ATTRIBUTE_NAME_ID);
						idPath = SHARP + mapId;
						idStack.push(mapId);
					}
					//Added by William on 2009-10-10 for conrefPush bug:2872954 end
					String classAttribute = atts.getValue(ATTRIBUTE_NAME_CLASS);
					boolean containpushbefore= false;
					if (movetable.containsKey(idPath+STICK+"pushbefore")){
						containkey=idPath+STICK+"pushbefore";
						containpushbefore = true;
					}else if (movetable.containsKey(defaultidPath+STICK+"pushbefore")){
						containkey=defaultidPath+STICK+"pushbefore";
						containpushbefore = true;
					}
					if (containpushbefore){
						output.write(replaceElementName(classValue, movetable.remove(containkey)));
					}
						
					
					boolean containpushplace = false;
					
					if  (movetable.containsKey(idPath+STICK+"pushreplace")){
						containkey=idPath+STICK+"pushreplace";
						containpushplace = true;
					}else if (movetable.containsKey(defaultidPath+STICK+"pushreplace")){
						containkey = defaultidPath+STICK+"pushreplace";
						containpushplace= true;
					}
					
					if (containpushplace){
						output.write(replaceElementName(classValue, movetable.remove(containkey)));
						isReplaced = true;
						level = 0;
						level ++;
					}
					
					boolean containpushafter = false;
					if  (movetable.containsKey(idPath+STICK+"pushafter")){
						containkey= idPath + STICK+"pushafter";
						containpushafter = true;
					}else if (movetable.containsKey(defaultidPath+STICK+"pushafter")){
						containpushafter= true;
						containkey = defaultidPath+STICK+"pushafter";
					}
					if (containpushafter){
						if (hasPushafter && levelForPushAfter > 0){
							//there is a "pushafter" action for an ancestor element.
							//we need to push the levelForPushAfter to stack before
							//initialize it.
							levelForPushAfterStack.push(levelForPushAfter);
							contentForPushAfterStack.push(contentForPushAfter);
						}else{
							hasPushafter = true;
						}						
						levelForPushAfter = 0;
						levelForPushAfter ++;
						contentForPushAfter = replaceElementName(classValue, movetable.remove(containkey));
						//The output for the pushcontent will be in endElement(...)
					}
				}
			
				//although the if branch before checked whether isReplaced is true
				//we still need to check here because isReplaced might be turn on.
				if (!isReplaced){
					//output the element
					output.write(LESS_THAN);
					output.write(name);
					for(int index = 0; index < atts.getLength(); index++){
						output.write(STRING_BLANK);
						output.write(atts.getQName(index));
						output.write("=\"");
						//Edited by william on 2009-11-8 for ampbug:2893664 start
						String value =  atts.getValue(index);
						value =  StringUtils.escapeXML(value);
						//Edited by william on 2009-11-8 for ampbug:2893664 end
						output.write(value);
						output.write("\"");
					}
					output.write(GREATER_THAN);
				}
			}catch (Exception e) {
				logger.logException(e);
			}
		}
	}


	@Override
	public void endDocument() throws SAXException {
		try{
			output.flush();
			output.close();
		}catch (Exception e) {
			logger.logException(e);
		}finally{
			try{
				output.close();
			}catch (Exception e) {
				logger.logException(e);
			}
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if(!isReplaced){
			try{
				output.write(ch, start, length);
			}catch (Exception e) {
				logger.logException(e);
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}
	
	//Added by william on 2009-11-8 for ampbug:2893664 start
	@Override
	public void startEntity(String name) throws SAXException {
            try {
            	needResolveEntity = StringUtils.checkEntity(name);
            	if(!needResolveEntity){
            		output.write(StringUtils.getEntity(name));
            	}
            } catch (Exception e) {
            	//logger.logException(e);
            }
    }
	
	@Override
	public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}
	//Added by william on 2009-11-8 for ampbug:2893664 end
}
