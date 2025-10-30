/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2006 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import static org.dita.dost.platform.PluginParser.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;
import org.dita.dost.util.FileUtils;
import org.w3c.dom.Element;

/**
 * Collection of features.
 * @author Zhang, Yuan Peng
 */
record Features(
  String pluginId,
  SemVer pluginVersion,
  File pluginDir,
  File ditaDir,
  List<ExtensionPoint> extensionPoints,
  Map<String, List<Value>> features,
  List<PluginRequirement> requiredPlugins,
  Map<String, String> metaTable,
  List<String> templates
)
  implements Plugin {
  public static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private String pluginId;
    private SemVer pluginVersion;
    private File pluginDir;
    private File ditaDir;
    private final List<ExtensionPoint> extensionPoints;
    private final Map<String, List<Value>> featureTable;
    private final List<PluginRequirement> requireList;
    private final Map<String, String> metaTable;
    private final List<String> templateList;

    /**
     * Constructor init pluginDir.
     */
    public Builder() {
      this.extensionPoints = new ArrayList<>();
      this.featureTable = new HashMap<>();
      this.requireList = new ArrayList<>();
      this.metaTable = new HashMap<>();
      this.templateList = new ArrayList<>();
    }

    public Features build() {
      return new Features(
        pluginId,
        pluginVersion,
        pluginDir,
        ditaDir,
        List.copyOf(extensionPoints),
        featureTable
          .entrySet()
          .stream()
          .collect(Collectors.toMap(Map.Entry::getKey, entry -> List.copyOf(entry.getValue()))),
        List.copyOf(requireList),
        Map.copyOf(metaTable),
        List.copyOf(templateList)
      );
    }

    /**
     * Follow OSGi symbolic name syntax rules:
     *
     * <pre>
     * digit         ::= [0..9]
     * alpha         ::= [a..zA..Z]
     * alphanum      ::= alpha | digit
     * token         ::= ( alphanum | '_' | '-' )+
     * symbolic-name ::= token('.'token)*
     * </pre>
     *
     * @param id plug-in ID
     */
    Builder setPluginId(final String id) {
      if (!ID_PATTERN.matcher(id).matches()) {
        throw new IllegalArgumentException("Plug-in ID '%s' doesn't follow syntax rules.".formatted(id));
      }
      this.pluginId = id;
      return this;
    }

    Builder addExtensionPoint(String extensionPointId, String name) {
      extensionPoints.add(new ExtensionPoint(extensionPointId, name));
      return this;
    }

    /**
     * Add feature to the feature table.
     *
     * @param featureId feature id
     * @param elem configuration element
     */
    Builder addFeature(final String featureId, final Element elem) {
      boolean isFile;
      String value = elem.getAttribute(FEATURE_FILE_ATTR);
      if (!value.isEmpty()) {
        isFile = true;
      } else {
        value = elem.getAttribute(FEATURE_VALUE_ATTR);
        isFile = FEATURE_TYPE_VALUE_FILE.equals(elem.getAttribute(FEATURE_TYPE_ATTR));
      }
      final StringTokenizer valueTokenizer = new StringTokenizer(value, Integrator.FEAT_VALUE_SEPARATOR);
      final List<Value> valueBuffer = new ArrayList<>();
      if (featureTable.containsKey(featureId)) {
        valueBuffer.addAll(featureTable.get(featureId));
      }
      while (valueTokenizer.hasMoreElements()) {
        final String valueElement = valueTokenizer.nextToken();
        if (valueElement != null && valueElement.trim().length() != 0) {
          if (isFile && !FileUtils.isAbsolutePath(valueElement)) {
            if (featureId.equals("ant.import")) {
              valueBuffer.add(new Value.PathValue(this.pluginId, pluginDir, valueElement.trim()));
            } else {
              valueBuffer.add(new Value.PathValue(this.pluginId, pluginDir, valueElement.trim()));
            }
          } else {
            if (featureId.equals("package.version")) {
              setPluginVersion(valueElement.trim());
            }
            valueBuffer.add(new Value.StringValue(this.pluginId, valueElement.trim()));
          }
        }
      }

      featureTable.put(featureId, valueBuffer);
      return this;
    }

    /**
     * Add plugin-verion.
     * @param pluginVersion plug-in version
     */
    public Builder setPluginVersion(String pluginVersion) {
      this.pluginVersion = new SemVer(pluginVersion);
      return this;
    }

    /**
     * Add the required feature id.
     * @param id feature id
     */
    Builder addRequire(final String id) {
      final PluginRequirement.Builder requirement = PluginRequirement.builder().addPlugins(id);
      requireList.add(requirement.build());
      return this;
    }

    /**
     * Add the required feature id.
     * @param id feature id
     * @param importance importance
     */
    Builder addRequire(final String id, final String importance) {
      final PluginRequirement.Builder requirement = PluginRequirement.builder();
      requirement.addPlugins(id);
      if (importance != null) {
        requirement.setRequired(importance.equals(REQUIRE_IMPORTANCE_VALUE_REQUIRED));
      }
      requireList.add(requirement.build());
      return this;
    }

    /**
     * Add meta info to meta table.
     * @param type type
     * @param value value
     */
    Builder addMeta(final String type, final String value) {
      metaTable.put(type, Objects.requireNonNull(value));
      return this;
    }

    /**
     * Add a template.
     * @param file file name
     */
    Builder addTemplate(final String file) {
      templateList.add(file);
      return this;
    }

    public Builder setDitaDir(File ditaDir) {
      if (!ditaDir.isAbsolute()) {
        throw new IllegalArgumentException("Installation base directory must be absolute: " + pluginDir.toString());
      }
      this.ditaDir = ditaDir;
      return this;
    }

    public Builder setPluginDir(File pluginDir) {
      if (!pluginDir.isAbsolute()) {
        throw new IllegalArgumentException("Plug-in directory must be absolute: " + pluginDir.toString());
      }
      this.pluginDir = pluginDir;
      return this;
    }
  }
}
