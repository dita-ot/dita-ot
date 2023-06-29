/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.invoker;

import org.apache.tools.ant.BuildException;

public class CliException extends BuildException {

  public final String info;

  public CliException() {
    this("", (String) null);
  }

  public CliException(String msg) {
    this(msg, (String) null);
  }

  public CliException(Throwable cause) {
    super(cause);
    this.info = null;
  }

  public CliException(String msg, Throwable cause) {
    super(msg, cause);
    this.info = null;
  }

  public CliException(String msg, String info) {
    super(msg);
    this.info = info;
  }
}
