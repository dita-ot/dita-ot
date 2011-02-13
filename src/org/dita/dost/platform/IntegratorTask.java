/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Task run by ant scripts, invoking Task.
 * @author Zhang, Yuan Peng
 */
public class IntegratorTask extends Task {

	private final Integrator adaptee;
	
	/**
	 * Default Constructor.
	 */
	public IntegratorTask() {
		adaptee = new Integrator();
	}

	@Override
	public void execute() throws BuildException {
		adaptee.execute();
	}

	/**
	 * Return the basedir.
	 * @return basedir
	 */
	public String getBasedir() {
		return adaptee.getBasedir();
	}
	
	/**
	 * Set the basedir.
	 * @param basedir basedir
	 */
	public void setBasedir(final String basedir) {
		adaptee.setBasedir(basedir);
	}
	
	/**
	 * Return the ditaDir.
	 * @return ditaDir
	 */
	public String getDitadir() {
		return adaptee.getDitaDir();
	}

	/**
	 * Set the ditaDir.
	 * @param ditaDir ditaDir
	 */
	public void setDitadir(final String ditaDir) {
		adaptee.setDitaDir(ditaDir);
	}

	/**
	 * Return the properties file.
	 * @return file
	 */
	public File getProperties() {
		return adaptee.getProperties();
	}

	/**
	 * Set the properties file.
	 * @param propertiesFile propertiesFile
	 */
	public void setProperties(final File propertiesFile) {
		adaptee.setProperties(propertiesFile);
	}

}
