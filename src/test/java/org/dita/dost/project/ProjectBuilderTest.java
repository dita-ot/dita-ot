/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Deliverable.Inputs;
import org.dita.dost.project.Project.Deliverable.Inputs.Input;
import org.dita.dost.project.Project.Deliverable.Profile;
import org.dita.dost.project.Project.Deliverable.Profile.DitaVal;
import org.dita.dost.project.Project.Publication;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProjectBuilderTest {

    private final ObjectReader jsonReader = new ObjectMapper().readerFor(ProjectBuilder.class);

    @Test
    public void deserializeJsonSimple() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/simple.json")) {
            final ProjectBuilder project = jsonReader.readValue(in);
            assertEquals(1, project.deliverables.size());
            assertNull(project.includes);
        }
    }

    @Test
    public void deserializeJsonCommon() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/common.json")) {
            final ProjectBuilder project = jsonReader.readValue(in);
            assertNull(project.deliverables);
            assertNull(project.includes);
            assertEquals(1, project.contexts.size());
            assertEquals(1, project.publications.size());
        }
    }

    @Test
    public void deserializeJsonProduct() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/product.json")) {
            final ProjectBuilder project = jsonReader.readValue(input);
            assertEquals(1, project.deliverables.size());
            assertNull(project.publications);
            assertEquals("common-sitePub2", project.deliverables.get(0).publication.idref);
        }
    }
}
