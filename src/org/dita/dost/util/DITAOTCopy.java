/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;

/**
 * Class description goes here. 
 *
 * @author Wu, Zhi Qiang
 */
public class DITAOTCopy extends Task {
	private String includes = null;
	private String destDir = null;  // the destination directory	
	
	/**
	 * @param includes The includes to set.
	 */
	public void setIncludes(String includes) {
		this.includes = includes;
	}
	
    /**
     * Set the destination directory.
     * @param destDir the destination directory.
     */
    public void setTodir(String destDir) {
        this.destDir = destDir;
    }

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		FileUtils fileUitls = FileUtils.newFileUtils();
		
		if (includes == null) {
			return;
		}
		
		StringTokenizer tokenizer = new StringTokenizer(includes, Constants.COMMA);
		while (tokenizer.hasMoreTokens()) {
			File srcFile = new File(tokenizer.nextToken());
			if (srcFile.exists()) {
				File destFile = new File(destDir, srcFile.getName());
				
				try {
					fileUitls.copyFile(srcFile, destFile);
				} catch (IOException e) {
				}
			}
		}
	}

}
