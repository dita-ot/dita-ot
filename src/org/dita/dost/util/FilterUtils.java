/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.xml.sax.Attributes;

/**
 * Utility class used for flagging and filtering.
 * 
 * @author Wu, Zhi Qiang
 */
public final class FilterUtils {
    private static Map<String, String> filterMap = null;
    private static Set<String> notMappingRules=new HashSet<String>();
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
    public static void setFilterMap(final Map<String, String> filtermap) {
        FilterUtils.filterMap = filtermap;
    }

    /**
     * Getter for filter map.
     * @return filter map
     */
    public static Map<String, String> getFilterMap() {
        return filterMap;
    }

    /**
     * Check if the given Attributes need to be excluded.
     * @param atts attributes
     * @param extProps props a(props...)
     * @return true if any one of attributes 'audience', 'platform',
     * 'product', 'otherprops' was excluded.
     */
    public static boolean needExclude(final Attributes atts, final String extProps) {
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

        ret = checkExclude(TOPIC_AUDIENCE.localName,
                atts.getValue(TOPIC_AUDIENCE.localName))
                || checkExclude(TOPIC_PLATFORM.localName,
                        atts.getValue(TOPIC_PLATFORM.localName))
                        || checkExclude(ELEMENT_NAME_PRODUCT,
                                atts.getValue(ELEMENT_NAME_PRODUCT))
                                || checkExclude(ELEMENT_NAME_OTHERPROPS,
                                        atts.getValue(ELEMENT_NAME_OTHERPROPS))
                                        //Added by William on 2010-07-16 for bug:3030317 start
                                        || checkExclude(ELEMENT_NAME_PROPS,
                                                atts.getValue(ELEMENT_NAME_PROPS));
        //Added by William on 2010-07-16 for bug:3030317 end

        if(extProps == null){
            return ret;
        }

        prop = new StringTokenizer(extProps, COMMA);

        while(prop.hasMoreElements()){
            propPath = (String)prop.nextElement();
            propPathTokenizer = new StringTokenizer(propPath, STRING_BLANK);
            propList = new ArrayList<String>(INT_128);
            while(propPathTokenizer.hasMoreElements()){
                propList.add((String)propPathTokenizer.nextElement());
            }
            propListIndex = propList.size()-1;
            propName = propList.get(propListIndex);
            propValue = atts.getValue(propName);

            while (propValue == null && propListIndex > 0){
                propListIndex--;
                attrPropsValue = atts.getValue(propList.get(propListIndex));
                if (attrPropsValue != null){
                    propStart=-1;
                    if(attrPropsValue.startsWith(new StringBuffer(propName).append("(").toString(), 0)==true
                            || attrPropsValue.indexOf(new StringBuffer(STRING_BLANK+propName).append("(").toString(), 0)!=-1){
                        propStart = attrPropsValue.indexOf(new StringBuffer(propName).append("(").toString());
                    }
                    if(propStart!=-1) {
                        propStart=propStart+ propName.length() + 1;
                    }
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
    private static boolean extCheckExclude(final List<String> propList, final String attValue){
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
        checkRuleMapping(propList.get(propListIndex),attValue);
        while (propListIndex >= 0){
            hasNullAction = false;
            hasExcludeAction = false;
            tokenizer = new StringTokenizer(attValue,
                    STRING_BLANK);

            attName = propList.get(propListIndex);
            while (tokenizer.hasMoreTokens()) {
                final String attSubValue = tokenizer.nextToken();
                final String filterKey = new StringBuffer().append(attName).append(
                        EQUAL).append(attSubValue).toString();
                final String filterAction = filterMap.get(filterKey);
                // no action will be considered as 'not exclude'
                if (filterAction == null) {
                    //check Specified DefaultAction mapping this attribute's name
                    final String attDefaultAction=filterMap.get(attName);
                    if(attDefaultAction!=null){
                        //filterAction=attDefaultAction;
                        if(!FILTER_ACTION_EXCLUDE.equalsIgnoreCase(attDefaultAction)){
                            return false;
                        }else{
                            hasExcludeAction=true;
                            if(hasNullAction==true){
                                if (checkExcludeOfGlobalDefaultAction()==true) {
                                    hasNullAction=false;
                                } else {
                                    return false;
                                }
                            }
                        }
                    }else{
                        if(hasExcludeAction==true){
                            if (checkExcludeOfGlobalDefaultAction()==false) {
                                return false;
                            }
                        } else {
                            hasNullAction=true;
                        }
                    }
                }else if (FILTER_ACTION_EXCLUDE.equalsIgnoreCase(filterAction)) {
                    hasExcludeAction = true;
                    if(hasNullAction==true){
                        if (checkExcludeOfGlobalDefaultAction()==true) {
                            hasNullAction=false;
                        } else {
                            return false;
                        }
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
    private static boolean checkExclude(final String attName, final String attValue) {
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
                STRING_BLANK);
        while (tokenizer.hasMoreTokens()) {
            final String attSubValue = tokenizer.nextToken();
            final String filterKey = new StringBuffer().append(attName).append(
                    EQUAL).append(attSubValue).toString();
            String filterAction = filterMap.get(filterKey);

            //not mapping ,no action will be considered as default action,
            //if default action does not exists ,considered as "not exclude"
            if (filterAction == null) {
                //check Specified DefaultAction mapping this attribute's name
                final String attDefaultAction=filterMap.get(attName);
                if(attDefaultAction!=null){
                    filterAction=attDefaultAction;
                    if(!FILTER_ACTION_EXCLUDE.equalsIgnoreCase(attDefaultAction)){
                        return false;
                    }
                }else{
                    if(checkExcludeOfGlobalDefaultAction()==false) {
                        return false;
                    }
                }
            }
            // action is case insensitive
            else if (!(FILTER_ACTION_EXCLUDE
                    .equalsIgnoreCase(filterAction))) {
                return false;
            }
        }

        return true;
    }
    private static boolean checkExcludeOfGlobalDefaultAction(){
        final String defaultAction=filterMap.get(DEFAULT_ACTION);
        if(defaultAction==null) {
            return false;
        } else {
            if(!FILTER_ACTION_EXCLUDE.equalsIgnoreCase(defaultAction)) {
                return false;
            } else {
                return true;
            }
        }
    }
    private static void checkRuleMapping(final String attName, final String attValue){
        StringTokenizer tokenizer;
        if (attValue == null || attValue.trim().length()==0 ) {
            return ;
        }
        tokenizer = new StringTokenizer(attValue,
                STRING_BLANK);
        while (tokenizer.hasMoreTokens()) {
            final String attSubValue = tokenizer.nextToken();
            final String filterKey = new StringBuffer().append(attName).append(
                    EQUAL).append(attSubValue).toString();
            final String filterAction = filterMap.get(filterKey);
            if(filterAction==null){
                noRuleMapping(filterKey);
            }
        }
    }
    private static void noRuleMapping(final String notMappingKey){
        if(!alreadyShowed(notMappingKey)){
            showInfoOfNoRuleMapping(notMappingKey);
        }
    }
    private static void showInfoOfNoRuleMapping(final String notMappingKey){
        final Properties prop=new Properties();
        prop.put("%1", notMappingKey);
        final DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();
        javaLogger.logInfo(MessageUtils.getMessage("DOTJ031I", prop).toString());
    }
    private static boolean alreadyShowed(final String notMappingKey){
        if(!notMappingRules.contains(notMappingKey)){
            notMappingRules.add(notMappingKey);
            return false;
        }
        return true;
    }
}
