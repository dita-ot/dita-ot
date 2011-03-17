/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * SAX Parser that handles topic id identification.
 * 
 */
public final class TopicIdParser implements ContentHandler {
	private boolean isFirstId = true;
	private StringBuffer firstId = null;
	
	/**
	 * Default Constructor.
	 *
	 */
	public TopicIdParser(){
		this(null);
	}
	
	/**
	 * Constructor.
	 * @param result to store the topic id
	 */
	public TopicIdParser(final StringBuffer result) {
		firstId = result;
	}

	/**
	 * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
	 */
	public void setDocumentLocator(final Locator locator) {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		isFirstId = true;

	}

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
	 */
	public void startPrefixMapping(final String prefix, final String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
	 */
	public void endPrefixMapping(final String prefix) throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(final String uri, final String localName, final String qName,
			final Attributes atts) throws SAXException {
		if (isFirstId){
			if (atts.getValue(Constants.ATTRIBUTE_NAME_ID)!=null){
				isFirstId = false;
				firstId.append(atts.getValue(Constants.ATTRIBUTE_NAME_ID));
			}
		}

	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(final char[] ch, final int start, final int length)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(final char[] ch, final int start, final int length)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
	 */
	public void processingInstruction(final String target, final String data)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
	 */
	public void skippedEntity(final String name) throws SAXException {
		// TODO Auto-generated method stub

	}

}
