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

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;

/**
 * Normalize content.
 *
 * <ul>
 *   <li>Add default metadata {@code cascade} attribute value.</li>
 *   <li>Strip redundant whitespace from {@code domains} attribute value.</li>
 * </ul>
 */
public final class NormalizeFilter extends AbstractXMLFilter {

    private final Pattern whitespace = Pattern.compile("\\s+");
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

        AttributesImpl res = null;
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (MAP_MAP.matches(cls)) {
            if (atts.getIndex(ATTRIBUTE_NAME_CASCADE) == -1) {
                if (res == null) {
                    res = new AttributesImpl(atts);
                }
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CASCADE,
                        configuration.getOrDefault("default.cascade", ATTRIBUTE_CASCADE_VALUE_MERGE));
            }
        }
        if (MAP_MAP.matches(cls) || TOPIC_TOPIC.matches(cls)) {
            final String domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
            if (domains != null) {
                final String normalized = whitespace.matcher(domains.trim()).replaceAll(" ");
                if (res == null) {
                    res = new AttributesImpl(atts);
                }
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_DOMAINS, normalized);
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_SPECIALIZATIONS, domainsToSpecializations(normalized));
            }
            final String specializations = atts.getValue(ATTRIBUTE_NAME_SPECIALIZATIONS);
            if (specializations != null) {
                final String normalized = whitespace.matcher(specializations.trim()).replaceAll(" ");
                if (res == null) {
                    res = new AttributesImpl(atts);
                }
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_SPECIALIZATIONS, normalized);
                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_DOMAINS, specializationsToDomains(normalized));
            }
        }

        getContentHandler().startElement(uri, localName, qName, res != null ? res : atts);
    }

    private String domainsToSpecializations(final String domains) {
        return Arrays.stream(domains.trim().replaceAll("(\\))\\s+(a?\\()", "$1\n$2").split("\n"))
                .filter(token -> token.startsWith("a("))
                .map(token -> "@" + token.substring(2, token.length() - 1).replace(' ', '/'))
                .collect(Collectors.joining(" "));
    }

    private String specializationsToDomains(final String specializations) {
        return Arrays.stream(specializations.trim().split("\\s+"))
                .map(token -> token.replaceAll("^@(.+)/(.+)$", "a($1 $2)"))
                .collect(Collectors.joining(" "));
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
