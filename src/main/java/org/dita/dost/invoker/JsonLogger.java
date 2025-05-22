/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2025 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.*;
import org.apache.tools.ant.util.StringUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.AbstractLogger;
import org.dita.dost.log.MessageBean;

/**
 * Writes build events to a output stream as JSON objects.
 */
public class JsonLogger extends AbstractLogger implements BuildLogger {

  static final String FIELD_CODE = "code";
  static final String FIELD_DURATION = "duration";
  static final String FIELD_LEVEL = "level";
  static final String FIELD_LINE = "line";
  static final String FIELD_LOCATION = "location";
  static final String FIELD_MSG = "msg";
  static final String FIELD_ROW = "row";
  static final String FIELD_STACKTRACE = "stacktrace";
  static final String FIELD_TARGET = "target";
  static final String FIELD_TASK = "task";
  static final String FIELD_TIMESTAMP = "timestamp";

  /**
   * PrintStream to write non-error messages to
   */
  private PrintStream out;
  private JsonGenerator generator;

  private boolean isArray = false;
  private Clock clock = Clock.systemDefaultZone();
  /** Timestamp stack to track build/target/task duration. */
  private final Deque<Long> timestampStack = new ArrayDeque<>();

  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

  public JsonLogger() {
    msgOutputLevel = Project.MSG_ERR;
  }

  @Override
  public void setMessageOutputLevel(final int level) {
    msgOutputLevel = level;
  }

  @Override
  public int getMessageOutputLevel() {
    return msgOutputLevel;
  }

  @Override
  public void setOutputPrintStream(final PrintStream output) {
    out = new PrintStream(output, true);
  }

  @Override
  public void setEmacsMode(boolean emacsMode) {
    // Ignore
  }

  @Override
  public void setErrorPrintStream(final PrintStream err) {
    // Ignore
  }

