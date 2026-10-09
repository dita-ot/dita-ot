/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2026 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Restore key reference attributes, i.e. {@code keyref}, from the {@code dita-ot:orig-*} attributes that key resolution
 * stores their values in, so that processing after preprocessing can rely on them being present. The
 * {@code dita-ot:orig-*} attributes are retained.
 */
public final class KeyrefRestoreFilter extends AbstractXMLFilter {

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
    throws SAXException {
    AttributesImpl res = null;
    for (final String attr : KeyrefParser.KEYREF_ATTRIBUTES) {
      final String orig = atts.getValue(DITA_OT_NS, KeyrefParser.origAttributeName(attr));
      if (orig != null && atts.getIndex(attr) == -1) {
        if (res == null) {
          res = new AttributesImpl(atts);
        }
        XMLUtils.addOrSetAttribute(res, attr, orig);
      }
    }
    getContentHandler().startElement(uri, localName, qName, res != null ? res : atts);
  }
}
