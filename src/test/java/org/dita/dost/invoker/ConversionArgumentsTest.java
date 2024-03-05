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

  @Test
  public void input_short() {
    arguments.parse(new String[] { "-i", "foo.dita" });

    assertEquals("foo.dita", arguments.definedProps.get("args.input"));
  }

  @Test
  public void input_long() {
    arguments.parse(new String[] { "--input=foo.dita" });

    assertEquals("foo.dita", arguments.definedProps.get("args.input"));
  }

  @ParameterizedTest
  @MethodSource
  public void input_reserved(String argument) {
    assertThrows(BuildException.class, () -> arguments.parse(new String[] { argument }));
  }

  public static Stream<Arguments> input_reserved() {
    return Stream
      .of("args.input", "output.dir", "args.filter", "dita.temp.dir")
      .flatMap(name -> Stream.of("-D", "--").map(prefix -> Arguments.of("%s%s=value".formatted(prefix, name))));
  }

  @Test
  public void format_short() {
    arguments.parse(new String[] { "-f", "html5" });

    assertEquals("html5", arguments.definedProps.get("transtype"));
  }

  @Test
  public void format_long() {
    arguments.parse(new String[] { "--format=html5" });

    assertEquals("html5", arguments.definedProps.get("transtype"));
  }

  @Test
  public void resource_short_multipleOptions() {
    arguments.parse(new String[] { "-r", "foo.dita", "-r", "bar.dita" });

    assertEquals("foo.dita" + pathSeparator + "bar.dita", arguments.definedProps.get("args.resources"));
  }

  @Test
  public void resource_long_multipleOptions() {
    arguments.parse(new String[] { "--resource=foo.dita", "--resource=bar.dita" });

    assertEquals("foo.dita" + pathSeparator + "bar.dita", arguments.definedProps.get("args.resources"));
  }

  @Test
  public void filter_multipleOptions() {
    arguments.parse(new String[] { "--filter=foo.ditaval", "--filter=bar.ditaval" });

    assertEquals(
      new File("foo.ditaval").getAbsolutePath() + pathSeparator + new File("bar.ditaval").getAbsolutePath(),
      arguments.definedProps.get("args.filter")
    );
  }

  @Test
  public void filter_listValue() {
    arguments.parse(new String[] { "--filter=foo.ditaval" + pathSeparator + "bar.ditaval" });

    assertEquals(
      new File("foo.ditaval").getAbsolutePath() + pathSeparator + new File("bar.ditaval").getAbsolutePath(),
      arguments.definedProps.get("args.filter")
    );
  }
}
