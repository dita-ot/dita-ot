/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Content handler to read {@code xml:lang} attribute from map or topic element.
 * 
 * @author william
 *
 */
public final class LangParser extends DefaultHandler {

    private String langCode = null;

    /**
     * Get {@code xml:lang} attribute value of last map or topic element.
     * 
     * @return language code, {@code null} if not found
     */
    public String getLangCode() {
        return langCode;
    }

    public LangParser() {

    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes attributes) throws SAXException {
        //String processedString;
        final String classAttr = attributes.getValue(ATTRIBUTE_NAME_CLASS);
        final String langAttr = attributes.getValue(ATTRIBUTE_NAME_XML_LANG);

        if(classAttr != null && langAttr != null) {
            if (MAP_MAP.matches(classAttr) || TOPIC_TOPIC.matches(classAttr)) {
                langCode = langAttr.toLowerCase();
            }
        }

    }

    @Override
    public void startDocument() throws SAXException {
        langCode = null;
    }
}
