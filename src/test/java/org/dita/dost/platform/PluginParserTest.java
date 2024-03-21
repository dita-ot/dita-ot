/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.dita.dost.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PluginParserTest {

  final File resourceDir = TestUtils.getResourceDir(PluginParserTest.class);
  final PluginParser p = new PluginParser(resourceDir);

  @BeforeEach
  public void setUp() throws Exception {
    p.setPluginDir(resourceDir);
    p.parse(new File(resourceDir, "plugin.xml"));
  }

  @Test
  public void testGetAllTemplates() {
    final Plugin f = p.getPlugin();
    assertEquals(
      Arrays.asList(new Value("dummy", "xsl/shell_template.xsl"), new Value("dummy", "xsl/shell2_template.xsl")),
      f.templates()
    );
  }

  @Test
  public void testRequirements() {
    final Plugin f = p.getPlugin();
    final Map<String, Boolean> exp = new HashMap<>();
    exp.put("foo", true);
    exp.put("bar", true);
    exp.put("baz", false);
    for (final PluginRequirement r : f.requiredPlugins()) {
      for (String p : r.getPlugins()) {
        assertTrue(exp.containsKey(p));
        assertEquals(exp.get(p), r.getRequired());
      }
    }
  }

  @Test
  public void testGetMeta() {
    final Plugin f = p.getPlugin();
    assertEquals("bar", f.getMeta("foo"));
    assertEquals("quxx", f.getMeta("baz"));
    assertNull(f.getMeta("undefined"));
  }

  @Test
  public void testValueFeature() {
    final Plugin f = p.getPlugin();
    assertEquals(asList("foo", "bar"), f.getFeature("type_text"));
    assertEquals(asList("foo", "bar"), f.getFeature("multiple_type_text"));
    assertNull(f.getFeature("undefined"));
  }

  @Test
  public void testFileValueFeature() {
    final Plugin f = p.getPlugin();
    assertEquals(
      asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
      f.getFeature("type_file")
    );
    assertEquals(
      asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
      f.getFeature("multiple_type_file")
    );
  }

  @Test
  public void testFileFeature() {
    final Plugin f = p.getPlugin();
    assertEquals(
      asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
      f.getFeature("file")
    );
    assertEquals(
      asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
      f.getFeature("multiple_file")
    );
  }
}
