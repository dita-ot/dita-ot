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
import org.apache.tools.ant.Project;
import org.slf4j.helpers.MarkerIgnoringBase;

/**
 * Logger to standard out and error.
 */
public final class StandardLogger extends MarkerIgnoringBase implements DITAOTLogger {

  private final PrintStream err;
  private final PrintStream out;
  private final int msgOutputLevel;

  public StandardLogger(final PrintStream out, final PrintStream err, final int msgOutputLevel) {
    this.out = out;
    this.err = err;
    this.msgOutputLevel = msgOutputLevel;
  }

  @Override
  public void info(final String msg) {
    log(msg, null, Project.MSG_INFO);
  }

  @Override
  public void info(String format, Object arg) {
    log(MessageFormat.format(format, arg), null, Project.MSG_INFO);
  }

  @Override
  public void info(String format, Object arg1, Object arg2) {
    log(MessageFormat.format(format, arg1, arg2), null, Project.MSG_INFO);
  }

  @Override
  public void info(String format, Object... arguments) {
    log(MessageFormat.format(format, arguments), null, Project.MSG_INFO);
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
    log(MessageFormat.format(format, arg), null, Project.MSG_WARN);
  }

  @Override
  public void warn(String format, Object... arguments) {
    log(MessageFormat.format(format, arguments), null, Project.MSG_WARN);
  }

  @Override
  public void warn(String format, Object arg1, Object arg2) {
    log(MessageFormat.format(format, arg1, arg2), null, Project.MSG_WARN);
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
      log(MessageFormat.format(format, arg), null, Project.MSG_ERR);
    }
  }

  @Override
  public void error(String format, Object arg1, Object arg2) {
    if (arg2 instanceof Throwable) {
      log(MessageFormat.format(format, arg1), (Throwable) arg2, Project.MSG_ERR);
    } else {
      log(MessageFormat.format(format, arg1, arg2), null, Project.MSG_ERR);
    }
  }

  @Override
  public void error(String format, Object... arguments) {
    final Object last = arguments[arguments.length - 1];
    if (last instanceof Throwable) {
      final Object[] init = new Object[arguments.length - 1];
      System.arraycopy(arguments, 0, init, 0, init.length);
      log(MessageFormat.format(format, init), (Throwable) last, Project.MSG_ERR);
    } else {
      log(MessageFormat.format(format, arguments), null, Project.MSG_ERR);
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
    log(MessageFormat.format(format, arg), null, Project.MSG_VERBOSE);
  }

  @Override
  public void debug(String format, Object arg1, Object arg2) {
    log(MessageFormat.format(format, arg1, arg2), null, Project.MSG_VERBOSE);
  }

  @Override
  public void debug(String format, Object... arguments) {
    log(MessageFormat.format(format, arguments), null, Project.MSG_VERBOSE);
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
    if (level > msgOutputLevel) {
      return;
    }
    if (level == Project.MSG_ERR || level == Project.MSG_WARN) {
      err.println(msg);
    } else {
      out.println(msg);
    }
  }
}
