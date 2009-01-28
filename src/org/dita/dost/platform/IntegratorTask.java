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
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;

/**
 * Task run by ant scripts, invoking Task
 * @author Zhang, Yuan Peng
 */
public class IntegratorTask extends Task {

	private Integrator adaptee;
	
	/**
	 * Default Constructor
	 */
	public IntegratorTask() {
		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
            //The default sax driver is set to xerces's sax driver
			StringUtils.initSaxDriver();
        }
		adaptee = new Integrator();
	}

	/**
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		adaptee.execute();
	}

	/**
	 * Return the basedir
	 * @return
	 */
	public String getBasedir() {
		return adaptee.getBasedir();
	}
	
	/**
	 * Set the basedir
	 * @param basedir
	 */
	public void setBasedir(String basedir) {
		adaptee.setBasedir(basedir);
	}
	
	/**
	 * Return the ditaDir
	 * @return
	 */
	public String getDitadir() {
		return adaptee.getDitaDir();
	}

	/**
	 * Set the ditaDir
	 * @param ditaDir
	 */
	public void setDitadir(String ditaDir) {
		adaptee.setDitaDir(ditaDir);
	}

	/**
	 * Return the properties file
	 * @return
	 */
	public File getProperties() {
		return adaptee.getProperties();
	}

	/**
	 * Set the properties file
	 * @param propertiesFile
	 */
	public void setProperties(File propertiesFile) {
		adaptee.setProperties(propertiesFile);
	}

}
