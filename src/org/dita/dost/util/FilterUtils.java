/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.HashMap;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

/**
 * Utility class used for flagging and filtering.
 * 
 * @author Wu, Zhi Qiang
 */
public class FilterUtils {
	private static HashMap filterMap = null;

	/**
	 * @param filterMap
	 *            The filterMap to set.
	 */
	public static void setFilterMap(HashMap filterMap) {
		FilterUtils.filterMap = filterMap;
	}

	/**
	 * Check if the given Attributes need to be excluded.
	 * 
	 * @param atts
	 * @return true if any one of attributes 'audience', 'platform', 
	 * 'product', 'otherprops' was excluded, 
	 */
	public static boolean needExclude(Attributes atts, String extProps) {
		boolean ret = false;
		boolean extRet = false;
		StringTokenizer prop = null;
		String propName = null;
		String propValue = null;
		String attrPropsValue = null;
		int propStart;
		int propEnd;
		if (filterMap == null) {
			return false;
		}

		ret = checkExclude(Constants.ELEMENT_NAME_AUDIENCE, 
					atts.getValue(Constants.ELEMENT_NAME_AUDIENCE))
				|| checkExclude(Constants.ELEMENT_NAME_PLATFORM, 
					atts.getValue(Constants.ELEMENT_NAME_PLATFORM))
				|| checkExclude(Constants.ELEMENT_NAME_PRODUCT, 
					atts.getValue(Constants.ELEMENT_NAME_PRODUCT))
				|| checkExclude(Constants.ELEMENT_NAME_OTHERPROPS, 
						atts.getValue(Constants.ELEMENT_NAME_OTHERPROPS));
		
		if(extProps == null){
			return ret;
		}
		
		prop = new StringTokenizer(extProps, Constants.STRING_BLANK);
		
		while(prop.hasMoreElements()){
			propName = (String)prop.nextElement();
			propValue = atts.getValue(propName);
			
			if(propValue == null){
				attrPropsValue = atts.getValue(Constants.ATTRIBUTE_NAME_PROPS);
				if (attrPropsValue != null){
					propStart = attrPropsValue.indexOf(propName+"(") + propName.length() + 1;
					propEnd = attrPropsValue.indexOf(")", propStart);
					if (propStart != -1 && propEnd != -1){
						propValue = attrPropsValue.substring(propStart, propEnd).trim();
					}
				}
			}
			
			extRet = extRet || checkExclude(propName, propValue);
		}
		return ret || extRet;
	}

	/**
	 * Check the given attName to see if it was excluded.
	 * 
	 * Note: attName is case sensitive, action is case insensitive
	 * 
	 * @param attName
	 * @param atts
	 * @return
	 */
	private static boolean checkExclude(String attName, String attValue) {
		
		if (attValue == null) {
			return false;
		}

		/*
		 * attValue may has several values, so we need to check them separately
		 * 1. if one of those values was not set to 'exclude', then don't
		 * exclude; 2. only if all of those values were set to 'exluce', it can
		 * be exclude.
		 */
		StringTokenizer tokenizer = new StringTokenizer(attValue,
				Constants.STRING_BLANK);
		while (tokenizer.hasMoreTokens()) {
			String attSubValue = tokenizer.nextToken();
			String filterKey = new StringBuffer().append(attName).append(
					Constants.EQUAL).append(attSubValue).toString();
			String filterAction = (String) filterMap.get(filterKey);

			// no action will be considered as 'not exclude'
			if (filterAction == null) {
				return false;
			}

			// action is case insensitive
			if (!(Constants.FILTER_ACTION_EXCLUDE
					.equalsIgnoreCase(filterAction))) {
				return false;
			}
		}

		return true;
	}

}
