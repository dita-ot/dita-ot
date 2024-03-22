/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

public class PluginRequirementTest {

  @Test
  public void testAddPlugins() {
    final PluginRequirement.Builder pr = PluginRequirement.builder();
    pr.addPlugins("foo | bar | baz");
    try {
      pr.addPlugins(null);
      fail();
    } catch (final NullPointerException e) {}
  }

  @Test
  public void testSetRequired() {
    final PluginRequirement.Builder pr = PluginRequirement.builder();
    pr.setRequired(true);
    pr.setRequired(false);
  }

  @Test
  public void testGetPlugins() {
    final PluginRequirement.Builder pr = PluginRequirement.builder();
    pr.addPlugins("foo | bar | baz");

    PluginRequirement pluginRequirement = pr.build();
    final List<String> act = pluginRequirement.plugins();

    assertArrayEquals(new String[] { "foo ", " bar ", " baz" }, act.toArray(new String[0]));
  }

  @Test
  public void testGetRequired() {
    final PluginRequirement.Builder pr = PluginRequirement.builder();
    PluginRequirement pluginRequirement2 = pr.build();
    assertTrue(pluginRequirement2.required());
    pr.setRequired(true);
    PluginRequirement pluginRequirement1 = pr.build();
    assertTrue(pluginRequirement1.required());
    pr.setRequired(false);
    PluginRequirement pluginRequirement = pr.build();
    assertFalse(pluginRequirement.required());
  }
}
