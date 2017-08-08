/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.apache.commons.io.FileUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.dita.dost.util.Constants.FEATURE_VALIDATION;
import static org.dita.dost.util.Constants.FEATURE_VALIDATION_SCHEMA;
import static org.junit.Assert.*;

public class TestGenListModuleReader {

    private static final File baseDir = TestUtils.getResourceDir(TestGenListModuleReader.class);
    private static final File srcDir = new File(baseDir, "src");
    private static final URI srcDirUri = srcDir.toURI();
    private static final File inputDir = new File(srcDir, "maps");
    private static File tempDir;

    private GenListModuleReader reader;
    private XMLReader parser;

    @BeforeClass
    public static void setUpClass() throws Exception {
        tempDir = TestUtils.createTempDir(TestGenListModuleReader.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        FileUtils.deleteQuietly(tempDir);
    }

    @Test
    public void testParse() throws Exception {
        final File rootFile = new File(inputDir, "root-map-01.ditamap");
        run(rootFile);

        assertTrue(reader.getConrefTargets().isEmpty());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                reader.getHrefTargets());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                reader.getNonConrefCopytoTargets());

        assertEquals(new HashSet(Arrays.asList(
                new Reference(srcDirUri.resolve("topics/xreffin-topic-1.xml")),
                new Reference(srcDirUri.resolve("topics/target-topic-c.xml")),
                new Reference(srcDirUri.resolve("topics/target-topic-a.xml")))),
                reader.getNonCopytoResult());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                reader.getOutDitaFilesSet());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                reader.getOutFilesSet());

        assertEquals(emptySet(),
                reader.getNonTopicrefReferenceSet());

        assertTrue(reader.getResourceOnlySet().isEmpty());

        assertTrue(reader.getCoderefTargets().isEmpty());

        assertFalse(reader.isDitaTopic());
        assertTrue(reader.isDitaMap());
        assertFalse(reader.hasCodeRef());
        assertFalse(reader.hasConaction());
        assertFalse(reader.hasConRef());
        assertTrue(reader.hasHref());
        assertTrue(reader.hasKeyRef());
    }

    @Test
    public void testChunkParse() throws Exception {
        final File rootFile = new File(inputDir, "Manual.ditamap");
        run(rootFile);

        assertTrue(reader.getConrefTargets().isEmpty());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("maps/toolbars.dita"))),
                reader.getHrefTargets());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("maps/toolbars.dita"))),
                reader.getNonConrefCopytoTargets());

        assertEquals(new HashSet(Arrays.asList(
                new Reference(srcDirUri.resolve("maps/toolbars.dita")))),
                reader.getNonCopytoResult());

        assertTrue(reader.getOutDitaFilesSet().isEmpty());

        assertTrue(reader.getOutFilesSet().isEmpty());

        assertTrue(reader.getResourceOnlySet().isEmpty());

        assertTrue(reader.getCoderefTargets().isEmpty());

        assertEquals(emptySet(),
                reader.getNonTopicrefReferenceSet());

        assertFalse(reader.isDitaTopic());
        assertTrue(reader.isDitaMap());
        assertFalse(reader.hasCodeRef());
        assertFalse(reader.hasConaction());
        assertFalse(reader.hasConRef());
        assertTrue(reader.hasHref());
        assertFalse(reader.hasKeyRef());
    }

    @Test
    public void testConrefParse() throws Exception {
        final File conrefDir = new File(srcDir, "conref");
        final URI conrefDirUri = conrefDir.toURI();
        final File rootFile = new File(conrefDir, "main.ditamap");
        run(rootFile);

        assertTrue(reader.getConrefTargets().isEmpty());

        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "normal.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(conrefDirUri::resolve)
                        .collect(Collectors.toSet()),
                reader.getHrefTargets());

        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "normal.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(conrefDirUri::resolve)
                        .collect(Collectors.toSet()),
                reader.getNonConrefCopytoTargets());

        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "normal.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(f -> new Reference(conrefDirUri.resolve(f)))
                        .collect(Collectors.toSet()),
                reader.getNonCopytoResult());

        assertEquals(emptySet(),
                reader.getOutDitaFilesSet());

        assertEquals(emptySet(),
                reader.getOutFilesSet());

        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(conrefDirUri::resolve)
                        .collect(Collectors.toSet()),
                reader.getResourceOnlySet());

        assertTrue(reader.getCoderefTargets().isEmpty());

        assertEquals(emptySet(),
                reader.getNonTopicrefReferenceSet());

        assertFalse(reader.isDitaTopic());
        assertTrue(reader.isDitaMap());
        assertFalse(reader.hasCodeRef());
        assertFalse(reader.hasConaction());
        assertFalse(reader.hasConRef());
        assertTrue(reader.hasHref());
        assertFalse(reader.hasKeyRef());
    }

    private void run(final File rootFile) throws Exception {
        final File ditaDir = new File("src" + File.separator + "main").getAbsoluteFile();

        final boolean validate = false;
        reader = new GenListModuleReader();
        reader.setLogger(new TestUtils.TestLogger());
        reader.setCurrentFile(rootFile.toURI());
        reader.setPrimaryDitamap(rootFile.toURI());
        reader.setJob(new Job(tempDir));

        reader.setContentHandler(new DefaultHandler());

        final XMLReader parser = initXMLReader(ditaDir, validate, new File(rootFile.getPath()).getCanonicalFile());
        parser.setContentHandler(reader);

        parser.parse(rootFile.toURI().toString());
    }

    private XMLReader initXMLReader(final File ditaDir, final boolean validate, final File rootFile) throws SAXException, IOException {
        final XMLReader parser = XMLUtils.getXMLReader();
        if (validate == true) {
            parser.setFeature(FEATURE_VALIDATION, true);
            try {
                parser.setFeature(FEATURE_VALIDATION_SCHEMA, true);
            } catch (final SAXNotRecognizedException e) {
                // Not Xerces, ignore exception
            }
        } else {
            final String msg = MessageUtils.getMessage("DOTJ037W").toString();
        }
        CatalogUtils.setDitaDir(ditaDir);
        parser.setEntityResolver(CatalogUtils.getCatalogResolver());

        return parser;
    }

}
