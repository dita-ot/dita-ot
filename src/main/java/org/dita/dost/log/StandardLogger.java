/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import java.io.PrintStream;
import org.apache.tools.ant.Project;

/**
 * Logger to standard out and error.
 */
public final class StandardLogger extends AbstractLogger {

  private final PrintStream err;
  private final PrintStream out;

  public StandardLogger(
    final PrintStream out,
    final PrintStream err,
    final int msgOutputLevel,
    final boolean useColor
  ) {
    super();
    this.out = out;
    this.err = err;
    this.msgOutputLevel = msgOutputLevel;
    this.useColor = useColor;
  }

  @Override
  public void log(final String msg, final Throwable t, final int level) {
    if (level == Project.MSG_ERR || level == Project.MSG_WARN) {
      err.println(msg);
    } else {
      out.println(msg);
    }
  }
}
