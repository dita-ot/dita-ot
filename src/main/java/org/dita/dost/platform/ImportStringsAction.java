/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.NULL_NS_URI;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ImportStringsAction class.
 *
 */
final class ImportStringsAction extends ImportAction {

  /**
   * get result.
   */
  @Override
  public void getResult(final ContentHandler buf) throws SAXException {
    final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
    for (final Value value : valueSet) {
      final String path;
      if (value instanceof Value.PathValue pathValue) {
        path = pathValue.getPath();
      } else {
        logger.error("String import must be a file feature: " + value.value());
        continue;
      }
      buf.startElement(NULL_NS_URI, "stringfile", "stringfile", new AttributesBuilder().build());
      final char[] location = FileUtils.getRelativeUnixPath(templateFilePath, path).toCharArray();
      buf.characters(location, 0, location.length);
      buf.endElement(NULL_NS_URI, "stringfile", "stringfile");
    }
  }
}
