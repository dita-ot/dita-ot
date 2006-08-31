package com.idiominc.ws.opentopic.fo.index2;

import java.util.Locale;

/**
 * User: Volodymyr.Mykhailyk
 * Date: 31/7/2006
 * Time: 13:03:18
 */
public class IndexCollator {

    private com.ibm.icu.text.Collator icu4jCollator = null;
    private java.text.Collator defaultCollator = null;
    private boolean icuCollator = true;

    public IndexCollator(Locale theLocale) {
        this.defaultCollator = java.text.Collator.getInstance(theLocale);
        try {
            this.icu4jCollator = com.ibm.icu.text.Collator.getInstance(theLocale);
        } catch (NoClassDefFoundError ex) {
            System.out.println("[INFO] IBM ICU4J Collator is not found. Default Java Collator will be used");
            icuCollator = false;
        }
    }

    public int compare(Object o1, Object o2) {
        if (icuCollator) {
            return this.icu4jCollator.compare(o1, o2);
        } else return this.defaultCollator.compare(o1, o2);

    }

}
