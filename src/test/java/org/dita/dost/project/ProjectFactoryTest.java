/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import org.dita.dost.project.Project.Context;
import org.dita.dost.project.Project.Deliverable;
import org.dita.dost.project.Project.Publication;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

public class ProjectFactoryTest {

    private ProjectFactory factory;

    @Before
    public void setUp() {
        factory = ProjectFactory.getInstance();
    }

    @Test
    public void resolveReferences_deliverable() {
        final Project src = new Project(
                singletonList(
                        new Deliverable(
                                null,
                                null,
                                null,
                                null,
                                new Publication(
                                        null,
                                        null,
                                        "id",
                                        null,
                                        null)
                        )
                ),
                null,
                singletonList(
                        new Publication(
                                null,
                                "id",
                                null,
                                null,
                                null)
                ),
                null
        );
        ProjectFactory.resolveReferences(src);
    }

    @Test
    public void resolveReferences_context() {
        final Project src = new Project(
                singletonList(
                        new Deliverable(
                                null,
                                null,
                                new Context(
                                        null,
                                        null,
                                        "id",
                                        null,
                                        null),
                                null,
                                null
                        )
                ),
                null,
                null,
                singletonList(
                        new Context(
                                null,
                                "id",
                                null,
                                null,
                                null)
                )
        );
        ProjectFactory.resolveReferences(src);
    }

    @Test(expected = RuntimeException.class)
    public void resolveReferences_notFound() {
        final Project src = new Project(
                singletonList(
                        new Deliverable(
                                null,
                                null,
                                null,
                                null,
                                new Publication(
                                        null,
                                        null,
                                        "missing",
                                        null,
                                        null)
                        )
                ),
                null,
                Collections.emptyList(),
                null
        );
        final Project act = ProjectFactory.resolveReferences(src);
        assertEquals("id", act.deliverables.get(0).publication.id);
    }


    @Test
    public void read() throws IOException, URISyntaxException, SAXException {
        final URI file = getClass().getClassLoader().getResource("org/dita/dost/project/simple.json").toURI();
        final Project project = factory.load(file);
        assertEquals(1, project.deliverables.size());
        assertTrue(project.deliverables.get(0).context.inputs.inputs.get(0).href.isAbsolute());
        assertTrue(project.includes.isEmpty());
    }

    @Test
    public void readMultiple() throws IOException, URISyntaxException, SAXException {
        final URI file = getClass().getClassLoader().getResource("org/dita/dost/project/multiple.json").toURI();
        final Project project = factory.load(file);
        assertEquals(1, project.deliverables.size());
        assertTrue(project.deliverables.get(0).context.inputs.inputs.get(0).href.isAbsolute());
        assertTrue(project.includes.isEmpty());
    }


    @Test
    public void deserializeJsonRoot() throws IOException, URISyntaxException, SAXException {
        final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/root.json").toURI();
        final Project project = factory.load(input);
        assertEquals(1, project.deliverables.size());
        assertEquals(2, project.includes.size());
    }

    @Test
    public void deserializeJsonProduct() throws IOException, URISyntaxException, SAXException {
        final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/product.json").toURI();
        final Project project = factory.load(input);
        assertEquals(1, project.deliverables.size());
        assertEquals(1, project.publications.size());
        assertEquals("common-sitePub2", project.deliverables.get(0).publication.id);
    }

    @Test(expected = RuntimeException.class)
    public void deserializeJsonRecursive() throws IOException, URISyntaxException, SAXException {
        final URI input = getClass().getClassLoader().getResource("org/dita/dost/project/recursive.json").toURI();
        factory.load(input);
    }

}