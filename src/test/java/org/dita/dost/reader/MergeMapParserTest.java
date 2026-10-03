/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.TestUtils.assertXMLEqual;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.TestUtils;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MergeMapParserTest {

  final File resourceDir = TestUtils.getResourceDir(MergeMapParserTest.class);
  private final File srcDir = new File(resourceDir, "src");
  private final File expDir = new File(resourceDir, "exp");

  @ParameterizedTest
  @CsvSource(
    {
      "test.ditamap,merged.xml",
      "test.ditamap,merged.xml",
      "space in map name.ditamap,merged.xml",
      "testcomposite.ditamap,mergedwithditasub.xml",
      "testsubtopic.ditamap,mergedsub.xml",
    }
  )
  public void testReadStringString(String src, String exp) throws IOException {
    var parser = new MergeMapParser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));

    try (var output = new ByteArrayOutputStream()) {
      output.write("<wrapper>".getBytes(StandardCharsets.UTF_8));
      parser.setOutputStream(output);
      parser.read(new File(srcDir, src).getAbsoluteFile(), srcDir.getAbsoluteFile());
      output.write("</wrapper>".getBytes(StandardCharsets.UTF_8));
      assertXMLEqual(
        new InputSource(new File(expDir, exp).toURI().toString()),
        new InputSource(new ByteArrayInputStream(output.toByteArray()))
      );
    }
  }

  @ParameterizedTest
  @CsvSource(
    {
      "test.ditamap,merged.xml",
      "test.ditamap,merged.xml",
      "space in map name.ditamap,merged.xml",
      "testcomposite.ditamap,mergedwithditasub.xml",
      "testsubtopic.ditamap,mergedsub.xml",
    }
  )
  public void testCacheStore(String src, String exp) throws SAXException, IOException, ParserConfigurationException {
    var parser = new MergeMapParser();
    parser.setLogger(new TestUtils.TestLogger());
    var tmpDir = new File(resourceDir, "tmpRandom");
    var store = new CacheStore(tmpDir, new XMLUtils());
    var job = new Job(tmpDir, store);
    var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    for (String child : new String[] { src, "test.xml", "test2.xml", "testdita1.xml", "testdita2.xml" }) {
      var doc = builder.parse(new File(srcDir, child));
      store.writeDocument(doc, new File(tmpDir, child).toURI());
    }
    parser.setJob(job);

    try (var output = new ByteArrayOutputStream()) {
      output.write("<wrapper>".getBytes(StandardCharsets.UTF_8));
      parser.setOutputStream(output);
      parser.read(new File(tmpDir, src).getAbsoluteFile(), srcDir.getAbsoluteFile());
      output.write("</wrapper>".getBytes(StandardCharsets.UTF_8));

      assertXMLEqual(
        new InputSource(new File(expDir, exp).toURI().toString()),
        new InputSource(new ByteArrayInputStream(output.toByteArray()))
      );
    }
  }
}
