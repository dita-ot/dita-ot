package com.idiominc.ws.opentopic.xsl.extension;

import java.util.Locale;

import com.ibm.icu.text.Collator;


/**
 * User: Ivan Luzyanin
 * Date: 3/9/2004
 * Time: 10:27:42
 */
public class CompareStrings {
    public static Integer compare(String theS1, String theS2, String theLanguage, String theCountry) {
        Collator collator = Collator.getInstance(new Locale(theLanguage, theCountry));
        return new Integer(collator.compare(theS1, theS2));
    }


    public static Integer compare(String theS1, String theS2, String theLanguage) {
        return compare(theS1, theS2, theLanguage, "");
    }

}
