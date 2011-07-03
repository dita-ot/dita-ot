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
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
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
/**
 * KeyrefPaser class parsing keyref attribute.
 *
 */
public final class KeyrefPaser extends AbstractXMLWriter {

	private XMLReader parser = null;

	private OutputStreamWriter output = null;

	private Content content;

	private String tempDir;

	// It is stack used to store the place of current element
	// relative to the key reference element. Because keyref can be nested.
	private final Stack<Integer> keyrefLevalStack;

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
	private static final Set<String> withHref = new HashSet<String>();

	// It is the element set which does not contain attribute href.
	private static final Set<String> withOutHref = new HashSet<String>();

	// It is the attributes set which should not be copied from
	// key definition to key reference which is <topicref>
	private static final Set<String> no_copy = new HashSet<String>();

	// It is the attributes set which should not be copied from
	// key definition to key reference which is not <topicref>
	private static final Set<String> no_copy_topic = new HashSet<String>();

	// It is used to store the target of the keys
	// In the from the map <keys, target>.
	private Map<String, String> keyMap;

	// It is used to indicate whether the keyref is valid.
	// The descendant element should know whether keyref is valid.
	// Because keryef can be nested
	private final Stack<Boolean> validKeyref;

	// Flag indicating whether the key reference element is empty,
	// If it is empty, it should pull matching content from the key definition.
	private boolean empty;

	// It is used to store the value of attribute keyref,
	// Because keyref can be nested.
	//private Stack<String> keyref;
	
	// It is used to store the name of the element containing keyref attribute.
	private final Stack<String> elemName;
	
	// It is used to store the class value of the element, because in the function of 
	// endElement() the class value can not be acquired.
	private String classValue;
	
	private boolean hasChecked;
	
