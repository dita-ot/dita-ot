/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlresolver.Resolver;

public class XSpecTest {

  public static final String XSPEC_NS = "http://www.jenitennison.com/xslt/xspec";
  private static TransformerFactory transformerFactory;
  private static Transformer compiler;
  private static URIResolver resolver;

  public static Stream<Arguments> getFiles() {
    final List<File> cases = new ArrayList<>();
    findXSpec(new File("src/test").getAbsoluteFile(), cases);
    return cases.stream().map(Arguments::of);
  }

  private static void findXSpec(final File f, final List<File> res) {
    if (f.isDirectory()) {
      for (final File c : f.listFiles()) {
        findXSpec(c, res);
      }
    } else {
      if (f.getName().endsWith(".xspec")) {
        res.add(f);
      }
    }
  }

  //  private final File xspec;

  //  public XSpecTest(final File xspec) {
  //    this.xspec = xspec;
  //  }

  @BeforeAll
  public static void setUpClass() throws TransformerException {
    transformerFactory = TransformerFactory.newInstance();
    final File ditaDir = new File(
      Optional.ofNullable(System.getProperty("dita.dir")).orElse("src" + File.separator + "main")
    )
      .getAbsoluteFile();
    CatalogUtils.setDitaDir(ditaDir);
    final Resolver catalogResolver = CatalogUtils.getCatalogResolver();
    resolver = new ClassPathResolver(catalogResolver);
    transformerFactory.setURIResolver(resolver);
    final Source stylesheet = resolver.resolve("classpath:///XSpec/generate-xspec-tests.xsl", "");
    compiler = transformerFactory.newTransformer(stylesheet);
    compiler.setURIResolver(resolver);
  }

  @ParameterizedTest
  @MethodSource("getFiles")
  public void testXSpec(File xspec) throws TransformerException, ParserConfigurationException {
    final DOMResult stylesheet = new DOMResult();
    compiler.transform(new StreamSource(xspec), stylesheet);

    final Transformer compiledXspec = transformerFactory.newTransformer(new DOMSource(stylesheet.getNode()));
    final DOMResult results = new DOMResult();
    compiledXspec.transform(
      new DOMSource(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument()),
      results
    );

    final NodeList tests = ((Document) results.getNode()).getElementsByTagNameNS(XSPEC_NS, "test");
    for (int i = 0; i < tests.getLength(); i++) {
      final Element test = (Element) tests.item(i);
      final boolean res =
        Boolean.parseBoolean(test.getAttribute("successful")) || test.getAttributeNode("pending") != null;
      if (!res) {
        final Element scenario = (Element) test.getParentNode();
        final Node label = scenario.getElementsByTagNameNS(XSPEC_NS, "label").item(0).getFirstChild();
        final String act =
          ((Element) scenario.getElementsByTagNameNS(XSPEC_NS, "result").item(0)).getAttribute("select");
        final String exp = ((Element) test.getElementsByTagNameNS(XSPEC_NS, "expect").item(0)).getAttribute("select");
        assertEquals(exp, act, label != null ? label.getTextContent() : null);
      }
    }
  }
}
