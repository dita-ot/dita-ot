/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * String relevant utilities.

 * @author Wu, Zhi Qiang
 */
public class StringUtils {
	
	private static String notResolveEntityList= new String("-lt--gt--quot--amp-");
	
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
        if ((coll == null) || coll.isEmpty()) {
            return "";
        }

        StringBuffer buff = new StringBuffer(200);

        Iterator iter = coll.iterator();
        while (iter.hasNext()) {
            buff.append(iter.next());

            if (iter.hasNext()) {
                buff.append(delim);
            }
        }

        return buff.toString();
    }

    /**
     * @param rootPath
     * @param relativePath
     * @return
     */
    public static String resolveDirectory(String rootPath, String relativePath) {
    	if(relativePath.indexOf('#') != -1){
    		//ignore the part after '#'
    		relativePath = relativePath.substring(0,relativePath.indexOf('#'));
    	}
        relativePath = relativePath.replace('\\', File.separatorChar);
        relativePath = relativePath.replace('/', File.separatorChar);

        if (rootPath == null) {
            return relativePath;
        }

        String prefix = null;
        if (rootPath.charAt(rootPath.length() - 1) == File.separatorChar) {
            prefix = rootPath.substring(0, rootPath.length() - 1);
        } else {
            prefix = rootPath;
        }

        String postfix = relativePath;
        if (relativePath.startsWith("..")) {
            int sepPos = relativePath.indexOf(File.separatorChar);
            int lastPos = prefix.lastIndexOf(File.separatorChar);
            postfix = relativePath.substring(sepPos == -1 ? 0 : sepPos
                    + File.separator.length(), relativePath.length());
            if(lastPos == -1){
                return postfix;
            }
            prefix = prefix.substring(0, lastPos);
        }

        return prefix + File.separatorChar + postfix;
    }

    /**
     * @param name
     * @return
     */
    public static String getEntity(String name) {
        if (name.startsWith("%")) {
            return (name + ";");
        } else {
            return ("&" + name + ";");
        }
    }
    
    /**
     * @param name
     * @return
     */
    public static boolean checkEntity(String name){
    	//check whether this entity need resolve
    	if(notResolveEntityList.indexOf("-"+name.trim()+"-") != -1){
    		return false;
    	}else{
    		return true;
    	}
    }
}
