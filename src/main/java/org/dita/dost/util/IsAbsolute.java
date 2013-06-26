/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.condition.Condition;

/**
 * Extended from Ant's Condition interface, this class is used to
 * determine if a given path is absolute path.
 *
 * @author Wu, Zhi Qiang
 */
public final class IsAbsolute implements Condition {
    private String path = null;

    /**
     * Default Constructor.
     *
     */
    public IsAbsolute(){
    }

    /**
     * Set the path.
     * @param pth The path to set.
     */
    public void setPath(final String pth) {
        path = pth;
    }

    /**
     * 
     * @see org.apache.tools.ant.taskdefs.condition.Condition#eval()
     */
    @Override
    public boolean eval() throws BuildException {
        return new File(path).isAbsolute();
    }

}
