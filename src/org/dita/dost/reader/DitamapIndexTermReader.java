/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */

package org.dita.dost.reader;

import java.util.Iterator;
import java.util.Stack;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.index.TopicrefElement;
import org.dita.dost.util.Constants;
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

	private String mapPath = null;

	/**
	 * Create a new instance of sax handler for ditamap.
	 */
	public DitamapIndexTermReader() {
		super();
		elementStack = new Stack();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {		
		String temp = new String(ch, start, length).trim();
		IndexTerm indexTerm = null;
		
		if (temp.length() == 0) {
			return;
		}
		
		if (elementStack.empty() || !(elementStack.peek() instanceof IndexTerm)) {
			return;
		}

	    indexTerm = (IndexTerm) elementStack.peek();

		if (indexTerm.getTermName() == null) {
			indexTerm.setTermName(temp);
		} else {
			indexTerm.setTermName(new StringBuffer(indexTerm.getTermName()).append(
					Constants.STRING_BLANK).append(temp).toString());
		}

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

		if (Constants.ELEMENT_NAME_INDEXTERM.equals(localName)
				&& needPushTerm()) {
			IndexTerm indexTerm = (IndexTerm) elementStack.pop();

			Object obj = elementStack.peek();

			if (obj instanceof TopicrefElement) {
				IndexTermCollection.addTerm(indexTerm);
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

		if (Constants.ELEMENT_NAME_INDEXTERM.equals(localName)
				&& needPushTerm()) {
			IndexTerm indexTerm = new IndexTerm();
			Object obj = elementStack.peek();

			if (obj instanceof TopicrefElement) {
				TopicrefElement topicref = (TopicrefElement) obj;
				IndexTermTarget target = new IndexTermTarget();

				String href = topicref.getHref();
				String targetName = href;

				StringBuffer buffer = new StringBuffer();
				if (mapPath != null && !mapPath.equals(Constants.STRING_EMPTY)) {
					buffer.append(mapPath);
					buffer.append(Constants.SLASH);
				}
				buffer.append(href);
				String targetURI = StringUtils.removeRedundantNames(buffer
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
	 * @param mapPath
	 */
	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

}
