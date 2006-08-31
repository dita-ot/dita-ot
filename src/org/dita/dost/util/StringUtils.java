/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

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
	 * Escape XML characters
	 * Suggested by hussein_shafie
	 * @param s
	 * @return
	 */
	public static String escapeXML(String s){
		char[] chars = s.toCharArray();
        return escapeXML(chars, 0, chars.length);
	}
	
	/**
	 * Escape XML characters
	 * Suggested by hussein_shafie
	 * @param chars
	 * @param offset
	 * @param length
	 * @return
	 */
	public static String escapeXML(char[] chars, int offset, int length){
		StringBuffer escaped = new StringBuffer();

        int end = offset + length;
        for (int i = offset; i < end; ++i) {
            char c = chars[i];

            switch (c) {
            case '\'':
                escaped.append("&apos;");
                break;
            case '\"':
                escaped.append("&quot;");
                break;
            case '<':
                escaped.append("&lt;");
                break;
            case '>':
                escaped.append("&gt;");
                break;
            case '&':
                escaped.append("&amp;");
                break;
            default:
                escaped.append(c);
            }
        }

        return escaped.toString();
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
	 * Replace all the pattern with replacement
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
	
	/**
	 * Get ASCII code of a string
	 * @param inStr
	 * @return
	 */
	public static String getAscii(String inStr){
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
		
	}
	
	/**
	 * Get the props
	 * @param domains
	 * @return
	 */
	public static String getExtProps (String domains){
		StringBuffer propsBuffer = new StringBuffer();
    	int propsStart = domains.indexOf("a(props");
    	int propsEnd = domains.indexOf(")",propsStart);
    	while (propsStart != -1 && propsEnd != -1){
    		propsBuffer.append(Constants.COMMA);
    		propsBuffer.append(domains.substring(propsStart+2,propsEnd).trim());
    		propsStart = domains.indexOf("a(props", propsEnd);
    		propsEnd = domains.indexOf(")",propsStart);
    	}
    	return (propsBuffer.length() > 0) ? propsBuffer.substring(Constants.INT_1) : null;
	}
	
	/**
	 * Restore entity
	 * @param s
	 * @return
	 */
	public static String restoreEntity(String s) {
		String localEntity = s;
		localEntity = StringUtils.replaceAll(localEntity, "&", "&amp;");
		localEntity = StringUtils.replaceAll(localEntity, "<", "&lt;");
		localEntity = StringUtils.replaceAll(localEntity, ">", "&gt;");		
		localEntity = StringUtils.replaceAll(localEntity, "'", "&apos;");
		localEntity = StringUtils.replaceAll(localEntity, "\"", "&quot;");
		
		return localEntity;
	}
	
	/**
	 * Restore map
	 * @param s
	 * @return
	 */
	public static Map restoreMap(String s) {
		Map copytoMap = new HashMap();
		StringTokenizer st = new StringTokenizer(s, Constants.COMMA);
		
        while (st.hasMoreTokens()) {
        	String entry = st.nextToken();
        	int index = entry.indexOf('=');
        	copytoMap.put(entry.substring(0, index), entry.substring(index+1));
        }
        
        return copytoMap;
	}
	
	/**
	 * Return is the string is null or ""
	 * @param s
	 * @return
	 */
	public static boolean isEmptyString(String s){
		return (s == null || Constants.STRING_EMPTY.equals(s.trim()));
	}
	
	/**
	 * If target is null, return the value; else append value to target. 
	 * If withSpace is true, insert a blank between them.
	 * @param target
	 * @param value
	 * @param withSpace
	 * @return
	 */
	public static String setOrAppend(String target, String value, boolean withSpace){
		if(target == null){
			return value;
		}else{
			if(withSpace){
				return target + Constants.STRING_BLANK + value;
			}else{
				return target + value;
			}
		}
	}
}