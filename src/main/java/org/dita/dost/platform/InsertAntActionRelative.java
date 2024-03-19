/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import java.io.File;
import java.util.Map;
import org.dita.dost.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * InsertAntActionRelative inserts the children of the root element of an XML document
 * into a plugin extension point, rewriting relative file references so that they
 * are still correct in their new location.
 * <p>
 * Attributes affected: import/@file
 *
 * @author Deborah Pickett
 *
 */
final class InsertAntActionRelative extends InsertAction {

  private static final Map<String, String> RELATIVE_ATTRS = Map.of("import", "file", "lang", "filename");

  @Override
  public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
    throws SAXException {
    final AttributesImpl attrBuf = new AttributesImpl();

    final int attLen = attributes.getLength();
    for (int i = 0; i < attLen; i++) {
      final String name = attributes.getQName(i);
      String value = attributes.getValue(i);
      if (
        RELATIVE_ATTRS.containsKey(localName) &&
        RELATIVE_ATTRS.get(localName).equals(name) &&
        !FileUtils.isAbsolutePath(value)
      ) {
        // Rewrite file path to be local to its final resting place.
        final File targetFile = new File(new File(currentFile).getParentFile(), value);
        value = FileUtils.getRelativeUnixPath(paramTable.get(FileGenerator.PARAM_TEMPLATE), targetFile.toString());
      }
      attrBuf.addAttribute(attributes.getURI(i), attributes.getLocalName(i), name, attributes.getType(i), value);
    }

    super.startElement(uri, localName, qName, attrBuf);
  }
}
