/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

	/** The title of the dita file under parsing */
	private String title = null;

	/** Whether or not current element under parsing is a title element */
	private boolean inTitleElement = false;

	/** title element found? */
	private boolean isTitleFound = false;

	/** Whether or not current element under parsing is <index-sort-as> */
	private boolean insideSortingAs = false;
	
	/** Stack used to store index term */
	private Stack termStack = null;

	/** List used to store all the specilized index terms */
	private List indexTermSpecList = null;
	
	/** List used to store all the specilized index-see */
	private List indexSeeSpecList = null;
	
	/** List used to store all the specilized index-see-also */
	private List indexSeeAlsoSpecList = null;
	
	/** List used to store all the specilized index-sort-as */
	private List indexSortAsSpecList = null;
	
	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

	/**
	 * Constructor
	 */
	public IndexTermReader() {
		termStack = new Stack();
		indexTermSpecList = new ArrayList(Constants.INT_256);
		indexSeeSpecList = new ArrayList(Constants.INT_256);
		indexSeeAlsoSpecList = new ArrayList(Constants.INT_256);
		indexSortAsSpecList = new ArrayList(Constants.INT_256);
	}

	/**
	 * Reset the reader.
	 */
	public void reset() {
		targetFile = null;
		title = null;
		inTitleElement = false;
		isTitleFound = false;
		termStack.clear();
		indexTermSpecList.clear();
		indexSeeSpecList.clear();
		indexSeeAlsoSpecList.clear();
		indexSortAsSpecList.clear();
	}

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String temp = new String(ch, start, length).trim();

		if (temp.length() == 0) {
			return;
		}
		
		/*
		 * For title info
		 */
		if (inTitleElement) {
			temp = StringUtils.restoreEntity(temp);
			title = (title == null) ? temp : new StringBuffer(title).append(
					temp).toString();
		} else if (insideSortingAs && temp.length() > 0) {
			IndexTerm indexTerm = (IndexTerm) termStack.peek();
			temp = StringUtils.restoreEntity(temp);
			indexTerm.setTermKey(temp);
		} else if (!termStack.empty()) {
			IndexTerm indexTerm = (IndexTerm) termStack.peek();
			temp = StringUtils.restoreEntity(temp);
			if (indexTerm.getTermName() == null) {
				indexTerm.setTermName(temp);
			} else {
				indexTerm.setTermName(new StringBuffer(indexTerm.getTermName())
						.append(temp).toString());
			}
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
				IndexTermCollection.getInstantce().addTerm(term);
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
				term.setTermKey(term.getTermName());
			}
			term.addTargets(parentTerm.getTargetList());
			parentTerm.addSubTerm(term);
		}
		
		/*
		 * For title info
		 */
		if (Constants.ELEMENT_NAME_TITLE.equals(localName)
				&& !isTitleFound) {
			isTitleFound = true;
			inTitleElement = false;
		}
		
		// For <index-sort-as>
		if (indexSortAsSpecList.contains(localName)) {
			insideSortingAs = false;
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
		parseIndexTerm(localName);
		parseIndexSee(localName);
		parseIndexSeeAlso(localName);
		
		if (IndexTerm.getTermLocale() == null) {
			String xmlLang = attributes
					.getValue(Constants.ATTRIBUTE_NAME_XML_LANG);

			if (xmlLang != null) {
				Locale locale = new Locale(xmlLang.substring(0, 1), xmlLang
						.substring(3, 4));
				IndexTerm.setTermLocale(locale);
			}
		}

		/*
		 * For title info
		 */
		if (Constants.ELEMENT_NAME_TITLE.equals(localName)
				&& !isTitleFound) {
			inTitleElement = true;
		}
		
		// For <index-sort-as>
		if (indexSortAsSpecList.contains(localName)) {
			insideSortingAs = true;
		}
	}

	private void parseIndexSeeAlso(String localName) {
		// check to see it the index-see-also element or a specialized version
		// is in the list.
		if (indexSeeAlsoSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			indexTerm.setTermName("See also ");
			termStack.push(indexTerm);
		}
	}

	private void parseIndexSee(String localName) {
		// check to see it the index-see element or a specialized version is
		// in the list.
		if (indexSeeSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			indexTerm.setTermName("See ");
			termStack.push(indexTerm);
		}
	}

	private void parseIndexTerm(String localName) {
		// check to see it the indexterm element or a specialized version is 
		// in the list.
		if (indexTermSpecList.contains(localName)) {
			IndexTerm indexTerm = new IndexTerm();
			IndexTermTarget target = new IndexTermTarget();

			if (title != null) {
				target.setTargetName(title);
			} else {
				target.setTargetName(targetFile);
			}

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

}
