/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class extends SAX's DefaultHandler, used for parse index term from dita
 * files.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTermReader extends AbstractXMLReader {
	/** The target file under parsing */
	private String targetFile = null;

	/** The title of the topic under parsing */
	private String title = null;
	
	/** The title of the main topic */
	private String defaultTitle = null;

	/** Whether or not current element under parsing is a title element */
	private boolean inTitleElement = false;

	/** Whether or not current element under parsing is <index-sort-as> */
	private boolean insideSortingAs = false;
	
	/** Stack used to store index term */
	private Stack termStack = null;
	
	/** Stack used to store topic id */
	private Stack topicIdStack = null;

	/** List used to store all the specilized index terms */
	private List indexTermSpecList = null;
	
	/** List used to store all the specilized index-see */
	private List indexSeeSpecList = null;
	
	/** List used to store all the specilized index-see-also */
	private List indexSeeAlsoSpecList = null;
	
	/** List used to store all the specilized index-sort-as */
	private List indexSortAsSpecList = null;
	
	/** List used to store all the specilized topics */
	private List topicSpecList;
	
	/** List used to store all the indexterm found in this topic file */
	private List indexTermList;
	
	/** Map used to store the title info accessed by its topic id*/
	private Map titleMap;
	
	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

	/**
	 * Constructor
	 */
	public IndexTermReader() {
		termStack = new Stack();
		topicIdStack = new Stack();
		indexTermSpecList = new ArrayList(Constants.INT_256);
		indexSeeSpecList = new ArrayList(Constants.INT_256);
		indexSeeAlsoSpecList = new ArrayList(Constants.INT_256);
		indexSortAsSpecList = new ArrayList(Constants.INT_256);
		topicSpecList = new ArrayList(Constants.INT_256);
		indexTermList = new ArrayList(Constants.INT_256);
		titleMap = new HashMap(Constants.INT_256);
	}

	/**
	 * Reset the reader.
	 */
	public void reset() {
		targetFile = null;
		title = null;
		defaultTitle = null;
		inTitleElement = false;
		termStack.clear();
		topicIdStack.clear();
		indexTermSpecList.clear();
		indexSeeSpecList.clear();
		indexSeeAlsoSpecList.clear();
		indexSortAsSpecList.clear();
		topicSpecList.clear();
		indexTermList.clear();
		titleMap.clear();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String temp = new String(ch, start, length).trim();
		boolean withSpace = (ch[start] == '\n' || temp.startsWith(Constants.LINE_SEPARATOR));

		if (temp.length() == 0) {
			return;
		}
		
		/*
		 * For title info
		 */
		if (!insideSortingAs && !termStack.empty()) {
			IndexTerm indexTerm = (IndexTerm) termStack.peek();
			temp = StringUtils.restoreEntity(temp);
			indexTerm.setTermName(StringUtils.setOrAppend(indexTerm.getTermName(), temp, withSpace));
		} else if (insideSortingAs && temp.length() > 0) {
			IndexTerm indexTerm = (IndexTerm) termStack.peek();
			temp = StringUtils.restoreEntity(temp);
			indexTerm.setTermKey(StringUtils.setOrAppend(indexTerm.getTermKey(), temp, withSpace));
		} else if (inTitleElement) {
			temp = StringUtils.restoreEntity(temp);
			//Always append space if: <title>abc<ph/>df</title>
			title = StringUtils.setOrAppend(title, temp, true);
		}
	}
	
	/**
	 * @see org.dita.dost.reader.AbstractXMLReader#endDocument()
	 */
	public void endDocument() throws SAXException {
		int size = indexTermList.size();
		updateIndexTermTargetName();
		for(int i=0; i<size; i++){
			IndexTerm indexterm = (IndexTerm)indexTermList.get(i);
			IndexTermCollection.getInstantce().addTerm(indexterm);
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// Check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName)) {
			IndexTerm term = (IndexTerm) termStack.pop();

			if (term.getTermName() == null) {
				term.setTermName("***");
				javaLogger.logWarn(MessageUtils.getMessage("DOTJ014W").toString());				
			}
			
			if (term.getTermKey() == null) {
				term.setTermKey(term.getTermName());
			}
			
			if (termStack.empty()) {
				indexTermList.add(term);
			} else {
				IndexTerm parentTerm = (IndexTerm) termStack.peek();
				parentTerm.addSubTerm(term);
			}
		}

		// Check to see if the index-see or index-see-also or a specialized 
		// version is in the list.
		if (indexSeeSpecList.contains(localName)
				|| indexSeeAlsoSpecList.contains(localName)) {
			IndexTerm term = (IndexTerm) termStack.pop();
			IndexTerm parentTerm = (IndexTerm) termStack.peek();
			if (term.getTermKey() == null) {
				term.setTermKey(term.getTermFullName());
			}
			term.addTargets(parentTerm.getTargetList());
			parentTerm.addSubTerm(term);
		}
		
		/*
		 * For title info
		 */
		if (Constants.ELEMENT_NAME_TITLE.equals(localName)) {
			inTitleElement = false;
			if(!titleMap.containsKey(topicIdStack.peek())){
				//If this is the first topic title
				if(titleMap.size() == 0)
					defaultTitle = title;
				titleMap.put(topicIdStack.peek(), title);
			}
		}
		
		// For <index-sort-as>
		if (indexSortAsSpecList.contains(localName)) {
			insideSortingAs = false;
		}
		
		// For <topic>
		if (topicSpecList.contains(localName)){
			topicIdStack.pop();
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String classAttr = attributes.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		handleSpecialization(localName, classAttr);
		parseTopic(localName, attributes.getValue(Constants.ATTRIBUTE_NAME_ID));
		parseIndexTerm(localName);
		parseIndexSee(localName);
		parseIndexSeeAlso(localName);
		
		if (IndexTerm.getTermLocale() == null) {
			String xmlLang = attributes
					.getValue(Constants.ATTRIBUTE_NAME_XML_LANG);
			
			if (xmlLang != null) {
				Locale locale;
//				if (xmlLang.length() == 5) {
//					locale = new Locale(xmlLang.substring(0, 2).toLowerCase(),
//							xmlLang.substring(3, 5).toUpperCase());
//				} else {
//					locale = new Locale(xmlLang.substring(0, 2).toLowerCase());
//				}
				IndexTerm.setTermLocale(StringUtils.getLocale(xmlLang));
			}
		}

		/*
		 * For title info
		 */
		if (Constants.ELEMENT_NAME_TITLE.equals(localName)
				&& !titleMap.containsKey(topicIdStack.peek())) {
			inTitleElement = true;
			title = null;
		}
		
		// For <index-sort-as>
		if (indexSortAsSpecList.contains(localName)) {
			insideSortingAs = true;
		}
	}
	
	private void parseTopic(String localName, String id){
		if (topicSpecList.contains(localName)){
			topicIdStack.push(id);
		}
	}

	private void parseIndexSeeAlso(String localName) {
		// check to see it the index-see-also element or a specialized version
		// is in the list.
		if (indexSeeAlsoSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTerm parentTerm = null;
			if(!termStack.isEmpty()){
				parentTerm = (IndexTerm)termStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
				}
			}
			indexTerm.setTermPrefix("See also");
			termStack.push(indexTerm);
		}
	}

	private void parseIndexSee(String localName) {
		// check to see it the index-see element or a specialized version is
		// in the list.
		if (indexSeeSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTerm parentTerm = null;
			
			indexTerm.setTermPrefix("See");
			
			if(!termStack.isEmpty()){
				parentTerm = (IndexTerm)termStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
					indexTerm.setTermPrefix("See also");
				}
			}
			termStack.push(indexTerm);
		}
	}

	private void parseIndexTerm(String localName) {
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTermTarget target = new IndexTermTarget();
			String fragment = null;
			
			IndexTerm parentTerm = null;
			if(!termStack.isEmpty()){
				parentTerm = (IndexTerm)termStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
				}
			}
			
			if(topicIdStack.peek() == null){
				fragment = null;
			}else{
				fragment = topicIdStack.peek().toString();
			}

			if (title != null) {
				target.setTargetName(title);
			} else {
				target.setTargetName(targetFile);
			}
			if(fragment != null)
				target.setTargetURI(targetFile + Constants.SHARP + fragment);
			else
				target.setTargetURI(targetFile);
			indexTerm.addTarget(target);
			termStack.push(indexTerm);
		}
	}

	/**
	 * Note: <index-see-also> should be handled before <index-see>.
	 * 
	 * @param localName
	 * @param classAttr
	 */
	private void handleSpecialization(String localName, String classAttr) {
		if (classAttr == null) {
			return;
		} else if (classAttr.indexOf(Constants.ELEMENT_NAME_INDEXTERM) != -1) {
			// add the element name to the indexterm specialization element
			// list if it does not already exist in that list.
			if (!indexTermSpecList.contains(localName)) {
				indexTermSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ELEMENT_NAME_INDEXSEEALSO) != -1) {
			// add the element name to the index-see-also specialization element
			// list if it does not already exist in that list.
			if (!indexSeeAlsoSpecList.contains(localName)) {
				indexSeeAlsoSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ELEMENT_NAME_INDEXSEE) != -1) {
			// add the element name to the index-see specialization element
			// list if it does not already exist in that list.
			if (!indexSeeSpecList.contains(localName)) {
				indexSeeSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ELEMENT_NAME_INDEXSORTAS) != -1) {
			// add the element name to the index-sort-as specialization element
			// list if it does not already exist in that list.
			if (!indexSortAsSpecList.contains(localName)) {
				indexSortAsSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC) != -1) {
			//add the element name to the topic specialization element
			// list if it does not already exist in that list.
			if (!topicSpecList.contains(localName)) {
				topicSpecList.add(localName);
			}
		}
	}

	/**
	 * Set the current parsing file
	 * 
	 * @param target
	 *            The parsingFile to set.
	 */
	public void setTargetFile(String target) {
		this.targetFile = target;
	}
	
	/**
	 * Update the target name of constructed IndexTerm recursively
	 *
	 */
	private void updateIndexTermTargetName(){
		int size = indexTermList.size();
		if(defaultTitle == null){
			defaultTitle = targetFile;
		}
		for(int i=0; i<size; i++){
			IndexTerm indexterm = (IndexTerm)indexTermList.get(i);
			updateIndexTermTargetName(indexterm);
		}
	}
	
	/**
	 * Update the target name of each IndexTerm, recursively
	 * @param indexterm
	 */
	private void updateIndexTermTargetName(IndexTerm indexterm){
		int targetSize = indexterm.getTargetList().size();
		int subtermSize = indexterm.getSubTerms().size();
		
		for(int i=0; i<targetSize; i++){
			IndexTermTarget target = (IndexTermTarget)indexterm.getTargetList().get(i);
			String uri = target.getTargetURI();
			int indexOfSharp = uri.lastIndexOf(Constants.SHARP);
			String fragment = (indexOfSharp == -1 || uri.endsWith(Constants.SHARP))?
								null:
								uri.substring(indexOfSharp+1);
			if(fragment != null && titleMap.containsKey(fragment)){
				target.setTargetName(titleMap.get(fragment).toString());
			}else{
				target.setTargetName(defaultTitle);
			}
		}
		
		for(int i=0; i<subtermSize; i++){
			IndexTerm subterm = (IndexTerm)indexterm.getSubTerms().get(i);
			updateIndexTermTargetName(subterm);
		}
	}
	
}
