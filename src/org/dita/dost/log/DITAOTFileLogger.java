/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class DITAOTFileLogger {
	private File tmpLogFile = null;

	private String logFile = null;

	private String logDir = null;

	private PrintWriter printWriter = null;

	private static DITAOTFileLogger logger = null;

	private DITAOTFileLogger() {
		try {
			tmpLogFile = File.createTempFile("ditaot-", ".log");
			printWriter = new PrintWriter(new FileOutputStream(tmpLogFile));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public static DITAOTFileLogger getInstance() {
		if (logger == null) {
			logger = new DITAOTFileLogger();
		}

		return logger;
	}

	public void closeLogger() {
		DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
		
		if (logger == null) {
			return;
		}

		printWriter.close();
				
		// move log file to logDir
		if (logDir != null && logFile != null) {			
			File log = new File(logDir, logFile);
			
			if (log.exists()) {
				log.delete();
			}
			
			if (tmpLogFile.renameTo(log)) {
				StringBuffer buff = new StringBuffer(256);
				buff.append("Log file '").append(logFile);
				buff.append("' was generated successfully in directory '");
				buff.append(logDir).append("'.");				
				
				javaLogger.logInfo(buff.toString());
				return;
			}
		}
		
		// Try to delete the temp log file.
		if (tmpLogFile.exists()) {
			tmpLogFile.delete();
		}
		
		javaLogger.logError("Failed to generate log file.");
	}

	/**
	 * @return Returns the logDir.
	 */
	public String getLogDir() {
		return logDir;
	}

	/**
	 * This method used to set the log file.
	 * 
	 * @param filename
	 */
	public void setLogFile(String filename) {
		this.logFile = filename;
	}

	/**
	 * @param logDir
	 *            The logDir to set.
	 */
	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}

	public void logInfo(String msg) {
		logMessage(msg);
	}

	public void logWarn(String msg) {
		logMessage(msg);
	}

	public void logError(String msg) {
		logMessage(msg);
	}

	public void logDebug(String msg) {
		logMessage(msg);
	}

	public void logException(Throwable t) {
		logError(t.getMessage());
		t.printStackTrace(printWriter);
	}

	private void logMessage(String msg) {
		printWriter.println(msg);
	}

}
