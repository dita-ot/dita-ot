/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.StringUtils;
import org.dita.dost.util.Constants;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class DITAOTBuildLogger implements BuildLogger {

	/**
	 * Size of left-hand column for right-justified task name.
	 * 
	 * @see #messageLogged(BuildEvent)
	 */
	public static final int LEFT_COLUMN_SIZE = 12;

	private PrintStream out = null;

	private PrintStream err = null;

	private DITAOTFileLogger logger = null;

	/** Lowest level of message to write out */
	protected int msgOutputLevel = Project.MSG_ERR;

	/** Time of the start of the build */
	private long startTime = System.currentTimeMillis();

	/** Line separator */
	protected static final String lSep = StringUtils.LINE_SEP;

	public DITAOTBuildLogger() {
		initLogger();
	}

	public void setMessageOutputLevel(int level) {
		this.msgOutputLevel = level;
	}

	public void setOutputPrintStream(PrintStream output) {
		this.out = new PrintStream(output, true);
	}

	public void setErrorPrintStream(PrintStream err) {
		this.err = new PrintStream(err, true);
	}

	public void setEmacsMode(boolean err) {
	}

	public void buildStarted(BuildEvent event) {
		startTime = System.currentTimeMillis();
	}

	public void buildFinished(BuildEvent event) {
		Throwable error = event.getException();
		StringBuffer message = new StringBuffer();
		String msg = null;

		message.append("Processing ended.");
		message.append(lSep);

		if (error == null) {
			message.append(lSep);
			message.append(getBuildSuccessfulMessage());
		} else {
			message.append(lSep);
			message.append(getBuildFailedMessage());
			message.append(lSep);
			message.append(error.toString());
			message.append(lSep);
			if (Project.MSG_VERBOSE <= msgOutputLevel) {
				message.append(StringUtils.getStackTrace(error));
			}
		}

		message.append(lSep);
		message.append("Total time: ");
		message.append(formatTime(System.currentTimeMillis() - startTime));

		msg = message.toString();

		if (error == null) {
			printMessage(msg, out, Project.MSG_INFO);
			logger.logInfo(msg);
		} else {
			printMessage(msg, err, Project.MSG_ERR);
			logger.logError(msg);
		}

		logger.closeLogger();
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

	public void targetStarted(BuildEvent event) {
		if (Project.MSG_INFO <= msgOutputLevel
				&& !event.getTarget().getName().equals("")) {
			String desc = event.getTarget().getDescription();
			if (desc == null || Constants.STRING_EMPTY.equals(desc.trim())) {
				return;
			}
			String msg = desc + "...";
			printMessage(msg, out, Project.MSG_INFO);
			logger.logInfo(msg);
		}
	}

	public void targetFinished(BuildEvent event) {
	}

	public void taskStarted(BuildEvent event) {
	}

	public void taskFinished(BuildEvent event) {
	}

	public void messageLogged(BuildEvent event) {
		StringBuffer message = new StringBuffer();
		String msg = null;
		Task eventTask = event.getTask();
		int priority = event.getPriority();
		
		// Filter out messages based on priority
		if (priority > msgOutputLevel) {
			return;
		}

		if (eventTask != null) {
			// Print out the name of the task if we're in one
			String label = new StringBuffer().append("  [").append(
					eventTask.getTaskName()).append("] ").toString();

			try {
				BufferedReader r = new BufferedReader(new StringReader(event
						.getMessage()));
				String line = r.readLine();
				boolean first = true;
				while (line != null) {
					if (!first) {
						message.append(StringUtils.LINE_SEP);
					}
					first = false;
					message.append(label).append(line);
					line = r.readLine();
				}
			} catch (IOException e) {
				// shouldn't be possible
				message.append(label).append(event.getMessage());
			}
		} else {
			message.append(event.getMessage());
		}

		msg = message.toString();

		if (priority != Project.MSG_ERR) {
			boolean flag = false;
			// filter out message came from XSLT in console,
			// except those contains [DOTXxxx]
			if (eventTask != null && eventTask.getTaskName().equals("xslt")
					&& msg.indexOf("DOTX") == -1) {
				flag = true;
			}
			
			// filter out fop messages in console
			if (eventTask != null && eventTask.getTaskName().equals("fop")) {
				flag = true;
			}

			if (!flag) {
				printMessage(msg, out, priority);
			}
			
			// always log to log file
			logger.logInfo(msg);
		} else {
			printMessage(msg, err, priority);
			logger.logError(msg);
		}
	}

	/**
	 * Convenience method to format a specified length of time.
	 * 
	 * @param millis
	 *            Length of time to format, in milliseconds.
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
	 * @param message
	 *            The message to print. Should not be <code>null</code>.
	 * @param stream
	 *            A PrintStream to print the message to. Must not be
	 *            <code>null</code>.
	 * @param priority
	 *            The priority of the message. (Ignored in this implementation.)
	 */
	protected void printMessage(final String message, final PrintStream stream,
			final int priority) {
		if (priority <= Project.MSG_INFO) {
			stream.println(message);
		}
	}

	private void initLogger() {
		logger = DITAOTFileLogger.getInstance();
	}

}
