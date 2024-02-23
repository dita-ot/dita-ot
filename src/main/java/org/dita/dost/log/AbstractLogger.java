/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.Project;
import org.dita.dost.invoker.Main;
import org.dita.dost.util.Configuration;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * Abstract logger implementation based on SLF4J.
 */
public abstract class AbstractLogger extends MarkerIgnoringBase implements DITAOTLogger {

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

  protected int msgOutputLevel = Project.MSG_DEBUG;
  protected boolean useColor;
  protected boolean legacyFormat = Configuration.configuration
    .getOrDefault("cli.log-format", "legacy")
    .equals("legacy");

  public void setOutputLevel(final int msgOutputLevel) {
    this.msgOutputLevel = msgOutputLevel;
  }

  public void setUseColor(final boolean useColor) {
    this.useColor = useColor;
  }

  @Override
  public void info(final String msg) {
    log(msg, new Object[] {}, null, Project.MSG_INFO);
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
    log(msg, new Object[] {}, t, Project.MSG_INFO);
  }

  @Override
  public boolean isWarnEnabled() {
    return msgOutputLevel >= Project.MSG_WARN;
  }

  @Override
  public void warn(final String msg) {
    log(msg, new Object[] {}, null, Project.MSG_WARN);
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
    log(msg, new Object[] {}, t, Project.MSG_WARN);
  }

  @Override
  public boolean isErrorEnabled() {
    return msgOutputLevel >= Project.MSG_ERR;
  }

  @Override
  public void error(final String msg) {
    log(msg, new Object[] {}, null, Project.MSG_ERR);
  }

  @Override
  public void error(String format, Object arg) {
    if (arg instanceof Throwable) {
      log(format, new Object[] {}, (Throwable) arg, Project.MSG_ERR);
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
    log(msg, new Object[] {}, t, Project.MSG_ERR);
  }

  @Override
  public boolean isTraceEnabled() {
    return msgOutputLevel >= Project.MSG_DEBUG;
  }

  @Override
  public void trace(String msg) {
    log(msg, new Object[] {}, null, Project.MSG_DEBUG);
  }

  @Override
  public void trace(String format, Object arg) {
    if (arg instanceof Throwable) {
      log(format, new Object[] {}, (Throwable) arg, Project.MSG_DEBUG);
    } else {
      log(format, new Object[] { arg }, null, Project.MSG_DEBUG);
    }
  }

  @Override
  public void trace(String format, Object arg1, Object arg2) {
    if (arg2 instanceof Throwable) {
      log(format, new Object[] { arg1 }, (Throwable) arg2, Project.MSG_DEBUG);
    } else {
      log(format, new Object[] { arg1, arg2 }, null, Project.MSG_DEBUG);
    }
  }

  @Override
  public void trace(String format, Object... arguments) {
    final Object last = arguments[arguments.length - 1];
    if (last instanceof Throwable) {
      final Object[] args = new Object[arguments.length - 1];
      System.arraycopy(arguments, 0, args, 0, args.length);
      log(format, args, (Throwable) last, Project.MSG_DEBUG);
    } else {
      log(format, arguments, null, Project.MSG_DEBUG);
    }
  }

  @Override
  public void trace(String msg, Throwable t) {
    log(msg, new Object[] {}, t, Project.MSG_DEBUG);
  }

  @Override
  public boolean isDebugEnabled() {
    return msgOutputLevel >= Project.MSG_VERBOSE;
  }

  @Override
  public void debug(final String msg) {
    log(msg, new Object[] {}, null, Project.MSG_VERBOSE);
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
    log(msg, new Object[] {}, t, Project.MSG_VERBOSE);
  }

  @Override
  public boolean isInfoEnabled() {
    return msgOutputLevel >= Project.MSG_INFO;
  }

  public abstract void log(final String msg, final Throwable t, final int level);

  protected void log(final String msg, final Object[] args, final Throwable t, final int level) {
    if (level > msgOutputLevel) {
      return;
    }
    StringBuilder buf = null;
    if (!legacyFormat) {
      if (level == Project.MSG_ERR) {
        buf = new StringBuilder();
        if (useColor) {
          buf.append(ANSI_RED);
        }
        buf.append(Main.locale.getString("error_msg").formatted(""));
        if (useColor) {
          buf.append(ANSI_RESET);
        }
      } else if (level == Project.MSG_WARN) {
        buf = new StringBuilder();
        if (useColor) {
          buf.append(ANSI_YELLOW);
        }
        buf.append(Main.locale.getString("warn_msg").formatted(""));
        if (useColor) {
          buf.append(ANSI_RESET);
        }
      }
    }
    if (args.length > 0) {
      if (buf == null) {
        buf = new StringBuilder();
      }
      if (msg.contains("{}") || msg.contains("%s")) {
        buf.append(MessageFormat.format(addIndex(msg), args));
      } else {
        buf.append(MessageFormat.format(msg, args));
      }
    } else if (buf != null) {
      buf.append(msg);
    }
    final String res;
    if (legacyFormat) {
      res = buf != null ? buf.toString() : msg;
    } else if (buf != null) {
      res = removeLevelPrefix(buf).toString();
    } else {
      res = removeLevelPrefix(msg);
    }
    log(res, t, level);
  }

  protected static String removeLevelPrefix(String msg) {
    var start = msg.indexOf("][");
    if (start == -1) {
      return msg;
    }
    var end = msg.indexOf("]", start + 1);
    if (end == -1) {
      return msg;
    }
    return switch (msg.substring(start + 2, end)) {
      case "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL" -> msg.substring(0, start) + msg.substring(end);
      default -> msg;
    };
  }

  protected static StringBuilder removeLevelPrefix(StringBuilder msg) {
    var start = msg.indexOf("][");
    if (start == -1) {
      return msg;
    }
    var end = msg.indexOf("]", start + 1);
    if (end == -1) {
      return msg;
    }
    return switch (msg.substring(start + 2, end)) {
      case "TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL" -> msg.replace(start, end, "");
      default -> msg;
    };
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
