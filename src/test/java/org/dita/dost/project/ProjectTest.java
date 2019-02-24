/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.collect.ImmutableMap;
import org.dita.dost.project.Project.Deliverable.Inputs;
import org.dita.dost.project.Project.Deliverable.Inputs.Input;
import org.dita.dost.project.Project.Deliverable.Profile;
import org.dita.dost.project.Project.Deliverable.Profile.DitaVal;
import org.dita.dost.project.Project.Deliverable.Publication;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;

public class ProjectTest {

    @Test
    public void deserializeXmlSimple() throws IOException {
        final ObjectMapper xmlMapper = new XmlMapper();
        final InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/simple.xml");
        xmlMapper.readValue(in, Project.class);
    }

    @Test
    public void deserializeJsonSimple() throws IOException {
        final ObjectMapper xmlMapper = new ObjectMapper();
        final InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/simple.json");
        xmlMapper.readValue(in, Project.class);
    }

    @Test
    public void serializeXmlSimple() throws IOException {
        final ObjectMapper xmlMapper = new XmlMapper().enable(SerializationFeature.INDENT_OUTPUT);
        final Project project = getProject();
        xmlMapper.writeValueAsString(project);
    }

    @Test
    public void serializeJsonSimple() throws IOException {
        final ObjectMapper xmlMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        final Project project = getProject();
        xmlMapper.writeValueAsString(project);
    }

    @Test
    public void getArgumentsSimple() {
        final Project project = getProject();
        final List<Map<String, String>> act = project.getArguments();
        final List<Map<String, String>> exp = Arrays.asList(ImmutableMap.of(
                "args.input", new File(".").toURI().resolve("site.ditamap").toString(),
                "transtype", "html5"));
        assertEquals(exp, act);
    }

    private Project getProject() {
        return new Project(Arrays.asList(new Project.Deliverable(
                "name",
                new Inputs("inputs-name",
//                        "inputs-ref",
                        Arrays.asList(new Input(URI.create("site.ditamap")))),
                new Profile("profile-name",
//                        "profile-ref",
                        Arrays.asList(new DitaVal(URI.create("site.ditaval")))),
                new Publication("html5", Arrays.asList(
                        new Publication.Param("args.gen.task.lbl", "YES", null),
                        new Publication.Param("args.rellinks", "noparent", null)
                ))
        )));
    }
}
