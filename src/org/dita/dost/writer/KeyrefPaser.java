/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
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
import org.xml.sax.helpers.XMLReaderFactory;
/**
 * KeyrefPaser class parsing keyref attribute.
 *
 */
public class KeyrefPaser extends AbstractXMLWriter {

	private XMLReader parser = null;

	private DITAOTJavaLogger javaLogger = null;

	private OutputStreamWriter output = null;

	private Content content;

	private String tempDir;

	// It is stack used to store the place of current element
	// relative to the key reference element. Because keyref can be nested.
	private Stack<Integer> keyrefLevalStack;

	// It is used to store the place of current element
	// relative to the key reference element. If it is out of range of key
	// reference element it is zero, otherwise it is positive number.
	// It is also used to indicate whether current element is descendant of the
	// key reference element.
	private int keyrefLeval;

	// flat for whether the ancestor element has keyref
	//private boolean hasKeyref;

	// relative path of the filename to the temp directory
	private String filepath;

	// It is the element set which does contain attribute href,
	private static Set<String> withHref = new HashSet<String>();

	// It is the element set which does not contain attribute href.
	private static Set<String> withOutHref = new HashSet<String>();

	// It is the attributes set which should not be copied from
	// key definition to key reference which is <topicref>
	private static Set<String> no_copy = new HashSet<String>();

	// It is the attributes set which should not be copied from
	// key definition to key reference which is not <topicref>
	private static Set<String> no_copy_topic = new HashSet<String>();

	// It is used to store the target of the keys
	// In the from the map <keys, target>.
	private Map<String, String> keyMap;

	// It is used to indicate whether the keyref is valid.
	// The descendant element should know whether keyref is valid.
	// Because keryef can be nested
	private Stack<Boolean> validKeyref;

	// Flag indicating whether the key reference element is empty,
	// If it is empty, it should pull matching content from the key definition.
	private boolean empty;

	// It is used to store the value of attribute keyref,
	// Because keyref can be nested.
	//private Stack<String> keyref;
	
	// It is used to store the name of the element containing keyref attribute.
	private Stack<String> elemName;
	
	// It is used to store the class value of the element, because in the function of 
	// endElement() the class value can not be acquired.
	private String classValue;
	
	private boolean hasChecked;
	
	// It is used to indicate whether key reference element has sub element. 
	private Stack<Boolean> hasSubElem;
	
	private Document doc;
	
	// added By Alan for ID: 2860433 on 2009-09-17
	// file name with relative path to the tempDir of input file.
	private String fileName;

	static {
		withHref.add("topic/author");
		withHref.add("topic/data");
		withHref.add("topic/data-about");
		withHref.add("topic/image");
		withHref.add("topic/link");
		withHref.add("topic/lq");
		withHref.add("topic/navref");
		withHref.add("topic/publisher");
		withHref.add("topic/source");
		withHref.add("map/topicref");
		withHref.add("topic/xref");
	}



	static {
		no_copy.add("id");
		no_copy.add("class");
		no_copy.add("xtrc");
		no_copy.add("xtrf");
		no_copy.add("href");
		no_copy.add("keys");
		//added by William on 2009-09-25 for keyref bug:2866204 start
		no_copy.add(Constants.ATTRIBUTE_NAME_TOC);
		no_copy.add(Constants.ATTRIBUTE_NAME_PROCESSING_ROLE);
		//added by William on 2009-09-25 for keyref bug:2866204 end
	}
	
	static {
		no_copy_topic.addAll(no_copy);
		no_copy_topic.add("query");
		no_copy_topic.add("search");
		no_copy_topic.add("toc");
		no_copy_topic.add("print");
		no_copy_topic.add("copy-to");
		no_copy_topic.add("chunk");
		no_copy_topic.add("navtitle");
	}

