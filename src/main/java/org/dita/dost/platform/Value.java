/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import java.io.File;

public sealed interface Value {
  String pluginId();
  String value();

  record StringValue(String pluginId, String value) implements Value {}

  record PathValue(String pluginId, File baseDir, String value) implements Value {}
}
