/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

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
final class LangParser extends DefaultHandler {

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

        if (classAttr != null && langAttr != null) {
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
