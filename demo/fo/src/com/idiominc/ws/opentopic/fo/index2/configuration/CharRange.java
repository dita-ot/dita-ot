package com.idiominc.ws.opentopic.fo.index2.configuration;

import com.idiominc.ws.opentopic.fo.index2.IndexCollator;

/**
 * Created by IntelliJ IDEA.
 * User: BlackSide
 * Date: 30/8/2007
 * Time: 18:34:32
 * To change this template use File | Settings | File Templates.
 */
public class CharRange {

	private String start;
	private String end;

	public CharRange(String theStart, String theEnd) {
		start = theStart;
		end = theEnd;
	}

	public boolean isInRange(String value, IndexCollator collator){
		return (collator.compare(value,start) > 0) && (collator.compare(value,end) < 0);
	}
}
