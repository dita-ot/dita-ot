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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Date;
import java.text.DateFormat;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.StringUtils;
import org.apache.tools.ant.util.FileUtils;

/**
 * Writes build events to a PrintStream. Currently, it only writes which targets
 * are being executed, and any messages that get logged.
 *
 */
class DefaultLogger implements BuildLogger {

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

    /**
     * Size of left-hand column for right-justified task name.
     *
     * @see #messageLogged(BuildEvent)
     */
    private static final int LEFT_COLUMN_SIZE = 12;

    // CheckStyle:VisibilityModifier OFF - bc
    /** PrintStream to write non-error messages to */
    private PrintStream out;

    /** PrintStream to write error messages to */
    private PrintStream err;

    /** Lowest level of message to write out */
    private int msgOutputLevel = Project.MSG_ERR;

    /** Time of the start of the build */
    private long startTime = System.currentTimeMillis();

    // CheckStyle:ConstantNameCheck OFF - bc
    /** Line separator */
    protected static final String lSep = StringUtils.LINE_SEP;
    // CheckStyle:ConstantNameCheck ON

    /** Whether or not to use emacs-style output */
    private boolean emacsMode = false;
    private boolean useColor = false;

    // CheckStyle:VisibilityModifier ON

    /**
     * Sole constructor.
     */
    public DefaultLogger() {
    }

