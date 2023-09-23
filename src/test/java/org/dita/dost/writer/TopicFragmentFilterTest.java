/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.dita.dost.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class TopicFragmentFilterTest {

  private static File tempDir;
  private static final File resourceDir = TestUtils.getResourceDir(TopicFragmentFilterTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  @BeforeEach
  public void setUp() throws Exception {
    tempDir = TestUtils.createTempDir(TopicFragmentFilterTest.class);
  }

  @AfterEach
  public void tearDown() throws Exception {
    TestUtils.forceDelete(tempDir);
  }

  @Test
  public void test() throws Exception {
    final TopicFragmentFilter f = new TopicFragmentFilter(ATTRIBUTE_NAME_HREF);
    f.setParent(SAXParserFactory.newInstance().newSAXParser().getXMLReader());

    final DOMResult dst = new DOMResult();
    TransformerFactory
      .newInstance()
      .newTransformer()
      .transform(new SAXSource(f, new InputSource(new File(srcDir, "topic.dita").toURI().toString())), dst);

    final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    builderFactory.setIgnoringComments(true);
    final Document exp = builderFactory.newDocumentBuilder().parse(new File(expDir, "topic.dita"));
    assertXMLEqual(exp, (Document) dst.getNode());
  }
}
