/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * @author william
 *
 */
public class LangParser extends DefaultHandler {
	
	private String langCode = null;

	public String getLangCode() {
		return langCode;
	}
	
	public LangParser() {
		
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
        //String processedString;
        String classAttr = attributes.getValue("class");
        String langAttr = attributes.getValue("xml:lang");

        if(classAttr != null && langAttr != null) {
            if ((classAttr.indexOf(" map/map ") > -1) ||
                (classAttr.indexOf(" topic/topic ") > -1)) {
                    langCode = langAttr.toLowerCase();
            }
        }

    }
	
	@Override
	public void startDocument() throws SAXException {
		langCode = null;
	}
}
