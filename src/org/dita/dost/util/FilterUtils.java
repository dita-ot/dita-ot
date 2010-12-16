/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.xml.sax.Attributes;

/**
 * Utility class used for flagging and filtering.
 * 
 * @author Wu, Zhi Qiang
 */
public class FilterUtils {
	private static HashMap<String, String> filterMap = null;
	private static HashSet<String> notMappingRules=new HashSet<String>();
	/**
	 * Default Constructor
	 *
	 */
	private FilterUtils(){
	}
	/**
	 * Set the filter map.
	 * @param filtermap The filterMap to set.
	 */
	public static void setFilterMap(HashMap<String, String> filtermap) {
		FilterUtils.filterMap = filtermap;
	}
	
	/**
	 * Getter for filter map.
	 * @return filter map
	 */
	public static HashMap<String, String> getFilterMap() {
		return filterMap;
	}

	/**
	 * Check if the given Attributes need to be excluded.
	 * @param atts attributes
	 * @param extProps props a(props...)
	 * @return true if any one of attributes 'audience', 'platform', 
	 * 'product', 'otherprops' was excluded.
	 */
	public static boolean needExclude(Attributes atts, String extProps) {
		boolean ret = false;
		boolean extRet = false;
		StringTokenizer prop = null;
		StringTokenizer propPathTokenizer = null;
		String propName = null;
		String propValue = null;
		String propPath = null;
		List<String> propList = null;
		int propListIndex = 0;
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
				atts.getValue(Constants.ELEMENT_NAME_OTHERPROPS))
			//Added by William on 2010-07-16 for bug:3030317 start	
			|| checkExclude(Constants.ELEMENT_NAME_PROPS, 
				atts.getValue(Constants.ELEMENT_NAME_PROPS));
			//Added by William on 2010-07-16 for bug:3030317 end
		
		if(extProps == null){
			return ret;
		}
		
		prop = new StringTokenizer(extProps, Constants.COMMA);
		
