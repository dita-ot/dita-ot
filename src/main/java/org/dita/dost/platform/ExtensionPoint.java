/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import java.util.Objects;

/**
 * Extension point.
 *
 * @param id extension point ID
 * @param name extension point name, may be {@code null}
 * @since 1.5.3
 * @author Jarno Elovirta
 */
record ExtensionPoint(String id, String name) {
  public ExtensionPoint {
    Objects.requireNonNull(id);
  }
}
