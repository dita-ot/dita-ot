/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xmlresolver.Resolver;

public class XSpecTest {

  public static final String XSPEC_NS = "http://www.jenitennison.com/xslt/xspec";
  private static TransformerFactory transformerFactory;
  private static Transformer transformer;
  private static URIResolver resolver;
  private static XSpecRunner runner;

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

  @BeforeAll
  public static void setUpClass() throws TransformerException {
    final File resourceDir = TestUtils.getResourceDir(XSpecTest.class);
    final String resourceDirUri = resourceDir.toURI() + "/";
    transformerFactory = TransformerFactory.newInstance();
    final File ditaDir = new File(
      Optional.ofNullable(System.getProperty("dita.dir")).orElse("src" + File.separator + "main")
    )
      .getAbsoluteFile();
    CatalogUtils.setDitaDir(ditaDir);
    final Resolver catalogResolver = CatalogUtils.getCatalogResolver();
    resolver = new ClassPathResolver(catalogResolver);
    transformerFactory.setURIResolver(resolver);
    final Source stylesheet = resolver.resolve("src/compiler/compile-xslt-tests.xsl", resourceDirUri);
    transformer = transformerFactory.newTransformer(stylesheet);
    transformer.setURIResolver(resolver);
    runner = new XSpecRunner(resolver);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("getFiles")
  public void testXSpec(File xspec) throws TransformerException, SaxonApiException {
    final DOMSource compiledXSpec = getCompiledXSpec(xspec);
    final XdmNode results = runner.runXSpec(compiledXSpec);
    parseXSpecResults(results);
  }

  private static DOMSource getCompiledXSpec(File xspec) throws TransformerException {
    final DOMResult stylesheet = new DOMResult();
    transformer.transform(new StreamSource(xspec), stylesheet);
    return new DOMSource(stylesheet.getNode());
  }

  private void parseXSpecResults(XdmNode results) throws SaxonApiException {
    List<Executable> testResults = new ArrayList<>();
    Processor processor = runner.getProcessor(); // If not already shared
    XPathCompiler xpath = processor.newXPathCompiler();
    xpath.declareNamespace("x", XSPEC_NS);

    XPathSelector failingScenarios = xpath
      .compile("//x:scenario[x:test[@successful='false' and not(@pending)]]")
      .load();
    failingScenarios.setContextItem(results);

    for (XdmItem scenarioItem : failingScenarios) {
      XdmNode scenario = (XdmNode) scenarioItem;
      XPathSelector resultQuery = xpath.compile("x:result/*").load();
      resultQuery.setContextItem(scenario);
      XdmItem actual = resultQuery.evaluateSingle();
      String act = actual != null ? actual.toString() : "";
      XPathSelector expectQuery = xpath.compile("x:test/x:expect/*").load();
      expectQuery.setContextItem(scenario);
      XdmItem expected = expectQuery.evaluateSingle();
      String exp = expected != null ? expected.toString() : "";
      testResults.add(() -> Assertions.assertEquals(exp, act));
    }

    Assertions.assertAll(testResults);
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
