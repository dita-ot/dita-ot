/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.*;
import org.junit.jupiter.api.*;
import org.opentest4j.TestAbortedException;
import org.xmlresolver.Resolver;

public class XSpecTest {

  public static final String XSPEC_NS = "http://www.jenitennison.com/xslt/xspec";
  private static Transformer transformer;
  private static XSpecRunner runner;

  public static List<File> getFiles() {
    final List<File> cases = new ArrayList<>();
    findXSpec(new File("src/test").getAbsoluteFile(), cases);
    return cases;
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

  @BeforeAll
  public static void setUpClass() throws TransformerException {
    var transformerFactory = TransformerFactory.newInstance();
    final File ditaDir = new File(
      Optional.ofNullable(System.getProperty("dita.dir")).orElse("src" + File.separator + "main")
    )
      .getAbsoluteFile();

    CatalogUtils.setDitaDir(ditaDir);
    final Resolver catalogResolver = CatalogUtils.getCatalogResolver();
    var resolver = new ClassPathResolver(catalogResolver);
    transformerFactory.setURIResolver(resolver);
    var stylesheet = resolver.resolve("classpath:/io/xspec/xspec/impl/src/compiler/compile-xslt-tests.xsl", "");
    transformer = transformerFactory.newTransformer(stylesheet);
    transformer.setURIResolver(resolver);
    runner = new XSpecRunner(resolver);
  }

  @TestFactory
  Stream<DynamicNode> xspecTests() {
    return getFiles().stream().map(file -> dynamicContainer(file.getName(), testXSpec(file)));
  }

  public Stream<DynamicTest> testXSpec(File xspec) {
    try {
      var compiledXSpec = getCompiledXSpec(xspec);
      var results = runner.runXSpec(compiledXSpec);
      return parseXSpecResults(results);
    } catch (SaxonApiException | TransformerException e) {
      throw new RuntimeException(e);
    }
  }

  private static DOMSource getCompiledXSpec(File xspec) throws TransformerException {
    final DOMResult stylesheet = new DOMResult();
    transformer.transform(new StreamSource(xspec), stylesheet);
    return new DOMSource(stylesheet.getNode());
  }

  private Stream<DynamicTest> parseXSpecResults(XdmNode results) throws SaxonApiException {
    var dynamicTests = new ArrayList<DynamicTest>();
    var xpath = getXPathCompiler();
    var allScenarios = getAllScenarios(results, xpath);

    for (XdmItem scenario : allScenarios) {
      var label = getLabel(xpath, scenario);

      if (isPendingTest(xpath, scenario)) {
        addPending(dynamicTests, label);
      } else if (isSuccessfulTest(xpath, scenario)) {
        addSuccess(dynamicTests, label);
      } else {
        addFailure(xpath, scenario, dynamicTests, label);
      }
    }

    return dynamicTests.stream();
  }

  private static XPathCompiler getXPathCompiler() {
    var processor = runner.getProcessor();
    var xpath = processor.newXPathCompiler();
    xpath.declareNamespace("x", XSPEC_NS);
    return xpath;
  }

  private static XPathSelector getAllScenarios(XdmNode results, XPathCompiler xpath) throws SaxonApiException {
    var xpathAllScenarios = "//x:scenario";
    var allScenarios = xpath.compile(xpathAllScenarios).load();
    allScenarios.setContextItem(results);
    return allScenarios;
  }

  private void addPending(List<DynamicTest> dynamicTests, String label) {
    dynamicTests.add(DynamicTest.dynamicTest(label, this::skipTest));
  }

  private static void addSuccess(List<DynamicTest> dynamicTests, String label) {
    dynamicTests.add(DynamicTest.dynamicTest(label, () -> Assertions.assertTrue(true)));
  }

  private static void addFailure(XPathCompiler xpath, XdmItem scenario, List<DynamicTest> dynamicTests, String label)
    throws SaxonApiException {
    var act = getNodeText(xpath, scenario, "x:result/*");
    var exp = getExpected(xpath, scenario);
    dynamicTests.add(DynamicTest.dynamicTest(label, () -> Assertions.assertEquals(exp, act, label)));
  }

  private static String getExpected(XPathCompiler xpath, XdmItem scenario) throws SaxonApiException {
    var exp = getNodeText(xpath, scenario, "x:test/x:expect/*");
    if (exp.isEmpty()) {
      exp = getNodeText(xpath, scenario, "string(x:test/x:expect/@select)");
    }
    return exp;
  }

  private static boolean isSuccessfulTest(XPathCompiler xpath, XdmItem scenario) throws SaxonApiException {
    return Boolean.parseBoolean(getNodeText(xpath, scenario, "string(x:test/@successful)"));
  }

  private static boolean isPendingTest(XPathCompiler xpath, XdmItem scenario) throws SaxonApiException {
    var selector = xpath.compile("x:test/@pending").load();
    selector.setContextItem(scenario);
    var pending = selector.evaluateSingle();
    return (pending != null);
  }

  private void skipTest() {
    throw new TestAbortedException("Pending");
  }

  private static String getLabel(XPathCompiler xpath, XdmItem scenario) throws SaxonApiException {
    var selector = xpath.compile("ancestor-or-self::x:scenario/x:label/text() | x:test/x:label/text()").load();
    selector.setContextItem(scenario);
    var labels = selector.evaluate();
    var final_label = new StringBuilder();
    for (XdmItem label : labels) {
      final_label.append(" ").append(label.toString());
    }
    return final_label.toString().trim();
  }

  private static String getNodeText(XPathCompiler xpath, XdmItem scenario, String expr) throws SaxonApiException {
    var selector = xpath.compile(expr).load();
    selector.setContextItem(scenario);
    var item = selector.evaluateSingle();
    return item != null ? item.toString() : "";
  }

  public static class XSpecRunner {

    private static final QName XSPEC_MAIN_TEMPLATE = new QName("x", "http://www.jenitennison.com/xslt/xspec", "main");

    private final Processor processor;
    private final URIResolver uriResolver;
    private final XsltCompiler compiler;

    public XSpecRunner(URIResolver uriResolver) {
      this.processor = new Processor(false);
      this.uriResolver = uriResolver;
      this.compiler = processor.newXsltCompiler();
      compiler.setURIResolver(uriResolver);
    }

    public Processor getProcessor() {
      return this.processor;
    }

    public XdmNode runXSpec(DOMSource compiledXSpecSource) throws SaxonApiException {
      var executable = compiler.compile(compiledXSpecSource);
      var transformer = executable.load30();
      transformer.setURIResolver(uriResolver);

      var destination = new XdmDestination();
      transformer.callTemplate(XSPEC_MAIN_TEMPLATE, destination);
      return destination.getXdmNode();
    }
  }
}
