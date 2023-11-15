/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.platform;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dita.dost.platform.Registry.Dependency;
import org.junit.jupiter.api.Test;

public class PluginInstallTest {

  final PluginInstall registryTask = new PluginInstall();

  @Test
  public void matchingPlatformVersion() {
    assertFalse(registryTask.matchingPlatformVersion(createRegistry(">=2.5")));
    assertTrue(registryTask.matchingPlatformVersion(createRegistry(">=1.2")));
    assertTrue(registryTask.matchingPlatformVersion(createRegistry("1.2.3")));
  }

  private Registry createRegistry(String version) {
    return new Registry(null, "1.0.0", new Dependency[] { new Dependency("org.dita.base", version) }, null, null);
  }
}