		while(prop.hasMoreElements()){
			propPath = (String)prop.nextElement();
			propPathTokenizer = new StringTokenizer(propPath, Constants.STRING_BLANK);
			propList = new ArrayList<String>(Constants.INT_128);
			while(propPathTokenizer.hasMoreElements()){
				propList.add((String)propPathTokenizer.nextElement());
			}
			propListIndex = propList.size()-1;
			propName = (String)propList.get(propListIndex);
			propValue = atts.getValue(propName);
			
			while (propValue == null && propListIndex > 0){
				propListIndex--;
				attrPropsValue = atts.getValue((String)propList.get(propListIndex));
				if (attrPropsValue != null){
					propStart=-1;
					if(attrPropsValue.startsWith(new StringBuffer(propName).append("(").toString(), 0)==true
							|| attrPropsValue.indexOf(new StringBuffer(Constants.STRING_BLANK+propName).append("(").toString(), 0)!=-1){
						propStart = attrPropsValue.indexOf(new StringBuffer(propName).append("(").toString());
					}
					if(propStart!=-1)
						propStart=propStart+ propName.length() + 1;
					propEnd = attrPropsValue.indexOf(")", propStart);
					if (propStart != -1 && propEnd != -1){
						propValue = attrPropsValue.substring(propStart, propEnd).trim();
					}
				}
			}
			extRet = extRet || extCheckExclude(propList, propValue);
		}
		return ret || extRet;
	}

	
	/**
	 * Check the given extended attribute in propList to see if it was excluded.
	 * 
	 * 
	 * @param propList
	 * @param attValue
	 * @return
	 */
	private static boolean extCheckExclude(List<String> propList, String attValue){
		int propListIndex = 0;
		boolean hasNullAction = false;
		boolean hasExcludeAction = false;
		StringTokenizer tokenizer = null;
		String attName = null;
		//to check if the value is just only "" or " ",ignore it
		if (attValue == null || attValue.trim().length()==0 || propList.size() == 0 || attValue.indexOf("(")!=-1) {
			return false;
		}

		propListIndex = propList.size() - 1;
		checkRuleMapping((String)propList.get(propListIndex),attValue);
		while (propListIndex >= 0){			
			hasNullAction = false;
			hasExcludeAction = false;			
			tokenizer = new StringTokenizer(attValue,
					Constants.STRING_BLANK);

			attName = (String)propList.get(propListIndex);
			while (tokenizer.hasMoreTokens()) {
				String attSubValue = tokenizer.nextToken();
				String filterKey = new StringBuffer().append(attName).append(
						Constants.EQUAL).append(attSubValue).toString();
				String filterAction = (String) filterMap.get(filterKey);
				// no action will be considered as 'not exclude'
				if (filterAction == null) {
					//check Specified DefaultAction mapping this attribute's name
					String attDefaultAction=(String) filterMap.get(attName);
					if(attDefaultAction!=null){
						//filterAction=attDefaultAction;
						if(!Constants.FILTER_ACTION_EXCLUDE.equalsIgnoreCase(attDefaultAction)){
							return false;
						}else{
							hasExcludeAction=true;
							if(hasNullAction==true){
								if (checkExcludeOfGlobalDefaultAction()==true)
									hasNullAction=false;
								else 
									return false;
							}
						}
					}else{
						if(hasExcludeAction==true){
							if (checkExcludeOfGlobalDefaultAction()==false)
								return false;
						}else
							hasNullAction=true;
					}
				}else if (Constants.FILTER_ACTION_EXCLUDE.equalsIgnoreCase(filterAction)) {
					hasExcludeAction = true;
					if(hasNullAction==true){
						if (checkExcludeOfGlobalDefaultAction()==true)
							hasNullAction=false;
						else 
							return false;
					}
				}else{
					return false;
				}
			}
			
			if(hasNullAction){
				//if there is exclude action but not all value should be excluded 
				//under the condition of default action also not exist or not excluded
				if(0==propListIndex){
					//the ancient parent on the top level 
					return checkExcludeOfGlobalDefaultAction();
				}
			}else if(hasExcludeAction){
				//if all of the value should be excluded
				return true;
			}
			//If no action for this extended prop has been found, we need to check the 
			//parent prop action
				
			propListIndex --;			
		}
		
		return false;
	}
	

    
	
	
	/**
	 * Check the given attName to see if it was excluded.
	 * 
	 * Note: attName is case sensitive, action is case insensitive
	 * 
	 * @param attName
	 * @param attValue
	 * @return
	 */
	private static boolean checkExclude(String attName, String attValue) {
		StringTokenizer tokenizer;
		
		//for the special value :"" or " ",just ignore it
		if (attValue == null || attValue.trim().length()==0) {
			return false;
		}
		checkRuleMapping(attName,attValue);
		/*
		 * attValue may has several values, so we need to check them separately
		 * 1. if one of those values was not set to 'exclude', then don't
		 * exclude; 2. only if all of those values were set to 'exclude', it can
		 * be exclude.
		 */
		tokenizer = new StringTokenizer(attValue,
				Constants.STRING_BLANK);
		while (tokenizer.hasMoreTokens()) {
			String attSubValue = tokenizer.nextToken();
			String filterKey = new StringBuffer().append(attName).append(
					Constants.EQUAL).append(attSubValue).toString();
			String filterAction = (String) filterMap.get(filterKey);

			//not mapping ,no action will be considered as default action,
			//if default action does not exists ,considered as "not exclude"
			if (filterAction == null) {
				//check Specified DefaultAction mapping this attribute's name
				String attDefaultAction=(String) filterMap.get(attName);
				if(attDefaultAction!=null){
					filterAction=attDefaultAction;
					if(!Constants.FILTER_ACTION_EXCLUDE.equalsIgnoreCase(attDefaultAction)){
						return false;
					}	
				}else{
					if(checkExcludeOfGlobalDefaultAction()==false)
						return false;
				}
			}
			// action is case insensitive
			else if (!(Constants.FILTER_ACTION_EXCLUDE
					.equalsIgnoreCase(filterAction))) {
					return false;
				}
		}

		return true;
	}
	private static boolean checkExcludeOfGlobalDefaultAction(){
		String defaultAction=(String) filterMap.get(Constants.DEFAULT_ACTION);
		if(defaultAction==null)
			return false;
		else {
			if(!Constants.FILTER_ACTION_EXCLUDE.equalsIgnoreCase(defaultAction))
				return false;	
			else 
				return true;
		}
	}
	private static void checkRuleMapping(String attName, String attValue){
		StringTokenizer tokenizer;
		if (attValue == null || attValue.trim().length()==0 ) {
			return ;
		}
		tokenizer = new StringTokenizer(attValue,
				Constants.STRING_BLANK);
		while (tokenizer.hasMoreTokens()) {
			String attSubValue = tokenizer.nextToken();
			String filterKey = new StringBuffer().append(attName).append(
					Constants.EQUAL).append(attSubValue).toString();
			String filterAction = (String) filterMap.get(filterKey);
			if(filterAction==null){
				noRuleMapping(filterKey);
			}
		}
	}
	private static void noRuleMapping(String notMappingKey){
		if(!alreadyShowed(notMappingKey)){
			showInfoOfNoRuleMapping(notMappingKey);
		}
	}
	private static void showInfoOfNoRuleMapping(String notMappingKey){
		Properties prop=new Properties();
		prop.put("%1", notMappingKey);
		DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();
		javaLogger.logInfo(MessageUtils.getMessage("DOTJ031I", prop).toString());
	}
	private static boolean alreadyShowed(String notMappingKey){
		if(!notMappingRules.contains(notMappingKey)){
			notMappingRules.add(notMappingKey);
			return false;
		}
		return true;
	}
}
