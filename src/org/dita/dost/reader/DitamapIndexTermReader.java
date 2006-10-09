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
	private Stack elementStack = null;
	
	/** List used to store all the specilized index terms */
	private List indexTermSpecList = null;
	
	private String mapPath = null;

	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

	/**
	 * Create a new instance of sax handler for ditamap.
	 */
	public DitamapIndexTermReader() {
		super();
		elementStack = new Stack();
		indexTermSpecList = new ArrayList(Constants.INT_256);
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String temp = new String(ch, start, length).trim();
		IndexTerm indexTerm = null;
		boolean withSpace = (ch[start] == '\n' || temp.startsWith(Constants.LINE_SEPARATOR));

		if (temp.length() == 0) {
			return;
		}

		if (elementStack.empty() || !(elementStack.peek() instanceof IndexTerm)) {
			return;
		}

		indexTerm = (IndexTerm) elementStack.peek();
		
		indexTerm.setTermName(StringUtils.setOrAppend(indexTerm.getTermName(), temp, withSpace));

	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (Constants.ELEMENT_NAME_TOPICREF.equals(localName)) {
			elementStack.pop();
			return;
		}
		
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName) && needPushTerm()) {
			IndexTerm indexTerm = (IndexTerm) elementStack.pop();
			Object obj = null;

			if (indexTerm.getTermName() == null) {
				indexTerm.setTermName("***");
				javaLogger
						.logWarn("The indexterm element does not have any content. Setting the term to ***.");
			}
			
			if(indexTerm.getTermKey() == null){
				indexTerm.setTermKey(indexTerm.getTermName());
			}

			obj = elementStack.peek();

			if (obj instanceof TopicrefElement) {
				IndexTermCollection.getInstantce().addTerm(indexTerm);
			} else {
				IndexTerm parentTerm = (IndexTerm) obj;
				parentTerm.addSubTerm(indexTerm);
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
				&& classAttr.indexOf(Constants.ELEMENT_NAME_INDEXTERM) != -1) {
			// add the element name to the indexterm specialization element 
			// list if it does not already exist in that list.  
			if (!indexTermSpecList.contains(localName)){
				indexTermSpecList.add(localName);
			}
		}
		
		if (Constants.ELEMENT_NAME_TOPICREF.equals(localName)) {
			String href = attributes.getValue(Constants.ATTRIBUTE_NAME_HREF);
			String format = attributes
					.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
			TopicrefElement topicref = new TopicrefElement();

			topicref.setHref(href);
			topicref.setFormat(format);
			elementStack.push(topicref);

			return;
		}
		
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName) && needPushTerm()) {
			IndexTerm indexTerm = new IndexTerm();
			Object obj = elementStack.peek();

			if (obj instanceof TopicrefElement) {
				TopicrefElement topicref = (TopicrefElement) obj;
				IndexTermTarget target = new IndexTermTarget();
				String targetURI = null;

				String href = topicref.getHref();
				String targetName = href;

				StringBuffer buffer = new StringBuffer();
				if (mapPath != null && !Constants.STRING_EMPTY.equals(mapPath)) {
					buffer.append(mapPath);
					buffer.append(Constants.SLASH);
				}
				buffer.append(href);
				targetURI = FileUtils.removeRedundantNames(buffer
						.toString());

				if (targetName.lastIndexOf(Constants.SLASH) != -1) {
					targetName = targetName.substring(targetName
							.lastIndexOf(Constants.SLASH) + 1);
				}

				if (targetName.lastIndexOf(Constants.BACK_SLASH) != -1) {
					targetName = targetName.substring(targetName
							.lastIndexOf(Constants.BACK_SLASH) + 1);
				}

				target.setTargetName(targetName);
				target.setTargetURI(targetURI);
				indexTerm.addTarget(target);

			} else {
				IndexTerm parentTerm = (IndexTerm) obj;

				indexTerm.addTargets(parentTerm.getTargetList());
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
			return ((TopicrefElement) elementStack.peek()).needExtractTerm();
		}

		return true;
	}

	/**
	 * Set map path.
	 * 
	 * @param mappath
	 */
	public void setMapPath(String mappath) {
		this.mapPath = mappath;
	}

}
