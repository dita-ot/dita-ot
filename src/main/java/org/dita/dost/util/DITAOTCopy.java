/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.DITAOTAntLogger;

/**
 * Class description goes here.
 *
 * @author Wu, Zhi Qiang
 */
public final class DITAOTCopy extends Task {
    private String includes = null;
    private String relativePaths = null;
    private String destDir = null;  // the destination directory
    private DITAOTLogger logger;

    /**
     * Default Constructor.
     * 
     */
    public DITAOTCopy(){
    }

    /**
     * Set the copy files.
     * @param incld The includes to set.
     */
    public void setIncludes(final String incld) {
        includes = incld;
    }

    /**
     * Set the destination directory.
     * @param destdir the destination directory.
     */
    public void setTodir(final String destdir) {
        destDir = destdir;
    }

    /**
     * Set the relative path from output directory.
     * @param relPaths the relative path .
     */
    public void setRelativePaths(final String relPaths) {
        relativePaths = relPaths;
    }

    /**
     * @see org.apache.tools.ant.Task#execute()
     */
    @Override
    public void execute() throws BuildException {
        logger = new DITAOTAntLogger(getProject());
        final FileUtils fileUitls = FileUtils.newFileUtils();
        StringTokenizer tokenizer;
        StringTokenizer pathTokenizer;
        if (includes == null) {
            return;
        }
        tokenizer = new StringTokenizer(includes, COMMA);
        if (relativePaths == null) {
            try {
                while (tokenizer.hasMoreTokens()) {
                    final File srcFile = new File(tokenizer.nextToken());
                    if (srcFile.exists()) {
                        final File destFile = new File(destDir, srcFile.getName());
                        fileUitls.copyFile(srcFile, destFile);
                    }
                }
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            }
        }else{
            pathTokenizer = new StringTokenizer(relativePaths, COMMA);
            StringBuffer realDest=null;
            try {
                while (tokenizer.hasMoreTokens()) {
                	final File srcFile = new File(tokenizer.nextToken());
                    File destFile = null;
                    //destDir is the ouput dir
                    //pathTokenizer is the relative path with the filename
                    pathTokenizer = new StringTokenizer(relativePaths, COMMA);
					while (pathTokenizer.hasMoreTokens()) {
						if (destDir != null && destDir.trim().length() > 0) {
							realDest=new StringBuffer();
							realDest.append(destDir).append(File.separator)
									.append(pathTokenizer.nextToken());
							final File temp = new File(realDest.toString());
							if (temp.getName().equalsIgnoreCase(srcFile.getName())){
								destFile = temp;
								break;
							}
						}
					}
                    if (srcFile.exists()&&destFile!=null) {                      
                        fileUitls.copyFile(srcFile, destFile);
                    }
                }
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

}