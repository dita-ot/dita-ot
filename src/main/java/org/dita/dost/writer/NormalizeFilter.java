/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;

/**
 * Normalize content.
 * 
 * <ul>
 *   <li>Add default metadata {@code cascade} attribute value.</li>
 * </ul>
 */
public final class NormalizeFilter extends AbstractXMLFilter {

    private Configuration.Mode processingMode;

    private int depth;

    public NormalizeFilter() {
        super();
        depth = 0;
    }

    public void setProcessingMode(final Configuration.Mode processingMode) {
        this.processingMode = processingMode;
    }

    // SAX methods

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        depth++;
        if (depth == 1) {
            super.startPrefixMapping(DITA_OT_NS_PREFIX, DITA_OT_NS);
        }

        final AttributesImpl res = new AttributesImpl(atts);
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (MAP_MAP.matches(cls)) {
            if (res.getIndex(ATTRIBUTE_NAME_CASCADE) == -1) {
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CASCADE,
                        configuration.getOrDefault("default.cascade", ATTRIBUTE_CASCADE_VALUE_MERGE));
            }
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);

        if (depth == 1) {
            super.endPrefixMapping(DITA_OT_NS_PREFIX);
        }
        depth--;
    }

}
