/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dita.dost.invoker;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.StringUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.AbstractLogger;

/**
 * Writes build events to a output stream as JSON objects.
 */
class JsonLogger extends AbstractLogger implements BuildLogger {

  // CheckStyle:VisibilityModifier OFF - bc
  /** PrintStream to write non-error messages to */
  private PrintStream out;

  /** PrintStream to write error messages to */
  private PrintStream err;

  //** Lowest level of message to write out */
  //  private int msgOutputLevel = Project.MSG_ERR;

  /** Time of the start of the build */
  private long startTime = System.currentTimeMillis();

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

  /**
   * Sets the output stream to which this logger is to send its output.
   *
   * @param output The output stream for the logger. Must not be
   *            <code>null</code>.
   */
  @Override
  public void setOutputPrintStream(final PrintStream output) {
    out = new PrintStream(output, true);
  }

  @Override
  public void setEmacsMode(boolean emacsMode) {
    // Ignore
  }

  /**
   * Sets the output stream to which this logger is to send error messages.
   *
   * @param err The error stream for the logger. Must not be <code>null</code>
   *            .
   */
  @Override
  public void setErrorPrintStream(final PrintStream err) {
    this.err = new PrintStream(err, true);
  }

  private JsonGenerator generator;

  /**
   * Responds to a build being started by just remembering the current time.
   *
   * @param event Ignored.
   */
  @Override
  public void buildStarted(final BuildEvent event) {
    startTime = System.currentTimeMillis();
    try {
      generator = new ObjectMapper().createGenerator(out);
    } catch (IOException e) {
      throw new RuntimeException("Failed to open JSON generator: " + e.getMessage(), e);
    }
  }

  //  private static void throwableMessage(final StringBuilder m, final Throwable error, final boolean verbose) {
  //    String msg = error.getMessage();
  //    final int i = msg.indexOf(": ");
  //    if (i != -1) {
  //      msg = msg.substring(i + 1).trim();
  //    }
  //    m.append(msg);
  //    //        m.append(lSep);
  //  }

  /**
   * Prints whether the build succeeded or failed, any errors the occurred
   * during the build, and how long the build took.
   *
   * @param event An event with any relevant extra information. Must not be
   *            <code>null</code>.
   */
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
      final StringBuilder message = new StringBuilder();
      if (error == null) {
        if (msgOutputLevel >= Project.MSG_INFO) {
          //          generator.writeStartObject();
          //          generator.writeNumberField("duration", System.currentTimeMillis() - startTime);
          //          generator.writeEndObject();
          //          out.append(System.lineSeparator());
          //          return;
        }
      } else {
        message.append(Main.locale.getString("error_msg").formatted(""));
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

        if (msgOutputLevel >= Project.MSG_INFO) { //
          //          message.append(StringUtils.LINE_SEP);
          //          message.append("BUILD FAILED");
          //          message.append(" in ").append(formatTime(System.currentTimeMillis() - startTime));
        }
      }
      // message.append(StringUtils.LINE_SEP);
      // message.append("Total time: ");
      // message.append(formatTime(System.currentTimeMillis() - startTime));

      generator.writeStartObject();
      generator.writeStringField("level", "info");
      generator.writeNumberField("duration", System.currentTimeMillis() - startTime);
      generator.writeEndObject();
      generator.flush();
      out.append(System.lineSeparator());
      out.flush();

      if (!message.isEmpty()) {
        generator.writeStartObject();
        generator.writeStringField("level", "info");
        if (error == null) {
          generator.writeStringField("msg", message.toString());
          //          out.println(msg);
        } else {
          generator.writeStringField("msg", removeLevelPrefix(message).toString());
          //          err.println(removeLevelPrefix(message));
        }
        generator.writeEndObject();
        generator.flush();
        out.append(System.lineSeparator());
        out.flush();
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

  private boolean evaluate(final Project project, final String condition) {
    final String value = project.replaceProperties(condition);
    return switch (value) {
      case "true" -> true;
      case "false" -> false;
      default -> project.getProperty(value) != null || project.getUserProperty(value) != null;
    };
  }

  /**
   * Logs a message to say that the target has started if this logger allows
   * information-level messages.
   *
   * @param event An event with any relevant extra information. Must not be
   *            <code>null</code>.
   */
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
        generator.writeStartObject();
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
        generator.writeEndObject();
        generator.flush();
        out.append(System.lineSeparator());
        out.flush();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
    }
  }

  /**
   * No-op implementation.
   *
   * @param event Ignored.
   */
  @Override
  public void targetFinished(final BuildEvent event) {}

  /**
   * No-op implementation.
   *
   * @param event Ignored.
   */
  @Override
  public void taskStarted(final BuildEvent event) {}

  /**
   * No-op implementation.
   *
   * @param event Ignored.
   */
  @Override
  public void taskFinished(final BuildEvent event) {}

  /**
   * Logs a message, if the priority is suitable. In non-emacs mode, task
   * level messages are prefixed by the task name which is right-justified.
   *
   * @param event A BuildEvent containing message information. Must not be
   *            <code>null</code>.
   */
  @Override
  public void messageLogged(final BuildEvent event) {
    final int priority = event.getPriority();
    // Filter out messages based on priority
    if (priority <= msgOutputLevel) {
      try {
        generator.writeStartObject();
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
        generator.writeEndObject();
        generator.flush();
        out.append(System.lineSeparator());
        out.flush();
      } catch (IOException e) {
        throw new RuntimeException("Failed to write JSON: " + e.getMessage(), e);
      }
      //      log(msg);
    }
  }

  /**
   * Convenience method to format a specified length of time.
   *
   * @param millis Length of time to format, in milliseconds.
   *
   * @return the time as a formatted string.
   *
   * @see DateUtils#formatElapsedTime(long)
   */
  protected static String formatTime(final long millis) {
    return DateUtils.formatElapsedTime(millis);
  }

  /**
   * Empty implementation which allows subclasses to receive the same output
   * that is generated here.
   *
   * @param message Message being logged. Should not be <code>null</code>.
   */
  private void log(final String message) {}

  //  /**
  //   * Get the current time.
  //   *
  //   * @return the current time as a formatted string.
  //   * @since Ant1.7.1
  //   */
  //  protected String getTimestamp() {
  //    final Date date = new Date(System.currentTimeMillis());
  //    final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
  //    return formatter.format(date);
  //  }

  //  /**
  //   * Get the project name or null
  //   *
  //   * @param event the event
  //   * @return the project that raised this event
  //   * @since Ant1.7.1
  //   */
  //  protected String extractProjectName(final BuildEvent event) {
  //    final Project project = event.getProject();
  //    return (project != null) ? project.getName() : null;
  //  }

  @Override
  public void log(String msg, Throwable t, int level) {
    throw new UnsupportedOperationException();
  }
}
