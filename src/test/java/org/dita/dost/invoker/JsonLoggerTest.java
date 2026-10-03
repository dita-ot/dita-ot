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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageBean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonLoggerTest {

  private static final ObjectReader objectReader = new ObjectMapper()
    .findAndRegisterModules()
    .readerForArrayOf(LogEntry.class);
  private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.of("Z"));
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
      new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "Build successful", 0L, null, null),
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
    assertEquals(0L, act[0].duration);
  }

  @Test
  void target() throws IOException {
    var event = new BuildEvent(createTarget());
    event.setMessage("message", Project.MSG_DEBUG);

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
          "target",
          null
        ),
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Finished target target: description",
          0L,
          "target",
          null
        ),
        new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "Build successful", 0L, null, null),
      },
      act
    );
  }

  @Test
  void task() throws IOException {
    var event = new BuildEvent(createTask());
    event.setMessage("message", Project.MSG_DEBUG);
    logger.setOutputLevel(Project.MSG_DEBUG);

    logger.taskStarted(event);
    logger.taskFinished(event);
    logger.buildFinished(new BuildEvent(createProject()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertArrayEquals(
      new LogEntry[] {
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.TRACE,
          "Started task task: description",
          null,
          "target",
          "task"
        ),
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.TRACE,
          "Finished task task: description",
          0L,
          "target",
          "task"
        ),
        new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "Build successful", 0L, null, null),
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

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(
      new LogEntry(ZonedDateTime.now(clock), MessageBean.Type.INFO, "message", null, "target", "task"),
      act[0]
    );
  }

  static List<Arguments> removeLevelPrefix() {
    return List.of(
      Arguments.of(
        "[DOTJ037W][INFO] Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          null,
          null,
          null
        )
      ),
      Arguments.of(
        "[DOTJ037W][INFO]: Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          null,
          null,
          null
        )
      ),
      Arguments.of(
        "[DOTJ037W] Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          null,
          null,
          null
        )
      ),
      Arguments.of(
        "[DOTJ037W][Warning]: Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          null,
          null,
          null
        )
      ),
      Arguments.of(
        "[WARN][DOTJ037W]: Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          null,
          null,
          null
        )
      ),
      Arguments.of(
        "[WARN] Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          null,
          null,
          null,
          null
        )
      ),
      Arguments.of(
        "file:/src/path.dita:2:3: [DOTJ037W][INFO] Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          "file:/src/path.dita",
          2,
          3
        )
      ),
      Arguments.of(
        "file:/src/path.dita:2:3: [DOTJ037W][INFO]: Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          "file:/src/path.dita",
          2,
          3
        )
      ),
      Arguments.of(
        "file:/src/path.dita:2:3: [DOTJ037W] Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          "file:/src/path.dita",
          2,
          3
        )
      ),
      Arguments.of(
        "file:/src/path.dita:2:3: [DOTJ037W][Warning]: Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          "file:/src/path.dita",
          2,
          3
        )
      ),
      Arguments.of(
        "file:/src/path.dita:2:3: [WARN][DOTJ037W]: Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          "DOTJ037W",
          "file:/src/path.dita",
          2,
          3
        )
      ),
      Arguments.of(
        "file:/src/path.dita:2:3: [WARN] Message",
        new LogEntry(
          ZonedDateTime.now(clock),
          MessageBean.Type.INFO,
          "Message",
          null,
          "target",
          "task",
          null,
          "file:/src/path.dita",
          2,
          3
        )
      )
    );
  }

  @ParameterizedTest
  @MethodSource("removeLevelPrefix")
  void messageLogged_prefix(String src, LogEntry exp) throws IOException {
    var event = new BuildEvent(createTask());
    event.setMessage(src, Project.MSG_INFO);

    logger.messageLogged(event);
    logger.buildFinished(new BuildEvent(createProject()));

    final LogEntry[] act = objectReader.readValue(buf.toByteArray());
    assertEquals(exp, act[0]);
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
    Long duration,
    String target,
    String task,
    String code,
    String location,
    Integer line,
    Integer row
  ) {
    LogEntry(ZonedDateTime timestamp, MessageBean.Type level, String msg, Long duration, String target, String task) {
      this(timestamp, level, msg, duration, target, task, null, null, null, null);
    }
  }
}
