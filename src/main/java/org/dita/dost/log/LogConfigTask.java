/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.util.Properties;
import java.util.Vector;

import org.apache.tools.ant.BuildListener;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageBean;

/**
 * Initialise log output directory and open {@link org.dita.dost.log.DITAOTFileLogger DITAOTFileLogger}.
 * 
 * Run only when {@link org.dita.dost.log.DITAOTBuildLogger DITAOTBuildLogger} is used as a build logger.
 * 
 * @author Wu, Zhi Qiang
 */
public final class LogConfigTask extends Task {

    private String logDir = null;
    private String logFile = null;

    /**
     * Default Construtor.
     *
     */
    public LogConfigTask(){
    }
    /**
     * Task execution point.
     * @throws BuildException exception
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        if (!isEnabled()) {
            return;
        }
        final DITAOTFileLogger logger = DITAOTFileLogger.getInstance();
        final String oldLogDir = logger.getLogDir();

        initLogDirectory();
        initLogFile();

        if (oldLogDir != null) {
            /*
             * Try to re-do log configuration, so the transformation
             * is in batch mode.
             * 
             * If the user has specified a common logdir for all
             * transformations, it will be used as log directory;
             * 
             * If the user hasn't specified a common dir for all
             * transformations, and if all transformations have same
             * output directory, the common output direcory will be
             * used as log directory.
             * 
             * If there is no same output directory for all transformations,
             * the basedir will be used as default log directory.
             **/
            if (!oldLogDir.equals(logDir)) {
                logDir = getProject().getBaseDir().getAbsolutePath();
            }
            logFile = "ditaot_batch.log";
        }

        logger.setLogDir(logDir);
        logger.setLogFile(logFile);
    }
    
    /**
     * Check if log configuration is enabled.
     * 
     * This exists for backwards compatibility, {@link org.dita.dost.log.DITAOTFileLogger  DITAOTFileLogger} should be initialised only if
     * {@link org.dita.dost.log.DITAOTBuildLogger DITAOTBuildLogger} is used as a build logger.
     * 
     * @return {@code true} if enabled, otherwise {@code false}
     */
    private boolean isEnabled() {
        @SuppressWarnings("unchecked")
        final Vector<BuildListener> listeners = getProject().getBuildListeners();
        for (final BuildListener listener: listeners) {
            if (listener instanceof DITAOTBuildLogger) {
                return true;
            }
        }
        return false;
    }

    private void initLogDirectory() throws BuildException {
        final Project project = getProject();
        File dir = null;

        logDir = project.getProperty("args.logdir");

        if (logDir == null) {
            logDir = project.getProperty("output.dir");
        }

        if (logDir == null || "".equals(logDir)) {
            final MessageBean msgBean=MessageUtils.getInstance().getMessage("DOTJ015F");
            final String msg = msgBean.toString();
            throw new BuildException(msg,new DITAOTException(msgBean,null,msg));
        }

        if (!new File(logDir).isAbsolute()) {
            logDir = new File(project.getBaseDir(), logDir).getAbsolutePath();
        }

        // create log directory
        dir = new File(logDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                final MessageBean msgBean=MessageUtils.getInstance().getMessage("DOTJ016F", logDir);
                final String msg = msgBean.toString();
                throw new BuildException(msg,new DITAOTException(msgBean,null,msg));
            }
        }
    }

    private void initLogFile() throws BuildException {
        final Project project = getProject();
        String inputFile = null;
        String rootName = null;
        String transType = null;
        int pos = 0;

        String input = project.getProperty("args.input");
        if (input == null) {
            input = project.getProperty("dita.input");
        }

        if (input == null) {
            final MessageBean msgBean=MessageUtils.getInstance().getMessage("DOTJ017F");
            final String msg = msgBean.toString();
            throw new BuildException(msg,new DITAOTException(msgBean,null,msg));
        }

        transType = project.getProperty("transtype");

        if (transType == null) {
            transType = "";
        }

        inputFile = new File(input).getName();
        pos = inputFile.indexOf('.');
        rootName = (pos == -1) ? inputFile : inputFile.substring(0, pos);
        logFile = rootName + "_" + transType + ".log";
    }
}
