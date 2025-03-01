/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.*;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageBean;
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

    logger.buildStarted(new BuildEvent(createProject()));
  }

  @AfterEach
  void tearDown() {
    out.close();
  }

  @Test
  void buildFinished() throws IOException {
    var event = new BuildEvent(createProject());

    logger.buildFinished(event);

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(
      new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "BUILD SUCCESSFUL", Duration.ZERO, null, null),
      act[0]
    );
  }

  @Test
  void buildFinished_error() throws IOException {
    var event = new BuildEvent(createProject());
    event.setException(new DITAOTException("test"));

    logger.buildFinished(event);

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(MessageBean.Type.FATAL, act[0].level);
    assertEquals(Duration.ZERO, act[0].duration);
  }

  @Test
  void target() throws IOException {
    var event = new BuildEvent(createTarget());
    event.setMessage("message", Project.MSG_INFO);

    logger.targetStarted(event);
    logger.targetFinished(event);
    logger.buildFinished(new BuildEvent(createProject()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertArrayEquals(
      new LogEntry[] {
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Started target target: description",
          null,
          null,
          null
        ),
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Finished target target: description",
          Duration.ZERO,
          null,
          null
        ),
        new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "BUILD SUCCESSFUL", Duration.ZERO, null, null),
      },
      act
    );
  }

  @Test
  void task() throws IOException {
    var event = new BuildEvent(createTask());
    event.setMessage("message", Project.MSG_INFO);

    logger.taskStarted(event);
    logger.taskFinished(event);
    logger.buildFinished(new BuildEvent(createProject()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertArrayEquals(
      new LogEntry[] {
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Started task task: description",
          null,
          null,
          null
        ),
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Finished task task: description",
          Duration.ZERO,
          null,
          null
        ),
        new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "BUILD SUCCESSFUL", Duration.ZERO, null, null),
      },
      act
    );
  }

  @Test
  void messageLogged() throws IOException {
    var event = new BuildEvent(createTask());
    event.setMessage("message", Project.MSG_INFO);

    logger.messageLogged(event);
    logger.buildFinished(new BuildEvent(createProject()));

    System.out.println(new String(buf.toByteArray()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "message", null, null, "task"), act[0]);
  }

  private static Project createProject() {
    return new Project();
  }

  private static Task createTask() {
    final Task task = new Task() {};
    task.setTaskName("task");
    task.setDescription("description");
    task.setOwningTarget(createTarget());
    task.setProject(task.getOwningTarget().getProject());
    return task;
  }

  private static Target createTarget() {
    var target = new Target();
    target.setName("target");
    target.setDescription("description");
    target.setProject(createProject());
    return target;
  }

  private record LogEntry(
    ZonedDateTime timestamp,
    MessageBean.Type level,
    String msg,
    Duration duration,
    String target,
    String task
  ) {}
}
