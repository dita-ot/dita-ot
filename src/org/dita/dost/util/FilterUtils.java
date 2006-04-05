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
	public static boolean needExclude(Attributes atts) {
		if (filterMap == null) {
			return false;
		}

		return checkExclude(Constants.ELEMENT_NAME_AUDIENCE, atts)
				|| checkExclude(Constants.ELEMENT_NAME_PLATFORM, atts)
				|| checkExclude(Constants.ELEMENT_NAME_PRODUCT, atts)
				|| checkExclude(Constants.ELEMENT_NAME_OTHERPROPS, atts);
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
	private static boolean checkExclude(String attName, Attributes atts) {
		String attValue = atts.getValue(attName);

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
