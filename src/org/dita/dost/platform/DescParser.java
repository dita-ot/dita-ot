/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Zhang, Yuan Peng
 */
public class DescParser extends DefaultHandler{
	private String currentPlugin;
	private String currentElement;
	private Features features;
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		currentElement = qName;
		if( "plugin".equals(currentElement) ){
			currentPlugin = attributes.getValue("id");
		} else if ("feature".equals(currentElement)){
			features.addFeature(attributes.getValue("extension"), attributes.getValue("value"), attributes.getValue("type"));
		} else if ("require".equals(currentElement)){
			features.addRequire(attributes.getValue("plugin"));
		} else if ("meta".equals(currentElement)){
			features.addMeta(attributes.getValue("type"), attributes.getValue("value"));
		}
	}

	public void endDocument() throws SAXException {
		Integrator.pluginTable.put(currentPlugin, features);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentElement = null;
	}

	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	}

	public DescParser(String location) {
		
		features = new Features(location);
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
	}
	
}
