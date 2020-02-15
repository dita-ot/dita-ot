/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class IncludeFilter extends XMLFilterImpl {
    public IncludeFilter(final ContentHandler handler) {
        super();
        setContentHandler(handler);
    }

    public void startDocument() throws SAXException {
        // Ignore
    }

    public void endDocument() throws SAXException {
        // Ignore
    }
}