	// It is used to indicate whether key reference element has sub element. 
	private final Stack<Boolean> hasSubElem;
	
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
		no_copy.add(ATTRIBUTE_NAME_ID);
		no_copy.add(ATTRIBUTE_NAME_CLASS);
		no_copy.add(ATTRIBUTE_NAME_XTRC);
		no_copy.add(ATTRIBUTE_NAME_XTRF);
		no_copy.add(ATTRIBUTE_NAME_HREF);
		no_copy.add(ATTRIBUTE_NAME_KEYS);
		//added by William on 2009-09-25 for keyref bug:2866204 start
		no_copy.add(ATTRIBUTE_NAME_TOC);
		no_copy.add(ATTRIBUTE_NAME_PROCESSING_ROLE);
		//added by William on 2009-09-25 for keyref bug:2866204 end
	}
	
	static {
		no_copy_topic.addAll(no_copy);
		no_copy_topic.add("query");
		no_copy_topic.add("search");
		no_copy_topic.add(ATTRIBUTE_NAME_TOC);
		no_copy_topic.add(ATTRIBUTE_NAME_PRINT);
		no_copy_topic.add(ATTRIBUTE_NAME_COPY_TO);
		no_copy_topic.add(ATTRIBUTE_NAME_CHUNK);
		no_copy_topic.add(ATTRIBUTE_NAME_NAVTITLE);
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
			parser = StringUtils.getXMLReader();
			parser.setFeature(FEATURE_NAMESPACE_PREFIX, true);
			parser.setFeature(FEATURE_NAMESPACE, true);
			parser.setContentHandler(this);
		} catch (final Exception e) {
			logger.logException(e);
		}
	}

	@Override
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		try {
			if (keyrefLeval != 0 && new String(ch,start,length).trim().length() == 0) {
				if(!hasChecked) {
                    empty = true;
                }
			}else{
				hasChecked = true;
				empty = false;
			}
			output.write(StringUtils.escapeXML(ch, start, length));
		} catch (final IOException e) {

			logger.logException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			output.flush();
			output.close();
		} catch (final Exception e) {
			logger.logException(e);
		} finally {
			try {
				output.close();
			} catch (final Exception e) {
				logger.logException(e);
			}
		}
	}

	@Override
	public void endElement(final String uri, final String localName, final String name)
			throws SAXException {
		// write the end element
		try {
			
			if (keyrefLeval != 0 && empty && !elemName.peek().equals(MAP_TOPICREF.localName)) {
				// If current element is in the scope of key reference element 
				// and the element is empty
				if (!validKeyref.isEmpty() && validKeyref.peek()) {
					// Key reference is valid, 
					// need to pull matching content from the key definition
					final Element  elem = doc.getDocumentElement();
					NodeList nodeList = null;
					// If current element name doesn't equal the key reference element
					// just grab the content from the matching element of key definition
					if(!name.equals(elemName.peek())){
						nodeList = elem.getElementsByTagName(name);
						if(nodeList.getLength() > 0){
							final Node node = nodeList.item(0);
							final NodeList nList = node.getChildNodes();
							int index = 0;
							while(index < nList.getLength()){
								final Node n = nList.item(index++);
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
										output.write("<linktext class=\"- topic/linktext \">");
									}
									for(int index =0; index<nodeList.getLength(); index++){
										final Node node = nodeList.item(index);
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
									final NodeList linktext = elem.getElementsByTagName("linktext");
									if(linktext.getLength()>0){
										output.write(nodeToString((Element)linktext.item(0), true));
									}else if (!StringUtils.isEmptyString(elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE))){
										output.write("<linktext class=\"- topic/linktext \">");
										output.append(elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE));
										output.write("</linktext>");
									}
								}else if(withHref.contains(classValue)){
									final NodeList linktext = elem.getElementsByTagName(TOPIC_LINKTEXT.localName);
									if(linktext.getLength()>0){
										output.write(nodeToString((Element)linktext.item(0), false));
									}else{
										output.append(elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE));
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
			output.write(LESS_THAN);
			output.write(SLASH);
			output.write(name);
			output.write(GREATER_THAN);

		} catch (final Exception e) {
			logger.logException(e);
		}
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		try {
			output.write(ch, start, length);
		} catch (final Exception e) {
			logger.logException(e);
		}
	}

	@Override
	public void processingInstruction(final String target, final String data)
			throws SAXException {
		try {
			final String pi = (data != null) ? target + STRING_BLANK + data
					: target;
			output.write(LESS_THAN + QUESTION + pi
					+ QUESTION + GREATER_THAN);
		} catch (final Exception e) {
			logger.logException(e);
		}
	}

	@Override
	public void setContent(final Content content) {
		this.content = content;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(final String uri, final String localName, final String name,
			final Attributes atts) throws SAXException {
		try {
			hasChecked = false;
			empty = true;
			output.write(LESS_THAN);
			output.write(name);
			boolean valid = false;
			classValue = atts
			.getValue(ATTRIBUTE_NAME_CLASS);
			classValue = classValue.substring(classValue.indexOf(STRING_BLANK) + 1, classValue.indexOf(STRING_BLANK, 4));
			if (atts.getIndex(ATTRIBUTE_NAME_KEYREF) == -1) {
				// If the keyrefLeval doesn't equal 0, it means that current element is under the key reference element
				if(keyrefLeval != 0){
					keyrefLeval ++;
					hasSubElem.pop();
					hasSubElem.push(true);
				}
				// Output the attributes directly
				for (int index = 0; index < atts.getLength(); index++) {
					output.write(STRING_BLANK);
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
				final Set<String> aset = new HashSet<String>();
				//hasKeyref = true;
				if (keyrefLeval != 0) {
					keyrefLevalStack.push(keyrefLeval);
					hasSubElem.pop();
					hasSubElem.push(true);
				}
				hasSubElem.push(false);
				keyrefLeval = 0;
				keyrefLeval++;
				//keyref.push(atts.getValue(ATTRIBUTE_NAME_KEYREF));
				//Edit by Alan for bug ID: 2849078   date:2009-09-03  --start
				//the @keyref could be in the following forms:
				// 1.keyName 2.keyName/elementId 
				/*String definition = ((Hashtable<String, String>) content
						.getValue()).get(atts
						.getValue(ATTRIBUTE_NAME_KEYREF));*/
				final String keyrefValue=atts.getValue(ATTRIBUTE_NAME_KEYREF);
				final int slashIndex=keyrefValue.indexOf(SLASH);
				String keyName= keyrefValue;
				String tail= "";
				if (slashIndex != -1) {
					keyName = keyrefValue.substring(0, slashIndex);
					tail = keyrefValue.substring(slashIndex);
				}
				final String definition = ((Hashtable<String, String>)content.getValue()).get(keyName);
				//Edit by Alan for bug ID: 2849078   date:2009-09-03  --End
				
				// If definition is not null 
				if(definition!=null){
					doc = keyDefToDoc(definition);
					final Element elem = doc.getDocumentElement();
					final NamedNodeMap namedNodeMap = elem.getAttributes();
					// first resolve the keyref attribute
					if (withHref.contains(classValue)) {
						String target = keyMap.get(keyName);
						if (target != null && target.length() != 0) {
							String target_output = target;
							// if the scope equals local, the target should be verified that
							// it exists, and add the href and scope to aSet.
							final String scopeValue=elem.getAttribute(ATTRIBUTE_NAME_SCOPE);						 
							if (("".equals(scopeValue) || "local".equals(scopeValue)) && !"topic/image".equals(classValue)){
								target = FileUtils.replaceExtName(target, extName);
								if (new File(FileUtils.resolveFile(tempDir, target))
										.exists()) {
									//Added by William on 2010-05-26 for bug:3004060 start
									final File topicFile = new File(FileUtils.resolveFile(tempDir, target));
									final String topicId = this.getFirstTopicId(topicFile);
									//Added by William on 2010-05-26 for bug:3004060 end
									target_output = FileUtils
											.getRelativePathFromMap(filepath,
													new File(tempDir, target)
															.getAbsolutePath());
									valid = true;
									aset.add(ATTRIBUTE_NAME_HREF);
									aset.add(ATTRIBUTE_NAME_SCOPE);
									aset.add(ATTRIBUTE_NAME_TYPE);
									aset.add(ATTRIBUTE_NAME_FORMAT);
									output.write(STRING_BLANK);
									output.write(ATTRIBUTE_NAME_HREF);
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
								aset.add(ATTRIBUTE_NAME_SCOPE);
								aset.add(ATTRIBUTE_NAME_HREF);
								aset.add(ATTRIBUTE_NAME_TYPE);
								aset.add(ATTRIBUTE_NAME_FORMAT);
								output.write(STRING_BLANK);
								output.write(ATTRIBUTE_NAME_HREF);
								output.write("=\"");
								target_output = normalizeHrefValue(target_output, tail);
								output.write(target_output);
								output.write("\"");
							}
	
						} else if(target.length() == 0){
							// Key definition does not carry an href or href equals "".
							valid = true;
							aset.add(ATTRIBUTE_NAME_SCOPE);
							aset.add(ATTRIBUTE_NAME_HREF);
							aset.add(ATTRIBUTE_NAME_TYPE);
							aset.add(ATTRIBUTE_NAME_FORMAT);
						}else{
							// key does not exist.
							final Properties prop = new Properties();
							prop.put("%1", atts.getValue("keyref"));
							logger
									.logInfo(MessageUtils.getMessage("DOTJ047I", prop)
											.toString());
						}
	
					} else if (withOutHref.contains(classValue)) {
						final String target = keyMap.get(keyName);
	
						if (target != null) {
							valid = true;
							aset.add(ATTRIBUTE_NAME_SCOPE);
							aset.add(ATTRIBUTE_NAME_HREF);
							aset.add(ATTRIBUTE_NAME_TYPE);
							aset.add(ATTRIBUTE_NAME_FORMAT);
						} else {
							// key does not exist
							final Properties prop = new Properties();
							prop.put("%1", atts.getValue("keyref"));
							logger
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
								final Node node = namedNodeMap.item(index);
								if (node.getNodeType() == Node.ATTRIBUTE_NODE
										&& !no_copy.contains(node.getNodeName())) {
									aset.add(node.getNodeName());
									output.append(STRING_BLANK);
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
									final Node node = namedNodeMap.item(index);
									if (node.getNodeType() == Node.ATTRIBUTE_NODE
											&& !no_copy_topic.contains(node
													.getNodeName())) {
										aset.add(node.getNodeName());
										output.append(STRING_BLANK);
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
									final Node node = namedNodeMap.item(index);
									if (node.getNodeType() == Node.ATTRIBUTE_NODE
											&& !no_copy_topic.contains(node
													.getNodeName())
											&& !(node.getNodeName().equals(ATTRIBUTE_NAME_SCOPE)
													|| node.getNodeName().equals(
															ATTRIBUTE_NAME_FORMAT) || node
													.getNodeName().equals(ATTRIBUTE_NAME_TYPE))) {
										aset.add(node.getNodeName());
										output.append(STRING_BLANK);
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
					final Properties prop = new Properties();
					prop.put("%1", atts.getValue("keyref"));
					logger
							.logInfo(MessageUtils.getMessage("DOTJ047I", prop)
									.toString());;
				}
				
				validKeyref.push(valid);

				// output attributes which are not replaced in current element
				// in the help of aSet. aSet stores the attributes which have been copied
				// from key definition to key reference.
				for (int index = 0; index < atts.getLength(); index++) {
					if (!aset.contains(atts.getQName(index))) {
						output.append(STRING_BLANK);
						output.append(atts.getQName(index));
						output.write("=\"");
                        output.write(StringUtils.escapeXML(atts.getValue(index))); //2878446
						output.write("\"");
					}
				}

			}

			output.write(GREATER_THAN);

			output.flush();
		} catch (final IOException e) {
			logger.logException(e);
		}

	}

	@Override
	public void write(final String filename) throws DITAOTException {
		// added By Alan for ID: 2860433 on 2009-09-17
		this.fileName=filename;
		try {
			final File inputFile = new File(tempDir, filename);
			filepath = inputFile.getAbsolutePath();
			final File outputFile = new File(tempDir, filename + "keyref");
			output = new OutputStreamWriter(new FileOutputStream(outputFile),UTF8);
			parser.parse(inputFile.getAbsolutePath());
			output.close();
			if (!inputFile.delete()) {
				final Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}
			if (!outputFile.renameTo(inputFile)) {
				final Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}

		} catch (final Exception e) {
			logger.logException(e);
		} finally {
			try {
				output.close();
			} catch (final Exception ex) {
				logger.logException(ex);
			}
		}

	}
	/**
	 * Set temp dir.
	 * @param tempDir temp dir
	 */
	public void setTempDir(final String tempDir) {
		this.tempDir = tempDir;
	}
	/**
	 * Set key map.
	 * @param map key map
	 */
	public void setKeyMap(final Map<String, String> map) {
		this.keyMap = map;
	}

	private Document keyDefToDoc(final String key) {
		InputSource inputSource = null;
		Document document = null;
		inputSource = new InputSource(new StringReader(key));
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try {
			final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
			document = documentBuilder.parse(inputSource);
			return document;
		} catch (final Exception e) {
			logger.logException(e);
			return document;
		}
	}
	
	private String nodeToString(final Element elem, final boolean flag){
		// use flag to indicate that whether there is need to copy the element name
		final StringBuffer stringBuffer = new StringBuffer();
		if(flag){
			stringBuffer.append(LESS_THAN).append(elem.getNodeName());
			final NamedNodeMap namedNodeMap = elem.getAttributes();
			for(int i=0; i<namedNodeMap.getLength(); i++){
				String classValue = namedNodeMap.item(i).getNodeValue();
				if(namedNodeMap.item(i).getNodeName().equals(ATTRIBUTE_NAME_CLASS)) {
                    classValue = changeclassValue(classValue);
                }
				stringBuffer.append(STRING_BLANK).append(namedNodeMap.item(i).getNodeName()).append(EQUAL).append(QUOTATION+classValue+QUOTATION);
			}
			stringBuffer.append(GREATER_THAN);
		}
		final NodeList nodeList = elem.getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++){
			final Node node = nodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				//Added by William on 2010-05-20 for bug:3004220 start
				//special process for tm tag.
				final String classValue = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS).getNodeValue();
				if(classValue.contains(TOPIC_TM.matcher)){
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
		if(flag) {
            stringBuffer.append("</").append(elem.getNodeName()).append(GREATER_THAN);
        }
		return stringBuffer.toString();
	}
	
	private String changeclassValue(final String classValue){
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
	public void setExtName(final String extName) {
		this.extName = extName;
	}
	//Added by Alan Date:2009-08-04 --end
	
	//Added by Alan Date:2009-09-03 Bug ID: 2849078
	//change elementId into topicId if there is no topicId in key definition.
	private static String normalizeHrefValue(final String keyName, final String tail) {
		final int sharpIndex=keyName.indexOf(SHARP);
		if(sharpIndex == -1){
			return keyName + tail.replaceAll(SLASH, SHARP);
		}
		return keyName + tail;
	}
	
	//Added by William on 2010-05-26 for bug:3004060 start
	//Get first topic id
	private String getFirstTopicId(final File topicFile) {
		final String path = topicFile.getParent();
		final String name = topicFile.getName();
		final String topicId = MergeUtils.getFirstTopicId(name, path, false);
		return topicId;
	}
	//Insert topic id into href 
	private static String normalizeHrefValue(final String fileName, final String tail, final String topicId) {
		final int sharpIndex=fileName.indexOf(SHARP);
		//Insert first topic id only when topicid is not set in keydef 
		//and keyref has elementid
		if(sharpIndex == -1 && !"".equals(tail)){
			return fileName + SHARP + topicId + tail;
		}
		return fileName + tail;
	}
	//Added by William on 2010-05-26 for bug:3004060 end
}
