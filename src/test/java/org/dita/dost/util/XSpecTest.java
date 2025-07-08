/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta, 2025 David Bertalan
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
import org.dita.dost.TestUtils;
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
    final File resourceDir = TestUtils.getResourceDir(XSpecTest.class);
    final String resourceDirUri = resourceDir.toURI() + "/";
    TransformerFactory transformerFactory = TransformerFactory.newInstance();
    final File ditaDir = new File(
      Optional.ofNullable(System.getProperty("dita.dir")).orElse("src" + File.separator + "main")
    )
      .getAbsoluteFile();
    CatalogUtils.setDitaDir(ditaDir);
    final Resolver catalogResolver = CatalogUtils.getCatalogResolver();
    URIResolver resolver = new ClassPathResolver(catalogResolver);
    transformerFactory.setURIResolver(resolver);
    final Source stylesheet = resolver.resolve("src/compiler/compile-xslt-tests.xsl", resourceDirUri);
    transformer = transformerFactory.newTransformer(stylesheet);
    transformer.setURIResolver(resolver);
    runner = new XSpecRunner(resolver);
  }

  @TestFactory
  Stream<DynamicNode> xspecTests() {
    List<File> files = getFiles(); // or any other filter

    return files.stream().map(file -> dynamicContainer(file.getName(), testXSpec(file)));
  }

  public Stream<DynamicTest> testXSpec(File xspec) {
    try {
      final DOMSource compiledXSpec = getCompiledXSpec(xspec);
      final XdmNode results = runner.runXSpec(compiledXSpec);
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
    List<DynamicTest> dynamicTests = new ArrayList<>();
    Processor processor = runner.getProcessor();
    XPathCompiler xpath = processor.newXPathCompiler();
    xpath.declareNamespace("x", XSPEC_NS);

    String xpathFailingScenario = "//x:scenario[x:test[@successful='false' and not(@pending)]]";
    XPathSelector failingScenarios = xpath.compile(xpathFailingScenario).load();
    failingScenarios.setContextItem(results);
    processFailingScenarios(failingScenarios, xpath, dynamicTests);

    String xpathSuccessfulScenarios = "//x:scenario[x:test[@successful='true' and not(@pending)]]";
    XPathSelector successfulScenarios = xpath.compile(xpathSuccessfulScenarios).load();
    successfulScenarios.setContextItem(results);
    processSuccessfulScenarios(successfulScenarios, xpath, dynamicTests);

    String xpathSkippedScenarios = "//x:scenario[x:test[@pending]]";
    XPathSelector skippedScenarios = xpath.compile(xpathSkippedScenarios).load();
    skippedScenarios.setContextItem(results);
    processSkippedScenarios(skippedScenarios, xpath, dynamicTests);

    return dynamicTests.stream();
  }

  private void processSuccessfulScenarios(
    XPathSelector successfulScenarios,
    XPathCompiler xpath,
    List<DynamicTest> dynamicTests
  ) throws SaxonApiException {
    for (XdmItem scenarioItem : successfulScenarios) {
      XdmNode scenario = (XdmNode) scenarioItem;
      String label = getLabel(xpath, scenario);

      dynamicTests.add(DynamicTest.dynamicTest(label, () -> Assertions.assertTrue(true)));
    }
  }

  private void processSkippedScenarios(
    XPathSelector skippedScenarios,
    XPathCompiler xpath,
    List<DynamicTest> dynamicTests
  ) throws SaxonApiException {
    for (XdmItem scenarioItem : skippedScenarios) {
      XdmNode scenario = (XdmNode) scenarioItem;
      String label = getLabel(xpath, scenario);

      dynamicTests.add(DynamicTest.dynamicTest(label, this::pending));
    }
  }

  private void pending() {
    throw new TestAbortedException("Pending");
  }

  private void processFailingScenarios(
    XPathSelector failingScenarios,
    XPathCompiler xpath,
    List<DynamicTest> dynamicTests
  ) throws SaxonApiException {
    for (XdmItem scenarioItem : failingScenarios) {
      XdmNode scenario = (XdmNode) scenarioItem;

      String actual = getNodeText(xpath, scenario, "x:result/*");
      String expected = getNodeText(xpath, scenario, "x:test/x:expect/*");
      if (expected.isEmpty()) {
        expected = getNodeText(xpath, scenario, "string(x:test/x:expect/@select)");
      }
      String finalExpected = expected;

      String label = getLabel(xpath, scenario);

      dynamicTests.add(DynamicTest.dynamicTest(label, () -> Assertions.assertEquals(finalExpected, actual, label)));
    }
  }

  private static String getLabel(XPathCompiler xpath, XdmNode scenario) throws SaxonApiException {
    String mainLabel = getNodeText(xpath, scenario, "ancestor::x:scenario/x:label/text()");
    String subLabel = getNodeText(xpath, scenario, "x:label/text()");
    return mainLabel + (mainLabel.isEmpty() ? "" : " : ") + subLabel;
  }

  private static String getNodeText(XPathCompiler xpath, XdmNode context, String expr) throws SaxonApiException {
    XPathSelector selector = xpath.compile(expr).load();
    selector.setContextItem(context);
    XdmItem item = selector.evaluateSingle();
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
      XsltExecutable executable = compiler.compile(compiledXSpecSource);
      Xslt30Transformer transformer = executable.load30();
      transformer.setURIResolver(uriResolver);

      XdmDestination destination = new XdmDestination();
      transformer.callTemplate(XSPEC_MAIN_TEMPLATE, destination);
      return destination.getXdmNode();
    }
  }
}
