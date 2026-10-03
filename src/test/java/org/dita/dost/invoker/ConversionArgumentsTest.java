/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static java.io.File.pathSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.apache.tools.ant.BuildException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ConversionArgumentsTest {

  private ConversionArguments arguments;

  @BeforeEach
  public void setUp() {
    arguments = new ConversionArguments();
  }

  static Stream<Arguments> input() {
    return Stream.of(
      Arguments.of(List.of("-i", "foo.dita"), List.of("foo.dita")),
      Arguments.of(List.of("--input=foo.dita"), List.of("foo.dita")),
      Arguments.of(List.of("-i", "foo.dita", "-i", "bar.dita"), List.of("foo.dita", "bar.dita")),
      Arguments.of(List.of("--input=foo.dita", "--input=bar.dita"), List.of("foo.dita", "bar.dita"))
    );
  }

  @ParameterizedTest
  @MethodSource("input")
  public void input(List<String> args, List<String> exp) {
    arguments.parse(args.toArray(new String[] {}));

    assertEquals(exp, arguments.inputs);
  }

  @Test
  public void input_absolutePath() throws URISyntaxException {
    var file = Paths.get(getClass().getResource("/messages.xml").toURI()).toAbsolutePath();
    arguments.parse(new String[] { "--input=%s".formatted(file) });

    assertEquals(file.toString(), arguments.inputs.get(0));
  }

  @Test
  public void input_relativePath() throws URISyntaxException {
    var currentDir = Paths.get(".").toAbsolutePath();
    var absoluteFile = Paths.get(getClass().getResource("/messages.xml").toURI()).toAbsolutePath();
    var file = currentDir.relativize(absoluteFile);
    arguments.parse(new String[] { "--input=%s".formatted(file) });

    assertEquals(absoluteFile.toString(), arguments.inputs.get(0));
  }

  @Test
  public void input_uri() {
    arguments.parse(new String[] { "--input=https://example.com/foo.dita" });

    assertEquals("https://example.com/foo.dita", arguments.inputs.get(0));
  }

  @ParameterizedTest
  @MethodSource
  public void input_reserved(String argument) {
    assertThrows(BuildException.class, () -> arguments.parse(new String[] { argument }));
  }

  public static Stream<Arguments> input_reserved() {
    return Stream
      .of("args.input", "output.dir", "args.filter", "dita.temp.dir")
      .flatMap(name -> Stream.of("-D").map(prefix -> Arguments.of("%s%s=value".formatted(prefix, name))));
  }

  static Stream<Arguments> format() {
    return Stream.of(Arguments.of(List.of("-f", "html5")), Arguments.of(List.of("--format=html5")));
  }

  @ParameterizedTest
  @MethodSource("format")
  public void format(List<String> args) {
    arguments.parse(args.toArray(new String[] {}));

    assertEquals(List.of("html5"), arguments.formats);
  }

  @Test
  public void deliverable() {
    arguments.parse(new String[] { "--project=project.yaml", "--deliverable", "pdf", "--deliverable=html5" });

    assertEquals(List.of("pdf", "html5"), arguments.deliverables);
  }

  @Test
  public void deliverable_whenProjectNotDefined_shouldThrowException() {
    assertThrows(CliException.class, () -> arguments.parse(new String[] { "--deliverable", "pdf" }));
  }

  static Stream<Arguments> resource() {
    return Stream.of(
      Arguments.of(List.of("-r", "foo.dita", "-r", "bar.dita")),
      Arguments.of(List.of("--resource=foo.dita", "--resource=bar.dita"))
    );
  }

  @ParameterizedTest
  @MethodSource("resource")
  public void resource(List<String> args) {
    arguments.parse(args.toArray(new String[] {}));

    assertEquals("foo.dita" + pathSeparator + "bar.dita", arguments.definedProps.get("args.resources"));
  }

  static Stream<Arguments> filter() {
    return Stream.of(
      Arguments.of(List.of("--filter=foo.ditaval", "--filter=bar.ditaval")),
      Arguments.of(List.of("--filter=foo.ditaval" + pathSeparator + "bar.ditaval"))
    );
  }

  @ParameterizedTest
  @MethodSource("filter")
  public void filter(List<String> args) {
    arguments.parse(args.toArray(new String[] {}));

    assertEquals(
      new File("foo.ditaval").getAbsolutePath() + pathSeparator + new File("bar.ditaval").getAbsolutePath(),
      arguments.definedProps.get("args.filter")
    );
  }
}
