/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author william
 *
 */
public class DelayConrefUtils {
	
	private Document root = null;
	
	private static DelayConrefUtils instance = null;
	/**
	 * Return the MergeUtils instance. Singleton.
	 * @return
	 */
	public static DelayConrefUtils getInstance(){
		if(instance == null){
			instance = new DelayConrefUtils();
		}
		return instance;
	}
	
	
	
	/**
	 * constructor
	 */
	public DelayConrefUtils() {
		super();
		root = null;
	}



	/**
	 * find whether an id is refer to a topic in a dita file
	 * @param absolutePathToFile
	 * @param id
	 * @return
	 */
	public boolean findTopicId(String absolutePathToFile, String id) {
		
		if(!FileUtils.fileExists(absolutePathToFile)){
			return false;
		}
		try {
			//load the file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			//factory.setFeature("http://xml.org/sax/features/validation", false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			try {
    			Class.forName(Constants.RESOLVER_CLASS);
    			builder.setEntityResolver(CatalogUtils.getCatalogResolver());
    		}catch (ClassNotFoundException e){
    			builder.setEntityResolver(null);
    		}
			Document root = builder.parse(new InputSource(new FileInputStream(absolutePathToFile)));
			
			//get root element
			Element doc = root.getDocumentElement();
			//do BFS
			Queue<Element> queue = new LinkedList<Element>();
			queue.offer(doc);
			while (!queue.isEmpty()) {
				Element pe = queue.poll();
				NodeList pchildrenList = pe.getChildNodes();
				for (int i = 0; i < pchildrenList.getLength(); i++) {
					Node node = pchildrenList.item(i);
					if (node.getNodeType() == Node.ELEMENT_NODE)
						queue.offer((Element)node);
				}
				String classValue = pe.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
				if(classValue!=null && classValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC)){
					//topic id found
					if(pe.getAttribute(Constants.ATTRIBUTE_NAME_ID).equals(id)){
						return true;
					}
				}
			}
			return false;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	/**check whether the keyref/id element has been exported. 
	 * @param href
	 * @param id
	 * @param key
	 */
	public List<Boolean> checkExport(String href, String id, String key, String tempDir) {
		//parsed export .xml to get exported elements
		String exportFile = (new File(tempDir, Constants.FILE_NAME_EXPORT_XML)).
		getAbsolutePath();
		
		boolean idExported = false;
		boolean keyrefExported = false;
		try {
			//load export.xml only once
			if(root==null){
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				//factory.setFeature("http://xml.org/sax/features/validation", false);
				DocumentBuilder builder = factory.newDocumentBuilder();
				try {
	    			Class.forName(Constants.RESOLVER_CLASS);
	    			builder.setEntityResolver(CatalogUtils.getCatalogResolver());
	    		}catch (ClassNotFoundException e){
	    			builder.setEntityResolver(null);
	    		}
				root = builder.parse(new InputSource(new FileInputStream(exportFile)));
			}
			//if dita file's extension name is ".xml"
			if(href.endsWith(Constants.FILE_EXTENSION_XML)){
				//change the extension to ".dita"
				href = href.replace(Constants.FILE_EXTENSION_XML, Constants.FILE_EXTENSION_DITA);
			}
			//get file node which contains the export node
			Element fileNode = searchForKey(root.getDocumentElement(), href, "file");
			if(fileNode!=null){
				//iterate the child nodes
				NodeList pList = fileNode.getChildNodes();
				for (int j = 0; j < pList.getLength(); j++) {
					Node node = pList.item(j);
					if(Node.ELEMENT_NODE == node.getNodeType()){
						Element child = (Element)node;
						//compare keys
						if(child.getNodeName().equals("keyref")&&
						   child.getAttribute(Constants.ATTRIBUTE_NAME_NAME)
						   .equals(key)){
							keyrefExported = true;
						//compare topic id
						}else if(child.getNodeName().equals("topicid")&&
							child.getAttribute(Constants.ATTRIBUTE_NAME_NAME)
							.equals(id)){
							idExported = true;
						//compare element id
						}else if(child.getNodeName().equals("id")&&
							child.getAttribute(Constants.ATTRIBUTE_NAME_NAME)
							.equals(id)){
							idExported = true;
						}
					}
					if(idExported && keyrefExported){
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<Boolean> list = new ArrayList<Boolean>();
		list.add(Boolean.valueOf(idExported));
		list.add(Boolean.valueOf(keyrefExported));
		return list;
	}
	/**
	 * Search specific element by key and tagName
	 * @param root
	 * @param key
	 * @param tagName
	 * @return
	 */
	public Element searchForKey(Element root, String key, String tagName) {
		if (root == null || StringUtils.isEmptyString(key)) return null;
		Queue<Element> queue = new LinkedList<Element>();
		queue.offer(root);
		
		while (!queue.isEmpty()) {
			Element pe = queue.poll();
			NodeList pchildrenList = pe.getChildNodes();
			for (int i = 0; i < pchildrenList.getLength(); i++) {
				Node node = pchildrenList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE)
					queue.offer((Element)node);
			}
			String value = pe.getNodeName();
			if(StringUtils.isEmptyString(value)||
				!value.equals(tagName)){
				continue;
			}
			
			value = pe.getAttribute(Constants.ATTRIBUTE_NAME_NAME);
			if (StringUtils.isEmptyString(value)) continue;
			
			if (value.equals(key)) return pe;
		}
		return null;
	}

}
