/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.dita.dost.invoker.Main.ANT_OUTPUT_DIR;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.dita.dost.project.Project.Deliverable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class MainTest {

  public static Stream<Arguments> data() {
    return Stream.of(Arguments.of(""), Arguments.of("/"));
  }

  private Main main;
  private Path current;

  @BeforeEach
  public void setUp() throws Exception {
    this.main = new Main();
    this.current = new File("").getAbsoluteFile().toPath();
  }

  @ParameterizedTest
  @MethodSource("data")
  public void withoutEither(String suffix) {
    assertEquals(current.resolve("out"), getOutputDir(null, null, suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void relativeArgument(String suffix) {
    assertEquals(current.resolve("baz/qux"), getOutputDir(null, "baz/qux", suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void absoluteArgument(String suffix) {
    assertEquals(Paths.get("/baz/qux"), getOutputDir(null, "/baz/qux", suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void relativeProject(String suffix) {
    assertEquals(current.resolve("out/foo/bar"), getOutputDir("foo/bar", null, suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void absoluteProject(String suffix) {
    assertEquals(Paths.get("/foo/bar"), getOutputDir("/foo/bar", null, suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void relativeArgument_relativeProject(String suffix) {
    assertEquals(current.resolve("baz/qux/foo/bar"), getOutputDir("foo/bar", "baz/qux", suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void absoluteArgument_relativeProject(String suffix) {
    assertEquals(Paths.get("/baz/qux/foo/bar"), getOutputDir("foo/bar", "/baz/qux", suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void relativeArgument_absoluteProject(String suffix) {
    assertEquals(Paths.get("/foo/bar"), getOutputDir("/foo/bar", "baz/qux", suffix));
  }

  @ParameterizedTest
  @MethodSource("data")
  public void absoluteArgument_absoluteProject(String suffix) {
    assertEquals(Paths.get("/foo/bar"), getOutputDir("/foo/bar", "/baz/qux", suffix));
  }

  private Path getOutputDir(final String output, final String arg, String suffix) {
    final Deliverable deliverable = new Deliverable(
      null,
      null,
      null,
      output != null ? URI.create(output + suffix) : null,
      null
    );
    final Map<String, Object> props = new HashMap<>();
    if (arg != null) {
      props.put(ANT_OUTPUT_DIR, arg + suffix);
    }
    return main.getOutputDir(deliverable, props);
  }
}
