/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static java.util.Arrays.asList;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.platform.PluginParser.FEATURE_ELEM;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.TestUtils;
import org.dita.dost.log.DITAOTLogger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FileGeneratorTest {

  final File resourceDir = TestUtils.getResourceDir(FileGeneratorTest.class);
  private File tempDir;

  private static File tempFile;
  private static final Hashtable<String, List<Value>> features = new Hashtable<>();

  static {
    features.put(
      "element",
      asList(new Value.StringValue(null, "foo"), new Value.StringValue(null, "bar"), new Value.StringValue(null, "baz"))
    );
    features.put(
      "attribute",
      asList(new Value.StringValue(null, "foo"), new Value.StringValue(null, "bar"), new Value.StringValue(null, "baz"))
    );
  }

  private static final Map<String, Plugin> plugins = new HashMap<>();

  static {
    Document doc;
    try {
      doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    } catch (final ParserConfigurationException e) {
      throw new RuntimeException(e);
    }
    final Features.Builder a = Features.builder();
    final Element aFeature = doc.createElement(FEATURE_ELEM);
    aFeature.setAttribute("value", "foo,bar,baz");
    aFeature.setAttribute("type", "text");
    a.addFeature("element", aFeature);
    plugins.put("a", a.build());
    final Features.Builder b = Features.builder();
    final Element bFeature = doc.createElement(FEATURE_ELEM);
    bFeature.setAttribute("value", "foo,bar,baz");
    bFeature.setAttribute("type", "text");
    b.addFeature("attribute", bFeature);
    plugins.put("b", b.build());
  }

  @BeforeEach
  public void setUp() throws Exception {
    tempDir = TestUtils.createTempDir(getClass());
    tempFile = new File(tempDir, "dummy_template.xml");
    copyFile(new File(resourceDir, "src" + File.separator + "dummy_template.xml"), tempFile);
  }

  @Test
  public void testGenerate() throws IOException, SAXException {
    final FileGenerator f = new FileGenerator(features, plugins);
    f.generate(tempFile);

    assertXMLEqual(
      new InputSource(new File(resourceDir, "exp" + File.separator + "dummy.xml").toURI().toString()),
      new InputSource(new File(tempDir, "dummy.xml").toURI().toString())
    );
  }

  @AfterEach
  public void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }

  private abstract static class AbstractAction implements IAction {

    protected List<Value> inputs = new ArrayList<>();
    protected Map<String, String> params = new HashMap<>();
    protected Map<String, Plugin> features;
    protected boolean useClasspath;

    @Override
    public void setInput(final List<Value> input) {
      inputs.addAll(input);
    }

    @Override
    public void addParam(final String name, final String value) {
      params.put(name, value);
    }

    @Override
    public void setFeatures(final Map<String, Plugin> features) {
      this.features = features;
    }

    @Override
    public abstract String getResult();

    @Override
    public void setLogger(final DITAOTLogger logger) {
      // NOOP
    }

    @Override
    public void setUseClasspath(boolean useClasspath) {
      this.useClasspath = useClasspath;
    }
  }

  public static class ElementAction extends AbstractAction {

    @Override
    public void getResult(ContentHandler output) throws SAXException {
      final Map<String, String> paramsExp = new HashMap<>();
      paramsExp.put(FileGenerator.PARAM_TEMPLATE, tempFile.getAbsolutePath());
      paramsExp.put("id", "element");
      paramsExp.put("behavior", this.getClass().getName());
      assertEquals(paramsExp, params);
      final List<Value> inputExp = Arrays.asList(
        new Value.StringValue(null, "foo"),
        new Value.StringValue(null, "bar"),
        new Value.StringValue(null, "baz")
      );
      assertEquals(inputExp, inputs);
      assertEquals(FileGeneratorTest.plugins, features);
      output.startElement(NULL_NS_URI, "foo", "foo", new AttributesBuilder().add("bar", "baz").build());
      output.characters(new char[] { 'q', 'u', 'z' }, 0, 3);
      output.endElement(NULL_NS_URI, "foo", "foo");
    }

    @Override
    public String getResult() {
      throw new UnsupportedOperationException();
    }
  }

  public static class AttributeAction extends AbstractAction {

    @Override
    public String getResult() {
      final Map<String, String> paramsExp = new HashMap<>();
      paramsExp.put(FileGenerator.PARAM_TEMPLATE, tempFile.getAbsolutePath());
      assertEquals(paramsExp, params);
      final List<Value> inputExp = List.of(new Value.StringValue(null, "attribute"));
      assertEquals(inputExp, inputs);
      assertEquals(FileGeneratorTest.plugins, features);
      return "bar";
    }

    @Override
    public void getResult(ContentHandler output) {
      throw new UnsupportedOperationException();
    }
  }
}