	static {
		withOutHref.add("topic/cite");
		withOutHref.add("topic/dt");
		withOutHref.add("topic/keyword");
		withOutHref.add("topic/term");
		withOutHref.add("topic/ph");
		withOutHref.add("topic/indexterm");
		withOutHref.add("topic/index-base");
		withOutHref.add("topic/indextermref");
	}
	/**
	 * Constructor.
	 */
	public KeyrefPaser() {
		javaLogger = new DITAOTJavaLogger();
		keyrefLeval = 0;
		//hasKeyref = false;
		keyrefLevalStack = new Stack<Integer>();
		validKeyref = new Stack<Boolean>();
		empty = true;
		keyMap = new HashMap<String, String>();
		//keyref = new Stack<String>();
		elemName = new Stack<String>();
		hasSubElem = new Stack<Boolean>();
		try {
			parser = XMLReaderFactory.createXMLReader();
			parser.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			parser.setFeature(Constants.FEATURE_NAMESPACE, true);
			parser.setContentHandler(this);
		} catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			if (keyrefLeval != 0 && new String(ch,start,length).trim().length() == 0) {
				if(!hasChecked)
					empty = true;
			}else{
				hasChecked = true;
				empty = false;
			}
			output.write(StringUtils.escapeXML(ch, start, length));
		} catch (IOException e) {

			javaLogger.logException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			output.flush();
			output.close();
		} catch (Exception e) {
			javaLogger.logException(e);
		} finally {
			try {
				output.close();
			} catch (Exception e) {
				javaLogger.logException(e);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// write the end element
		try {
			
			if (keyrefLeval != 0 && empty && !elemName.peek().equals("topicref")) {
				// If current element is in the scope of key reference element 
				// and the element is empty
				if (!validKeyref.isEmpty() && validKeyref.peek()) {
					// Key reference is valid, 
					// need to pull matching content from the key definition
					Element  elem = doc.getDocumentElement();
					NodeList nodeList = null;
					// If current element name doesn't equal the key reference element
					// just grab the content from the matching element of key definition
					if(!name.equals(elemName.peek())){
						nodeList = elem.getElementsByTagName(name);
						if(nodeList.getLength() > 0){
							Node node = nodeList.item(0);
							NodeList nList = node.getChildNodes();
							int index = 0;
							while(index < nList.getLength()){
								Node n = nList.item(index++);
								if(n.getNodeType() == Node.TEXT_NODE){
									output.write(n.getNodeValue());
									break;
								}
							}
							output.flush();
						}
					}else{
						// Current element name equals the key reference element
						// grab keyword or term from key definition
						nodeList = elem.getElementsByTagName("keyword");
						if(nodeList.getLength() == 0 ){
							nodeList = elem.getElementsByTagName("term");
						}
						if(!hasSubElem.peek()){
							if(nodeList.getLength() > 0){
								if(withOutHref.contains(classValue)){
									// only one keyword or term is used.
									output.write(nodeToString((Element)nodeList.item(0), false));
									output.flush();
								} else if(withHref.contains(classValue) ){
									// If the key reference element carries href attribute 
									// all keyword or term are used.
									if(classValue.equals("topic/link")){
										output.write("<linktext class=\" topic/linktext \">");
									}
									for(int index =0; index<nodeList.getLength(); index++){
										Node node = nodeList.item(index);
										if(node.getNodeType() == Node.ELEMENT_NODE){
											output.write(nodeToString((Element)node, true));
										}
									}
									if(classValue.equals("topic/link")){
										output.write("</linktext>");
									}
									output.flush();
								}
							}else{
								if(classValue.equals("topic/link")){
									// If the key reference element is link or its specification, 
									// should pull in the linktext
									NodeList linktext = elem.getElementsByTagName("linktext");
									if(linktext.getLength()>0){
										output.write(nodeToString((Element)linktext.item(0), true));
									}else if (!StringUtils.isEmptyString(elem.getAttribute("navtitle"))){
										output.write("<linktext class=\" topic/linktext \">");
										output.append(elem.getAttribute("navtitle"));
										output.write("</linktext>");
									}
								}else if(withHref.contains(classValue)){
									NodeList linktext = elem.getElementsByTagName("linktext");
									if(linktext.getLength()>0){
										output.write(nodeToString((Element)linktext.item(0), false));
									}else{
										output.append(elem.getAttribute("navtitle"));
									}
								}
								output.flush();
							}
								
						}
					}
				}
			}
			if (keyrefLeval != 0){
				keyrefLeval--;
				empty = false;
			}

			if (keyrefLeval == 0 && !keyrefLevalStack.empty()) {
				// To the end of key reference, pop the stacks.
				keyrefLeval = keyrefLevalStack.pop();
				validKeyref.pop();
				elemName.pop();
				hasSubElem.pop();
			}
			output.write(Constants.LESS_THAN);
			output.write(Constants.SLASH);
			output.write(name);
			output.write(Constants.GREATER_THAN);

		} catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		try {
			output.write(ch, start, length);
		} catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		try {
			String pi = (data != null) ? target + Constants.STRING_BLANK + data
					: target;
			output.write(Constants.LESS_THAN + Constants.QUESTION + pi
					+ Constants.QUESTION + Constants.GREATER_THAN);
		} catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	@Override
	public void setContent(Content content) {
		this.content = content;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		try {
			hasChecked = false;
			empty = true;
			output.write(Constants.LESS_THAN);
			output.write(name);
			boolean valid = false;
			classValue = atts
			.getValue(Constants.ATTRIBUTE_NAME_CLASS);
			classValue = classValue.substring(classValue.indexOf(Constants.STRING_BLANK) + 1, classValue.indexOf(Constants.STRING_BLANK, 4));
			if (atts.getIndex(Constants.ATTRIBUTE_NAME_KEYREF) == -1) {
				// If the keyrefLeval doesn't equal 0, it means that current element is under the key reference element
				if(keyrefLeval != 0){
					keyrefLeval ++;
					hasSubElem.pop();
					hasSubElem.push(true);
				}
				// Output the attributes directly
				for (int index = 0; index < atts.getLength(); index++) {
					output.write(Constants.STRING_BLANK);
					output.write(atts.getQName(index));
					output.write("=\"");
					output.write(StringUtils.escapeXML(atts.getValue(index))); //2878446
					output.write("\"");
				}
			} else {
				// If there is @keyref, use the key definition to do
				// combination.
				// HashSet to store the attributes copied from key
				// definition to key reference.
				
				elemName.push(name);
				Set<String> aset = new HashSet<String>();
				//hasKeyref = true;
				if (keyrefLeval != 0) {
					keyrefLevalStack.push(keyrefLeval);
					hasSubElem.pop();
					hasSubElem.push(true);
				}
				hasSubElem.push(false);
				keyrefLeval = 0;
				keyrefLeval++;
				//keyref.push(atts.getValue(Constants.ATTRIBUTE_NAME_KEYREF));
				//Edit by Alan for bug ID: 2849078   date:2009-09-03  --start
				//the @keyref could be in the following forms:
				// 1.keyName 2.keyName/elementId 
				/*String definition = ((Hashtable<String, String>) content
						.getValue()).get(atts
						.getValue(Constants.ATTRIBUTE_NAME_KEYREF));*/
				String keyrefValue=atts.getValue(Constants.ATTRIBUTE_NAME_KEYREF);
				int slashIndex=keyrefValue.indexOf(Constants.SLASH);
				String keyName= keyrefValue;
				String tail= "";
				if (slashIndex != -1) {
					keyName = keyrefValue.substring(0, slashIndex);
					tail = keyrefValue.substring(slashIndex);
				}
				String definition = ((Hashtable<String, String>)content.getValue()).get(keyName);
				//Edit by Alan for bug ID: 2849078   date:2009-09-03  --End
				
				// If definition is not null 
				if(definition!=null){
					doc = keyDefToDoc(definition);
					Element elem = doc.getDocumentElement();
					NamedNodeMap namedNodeMap = elem.getAttributes();
					// first resolve the keyref attribute
					if (withHref.contains(classValue)) {
						String target = keyMap.get(keyName);
						if (target != null && !target.equals(Constants.STRING_EMPTY)) {
							String target_output = target;
							// if the scope equals local, the target should be verified that
							// it exists, and add the href and scope to aSet.
							String scopeValue=elem.getAttribute(Constants.ATTRIBUTE_NAME_SCOPE);						 
							if (("".equals(scopeValue) || "local".equals(scopeValue)) && !"topic/image".equals(classValue)){
								target = FileUtils.replaceExtName(target, extName);
								if (new File(FileUtils.resolveFile(tempDir, target))
										.exists()) {
									//Added by William on 2010-05-26 for bug:3004060 start
									File topicFile = new File(FileUtils.resolveFile(tempDir, target));
									String topicId = this.getFirstTopicId(topicFile);
									//Added by William on 2010-05-26 for bug:3004060 end
									target_output = FileUtils
											.getRelativePathFromMap(filepath,
													new File(tempDir, target)
															.getAbsolutePath());
									valid = true;
									aset.add("href");
									aset.add("scope");
									aset.add("type");
									aset.add("format");
									output.write(Constants.STRING_BLANK);
									output.write(Constants.ATTRIBUTE_NAME_HREF);
									output.write("=\"");
									target_output = normalizeHrefValue(target_output, tail, topicId);
									output.write(target_output);
									output.write("\"");
								} else {
									// referenced file does not exist, emits a message.
									// Should only emit this if in a debug mode; comment out for now
									/*Properties prop = new Properties();
									prop.put("%1", atts.getValue("keyref"));
									javaLogger
											.logInfo(MessageUtils.getMessage("DOTJ047I", prop)
													.toString());*/
								}
							} 
							// scope equals peer or external
							else {
								// added By Alan for ID: 2860433 on 2009-09-17
								// get the relative path
								if("topic/image".equals(classValue)){
									target_output = FileUtils.getRelativePathFromMap(fileName, target_output);
								}							
								valid = true;
								aset.add("scope");
								aset.add("href");
								aset.add("type");
								aset.add("format");
								output.write(Constants.STRING_BLANK);
								output.write(Constants.ATTRIBUTE_NAME_HREF);
								output.write("=\"");
								target_output = normalizeHrefValue(target_output, tail);
								output.write(target_output);
								output.write("\"");
							}
	
						} else if(target.equals(Constants.STRING_EMPTY)){
							// Key definition does not carry an href or href equals "".
							valid = true;
							aset.add("scope");
							aset.add("href");
							aset.add("type");
							aset.add("format");
						}else{
							// key does not exist.
							Properties prop = new Properties();
							prop.put("%1", atts.getValue("keyref"));
							javaLogger
									.logInfo(MessageUtils.getMessage("DOTJ047I", prop)
											.toString());
						}
	
					} else if (withOutHref.contains(classValue)) {
						String target = keyMap.get(keyName);
	
						if (target != null) {
							valid = true;
							aset.add("scope");
							aset.add("href");
							aset.add("type");
							aset.add("format");
						} else {
							// key does not exist
							Properties prop = new Properties();
							prop.put("%1", atts.getValue("keyref"));
							javaLogger
									.logInfo(MessageUtils.getMessage("DOTJ047I", prop)
											.toString());
						}
	
					}
					
	
					// copy attributes in key definition to key reference
					// Set no_copy and no_copy_topic define some attributes should not be copied.
					if (valid) {
						if (classValue.contains("map/topicref")) {
							// @keyref in topicref
							for (int index = 0; index < namedNodeMap.getLength(); index++) {
								Node node = namedNodeMap.item(index);
								if (node.getNodeType() == Node.ATTRIBUTE_NODE
										&& !no_copy.contains(node.getNodeName())) {
									aset.add(node.getNodeName());
									output.append(Constants.STRING_BLANK);
									output.append(node.getNodeName());
									output.write("=\"");
									output.write(node.getNodeValue());
									output.write("\"");
								}
							}
						} else {
							// @keyref not in topicref
							// different elements have different attributes
							if (withHref.contains(classValue)) {
								// current element with href attribute
								for (int index = 0; index < namedNodeMap.getLength(); index++) {
									Node node = namedNodeMap.item(index);
									if (node.getNodeType() == Node.ATTRIBUTE_NODE
											&& !no_copy_topic.contains(node
													.getNodeName())) {
										aset.add(node.getNodeName());
										output.append(Constants.STRING_BLANK);
										output.append(node.getNodeName());
										output.write("=\"");
										output.write(node.getNodeValue());
										output.write("\"");
									}
								}
							} else if (withOutHref.contains(classValue)) {
								// current element without href attribute
								// so attributes about href should not be copied.
								for (int index = 0; index < namedNodeMap.getLength(); index++) {
									Node node = namedNodeMap.item(index);
									if (node.getNodeType() == Node.ATTRIBUTE_NODE
											&& !no_copy_topic.contains(node
													.getNodeName())
											&& !(node.getNodeName().equals("scope")
													|| node.getNodeName().equals(
															"format") || node
													.getNodeName().equals("type"))) {
										aset.add(node.getNodeName());
										output.append(Constants.STRING_BLANK);
										output.append(node.getNodeName());
										output.write("=\"");
										output.write(node.getNodeValue());
										output.write("\"");
									}
								}
							}
	
						}
					} else {
						// keyref is not valid, don't copy any attribute.
					}
				}else{
					// key does not exist
					Properties prop = new Properties();
					prop.put("%1", atts.getValue("keyref"));
					javaLogger
							.logInfo(MessageUtils.getMessage("DOTJ047I", prop)
									.toString());;
				}
				
				validKeyref.push(valid);

				// output attributes which are not replaced in current element
				// in the help of aSet. aSet stores the attributes which have been copied
				// from key definition to key reference.
				for (int index = 0; index < atts.getLength(); index++) {
					if (!aset.contains(atts.getQName(index))) {
						output.append(Constants.STRING_BLANK);
						output.append(atts.getQName(index));
						output.write("=\"");
                        output.write(StringUtils.escapeXML(atts.getValue(index))); //2878446
						output.write("\"");
					}
				}

			}

			output.write(Constants.GREATER_THAN);

			output.flush();
		} catch (IOException e) {
			javaLogger.logException(e);
		}

	}

	@Override
	public void write(String filename) throws DITAOTException {
		// added By Alan for ID: 2860433 on 2009-09-17
		this.fileName=filename;
		try {
			File inputFile = new File(tempDir, filename);
			filepath = inputFile.getAbsolutePath();
			File outputFile = new File(tempDir, filename + "keyref");
			output = new OutputStreamWriter(new FileOutputStream(outputFile),Constants.UTF8);
			parser.parse(inputFile.getAbsolutePath());
			output.close();
			if (!inputFile.delete()) {
				Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				javaLogger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}
			if (!outputFile.renameTo(inputFile)) {
				Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				javaLogger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}

		} catch (Exception e) {
			javaLogger.logException(e);
		} finally {
			try {
				output.close();
			} catch (Exception ex) {
				javaLogger.logException(ex);
			}
		}

	}
	/**
	 * Set temp dir.
	 * @param tempDir temp dir
	 */
	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}
	/**
	 * Set key map.
	 * @param map key map
	 */
	public void setKeyMap(Map<String, String> map) {
		this.keyMap = map;
	}

	private Document keyDefToDoc(String key) {
		InputSource inputSource = null;
		Document document = null;
		inputSource = new InputSource(new StringReader(key));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			document = documentBuilder.parse(inputSource);
			return document;
		} catch (Exception e) {
			javaLogger.logException(e);
			return document;
		}
	}
	
