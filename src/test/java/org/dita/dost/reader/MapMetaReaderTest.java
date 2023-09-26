/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.Configuration;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.sapling.SaplingDocument;
import net.sf.saxon.sapling.SaplingElement;
import net.sf.saxon.sapling.Saplings;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

public class MapMetaReaderTest {

  private static final File resourceDir = TestUtils.getResourceDir(MapMetaReaderTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  private final Processor processor = new Processor(Configuration.newConfiguration());

  private File tempDir;
  private MapMetaReader reader;
  private DocumentBuilder db;

  @BeforeEach
  public void setUp() throws Exception {
    CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
    tempDir = TestUtils.createTempDir(MapMetaReaderTest.class);

    reader = new MapMetaReader();
    reader.setLogger(new TestUtils.TestLogger());
    reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));

    db = XMLUtils.getDocumentBuilder();
    db.setEntityResolver(CatalogUtils.getCatalogResolver());
  }

  @ParameterizedTest
  @ValueSource(strings = { "base", "locktitle", "duplicate" })
  public void testRead(String dir) throws DITAOTException, IOException, SAXException {
    Files
      .list(srcDir.toPath().resolve(dir))
      .forEach(f -> {
        try {
          TestUtils.normalize(f.toFile(), new File(tempDir, f.toFile().getName()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

    final Path mapFile = tempDir.toPath().resolve("test.ditamap");
    reader.read(mapFile.toAbsolutePath().toFile());

    final Document exp = db.parse(expDir.toPath().resolve(dir).resolve("test.ditamap").toFile());
    final Document act = db.parse(tempDir.toPath().resolve("test.ditamap").toFile());
    assertXMLEqual(exp, act);
  }

  @Test
  public void testReadDuplicateSingle() throws DITAOTException, IOException, SAXException {
    var dir = "duplicate-single";
    Files
      .list(srcDir.toPath().resolve(dir))
      .forEach(f -> {
        try {
          TestUtils.normalize(f.toFile(), new File(tempDir, f.toFile().getName()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

    final Path mapFile = tempDir.toPath().resolve("test.ditamap");
    reader.read(mapFile.toAbsolutePath().toFile());

    //    final Document exp = db.parse(expDir.toPath().resolve(dir).resolve("test.ditamap").toFile());
    //    final Document act = db.parse(tempDir.toPath().resolve("test.ditamap").toFile());
    //    assertXMLEqual(exp, act);

    assertMappingEquals(
      Map.of(
        URI.create("a.xml"),
        Map.of(
          " topic/author ",
          wrap(
            Saplings.elem("author").withAttr("class", Constants.TOPIC_AUTHOR.toString()).withText("topicref author 1"),
            Saplings.elem("author").withAttr("class", Constants.TOPIC_AUTHOR.toString()).withText("topicref author 2"),
            Saplings.elem("author").withAttr("class", Constants.TOPIC_AUTHOR.toString()).withText("topicref author 3")
          )
        )
      ),
      reader.getMapping()
    );
  }

  private SaplingDocument wrap(SaplingElement... children) {
    return Saplings.doc().withChild(Saplings.elem("stub").withChild(children));
  }

  private void assertMappingEquals(Map<URI, Map<String, SaplingDocument>> exp, Map<URI, Map<String, Element>> act) {
    assertEquals(exp.keySet(), act.keySet());
    for (URI href : exp.keySet()) {
      var expElems = exp.get(href);
      var actElems = act.get(href);
      assertEquals(expElems.keySet(), actElems.keySet());
      for (String cls : expElems.keySet()) {
        assertDocumentEquals(expElems.get(cls), actElems.get(cls));
      }
    }
  }

  private void assertDocumentEquals(SaplingDocument exp, Element act) {
    try {
      var expElem =
        ((Document) NodeOverNodeInfo.wrap(exp.toXdmNode(processor).getUnderlyingValue())).getDocumentElement();
      final Diff d = DiffBuilder.compare(expElem).withTest(act).build();
      if (d.hasDifferences()) {
        try {
          TransformerFactory.newInstance().newTransformer().transform(new DOMSource(act), new StreamResult(System.out));
        } catch (TransformerException e) {
          throw new RuntimeException(e);
        }
        throw new AssertionError(d.toString());
      }
    } catch (SaxonApiException e) {
      throw new RuntimeException(e);
    }
  }

  @AfterEach
  public void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
