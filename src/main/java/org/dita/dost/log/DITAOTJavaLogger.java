/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.log;
import org.dita.dost.util.LogUtils;
/**
 * Logger to standard output and error.
 * 
 * @author Wu, Zhi Qiang
 * @deprecated since 2.3
 */
@Deprecated
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
    public void info(final String msg) {
        System.out.println(msg);
    }

    /**
     * Log warning message.
     * 
     * @param msg message
     */
    @Override
    public void warn(final String msg) {
        LogUtils.increaseNumOfWarnings();
        System.out.println(msg);
    }

    /**
     * Log error message.
     * 
     * @param msg message
     */
    @Override
    public void error(final String msg) {
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
    public void error(final String msg, final Throwable t) {
        error(t.toString());
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
    public void debug(final String msg) {
        if (debugMode) {
            System.out.println(msg);
        }
    }

}
