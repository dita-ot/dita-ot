package com.idiominc.ws.opentopic.fo.index2;

import java.util.Comparator;
import java.util.Locale;

/**
 * User: Volodymyr.Mykhailyk
 * Date: 31/7/2006
 * Time: 13:03:18
 */
class IndexComparator
        implements Comparator {

    private IndexCollator Collator;


    public IndexComparator(Locale theLocale) {
        this.Collator = new IndexCollator(theLocale);
    }


    public int compare(Object o1, Object o2) {
        String value1 = getSortString((IndexEntry) o1);
        String value2 = getSortString((IndexEntry) o2);

        return this.Collator.compare(value1,value2);
    }


    private String getSortString(IndexEntry theEntry1) {
        String result;
        if (theEntry1.getSortString() != null) {
            result = theEntry1.getSortString();
        } else if (theEntry1.getSoValue() != null) {
            result = theEntry1.getSoValue();
        } else {
            result = theEntry1.getValue();
        }
        return result;
    }
}
