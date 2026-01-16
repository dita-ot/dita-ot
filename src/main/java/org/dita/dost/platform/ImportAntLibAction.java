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

import java.io.File;

import org.apache.commons.io.FileSystem;
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
    final String ditaDirPath = paramTable.get(FileGenerator.PARAM_DITA_DIR);
    for (final Value value : valueSet) {
      final String path;
      if (value instanceof Value.PathValue pathValue) {
        path = pathValue.getPath();
      } else {
        logger.error("Ant import must be a file feature: " + value.value());
        continue;
      }
      // Use DITA directory instead of template file path for correct relative path calculation
      final String basePath = ditaDirPath + File.separator + "dummy";
      
      final String resolvedValue = FileUtils.getRelativeUnixPath(basePath, path);

      retBuf.startElement(
        NULL_NS_URI,
        "pathelement",
        "pathelement",
        new AttributesBuilder().add("location", "${dita.dir}/" + resolvedValue).build()
      );
      retBuf.endElement(NULL_NS_URI, "pathelement", "pathelement");
    }
  }
}
