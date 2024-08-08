/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface Plugin {
  String pluginId();
  File pluginDir();
  File ditaDir();
  SemVer pluginVersion();
  /** Extension point by extension point id.  */
  Map<String, ExtensionPoint> extensionPoints();
  /** List of feature values by feature id. */
  Map<String, List<Value>> features();
  /** Required plug-ins. */
  List<PluginRequirement> requiredPlugins();
  Map<String, String> metaTable();
  List<String> templates();

  /**
   * Return the feature name by id.
   * @param id feature id
   * @return feature name
   */
  default List<Value> getFeature(final String id) {
    return features().get(id);
  }

  /**
   * Return meta info specifying type.
   * @param type type
   * @return meta info
   */
  default String getMeta(final String type) {
    return metaTable().get(type);
  }
}