	private String nodeToString(Element elem, boolean flag){
		// use flag to indicate that whether there is need to copy the element name
		StringBuffer stringBuffer = new StringBuffer();
		if(flag){
			stringBuffer.append(Constants.LESS_THAN).append(elem.getNodeName());
			NamedNodeMap namedNodeMap = elem.getAttributes();
			for(int i=0; i<namedNodeMap.getLength(); i++){
				String classValue = namedNodeMap.item(i).getNodeValue();
				if(namedNodeMap.item(i).getNodeName().equals("class"))
					classValue = changeclassValue(classValue);
				stringBuffer.append(Constants.STRING_BLANK).append(namedNodeMap.item(i).getNodeName()).append(Constants.EQUAL).append(Constants.QUOTATION+classValue+Constants.QUOTATION);
			}
			stringBuffer.append(Constants.GREATER_THAN);
		}
		NodeList nodeList = elem.getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				//Added by William on 2010-05-20 for bug:3004220 start
				//special process for tm tag.
				String classValue = node.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_CLASS).getNodeValue();
				if(classValue.contains(" topic/tm ")){
					stringBuffer.append(nodeToString((Element)node, true));
				}else{
					// If the type of current node is ELEMENT_NODE, process current node.
					stringBuffer.append(nodeToString((Element)node, flag));
				}
				//Added by William on 2010-05-20 for bug:3004220 end
				// If the type of current node is ELEMENT_NODE, process current node.
				//stringBuffer.append(nodeToString((Element)node, flag));
			}
			if(node.getNodeType() == Node.TEXT_NODE){
				stringBuffer.append(node.getNodeValue());
			}
		}
		if(flag)
			stringBuffer.append("</").append(elem.getNodeName()).append(Constants.GREATER_THAN);
		return stringBuffer.toString();
	}
	
	private String changeclassValue(String classValue){
		return classValue.replaceAll("map/", "topic/");
	}
	
	//Added by Alan Date:2009-08-04 --begin
	private String extName;
	/**
	 * Get extension name.
	 * @return extension name
	 */
	public String getExtName() {
		return extName;
	}
	/**
	 * Set extension name.
	 * @param extName extension name
	 */
	public void setExtName(String extName) {
		this.extName = extName;
	}
	//Added by Alan Date:2009-08-04 --end
	
	//Added by Alan Date:2009-09-03 Bug ID: 2849078
	//change elementId into topicId if there is no topicId in key definition.
	private static String normalizeHrefValue(String keyName, String tail) {
		int sharpIndex=keyName.indexOf(Constants.SHARP);
		if(sharpIndex == -1){
			return keyName + tail.replaceAll(Constants.SLASH, Constants.SHARP);
		}
		return keyName + tail;
	}
	
	//Added by William on 2010-05-26 for bug:3004060 start
	//Get first topic id
	private String getFirstTopicId(File topicFile) {
		String path = topicFile.getParent();
		String name = topicFile.getName();
		MergeUtils util = MergeUtils.getInstance();
		String topicId = util.getFirstTopicId(name, path, false);
		return topicId;
	}
	//Insert topic id into href 
	private static String normalizeHrefValue(String fileName, String tail, String topicId) {
		int sharpIndex=fileName.indexOf(Constants.SHARP);
		//Insert first topic id only when topicid is not set in keydef 
		//and keyref has elementid
		if(sharpIndex == -1 && !"".equals(tail)){
			return fileName + Constants.SHARP + topicId + tail;
		}
		return fileName + tail;
	}
	//Added by William on 2010-05-26 for bug:3004060 end
}
