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
	private final Features features;
	
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
	public DescParser(final String location) {
		super();
		features = new Features(location);
	}
	
	/**
	 * Process configuration start element.
	 */
	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if( "plugin".equals(qName) ){
			currentPlugin = attributes.getValue("id");
		} else if ("feature".equals(qName)){
			features.addFeature(attributes.getValue("extension"), attributes);
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
	
	/**
	 * Get plugin ID.
	 * 
	 * @return plugin ID, <code>null</code> if not defined
	 */
	public String getPluginId() {
		return currentPlugin; 
	}
	
}
