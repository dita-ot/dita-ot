/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.log;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

/**
 * Logger proxy to Ant logger.
 *
 * @author Jarno Elovirta
 */
public final class DITAOTAntLogger extends AbstractLogger {

  private final Project project;
  private Task task;
  private Target target;

  /**
   * Construct a new logger that forwards messages to Ant project logger.
   * @param project Ant project to log to
   * @throws NullPointerException if project is {@code null}
   */
  public DITAOTAntLogger(final Project project) {
    super();
    if (project == null) {
      throw new NullPointerException();
    }
    this.project = project;
  }

  /**
   * Set log message source task.
   * @param task logging task
   */
  public void setTask(final Task task) {
    this.task = task;
  }

  /**
   * Set log message source target.
   * @param target logging target
   */
  public void setTarget(final Target target) {
    this.target = target;
  }

  @Override
  void log(final String msg, final Throwable t, final int level) {
    if (task != null) {
      project.log(task, msg, level);
    } else if (target != null) {
      project.log(target, msg, level);
    } else {
      project.log(msg, level);
    }
  }
}
