/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

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
    void info(final String msg);

    /**
     * Log a warning message.
     * 
     * @param msg message
     */
    void warn(final String msg);

    /**
     * Log an error message.
     * 
     * @param msg message
     */
    void error(final String msg);

    /**
     * Log an error message with cause exception.
     * 
     * @param msg message
     * @param t exception
     */
    void error(final String msg, final Throwable t);

    /**
     * Log a debug message.
     * 
     * @param msg message
     */
    void debug(final String msg);

}
