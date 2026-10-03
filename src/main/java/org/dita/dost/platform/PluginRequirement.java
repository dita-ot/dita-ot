/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005, 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */

package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * PluginRequirement class.
 */
public record PluginRequirement(List<String> plugins, boolean required) {
  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private static final String REQUIREMENT_SEPARATOR = "|";

    private final ArrayList<String> plugins;
    private boolean required;

    /**
     * Constructor.
     */
    private Builder() {
      plugins = new ArrayList<>();
      required = true;
    }

    PluginRequirement build() {
      return new PluginRequirement(plugins, required);
    }

    /**
     * Add plugins.
     *
     * @param s plugins name
     */
    Builder addPlugins(final String s) {
      final StringTokenizer t = new StringTokenizer(s, REQUIREMENT_SEPARATOR);
      while (t.hasMoreTokens()) {
        plugins.add(t.nextToken());
      }
      return this;
    }

    /**
     * Set require.
     *
     * @param r require
     */
    Builder setRequired(final boolean r) {
      required = r;
      return this;
    }
  }
}
