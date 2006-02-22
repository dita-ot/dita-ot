/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class FileUtils {

	/**
	 * @param lcasefn
	 * @return
	 */
	public static boolean isHTMLFile(String lcasefn) {
		return (lcasefn.endsWith(Constants.FILE_EXTENSION_HTML) || lcasefn
				.endsWith(Constants.FILE_EXTENSION_HTM));
	}

	/**
	 * @param lcasefn
	 * @return
	 */
	public static boolean isDITAFile(String lcasefn) {
		return isDITATopicFile(lcasefn) || isDITAMapFile(lcasefn);
	}

	/**
	 * @param lcasefn
	 * @return
	 */
	public static boolean isDITATopicFile(String lcasefn) {
		return lcasefn.endsWith(Constants.FILE_EXTENSION_DITA)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_XML);
	}

	/**
	 * @param lcasefn
	 * @return
	 */
	public static boolean isDITAMapFile(String lcasefn) {
		return lcasefn.endsWith(Constants.FILE_EXTENSION_DITAMAP);
	}

	/**
	 * @param lcasefn
	 * @return
	 */
	public static boolean isSupportedImageFile(String lcasefn) {
		return lcasefn.endsWith(Constants.FILE_EXTENSION_JPG)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_GIF)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_EPS);
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static boolean isTopicFile(String lcasefn) {
		return lcasefn.endsWith(Constants.FILE_EXTENSION_DITA)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_XML);
	}

	/**
	 * @param fileName
	 * @return
	 */
	public static boolean isValidTarget(String lcasefn) {		
		return lcasefn.endsWith(Constants.FILE_EXTENSION_DITA)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_DITAMAP)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_XML)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_JPG)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_GIF)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_EPS)
				|| lcasefn.endsWith(Constants.FILE_EXTENSION_HTML);
	}

	/**
	 * Get the path of topicFile relative to the input map.
	 * 
	 * @param mapFilePathName
	 * @param topicFilePathName
	 * @return
	 */
	public static String getRelativePathFromMap(String mapFilePathName,
			String topicFilePathName) {
		StringBuffer upPathBuffer = new StringBuffer(Constants.INT_128);
		StringBuffer downPathBuffer = new StringBuffer(Constants.INT_128);

		StringTokenizer mapTokenizer = new StringTokenizer(mapFilePathName,
				Constants.SLASH);
		StringTokenizer topicTokenizer = new StringTokenizer(topicFilePathName,
				Constants.SLASH);

		while (mapTokenizer.countTokens() > 1
				&& topicTokenizer.countTokens() > 1) {
			String mapToken = mapTokenizer.nextToken();
			String topicToken = topicTokenizer.nextToken();

			if (!(mapToken.equals(topicToken))) {
				upPathBuffer.append("..");
				upPathBuffer.append(Constants.SLASH);
				downPathBuffer.append(topicToken);
				downPathBuffer.append(Constants.SLASH);
				break;
			}
		}

		while (mapTokenizer.countTokens() > 1) {
			mapTokenizer.nextToken();

			upPathBuffer.append("..");
			upPathBuffer.append(Constants.SLASH);
		}

		while (topicTokenizer.hasMoreTokens()) {
			downPathBuffer.append(topicTokenizer.nextToken());
			if (topicTokenizer.hasMoreTokens()) {
				downPathBuffer.append(Constants.SLASH);
			}
		}

		return upPathBuffer.append(downPathBuffer).toString();
	}
	
	/**
	 * Get path2Project from the relative path of a file
	 */
	public static String getPathtoProject (String relativePath){
		StringTokenizer tokenizer = new StringTokenizer(relativePath, Constants.SLASH);
		StringBuffer buffer = new StringBuffer();
		if (tokenizer.countTokens() == 1){
			return null;
		}else{
			while(tokenizer.countTokens() > 1){
				tokenizer.nextToken();
				buffer.append("..");
				buffer.append(Constants.SLASH);
			}
			return buffer.toString();
		}
	}

	/**
	 * Removing redundant names ".." from the given path.
	 * 
	 * @param path
	 * @return
	 */
	public static String removeRedundantNames(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, File.separator);
        StringBuffer buff = new StringBuffer(path.length());
        List dirNameList = new LinkedList();
        Iterator iter = null;
        int dirNameNum = 0;
        int i = 0;
    
        while (tokenizer.hasMoreTokens()) {
            dirNameList.add(tokenizer.nextToken());
        }
    
        dirNameNum = dirNameList.size();
        while (i < dirNameNum) {
        	if (i > 0) {
        	  String lastDirName = (String) dirNameList.get(i-1);
        	  String dirName = (String) dirNameList.get(i);
        	  if ("..".equals(dirName) && !("..".equals(lastDirName))) {
        		  // remove ".." and the dir name before
                  dirNameList.remove(i);
                  dirNameList.remove(i - 1);
                  dirNameNum = dirNameList.size();
                  i = i - 1;
                  continue;
        	  }
        	}
        	
        	i++;
        }
    
        iter = dirNameList.iterator();
        while (iter.hasNext()) {
            buff.append(iter.next());
            if (iter.hasNext()) {
                buff.append(File.separator);
            }
        }
    
        return buff.toString();
    }

    /** 
     * @param path
     * @return
     */
    public static boolean isAbsolutePath (String path) {
    	if (path == null || Constants.STRING_EMPTY.equals(path.trim())) {
    		return false;
    	}
    	
        if (Constants.FILE_SEPARATOR.equals ("/")) {
            return path.startsWith ("/");
        }
        
        if (Constants.FILE_SEPARATOR.equals ("\\") && path.length() > 2) {         
        	return path.matches("[a-zA-Z]:\\\\.*");
        }

        return false;
    }

}
