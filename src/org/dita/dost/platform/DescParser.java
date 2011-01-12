/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parser to parse description file of plugin.
 * @author Zhang, Yuan Peng
 */
public class DescParser extends DefaultHandler{
	private String currentPlugin = null;
	private Features features = null;
	
	/**
	 * DescParser Constructor.
	 *
	 */
	public DescParser(){
		this(null);
	}
	
	/**
	 * Constructor initialize Feature with location.
	 * @param location location
	 */
	public DescParser(String location) {
		super();
		features = new Features(location);
	}
	
	/**
	 * Process configuration start element.
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if( "plugin".equals(qName) ){
			currentPlugin = attributes.getValue("id");
		} else if ("feature".equals(qName)){
			features.addFeature(attributes.getValue("extension"), attributes.getValue("value"), attributes.getValue("type"));
		} else if ("require".equals(qName)){
			features.addRequire(attributes.getValue("plugin"), attributes.getValue("importance"));
		} else if ("meta".equals(qName)){
			features.addMeta(attributes.getValue("type"), attributes.getValue("value"));
		} else if ("template".equals(qName)){
			features.addTemplate(attributes.getValue("file"));
		}
	}
	
	/**
	 * Get plug-in features.
	 * 
	 * @return plug-in features
	 */
	public Features getFeatures() {
		return features;
	}
	
	public String getPluginId() {
		return currentPlugin; 
	}
	
}
