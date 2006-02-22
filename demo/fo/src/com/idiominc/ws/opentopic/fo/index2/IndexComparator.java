package com.idiominc.ws.opentopic.fo.index2;

import com.ibm.icu.text.Collator;

import java.util.Comparator;
import java.util.Locale;

/**
 * User: Ivan Luzyanin
 * Date: 29.06.2005
 * Time: 15:54:47
 */
class IndexComparator
		implements Comparator {

	private final Collator collator;


	public IndexComparator(Locale theLocale) {
		this.collator = Collator.getInstance(theLocale);
	}


	public int compare(Object o1, Object o2) {
		String value1 = getSortString((IndexEntry) o1);
		String value2 = getSortString((IndexEntry) o2);

		return this.collator.compare(value1, value2);
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
