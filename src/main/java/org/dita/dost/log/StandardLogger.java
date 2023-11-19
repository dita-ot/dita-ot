/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import java.io.PrintStream;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.Project;
import org.dita.dost.invoker.Main;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * Logger to standard out and error.
 */
public final class StandardLogger extends MarkerIgnoringBase implements DITAOTLogger {

  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";
  public static final String ANSI_BOLD = "\u001b[1m";

  private final PrintStream err;
  private final PrintStream out;
  private int msgOutputLevel;
  private boolean useColor;

  public StandardLogger(
    final PrintStream out,
    final PrintStream err,
    final int msgOutputLevel,
    final boolean useColor
  ) {
    this.out = out;
    this.err = err;
    this.msgOutputLevel = msgOutputLevel;
    this.useColor = useColor;
  }

  public void setOutputLevel(final int msgOutputLevel) {
    this.msgOutputLevel = msgOutputLevel;
  }

  public void setUseColor(final boolean useColor) {
    this.useColor = useColor;
  }

  @Override
  public void info(final String msg) {
    log(msg, null, Project.MSG_INFO);
  }

  @Override
  public void info(String format, Object arg) {
    log(format, new Object[] { arg }, null, Project.MSG_INFO);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    log(format, new Object[] { arg1, arg2 }, null, Project.MSG_INFO);
  }

  @Override
  public void info(String format, Object... arguments) {
    log(format, arguments, null, Project.MSG_INFO);
  }

  @Override
  public void info(String msg, Throwable t) {
    log(msg, t, Project.MSG_INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return false;
  }

  @Override
  public void warn(final String msg) {
    log(msg, null, Project.MSG_WARN);
  }

  @Override
  public void warn(String format, Object arg) {
    log(format, new Object[] { arg }, null, Project.MSG_WARN);
  }

  @Override
  public void warn(String format, Object... arguments) {
    log(format, arguments, null, Project.MSG_WARN);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    log(format, new Object[] { arg1, arg2 }, null, Project.MSG_WARN);
  }

  @Override
  public void warn(String msg, Throwable t) {
    log(msg, t, Project.MSG_WARN);
  }

  @Override
  public boolean isErrorEnabled() {
    return true;
  }

  @Override
  public void error(final String msg) {
    log(msg, null, Project.MSG_ERR);
  }

  @Override
  public void error(String format, Object arg) {
    if (arg instanceof Throwable) {
      log(format, (Throwable) arg, Project.MSG_ERR);
    } else {
      log(format, new Object[] { arg }, null, Project.MSG_ERR);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    if (arg2 instanceof Throwable) {
      log(format, new Object[] { arg1 }, (Throwable) arg2, Project.MSG_ERR);
    } else {
      log(format, new Object[] { arg1, arg2 }, null, Project.MSG_ERR);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    final Object last = arguments[arguments.length - 1];
    if (last instanceof Throwable) {
      final Object[] args = new Object[arguments.length - 1];
      System.arraycopy(arguments, 0, args, 0, args.length);
      log(format, args, (Throwable) last, Project.MSG_ERR);
    } else {
      log(format, arguments, null, Project.MSG_ERR);
    }
  }

  @Override
  public void error(final String msg, final Throwable t) {
    log(msg, t, Project.MSG_ERR);
  }

  @Override
  public boolean isTraceEnabled() {
    return false;
  }

  @Override
  public void trace(String msg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void trace(String format, Object arg) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void trace(String format, Object... arguments) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void trace(String msg, Throwable t) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean isDebugEnabled() {
    return true;
  }

  @Override
  public void debug(final String msg) {
    log(msg, null, Project.MSG_VERBOSE);
  }

  @Override
  public void debug(String format, Object arg) {
    log(format, new Object[] { arg }, null, Project.MSG_VERBOSE);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    log(format, new Object[] { arg1, arg2 }, null, Project.MSG_VERBOSE);
  }

  @Override
  public void debug(String format, Object... arguments) {
    log(format, arguments, null, Project.MSG_VERBOSE);
  }

  @Override
  public void debug(String msg, Throwable t) {
    log(msg, t, Project.MSG_VERBOSE);
  }

  @Override
  public boolean isInfoEnabled() {
    return true;
  }

  private void log(final String msg, final Throwable t, final int level) {
    log(msg, new Object[] {}, t, level);
  }

  private void log(final String msg, final Object[] args, final Throwable t, final int level) {
    if (level > msgOutputLevel) {
      return;
    }
    if (useColor && level == Project.MSG_ERR) {
      err.print(ANSI_RED);
      err.printf(Main.locale.getString("error_msg"), "");
      err.print(ANSI_RESET);
    } else if (useColor && level == Project.MSG_WARN) {
      err.print(ANSI_YELLOW);
      err.printf(Main.locale.getString("warn_msg"), "");
      err.print(ANSI_RESET);
    }
    if (args.length > 0) {
      if (msg.contains("{}") || msg.contains("%s")) {
        out.println(MessageFormat.format(addIndex(msg), args));
      } else {
        out.println(MessageFormat.format(msg, args));
      }
    } else {
      out.println(msg);
    }
  }

  private static final Pattern ARGUMENT = Pattern.compile("\\{}|%s");

  private String addIndex(String msg) {
    final Matcher matcher = ARGUMENT.matcher(msg);
    final StringBuilder buf = new StringBuilder();
    int start = 0;
    for (int i = 0; matcher.find(start); i++) {
      buf.append(msg, start, matcher.start());
      buf.append("{");
      buf.append(i);
      buf.append("}");
      start = matcher.end();
    }
    if (start < msg.length()) {
      buf.append(msg, start, msg.length());
    }
    return buf.toString();
  }
}
