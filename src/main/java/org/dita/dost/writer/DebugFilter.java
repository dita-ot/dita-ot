/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.util.HashMap;
import java.util.Map;

import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.DitaClass;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Add debug attributes.
 *
 * <p>The following attributes are added to elements:</p>
 * <dl>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRF xtrf}</dt>
 *   <dd>Absolute system path of the source file.</dd>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRC xtrc}</dt>
 *   <dd>Element location in the document, {@code element-name ":" element-count ";" row-number ":" colum-number}.</dd>
 * </dl>
 */
public final class DebugFilter extends AbstractXMLFilter {

    private Locator locator;
    private final Map<String, Integer> counterMap = new HashMap<>();

    // Locator methods

    @Override
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
        getContentHandler().setDocumentLocator(locator);
    }

    // SAX methods
    @Override
    public void startDocument() throws SAXException {
        getContentHandler().startDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final DitaClass cls = DitaClass.getInstance(atts);

        final AttributesImpl res = new AttributesImpl(atts);
        if (cls != null && !ELEMENT_NAME_DITA.equals(localName)) {
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_XTRF, currentFile.toString());

            Integer nextValue;
            if (counterMap.containsKey(qName)) {
                final Integer value = counterMap.get(qName);
                nextValue = value + 1;
            } else {
                nextValue = 1;
            }
            counterMap.put(qName, nextValue);
            final StringBuilder xtrc = new StringBuilder(qName).append(COLON).append(nextValue.toString());
            if (locator != null) {
                xtrc.append(';')
                    .append(Integer.toString(locator.getLineNumber()))
                    .append(COLON)
                    .append(Integer.toString(locator.getColumnNumber()));
            }
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_XTRC, xtrc.toString());
        }
        super.startElement(uri, localName, qName, res);
    }

}
