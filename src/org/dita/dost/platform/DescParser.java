/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser to parse description file of plugin
 * @author Zhang, Yuan Peng
 */
public class DescParser extends DefaultHandler{
	private String currentPlugin = null;
	private String currentElement = null;
	private Features features = null;
	
	/**
	 * DescParser Constructor
	 *
	 */
	public DescParser(){
		this(null);
	}
	
	/**
	 * Constructor initialize Feature with location
	 * @param location
	 */
	public DescParser(String location) {
		features = new Features(location);
	}
	
	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
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

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		Integrator.pluginTable.put(currentPlugin, features);
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		currentElement = null;
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
	}
	
}
