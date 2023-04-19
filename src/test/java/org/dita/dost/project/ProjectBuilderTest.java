/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class ProjectBuilderTest {

  private final ObjectReader jsonReader = new ObjectMapper()
    .readerFor(ProjectBuilder.class)
    .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
  private final ObjectReader yamlReader = new YAMLMapper()
    .readerFor(ProjectBuilder.class)
    .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

  @ParameterizedTest
  @ValueSource(strings = { "simple", "common", "product", "root", "minimal", "multiple" })
  public void deserializeJson(final String name) throws IOException {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/" + name + ".json")) {
      jsonReader.readValue(in);
    }
  }

  @ParameterizedTest
  @ValueSource(strings = { "simple", "common", "product", "root", "minimal", "multiple" })
  public void deserializeYaml(final String name) throws IOException {
    try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/" + name + ".yaml")) {
      yamlReader.readValue(in);
    }
  }
}
