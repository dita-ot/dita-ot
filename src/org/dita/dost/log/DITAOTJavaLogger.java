/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;
import org.dita.dost.util.LogUtils;
/**
 * Logger to standard output and error.
 * 
 * @author Wu, Zhi Qiang
 */
public class DITAOTJavaLogger implements DITAOTLogger {
	private static boolean debugMode = false;
	
	/**
	 * Default Constructor.
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
	 * @param msg message
	 */
	public void logInfo(String msg) {
		System.out.println(msg);
	}

	/**
	 * Log warning message.
	 * 
	 * @param msg message
	 */
	public void logWarn(String msg) {
		//add by wxzhang 20070514
		LogUtils.increaseNumOfWarnings();
		//add end by wxzhang 20070514
		System.out.println(msg);
	}

	/**
	 * Log error message.
	 * 
	 * @param msg message
	 */
	public void logError(String msg) {
		//add by wxzhang 20070514
		LogUtils.increaseNumOfErrors();
		//add end by wxzhang 20070514
		System.err.println(msg);
	}

	/**
	 * Log fatal error message.
	 * 
	 * @param msg message
	 */
	public void logFatal(String msg) {
		//add by wxzhang 20070514
		LogUtils.increaseNumOfFatals();
		//add end by wxzhang 20070514
		System.err.println(msg);
	}

	/**
	 * Log debug info when DEBUG mode enabled. 
	 * 
	 * @param msg message
	 */
	public void logDebug(String msg) {
		if (debugMode) {
			System.out.println(msg);
		}
	}
	
	/**
	 * Log exception.
	 * 
	 * @param t exception
	 */
	public void logException(Throwable t) {
		logError(t.toString());
		if (debugMode) {
			t.printStackTrace(System.err);
		}
	}
}
