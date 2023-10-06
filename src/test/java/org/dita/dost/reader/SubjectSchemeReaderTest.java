/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.reader;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.sapling.SaplingElement;
import net.sf.saxon.sapling.Saplings;
import org.dita.dost.TestUtils;
import org.dita.dost.module.filter.SubjectScheme;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlunit.builder.DiffBuilder;

class SubjectSchemeReaderTest {

  private final XMLUtils xmlUtils = new XMLUtils();

  private Job job;
  private SubjectSchemeReader reader;

  @TempDir
  File tempDir;

  @BeforeEach
  void init() throws IOException {
    job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));
    reader = new SubjectSchemeReader();
    reader.setJob(job);
    var logger = new TestUtils.CachingLogger();
    reader.setLogger(logger);
  }

  @Test
  void loadSubjectScheme() throws URISyntaxException, IOException {
    final Path src = tempDir.toPath().resolve("example-subjectScheme-filtering.ditamap");
    Files.copy(
      Paths.get(
        getClass()
          .getResource("/org/dita/dost/reader.SubjectSchemeReaderTest.src/example-subjectScheme-filtering.ditamap")
          .toURI()
      ),
      src
    );

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    var exp = new SubjectScheme(
      Map.of(
        QName.valueOf("platform"),
        Map.of(
          "*",
          Set.of(
            createElement(
              createSubjectDef("os", "Operating system")
                .withChild(
                  createSubjectDef("linux", "Linux")
                    .withChild(createSubjectDef("redhat", "RedHat Linux"), createSubjectDef("suse", "SuSE Linux")),
                  createSubjectDef("windows", "Windows"),
                  createSubjectDef("zos", "z/OS")
                )
            )
          )
        )
      )
    );
    assertSubjectSchemeEquals(act, exp);
    assertEquals(
      Map.of(QName.valueOf("platform"), Map.of("*", Set.of("suse", "linux", "windows", "redhat", "zos"))),
      reader.getValidValuesMap()
    );
    assertEquals(Map.of(), reader.getDefaultValueMap());
  }

  @Test
  void loadSubjectScheme_indirectSubjectdef() throws URISyntaxException, IOException {
    final Path src = tempDir.toPath().resolve("indirect-subjectdef.ditamap");
    Files.copy(
      Paths.get(
        getClass().getResource("/org/dita/dost/reader.SubjectSchemeReaderTest.src/indirect-subjectdef.ditamap").toURI()
      ),
      src
    );

    reader.loadSubjectScheme(src.toFile());

    var act = reader.getSubjectSchemeMap();
    assertFalse(act.isEmpty());
    var exp = new SubjectScheme(
      Map.of(
        QName.valueOf("platform"),
        Map.of(
          "*",
          Set.of(
            createElement(
              createSubjectDef("os", "Operating system")
                .withChild(
                  Saplings
                    .elem("subjectdef")
                    .withAttr("class", "- map/topicref subjectScheme/subjectdef ")
                    .withAttr("keyref", "linux"),
                  createSubjectDef("windows", "Windows"),
                  createSubjectDef("zos", "z/OS")
                )
            )
          )
        )
      )
    );
    assertSubjectSchemeEquals(act, exp);
    assertEquals(Map.of(QName.valueOf("platform"), Map.of("*", Set.of("windows", "zos"))), reader.getValidValuesMap());
    assertEquals(Map.of(), reader.getDefaultValueMap());
  }

  private void assertSubjectSchemeEquals(SubjectScheme act, SubjectScheme exp) {
    assertEquals(exp.subjectSchemeMap().keySet(), act.subjectSchemeMap().keySet());
    for (QName expKey : exp.subjectSchemeMap().keySet()) {
      final Map<String, Set<Element>> actStringSetMap = act.subjectSchemeMap().get(expKey);
      final Map<String, Set<Element>> expStringSetMap = exp.subjectSchemeMap().get(expKey);
      assertEquals(expStringSetMap.keySet(), actStringSetMap.keySet());
      for (String expSetKey : expStringSetMap.keySet()) {
        var expValueSet = expStringSetMap.get(expSetKey);
        var actValueSet = actStringSetMap.get(expSetKey);
        assertDocumentSetEquals(expValueSet, actValueSet);
      }
    }
  }

  private void assertDocumentSetEquals(Set<Element> expValueSet, Set<Element> actValueSet) {
    assertEquals(expValueSet.size(), actValueSet.size());
    final List<Element> expList = new ArrayList<>(expValueSet);
    for (Element act : actValueSet) {
      for (Element exp : expList) {
        var d = DiffBuilder.compare(exp).withTest(act).ignoreWhitespace().build();
        if (!d.hasDifferences()) {
          expList.remove(exp);
          break;
        }
      }
    }
    assertTrue(expList.isEmpty());
  }

  private SaplingElement createSubjectDef(String keys, String navTitle) {
    return Saplings
      .elem("subjectdef")
      .withAttr("class", "- map/topicref subjectScheme/subjectdef ")
      .withAttr("keys", keys)
      .withAttr("navtitle", navTitle);
  }

  private void assertDocumentEquals(Element exp, Element act) {
    var d = DiffBuilder.compare(exp).withTest(act).ignoreWhitespace().build();
    if (d.hasDifferences()) {
      try {
        TransformerFactory.newInstance().newTransformer().transform(new DOMSource(act), new StreamResult(System.out));
      } catch (TransformerException e) {
        throw new RuntimeException(e);
      }
      throw new AssertionError(d.toString());
    }
  }

  private Element createElement(SaplingElement exp) {
    try {
      var underlyingValue = Saplings.doc().withChild(exp).toXdmNode(xmlUtils.getProcessor()).getUnderlyingValue();
      return ((Document) NodeOverNodeInfo.wrap(underlyingValue)).getDocumentElement();
    } catch (SaxonApiException e) {
      throw new RuntimeException(e);
    }
  }
}
