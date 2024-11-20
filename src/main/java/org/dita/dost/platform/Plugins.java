/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import static org.dita.dost.util.Constants.PLUGIN_CONF;
import static org.dita.dost.util.XMLUtils.toList;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class Plugins {

  private DITAOTLogger logger;
  private File ditaDir;

  /**
   * Read the list of installed plugins
   */
  public static List<Map.Entry<String, String>> getInstalledPlugins() {
    final List<Element> plugins = toList(getPluginConfiguration().getElementsByTagName("plugin"));
    return plugins
      .stream()
      .map((Element elem) ->
        new AbstractMap.SimpleImmutableEntry<>(
          Optional.ofNullable(elem.getAttributeNode("id")).map(Attr::getValue).orElse(null),
          Optional.ofNullable(elem.getAttributeNode("version")).map(Attr::getValue).orElse(null)
        )
      )
      .filter(entry -> Objects.nonNull(entry.getKey()))
      .sorted(Map.Entry.comparingByKey())
      .collect(Collectors.toList());
  }

  /**
   * Read plugin configuration
   */
  public static Document getPluginConfiguration() {
    try (final InputStream in = Plugins.class.getClassLoader().getResourceAsStream(PLUGIN_CONF)) {
      final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      documentBuilderFactory.setNamespaceAware(true);
      return documentBuilderFactory.newDocumentBuilder().parse(in);
    } catch (final ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException("Failed to read plugin configuration: " + e.getMessage(), e);
    }
  }

  public List<Plugin> getPlugins() {
    PluginParser parser = new PluginParser(ditaDir);
    parser.setLogger(logger);

    final Document doc = getPluginConfiguration();
    final URI pluginsFile = ditaDir.toURI().resolve("config/plugins.xml");
    final List<Element> plugins = XMLUtils.toList(doc.getElementsByTagName("plugin"));
    return plugins
      .stream()
      .map(plugin -> {
        final String base = plugin.getAttributeNS(XMLConstants.XML_NS_URI, "base");
        final File pluginDir = new File(pluginsFile.resolve(base).resolve("."));
        parser.setPluginDir(pluginDir);
        parser.parse(plugin);
        return parser.getPlugin();
      })
      .toList();
  }

  // FIXME
  public Map<String, List<Value>> getFeatureTbl() {
    return Map.of();
  }

  // FIXME
  public final Map<String, Plugin> getPluginTable() {
    return Map.of();
  }

  public void setLogger(DITAOTLogger logger) {
    this.logger = logger;
  }

  public void setDitaDir(File ditaDir) {
    this.ditaDir = ditaDir;
  }
}
