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

class InstallArgumentsTest {

  private InstallArguments arguments;

  @BeforeEach
  void setUp() {
    arguments = new InstallArguments();
  }

  @Test
  void install() {
    arguments.parse(new String[] { "install", "org.dita.html5" });

    assertEquals("org.dita.html5", arguments.installFile);
  }

  @Test
  void force() {
    arguments.parse(new String[] { "install", "org.dita.html5", "--force" });

    assertEquals("org.dita.html5", arguments.installFile);
    assertEquals("true", arguments.definedProps.get("force"));
  }

  @Test
  void unsupportedOrder() {
    assertThrows(CliException.class, () -> arguments.parse(new String[] { "install", "--force", "org.dita.html5" }));
  }
}
