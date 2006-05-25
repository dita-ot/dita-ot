/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

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
	
	public static String getAscii(String inStr){
		try{
		byte [] input = inStr.getBytes();
		/*byte [] output;
		ByteArrayInputStream byteIS = new ByteArrayInputStream(input);
		InputStreamReader reader = new InputStreamReader(byteIS,"UTF-8");
		char [] cbuf = new char[Constants.INT_128];
		int count = reader.read(cbuf);*/
		StringBuffer ret = new StringBuffer(Constants.INT_1024);
		String strByte = null;
		for(int i = 0; i < input.length; i++){
			ret.append("\\\'");
			strByte = Integer.toHexString(input[i]);
			ret.append(strByte.substring(strByte.length()-2));
			//System.out.println(Integer.toHexString(input[i]));
			//System.out.println(strByte);
		}
		/*while(count > 0){
			output = (new String(cbuf, 0, count)).getBytes();
			for(int j = 0; j < output.length; j++){
				ret.append("\\\'");
				strByte = Integer.toHexString(output[j]);
				ret.append(strByte.substring(strByte.length()-2));
			}
			count = reader.read(cbuf);
		}*/
		
		return ret.toString();
		}catch (Exception e){
			return null;
		}
	}
}
