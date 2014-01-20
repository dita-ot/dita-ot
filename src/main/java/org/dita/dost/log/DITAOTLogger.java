/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
    public void info(final String msg);

    /**
     * Log a warning message.
     * 
     * @param msg message
     */
    public void warn(final String msg);

    /**
     * Log an error message.
     * 
     * @param msg message
     */
    public void error(final String msg);

    /**
     * Log an error message with cause exception.
     * 
     * @param msg message
     * @param t exception
     */
    public void error(final String msg, final Throwable t);

    /**
     * Log a debug message.
     * 
     * @param msg message
     */
    public void debug(final String msg);

}
