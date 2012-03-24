/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.log;

/**
 * Common logging interface.
 */
public interface DITAOTLogger {

    /**
     * Log an information message.
     * 
     * @param msg message
     */
    public void logInfo(final String msg);

    /**
     * Log a warning message.
     * 
     * @param msg message
     */
    public void logWarn(final String msg);

    /**
     * Log an error message.
     * 
     * @param msg message
     */
    public void logError(final String msg);

    /**
     * Log an error message with cause exception.
     * 
     * @param msg message
     * @param t exception
     */
    public void logError(final String msg, final Throwable t);

    /**
     * Log a fatal error message.
     * 
     * @param msg message
     * @deprecated throw exception instead
     */
    @Deprecated
    public void logFatal(final String msg);

    /**
     * Log a debug message.
     * 
     * @param msg message
     */
    public void logDebug(final String msg);

    /**
     * Log an exception.
     * 
     * @param t exception
     * @deprecated use {@link #logError(String, Throwable)} instead
     */
    @Deprecated
    public void logException(final Throwable t);

}
