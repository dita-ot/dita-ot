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
	private boolean hasKeyref;

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
	private Stack<String> keyref;
	
	// It is used to store the name of the element containing keyref attribute.
	private String elemName;
	
	// It is used to store the class value of the element, because in the function of 
	// endElement() the class value can not be acquired.
	private String classValue;
	
	private Document doc;

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
		no_copy_topic.addAll(no_copy);
		no_copy_topic.add("query");
		no_copy_topic.add("search");
		no_copy_topic.add("toc");
		no_copy_topic.add("print");
		no_copy_topic.add("locktitle");
		no_copy_topic.add("copy-to");
		no_copy_topic.add("chunk");
	}

	static {
		no_copy.add("id");
		no_copy.add("class");
		no_copy.add("xtrc");
		no_copy.add("xtrf");
		no_copy.add("href");
		no_copy.add("keys");

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

	public KeyrefPaser() {
		javaLogger = new DITAOTJavaLogger();
		keyrefLeval = 0;
		hasKeyref = false;
		keyrefLevalStack = new Stack<Integer>();
		validKeyref = new Stack<Boolean>();
		empty = true;
		keyMap = new HashMap<String, String>();
		keyref = new Stack<String>();
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
				empty = true;
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
			output.write(Constants.LESS_THAN);
			output.write(Constants.SLASH);
			output.write(name);
			output.write(Constants.GREATER_THAN);
			
			if (keyrefLeval != 0 && empty) {
				// If current element is in the scope of key reference element 
				// and the element is empty
				if (!validKeyref.isEmpty() && validKeyref.peek()) {
					// Key reference is valid, 
					// need to pull matching content from the key definition
					Element  elem = doc.getDocumentElement();
					NodeList nodeList = null;
					// If current element name doesn't equal the key reference element
					if(!name.equals(elemName)){
						nodeList = elem.getElementsByTagName(name);
						if(nodeList != null){
							output.write(nodeList.item(0).getNodeValue());
							output.flush();
						}
					}else{
						// Current element name equals the key reference element
						nodeList = elem.getElementsByTagName("keyword");
						if(nodeList == null){
							nodeList = elem.getElementsByTagName("term");
						}
						if(nodeList!=null){
							if(withOutHref.contains(classValue)){
								output.write(nodeToString((Element)nodeList.item(0)));
								output.flush();
							} else if(withHref.contains(classValue)){
								for(int index =0; index<nodeList.getLength(); index++){
									Node node = nodeList.item(index);
									if(node.getNodeType() == Node.ELEMENT_NODE)
										output.write(nodeToString((Element)node));
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
				keyrefLeval = keyrefLevalStack.pop();
				validKeyref.pop();
			}

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
			empty = true;
			if (keyrefLeval != 0 ) {
				empty = false;
			}
			output.write(Constants.LESS_THAN);
			output.write(name);
			boolean valid = false;
			classValue = atts
			.getValue(Constants.ATTRIBUTE_NAME_CLASS);
			classValue = classValue.substring(classValue.indexOf(Constants.STRING_BLANK) + 1, classValue.indexOf(Constants.STRING_BLANK, 4));
			if (atts.getIndex(Constants.ATTRIBUTE_NAME_KEYREF) == -1) {
				// Output the attributes directly
				for (int index = 0; index < atts.getLength(); index++) {
					output.write(Constants.STRING_BLANK);
					output.write(atts.getQName(index));
					output.write("=\"");
					output.write(atts.getValue(index));
					output.write("\"");
				}
			} else {
				// If there is @keyref, use the key definition to do
				// combination.
				// HashSet to store the attributes copied from key
				// definition to key reference.
				elemName = name;
				Set<String> aset = new HashSet<String>();

				hasKeyref = true;
				if (keyrefLeval != 0) {
					keyrefLevalStack.push(keyrefLeval);
				}
				keyrefLeval = 0;
				keyrefLeval++;
				keyref.push(atts.getValue(Constants.ATTRIBUTE_NAME_KEYREF));
				String definition = ((Hashtable<String, String>) content
						.getValue()).get(atts
						.getValue(Constants.ATTRIBUTE_NAME_KEYREF));
				// TODO if definition is null or it can not be parsed to
				// document
				doc = keyDefToDoc(definition);
				Element elem = doc.getDocumentElement();
				NamedNodeMap namedNodeMap = elem.getAttributes();
				// first resolve the keyref attribute
				if (withHref.contains(classValue)) {
					String target = keyMap.get(atts.getValue("keyref"));
					if (target != null) {
						String target_output = target;
						if (elem.getAttribute("scope").equals("")
								|| (!elem.getAttribute("scope").equals("") && elem
										.getAttribute("scope").equals("local"))) {
							if (new File(FileUtils.resolveFile(tempDir, target))
									.exists()) {
								target_output = FileUtils
										.getRelativePathFromMap(filepath,
												new File(tempDir, target)
														.getAbsolutePath());
								valid = true;
								output.write(Constants.STRING_BLANK);
								output.write(Constants.ATTRIBUTE_NAME_HREF);
								output.write("=\"");
								output.write(target_output);
								output.write("\"");
							} else {
								// referenced file does not exist
								Properties prop = new Properties();
								prop.setProperty("%1", atts.getValue("keyref"));
								prop.setProperty("%2", atts.getValue("xtrf"));
								prop.setProperty("%3", atts.getValue("xtrc"));
								javaLogger.logWarn(MessageUtils.getMessage(
										"DOTJ045W", prop).toString());
							}

						} else {
							// scope equals peer or external
							valid = true;
							output.write(Constants.ATTRIBUTE_NAME_HREF);
							output.write("=\"");
							output.write(target_output);
							output.write("\"");
						}

					} else {
						// key does not exist
						Properties prop = new Properties();
						prop.setProperty("%1", atts.getValue("keyref"));
						prop.setProperty("%2", atts.getValue("xtrf"));
						prop.setProperty("%3", atts.getValue("xtrc"));
						javaLogger.logWarn(MessageUtils.getMessage("DOTJ045W",
								prop).toString());
					}

				} else if (withOutHref.contains(classValue)) {
					String target = keyMap.get(atts.getValue("keyref"));
					if (elem.getAttribute("scope") == null
							|| (elem.getAttribute("scope") != null && elem
									.getAttribute("scope").equals("local"))) {
						if (target != null
								&& new File(FileUtils.resolveFile(filepath,
										target)).exists()) {
							valid = true;
						} else {
							Properties prop = new Properties();
							prop.setProperty("%1", atts.getValue("keyref"));
							prop.setProperty("%2", atts.getValue("xtrf"));
							prop.setProperty("%3", atts.getValue("xtrc"));
							javaLogger.logWarn(MessageUtils.getMessage(
									"DOTJ045W", prop).toString());
						}
					} else {
						// TODO
						// emit a warning, if the key reference element does not
						// carry an href attribute
						// but the scope in key definition does not equal local.
					}

				}
				
				validKeyref.push(valid);

				// copy attributes in key definition to key reference
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

				// output attributes which are not replaced in current element
				// in the help of aSet.
				for (int index = 0; index < atts.getLength(); index++) {
					if (!aset.contains(atts.getQName(index)) && atts.getQName(index) != Constants.ATTRIBUTE_NAME_CLASS) {
						output.append(Constants.STRING_BLANK);
						output.append(atts.getQName(index));
						output.write("=\"");
						output.write(atts.getValue(index));
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
		try {
			File inputFile = new File(tempDir, filename);
			filepath = inputFile.getAbsolutePath();
			File outputFile = new File(tempDir, filename + "keyref");
			output = new OutputStreamWriter(new FileOutputStream(outputFile));
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

	public void setTempDir(String tempDir) {
		this.tempDir = tempDir;
	}

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
	
	private String nodeToString(Element elem){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constants.LESS_THAN).append(elem.getNodeName());
		NamedNodeMap namedNodeMap = elem.getAttributes();
		for(int i=0; i<namedNodeMap.getLength(); i++){
			stringBuffer.append(Constants.STRING_BLANK).append(namedNodeMap.item(i).getNodeName()).append(Constants.EQUAL).append(Constants.QUOTATION+namedNodeMap.item(i).getNodeValue()+Constants.QUOTATION);
		}
		stringBuffer.append(Constants.GREATER_THAN);
		NodeList nodeList = elem.getChildNodes();
		for(int i=0; i<nodeList.getLength(); i++){
			Node node = nodeList.item(i);
			if(node.getNodeType() == Node.ELEMENT_NODE){
				// If the type of current node is ELEMENT_NODE, process current node.
				stringBuffer.append(nodeToString((Element)node));
			}
			if(node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
				stringBuffer.append("<?").append(node.getNodeName()).append("?>");
			}
			if(node.getNodeType() == Node.TEXT_NODE){
				stringBuffer.append(node.getNodeValue());
			}
		}
		stringBuffer.append("</").append(elem.getNodeName()).append(Constants.GREATER_THAN);
		return stringBuffer.toString();
	}
}
