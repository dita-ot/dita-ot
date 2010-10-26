/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2009 All Rights Reserved.
 */
package org.dita.dost.util;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

/**
 * Version class, show the version of dita-ot in ant script.
 * @author William
 *
 */
public class AntVersion extends Task{
	
	//private VersionUtil versionUtil = new VersionUtil();

	/**
	 * main function.
	 * @param args input arguments from command line
	 */
	public static void main(String[] args) {
		//System.out.println (fversion);

	}
	
	/**
	 * Task execute point.
	 * @throws BuildException exception
	 * @see org.apache.tools.ant.taskdefs.Echo#execute()
	 */
	public void execute() throws BuildException {
		VersionUtil versionUtil = new VersionUtil();
		
		String otversion = versionUtil.getOtversion();
		String milestone = versionUtil.getMilestone();
		
		//set current OT version into antscript REQ ID:3079610
		this.setActiveProjectProperty("otversion", otversion);
		this.setActiveProjectProperty("milestone", milestone);
		//logger.logInfo(message);
	}
	
	/**
     * Sets property in active ant project with name specified inpropertyName,
     * and value specified in propertyValue parameter
     */
    private void setActiveProjectProperty(String propertyName, String propertyValue) {
        Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }

}
