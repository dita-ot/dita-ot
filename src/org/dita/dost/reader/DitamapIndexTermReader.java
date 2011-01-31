/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */

package org.dita.dost.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.index.TopicrefElement;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class extends SAX's DefaultHandler, used for parsing indexterm in
 * ditamap.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */

public class DitamapIndexTermReader extends AbstractXMLReader {
	/** The stack used to store elements */
	private Stack<Object> elementStack = null;
	
	/** List used to store all the specialized index terms */
	private List<String> indexTermSpecList = null;
	
	/** List used to store all the specialized topicref tags */
	private List<String> topicrefSpecList = null;
	
	/** List used to store all the specialized index-see tags */
	private List<String> indexSeeSpecList = null;
	
	/** List used to store all the specialized index-see-also tags */
	private List<String> indexSeeAlsoSpecList = null;
	
	private String mapPath = null;

	private DITAOTJavaLogger javaLogger = null;
	//Added by William on 2010-04-26 for ref:2990783 start
	private IndexTermCollection result;
	// assumes index terms have been moved by preprocess
	private boolean indexMoved = true; 
	//Added by William on 2010-04-26 for ref:2990783 end
	
	/**
	 * Create a new instance of sax handler for ditamap.
	 * 
	 * @deprecated use {@link #DitamapIndexTermReader(IndexTermCollection, boolean)} instead
	 */
	@Deprecated
	public DitamapIndexTermReader() {
		super();
		elementStack = new Stack<Object>();
		indexTermSpecList = new ArrayList<String>(Constants.INT_16);
		topicrefSpecList = new ArrayList<String>(Constants.INT_16);
		indexSeeSpecList = new ArrayList<String>(Constants.INT_16);
		indexSeeAlsoSpecList = new ArrayList<String>(Constants.INT_16);
		javaLogger = new DITAOTJavaLogger();
		if (result == null) {
		    result = IndexTermCollection.getInstantce();
		}
	}
	//Added by William on 2010-04-26 for ref:2990783 start
	public DitamapIndexTermReader(IndexTermCollection result, boolean indexMoved) {
		this();
		this.result = result;
 		this.indexMoved = indexMoved;
	}
	//Added by William on 2010-04-26 for ref:2990783 end

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		//SF Bug 2010062: Do not trim white space from text nodes. Convert newline
		//                to space, but leave all spaces. Also do not drop space-only nodes.
		String temp = new String(ch, start, length);
		IndexTerm indexTerm = null;
		//boolean withSpace = (ch[start] == '\n' || temp.startsWith(Constants.LINE_SEPARATOR));
		if (ch[start] == '\n' || temp.startsWith(Constants.LINE_SEPARATOR)) {
			temp = " " + temp.substring(1);
		}

//		if (temp.length() == 0) {
//			return;
//		}
		
		//TODO Added by William on 2009-05-22 for space bug:2793836 start
		//used for store the space
		char[] chars = temp.toCharArray();
		char flag = '\n';
		//used for store the new String
		StringBuffer sb = new StringBuffer();
		for(char c : chars){
			//when a whitespace is met
			if(c==' '){
				//this is the first whitespace
				if(flag!=' '){
					//put it in the result string
					sb.append(c);
					//store the space in the flag
					flag = c;
				}else{
					//abundant space, ignore it
					continue;
				}
			//the consecutive whitespace is interrupted
			}else{
				//put it in the result string
				sb.append(c);
				//clear the flag
				flag = c;	
			}
		}
		temp = sb.toString();
		//TODO Added by William on 2009-05-22 for space bug:2793836 end
		
		if (elementStack.empty() || !(elementStack.peek() instanceof IndexTerm)) {
			return;
		}

		indexTerm = (IndexTerm) elementStack.peek();
		
