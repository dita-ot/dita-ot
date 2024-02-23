/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.log;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class AbstractLoggerTest extends AbstractLogger {

  static List<Arguments> removeLevelPrefix() {
    return List.of(
      Arguments.of("[DOTJ037W][INFO] Message", "[DOTJ037W] Message"),
      Arguments.of("[DOTJ037W][INFO]: Message", "[DOTJ037W]: Message"),
      Arguments.of("[DOTJ037W] Message", "[DOTJ037W] Message"),
      Arguments.of("[DOTJ037W][Warning]: Message", "[DOTJ037W][Warning]: Message"),
      Arguments.of("[WARN][DOTJ037W]: Message", "[WARN][DOTJ037W]: Message"),
      Arguments.of("[WARN] Message", "[WARN] Message")
    );
  }

  @ParameterizedTest
  @MethodSource("removeLevelPrefix")
  void removeLevelPrefix_StringBuilder(String src, String exp) {
    assertEquals(exp, AbstractLogger.removeLevelPrefix(new StringBuilder(src)).toString());
  }

  @ParameterizedTest
  @MethodSource("removeLevelPrefix")
  void removeLevelPrefix_String(String src, String exp) {
    assertEquals(exp, AbstractLogger.removeLevelPrefix(src));
  }

  private String expMessage;
  private Throwable expThrowable;
  private Integer expLevel;

  @BeforeEach
  void setUp() {
    useColor = true;
    msgOutputLevel = Project.MSG_DEBUG;
    expMessage = null;
    expThrowable = null;
    expLevel = null;
  }

  @Test
  void logArguments_filter() {
    msgOutputLevel = Project.MSG_WARN;

    log("Message", null, null, Project.MSG_INFO);
  }

  @Test
  void logArguments_filter_default() {
    expMessage = "Message";
    expThrowable = null;
    expLevel = Project.MSG_DEBUG;

    log("Message", new Object[] {}, null, Project.MSG_DEBUG);
  }

  @ParameterizedTest
  @ValueSource(strings = { "Message {} {}", "Message {0} {1}", "Message %s %s" })
  void logArguments(String msg) {
    useColor = true;
    msgOutputLevel = Project.MSG_INFO;
    expMessage = "Message first second";
    expThrowable = null;
    expLevel = Project.MSG_INFO;

    log(msg, new Object[] { "first", "second" }, null, Project.MSG_INFO);
  }

  @Override
  public void log(String msg, Throwable t, int level) {
    assertEquals(expMessage, msg);
    assertEquals(expThrowable, t);
    assertEquals(expLevel, level);
  }
}
