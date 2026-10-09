/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2026 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

class KeyrefRestoreFilterTest {

  private Attributes filter(final Attributes atts) throws SAXException {
    final Attributes[] res = new Attributes[1];
    final KeyrefRestoreFilter filter = new KeyrefRestoreFilter();
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) {
          res[0] = new AttributesImpl(atts);
        }
      }
    );
    filter.startElement("", "xref", "xref", atts);
    return res[0];
  }

  private AttributesImpl origKeyref(final String value) {
    return orig(new AttributesImpl(), ATTRIBUTE_NAME_KEYREF, value);
  }

  private AttributesImpl orig(final AttributesImpl atts, final String attr, final String value) {
    final String name = ATTRIBUTE_NAME_ORIG_PREFIX + attr;
    atts.addAttribute(DITA_OT_NS, name, DITA_OT_NS_PREFIX + ":" + name, "CDATA", value);
    return atts;
  }

  @Test
  void restoreKeyref() throws SAXException {
    final Attributes act = filter(origKeyref("key"));

    assertEquals("key", act.getValue(ATTRIBUTE_NAME_KEYREF));
    assertEquals("key", act.getValue(DITA_OT_NS, ATTRIBUTE_NAME_ORIG_KEYREF));
  }

  @Test
  void restoreObjectKeyrefs() throws SAXException {
    final AttributesImpl atts = new AttributesImpl();
    orig(atts, ATTRIBUTE_NAME_ARCHIVEKEYREFS, "a");
    orig(atts, ATTRIBUTE_NAME_CLASSIDKEYREF, "b");
    orig(atts, ATTRIBUTE_NAME_CODEBASEKEYREF, "c");
    orig(atts, ATTRIBUTE_NAME_DATAKEYREF, "d");

    final Attributes act = filter(atts);

    assertEquals("a", act.getValue(ATTRIBUTE_NAME_ARCHIVEKEYREFS));
    assertEquals("b", act.getValue(ATTRIBUTE_NAME_CLASSIDKEYREF));
    assertEquals("c", act.getValue(ATTRIBUTE_NAME_CODEBASEKEYREF));
    assertEquals("d", act.getValue(ATTRIBUTE_NAME_DATAKEYREF));
    assertNull(act.getValue(ATTRIBUTE_NAME_KEYREF));
  }

  @Test
  void retainExistingKeyref() throws SAXException {
    final AttributesImpl atts = origKeyref("key");
    atts.addAttribute("", ATTRIBUTE_NAME_KEYREF, ATTRIBUTE_NAME_KEYREF, "CDATA", "other");

    final Attributes act = filter(atts);

    assertEquals("other", act.getValue(ATTRIBUTE_NAME_KEYREF));
  }

  @Test
  void noOrigKeyref() throws SAXException {
    final Attributes act = filter(new AttributesImpl());

    assertNull(act.getValue(ATTRIBUTE_NAME_KEYREF));
  }
}
