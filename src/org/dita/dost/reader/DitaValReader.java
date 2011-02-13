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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * DitaValReader reads and parses the information from ditaval file which
 * contains the information of filtering and flagging.
 * 
 * @author Zhang, Yuan Peng
 */
public class DitaValReader extends AbstractXMLReader {
	private HashMap<String, String> filterMap;
	
	private HashMap<String, String> schemeFilterMap;

	private ContentImpl content;

	private XMLReader reader;

	private DITAOTJavaLogger logger;

	private List<String> imageList = null;

	private String ditaVal = null;
	
	private List<String> relFlagImageList= null;
	
	private HashMap<String, HashMap<String, HashSet<Element>>> bindingMap = null;
	
	private HashMap<String, HashMap<String, HashSet<String>>> validValuesMap = null;
	
	private HashMap<String, HashMap<String, String>> defaultValueMap = null;
	
	private Element schemeRoot = null;
	
	//Added on 2010-08-24 for bug:3086552 start
	private static boolean setSystemid = true;
	//Added on 2010-08-24 for bug:3086552 end
	
	/**
	 * Default constructor of DitaValReader class.
	 */
	public DitaValReader() {
		super();
		filterMap = new HashMap<String, String>();
		schemeFilterMap = new HashMap<String, String>();
		content = null;
		logger = new DITAOTJavaLogger();
		imageList = new ArrayList<String>(Constants.INT_256);
		relFlagImageList= new ArrayList<String>(Constants.INT_256);
		validValuesMap = new HashMap<String, HashMap<String, HashSet<String>>>();
		defaultValueMap = new HashMap<String, HashMap<String, String>>();
		bindingMap = new HashMap<String, HashMap<String, HashSet<Element>>>();
		
		try {
			reader = StringUtils.getXMLReader();
			reader.setContentHandler(this);
		} catch (Exception e) {
			logger.logException(e);
		}
		
	}
	//Added on 2010-08-24 for bug:3086552 start
	public static void initXMLReader(boolean arg_setSystemid) {
		setSystemid= arg_setSystemid;
	}
	//Added on 2010-08-24 for bug:3086552 end
	/**
	 * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
	 */
	public void read(String input) {
		ditaVal = input;

		try {
			
			reader.setErrorHandler(new DITAOTXMLErrorHandler(ditaVal));
			//Added on 2010-08-24 for bug:3086552 start
			File file = new File(input);
			InputSource is = new InputSource(new FileInputStream(file));
			//Set the system ID
			if(setSystemid)
				//is.setSystemId(URLUtil.correct(file).toString());
				is.setSystemId(file.toURI().toURL().toString());
			//Added on 2010-08-24 for bug:3086552 end
			reader.parse(is); 
			
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/**
	 * @see org.dita.dost.reader.AbstractReader#getContent()
	 */
	public Content getContent() {
		content = new ContentImpl();
		content.setCollection(filterMap.entrySet());
		return content;
	}
	

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 * 
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String flagImage = null;
		if(atts.getValue(Constants.ATTRIBUTE_NAME_IMG)!=null){
			flagImage = atts.getValue(Constants.ATTRIBUTE_NAME_IMG);
		}else if(atts.getValue(Constants.ATTRIBUTE_NAME_IMAGEREF)!=null){
			flagImage = atts.getValue(Constants.ATTRIBUTE_NAME_IMAGEREF);
		}

		if (Constants.ELEMENT_NAME_PROP.equals(qName)) {
			String action = atts.getValue(Constants.ELEMENT_NAME_ACTION);
			String attName = atts.getValue(Constants.ATTRIBUTE_NAME_ATT);
			String attValue = atts.getValue(Constants.ATTRIBUTE_NAME_VAL);
			//first to check if the att attribute and val attribute are null 
			//which is a default action for elements without mapping with the other filter val
			String key=null;
			if(attName==null){
				key=Constants.DEFAULT_ACTION;
			}else 
				if(attValue==null){
					key=attName;//default action for the specified attribute
				}
				else{
					key = attName + Constants.EQUAL + attValue;
				}

			if (action != null) {
				insertAction(action, key);
			}
			
			if (attName != null && attValue != null && bindingMap != null && !bindingMap.isEmpty()) {
				HashMap<String, HashSet<Element>> schemeMap = bindingMap.get(attName);
				if (schemeMap != null && !schemeMap.isEmpty()) {
					Iterator<HashSet<Element>> subTreeIter = schemeMap.values().iterator();
					while (subTreeIter.hasNext()) {
						Iterator<Element> subTreeSet = subTreeIter.next().iterator();
						while (subTreeSet.hasNext()) {
							Element subRoot = this.searchForKey(subTreeSet.next(), attValue);
							if (subRoot != null && action != null) {
								this.insertAction(subRoot, attName, action);
							}
						}
					}
				}
			}
		}

		/*
		 * Parse image files for flagging
		 */
		if (flagImage != null && !"".equals(flagImage.trim())) {
			String filterDir;
			if (new File(flagImage).isAbsolute()) {
				imageList.add(flagImage);
				relFlagImageList.add(FileUtils.getRelativePathFromMap(ditaVal, flagImage));
				return;
			}

			// img is a relative path to the .ditaval file
			filterDir = new File(new File(ditaVal).getAbsolutePath())
					.getParent();
			imageList.add(new File(filterDir, flagImage).getAbsolutePath());
			relFlagImageList.add(flagImage);
		}
	}

	/**
	 * Insert action into filetermap if key not present in the map
	 * @param action
	 * @param key
	 */
	private void insertAction(String action, String key) {
		if (filterMap.get(key) == null) {
			filterMap.put(key, action);
		} else {
			Properties prop = new Properties();
			prop.put("%1", key);
			logger.logError(MessageUtils.getMessage("DOTJ007E", prop)
					.toString());
		}
	}
	
	private void insertAction(Element subTree, String attName, String action) {
		if (subTree == null || action == null) return;
		
		LinkedList<Element> queue = new LinkedList<Element>();
		
		// Skip the sub-tree root because it has been added already.
		NodeList children = subTree.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
				queue.offer((Element)children.item(i));
		}
		
		while (!queue.isEmpty()) {
			Element node = queue.poll();
			children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
					queue.offer((Element)children.item(i));
			}
			String attrValue = node.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
			if (attrValue != null && attrValue.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_DEF)) {
				String key = node.getAttribute(Constants.ATTRIBUTE_NAME_KEYS);
				if (!StringUtils.isEmptyString(key)) {
					key = attName + Constants.EQUAL + key;
					if (schemeFilterMap.get(key) == null) {
						schemeFilterMap.put(key, action);
					} 
//					else {
//						Properties prop = new Properties();
//						prop.put("%1", key);
//						logger.logError(MessageUtils.getMessage("DOTJ007E", prop)
//								.toString());
//					}
				}
			}
		}
	}

	/**
	 * Return the image list.
	 * @return image list
	 */
	public List<String> getImageList() {
		return imageList;
	}
	
	/**
	 * Return the filter map.
	 * @return filter map
	 */
	public HashMap<String, String> getFilterMap() {
		schemeFilterMap.putAll(filterMap);
		return schemeFilterMap;
	}
	/**
	 * reset.
	 */
	public void reset() {
		schemeFilterMap.clear();
		validValuesMap.clear();
		defaultValueMap.clear();
	}
	/**
	 * reset filter map.
	 */
	public void filterReset() {
		filterMap.clear();
	}
	/**
	 * get image list relative to the .ditaval file.
	 * @return image list
	 */
	public List<String> getRelFlagImageList(){
		return relFlagImageList;
	}
	/**
	 * load schema file.
	 * @param scheme scheme file
	 */
	public void loadSubjectScheme(String scheme) {
		
		if (!FileUtils.fileExists(scheme)) {
			return;
		}
		
		//schemeFilterMap.clear();
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new FileInputStream(
					new File(scheme))));
			schemeRoot = doc.getDocumentElement();
			if (schemeRoot == null) return;
			NodeList rootChildren = schemeRoot.getChildNodes();
			for (int i = 0; i < rootChildren.getLength(); i++) {
				if (rootChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
					Element node = (Element)rootChildren.item(i);
					String attrValue = node.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
					if (attrValue != null
							&& attrValue.contains(Constants.ATTR_CLASS_VALUE_ENUMERATION_DEF)) {
						NodeList enumChildren = node.getChildNodes();
						String elementName = "*";
						String attributeName = null;
						for (int j = 0; j < enumChildren.getLength(); j++) {
							if (enumChildren.item(j).getNodeType() == Node.ELEMENT_NODE) {
								node = (Element)enumChildren.item(j);
								attrValue = node.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
								if (attrValue != null 
										&& attrValue.contains(Constants.ATTR_CLASS_VALUE_ELEMENT_DEF)) {
									elementName = node.getAttribute(Constants.ATTRIBUTE_NAME_NAME);
								} else if (attrValue != null
										&& attrValue.contains(Constants.ATTR_CLASS_VALUE_ATTRIBUTE_DEF)) {
									attributeName = node.getAttribute(Constants.ATTRIBUTE_NAME_NAME);
									HashMap<String, HashSet<Element>> S = bindingMap.get(attributeName);
									if (S == null) {
										S = new HashMap<String, HashSet<Element>>();
										bindingMap.put(attributeName, S);
									}
								} else if (attrValue != null
										&& attrValue.contains(Constants.ATTR_CLASS_VALUE_DEFAULT_SUBJECT)) {
									// Put default values.
									String keyValue = node.getAttribute(Constants.ATTRIBUTE_NAME_KEYREF);
									if (keyValue != null) {
										HashMap<String, String> S = defaultValueMap.get(attributeName);
										if (S == null) {
											S = new HashMap<String, String>();
										}
										S.put(elementName, keyValue);
										defaultValueMap.put(attributeName, S);
									}
								} else if (attrValue != null
										&& attrValue.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_DEF)) {
									// Search for attributeName in schemeRoot
									String keyValue = node
											.getAttribute(Constants.ATTRIBUTE_NAME_KEYREF);
									if (StringUtils.isEmptyString(keyValue))
										keyValue = node
												.getAttribute(Constants.ATTRIBUTE_NAME_KEYS);
									Element subTree = searchForKey(schemeRoot,
											keyValue);
									if (subTree != null) {
										HashMap<String, HashSet<Element>> S = bindingMap
												.get(attributeName);
										if (S == null) {
											S = new HashMap<String, HashSet<Element>>();
										}
										HashSet<Element> A = S.get(elementName);
										if (A == null)
											A = new HashSet<Element>();
										if (!A.contains(subTree)) {
											// Add sub-tree to valid values map
											this.putValuePairsIntoMap(subTree, elementName, attributeName);
										}
										A.add(subTree);
										S.put(elementName, A);
										bindingMap.put(attributeName, S);
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			this.logger.logException(e);
		}
	}
	
	private void putValuePairsIntoMap(Element subtree, String elementName, String attName) {
		if (subtree == null || attName == null) return;
		
		HashMap<String, HashSet<String>> valueMap = this.validValuesMap.get(attName);
		if (valueMap == null)
			valueMap = new HashMap<String, HashSet<String>>();
		
		HashSet<String> valueSet = valueMap.get(elementName);
		if (valueSet == null) 
			valueSet = new HashSet<String>();
		
		LinkedList<Element> queue = new LinkedList<Element>();
		queue.offer(subtree);
		
		while (!queue.isEmpty()) {
			Element node = queue.poll();
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
					queue.offer((Element)children.item(i));
			}
			String attrValue = node.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
			if (attrValue != null && attrValue.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_DEF)) {
				String key = node.getAttribute(Constants.ATTRIBUTE_NAME_KEYS);
				if (!StringUtils.isEmptyString(key)) {
					valueSet.add(key);
				}
			}
		}
		valueMap.put(elementName, valueSet);
		this.validValuesMap.put(attName, valueMap);
	}
    
	private Element searchForKey(Element root, String keyValue) {
		if (root == null || keyValue == null) return null;
		LinkedList<Element> queue = new LinkedList<Element>();
		queue.add(root);
		while (!queue.isEmpty()) {
			Element node = queue.removeFirst();
			NodeList children = node.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				if (children.item(i).getNodeType() == Node.ELEMENT_NODE)
					queue.add((Element)children.item(i));
			}
			String attrValue = node.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
			if (attrValue != null && attrValue.contains(Constants.ATTR_CLASS_VALUE_SUBJECT_DEF)) {
				String key = node.getAttribute(Constants.ATTRIBUTE_NAME_KEYS);
				if (keyValue.equals(key)) {
					return node;
				}
			}
		}
		return null;
	}

	/**
	 * @return the validValuesMap
	 */
	public HashMap<String, HashMap<String,HashSet<String>>> getValidValuesMap() {
		return validValuesMap;
	}
	/**
	 * get map of default value.
	 * @return default value map
	 */
	public HashMap<String, HashMap<String, String>> getDefaultValueMap() {
		return this.defaultValueMap;
	}

}
