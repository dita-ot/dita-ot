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
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.AfterClass;
import org.junit.Before;
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
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.dita.dost.util.Constants.FEATURE_VALIDATION;
import static org.dita.dost.util.Constants.FEATURE_VALIDATION_SCHEMA;
import static org.dita.dost.util.URLUtils.stripFragment;
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

    @Before
    public void setUp() throws IOException {
        reader = new GenListModuleReader();
        reader.setLogger(new TestUtils.TestLogger());
        reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        reader.setContentHandler(new DefaultHandler());
        final URI currentFile = new File(inputDir, "root-map-01.ditamap").toURI();
        reader.setCurrentFile(currentFile);
        reader.setPrimaryDitamap(currentFile);
    }

    @AfterClass
    public static void tearDownClass() {
        FileUtils.deleteQuietly(tempDir);
    }

    @Test
    public void startDocument() throws SAXException {
        reader.startDocument();
    }

    @Test
    public void startElement() throws SAXException {
        reader.startDocument();
        reader.startElement("", "topic", "topic", new AttributesBuilder()
                .add("class", "- topic/topic ")
                .add("id", "abc")
                .build());
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

        final Set<URI> nonConrefCopytoTargets = reader.getNonConrefCopytoTargets().stream()
                .map(r -> r.filename)
                .collect(Collectors.toSet());
        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                nonConrefCopytoTargets);

        final Set<Reference> nonCopytoResult = new LinkedHashSet<>(128);
        nonCopytoResult.addAll(reader.getNonConrefCopytoTargets());
        for (final URI f : reader.getConrefTargets()) {
            nonCopytoResult.add(new Reference(stripFragment(f), reader.currentFileFormat()));
        }
        for (final URI f : reader.getCopytoMap().values()) {
            nonCopytoResult.add(new Reference(stripFragment(f)));
        }
        for (final URI f : reader.getIgnoredCopytoSourceSet()) {
            nonCopytoResult.add(new Reference(stripFragment(f)));
        }
        for (final URI filename : reader.getCoderefTargetSet()) {
            nonCopytoResult.add(new Reference(stripFragment(filename)));
        }
        assertEquals(new HashSet(Arrays.asList(
                new Reference(srcDirUri.resolve("topics/xreffin-topic-1.xml")),
                new Reference(srcDirUri.resolve("topics/target-topic-c.xml")),
                new Reference(srcDirUri.resolve("topics/target-topic-a.xml")))),
                nonCopytoResult);

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                reader.getOutDitaFilesSet());

        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("topics/xreffin-topic-1.xml"),
                srcDirUri.resolve("topics/target-topic-c.xml"),
                srcDirUri.resolve("topics/target-topic-a.xml"))),
                reader.getOutDitaFilesSet());

        final Set<URI> nonTopicrefReferenceSet = new HashSet<>(reader.getNonTopicrefReferenceSet());
        nonTopicrefReferenceSet.removeAll(reader.getNormalProcessingRoleSet());
        nonTopicrefReferenceSet.removeAll(reader.getResourceOnlySet());
        assertEquals(emptySet(),
                nonTopicrefReferenceSet);

        final Set<URI> resourceOnlySet = new HashSet<>(reader.getResourceOnlySet());
        resourceOnlySet.removeAll(reader.getNormalProcessingRoleSet());
        assertTrue(resourceOnlySet.isEmpty());

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

        final Set<URI> nonConrefCopytoTargets = reader.getNonConrefCopytoTargets().stream()
                .map(r -> r.filename)
                .collect(Collectors.toSet());
        assertEquals(new HashSet(Arrays.asList(
                srcDirUri.resolve("maps/toolbars.dita"))),
                nonConrefCopytoTargets);

        final Set<Reference> nonCopytoResult_computed = new LinkedHashSet<>(128);
        nonCopytoResult_computed.addAll(reader.getNonConrefCopytoTargets());
        for (final URI f : reader.getConrefTargets()) {
            nonCopytoResult_computed.add(new Reference(stripFragment(f), reader.currentFileFormat()));
        }
        for (final URI f : reader.getCopytoMap().values()) {
            nonCopytoResult_computed.add(new Reference(stripFragment(f)));
        }
        for (final URI f : reader.getIgnoredCopytoSourceSet()) {
            nonCopytoResult_computed.add(new Reference(stripFragment(f)));
        }
        for (final URI filename : reader.getCoderefTargetSet()) {
            nonCopytoResult_computed.add(new Reference(stripFragment(filename)));
        }
        assertEquals(new HashSet(Arrays.asList(
                new Reference(srcDirUri.resolve("maps/toolbars.dita")))),
                nonCopytoResult_computed);

        assertTrue(reader.getOutDitaFilesSet().isEmpty());

        assertTrue(reader.getOutDitaFilesSet().isEmpty());

        final Set<URI> resourceOnlySet = new HashSet<>(reader.getResourceOnlySet());
        resourceOnlySet.removeAll(reader.getNormalProcessingRoleSet());
        assertTrue(resourceOnlySet.isEmpty());

        assertTrue(reader.getCoderefTargets().isEmpty());

        final Set<URI> nonTopicrefReferenceSet = new HashSet<>(reader.getNonTopicrefReferenceSet());
        nonTopicrefReferenceSet.removeAll(reader.getNormalProcessingRoleSet());
        nonTopicrefReferenceSet.removeAll(reader.getResourceOnlySet());
        assertEquals(emptySet(),
                nonTopicrefReferenceSet);

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

        final Set<URI> nonConrefCopytoTargets = reader.getNonConrefCopytoTargets().stream()
                .map(r -> r.filename)
                .collect(Collectors.toSet());
        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "normal.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(conrefDirUri::resolve)
                        .collect(Collectors.toSet()),
                nonConrefCopytoTargets);

        final Set<Reference> nonCopytoResult = new LinkedHashSet<>(128);
        nonCopytoResult.addAll(reader.getNonConrefCopytoTargets());
        for (final URI f1 : reader.getConrefTargets()) {
            nonCopytoResult.add(new Reference(stripFragment(f1), reader.currentFileFormat()));
        }
        for (final URI f1 : reader.getCopytoMap().values()) {
            nonCopytoResult.add(new Reference(stripFragment(f1)));
        }
        for (final URI f1 : reader.getIgnoredCopytoSourceSet()) {
            nonCopytoResult.add(new Reference(stripFragment(f1)));
        }
        for (final URI filename : reader.getCoderefTargetSet()) {
            nonCopytoResult.add(new Reference(stripFragment(filename)));
        }
        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "normal.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(f -> new Reference(conrefDirUri.resolve(f)))
                        .collect(Collectors.toSet()),
                nonCopytoResult);

        assertEquals(emptySet(), reader.getOutDitaFilesSet());

        final Set<URI> resourceOnlySet = new HashSet<>(reader.getResourceOnlySet());
        resourceOnlySet.removeAll(reader.getNormalProcessingRoleSet());
        assertEquals(Stream.of(
                "resourceonly.dita",
                "link-from-resource-only-ALSORESOURCEONLY.dita",
                "link-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-normal-ALSORESOURCEONLY.dita",
                "conref-from-resource-only-ALSORESOURCEONLY.dita")
                        .map(conrefDirUri::resolve)
                        .collect(Collectors.toSet()),
                resourceOnlySet);

        assertTrue(reader.getCoderefTargets().isEmpty());

        final Set<URI> nonTopicrefReferenceSet = new HashSet<>(reader.getNonTopicrefReferenceSet());
        nonTopicrefReferenceSet.removeAll(reader.getNormalProcessingRoleSet());
        nonTopicrefReferenceSet.removeAll(reader.getResourceOnlySet());
        assertEquals(emptySet(),
                nonTopicrefReferenceSet);

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
        reader.setCurrentFile(rootFile.toURI());
        reader.setPrimaryDitamap(rootFile.toURI());

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
