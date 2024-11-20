/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.UNIX_SEPARATOR;

import java.io.File;
import java.util.List;
import java.util.Map.Entry;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Integration action to output plugin information to Ant build.
 *
 * @author Jarno Elovirta
 * @since 1.5.3
 */
final class ImportPluginInfoAction extends ImportAction {

  public ImportPluginInfoAction() {
    super();
  }

  @Override
  public void getResult(final ContentHandler buf) throws SAXException {
    // plugin properties
    for (final Entry<String, Plugin> e : featureTable.entrySet()) {
      final Plugin f = e.getValue();
      final String name = "dita.plugin." + e.getKey() + ".dir";
      if (useClasspath) {
        buf.startElement(
          NULL_NS_URI,
          "property",
          "property",
          new AttributesBuilder().add("name", name).add("value", "classpath:/%s".formatted(e.getKey())).build()
        );
        buf.endElement(NULL_NS_URI, "property", "property");
      } else {
        final StringBuilder location = new StringBuilder();
        final List<Value> baseDirValues = f.getFeature("dita.basedir-resource-directory");
        if (
          Boolean.parseBoolean(baseDirValues == null || baseDirValues.isEmpty() ? null : baseDirValues.get(0).value())
        ) {
          location.append("${dita.dir}");
        } else if (f.pluginDir().getAbsolutePath().startsWith(f.ditaDir().getAbsolutePath())) {
          location
            .append("${dita.dir}")
            .append(UNIX_SEPARATOR)
            .append(
              FileUtils.getRelativeUnixPath(
                new File(f.ditaDir(), "plugin.xml").getAbsolutePath(),
                f.pluginDir().getAbsolutePath()
              )
            );
        } else {
          location.append(f.pluginDir().getAbsolutePath());
        }
        buf.startElement(
          NULL_NS_URI,
          "property",
          "property",
          new AttributesBuilder().add("name", name).add("location", location.toString()).build()
        );
        buf.endElement(NULL_NS_URI, "property", "property");
      }
    }
  }
}
