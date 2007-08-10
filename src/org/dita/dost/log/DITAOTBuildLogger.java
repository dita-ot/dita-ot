/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

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
import org.dita.dost.util.LogUtils;

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

	/** Line separator */
	protected static final String LINE_SEP = StringUtils.LINE_SEP;

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
	 * This is an override point: the message that indicates whether a build
	 * failed. Subclasses can change/enhance the message.
	 * 
	 * @return The classic "BUILD FAILED"
	 */
	protected static String getBuildFailedMessage() {
		return "BUILD FAILED";
	}

	/**
	 * This is an override point: the message that indicates that a build
	 * succeeded. Subclasses can change/enhance the message.
	 * 
	 * @return The classic "BUILD SUCCESSFUL"
	 */
	protected static String getBuildSuccessfulMessage() {
		return "BUILD SUCCESSFUL";
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
	protected static void printMessage(final String message,
			final PrintStream stream, final int priority) {
		if (priority <= Project.MSG_INFO) {
			stream.println(message);
		}
	}

	private PrintStream err = null;

	private DITAOTFileLogger logger = null;

	/** Lowest level of message to write out */
	private int msgOutputLevel = Project.MSG_ERR;

	private PrintStream out = null;

	/** Time of the start of the build */
	private long startTime = System.currentTimeMillis();

	/**
	 * Constructor to init logger
	 * 
	 */
	public DITAOTBuildLogger() {
		logger = DITAOTFileLogger.getInstance();
	}

	/**
	 * Invoke when build finished. Do the logging.
	 * 
	 * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void buildFinished(BuildEvent event) {
		Throwable error = event.getException();
		StringBuffer message = new StringBuffer();
		String msg = null;

		message.append("Processing ended.");
		message.append(LINE_SEP);

		if (error == null && LogUtils.haveFatalOrError() == false) {
			message.append(LINE_SEP);
			message.append(getBuildSuccessfulMessage());
		} else {
			message.append(LINE_SEP);
			message.append(getBuildFailedMessage());
			message.append(LINE_SEP);
			// If ant have not errors
			if (error != null) {
				message.append(error.toString());
				message.append(LINE_SEP);
				if (Project.MSG_VERBOSE <= msgOutputLevel) {
					message.append(StringUtils.getStackTrace(error));
				}
				// add by start wxzhang 20070514
				warnAndErrorCaptured(error.toString());
				// add by end wxzhang 20070514
			}
		}
		// add by start wxzhang 20070514
		message.append(LINE_SEP);
		message.append(LogUtils.getLogStatisticInfo());
		// add by end wxzhang 20070514
		message.append(LINE_SEP);
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
	 * Record start time
	 * 
	 * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void buildStarted(BuildEvent event) {
		startTime = System.currentTimeMillis();
	}

	/**
	 * 
	 * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
	 */
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
			BufferedReader r = null;
			try {
				String line;
				boolean first = true;
				r = new BufferedReader(new StringReader(event.getMessage()));
				line = r.readLine();
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
			} finally {
				try {
					r.close();
				} catch (IOException ioe) {

				}
			}
		} else {
			message.append(event.getMessage());
		}

		msg = message.toString();
		// add start by wxzhang 20070518
		warnAndErrorCaptured(msg);
		// add end by wxzhang 20070518
		if (priority != Project.MSG_ERR) {
			boolean flag = false;
			// filter out message came from XSLT in console,
			// except those contains [DOTXxxx]
			if (eventTask != null && "xslt".equals(eventTask.getTaskName())
					&& msg.indexOf("DOTX") == -1) {
				flag = true;
			}

			// filter out fop messages in console
			if (eventTask != null && "fop".equals(eventTask.getTaskName())) {
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
	 * Ignored
	 * 
	 * @see org.apache.tools.ant.BuildLogger#setEmacsMode(boolean)
	 */
	public void setEmacsMode(boolean mode) {
	}

	/**
	 * Setter function for errorPrintStream
	 * 
	 * @see org.apache.tools.ant.BuildLogger#setErrorPrintStream(java.io.PrintStream)
	 */
	public void setErrorPrintStream(PrintStream errorPrintStream) {
		this.err = new PrintStream(errorPrintStream, true);
	}

	/**
	 * Setter function for messageOutputLevel
	 * 
	 * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
	 */
	public void setMessageOutputLevel(int level) {
		this.msgOutputLevel = level;
	}

	/**
	 * Setter function for outputPrintStream, and set it to autoflush
	 * 
	 * @see org.apache.tools.ant.BuildLogger#setOutputPrintStream(java.io.PrintStream)
	 */
	public void setOutputPrintStream(PrintStream output) {
		this.out = new PrintStream(output, true);
	}

	/**
	 * Ignored
	 * 
	 * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void targetFinished(BuildEvent event) {
	}

	/**
	 * 
	 * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void targetStarted(BuildEvent event) {
		if (Project.MSG_INFO <= msgOutputLevel
				&& !"".equals(event.getTarget().getName())) {
			String desc = event.getTarget().getDescription();
			String msg = desc + "...";
			if (desc == null || Constants.STRING_EMPTY.equals(desc.trim())) {
				return;
			}
			printMessage(msg, out, Project.MSG_INFO);
			logger.logInfo(msg);
		}
	}

	/**
	 * Ignored
	 * 
	 * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
	 */
	public void taskFinished(BuildEvent event) {
	}

	/**
	 * Ignored
	 * 
	 * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
	 */
	public void taskStarted(BuildEvent event) {
	}

	private void warnAndErrorCaptured(String msg) {
		if (msg != null) {
			if (msg.toUpperCase().indexOf("PIPELINE") != -1) {
				if (msg.toUpperCase().indexOf("[FATAL]") != -1) {
					LogUtils.increaseNumOfFatals();
				}
				if (msg.toUpperCase().indexOf("[ERROR]") != -1) {
					LogUtils.increaseNumOfErrors();
				}
				if (msg.toUpperCase().indexOf("[WARN]") != -1) {
					LogUtils.increaseNumOfWarnings();
				}
			}
			if (msg.toUpperCase().indexOf("DOTA") != -1) {
				// cann't captured the FATAL message of ant in the messageLogged()
				// so this should captured in the build finished!
				if (msg.toUpperCase().indexOf("[FATAL]") != -1) {
					LogUtils.increaseNumOfFatals();
				}
				if (msg.toUpperCase().indexOf("[ERROR]") != -1) {
					LogUtils.increaseNumOfErrors();
				}
				if (msg.toUpperCase().indexOf("[WARN]") != -1) {
					LogUtils.increaseNumOfWarnings();
				}
			}
		}
	}
}
