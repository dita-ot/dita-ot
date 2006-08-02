package org.dita.dost.util;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class TopicIdParser implements ContentHandler {
	private boolean isFirstId = true;
	private StringBuffer firstId = null;
	
	public TopicIdParser(StringBuffer result) {
		firstId = result;
	}

	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub

	}

	public void startDocument() throws SAXException {
		isFirstId = true;

	}

	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub

	}

	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub

	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (isFirstId){
			if (atts.getValue(Constants.ATTRIBUTE_NAME_ID)!=null){
				isFirstId = false;
				firstId.append(atts.getValue(Constants.ATTRIBUTE_NAME_ID));
			}
		}

	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub

	}

}
