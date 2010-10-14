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
import org.dita.dost.log.DITAOTJavaLogger;

/**
 * Version class, show the version of dita-ot.
 * @author William
 *
 */
public class Version extends Task{
	
	private static final String fversion = "@@VERSION@@";
	
	private static final String milestone = "@@MILESTONE@@";
	
	private static final String otversion = "@@OTVERSION@@";
	
	private DITAOTJavaLogger logger = new DITAOTJavaLogger();

	/**
	 * main function.
	 * @param args input arguments from command line
	 */
	public static void main(String[] args) {
		System.out.println (fversion);

	}
	
	/**
	 * Task execute point.
	 * @throws BuildException exception
	 * @see org.apache.tools.ant.taskdefs.Echo#execute()
	 */
	public void execute() throws BuildException {
		String otversion = getOtversion();
		String milestone = getMilestone();
		
		//set current OT version into antscript REQ ID:3079610
		this.setActiveProjectProperty("otversion", otversion);
		this.setActiveProjectProperty("milestone", milestone);
		//logger.logInfo(message);
		
		
		
		
		
	}


	/**
	 * @return the fversion
	 */
	public static String getVersion() {
		return fversion;
	}

	/**
	 * @return the milestone
	 */
	public static String getMilestone() {
		return "Milestone " + milestone;
	}

	/**
	 * @return the otversion
	 */
	public static String getOtversion() {
		return "DITA Open Toolkit " + otversion;
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
