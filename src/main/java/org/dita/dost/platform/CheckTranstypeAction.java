/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.NULL_NS_URI;

import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
/**
 * CheckTranstypeAction class.
 *
 */
final class CheckTranstypeAction extends ImportAction {

    /**
     * Get result.
     * @return result
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final String property = paramTable.containsKey("property") ? paramTable.get("property") : "transtype";
        for (final String value: valueSet) {
            buf.startElement(NULL_NS_URI, "not", "not", new AttributesBuilder().build());
            buf.startElement(NULL_NS_URI, "equals", "equals", new AttributesBuilder()
                .add("arg1", "${" + property + "}")
                .add("arg2", value)
                .add("casesensitive", "false")
                .build());
            buf.endElement(NULL_NS_URI, "equals", "equals");
            buf.endElement(NULL_NS_URI, "not", "not");
        }
    }

}
