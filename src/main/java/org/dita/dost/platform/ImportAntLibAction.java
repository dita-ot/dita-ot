/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

import org.dita.dost.util.FileUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ImportAntLibAction class.
 *
 */
final class ImportAntLibAction extends ImportAction {

  /**
   * get result.
   */
  @Override
  public void getResult(final ContentHandler retBuf) throws SAXException {
    final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
    for (final Value value : valueSet) {
      final String path;
      if (value instanceof Value.PathValue pathValue) {
        path = pathValue.getPath();
      } else {
        logger.error("Ant import must be a file feature: " + value.value());
        continue;
      }
      final String resolvedValue = FileUtils.getRelativeUnixPath(templateFilePath, path);
      if (FileUtils.isAbsolutePath(resolvedValue)) {
        // if resolvedValue is absolute path
        retBuf.startElement(
          NULL_NS_URI,
          "pathelement",
          "pathelement",
          new AttributesBuilder().add("location", resolvedValue).build()
        );
        retBuf.endElement(NULL_NS_URI, "pathelement", "pathelement");
      } else { // if resolvedValue is relative path
        retBuf.startElement(
          NULL_NS_URI,
          "pathelement",
          "pathelement",
          new AttributesBuilder().add("location", "${dita.dir}${file.separator}" + resolvedValue).build()
        );
        retBuf.endElement(NULL_NS_URI, "pathelement", "pathelement");
      }
    }
  }
}
