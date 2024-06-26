/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UninstallArgumentsTest {

  private UninstallArguments arguments;

  @BeforeEach
  void setUp() {
    arguments = new UninstallArguments();
  }

  @Test
  void install() {
    arguments.parse(new String[] { "uninstall", "org.dita.html5" });

    assertEquals("org.dita.html5", arguments.uninstallId);
  }

  @Test
  void unsupportedOrder() {
    assertThrows(CliException.class, () -> arguments.parse(new String[] { "install", "-v", "org.dita.html5" }));
  }
}
