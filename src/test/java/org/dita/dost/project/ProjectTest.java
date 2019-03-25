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
import org.dita.dost.project.Project.Deliverable.Context;
import org.dita.dost.project.Project.Deliverable.Inputs;
import org.dita.dost.project.Project.Deliverable.Inputs.Input;
import org.dita.dost.project.Project.Deliverable.Profile;
import org.dita.dost.project.Project.Deliverable.Profile.DitaVal;
import org.dita.dost.project.Project.Deliverable.Publication;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ProjectTest {

    @Test
    public void read() throws IOException, URISyntaxException {
        final URI file = getClass().getClassLoader().getResource("org/dita/dost/project/simple.json").toURI();
        final Project project = ProjectFactory.load(file);
        assertEquals(1, project.deliverables.size());
        assertNull(project.includes);
    }

    @Test
    public void deserializeXmlSimple() throws IOException {
        final ObjectMapper xmlMapper = new XmlMapper();
        final InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/simple.xml");
        final Project project = xmlMapper.readValue(in, Project.class);
        assertNotNull(project.deliverables);
        assertNull(project.includes);
    }

    @Test
    public void deserializeJsonSimple() throws IOException {
        final ObjectMapper xmlMapper = new ObjectMapper();
        final InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/simple.json");
        final Project project = xmlMapper.readValue(in, Project.class);
        assertNotNull(project.deliverables);
        assertNull(project.includes);
    }

    @Test
    public void deserializeJsonRoot() throws IOException, URISyntaxException {
        final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/root.json").toURI();
        final Project project = ProjectFactory.load(input);
        assertEquals(2, project.deliverables.size());
        assertEquals(2, project.includes.size());
    }

    @Test(expected = RuntimeException.class)
    public void deserializeJsonRecursive() throws IOException, URISyntaxException {
        final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/recursive.json").toURI();
        ProjectFactory.load(input);
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
    public void serializeJsonRoot() throws IOException {
        final ObjectMapper xmlMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        final Project project = new Project(null, Arrays.asList(new Project.ProjectRef(URI.create("simple.json"))));
        xmlMapper.writeValueAsString(project);
    }

    private Project getProject() {
        return new Project(Arrays.asList(new Project.Deliverable(
                "name",
                new Context("Site", "site",
                        new Inputs(//"inputs-name",
//                        "inputs-ref",
                                Arrays.asList(new Input(URI.create("site.ditamap")))),
                        new Profile(//"profile-name",
//                        "profile-ref",
                                Arrays.asList(new DitaVal(URI.create("site.ditaval"))))
                ),
                URI.create("./site"),
                new Publication("Site", "site", "html5", Arrays.asList(
                        new Publication.Param("args.gen.task.lbl", "YES", null),
                        new Publication.Param("args.rellinks", "noparent", null)
                ))
        )), null);
    }
}
