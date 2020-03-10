/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.XMLUtils;

import java.io.File;

import static org.dita.dost.util.Constants.ANT_REFERENCE_XML_UTILS;

public final class InitializeProjectTask extends Task {
    @Override
    public void execute() throws BuildException {
        log("Initializing project", Project.MSG_INFO);
        final File ditaDir = new File(getProject().getUserProperty("dita.dir"));
        CatalogUtils.setDitaDir(ditaDir);
        XMLUtils xmlUtils = getProject().getReference(ANT_REFERENCE_XML_UTILS);
        if (xmlUtils == null) {
            xmlUtils = new XMLUtils();
            xmlUtils.setLogger(new DITAOTAntLogger(getProject()));
            getProject().addReference(ANT_REFERENCE_XML_UTILS, xmlUtils);
        }
    }
}