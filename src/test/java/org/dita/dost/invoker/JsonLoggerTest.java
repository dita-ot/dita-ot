/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JsonLoggerTest {

  private final ObjectReader objectReader = new ObjectMapper()
    .findAndRegisterModules()
    .readerForArrayOf(LogEntry.class);
  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
  private JsonLogger logger;
  private ByteArrayOutputStream buf;
  private PrintStream out;

  @BeforeEach
  void setUp() {
    logger = new JsonLogger();
    buf = new ByteArrayOutputStream();
    out = new PrintStream(buf, true, StandardCharsets.UTF_8);
    logger.setOutputPrintStream(out);
    logger.setMessageOutputLevel(Project.MSG_INFO);
    logger.setArray(true);
    logger.setClock(clock);

    logger.buildStarted(new BuildEvent(new Project()));
  }

  @AfterEach
  void tearDown() {
    out.close();
  }

  @Test
  void buildFinished() throws IOException {
    var event = new BuildEvent(new Project());

    logger.buildFinished(event);

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(2, act.length);
    assertEquals(entry("info", "BUILD SUCCESSFUL"), act[0]);
  }

  @Test
  void buildFinished_error() throws IOException {
    var event = new BuildEvent(new Project());
    event.setException(new DITAOTException("test"));

    logger.buildFinished(event);

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(2, act.length);
    assertEquals("fatal", act[0].level);
  }

  @Test
  void targetStarted() throws IOException {
    final Target target = new Target();
    target.setName("target");
    target.setDescription("description");
    var event = new BuildEvent(target);
    event.setMessage("message", Project.MSG_INFO);

    logger.targetStarted(event);
    logger.buildFinished(new BuildEvent(new Project()));

    System.out.println(new String(buf.toByteArray()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(new LogEntry(ZonedDateTime.now(clock), "info", "description", "target", null), act[0]);
  }

  @Test
  void messageLogged() throws IOException {
    var target = new Target();
    target.setName("target");
    final Task task = new Task() {};
    task.setTaskName("task");
    task.setOwningTarget(target);
    var event = new BuildEvent(task);
    event.setMessage("message", Project.MSG_INFO);

    logger.messageLogged(event);
    logger.buildFinished(new BuildEvent(new Project()));

    System.out.println(new String(buf.toByteArray()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(new LogEntry(ZonedDateTime.now(clock), "info", "message", null, "task"), act[0]);
  }

  private LogEntry entry(String level, String msg) {
    return new LogEntry(ZonedDateTime.now(clock), level, msg, null, null);
  }

  private record LogEntry(ZonedDateTime timestamp, String level, String msg, String target, String task) {}
}
