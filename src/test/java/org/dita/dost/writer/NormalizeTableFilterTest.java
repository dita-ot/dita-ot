/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_PROCESSING_MODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import net.sf.saxon.trans.XPathException;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NormalizeTableFilterTest {

  private final DocumentBuilderFactory dbf;
  private final TransformerFactory tf;
  private NormalizeTableFilter f;

  public NormalizeTableFilterTest() {
    dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    tf = TransformerFactory.newInstance();
  }

  @BeforeEach
  public void setUp() throws SAXException {
    f = new NormalizeTableFilter();
    f.setParent(XMLUtils.getXMLReader());
  }

  @ParameterizedTest
  @ValueSource(
    strings = {
      "topic.dita",
      "test.dita",
      "simple.dita",
      "withoutColSpec.dita",
      "rowspan.dita",
      "onlyRows.dita",
      "nested.dita",
      "parallel.dita",
      "multiGroup.dita",
    }
  )
  public void filter(String file) throws Exception {
    final CachingLogger logger = test(file);
    assertEquals(0, logger.getMessages().size());
  }

  @Test
  public void broken() throws Exception {
    f.setParam(ANT_INVOKER_EXT_PARAM_PROCESSING_MODE, Configuration.Mode.STRICT.name());

    assertThrows(XPathException.class, () -> test("broken.dita"));
  }

  private CachingLogger test(final String file) throws Exception {
    return test(file, file);
  }

  private CachingLogger test(final String srcFile, final String expFile) throws Exception {
    final DocumentBuilder db = dbf.newDocumentBuilder();
    final InputStream expStream = getClass()
      .getClassLoader()
      .getResourceAsStream(this.getClass().getSimpleName() + "/exp/" + expFile);

    final Transformer t = tf.newTransformer();
    final InputStream src = getClass()
      .getClassLoader()
      .getResourceAsStream(this.getClass().getSimpleName() + "/src/" + srcFile);
    final CachingLogger logger = new CachingLogger();
    f.setLogger(logger);
    final SAXSource s = new SAXSource(f, new InputSource(src));

    final Document act = db.newDocument();
    t.transform(s, new DOMResult(act));
    assertXMLEqual(db.parse(expStream), act);
    return logger;
  }
}
