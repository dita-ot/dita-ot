/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

/**
 * String relevant utilities.
 * 
 * @author Wu, Zhi Qiang
 */
public class StringUtils {

	private static final String NOT_RESOLVE_ENTITY_LIST = "|lt|gt|quot|amp|";

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
			begin = relativePath.substring(0, relativePath.indexOf('#'));
			end = relativePath.substring(relativePath.indexOf('#'));
		}

		begin = begin.replace('\\', File.separatorChar);
		begin = begin.replace('/', File.separatorChar);

		relativePath = begin + end;

		if (rootPath == null) {
			return relativePath;
		}

		prefix = (rootPath.charAt(rootPath.length() - 1) == File.separatorChar) ? rootPath
				.substring(0, rootPath.length() - 1)
				: rootPath;

		postfix = relativePath;
		while (postfix.startsWith("..")) {
			int sepPos = postfix.indexOf(File.separatorChar);
			int lastPos = prefix.lastIndexOf(File.separatorChar);
			postfix = postfix.substring(sepPos == -1 ? 0 : sepPos
					+ File.separator.length(), postfix.length());
			if (lastPos == -1) {
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
	public static boolean checkEntity(String name) {
		// check whether this entity need resolve
		if (NOT_RESOLVE_ENTITY_LIST.indexOf(Constants.STICK + name.trim()
				+ Constants.STICK) != -1) {
			return false;
		}
		return true;

	}

	/**
	 * @param input
	 * @param pattern
	 * @param replacement
	 * @return
	 */
	public static String replaceAll(final String input,
			final String pattern, final String replacement) {
		StringBuffer result = new StringBuffer();
		int startIndex = 0;
		int newIndex = 0;

		while ((newIndex = input.indexOf(pattern, startIndex)) >= 0) {			
			result.append(input.substring(startIndex, newIndex));			
			result.append(replacement);			
			startIndex = newIndex + pattern.length();
		}
		
		result.append(input.substring(startIndex));
		
		return result.toString();
	}
}
