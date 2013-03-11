/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
public final class DITAOTJavaLogger implements DITAOTLogger {
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
    @Override
    public void logInfo(final String msg) {
        System.out.println(msg);
    }

    /**
     * Log warning message.
     * 
     * @param msg message
     */
    @Override
    public void logWarn(final String msg) {
        LogUtils.increaseNumOfWarnings();
        System.out.println(msg);
    }

    /**
     * Log error message.
     * 
     * @param msg message
     */
    @Override
    public void logError(final String msg) {
        LogUtils.increaseNumOfErrors();
        System.err.println(msg);
    }

    /**
     * Log error message.
     * 
     * @param msg message
     * @param t exception
     */
    @Override
    public void logError(final String msg, final Throwable t) {
        logError(t.toString());
        if (debugMode) {
            t.printStackTrace(System.err);
        }
    }

    /**
     * Log debug info when DEBUG mode enabled.
     * 
     * @param msg message
     */
    @Override
    public void logDebug(final String msg) {
        if (debugMode) {
            System.out.println(msg);
        }
    }

}