    /**
     * Sets the highest level of message this logger should respond to.
     *
     * Only messages with a message level lower than or equal to the given level
     * should be written to the log.
     * <p>
     * Constants for the message levels are in the {@link Project Project}
     * class. The order of the levels, from least to most verbose, is
     * <code>MSG_ERR</code>, <code>MSG_WARN</code>, <code>MSG_INFO</code>,
     * <code>MSG_VERBOSE</code>, <code>MSG_DEBUG</code>.
     * <p>
     * The default message level for DefaultLogger is Project.MSG_ERR.
     *
     * @param level the logging level for the logger.
     */
    @Override
    public void setMessageOutputLevel(final int level) {
        msgOutputLevel = level;
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

    /**
     * Sets this logger to produce emacs (and other editor) friendly output.
     *
     * @param emacsMode <code>true</code> if output is to be unadorned so that
     *            emacs and other editors can parse files names, etc.
     */
    @Override
    public void setEmacsMode(final boolean emacsMode) {
        this.emacsMode = emacsMode;
    }

    public void useColor(final boolean useColor) {
        this.useColor = useColor;
    }

    /**
     * Responds to a build being started by just remembering the current time.
     *
     * @param event Ignored.
     */
    @Override
    public void buildStarted(final BuildEvent event) {
        startTime = System.currentTimeMillis();
    }

    private static void throwableMessage(final StringBuilder m, final Throwable error, final boolean verbose) {
        String msg = error.getMessage();
        final int i = msg.indexOf(": ");
        if (i != -1) {
            msg = msg.substring(i + 1).trim();
        }
        m.append(msg);
//        m.append(lSep);
    }

    /**
     * Prints whether the build succeeded or failed, any errors the occurred
     * during the build, and how long the build took.
     *
     * @param event An event with any relevant extra information. Must not be
     *            <code>null</code>.
     */
    @Override
    public void buildFinished(final BuildEvent event) {
        final Throwable error = event.getException();
        final StringBuilder message = new StringBuilder();
        if (error == null) {
            // message.append(StringUtils.LINE_SEP);
            // message.append(getBuildSuccessfulMessage());
        } else {
            // message.append(StringUtils.LINE_SEP);
            // message.append(getBuildFailedMessage());
//            message.append(StringUtils.LINE_SEP);
            message.append("Error: ");
            throwableMessage(message, error, Project.MSG_VERBOSE <= msgOutputLevel);
        }
        // message.append(StringUtils.LINE_SEP);
        // message.append("Total time: ");
        // message.append(formatTime(System.currentTimeMillis() - startTime));

        final String msg = message.toString();
        if (error == null && !msg.trim().isEmpty()) {
            printMessage(msg, out, Project.MSG_VERBOSE);
        } else if (!msg.isEmpty()) {
            printMessage(msg, err, Project.MSG_ERR);
        }
        log(msg);
    }

    /**
     * This is an override point: the message that indicates whether a build
     * failed. Subclasses can change/enhance the message.
     *
     * @return The classic "BUILD FAILED"
     */
    protected String getBuildFailedMessage() {
        return "BUILD FAILED";
    }

    /**
     * This is an override point: the message that indicates that a build
     * succeeded. Subclasses can change/enhance the message.
     *
     * @return The classic "BUILD SUCCESSFUL"
     */
    protected String getBuildSuccessfulMessage() {
        return "BUILD SUCCESSFUL";
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
        if (Project.MSG_INFO <= msgOutputLevel && !event.getTarget().getName().equals("")) {
            final String msg = StringUtils.LINE_SEP + event.getTarget().getName() + ":";
            printMessage(msg, out, event.getPriority());
            log(msg);
        }
    }

    /**
     * No-op implementation.
     *
     * @param event Ignored.
     */
    @Override
    public void targetFinished(final BuildEvent event) {
    }

    /**
     * No-op implementation.
     *
     * @param event Ignored.
     */
    @Override
    public void taskStarted(final BuildEvent event) {
    }

    /**
     * No-op implementation.
     *
     * @param event Ignored.
     */
    @Override
    public void taskFinished(final BuildEvent event) {
    }

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

            final StringBuilder message = new StringBuilder();
            if (event.getTask() != null && !emacsMode) {
                // Print out the name of the task if we're in one
                final String name = event.getTask().getTaskName();
                String label = "[" + name + "] ";
                final int size = LEFT_COLUMN_SIZE - label.length();
                final StringBuilder tmp = new StringBuilder();
                for (int i = 0; i < size; i++) {
                    tmp.append(" ");
                }
                tmp.append(label);
                label = tmp.toString();

                BufferedReader r = null;
                try {
                    r = new BufferedReader(new StringReader(event.getMessage()));
                    String line = r.readLine();
                    boolean first = true;
                    do {
                        if (first) {
                            if (line == null) {
                                message.append(label);
                                break;
                            }
                        } else {
                            message.append(StringUtils.LINE_SEP);
                        }
                        first = false;
                        message.append(label).append(line);
                        line = r.readLine();
                    } while (line != null);
                } catch (final IOException e) {
                    // shouldn't be possible
                    message.append(label).append(event.getMessage());
                } finally {
                    if (r != null) {
                        FileUtils.close(r);
                    }
                }

            } else {
                // emacs mode or there is no task
                message.append(event.getMessage());
            }
            final Throwable ex = event.getException();
            if (Project.MSG_DEBUG <= msgOutputLevel && ex != null) {
                message.append(StringUtils.getStackTrace(ex));
            }

            final String msg = message.toString();
            if (priority != Project.MSG_ERR) {
                printMessage(msg, out, priority);
            } else {
                printMessage(msg, err, priority);
            }
            log(msg);
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
     * Prints a message to a PrintStream.
     *
     * @param message The message to print. Should not be <code>null</code>.
     * @param stream A PrintStream to print the message to. Must not be
     *            <code>null</code>.
     * @param priority The priority of the message. (Ignored in this
     *            implementation.)
     */
    private void printMessage(final String message, final PrintStream stream, final int priority) {
        if (useColor && priority == Project.MSG_ERR) {
            stream.print(ANSI_RED);
            stream.print(message);
            stream.println(ANSI_RESET);
        } else {
            stream.println(message);
        }
    }

    /**
     * Empty implementation which allows subclasses to receive the same output
     * that is generated here.
     *
     * @param message Message being logged. Should not be <code>null</code>.
     */
    private void log(final String message) {
    }

    /**
     * Get the current time.
     *
     * @return the current time as a formatted string.
     * @since Ant1.7.1
     */
    protected String getTimestamp() {
        final Date date = new Date(System.currentTimeMillis());
        final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        return formatter.format(date);
    }

    /**
     * Get the project name or null
     *
     * @param event the event
     * @return the project that raised this event
     * @since Ant1.7.1
     */
    protected String extractProjectName(final BuildEvent event) {
        final Project project = event.getProject();
        return (project != null) ? project.getName() : null;
    }

}
