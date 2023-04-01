/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.store;

import java.io.File;
import org.dita.dost.util.XMLUtils;

public interface StoreBuilder {
  String getType();

  StoreBuilder setTempDir(File tempDir);

  StoreBuilder setXmlUtils(XMLUtils xmlUtils);

  Store build();
}
