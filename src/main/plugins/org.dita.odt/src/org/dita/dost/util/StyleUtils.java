/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/**
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Style utility to get and set style names. Used from ODT transtype's XSLT stylesheets.
 * 
 * @author Zhang Di Hua
 *
 */
public final class StyleUtils {


    //list for style name in xslt for hi-d tags
    private static final List<String> hiStyleNameList = new ArrayList<>();

    //map for storing flagging style names.
    private static final Map<String, String> flagStyleNameMap = new HashMap<>();

    /**
     * Default Constructor
     *
     */
    private StyleUtils(){
    }

    //check hi-d style name
    public static String insertHiStyleName(final String styleName){
        for(final String item : hiStyleNameList){
            if(item.length() == styleName.length()){
                //sort strings to remove order effect
                final String cp1 = sort(item);
                final String cp2 = sort(styleName);

                if(cp1.equals(cp2)){
                    return "true";
                }else{
                }
            }else{
            }
        }
        //It is a new style name and add it to a list.
        hiStyleNameList.add(styleName);

        return "false";
    }
    //get hi-d style name
    public static String getHiStyleName(final String styleName){
        for(final String item : hiStyleNameList){

            if(item.length() == styleName.length()){
                //sort strings to remove order effect
                final String cp1 = sort(item);
                final String cp2 = sort(styleName);

                if(cp1.equals(cp2)){
                    return item;
                }else{
                }
            }else{
            }
        }
        //It is a new style name and add it to a list.
        hiStyleNameList.add(styleName);

        return styleName;
    }
    //sort string for compare.
    private static String sort(final String item) {

        final char[] chars = item.toCharArray();
        for(int i = 1; i < chars.length ; i++){
            for(int j = 0; j < chars.length - 1 ; j++){
                if(chars[j] > chars[j+1]){
                    final char temp = chars[j];
                    chars[j] = chars[j+1];
                    chars[j+1] = temp;
                }
            }
        }
        return String.valueOf(chars);


    }
    //store flagging style name
    public static String insertFlagStyleName(final String styleName){
        flagStyleNameMap.put(styleName, styleName);

        return styleName;
    }
    //get flagging style name
    public static String getFlagStyleName(final String flagStyleName){

        if(flagStyleNameMap.containsKey(flagStyleName)){
            return flagStyleNameMap.get(flagStyleName);
        }else{
            return "";
        }
    }

}
