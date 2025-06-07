/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.BuildingContentHandler;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.sapling.SaplingElement;
import net.sf.saxon.sapling.SaplingNode;
import net.sf.saxon.sapling.SaplingText;
import net.sf.saxon.sapling.Saplings;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class KeyrefParserTest {

  private static final File resourceDir = TestUtils.getResourceDir(KeyrefParserTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  private final XMLUtils xmlUtils = new XMLUtils();

  @TempDir
  private File tempDir;

  @ParameterizedTest
  @MethodSource("testWriteArguments")
  public void testWrite(Path file, Path map) throws Exception {
    TestUtils.copy(srcDir, tempDir);

    final KeyScope keyDefinition = readKeyMap(map);
    final KeyrefParser parser = new KeyrefParser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(Paths.get(tempDir.getAbsolutePath(), file.toString()).toUri());
    parser.write(Paths.get(tempDir.getAbsolutePath(), file.toString()).toFile());

    assertXMLEqual(
      new InputSource(Paths.get(expDir.getAbsolutePath(), file.toString()).toUri().toString()),
      new InputSource(Paths.get(tempDir.getAbsolutePath(), file.toString()).toUri().toString())
    );
  }

  private static Stream<Arguments> testWriteArguments() {
    return Stream.of(
      Arguments.of(Paths.get("whitespace.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("a.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("subdir", "subdirtopic.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("id.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("fallback.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("b.ditamap"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("d.ditamap"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("copy-to-keys.ditamap"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("subdir", "c.ditamap"), Paths.get("subdir", "c.ditamap"))
    );
  }

  @Test
  public void compatibulityMode() throws Exception {
    Path file = Paths.get("compatibility.xml");
    Path map = Paths.get("keys.ditamap");

    TestUtils.copy(srcDir, tempDir);

    final KeyScope keyDefinition = readKeyMap(map);
    final KeyrefParser parser = new KeyrefParser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(Paths.get(tempDir.getAbsolutePath(), file.toString()).toUri());
    parser.setCompatibilityMode(false);
    parser.write(Paths.get(tempDir.getAbsolutePath(), file.toString()).toFile());

    assertXMLEqual(
      new InputSource(Paths.get(expDir.getAbsolutePath(), file.toString()).toUri().toString()),
      new InputSource(Paths.get(tempDir.getAbsolutePath(), file.toString()).toUri().toString())
    );
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testDomToSaxArguments")
  public void testDomToSax(String ignore, SaplingElement src, SaplingNode exp, boolean retainElements)
    throws SaxonApiException, SAXException {
    final BuildingContentHandler buildingContentHandler = xmlUtils
      .getProcessor()
      .newDocumentBuilder()
      .newBuildingContentHandler();
    final KeyrefParser parser = new KeyrefParser();
    parser.setContentHandler(buildingContentHandler);
    final XdmNode xdmNode = src.toXdmNode(xmlUtils.getProcessor());

    buildingContentHandler.startDocument();
    parser.domToSax(xdmNode, retainElements);
    buildingContentHandler.endDocument();

    final XdmNode expNode = Saplings.elem("wrapper").withChild(exp).toXdmNode(xmlUtils.getProcessor());
    final Diff d = DiffBuilder
      .compare(expNode.toString())
      .withTest("<wrapper>" + buildingContentHandler.getDocumentNode().toString() + "</wrapper>")
      .ignoreWhitespace()
      .build();
    if (d.hasDifferences()) {
      System.err.println(expNode);
      System.err.println("<wrapper>" + buildingContentHandler.getDocumentNode().toString() + "</wrapper>");
      throw new AssertionError(d.toString());
    }
  }

  private static Stream<Arguments> testDomToSaxArguments() {
    final SaplingText characters = Saplings.text("text");
    final QName xmlLang = new QName("xml", XMLConstants.XML_NS_URI, "lang");
    final SaplingElement keyword = Saplings
      .elem(TOPIC_KEYWORD.localName)
      .withAttr("class", TOPIC_KEYWORD.toString())
      .withAttr(xmlLang, "en")
      .withChild(characters);
    final SaplingElement tm = Saplings
      .elem(TOPIC_TM.localName)
      .withAttr("class", TOPIC_TM.toString())
      .withAttr(xmlLang, "en")
      .withChild(characters);
    final SaplingElement text = Saplings
      .elem(TOPIC_TEXT.localName)
      .withAttr("class", TOPIC_TEXT.toString())
      .withAttr(xmlLang, "en")
      .withChild(characters);
    final SaplingElement keywordWithTm = Saplings
      .elem(TOPIC_KEYWORD.localName)
      .withAttr("class", TOPIC_KEYWORD.toString())
      .withAttr(xmlLang, "en")
      .withChild(tm);
    final SaplingElement keywordWithText = Saplings
      .elem(TOPIC_KEYWORD.localName)
      .withAttr("class", TOPIC_KEYWORD.toString())
      .withAttr(xmlLang, "en")
      .withChild(text);
    return Stream.of(
      Arguments.of("keyword > #text, strip", keyword, characters, false),
      Arguments.of("keyword > tm > #text, strip", keywordWithTm, tm, false),
      Arguments.of("keyword > text > #text, strip", keywordWithText, text, false),
      Arguments.of("keyword > #text, retain", keyword, keyword, true),
      Arguments.of("keyword > tm > #text, retain", keywordWithTm, keywordWithTm, true),
      Arguments.of("keyword > text > #text, retain", keywordWithText, keywordWithText, true)
    );
  }

  private KeyScope readKeyMap(final Path map) {
    KeyrefReader reader = new KeyrefReader();
    final URI keyMapFile = tempDir.toPath().resolve(map).toUri();
    final XdmNode document = parse(keyMapFile);

    reader.read(keyMapFile, document);

    return reader.getKeyDefinition();
  }

  private XdmNode parse(final URI in) {
    try {
      final StreamSource source = new StreamSource(in.toString());
      source.setSystemId(in.toString());
      return xmlUtils.getProcessor().newDocumentBuilder().build(source);
    } catch (SaxonApiException e) {
      throw new RuntimeException(e);
    }
  }
}
