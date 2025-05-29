/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.dita.dost.TestUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NormalizeSimpleTableFilterTest {

  private final DocumentBuilderFactory dbf;
  private final TransformerFactory tf;
  private NormalizeSimpleTableFilter f;

  public NormalizeSimpleTableFilterTest() {
    dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    tf = TransformerFactory.newInstance();
  }

  @BeforeEach
  public void setUp() throws SAXException {
    f = new NormalizeSimpleTableFilter();
    f.setParent(XMLUtils.getXMLReader());
  }

  @ParameterizedTest
  @ValueSource(strings = { "simple.dita", "topic.dita", "nested.dita", "rowspan.dita", "parallel.dita" })
  public void filter(String file) throws Exception {
    test(file);
  }

  private void test(final String file) throws Exception {
    final DocumentBuilder db = dbf.newDocumentBuilder();
    final InputStream expStream = getClass()
      .getClassLoader()
      .getResourceAsStream(this.getClass().getSimpleName() + "/exp/" + file);

    final Transformer t = tf.newTransformer();
    final InputStream src = getClass()
      .getClassLoader()
      .getResourceAsStream(this.getClass().getSimpleName() + "/src/" + file);
    f.setLogger(new TestUtils.TestLogger());
    final SAXSource s = new SAXSource(f, new InputSource(src));

    final Document act = db.newDocument();
    t.transform(s, new DOMResult(act));
    assertXMLEqual(db.parse(expStream), act);
  }
}
