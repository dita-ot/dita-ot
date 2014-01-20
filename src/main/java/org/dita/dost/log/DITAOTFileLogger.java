/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Logger to a log file. Intended to be used only with {@link org.dita.dost.log.DITAOTBuildLogger}, not as a stand-alone logger.
 * 
 * @author Wu, Zhi Qiang
 * @see org.dita.dost.log.DITAOTBuildLogger
 */
public final class DITAOTFileLogger implements DITAOTLogger {
    private static DITAOTFileLogger logger;

    private final File tmpLogFile;

    private String logFile;

    private String logDir;

    private final PrintWriter printWriter;

    private DITAOTFileLogger() {
        try {
            tmpLogFile = File.createTempFile("ditaot-", ".log");
            printWriter = new PrintWriter(new FileOutputStream(tmpLogFile));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the DITAOTFileLogger instance. Singleton.
     * @return DITAOTFileLogger logger
     */
    public static synchronized DITAOTFileLogger getInstance() {
        if (logger == null) {
            logger = new DITAOTFileLogger();
        }

        return logger;
    }

    /**
     * Close the logger. Move log file to logDir.
     * 
     */
    public void closeLogger() {
        final DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

        if (logger == null) {
            return;
        }

        printWriter.close();

        // move log file to logDir
        if (logDir != null && logFile != null) {
            final File log = new File(logDir, logFile);

            if (log.exists()) {
                log.delete();
            }

            if (tmpLogFile.renameTo(log)) {

                javaLogger.info("Log file '" + logFile + "' was generated successfully in directory '" + logDir + "'.");
                return;
            }
        }

        // Try to delete the temp log file.
        if (tmpLogFile.exists()) {
            tmpLogFile.delete();
        }

        javaLogger.error("Failed to generate log file.");
    }

    /**
     * Getter function of logDir.
     * @return Returns the logDir.
     */
    public String getLogDir() {
        return logDir;
    }

    /**
     * This method used to set the log file.
     * 
     * @param filename filename
     */
    public void setLogFile(final String filename) {
        logFile = filename;
    }

    /**
     * The logDir to set.
     * @param logdir logdir
     */
    public void setLogDir(final String logdir) {
        logDir = logdir;
    }

    /**
     * Log the message at info level.
     * @param msg msg
     */
    @Override
    public void info(final String msg) {
        logMessage(msg);
    }

    /**
     * Log the message at warning level.
     * @param msg msg
     */
    @Override
    public void warn(final String msg) {
        logMessage(msg);
    }

    /**
     * Log the message at error level.
     * @param msg msg
     */
    @Override
    public void error(final String msg) {
        logMessage(msg);
    }

    /**
     * Log the message at error level.
     * @param msg msg
     * @param t exception
     */
    @Override
    public void error(final String msg, final Throwable t) {
        error(t.getMessage());
        t.printStackTrace(printWriter);
    }

    /**
     * Log the message at debug level.
     * @param msg msg
     */
    @Override
    public void debug(final String msg) {
        logMessage(msg);
    }

    /**
     * Log ordinary message
     * @param msg
     */
    private void logMessage(final String msg) {
        printWriter.println(msg);
    }

}
