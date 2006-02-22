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
public class IsAbsolute implements Condition {
	private String path = null;

	/**
	 * @param path The path to set.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	public boolean eval() throws BuildException {
		return new File(path).isAbsolute();
	}

}
