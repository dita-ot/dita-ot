/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
public final class DITAOTAntLogger implements DITAOTLogger {

    private final Project project;
    private Task task;
    private Target target;

    /**
     * Construct a new logger that forwards messages to Ant project logger.
     * @param project Ant project to log to
     * @throws NullPointerException if project is {@code null}
     */
    public DITAOTAntLogger(final Project project) {
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
    public void info(final String msg) {
        log(msg, null, Project.MSG_INFO);
    }

    @Override
    public void warn(final String msg) {
        log(msg, null, Project.MSG_WARN);
    }

    @Override
    public void error(final String msg) {
        log(msg, null, Project.MSG_ERR);
    }

    @Override
    public void error(final String msg, final Throwable t) {
        log(msg, t, Project.MSG_ERR);
    }
    
    @Override
    public void debug(final String msg) {
        log(msg, null, Project.MSG_VERBOSE);
    }

    private void log(final String msg, final Throwable t, final int level) {
        if (task != null) {
            project.log(task, msg, level);
        } else if (target != null) {
            project.log(target, msg, level);
        } else  {
            project.log(msg, level);
        }
    }

}
