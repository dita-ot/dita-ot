/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.util.Deque;
import java.util.LinkedList;

import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.writer.ImageMetadataFilter.DITA_OT_NS;
import static org.dita.dost.writer.ImageMetadataFilter.DITA_OT_PREFIX;

/**
 * Normalize content.
 * 
 * <ul>
 *   <li>Add default metadata {@code cascade} attribute value.</li>
 * </ul>
 */
public final class NormalizeFilter extends AbstractXMLFilter {

    private Configuration.Mode processingMode;

    /** DITA class stack */
    private final Deque<String> classStack = new LinkedList<>();
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
            super.startPrefixMapping(DITA_OT_PREFIX, DITA_OT_NS);
        }

        final AttributesImpl res = new AttributesImpl(atts);
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        classStack.addFirst(cls);
        if (MAP_MAP.matches(cls)) {
            if (res.getIndex(ATTRIBUTE_NAME_CASCADE) == -1) {
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CASCADE,
                        configuration.containsKey("default.cascade")
                        ? configuration.get("default.cascade")
                        : ATTRIBUTE_CASCADE_VALUE_MERGE); 
            }
        }

        getContentHandler().startElement(uri, localName, qName, res);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        getContentHandler().endElement(uri, localName, qName);

        if (depth == 1) {
            super.endPrefixMapping(DITA_OT_PREFIX);
        }
        depth--;
    }

}
