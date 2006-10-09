/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class DITAOTJavaLogger {
	private static boolean debugMode = false;
	
	/**
	 * Default Constructor
	 *
	 */
	public DITAOTJavaLogger(){
	}

	/**
	 * Enable DEBUG mode.
	 */
	public static void enableDebugMode() {
		DITAOTJavaLogger.debugMode = true;
	}

	/**
	 * Log information.
	 * 
	 * @param msg
	 */
	public void logInfo(String msg) {
		System.out.println(msg);
	}

	/**
	 * Log warning message.
	 * 
	 * @param msg
	 */
	public void logWarn(String msg) {
		System.out.println(msg);
	}

	/**
	 * Log error message.
	 * 
	 * @param msg
	 */
	public void logError(String msg) {
		System.err.println(msg);
	}

	/**
	 * Log fatal error message.
	 * 
	 * @param msg
	 */
	public void logFatal(String msg) {
		System.err.println(msg);
	}

	/**
	 * Log debug info when DEBUG mode enabled. 
	 * 
	 * @param msg
	 */
	public void logDebug(String msg) {
		if (debugMode) {
			System.out.println(msg);
		}
	}
	
	/**
	 * Log exception.
	 * 
	 * @param t
	 */
	public void logException(Throwable t) {
		logError(t.toString());
		if (debugMode) {
			t.printStackTrace(System.err);
		}
	}
}
