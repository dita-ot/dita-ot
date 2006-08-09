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
import org.dita.dost.log.DITAOTJavaLogger;

/**
 * Class description goes here. 
 *
 * @author Wu, Zhi Qiang
 */
public class DITAOTCopy extends Task {
	private String includes = null;
	private String destDir = null;  // the destination directory
	private DITAOTJavaLogger logger = new DITAOTJavaLogger();
	
	/**
	 * Default Constructor
	 * 
	 */
	public DITAOTCopy(){
	}
	
	/**
	 * Set the copy files
	 * @param incld The includes to set.
	 */
	public void setIncludes(String incld) {
		this.includes = incld;
	}
	
    /**
     * Set the destination directory.
     * @param destdir the destination directory.
     */
    public void setTodir(String destdir) {
        this.destDir = destdir;
    }

	/** (non-Javadoc)
	 * @see org.apache.tools.ant.Task#execute()
	 */
	public void execute() throws BuildException {
		FileUtils fileUitls = FileUtils.newFileUtils();
		StringTokenizer tokenizer;
		
		if (includes == null) {
			return;
		}
		
		tokenizer = new StringTokenizer(includes, Constants.COMMA);
		try {
			while (tokenizer.hasMoreTokens()) {
				File srcFile = new File(tokenizer.nextToken());
				if (srcFile.exists()) {
					File destFile = new File(destDir, srcFile.getName());								
					fileUitls.copyFile(srcFile, destFile);
				}
			}
		}catch(IOException e){
			logger.logException(e);		
		}
	}

}
