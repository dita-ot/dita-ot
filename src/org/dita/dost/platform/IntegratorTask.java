/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.util.Constants;

/**
 *
 * @author Zhang, Yuan Peng
 */
public class IntegratorTask extends Task {

	private Integrator adaptee;
	
	public IntegratorTask() {
		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
            //The default sax driver is set to xerces's sax driver
            System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_DEFAULT_CLASS);
        }
		adaptee = new Integrator();
	}

	public void execute() throws BuildException {
		adaptee.execute();
	}

	public String getDitadir() {
		return adaptee.getDitaDir();
	}

	public void setDitadir(String ditaDir) {
		adaptee.setDitaDir(ditaDir);
	}
}
