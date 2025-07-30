/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StandardLoggerTest {

  private static final String MSG = "message ä";

  private StandardLogger logger;
  private ByteArrayOutputStream out;
  private ByteArrayOutputStream err;
  private static String EOL;

  @BeforeEach
  void setUp() {
    out = new ByteArrayOutputStream();
    err = new ByteArrayOutputStream();
    logger =
      new StandardLogger(
        new PrintStream(out, true, StandardCharsets.UTF_8),
        new PrintStream(err, true, StandardCharsets.UTF_8),
        Project.MSG_DEBUG,
        false
      );
  }

  @Test
  void debug() {
    logger.debug(MSG);

    assertOut(MSG);
    assertErr();
  }

  @ParameterizedTest
  @CsvSource(
    value = {
      "Message {0} {1} {2}, Message first second third",
      "Message {} {} {}, Message first second third",
      "Message {} {} {} suffix, Message first second third suffix",
      "{} {} {}, first second third",
      "Message {} {} {}., Message first second third.",
      "Message %s %s %s, Message first second third",
    }
  )
  void debug(final String msg, final String exp) {
    logger.debug(msg, "first", "second", "third");

    assertOut(exp);
    assertErr();
  }

  @Test
  void info() {
    logger.info(MSG);

    assertOut(MSG);
    assertErr();
  }

  @Test
  void warn() {
    logger.warn(MSG);

    assertOut();
    assertErr(MSG);
  }

  @Test
  void error() {
    logger.error(MSG);

    assertOut();
    assertErr(MSG);
  }

  private void assertOut() {
    assertEquals(0, out.size());
  }

  private void assertOut(final String exp) {
    assertEquals(exp + System.lineSeparator(), out.toString(StandardCharsets.UTF_8));
  }

  private void assertErr() {
    assertEquals(0, err.size());
  }

  private void assertErr(final String exp) {
    assertEquals(exp + System.lineSeparator(), err.toString(StandardCharsets.UTF_8));
  }
}
