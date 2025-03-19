/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.chunk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

class ChunkUtilsTest {

  private final DocumentBuilder documentBuilder = new XMLUtils().getDocumentBuilder();

  @ParameterizedTest
  @ValueSource(
    strings = {
      "multiple-to-content.ditamap",
      "nested-to-content.ditamap",
      "top-level-to-content.ditamap",
      "top-level-by-topic.ditamap",
      "only-by-topic.ditamap",
    }
  )
  void isCompatible(String file) throws IOException, SAXException {
    try (var in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/chunk/ChunkUtilsTest/" + file)) {
      var doc = documentBuilder.parse(in);
      assertTrue(ChunkUtils.isCompatible(doc));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = { "select.ditamap", "to-navigation.ditamap", "nested-by-topic-inside-to-content.ditamap" })
  void isNotCompatible(String file) throws IOException, SAXException {
    try (var in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/chunk/ChunkUtilsTest/" + file)) {
      var doc = documentBuilder.parse(in);
      assertFalse(ChunkUtils.isCompatible(doc));
    }
  }
}
