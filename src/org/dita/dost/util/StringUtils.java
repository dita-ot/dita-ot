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

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import org.dita.dost.log.DITAOTJavaLogger;

/**
 * String relevant utilities.
 * 
 * @author Wu, Zhi Qiang
 */
public class StringUtils {

	//Edited by william on 2009-11-8 for ampbug:2893664 start
	private static final String NOT_RESOLVE_ENTITY_LIST = "|lt|gt|quot|amp|";
	private static final String NOT_RESOLVE_ENTITY_CHAR = "|#38|";
	//Edited by william on 2009-11-8 for ampbug:2893664 end

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
	@SuppressWarnings("rawtypes")
    public static String assembleString(Collection coll, String delim) {
		StringBuffer buff = new StringBuffer(Constants.INT_256);
		Iterator iter = null;

		if ((coll == null) || coll.isEmpty()) {
			return "";
		}

		iter = coll.iterator();
		while (iter.hasNext()) {
			buff.append(iter.next().toString());

			if (iter.hasNext()) {
				buff.append(delim);
			}
		}

		return buff.toString();
	}
	
	/**
	 * Escape XML characters.
	 * Suggested by hussein_shafie
	 * @param s value needed to be escaped
	 * @return escaped value
	 */
	public static String escapeXML(String s){
		char[] chars = s.toCharArray();
        return escapeXML(chars, 0, chars.length);
	}
	
	/**
	 * Escape XML characters.
	 * Suggested by hussein_shafie
	 * @param chars char arrays
	 * @param offset start position
	 * @param length arrays lenth
	 * @return escaped value
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
	 * @param name entity name
	 * @return entity
	 */
	public static String getEntity(String name) {
	
		return (name.startsWith("%")) ? (name + ";") : ("&" + name + ";");
	}

	/**
	 * Check entity.
	 * 
	 * @param name entity name
	 * @return ture if this entity needs to be resolved
	 */
	public static boolean checkEntity(String name) {
		// check whether this entity need resolve
		if (NOT_RESOLVE_ENTITY_LIST.indexOf(Constants.STICK + name.trim()
				+ Constants.STICK) != -1 ||
			//Edited by william on 2009-11-8 for ampbug:2893664 start
			NOT_RESOLVE_ENTITY_CHAR.indexOf(Constants.STICK + name.trim()
						+ Constants.STICK) != -1 ) {
			//Edited by william on 2009-11-8 for ampbug:2893664 end
			return false;
		}
		return true;
		
	}

