/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MapMetaReader implements AbstractReader {
	private static final String INTERNET_LINK_MARK = "://";
	
	private static Hashtable resultTable = null;
	
	private static HashSet uniqueSet;
	
	static{
		uniqueSet = new HashSet(Constants.INT_16);
		uniqueSet.add(Constants.ATTR_CLASS_VALUE_CRITDATES);
		uniqueSet.add(Constants.ATTR_CLASS_VALUE_PERMISSIONS);
		uniqueSet.add(Constants.ATTR_CLASS_VALUE_PUBLISHER);
		uniqueSet.add(Constants.ATTR_CLASS_VALUE_SOURCE);
		uniqueSet.add(Constants.ATTR_CLASS_VALUE_MAP_SEARCHTITLE);
	}
	
	private static HashSet cascadeSet;
	
	static{
		cascadeSet = new HashSet(Constants.INT_16);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_AUDIENCE);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_AUTHOR);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_CATEGORY);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_COPYRIGHT);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_CRITDATES);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_PERMISSIONS);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_PRODINFO);
		cascadeSet.add(Constants.ATTR_CLASS_VALUE_PUBLISHER);
	}
	
	private static HashSet metaSet;
	
	static{
		metaSet = new HashSet(Constants.INT_16);
		metaSet.add(Constants.ATTR_CLASS_VALUE_AUDIENCE);
		metaSet.add(Constants.ATTR_CLASS_VALUE_AUTHOR);
		metaSet.add(Constants.ATTR_CLASS_VALUE_CATEGORY);
		metaSet.add(Constants.ATTR_CLASS_VALUE_COPYRIGHT);
		metaSet.add(Constants.ATTR_CLASS_VALUE_CRITDATES);
		metaSet.add(Constants.ATTR_CLASS_VALUE_DATA);
		metaSet.add(Constants.ATTR_CLASS_VALUE_DATAABOUT);
		metaSet.add(Constants.ATTR_CLASS_VALUE_FOREIGN);
		metaSet.add(Constants.ATTR_CLASS_VALUE_KEYWORDS);
		metaSet.add(Constants.ATTR_CLASS_VALUE_MAP_SEARCHTITLE);
		metaSet.add(Constants.ATTR_CLASS_VALUE_OTHERMETA);		
		metaSet.add(Constants.ATTR_CLASS_VALUE_PERMISSIONS);
		metaSet.add(Constants.ATTR_CLASS_VALUE_PRODINFO);
		metaSet.add(Constants.ATTR_CLASS_VALUE_PUBLISHER);
		metaSet.add(Constants.ATTR_CLASS_VALUE_RESOURCEID);
		metaSet.add(Constants.ATTR_CLASS_VALUE_SOURCE);
		metaSet.add(Constants.ATTR_CLASS_VALUE_UNKNOWN);
	}

	private DITAOTJavaLogger javaLogger = null;
	
	private Hashtable globalMeta = null;
	
	private Document doc = null;
	
	private String filePath = null;
	
	
	
	public MapMetaReader() {
		super();
		javaLogger = new DITAOTJavaLogger();
		globalMeta = new Hashtable(Constants.INT_16);
        resultTable = new Hashtable(Constants.INT_16);
	}

	public void read(String filename) {
		// TODO Auto-generated method stub
		File inputFile = new File(filename);
        filePath = inputFile.getParent();
        String filePathName = inputFile.getPath();
        
        //clear the history on global metadata table
        globalMeta.clear();
        
        
        try{
        	DocumentBuilderFactory factory = DocumentBuilderFactory
			.newInstance();
        	DocumentBuilder builder = factory.newDocumentBuilder();
        	doc = builder.parse(inputFile);
        	
        	Element root = doc.getDocumentElement();
        	NodeList list = root.getChildNodes();
        	for (int i = 0; i < list.getLength(); i++){
        		Node node = list.item(i);
        		Node classAttr = null;
        		if (node.getNodeType()==node.ELEMENT_NODE){
        			classAttr = node.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_CLASS);
        		}
        		if(classAttr != null && 
        				classAttr.getNodeValue().indexOf(Constants.ATTR_CLASS_VALUE_TOPICMETA) != -1){
        			//if this node is topicmeta node under root
        			handleGlobalMeta(node);
        		}else if(classAttr != null &&
        				classAttr.getNodeValue().indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF) != -1){
        			//if this node is topicref node under root
        			handleTopicref(node, globalMeta);
        		}
        			
        	}
        }catch (Exception e){
        	javaLogger.logException(e);
        }
	}

	private void handleTopicref(Node topicref, Hashtable inheritance) {
		Node hrefAttr = topicref.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_HREF);
		Node scopeAttr = topicref.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_SCOPE);
    	Node formatAttr = topicref.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_FORMAT);
		Hashtable current = mergeMeta(null,inheritance,cascadeSet);
		String topicPath = null;
    	
    	NodeList children = topicref.getChildNodes();
		for (int i = 0; i < children.getLength(); i++){
			Node node = children.item(i);
    		Node classAttr = null;
    		if(node.getNodeType()==node.ELEMENT_NODE){
    			classAttr = node.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_CLASS);
    		}
    		 
    		if(classAttr != null && hrefAttr != null &&
    				classAttr.getNodeValue().indexOf(Constants.ATTR_CLASS_VALUE_TOPICMETA) != -1 &&
    				hrefAttr != null && hrefAttr.getNodeValue().indexOf(INTERNET_LINK_MARK) == -1
            		&& (scopeAttr == null || Constants.ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeAttr.getNodeValue()))
            		&& (formatAttr == null || Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatAttr.getNodeValue()))
    				){
    			//if this node is topicmeta and the parent topicref refers to a valid dita topic
    			current = handleMeta(node, inheritance);
    			
    		}else if(classAttr != null &&
    				classAttr.getNodeValue().indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF) != -1){
    			//if this node is topicref node under topicref
    			handleTopicref(node, current);
    		}
		}
		
		if (!current.isEmpty() && hrefAttr != null){// prevent the metadata is empty
			topicPath = FileUtils.resolveTopic(filePath,hrefAttr.getNodeValue());
			if(resultTable.containsKey(topicPath)){
    			//if the result table already contains some result
    			//metadata for current topic path.
				Hashtable previous = (Hashtable)resultTable.get(topicPath);
				resultTable.put(topicPath, mergeMeta(previous, current, metaSet));
    		}else{
    			resultTable.put(topicPath, current);
    		}    		
		}
		
	}


	private Hashtable handleMeta(Node meta, Hashtable inheritance) {
		
		Hashtable topicMetaTable = new Hashtable(Constants.INT_16);
		
		NodeList children = meta.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			Node node = children.item(i);
			Node attr = null;
			if(node.getNodeType()==node.ELEMENT_NODE){
				attr = node.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_CLASS);
			}
			if (attr != null){
				String attrValue = attr.getNodeValue();
				// int number 1 to remove the first "-" or "+" character in class attribute
				String metaKey = attrValue.substring(1,
						attrValue.indexOf(Constants.STRING_BLANK,attrValue.indexOf(Constants.SLASH))+1 );
				if(topicMetaTable.containsKey(metaKey)){
					//append node to the list if it exist in topic meta table
					//use clone here to prevent the node is removed from original DOM tree;
					((Element) topicMetaTable.get(metaKey)).appendChild(node.cloneNode(true));				
				} else{
					Element stub = doc.createElement("stub");
					// use clone here to prevent the node is removed from original DOM tree;
					stub.appendChild(node.cloneNode(true));
					topicMetaTable.put(metaKey, stub);
				}
			}
		}
		
		return mergeMeta(topicMetaTable, inheritance, cascadeSet);		
		
	}

	private Hashtable mergeMeta(Hashtable topicMetaTable, Hashtable inheritance, HashSet enableSet) {
		
		// When inherited metadata need to be merged into current metadata
		// enableSet should be cascadeSet so that only metadata that can
		// be inherited are merged.
		// Otherwise enableSet should be metaSet in order to merge all
		// metadata.
		if (topicMetaTable == null){
			topicMetaTable = new Hashtable(Constants.INT_16);
		}
		Node item = null;
		Iterator iter = enableSet.iterator();
		while (iter.hasNext()){
			String key = (String)iter.next();
			if (inheritance.containsKey(key)){
				if(uniqueSet.contains(key) ){
					if(!topicMetaTable.containsKey(key)){
						topicMetaTable.put(key, inheritance.get(key));
					}					
					
				}else{  // not unique metadata
					
					if(!topicMetaTable.containsKey(key)){
						topicMetaTable.put(key, inheritance.get(key));
					}else{
						//not necessary to do node type check here
						//because inheritStub doesn't contains any node
						//other than Element.
						Node stub = (Node) topicMetaTable.get(key);
						Node inheritStub = (Node) inheritance.get(key);
						NodeList children = inheritStub.getChildNodes();
						for(int i = 0; i < children.getLength(); i++){
							item = children.item(i).cloneNode(true);
							item = stub.getOwnerDocument().importNode(item,true);
							stub.appendChild(item);
						}
						
						topicMetaTable.put(key, stub);
					}
				}
			}
		}
		return topicMetaTable;
	}

	private void handleGlobalMeta(Node metadata) {
		
		NodeList children = metadata.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			Node node = children.item(i);
			Node attr = null;
			if (node.getNodeType() == Node.ELEMENT_NODE){
				attr = node.getAttributes().getNamedItem(Constants.ATTRIBUTE_NAME_CLASS);
			}
			if (attr != null){
				String attrValue = attr.getNodeValue();
				String metaKey = attrValue.substring(1,
						attrValue.indexOf(Constants.STRING_BLANK,attrValue.indexOf(Constants.SLASH))+1 );
				if(cascadeSet.contains(metaKey) && globalMeta.containsKey(metaKey)){
					//append node to the list if it exist in global meta table
					//use clone here to prevent the node is removed from original DOM tree;
					((Element) globalMeta.get(metaKey)).appendChild(node.cloneNode(true));
				} else if(cascadeSet.contains(metaKey)){
					Element stub = doc.createElement("stub");
					stub.appendChild(node.cloneNode(true));
					globalMeta.put(metaKey, stub);
				}
			}
		}
		
	}

	public Content getContent() {
		ContentImpl result = new ContentImpl();
        result.setCollection( resultTable.entrySet());
        return result;
	}
	

}
