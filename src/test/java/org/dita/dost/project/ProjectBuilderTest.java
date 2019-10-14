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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class ProjectBuilderTest {

    @Parameters(name = "{0}")
    public static Collection<String[]> data() {
        return Arrays.asList(new String[][]{
                {"simple"}, {"common"}, {"product"}, {"root"}, {"minimal"}, {"multiple"}
        });
    }

    private final String name;
    private final ObjectReader jsonReader = new ObjectMapper()
            .readerFor(ProjectBuilder.class)
            .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
    private final ObjectReader yamlReader = new YAMLMapper()
            .readerFor(ProjectBuilder.class)
            .with(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

    public ProjectBuilderTest(final String name) {
        this.name = name;
    }

    @Test()
    public void deserializeJson() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/" + name + ".json")) {
            jsonReader.readValue(in);
        }
    }

    @Test
    public void deserializeYaml() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/" + name + ".yaml")) {
            yamlReader.readValue(in);
        }
    }
}
