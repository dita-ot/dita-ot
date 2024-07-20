/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import java.io.File;
import java.net.URI;
import org.dita.dost.platform.Value.PathValue;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ImportXSLAction class.
 *
 */
final class ImportXSLAction extends ImportAction {

  /**
   * get result.
   */
  @Override
  public void getResult(final ContentHandler buf) throws SAXException {
    for (final Value value : valueSet) {
      if (value instanceof PathValue pathValue) {
        final URI href = getHref(pathValue);
        buf.startElement(
          "http://www.w3.org/1999/XSL/Transform",
          "import",
          "xsl:import",
          new AttributesBuilder().add("href", href.toString()).build()
        );
        buf.endElement("http://www.w3.org/1999/XSL/Transform", "import", "xsl:import");
      } else {
        logger.error("XSLT import must be a file feature");
      }
    }
  }

  private URI getHref(final PathValue value) {
    final Plugin features = featureTable.get(value.pluginId());
    final URI pluginDir = features.pluginDir().toURI();
    final String path = value.baseDir() + File.separator + value.value();
    final URI templateFile = URLUtils.toFile(path).toURI().normalize();
    final URI template = pluginDir.relativize(templateFile);
    if (value.pluginId() == null || template.isAbsolute()) {
      final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
      return URLUtils.toURI(FileUtils.getRelativeUnixPath(templateFilePath, path));
    }
    return URI.create("plugin:" + value.pluginId() + ":" + template);
  }
}
