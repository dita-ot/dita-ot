/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_STYLE;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MoveMetaModuleTest extends AbstractModuleTest {

  public static Stream<Arguments> data() {
    return Stream.of(
      Arguments.of("MatadataInheritance_foreign"),
      Arguments.of("MatadataInheritance_keywords"),
      Arguments.of("MatadataInheritance_linktext"),
      Arguments.of("MatadataInheritance_othermeta"),
      Arguments.of("MatadataInheritance_pemissions"),
      Arguments.of("MatadataInheritance_pemissions_replace"),
      Arguments.of("MatadataInheritance_prodinfo"),
      Arguments.of("MatadataInheritance_publisher"),
      Arguments.of("MatadataInheritance_resourceid"),
      Arguments.of("MatadataInheritance_searchtitle"),
      Arguments.of("MatadataInheritance_shortdesc"),
      Arguments.of("MatadataInheritance_source"),
      Arguments.of("MatadataInheritance_source_replace"),
      Arguments.of("MatadataInheritance_unknown"),
      Arguments.of("MetadataInheritance_audience"),
      Arguments.of("MetadataInheritance_author"),
      Arguments.of("MetadataInheritance_category"),
      Arguments.of("MetadataInheritance_copyright"),
      Arguments.of("MetadataInheritance_critdates"),
      Arguments.of("MetadataInheritance_critdates_replace"),
      Arguments.of("MetadataInheritance_data"),
      Arguments.of("MetadataInheritance_dataabout"),
      Arguments.of("MetadataInheritance_linktitle")
    );
  }

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    final AbstractPipelineInput input = new PipelineHashIO();
    input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
    input.setAttribute(
      ANT_INVOKER_EXT_PARAM_STYLE,
      Paths.get("src", "main", "plugins", "org.dita.base", "xsl", "preprocess", "mappull.xsl").toString()
    );
    return input;
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new MoveMetaModule();
  }

  public void initTest() {
    try {
      final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
      f.setNamespaceAware(true);
      final DocumentBuilder b = f.newDocumentBuilder();
      for (File file : tempDir.listFiles((dir, name) -> name.endsWith("dita") || name.endsWith("ditamap"))) {
        final Document d = b.parse(file);
        d.appendChild(d.createProcessingInstruction("workdir-uri", tempDir.toURI().toString()));
        xmlUtils.writeDocument(d, file);
      }
    } catch (ParserConfigurationException | IOException | SAXException e) {
      throw new RuntimeException(e);
    }
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialFile(String testCase) {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    initTest();
    test();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void serialMemory(String testCase) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    initTest();
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    chunkModule.setJob(job);
    test();
  }
}
