/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class LogConfigTask extends Task {

	private String logDir = null;
	private String logFile = null;
	
	/**
	 * Default Construtor
	 *
	 */
	public LogConfigTask(){		
	}
	/**
	 * Task execution point
	 * 
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		DITAOTFileLogger logger = DITAOTFileLogger.getInstance();
		String oldLogDir = logger.getLogDir();
		
		initMessageFile();
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

	private void initMessageFile() {
		String messageFile = getProject().getProperty(
				"args.message.file");
		
		if (!new File(messageFile).isAbsolute()) {
			messageFile = new File(getProject().getBaseDir(), messageFile)
					.getAbsolutePath();
		}
		
		MessageUtils.loadMessages(messageFile);
	}
	
	private void initLogDirectory() throws BuildException {
		Project project = getProject();
		File dir = null;
		
		logDir = project.getProperty("args.logdir");
		
		if (logDir == null) {
			logDir = project.getProperty("output.dir");
		}
		
		if (logDir == null || "".equals(logDir)) {
			String msg = MessageUtils.getMessage("DOTJ015F").toString();
			throw new BuildException(msg);
		}
		
		if (!new File(logDir).isAbsolute()) {
			logDir = new File(project.getBaseDir(), logDir).getAbsolutePath();
		}
		
		// create log directory
		dir = new File(logDir);
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				Properties params = new Properties();
				String msg = null;
				params.put("%1", logDir);
				msg = MessageUtils.getMessage("DOTJ016F", params).toString();
				throw new BuildException(msg);
			}
		}
	}

	private void initLogFile() throws BuildException {
		Project project = getProject();
		String inputFile = null;
		String rootName = null;
		String transType = null;
		int pos = 0;
		
		String input = project.getProperty("args.input");
		if (input == null) {
			input = project.getProperty("dita.input");
		}

		if (input == null) {
			String msg = MessageUtils.getMessage("DOTJ017F").toString();
			throw new BuildException(msg);
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
