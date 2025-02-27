/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.StringUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.AbstractLogger;

/**
 * Writes build events to a output stream as JSON objects.
 */
public class JsonLogger extends AbstractLogger implements BuildLogger {

  // CheckStyle:VisibilityModifier OFF - bc
  /** PrintStream to write non-error messages to */
  private PrintStream out;
  private JsonGenerator generator;

  //  /** PrintStream to write error messages to */
  //  private PrintStream err;

  //** Lowest level of message to write out */
  //  private int msgOutputLevel = Project.MSG_ERR;

  /** Time of the start of the build */
  private long startTime = System.currentTimeMillis();

  private boolean isArray = false;
  private Clock clock = Clock.systemUTC();

  private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault());

  /**
   * Sole constructor.
   */
  public JsonLogger() {
    msgOutputLevel = Project.MSG_ERR;
  }

  @Override
  public void setMessageOutputLevel(final int level) {
    msgOutputLevel = level;
  }

  @Override
  public int getMessageOutputLevel() {
    return BuildLogger.super.getMessageOutputLevel();
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
    startTime = clock.instant().getEpochSecond();
    try {
      generator = new ObjectMapper().createGenerator(out);
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
      writeStart();

      final StringBuilder message = new StringBuilder();
      if (error == null) {
        generator.writeStringField("level", "info");
        if (msgOutputLevel >= Project.MSG_INFO) {
          //                    generator.writeStartObject();
          //                    generator.writeNumberField("duration", clock.instant().getEpochSecond() - startTime);
          //                    generator.writeEndObject();
          //                    out.append(System.lineSeparator());
          //                    return;
        }
      } else {
        generator.writeStringField("level", "fatal");
        //        message.append(Main.locale.getString("error_msg").formatted(""));
        if (error instanceof DITAOTException && msgOutputLevel < Project.MSG_INFO) {
          message.append(Main.locale.getString("exception_msg").formatted(error.getMessage()));
        } else {
          try (var buf = new StringWriter(); var printWriter = new PrintWriter(buf)) {
            error.printStackTrace(printWriter);
            printWriter.flush();
            message.append(Main.locale.getString("exception_msg").formatted(buf));
          } catch (IOException e) {
            // Failed to print stack trace
          }
        }
        //        if (msgOutputLevel >= Project.MSG_INFO) { //
        //          message.append(StringUtils.LINE_SEP);
        //          message.append("BUILD FAILED");
        //          message.append(" in ").append(DateUtils.formatElapsedTime(clock.instant().getEpochSecond() - startTime));
        //        }
      }
      // message.append(StringUtils.LINE_SEP);
      // message.append("Total time: ");
      // message.append(DateUtils.formatElapsedTime(clock.instant().getEpochSecond() - startTime));

      generator.writeStringField("msg", "BUILD SUCCESSFUL");
      writeEnd();

      if (!message.isEmpty()) {
        writeStart();
        generator.writeStringField("level", "info");
        if (error == null) {
          generator.writeStringField("msg", message.toString());
          //          out.println(msg);
        } else {
          generator.writeStringField("msg", removeLevelPrefix(message).toString());
          //          err.println(removeLevelPrefix(message));
        }
        writeEnd();
      }

      if (isArray) {
        generator.writeEndArray();
        generator.flush();
        //        out.append(System.lineSeparator());
        //        out.flush();
      }
      //      log(message.toString());
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

  private void writeStart() throws IOException {
    generator.writeStartObject();
    generator.writeStringField("timestamp", formatter.format(Instant.now(clock)));
  }

  private void writeEnd() throws IOException {
    generator.writeEndObject();
    generator.flush();
    if (!isArray) {
      out.append(System.lineSeparator());
      out.flush();
    }
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
    if (event.getTarget().getIf() != null && !evaluate(event.getProject(), event.getTarget().getIf())) {
      return;
    }
    if (event.getTarget().getUnless() != null && evaluate(event.getProject(), event.getTarget().getUnless())) {
      return;
    }
    if (Project.MSG_INFO <= msgOutputLevel && !event.getTarget().getName().equals("")) {
      try {
        writeStart();
        generator.writeStringField(
          "level",
          switch (event.getPriority()) {
            case Project.MSG_ERR -> "error";
            case Project.MSG_WARN -> "warn";
            case Project.MSG_INFO -> "info";
            case Project.MSG_VERBOSE -> "debug";
            case Project.MSG_DEBUG -> "trace";
            default -> throw new IllegalArgumentException("Unexpected value: " + event.getPriority());
          }
        );
        generator.writeStringField("target", event.getTarget().getName());
        generator.writeStringField("msg", event.getTarget().getDescription());
        writeEnd();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  @Override
  public void targetFinished(final BuildEvent event) {}

  @Override
  public void taskStarted(final BuildEvent event) {}

  @Override
  public void taskFinished(final BuildEvent event) {}

  @Override
  public void messageLogged(final BuildEvent event) {
    final int priority = event.getPriority();
    if (priority <= msgOutputLevel) {
      try {
        writeStart();
        generator.writeStringField("level", "info");
        final StringBuilder message = new StringBuilder(event.getMessage());
        if (event.getTask() != null) {
          generator.writeStringField("task", event.getTask().getTaskName());
        }
        final Throwable ex = event.getException();
        if (Project.MSG_VERBOSE <= msgOutputLevel && ex != null) {
          generator.writeStringField("stacktrace", StringUtils.getStackTrace(ex));
        }
        generator.writeStringField("msg", removeLevelPrefix(message).toString());
        writeEnd();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
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
}
