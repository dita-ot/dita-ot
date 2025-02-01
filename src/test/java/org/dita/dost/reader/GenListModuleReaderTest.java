/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class GenListModuleReaderTest {

  private static final File baseDir = TestUtils.getResourceDir(GenListModuleReaderTest.class);
  private static final File srcDir = new File(baseDir, "src");
  private static final URI srcDirUri = srcDir.toURI();
  private static final File inputDir = new File(srcDir, "maps");

  @TempDir
  private File tempDir;

  private GenListModuleReader reader;
  private Job job;

  @BeforeEach
  public void setUp() throws IOException {
    job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    reader = new GenListModuleReader();
    reader.setLogger(new TestUtils.TestLogger());
    reader.setJob(job);
    reader.setContentHandler(new DefaultHandler());
    final URI currentFile = new File(inputDir, "root-map-01.ditamap").toURI();
    reader.setCurrentFile(currentFile);
    reader.setPrimaryDitamap(currentFile);
  }

  @Test
  public void startDocument() throws SAXException {
    reader.startDocument();
  }

  @Test
  public void startElement() throws SAXException {
    reader.startDocument();
    reader.startElement(
      NULL_NS_URI,
      TOPIC_TOPIC.localName,
      TOPIC_TOPIC.localName,
      new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString()).add(ATTRIBUTE_NAME_ID, "abc").build()
    );
  }

  @Test
  public void startElement_localImage() throws SAXException {
    reader.startDocument();
    reader.startElement(
      NULL_NS_URI,
      TOPIC_IMAGE.localName,
      TOPIC_IMAGE.localName,
      new AttributesBuilder()
        .add(ATTRIBUTE_NAME_CLASS, TOPIC_IMAGE.toString())
        .add(ATTRIBUTE_NAME_HREF, "image.png")
        .build()
    );
    assertEquals(1, reader.getNonConrefCopytoTargets().size());
    assertEquals(ATTR_FORMAT_VALUE_IMAGE, reader.getNonConrefCopytoTargets().iterator().next().format);
    assertEquals(inputDir.toURI().resolve("image.png"), reader.getNonConrefCopytoTargets().iterator().next().filename);
    assertEquals(1, reader.getNonTopicrefReferenceSet().size());
    assertEquals(inputDir.toURI().resolve("image.png"), reader.getNonTopicrefReferenceSet().iterator().next());
  }

  @Test
  public void startElement_localImage_crawlMap() throws SAXException {
    job.setCrawl("map");

    reader.startDocument();
    reader.startElement(
      NULL_NS_URI,
      TOPIC_IMAGE.localName,
      TOPIC_IMAGE.localName,
      new AttributesBuilder()
        .add(ATTRIBUTE_NAME_CLASS, TOPIC_IMAGE.toString())
        .add(ATTRIBUTE_NAME_HREF, "image.png")
        .build()
    );
    assertEquals(1, reader.getNonConrefCopytoTargets().size());
    assertEquals(ATTR_FORMAT_VALUE_IMAGE, reader.getNonConrefCopytoTargets().iterator().next().format);
    assertEquals(inputDir.toURI().resolve("image.png"), reader.getNonConrefCopytoTargets().iterator().next().filename);
    assertEquals(0, reader.getNonTopicrefReferenceSet().size());
    assertFalse(reader.getNonTopicrefReferenceSet().iterator().hasNext());
  }

  @ParameterizedTest
  @MethodSource("startElement_xrefArguments")
  public void startElement_xref(
    String crawl,
    boolean onlyTopicInMap,
    Set<Reference> expNonConrefCopytoTargets,
    Set<URI> expNonTopicrefReferenceSet
  ) throws SAXException {
    job.setCrawl(crawl);
    job.setOnlyTopicInMap(onlyTopicInMap);

    reader.startDocument();
    reader.startElement(
      NULL_NS_URI,
      TOPIC_TOPIC.localName,
      TOPIC_TOPIC.localName,
      new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString()).add(ATTRIBUTE_NAME_ID, "topic").build()
    );
    reader.startElement(
      NULL_NS_URI,
      TOPIC_XREF.localName,
      TOPIC_XREF.localName,
      new AttributesBuilder()
        .add(ATTRIBUTE_NAME_CLASS, TOPIC_XREF.toString())
        .add(ATTRIBUTE_NAME_HREF, "topic.dita")
        .build()
    );

    assertEquals(expNonConrefCopytoTargets, reader.getNonConrefCopytoTargets());
    assertEquals(expNonTopicrefReferenceSet, reader.getNonTopicrefReferenceSet());
  }

  private static List<Arguments> startElement_xrefArguments() {
    return List.of(
      Arguments.of(
        "topic",
        false,
        Set.of(new Reference(inputDir.toURI().resolve("topic.dita"))),
        Set.of(inputDir.toURI().resolve("topic.dita"))
      ),
      Arguments.of("topic", true, Set.of(), Set.of(inputDir.toURI().resolve("topic.dita"))),
      Arguments.of("map", false, Set.of(), Set.of()),
      Arguments.of("map", true, Set.of(), Set.of())
    );
  }

  @ParameterizedTest
  @MethodSource("startElement_externalImageArguments")
  public void startElement_externalImage_withoutScope(String scheme, String scope) throws SAXException {
    reader.startDocument();
    final AttributesBuilder atts = new AttributesBuilder()
      .add(ATTRIBUTE_NAME_CLASS, TOPIC_IMAGE.toString())
      .add(ATTRIBUTE_NAME_HREF, scheme + "://example.com/image.png");
    if (scope != null) {
      atts.add(ATTRIBUTE_NAME_SCOPE, scope);
    }
    reader.startElement(NULL_NS_URI, TOPIC_IMAGE.localName, TOPIC_IMAGE.localName, atts.build());

    assertTrue(reader.getNonConrefCopytoTargets().isEmpty());
    assertTrue(reader.getNonTopicrefReferenceSet().isEmpty());
  }

  private static List<Arguments> startElement_externalImageArguments() {
    var res = new ArrayList<Arguments>();
    for (String scheme : new String[] { "http", "https", "ftp", "ftps", "sftp", "mailto" }) {
      res.add(Arguments.of(scheme, null));
    }
    res.add(Arguments.of("file", "external"));
    return res;
  }

  @Test
  public void testParse() throws Exception {
    final File rootFile = new File(inputDir, "root-map-01.ditamap");
    run(rootFile);

    assertTrue(reader.getConrefTargets().isEmpty());

    assertEquals(
      Set.of(
        srcDirUri.resolve("topics/xreffin-topic-1.xml"),
        srcDirUri.resolve("topics/target-topic-c.xml"),
        srcDirUri.resolve("topics/target-topic-a.xml")
      ),
      reader.getHrefTargets()
    );

    final Set<URI> nonConrefCopytoTargets = reader
      .getNonConrefCopytoTargets()
      .stream()
      .map(r -> r.filename)
      .collect(Collectors.toSet());
    assertEquals(
      Set.of(
        srcDirUri.resolve("topics/xreffin-topic-1.xml"),
        srcDirUri.resolve("topics/target-topic-c.xml"),
        srcDirUri.resolve("topics/target-topic-a.xml")
      ),
      nonConrefCopytoTargets
    );

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
    assertEquals(
      Set.of(
        new Reference(srcDirUri.resolve("topics/xreffin-topic-1.xml")),
        new Reference(srcDirUri.resolve("topics/target-topic-c.xml")),
        new Reference(srcDirUri.resolve("topics/target-topic-a.xml"))
      ),
      nonCopytoResult
    );

    assertEquals(
      Set.of(
        srcDirUri.resolve("topics/xreffin-topic-1.xml"),
        srcDirUri.resolve("topics/target-topic-c.xml"),
        srcDirUri.resolve("topics/target-topic-a.xml")
      ),
      reader.getOutDitaFilesSet()
    );

    assertEquals(
      Set.of(
        srcDirUri.resolve("topics/xreffin-topic-1.xml"),
        srcDirUri.resolve("topics/target-topic-c.xml"),
        srcDirUri.resolve("topics/target-topic-a.xml")
      ),
      reader.getOutDitaFilesSet()
    );

    final Set<URI> nonTopicrefReferenceSet = new HashSet<>(reader.getNonTopicrefReferenceSet());
    nonTopicrefReferenceSet.removeAll(reader.getNormalProcessingRoleSet());
    nonTopicrefReferenceSet.removeAll(reader.getResourceOnlySet());
    assertEquals(Set.of(), nonTopicrefReferenceSet);

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

    assertEquals(Set.of(srcDirUri.resolve("maps/toolbars.dita")), reader.getHrefTargets());

    final Set<URI> nonConrefCopytoTargets = reader
      .getNonConrefCopytoTargets()
      .stream()
      .map(r -> r.filename)
      .collect(Collectors.toSet());
    assertEquals(Set.of(srcDirUri.resolve("maps/toolbars.dita")), nonConrefCopytoTargets);

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
    assertEquals(Set.of(new Reference(srcDirUri.resolve("maps/toolbars.dita"))), nonCopytoResult_computed);

    assertTrue(reader.getOutDitaFilesSet().isEmpty());

    assertTrue(reader.getOutDitaFilesSet().isEmpty());

    final Set<URI> resourceOnlySet = new HashSet<>(reader.getResourceOnlySet());
    resourceOnlySet.removeAll(reader.getNormalProcessingRoleSet());
    assertTrue(resourceOnlySet.isEmpty());

    assertTrue(reader.getCoderefTargets().isEmpty());

    final Set<URI> nonTopicrefReferenceSet = new HashSet<>(reader.getNonTopicrefReferenceSet());
    nonTopicrefReferenceSet.removeAll(reader.getNormalProcessingRoleSet());
    nonTopicrefReferenceSet.removeAll(reader.getResourceOnlySet());
    assertEquals(Set.of(), nonTopicrefReferenceSet);

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

    assertEquals(
      Stream
        .of(
          "resourceonly.dita",
          "link-from-resource-only-ALSORESOURCEONLY.dita",
          "link-from-normal-ALSORESOURCEONLY.dita",
          "normal.dita",
          "conref-from-normal-ALSORESOURCEONLY.dita",
          "conref-from-resource-only-ALSORESOURCEONLY.dita"
        )
        .map(conrefDirUri::resolve)
        .collect(Collectors.toSet()),
      reader.getHrefTargets()
    );

    final Set<URI> nonConrefCopytoTargets = reader
      .getNonConrefCopytoTargets()
      .stream()
      .map(r -> r.filename)
      .collect(Collectors.toSet());
    assertEquals(
      Stream
        .of(
          "resourceonly.dita",
          "link-from-resource-only-ALSORESOURCEONLY.dita",
          "link-from-normal-ALSORESOURCEONLY.dita",
          "normal.dita",
          "conref-from-normal-ALSORESOURCEONLY.dita",
          "conref-from-resource-only-ALSORESOURCEONLY.dita"
        )
        .map(conrefDirUri::resolve)
        .collect(Collectors.toSet()),
      nonConrefCopytoTargets
    );

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
    assertEquals(
      Stream
        .of(
          "resourceonly.dita",
          "link-from-resource-only-ALSORESOURCEONLY.dita",
          "link-from-normal-ALSORESOURCEONLY.dita",
          "normal.dita",
          "conref-from-normal-ALSORESOURCEONLY.dita",
          "conref-from-resource-only-ALSORESOURCEONLY.dita"
        )
        .map(f -> new Reference(conrefDirUri.resolve(f)))
        .collect(Collectors.toSet()),
      nonCopytoResult
    );

    assertEquals(Set.of(), reader.getOutDitaFilesSet());

    final Set<URI> resourceOnlySet = new HashSet<>(reader.getResourceOnlySet());
    resourceOnlySet.removeAll(reader.getNormalProcessingRoleSet());
    assertEquals(
      Stream
        .of(
          "resourceonly.dita",
          "link-from-resource-only-ALSORESOURCEONLY.dita",
          "link-from-normal-ALSORESOURCEONLY.dita",
          "conref-from-normal-ALSORESOURCEONLY.dita",
          "conref-from-resource-only-ALSORESOURCEONLY.dita"
        )
        .map(conrefDirUri::resolve)
        .collect(Collectors.toSet()),
      resourceOnlySet
    );

    assertTrue(reader.getCoderefTargets().isEmpty());

    final Set<URI> nonTopicrefReferenceSet = new HashSet<>(reader.getNonTopicrefReferenceSet());
    nonTopicrefReferenceSet.removeAll(reader.getNormalProcessingRoleSet());
    nonTopicrefReferenceSet.removeAll(reader.getResourceOnlySet());
    assertEquals(Set.of(), nonTopicrefReferenceSet);

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

  private XMLReader initXMLReader(final File ditaDir, final boolean validate, final File rootFile)
    throws SAXException, IOException {
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