	/**
	 * Replaces each substring of this string that matches the given string 
	 * with the given replacement. Differ from the JDK String.replaceAll function,
	 * this method does not support regular expression based replacement on purpose.
	 * 
	 * @param input input string
	 * @param pattern This pattern is recognized as it is. It will not solve
	 *        as an regular expression.
	 * @param replacement string used to replace with
	 * @return replaced string
	 * 
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
	 * Get ASCII code of a string.
	 * @param inStr input string
	 * @return asscii code
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
	 * Get the props.
	 * @param domains input domain
	 * @return prop
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
	 * Restore map.
	 * @param s input string
	 * @return map created from string
	 */
	public static Map<String, String> restoreMap(String s) {
		Map<String,String> copytoMap = new HashMap<String,String>();
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
	 * @param s input string
	 * @return string set
	 */
	public static Set<String> restoreSet(String s) {
		return restoreSet(s, Constants.COMMA);
	}
	
	/**
	 * Break down a string separated by <code>delim</code> into a string set. 
	 * @param s String to be splitted
	 * @param delim Delimiter to be used.
	 * @return string set
	 */
	public static Set<String> restoreSet(String s, String delim) {
		Set<String> copytoSet = new HashSet<String>();
		
		if (StringUtils.isEmptyString(s)) {
			return copytoSet;
		}
		
		StringTokenizer st = new StringTokenizer(s, delim);
		
		while (st.hasMoreTokens()) {
			String entry = st.nextToken();
			if (!StringUtils.isEmptyString(entry)) {
				copytoSet.add(entry);
			}
		}
		return copytoSet;
	}
	
	/**
	 * Return is the string is null or "".
	 * @param s input string
	 * @return true if the string is null or ""
	 */
	public static boolean isEmptyString(String s){
		return (s == null || Constants.STRING_EMPTY.equals(s.trim()));
	}
	
	/**
	 * If target is null, return the value; else append value to target. 
	 * If withSpace is true, insert a blank between them.
	 * @param target target to be appended
	 * @param value value to append
	 * @param withSpace whether insert a blank
	 * @return processed string
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
	 * Init sax driver info.
	 * 
	 * @deprecated use {@link #getXMLReader} instead to get the preferred SAX parser
	 */
	@Deprecated
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
     * Get preferred SAX parser.
     * 
     * Preferred XML readers are in order:
     * 
     * <ol>
     *   <li>{@link Constants.SAX_DRIVER_DEFAULT_CLASS}</li>
     *   <li>{@link Constants.SAX_DRIVER_SUN_HACK_CLASS}</li>
     *   <li>{@link Constants.SAX_DRIVER_CRIMSON_CLASS}</li>
     * </ol>
     * 
     * @return XML parser instance.
	 * @throws SAXException if instantiating XMLReader failed
     */
    public static XMLReader getXMLReader() throws SAXException {
        if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) != null) {
            return XMLReaderFactory.createXMLReader();
        }
        try {
            Class.forName(Constants.SAX_DRIVER_DEFAULT_CLASS);
            return XMLReaderFactory.createXMLReader(Constants.SAX_DRIVER_DEFAULT_CLASS);
        } catch (ClassNotFoundException e) {
            try {
                Class.forName(Constants.SAX_DRIVER_SUN_HACK_CLASS);
                return XMLReaderFactory.createXMLReader(Constants.SAX_DRIVER_SUN_HACK_CLASS);
            } catch (ClassNotFoundException ex) {
                try {
                    Class.forName(Constants.SAX_DRIVER_CRIMSON_CLASS);
                    return XMLReaderFactory.createXMLReader(Constants.SAX_DRIVER_CRIMSON_CLASS);
                } catch (ClassNotFoundException exc){
                    return XMLReaderFactory.createXMLReader();
                }
            }
        }
    }

		/**
		 * Return a Java Locale object.
		 * @param anEncoding encoding
		 * @return locale
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
				}else if (underscoreIndex == 2 || underscoreIndex == 3){
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
		
		//added by William on 2009-11-26 for bug:1628937 start
		/**
		 * Get file's main name.
		 * @param input input filename
		 * @param marker delimiter
		 * @return file's main name 
		 */
		public static String getFileName(String input, String marker){
			int index = input.lastIndexOf(marker);
			if(index != -1){
				return input.substring(0, index);
			}else{
				return input;
			}
		}
		//added by William on 2009-11-26 for bug:1628937 end
		
		/**
		 * Get max value.
		 */
		public static Integer getMax(String ul_depth, String ol_depth, String sl_depth, 
				String dl_depth, String table_depth, String stable_depth){
			
			int unDepth = Integer.parseInt(ul_depth);
			int olDepth = Integer.parseInt(ol_depth);
			int slDepth = Integer.parseInt(sl_depth);
			int dlDepth = Integer.parseInt(dl_depth);
			int tableDepth = Integer.parseInt(table_depth);
			int stableDepth = Integer.parseInt(stable_depth);
			
			int max = unDepth;
			if(olDepth > max){
				max = olDepth;
			}
			if(slDepth > max){
				max = slDepth;
			}
			if(dlDepth > max){
				max = dlDepth;
			}
			if(tableDepth > max){
				max = tableDepth;
			}
			if(stableDepth > max){
				max = stableDepth;
			}
			
			return max;
			
		}
		
		/**
		 * Get max value.
		 */
		public static Integer getMax(String fn_depth, String list_depth, String dlist_depth, String table_depth, String stable_depth){
			
			int fnDepth = Integer.parseInt(fn_depth);
			int listDepth = Integer.parseInt(list_depth);
			int dlistDepth = Integer.parseInt(dlist_depth);
			int tableDepth = Integer.parseInt(table_depth);
			int stableDepth = Integer.parseInt(stable_depth);
			
			int max = fnDepth;
			if(listDepth > max){
				max = listDepth;
			}
			if(dlistDepth > max){
				max = dlistDepth;
			}
			if(tableDepth > max){
				max = tableDepth;
			}
			if(stableDepth > max){
				max = stableDepth;
			}
			
			return max;
			
		}
}