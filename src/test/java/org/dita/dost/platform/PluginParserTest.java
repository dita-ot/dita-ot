/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.dita.dost.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PluginParserTest {

  private final File resourceDir = TestUtils.getResourceDir(PluginParserTest.class);

  private PluginParser p;

  @BeforeEach
  public void setUp() {
    p = new PluginParser(resourceDir);
    p.setPluginDir(resourceDir);
  }

  @Test
  public void test() throws Exception {
    p.parse(new File(resourceDir, "plugin.xml"));
    var exp = new Features(
      "dummy",
      new SemVer(1, 2, 3),
      resourceDir,
      resourceDir,
      Map.of(),
      Map.of(
        "type_file",
        List.of(new File(resourceDir, "foo").getPath(), new File(resourceDir, "bar").getPath()),
        "file",
        List.of(new File(resourceDir, "foo").getPath(), new File(resourceDir, "bar").getPath()),
        "multiple_type_file",
        List.of(new File(resourceDir, "foo").getPath(), new File(resourceDir, "bar").getPath()),
        "multiple_file",
        List.of(new File(resourceDir, "foo").getPath(), new File(resourceDir, "bar").getPath()),
        "multiple_type_text",
        List.of("foo", "bar"),
        "type_text",
        List.of("foo", "bar")
      ),
      List.of(
        new PluginRequirement(List.of("foo"), true),
        new PluginRequirement(List.of("bar"), true),
        new PluginRequirement(List.of("baz"), false)
      ),
      Map.of("foo", "bar", "baz", "quxx"),
      List.of("xsl/shell_template.xsl", "xsl/shell2_template.xsl")
    );
    final Plugin act = p.getPlugin();
    assertEquals(exp.features(), act.features());
    assertEquals(exp, act);
  }

  @Test
  public void testNull() throws Exception {
    p.parse(new File(resourceDir, "plugin_null.xml"));
    var exp = Features
      .builder()
      .setDitaDir(resourceDir)
      .setPluginDir(resourceDir)
      .setPluginId("dummy")
      .setPluginVersion("0.0.0")
      .build();
    assertEquals(exp, p.getPlugin());
  }
}
