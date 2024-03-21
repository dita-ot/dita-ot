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
import java.util.*;
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
    assertEquals(
      new File("base", "plugins").getAbsoluteFile(),
      Features
        .builder()
        .setPluginDir(new File("base", "plugins").getAbsoluteFile())
        .setDitaDir(new File("base").getAbsoluteFile())
        .build()
        .getPluginDir()
    );
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

    assertEquals(2, f.getExtensionPoints().size());
    //    assertEquals(e, f.getExtensionPoints().get("id"));
    //    assertEquals(e2, f.getExtensionPoints().get("id2"));
  }

  @Test
  public void testGetFeature() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addFeature("foo", getElement("bar", null))
      .build();

    assertEquals(asList("bar"), f.getFeature("foo"));
  }

  @Test
  public void testGetAllFeatures() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addFeature("foo", getElement("bar", null))
      .addFeature("foo", getElement("baz", null))
      .addFeature("bar", getElement("qux", null))
      .build();

    final Map<String, List<String>> exp = new HashMap<>();
    exp.put("foo", asList("bar", "baz"));
    exp.put("bar", asList("qux"));

    assertEquals(exp, f.getAllFeatures());
  }

  @Test
  public void testAddFeature() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addFeature("foo", getElement(null, null))
      .addFeature("foo", getElement(" bar, baz ", null))
      .addFeature("foo", getElement("bar, baz", "file"))
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
    final Iterator<PluginRequirement> requirements = f.getRequireListIter();
    while (requirements.hasNext()) {
      final PluginRequirement requirement = requirements.next();
      final List<String> plugins = new ArrayList<>();
      for (final Iterator<String> ps = requirement.getPlugins(); ps.hasNext();) {
        plugins.add(ps.next());
      }
      Collections.sort(plugins);
      act.put(plugins, requirement.getRequired());
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
      .addTemplate(new Value("base", "foo"))
      .addTemplate(new Value("base", "foo"))
      .addTemplate(new Value("base", "bar"))
      .addTemplate(null)
      .build();
  }

  @Test
  public void testGetAllTemplates() {
    final Features f = Features
      .builder()
      .setPluginDir(new File("base", "plugins").getAbsoluteFile())
      .setDitaDir(new File("base").getAbsoluteFile())
      .addTemplate(new Value("base", "foo"))
      .addTemplate(new Value("base", "foo"))
      .addTemplate(new Value("base", "bar"))
      .addTemplate(null)
      .build();

    final List<Value> act = f.getAllTemplates();
    act.sort((a0, a1) -> {
      if (a0 == null || a1 == null) {
        return -1;
      }
      return Objects.compare(a0.value(), a1.value(), String::compareTo);
    });
    assertEquals(
      Arrays.asList(null, new Value("base", "bar"), new Value("base", "foo"), new Value("base", "foo")),
      act
    );
  }

  private static Element getElement(final String value, final String type) {
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
