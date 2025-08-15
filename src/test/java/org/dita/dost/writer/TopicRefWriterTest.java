/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static java.net.URI.create;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class TopicRefWriterTest {

  private final TransformerFactory transformerFactory;
  private final DocumentBuilder db;

  private TopicRefWriter reader;
  private XMLReader parser;

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of(
        "image",
        "<image class='- topic/image ' href='same.png'/>",
        "<image class='- topic/image ' href='same.png'/>"
      ),
      Arguments.of(
        "object",
        "<object class='- topic/object ' data='same.pdf'/>",
        "<object class='- topic/object ' data='same.pdf'/>"
      ),
      Arguments.of(
        "xrefSame",
        "<xref class='- topic/xref ' href='same.dita'/>",
        "<xref class='- topic/xref ' href='same.dita'/>"
      ),
      Arguments.of(
        "xrefWithoutId",
        "<xref class='- topic/xref ' href='source.dita'/>",
        "<xref class='- topic/xref ' href='change.dita'/>"
      ),
      Arguments.of(
        "xrefWithId",
        "<xref class='- topic/xref ' href='source.dita#from'/>",
        "<xref class='- topic/xref ' href='change.dita#to'/>"
      ),
      Arguments.of(
        "xrefWithElementId",
        "<xref class='- topic/xref ' href='source.dita#from/element'/>",
        "<xref class='- topic/xref ' href='change.dita#to/element'/>"
      ),
      Arguments.of(
        "xrefWithFileChange",
        "<xref class='- topic/xref ' href='source.dita#other'/>",
        "<xref class='- topic/xref ' href='change.dita#other'/>"
      ),
      Arguments.of(
        "xrefWithFileChangeElementId",
        "<xref class='- topic/xref ' href='source.dita#other/element'/>",
        "<xref class='- topic/xref ' href='change.dita#other/element'/>"
      ),
      Arguments.of(
        "xrefUnmapped",
        "<xref class='- topic/xref ' href='unmapped.dita'/>",
        "<xref class='- topic/xref ' href='unmapped.dita'/>"
      ),
      Arguments.of(
        "xrefUnmappedWithTopicId",
        "<xref class='- topic/xref ' href='unmapped.dita#topic'/>",
        "<xref class='- topic/xref ' href='unmapped.dita#topic'/>"
      ),
      Arguments.of(
        "xrefUnmappedWithElementId",
        "<xref class='- topic/xref ' href='unmapped.dita#topic/element'/>",
        "<xref class='- topic/xref ' href='unmapped.dita#topic/element'/>"
      )
    );
  }

  public TopicRefWriterTest() throws ParserConfigurationException {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    documentBuilderFactory.setNamespaceAware(true);
    db = documentBuilderFactory.newDocumentBuilder();
    transformerFactory = TransformerFactory.newInstance();
  }

  @BeforeEach
  public void setUp() throws Exception {
    final File tempDir = TestUtils.createTempDir(getClass());

    reader = new TopicRefWriter();
    reader.setLogger(new TestUtils.TestLogger());
    reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    reader.setCurrentFile(tempDir.toURI().resolve("dir/bar.dita"));

    reader.setup(
      map(tempDir.toURI().resolve("dir/same.dita").toString(), tempDir.toURI().resolve("dir/same.dita").toString())
    );
    reader.setChangeTable(
      map(
        tempDir.toURI().resolve("dir/same.dita").toString(),
        tempDir.toURI().resolve("dir/same.dita").toString(),
        tempDir.toURI().resolve("dir/source.dita").toString(),
        tempDir.toURI().resolve("dir/change.dita").toString(),
        tempDir.toURI().resolve("dir/source.dita#from").toString(),
        tempDir.toURI().resolve("dir/change.dita#to").toString()
      )
    );
    reader.setFixpath(null);

    parser = XMLUtils.getXMLReader();
    reader.setParent(parser);
  }

  private Map<URI, URI> map(String... arg) {
    final Map<URI, URI> res = new HashMap<>();
    for (int i = 0; i < arg.length; i++) {
      res.put(URI.create(arg[i]), URI.create(arg[++i]));
    }
    return res;
  }

  @ParameterizedTest
  @MethodSource("data")
  public void test_image(String name, String src, String exp) {
    assertEquals(exp, run(src));
  }

  private void assertEquals(final String exp, final Document act) {
    try {
      try (Reader in = new StringReader(exp)) {
        final Diff d = DiffBuilder
          .compare(db.parse(new InputSource(in)))
          .withTest(act)
          .ignoreWhitespace()
          .normalizeWhitespace()
          .build();
        if (d.hasDifferences()) {
          throw new AssertionError(d.toString());
        }
      }
    } catch (IOException | SAXException e) {
      throw new RuntimeException(e);
    }
  }

  private Document run(final String content) {
    try {
      final Document doc = db.newDocument();
      final InputSource inputSource = new InputSource(new StringReader(content));
      transformerFactory.newTransformer().transform(new SAXSource(reader, inputSource), new DOMResult(doc));
      return doc;
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
  }
}
