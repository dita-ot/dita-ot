/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;

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
	 * Break down a string separated by commas into a string set. 
	 * @param s
	 * @return
	 */
	public static Set<String> restoreSet(String s) {
		Set<String> copytoSet = new HashSet<String>();
		
		if (StringUtils.isEmptyString(s)) {
			return copytoSet;
		}
		
		StringTokenizer st = new StringTokenizer(s, Constants.COMMA);
		
		while (st.hasMoreTokens()) {
			String entry = st.nextToken();
			if (!StringUtils.isEmptyString(entry)) {
				copytoSet.add(entry);
			}
		}
		return copytoSet;
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
		}if(value == null){
			return target;
		}else{
			if(withSpace && !target.endsWith(Constants.STRING_BLANK)){
				return target + Constants.STRING_BLANK + value;
			}else{
				return target + value;
			}
		}
	}
	
	/**
	 * Init sax driver info
	 */
	public static void initSaxDriver(){
		//The default sax driver is set to xerces's sax driver
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		try {
			Class.forName(Constants.SAX_DRIVER_DEFAULT_CLASS);
			System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_DEFAULT_CLASS);
			logger.logInfo("Using XERCES.");
		} catch (ClassNotFoundException e){
			try{
				Class.forName(Constants.SAX_DRIVER_SUN_HACK_CLASS);
				System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_SUN_HACK_CLASS);
				logger.logInfo("Using XERCES in SUN JDK 1.5");
			}catch (ClassNotFoundException ex){
				try {
					Class.forName(Constants.SAX_DRIVER_CRIMSON_CLASS);
					System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_CRIMSON_CLASS);
					logger.logInfo("Using CRIMSON");
				}catch (ClassNotFoundException exc){
					logger.logException(e);
					logger.logException(ex);
					logger.logException(exc);
				}
			}
		}
		
	}
	

		/**
		 * Return a Java Locale object
		 * @param anEncoding
		 * @return
		 */
	
		public static Locale getLocale(String anEncoding){
			Locale aLocale = null;
			String country = null;
			String language = null;
			String variant = null;
			
			//Tokenize the string using "-" as the token string as per IETF RFC4646 (superceeds RFC3066).
			
			StringTokenizer tokenizer = new StringTokenizer(anEncoding, "-");
			
			//We need to know how many tokens we have so we can create a Locale object with the proper constructor.
			int numberOfTokens = tokenizer.countTokens();
			
			if (numberOfTokens == 1) { 
				String tempString = tokenizer.nextToken().toLowerCase();
				
				//Note: Newer XML parsers should throw an error if the xml:lang value contains 
				//underscore. But this is not guaranteed.
				
				//Check to see if some one used "en_US" instead of "en-US".  
				//If so, the first token will contain "en_US" or "xxx_YYYYYYYY". In this case,
				//we will only grab the value for xxx. 
				int underscoreIndex = tempString.indexOf("_");
				
				if (underscoreIndex == -1){
					language = tempString;
				}else if (underscoreIndex == 2 | underscoreIndex == 3){
					//check is first subtag is two or three characters in length.
					language = tempString.substring(0, underscoreIndex);
				}
				
				aLocale = new Locale(language);
			} else if (numberOfTokens == 2) {
				
				language = tokenizer.nextToken().toLowerCase();
				
				String subtag2 = tokenizer.nextToken();
				//All country tags should be three characters or less.  
				//If the subtag is longer than three characters, it assumes that 
				//is a dialect or variant. 
				if (subtag2.length() <= 3){
					country = subtag2.toUpperCase();
					aLocale = new Locale(language, country);
				}else if (subtag2.length() > 3 && subtag2.length() <= 8){
					variant = subtag2;
					aLocale = new Locale(language, "", variant);
				}else if (subtag2.length() > 8){
					//return an error!
				}
				
				
				
			} else if (numberOfTokens >= 3) {
				
				language = tokenizer.nextToken().toLowerCase();
				String subtag2 = tokenizer.nextToken();
				if (subtag2.length() <= 3){
					country = subtag2.toUpperCase();
				}else if (subtag2.length() > 3 && subtag2.length() <= 8){
					variant = subtag2;
				}else if (subtag2.length() > 8){
					//return an error!
				}
				variant = tokenizer.nextToken();
				
				aLocale = new Locale(language, country, variant);
				
			}else {
			  //return an warning or do nothing.  
			  //The xml:lang attribute is empty.
				aLocale = new Locale(Constants.LANGUAGE_EN,
						Constants.COUNTRY_US);
				
			}
	
			return aLocale; 
		 }
		
}