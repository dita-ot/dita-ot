/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.XMLUtils.getChildElements;
import static org.dita.dost.util.XMLUtils.toList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.log.DITAOTLogger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Parse plugin configuration file.
 */
public class PluginParser {

  private static final String EXTENSION_POINT_ELEM = "extension-point";
  private static final String EXTENSION_POINT_NAME_ATTR = "name";
  private static final String EXTENSION_POINT_ID_ATTR = "id";
  private static final String TEMPLATE_ELEM = "template";
  private static final String TEMPLATE_FILE_ATTR = "file";
  private static final String META_ELEM = "meta";
  private static final String META_VALUE_ATTR = "value";
  private static final String META_TYPE_ATTR = "type";
  private static final String REQUIRE_ELEM = "require";
  private static final String REQUIRE_IMPORTANCE_ATTR = "importance";
  static final String REQUIRE_IMPORTANCE_VALUE_REQUIRED = "required";
  private static final String REQUIRE_PLUGIN_ATTR = "plugin";
  public static final String FEATURE_ELEM = "feature";
  private static final String TRANSTYPE_ELEM = "transtype";
  private static final String TRANSTYPE_ABSTRACT_ATTR = "abstract";
  private static final String TRANSTYPE_NAME_ATTR = "name";
  public static final String FEATURE_ID_ATTR = "extension";
  public static final String FEATURE_VALUE_ATTR = "value";
  public static final String FEATURE_FILE_ATTR = "file";
  public static final String FEATURE_TYPE_ATTR = "type";
  public static final String FEATURE_TYPE_VALUE_FILE = "file";
  public static final String PLUGIN_ELEM = REQUIRE_PLUGIN_ATTR;
  private static final String PLUGIN_ID_ATTR = "id";
  private static final String PLUGIN_VERSION_ATTR = "version";

  public static final Pattern ID_PATTERN = Pattern.compile("[0-9a-zA-Z_\\-]+(?:\\.[0-9a-zA-Z_\\-]+)*");
  public static final Pattern VERSION_PATTERN = Pattern.compile("\\d+(?:\\.\\d+(?:\\.\\d+(?:\\.[0-9a-zA-Z_\\-]+)?)?)?");

  private final File ditaDir;
  private final DocumentBuilder builder;
  private File pluginDir;
  private Features.Builder features;
  private DITAOTLogger logger;

  /**
   * Constructor initialize Feature with location.
   * @param ditaDir absolute DITA-OT base directory
   */
  public PluginParser(final File ditaDir) {
    super();
    assert ditaDir.isAbsolute();
    this.ditaDir = ditaDir;
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newDefaultInstance();
    factory.setNamespaceAware(true);
    try {
      builder = factory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public void setLogger(final DITAOTLogger logger) {
    this.logger = logger;
  }

  /**
   * Set plug-in directory path.
   *
   * @param pluginDir absolute plug-in directory path
   * */
  public void setPluginDir(final File pluginDir) {
    assert pluginDir.isAbsolute();
    this.pluginDir = pluginDir;
  }

  /**
   * Get plug-in features.
   *
   * @return plug-in features
   */
  public Plugin getPlugin() {
    return features.build();
  }

  /**
   * Parse plug-in configuration file.
   */
  public Element parse(final File file) throws Exception {
    final Document doc;
    try {
      doc = builder.parse(file);
    } catch (final SAXException | IOException e) {
      throw new Exception("Failed to parse " + file + ": " + e.getMessage(), e);
    }
    final Element root = migrate(doc.getDocumentElement());

    final String pluginId = root.getAttribute(PLUGIN_ID_ATTR);
    features = Features.builder().setPluginDir(pluginDir).setDitaDir(ditaDir).setPluginId(pluginId);
    final Attr pluginVersion = root.getAttributeNode(PLUGIN_VERSION_ATTR);
    if (pluginVersion != null) {
      if (!VERSION_PATTERN.matcher(pluginVersion.getValue()).matches()) {
        logger.error("Plug-in version '%s' doesn't follow syntax rules.".formatted(pluginVersion.getValue()));
      } else {
        features.setPluginVersion(pluginVersion.getValue());
      }
    }
    for (Element elem : getChildElements(root)) {
      final String qName = elem.getTagName();
      if (EXTENSION_POINT_ELEM.equals(qName)) {
        addExtensionPoint(elem);
      } else if (TRANSTYPE_ELEM.equals(qName)) {
        addTranstype(elem);
      } else if (FEATURE_ELEM.equals(qName)) {
        features.addFeature(elem.getAttribute(FEATURE_ID_ATTR), elem);
      } else if (REQUIRE_ELEM.equals(qName)) {
        final String importance = elem.getAttribute(REQUIRE_IMPORTANCE_ATTR);
        features.addRequire(elem.getAttribute(REQUIRE_PLUGIN_ATTR), importance.isEmpty() ? null : importance);
      } else if (META_ELEM.equals(qName)) {
        features.addMeta(elem.getAttribute(META_TYPE_ATTR), elem.getAttribute(META_VALUE_ATTR));
      } else if (TEMPLATE_ELEM.equals(qName)) {
        features.addTemplate(elem.getAttribute(TEMPLATE_FILE_ATTR));
      }
    }

    return root;
  }

  /**
   * Migrate from deprecated plug-in configuration format to new configuration format.
   */
  private Element migrate(Element root) {
    if (root.getAttributeNode(PLUGIN_VERSION_ATTR) == null) {
      final List<Element> featureElems = toList(root.getElementsByTagName(FEATURE_ELEM));
      final String version = featureElems
        .stream()
        .filter(elem -> elem.getAttribute(FEATURE_ID_ATTR).equals("package.version"))
        .findFirst()
        .map(elem -> elem.getAttribute(FEATURE_VALUE_ATTR))
        .orElse("0.0.0");
      root.setAttribute(PLUGIN_VERSION_ATTR, version);
    }
    return root;
  }

  private void addTranstype(final Element elem) {
    if (!Boolean.parseBoolean(elem.getAttribute(TRANSTYPE_ABSTRACT_ATTR))) {
      final Document doc = elem.getOwnerDocument();
      for (final String transtype : elem.getAttribute(TRANSTYPE_NAME_ATTR).split("\\s+")) {
        final Element buf = doc.createElement(FEATURE_ELEM);
        buf.setAttribute(FEATURE_VALUE_ATTR, transtype);
        features.addFeature("dita.conductor.transtype.check", buf);
      }
    }
  }

  /**
   * Add extension point.
   *
   * @param elem extension point element attributes
   * @throws IllegalArgumentException if extension ID is {@code null}
   */
  private void addExtensionPoint(final Element elem) {
    final String id = elem.getAttribute(EXTENSION_POINT_ID_ATTR);
    if (id.isEmpty()) {
      throw new IllegalArgumentException(EXTENSION_POINT_ID_ATTR + " attribute not set on extension-point");
    }
    final String name = elem.getAttribute(EXTENSION_POINT_NAME_ATTR);
    features.addExtensionPoint(id, name);
  }
}
