/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.CONF_SUPPORTED_IMAGE_EXTENSIONS;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import org.dita.dost.TestUtils;
import org.dita.dost.util.Constants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public class IntegratorTest {

  final File resourceDir = TestUtils.getResourceDir(IntegratorTest.class);
  private final File expDir = new File(resourceDir, "exp");
  private File tempDir;

  @BeforeEach
  public void setUp() throws IOException {
    tempDir = TestUtils.createTempDir(getClass());
    TestUtils.copy(new File(resourceDir, "src"), tempDir);
  }

  @Test
  public void testConvertMessage() {
    assertEquals(
      "The index term ''{1}'' uses both an index-see element and {0} element. Convert the index-see element to ''index-see-also''.",
      Integrator.convertMessage(
        "The index term '%2' uses both an index-see element and %1 element. Convert the index-see element to 'index-see-also'."
      )
    );
    assertEquals("{0} foo {1} bar {2}", Integrator.convertMessage("%1 foo %2 bar %3"));
    assertEquals("'{0}", Integrator.convertMessage("{0}"));
    assertEquals("foo bar baz", Integrator.convertMessage("  foo  bar\nbaz  "));
  }

  @Test
  public void testVersionPattern() {
    assertTrue(PluginParser.VERSION_PATTERN.matcher("0").matches());
    assertTrue(PluginParser.VERSION_PATTERN.matcher("1").matches());
    assertTrue(PluginParser.VERSION_PATTERN.matcher("1.0").matches());
    assertTrue(PluginParser.VERSION_PATTERN.matcher("1.0.0").matches());
    assertTrue(PluginParser.VERSION_PATTERN.matcher("1.0.0.abc123").matches());
    assertTrue(PluginParser.VERSION_PATTERN.matcher("012.012.012.ABCabc123-_").matches());
    assertFalse(PluginParser.VERSION_PATTERN.matcher("").matches());
    assertFalse(PluginParser.VERSION_PATTERN.matcher(" 1").matches());
    assertFalse(PluginParser.VERSION_PATTERN.matcher("A").matches());
  }

  @Test
  public void testIdPattern() {
    assertTrue(PluginParser.ID_PATTERN.matcher("foo").matches());
    assertTrue(PluginParser.ID_PATTERN.matcher("1foo.2-_.bar").matches());
    assertFalse(PluginParser.ID_PATTERN.matcher("").matches());
    assertFalse(PluginParser.ID_PATTERN.matcher(" foo ").matches());
    assertFalse(PluginParser.ID_PATTERN.matcher(".foo").matches());
  }

  @Test
  public void testExecute() throws Exception {
    final File libDir = new File(tempDir, "lib");
    if (!libDir.exists() && !libDir.mkdirs()) {
      throw new IOException("Failed to create directory " + libDir);
    }
    final File resourcesDir = new File(tempDir, "resources");
    if (!resourcesDir.exists() && !resourcesDir.mkdirs()) {
      throw new IOException("Failed to create directory " + resourcesDir);
    }

    final Integrator i = new Integrator(tempDir);
    i.setProperties(new File(tempDir, "integrator.properties"));
    i.setLogger(new TestUtils.TestLogger());
    i.execute();

    final Properties expProperties = getProperties(
      new File(
        expDir,
        "lib" +
        File.separator +
        Integrator.class.getPackage().getName() +
        File.separator +
        Constants.GEN_CONF_PROPERTIES
      )
    );
    expProperties.setProperty("plugin.org.dita.base.dir", new File("plugins/org.dita.base").getPath());
    expProperties.setProperty("plugin.base.dir", "plugins/base");
    expProperties.setProperty("plugin.dummy.dir", "plugins/dummy");
    final Properties actProperties = getProperties(
      new File(
        tempDir,
        "config" +
        File.separator +
        Integrator.class.getPackage().getName() +
        File.separator +
        Constants.GEN_CONF_PROPERTIES
      )
    );
    // supported_image_extensions needs to be tested separately
    assertEquals(
      new HashSet<>(Arrays.asList(expProperties.getProperty(CONF_SUPPORTED_IMAGE_EXTENSIONS).split(";"))),
      new HashSet<>(Arrays.asList(expProperties.getProperty(CONF_SUPPORTED_IMAGE_EXTENSIONS).split(";")))
    );
    expProperties.remove(CONF_SUPPORTED_IMAGE_EXTENSIONS);
    actProperties.remove(CONF_SUPPORTED_IMAGE_EXTENSIONS);
    assertEquals(expProperties, actProperties);

    assertXMLEqual(
      new InputSource(new File(expDir, "build.xml").toURI().toString()),
      new InputSource(new File(tempDir, "build.xml").toURI().toString())
    );
    assertXMLEqual(
      new InputSource(new File(expDir, "catalog.xml").toURI().toString()),
      new InputSource(new File(tempDir, "catalog.xml").toURI().toString())
    );
    assertXMLEqual(
      new InputSource(new File(expDir, "xsl" + File.separator + "shell.xsl").toURI().toString()),
      new InputSource(new File(tempDir, "xsl" + File.separator + "shell.xsl").toURI().toString())
    );
    assertXMLEqual(
      new InputSource(
        new File(expDir, "plugins" + File.separator + "dummy" + File.separator + "xsl" + File.separator + "shell.xsl")
          .toURI()
          .toString()
      ),
      new InputSource(
        new File(tempDir, "plugins" + File.separator + "dummy" + File.separator + "xsl" + File.separator + "shell.xsl")
          .toURI()
          .toString()
      )
    );
    assertXMLEqual(
      new InputSource(Paths.get(expDir.getAbsolutePath(), "config", "messages.xml").toUri().toString()),
      new InputSource(Paths.get(tempDir.getAbsolutePath(), "config", "messages.xml").toUri().toString())
    );
  }

  @Test
  public void testExecute_missingFile() throws Exception {
    assertThrows(
      UncheckedIOException.class,
      () -> {
        Files.delete(tempDir.toPath().resolve(Paths.get("plugins", "dummy", "build.xml")));

        final File libDir = new File(tempDir, "lib");
        if (!libDir.exists() && !libDir.mkdirs()) {
          throw new IOException("Failed to create directory " + libDir);
        }
        final File resourcesDir = new File(tempDir, "resources");
        if (!resourcesDir.exists() && !resourcesDir.mkdirs()) {
          throw new IOException("Failed to create directory " + resourcesDir);
        }

        final Integrator i = new Integrator(tempDir);
        i.setProperties(new File(tempDir, "integrator.properties"));
        i.setLogger(new TestUtils.TestLogger(false));
        i.execute();
      }
    );
  }

  private Properties getProperties(final File f) throws IOException {
    final Properties p = new Properties();
    try (InputStream in = Files.newInputStream(f.toPath())) {
      p.load(in);
    }
    return p;
  }

  @AfterEach
  public void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