		indexTerm.setTermName(StringUtils.setOrAppend(indexTerm.getTermName(), temp, false));

	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (topicrefSpecList.contains(localName)) {
			elementStack.pop();
			return;
		}
		
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName) && needPushTerm()) {
			IndexTerm indexTerm = (IndexTerm) elementStack.pop();
			Object obj = null;

			if (indexTerm.getTermName() == null || indexTerm.getTermName().trim().equals("")) {
				if(indexTerm.getEndAttribute() != null && !indexTerm.hasSubTerms()){
					return;
				}else{
					indexTerm.setTermName("***");
					javaLogger.logWarn(MessageUtils.getMessage("DOTJ014W").toString());
				}
			}
			
			if(indexTerm.getTermKey() == null){
				indexTerm.setTermKey(indexTerm.getTermName());
			}

			obj = elementStack.peek();

			if (obj instanceof TopicrefElement) {
				if(((TopicrefElement)obj).getHref()!=null){
					genTargets(indexTerm, (TopicrefElement)obj);
					//IndexTermCollection.getInstantce().addTerm(indexTerm);
					//Added by William on 2010-04-26 for ref:2990783 start
					result.addTerm(indexTerm);
					//Added by William on 2010-04-26 for ref:2990783 end
					
					
				}				
			} else {
				IndexTerm parentTerm = (IndexTerm) obj;
				parentTerm.addSubTerm(indexTerm);
			}
		}
		
		// Check to see if the index-see or index-see-also or a specialized 
		// version is in the list.
		if (indexSeeSpecList.contains(localName)
				|| indexSeeAlsoSpecList.contains(localName)) {
			IndexTerm term = (IndexTerm) elementStack.pop();
			if (term.getTermKey() == null) {
				term.setTermKey(term.getTermFullName());
			}
			if (elementStack.peek() instanceof IndexTerm){
				IndexTerm parentTerm = (IndexTerm) elementStack.peek();
				parentTerm.addSubTerm(term);
			}
		}
	}

	private void genTargets(IndexTerm indexTerm, TopicrefElement obj) {
		
		TopicrefElement topicref = obj;
		IndexTermTarget target = new IndexTermTarget();
		String targetURI = null;

		String href = topicref.getHref();
		
		StringBuffer buffer = new StringBuffer();
		if (!href.contains(Constants.COLON_DOUBLE_SLASH) && !FileUtils.isAbsolutePath(href)){
			if (mapPath != null && !Constants.STRING_EMPTY.equals(mapPath)) {
				buffer.append(mapPath);
				buffer.append(Constants.SLASH);
			}
			buffer.append(href);
			targetURI = FileUtils.removeRedundantNames(buffer
					.toString());
		}else{
			targetURI = href;
		}
		
		if (topicref.getNavTitle() != null){
			target.setTargetName(topicref.getNavTitle());
		}else {
			target.setTargetName(href);
		}
		
		target.setTargetURI(targetURI);
			
		assignTarget(indexTerm, target);
			
	}

	private void assignTarget(IndexTerm indexTerm, IndexTermTarget target) {
		if (indexTerm.isLeaf()){
			indexTerm.addTarget(target);
		}
		
		if (indexTerm.hasSubTerms()){
			for (Object subTerm : indexTerm.getSubTerms()){
				assignTarget((IndexTerm)subTerm, target);
			}
		}
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String classAttr = attributes.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		if (classAttr != null
				&& classAttr.contains(Constants.ATTR_CLASS_VALUE_INDEXTERM)) {
			// add the element name to the indexterm specialization element 
			// list if it does not already exist in that list.  
			if (!indexTermSpecList.contains(localName)){
				indexTermSpecList.add(localName);
			}
		}
		
		if (classAttr != null
				&& classAttr.contains(Constants.ATTR_CLASS_VALUE_TOPICREF)){
			if (!topicrefSpecList.contains(localName)){
				topicrefSpecList.add(localName);
			}
		}
		
		if (classAttr != null
				&& classAttr.contains(Constants.ATTR_CLASS_VALUE_INDEXSEE)){
			if (!indexSeeSpecList.contains(localName)){
				indexSeeSpecList.add(localName);
			}
		}
		
		if (classAttr != null
				&& classAttr.contains(Constants.ATTR_CLASS_VALUE_INDEXSEEALSO)){
			if (!indexSeeAlsoSpecList.contains(localName)){
				indexSeeAlsoSpecList.add(localName);
			}
		}
		
		if (topicrefSpecList.contains(localName)) {
			String href = attributes.getValue(Constants.ATTRIBUTE_NAME_HREF);
			String format = attributes
					.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
			String navtitle =  attributes.getValue(Constants.ATTRIBUTE_NAME_NAVTITLE);
			TopicrefElement topicref = new TopicrefElement();

			topicref.setHref(href);
			topicref.setFormat(format);
			topicref.setNavTitle(navtitle);
			elementStack.push(topicref);

			return;
		}
		
		parseIndexTerm(localName, attributes);
		parseIndexSee(localName);
		parseIndexSeeAlso(localName);
				
	}
	
	private void parseIndexSeeAlso(String localName) {
		// check to see it the index-see-also element or a specialized version
		// is in the list.
		if (indexSeeAlsoSpecList.contains(localName)
				&& needPushTerm()) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTerm parentTerm = null;
			if(!elementStack.isEmpty()					
					&& elementStack.peek() instanceof IndexTerm){
				parentTerm = (IndexTerm)elementStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
				}
			}
			indexTerm.setTermPrefix(Constants.IndexTerm_Prefix_See_Also);
			elementStack.push(indexTerm);
		}
	}

	private void parseIndexSee(String localName) {
		// check to see it the index-see element or a specialized version is
		// in the list.
		if (indexSeeSpecList.contains(localName)
				&& needPushTerm()) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTerm parentTerm = null;
			
			indexTerm.setTermPrefix(Constants.IndexTerm_Prefix_See);
			
			if(!elementStack.isEmpty()
					&& elementStack.peek() instanceof IndexTerm){
				parentTerm = (IndexTerm)elementStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
					indexTerm.setTermPrefix(Constants.IndexTerm_Prefix_See_Also);
				}
			}
			elementStack.push(indexTerm);
		}
	}
	
	private void parseIndexTerm(String localName, Attributes attributes) {
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName) && needPushTerm()) {
			IndexTerm indexTerm = new IndexTerm();
			indexTerm.setStartAttribute(attributes.getValue(Constants.ATTRIBUTE_NAME_END));
			indexTerm.setEndAttribute(attributes.getValue(Constants.ATTRIBUTE_NAME_END));
			IndexTerm parentTerm = null;
			if(!elementStack.isEmpty()
					&& elementStack.peek() instanceof IndexTerm){
				parentTerm = (IndexTerm)elementStack.peek();
				if(parentTerm.hasSubTerms()){
					parentTerm.updateSubTerm();
				}
			}
			elementStack.push(indexTerm);
		}
	}

	/**
	 * Check element stack for the root topicref or indexterm element.
	 */
	private boolean needPushTerm() {
		if (elementStack.empty()) {
			return false;
		}
		

		if (elementStack.peek() instanceof TopicrefElement) {
//			if (!FileUtils.isHTMLFile(((TopicrefElement) elementStack.peek()).getHref())){ //Eric
//				return false;
//			}
//			return ((TopicrefElement) elementStack.peek()).needExtractTerm();
			// for dita files the indexterm has been moved to its <prolog>
			// therefore we don't need to collect these terms again.
			//Edited by William on 2010-04-26 for ref:2990783 start
			if (indexMoved && FileUtils.isDITAFile(((TopicrefElement) elementStack.peek()).getHref())){
			//Edited by William on 2010-04-26 for ref:2990783 end
				return false;
			}
		}
		return true;
	}

	/**
	 * Set map path.
	 * 
	 * @param mappath path of map file
	 */
	public void setMapPath(String mappath) {
		this.mapPath = mappath;
	}

}
