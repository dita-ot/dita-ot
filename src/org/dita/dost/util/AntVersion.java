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
public final class AntVersion extends Task{

    /**
     * Task execute point.
     * @throws BuildException exception
     * @see org.apache.tools.ant.taskdefs.Echo#execute()
     */
    @Override
    public void execute() throws BuildException {
        final VersionUtil versionUtil = new VersionUtil();

        final String otversion = versionUtil.getOtversion();

        //set current OT version into antscript REQ ID:3079610
        this.setActiveProjectProperty("otversion", otversion);
        //logger.logInfo(message);
    }

    /**
     * Sets property in active ant project with name specified inpropertyName,
     * and value specified in propertyValue parameter
     */
    private void setActiveProjectProperty(final String propertyName, final String propertyValue) {
        final Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }

}
