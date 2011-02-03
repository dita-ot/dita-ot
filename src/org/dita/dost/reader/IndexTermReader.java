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
	private Stack<IndexTerm> termStack = null;
	
	/** Stack used to store topic id */
	private Stack<String> topicIdStack = null;

	/** List used to store all the specialized index terms */
	private List<String> indexTermSpecList = null;
	
	/** List used to store all the specialized index-see */
	private List<String> indexSeeSpecList = null;
	
	/** List used to store all the specialized index-see-also */
	private List<String> indexSeeAlsoSpecList = null;
	
	/** List used to store all the specialized index-sort-as */
	private List<String> indexSortAsSpecList = null;
	
	/** List used to store all the specialized topics */
	private List<String> topicSpecList;
	
	/** List used to store all specialized titles */
	private List<String> titleSpecList;
	
	/** List used to store all the indexterm found in this topic file */
	private List<IndexTerm> indexTermList;
	
	/** Map used to store the title info accessed by its topic id*/
	private Map<String, String> titleMap;
	
	/** Stack for "@processing-role" value */
	private Stack<String> processRoleStack;
	
	/** Depth inside a "@processing-role" parent */
    private int processRoleLevel = 0;
    
	private DITAOTJavaLogger javaLogger = null;
	
	//Added by William on 2010-04-26 for ref:2990783 start
	private IndexTermCollection result;
	//Added by William on 2010-04-26 for ref:2990783 end
	
	//Added by William on 2010-04-26 for ref:2990783 start
	public IndexTermReader(IndexTermCollection result) {
 		this();
 		this.result = result;
	}
	//Added by William on 2010-04-26 for ref:2990783 end
	
	/**
	 * Constructor.
	 * 
	 * @deprecated use {@link #IndexTermReader(IndexTermCollection)} instead
	 */
	@Deprecated
	public IndexTermReader() {
		termStack = new Stack<IndexTerm>();
		topicIdStack = new Stack<String>();
		indexTermSpecList = new ArrayList<String>(Constants.INT_16);
		indexSeeSpecList = new ArrayList<String>(Constants.INT_16);
		indexSeeAlsoSpecList = new ArrayList<String>(Constants.INT_16);
		indexSortAsSpecList = new ArrayList<String>(Constants.INT_16);
		topicSpecList = new ArrayList<String>(Constants.INT_16);
		titleSpecList = new ArrayList<String>(Constants.INT_16);
		indexTermList = new ArrayList<IndexTerm>(Constants.INT_256);		
		titleMap = new HashMap<String, String>(Constants.INT_256);
		processRoleStack = new Stack<String>();
		processRoleLevel = 0;
		javaLogger = new DITAOTJavaLogger();
		if (result == null) {
		    result = IndexTermCollection.getInstantce();
		}
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
		processRoleStack.clear();
		processRoleLevel = 0;
		titleMap.clear();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		StringBuilder tempBuf = new StringBuilder(length);
		tempBuf.append(ch, start, length);
		normalizeAndCollapseWhitespace(tempBuf);
		String temp = tempBuf.toString();
		
		/*
		 * For title info
		 */
		if (processRoleStack.isEmpty() || 
				!Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processRoleStack.peek())) {
			if (!insideSortingAs && !termStack.empty()) {
				IndexTerm indexTerm = (IndexTerm) termStack.peek();
				temp = StringUtils.escapeXML(temp);
				temp = trimSpaceAtStart(temp, indexTerm.getTermName());
				indexTerm.setTermName(StringUtils.setOrAppend(indexTerm.getTermName(), temp, false));
			} else if (insideSortingAs && temp.length() > 0) {
				IndexTerm indexTerm = (IndexTerm) termStack.peek();
				temp = StringUtils.escapeXML(temp);
				temp = trimSpaceAtStart(temp, indexTerm.getTermKey());
				indexTerm.setTermKey(StringUtils.setOrAppend(indexTerm.getTermKey(), temp, false));
			} else if (inTitleElement) {
				temp = StringUtils.escapeXML(temp);
				temp = trimSpaceAtStart(temp, title);
				//Always append space if: <title>abc<ph/>df</title>
				//Updated with SF 2010062 - should only add space if one is in source
				title = StringUtils.setOrAppend(title, temp, false);
			}
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
			//IndexTermCollection.getInstantce().addTerm(indexterm);
			//Added by William on 2010-04-26 for ref:2990783 start
			result.addTerm(indexterm);
			//Added by William on 2010-04-26 for ref:2990783 end
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
		//Skip the topic if @processing-role="resource-only"
		if (processRoleLevel > 0) {
			String role = processRoleStack.peek();
			if (processRoleLevel == processRoleStack.size()) {
				role = processRoleStack.pop();
			}
			processRoleLevel--;
			if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY
					.equalsIgnoreCase(role)) {
				return;
			}
		}
		
		// Check to see if the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName)) {
			IndexTerm term = (IndexTerm) termStack.pop();
			//SF Bug 2010062: Also set to *** when the term is only white-space.
			if (term.getTermName() == null || term.getTermName().trim().equals("")){
				if(term.getEndAttribute() != null && !term.hasSubTerms()){
					return;
				} else{
					term.setTermName("***");
					javaLogger.logWarn(MessageUtils.getMessage("DOTJ014W").toString());				
				}
			}
			
			if (term.getTermKey() == null) {
				term.setTermKey(term.getTermName());
			}
			
			//if this term is the leaf term
			//leaf means the current indexterm element doesn't contains any subterms
			//or only has "index-see" or "index-see-also" subterms.
			if (term.isLeaf()){
				//generate a target which points to current topic and
				//assign it to current term.
				IndexTermTarget target = genTarget();
				term.addTarget(target);
			}
				
			if (termStack.empty()) {
				//most parent indexterm
				indexTermList.add(term);
			} else {
				//Assign parent indexterm to 
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
			//term.addTargets(parentTerm.getTargetList());
			term.addTarget(genTarget()); //assign current topic as the target of index-see or index-see-also term
			parentTerm.addSubTerm(term);
		}
		
		/*
		 * For title info
		 */
		if (titleSpecList.contains(localName)) {
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
	 * This method is used to create a target which refers to current topic.
	 * @return instance of IndexTermTarget created
	 */
	private IndexTermTarget genTarget() {
		IndexTermTarget target = new IndexTermTarget();
		String fragment = null;
		
		if(topicIdStack.peek() == null){
			fragment = null;
		}else{
			fragment = topicIdStack.peek();
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
		return target;
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		
		//Skip the topic if @processing-role="resource-only"
		String attrValue = attributes
				.getValue(Constants.ATTRIBUTE_NAME_PROCESSING_ROLE);
		if (attrValue != null) {
			processRoleStack.push(attrValue);
			processRoleLevel++;
			if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY
					.equals(attrValue)) {
				return;
			}
		} else if (processRoleLevel > 0) {
			processRoleLevel++;
			if (Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY
					.equals(processRoleStack.peek())) {
				return;
			}
		}
		
		String classAttr = attributes.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		handleSpecialization(localName, classAttr);
		parseTopic(localName, attributes.getValue(Constants.ATTRIBUTE_NAME_ID));
		//change parseIndexTerm(localName) to parseIndexTerm(localName,attributes)
		parseIndexTerm(localName,attributes);
		parseIndexSee(localName);
		parseIndexSeeAlso(localName);
		
		if (IndexTerm.getTermLocale() == null) {
			String xmlLang = attributes
					.getValue(Constants.ATTRIBUTE_NAME_XML_LANG);
			
			if (xmlLang != null) {
				IndexTerm.setTermLocale(StringUtils.getLocale(xmlLang));
			}
		}

		/*
		 * For title info
		 */
		if (titleSpecList.contains(localName)
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
			indexTerm.setTermPrefix(Constants.IndexTerm_Prefix_See_Also);
			termStack.push(indexTerm);
		}
	}

	private void parseIndexSee(String localName) {
		// check to see it the index-see element or a specialized version is
		// in the list.
		if (indexSeeSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTerm parentTerm = null;
			
			indexTerm.setTermPrefix(Constants.IndexTerm_Prefix_See);
			
			if(!termStack.isEmpty()){
				parentTerm = (IndexTerm)termStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
					indexTerm.setTermPrefix(Constants.IndexTerm_Prefix_See_Also);
				}
			}
			termStack.push(indexTerm);
		}
	}

	private void parseIndexTerm(String localName, Attributes attributes) {
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			indexTerm.setStartAttribute(attributes.getValue(Constants.ATTRIBUTE_NAME_END));
			indexTerm.setEndAttribute(attributes.getValue(Constants.ATTRIBUTE_NAME_END));
			
			IndexTerm parentTerm = null;
			if(!termStack.isEmpty()){
				parentTerm = (IndexTerm)termStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
				}
			}
			
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
		} else if (classAttr.indexOf(Constants.ATTR_CLASS_VALUE_INDEXTERM) != -1) {
			// add the element name to the indexterm specialization element
			// list if it does not already exist in that list.
			if (!indexTermSpecList.contains(localName)) {
				indexTermSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ATTR_CLASS_VALUE_INDEXSEEALSO) != -1) {
			// add the element name to the index-see-also specialization element
			// list if it does not already exist in that list.
			if (!indexSeeAlsoSpecList.contains(localName)) {
				indexSeeAlsoSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ATTR_CLASS_VALUE_INDEXSEE) != -1) {
			// add the element name to the index-see specialization element
			// list if it does not already exist in that list.
			if (!indexSeeSpecList.contains(localName)) {
				indexSeeSpecList.add(localName);
			}
		} else if (classAttr.indexOf(Constants.ATTR_CLASS_VALUE_INDEXSORTAS) != -1) {
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
		} else if (classAttr.indexOf(Constants.ATTR_CLASS_VALUE_TITLE) != -1) {
			//add the element name to the title specailization element list
			// if it does not exist in that list.
			if (!titleSpecList.contains(localName)){
				titleSpecList.add(localName);
			}
		}
	}

	/**
	 * Set the current parsing file.
	 * @param target The parsingFile to set.
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
	 * Update the target name of each IndexTerm, recursively.
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
				target.setTargetName(titleMap.get(fragment));
			}else{
				target.setTargetName(defaultTitle);
			}
		}
		
		for(int i=0; i<subtermSize; i++){
			IndexTerm subterm = (IndexTerm)indexterm.getSubTerms().get(i);
			updateIndexTermTargetName(subterm);
		}
	}

	/** Whitespace normalization state. */
	private enum WhiteSpaceState { WORD, SPACE };
	
	/**
	 * Normalize and collapse whitespaces from string buffer.
	 * 
	 * @param strBuffer The string buffer.
	 */
	private void normalizeAndCollapseWhitespace(StringBuilder strBuffer){
		WhiteSpaceState currentState = WhiteSpaceState.WORD;
		for (int i = strBuffer.length() - 1; i >= 0; i--) {
			char currentChar = strBuffer.charAt(i);
			if (Character.isWhitespace(currentChar)) {
				if (currentState == WhiteSpaceState.SPACE) {
					strBuffer.delete(i, i + 1);
				} else if(currentChar != ' ') {
					strBuffer.replace(i, i + 1, " ");
				}
				currentState = WhiteSpaceState.SPACE;
			} else {
				currentState = WhiteSpaceState.WORD;
			}
		}
	}

	/**
	 * Trim whitespace from start of the string.
	 * 
	 * @param temp
	 * @param termName
	 * @return
	 */
	private String trimSpaceAtStart(final String temp, final String termName) {
		if(termName != null && termName.charAt(termName.length() - 1) == ' ') {
			if(temp.charAt(0) == ' ') {
				return temp.substring(1);
			}
		}
		return temp;
	}

}
