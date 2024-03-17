/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URISyntaxException;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UriBasenameTaskTest {

  private UriBasenameTask basename;

  @BeforeEach
  public void setUp() throws URISyntaxException {
    final Project project = new Project();
    basename = new UriBasenameTask();
    basename.setProject(project);
    basename.setProperty("test");
  }

  @Test
  public void testPlain() throws URISyntaxException {
    basename.setFile(new URI("foo/bar.baz"));
    basename.execute();
    assertEquals("bar.baz", basename.getProject().getProperty("test"));
  }

  @Test
  public void testAbsolute() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/bar.baz"));
    basename.execute();
    assertEquals("bar.baz", basename.getProject().getProperty("test"));
  }

  @Test
  public void testSuffix() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/bar.baz"));
    basename.setSuffix("baz");
    basename.execute();
    assertEquals("bar", basename.getProject().getProperty("test"));
  }

  @Test
  public void testDotSuffix() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/bar.baz"));
    basename.setSuffix(".baz");
    basename.execute();
    assertEquals("bar", basename.getProject().getProperty("test"));
  }

  @Test
  public void testSuffixNoMatch() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/bar.baz"));
    basename.setSuffix(".qux");
    basename.execute();
    assertEquals("bar.baz", basename.getProject().getProperty("test"));
  }

  @Test
  public void testWildcardSuffix() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/bar.baz"));
    basename.setSuffix(".*");
    basename.execute();
    assertEquals("bar", basename.getProject().getProperty("test"));
  }

  @Test
  public void testWildcardSuffixWithEscapedCharacters() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/foo%20bar.baz"));
    basename.setSuffix(".*");
    basename.execute();
    assertEquals("foo bar", basename.getProject().getProperty("test"));
  }

  @Test
  public void testWildcardSuffixNoMatch() throws URISyntaxException {
    basename.setFile(new URI("file:/foo/bar"));
    basename.setSuffix(".*");
    basename.execute();
    assertEquals("bar", basename.getProject().getProperty("test"));
  }
}
