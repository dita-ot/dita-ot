/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * String relevant utilities.
 * 
 * @author Wu, Zhi Qiang
 */
public final class StringUtils {

    /**
     * Private default constructor to make class uninstantiable.
     */
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
    public static String join(final Collection coll, final String delim) {
        final StringBuilder buff = new StringBuilder(256);
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
     * Assemble all elements in map to a string.
     * 
     * @param value map to serializer
     * @param delim entry delimiter
     * @return concatenated map
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static String join(final Map value, final String delim) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        final StringBuilder buf = new StringBuilder();
        for (final Iterator<Map.Entry<String, String>> i = value.entrySet().iterator(); i.hasNext();) {
            final Map.Entry<String, String> e = i.next();
            buf.append(e.getKey()).append(EQUAL).append(e.getValue());
            if (i.hasNext()) {
                buf.append(delim);
            }
        }
        return buf.toString();
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
        final StringBuilder result = new StringBuilder();
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
     * Parse {@code props} attribute specializations
     * 
     * @param domains input domain
     * @return list of {@code props} attribute specializations
     */
    public static String[][] getExtProps(final String domains){
        final List<String[]> propsBuffer = new ArrayList<>();
        int propsStart = domains.indexOf("a(" + ATTRIBUTE_NAME_PROPS);
        int propsEnd = domains.indexOf(")",propsStart);
        while (propsStart != -1 && propsEnd != -1){
            final String propPath = domains.substring(propsStart+2,propsEnd).trim();
            final StringTokenizer propPathTokenizer = new StringTokenizer(propPath, STRING_BLANK);
            final List<String> propList = new ArrayList<>(128);
            while(propPathTokenizer.hasMoreTokens()){
                propList.add(propPathTokenizer.nextToken());
            }
            propsBuffer.add(propList.toArray(new String[propList.size()]));
            propsStart = domains.indexOf("a(" + ATTRIBUTE_NAME_PROPS, propsEnd);
            propsEnd = domains.indexOf(")",propsStart);
        }
        return propsBuffer.toArray(new String[propsBuffer.size()][]);
    }

    /**
     * Break down a string separated by <code>delim</code> into a string set.
     * @param s String to be splitted
     * @param delim Delimiter to be used.
     * @return string set
     */
    public static Set<String> restoreSet(final String s, final String delim) {
        final Set<String> copytoSet = new HashSet<>();

        if (StringUtils.isEmptyString(s)) {
            return copytoSet;
        }

        final StringTokenizer st = new StringTokenizer(s, delim);

        while (st.hasMoreTokens()) {
            final String entry = st.nextToken();
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
    public static boolean isEmptyString(final String s){
        return (s == null || s.trim().length() == 0);
    }

    /**
     * If target is null, return the value; else append value to target.
     * If withSpace is true, insert a blank between them.
     * @param target target to be appended
     * @param value value to append
     * @param withSpace whether insert a blank
     * @return processed string
     */
    public static String setOrAppend(final String target, final String value, final boolean withSpace){
        if(target == null){
            return value;
        }if(value == null){
            return target;
        }else{
            if(withSpace && !target.endsWith(STRING_BLANK)){
                return target + STRING_BLANK + value;
            }else{
                return target + value;
            }
        }
    }

    /**
     * Return a Java Locale object.
     * @param anEncoding encoding
     * @return locale
     * @throws NullPointerException when anEncoding parameter is {@code null}
     */

    public static Locale getLocale(final String anEncoding){
        Locale aLocale = null;
        String country = null;
        String language = null;
        String variant = null;

        //Tokenize the string using "-" as the token string as per IETF RFC4646 (superceeds RFC3066).

        final StringTokenizer tokenizer = new StringTokenizer(anEncoding, "-");

        //We need to know how many tokens we have so we can create a Locale object with the proper constructor.
        final int numberOfTokens = tokenizer.countTokens();

        if (numberOfTokens == 1) {
            final String tempString = tokenizer.nextToken().toLowerCase();

            //Note: Newer XML parsers should throw an error if the xml:lang value contains
            //underscore. But this is not guaranteed.

            //Check to see if some one used "en_US" instead of "en-US".
            //If so, the first token will contain "en_US" or "xxx_YYYYYYYY". In this case,
            //we will only grab the value for xxx.
            final int underscoreIndex = tempString.indexOf("_");

            if (underscoreIndex == -1){
                language = tempString;
            }else if (underscoreIndex == 2 || underscoreIndex == 3){
                //check is first subtag is two or three characters in length.
                language = tempString.substring(0, underscoreIndex);
            }

            aLocale = new Locale(language);
        } else if (numberOfTokens == 2) {

            language = tokenizer.nextToken().toLowerCase();

            final String subtag2 = tokenizer.nextToken();
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
            final String subtag2 = tokenizer.nextToken();
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
            aLocale = new Locale(LANGUAGE_EN,
                    COUNTRY_US);

        }

        return aLocale;
    }
    
    /** Whitespace normalization state. */
    private enum WhiteSpaceState { WORD, SPACE }

    /**
     * Normalize and collapse whitespaces from string buffer.
     * 
     * @param strBuffer The string buffer.
     */
    public static void normalizeAndCollapseWhitespace(final StringBuilder strBuffer){
        WhiteSpaceState currentState = WhiteSpaceState.WORD;
        for (int i = strBuffer.length() - 1; i >= 0; i--) {
            final char currentChar = strBuffer.charAt(i);
            if (Character.isWhitespace(currentChar)) {
                if (currentState == WhiteSpaceState.SPACE) {
                    strBuffer.delete(i, i + 1);
                } else if(currentChar != ' ') {
                    strBuffer.replace(i, i + 1, " ");
                }
                currentState = WhiteSpaceState.SPACE;
            } else {
                currentState = WhiteSpaceState.WORD;
            }
        }
    }

}