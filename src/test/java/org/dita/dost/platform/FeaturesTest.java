/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static java.util.Arrays.asList;
import static org.dita.dost.platform.PluginParser.FEATURE_ELEM;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class FeaturesTest {

  private static Document doc;

  @BeforeAll
  public static void setUp() throws Exception {
    doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
  }

  @Test
  public void testGetLocation() {
    Features features = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .build();
    assertEquals(new File("base", "plugins").getAbsoluteFile(), features.pluginDir());
  }

  @Test
  public void testAddExtensionPoint() {
    final Features.Builder f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .setPluginId("plugin");
    f.addExtensionPoint("id", "name");
    try {
      f.addExtensionPoint(null, null);
      fail();
    } catch (final NullPointerException ex) {}
  }

  @Test
  public void testGetExtensionPoints() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .setPluginId("plugin")
      .addExtensionPoint("id", "name")
      .addExtensionPoint("id2", "name2")
      .build();

    assertEquals(2, f.extensionPoints().size());
    //    assertEquals(e, f.getExtensionPoints().get("id"));
    //    assertEquals(e2, f.getExtensionPoints().get("id2"));
  }

  @Test
  public void testGetFeature() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addFeature("foo", createElement("bar", null))
      .build();

    assertEquals(asList("bar"), f.getFeature("foo"));
  }

  @Test
  public void testGetAllFeatures() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addFeature("foo", createElement("bar", null))
      .addFeature("foo", createElement("baz", null))
      .addFeature("bar", createElement("qux", null))
      .build();

    final Map<String, List<String>> exp = new HashMap<>();
    exp.put("foo", asList("bar", "baz"));
    exp.put("bar", asList("qux"));

    assertEquals(exp, f.features());
  }

  @Test
  public void testAddFeature() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addFeature("foo", createElement(null, null))
      .addFeature("foo", createElement(" bar, baz ", null))
      .addFeature("foo", createElement("bar, baz", "file"))
      .build();

    assertEquals(
      asList(
        "bar",
        "baz",
        new File("base", "plugins" + File.separator + "bar").getAbsolutePath(),
        new File("base", "plugins" + File.separator + "baz").getAbsolutePath()
      ),
      f.getFeature("foo")
    );
  }

  @Test
  public void testAddRequireString() {
    try {
      final Features f = Features
        .builder()
        .setPluginDir(new File("base", "plugins").getAbsoluteFile())
        .setDitaDir(new File("base").getAbsoluteFile())
        .addRequire("foo")
        .addRequire(null)
        .build();
      fail();
    } catch (final NullPointerException e) {}
  }

  @Test
  public void testAddRequireStringString() {
    try {
      final Features f = Features
        .builder()
        .setPluginDir(new File("base", "plugins").getAbsoluteFile())
        .setDitaDir(new File("base").getAbsoluteFile())
        .addRequire("foo")
        .addRequire("foo", null)
        .addRequire(null, null)
        .build();
      fail();
    } catch (final NullPointerException e) {}
  }

  @Test
  public void testGetRequireListIter() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addRequire("foo | bar ")
      .addRequire("baz", "unrequired")
      .addRequire("qux", "required")
      .build();

    final Map<List<String>, Boolean> act = new HashMap<>();
    for (PluginRequirement requirement : f.requiredPlugins()) {
      final List<String> plugins = requirement.plugins().stream().sorted().toList();
      act.put(plugins, requirement.required());
    }

    final Map<List<String>, Boolean> exp = new HashMap<>();
    exp.put(Arrays.asList(" bar ", "foo "), Boolean.TRUE);
    exp.put(Arrays.asList("baz"), Boolean.FALSE);
    exp.put(Arrays.asList("qux"), Boolean.TRUE);

    assertEquals(exp, act);
  }

  @Test
  public void testAddMeta() {
    try {
      final Features f = Features
        .builder()
        .setPluginDir(new File("base", "plugins").getAbsoluteFile())
        .setDitaDir(new File("base").getAbsoluteFile())
        .addMeta("foo", "bar")
        .addMeta("foo", "baz")
        .addMeta("bar", "baz")
        .addMeta("bar", null)
        .build();
      fail();
    } catch (final NullPointerException e) {}
  }

  @Test
  public void testGetMeta() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addMeta("foo", "bar")
      .addMeta("foo", "baz")
      .addMeta("bar", "baz")
      .build();

    assertEquals("baz", f.getMeta("foo"));
    assertEquals("baz", f.getMeta("bar"));
    assertNull(f.getMeta("qux"));
  }

  @Test
  public void testAddTemplate() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addTemplate("foo")
      .addTemplate("foo")
      .addTemplate("bar")
      .build();
  }

  @Test
  public void testGetAllTemplates() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addTemplate("foo")
      .addTemplate("foo")
      .addTemplate("bar")
      .build();

    final List<String> act = f.templates().stream().sorted().toList();
    assertEquals(Arrays.asList("bar", "foo", "foo"), act);
  }

  private static Element createElement(final String value, final String type) {
    final Element feature = doc.createElement(FEATURE_ELEM);
    if (value != null) {
      feature.setAttribute("value", value);
    }
    if (type != null) {
      feature.setAttribute("type", type);
    }
    return feature;
  }
}
