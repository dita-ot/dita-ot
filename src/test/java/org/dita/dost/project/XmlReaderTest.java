/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.project;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

public class XmlReaderTest {

    private final XmlReader xmlReader = new XmlReader();

    @Test
    public void deserializeXmlSimple() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/simple.xml")) {
            final Project project = xmlReader.read(in);
            assertEquals(1, project.deliverables.size());
            assertTrue(project.includes.isEmpty());
        }
    }

    @Test
    public void deserializeXmlCommon() throws IOException {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/common.xml")) {
            final Project project = xmlReader.read(in);
            assertTrue(project.deliverables.isEmpty());
            assertTrue(project.includes.isEmpty());
            assertEquals(1, project.contexts.size());
            assertEquals(1, project.publications.size());
        }
    }

    @Test
    public void deserializeXmlProduct() throws IOException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("org/dita/dost/project/product.xml")) {
            final Project project = xmlReader.read(input);
            assertEquals(1, project.deliverables.size());
            assertEquals(0, project.publications.size());
            assertEquals("common-sitePub2", project.deliverables.get(0).publication.idref);
        }
    }
}
