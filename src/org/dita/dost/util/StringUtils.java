/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * String relevant utilities.

 * @author Wu, Zhi Qiang
 */
public class StringUtils {
	
	private static final String NOT_RESOLVE_ENTITY_LIST= "|lt|gt|quot|amp|";
	
    private StringUtils() {
    }

    /**
     * Assemble all elements in collection to a string.
     * 
     * @param coll -
     *            java.util.List
     * @param delim -
     *            Description of the Parameter
     * @return java.lang.String
     */
    public static String assembleString(Collection coll, String delim) {
    	StringBuffer buff = new StringBuffer(Constants.INT_256);
    	Iterator iter = null;
    	
    	if ((coll == null) || coll.isEmpty()) {
            return "";
        }        

        iter = coll.iterator();
        while (iter.hasNext()) {
            buff.append(iter.next());

            if (iter.hasNext()) {
                buff.append(delim);
            }
        }

        return buff.toString();
    }

    /**
     * Resolve topic.
     * 
     * @param rootPath
     * @param relativePath
     * @return
     */
    public static String resolveTopic(String rootPath, String relativePath) {        
        String begin = relativePath;
        String end = "";
        String prefix = null;
        String postfix = null;
        
        if (relativePath.indexOf("#") != -1) {
            begin = relativePath.substring(0,relativePath.indexOf('#'));
            end = relativePath.substring(relativePath.indexOf('#'));
        }
                
        begin = begin.replace('\\', File.separatorChar);
        begin = begin.replace('/', File.separatorChar);

        relativePath = begin + end;
        
        if (rootPath == null) {
            return relativePath;
        }
        
        prefix = (rootPath.charAt(rootPath.length() - 1) == File.separatorChar) ? rootPath.substring(0, rootPath.length() - 1) : rootPath;

        postfix = relativePath;
        while (postfix.startsWith("..")) {
            int sepPos = postfix.indexOf(File.separatorChar);
            int lastPos = prefix.lastIndexOf(File.separatorChar);
            postfix = postfix.substring(sepPos == -1 ? 0 : sepPos
                    + File.separator.length(), postfix.length());
            if(lastPos == -1){
                return postfix;
            }
            prefix = prefix.substring(0, lastPos);
        }

        return prefix + File.separatorChar + postfix;
    }
    
    /**
     * Get entity.
     * 
     * @param name
     * @return
     */
    public static String getEntity(String name) {
        return (name.startsWith("%")) ? (name + ";") : ("&" + name + ";");
    }
    
    /**
     * Check entity.
     * 
     * @param name
     * @return
     */
    public static boolean checkEntity(String name){
    	//check whether this entity need resolve
    	if(NOT_RESOLVE_ENTITY_LIST.indexOf(Constants.STICK+name.trim()+Constants.STICK) != -1){
    		return false;
    	}
    	return true;
    	
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
            String dirName = (String) dirNameList.get(i);
            if ("..".equals(dirName) && i > 0) {
                // remove ".." and the dir name before
                dirNameList.remove(i);
                dirNameList.remove(i - 1);
                dirNameNum = dirNameList.size();
                i = i - 1;
            } else {
                i++;
            }
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
     * @param fileName
     * @return
     */
    public static boolean isValidTarget(String fileName) {
        String lcasefn = fileName.toLowerCase();
    
        return lcasefn.endsWith(Constants.FILE_EXTENSION_DITA)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_DITAMAP)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_XML)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_JPG)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_GIF)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_EPS)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_HTML);
    }

    /**
     * @param fileName
     * @return
     */
    public static boolean isTopicFile(String fileName) {
        String lcasefn = fileName.toLowerCase();
    
        return lcasefn.endsWith(Constants.FILE_EXTENSION_DITA)
                || lcasefn.endsWith(Constants.FILE_EXTENSION_XML);
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
}