  @Override
  public void buildStarted(final BuildEvent event) {
    timestampStack.push(clock.instant().toEpochMilli());
    try {
      var prettyPrinter = new MinimalPrettyPrinter();
      prettyPrinter.setRootValueSeparator(System.lineSeparator());
      generator = new ObjectMapper().createGenerator(out).setPrettyPrinter(prettyPrinter);
    } catch (IOException e) {
      throw new RuntimeException("Failed to open JSON generator: " + e.getMessage(), e);
    }
    if (isArray) {
      try {
        generator.writeStartArray();
        generator.flush();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void buildFinished(final BuildEvent event) {
    Throwable error = event.getException();
    for (var e = error; e != null; e = e.getCause()) {
      if (e instanceof DITAOTException) {
        error = e;
        break;
      }
    }
    try {
      if (error == null) {
        if (Project.MSG_INFO <= msgOutputLevel) {
          writeStart(MessageBean.Type.INFO);
          generator.writeStringField(FIELD_MSG, "Build successful");
          writeDuration();
          writeEnd();
        }
      } else {
        writeStart(MessageBean.Type.FATAL);
        if (error instanceof DITAOTException) { //  && msgOutputLevel < Project.MSG_INFO
          generator.writeStringField(FIELD_MSG, removeLevelPrefix(error.getMessage()));
        } else {
          try (var buf = new StringWriter(); var printWriter = new PrintWriter(buf, true)) {
            error.printStackTrace(printWriter);
            generator.writeStringField(FIELD_MSG, buf.toString());
          } catch (IOException e) {
            // Failed to print stack trace
          }
        }
        writeDuration();
        writeEnd();
      }

      if (isArray) {
        generator.writeEndArray();
        generator.flush();
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
    } finally {
      try {
        generator.close();
      } catch (IOException e) {
        throw new RuntimeException("Failed to close JSON generator: " + e.getMessage(), e);
      }
    }
  }

  private void writeDuration() throws IOException {
    generator.writeNumberField(FIELD_DURATION, clock.instant().toEpochMilli() - timestampStack.pop());
  }

  private void writeStart(MessageBean.Type level) throws IOException {
    generator.writeStartObject();
    generator.writeStringField(FIELD_TIMESTAMP, formatter.format(Instant.now(clock)));
    generator.writeStringField(FIELD_LEVEL, level.name());
  }

  private void writeEnd() throws IOException {
    generator.writeEndObject();
    generator.flush();
  }

  private boolean evaluate(final Project project, final String condition) {
    final String value = project.replaceProperties(condition);
    return switch (value) {
      case "true" -> true;
      case "false" -> false;
      default -> project.getProperty(value) != null || project.getUserProperty(value) != null;
    };
  }

  @Override
  public void targetStarted(final BuildEvent event) {
    final Target target = event.getTarget();
    if (target.getIf() != null && !evaluate(event.getProject(), target.getIf())) {
      return;
    }
    if (target.getUnless() != null && evaluate(event.getProject(), target.getUnless())) {
      return;
    }
    if (Project.MSG_INFO <= msgOutputLevel && !target.getName().equals("")) {
      timestampStack.push(clock.instant().toEpochMilli());
      try {
        writeStart(MessageBean.Type.INFO);
        generator.writeStringField(FIELD_TARGET, target.getName());
        generator.writeStringField(
          FIELD_MSG,
          target.getDescription() != null
            ? "Started target %s: %s".formatted(target.getName(), target.getDescription())
            : "Started target %s".formatted(target.getName())
        );
        writeEnd();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void targetFinished(final BuildEvent event) {
    final Target target = event.getTarget();
    if (target.getIf() != null && !evaluate(event.getProject(), target.getIf())) {
      return;
    }
    if (target.getUnless() != null && evaluate(event.getProject(), target.getUnless())) {
      return;
    }
    if (Project.MSG_INFO <= msgOutputLevel && !target.getName().equals("")) {
      try {
        writeStart(MessageBean.Type.INFO);
        generator.writeStringField(FIELD_TARGET, target.getName());
        generator.writeStringField(
          FIELD_MSG,
          target.getDescription() != null
            ? "Finished target %s: %s".formatted(target.getName(), target.getDescription())
            : "Finished target %s".formatted(target.getName())
        );
        writeDuration();
        writeEnd();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void taskStarted(final BuildEvent event) {
    final Task task = event.getTask();
    if (Project.MSG_DEBUG <= msgOutputLevel && !task.getTaskName().equals("")) {
      timestampStack.push(clock.instant().toEpochMilli());
      try {
        writeStart(toLevel(event));
        generator.writeStringField(FIELD_TARGET, task.getOwningTarget().getName());
        generator.writeStringField(FIELD_TASK, task.getTaskName());
        generator.writeStringField(
          FIELD_MSG,
          task.getDescription() != null
            ? "Started task %s: %s".formatted(task.getTaskName(), task.getDescription())
            : "Started task %s".formatted(task.getTaskName())
        );
        writeEnd();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void taskFinished(final BuildEvent event) {
    final Task task = event.getTask();
    if (Project.MSG_DEBUG <= msgOutputLevel && !task.getTaskName().equals("")) {
      try {
        writeStart(toLevel(event));
        generator.writeStringField(FIELD_TARGET, task.getOwningTarget().getName());
        generator.writeStringField(FIELD_TASK, task.getTaskName());
        generator.writeStringField(
          FIELD_MSG,
          task.getDescription() != null
            ? "Finished task %s: %s".formatted(task.getTaskName(), task.getDescription())
            : "Finished task %s".formatted(task.getTaskName())
        );
        writeDuration();
        writeEnd();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void messageLogged(final BuildEvent event) {
    final int priority = event.getPriority();
    if (priority > msgOutputLevel) {
      return;
    }
    try {
      writeStart(toLevel(event));
      if (event.getTarget() != null) {
        generator.writeStringField(FIELD_TARGET, event.getTarget().getName());
      }
      if (event.getTask() != null) {
        generator.writeStringField(FIELD_TASK, event.getTask().getTaskName());
      }
      final Throwable ex = event.getException();
      if (Project.MSG_VERBOSE <= msgOutputLevel && ex != null) {
        generator.writeStringField(FIELD_STACKTRACE, StringUtils.getStackTrace(ex));
      }
      final StringBuilder message = new StringBuilder(event.getMessage());
      extractLocation(message);
      generator.writeStringField(FIELD_MSG, removeLevelPrefix(message).toString());
      writeEnd();
    } catch (IOException e) {
      throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
    }
  }

  @Override
  public void log(String msg, Throwable t, int level) {
    throw new UnsupportedOperationException();
  }

  public void setArray(boolean isArray) {
    this.isArray = isArray;
  }

  public void setClock(Clock clock) {
    this.clock = clock;
  }

  private static MessageBean.Type toLevel(BuildEvent event) {
    return switch (event.getPriority()) {
      case Project.MSG_ERR -> MessageBean.Type.ERROR;
      case Project.MSG_WARN -> MessageBean.Type.WARN;
      case Project.MSG_INFO -> MessageBean.Type.INFO;
      case Project.MSG_VERBOSE -> MessageBean.Type.DEBUG;
      case Project.MSG_DEBUG -> MessageBean.Type.TRACE;
      default -> throw new IllegalArgumentException("Unexpected value: " + event.getPriority());
    };
  }

  private static final Pattern LOCATION_PREFIX = Pattern.compile(
    "^(?:(.+):(\\d+):(\\d+):\\s+)?(?:\\[(\\w+)])?(?:\\[(\\w+)])?:?\\s+"
  );

  private void extractLocation(StringBuilder message) throws IOException {
    final Matcher matcher = LOCATION_PREFIX.matcher(message);
    if (matcher.find()) {
      if (matcher.group(1) != null) {
        generator.writeStringField(FIELD_LOCATION, matcher.group(1));
      }
      if (matcher.group(2) != null) {
        generator.writeNumberField(FIELD_LINE, Integer.parseInt(matcher.group(2)));
      }
      if (matcher.group(3) != null) {
        generator.writeNumberField(FIELD_ROW, Integer.parseInt(matcher.group(3)));
      }
      for (int i = 4; i <= matcher.groupCount(); i++) {
        if (matcher.group(i) != null) {
          switch (matcher.group(i).toUpperCase()) {
            case "ERROR":
            case "WARN":
            case "WARNING":
            case "INFO":
            case "DEBUG":
            case "TRACE":
              break;
            default:
              generator.writeStringField(FIELD_CODE, matcher.group(i));
              break;
          }
          //          generator.writeStringField(FIELD_CODE, matcher.group(i));
        }
      }
      //      if (matcher.group(4) != null) {
      //        generator.writeStringField(FIELD_CODE, matcher.group(4));
      //      }
      message.delete(0, matcher.end());
    }
  }
}
