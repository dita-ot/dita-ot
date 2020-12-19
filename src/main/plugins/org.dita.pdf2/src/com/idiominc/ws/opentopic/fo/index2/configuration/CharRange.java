/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 Idiom Technologies, Inc
 *
 * See the accompanying LICENSE file for applicable license.
 */
package com.idiominc.ws.opentopic.fo.index2.configuration;

import com.idiominc.ws.opentopic.fo.index2.IndexCollator;

/**
 * Created by IntelliJ IDEA.
 * User: BlackSide
 * Date: 30/8/2007
 * Time: 18:34:32
 * To change this template use File | Settings | File Templates.
 * @deprecated since 3.4
 */
@Deprecated
public class CharRange {

    private final String start;
    private final String end;

    public CharRange(final String theStart, final String theEnd) {
        start = theStart;
        end = theEnd;
    }

    public boolean isInRange(final String value, final IndexCollator collator){
        return (collator.compare(value,start) > 0) && (collator.compare(value,end) < 0);
    }
}
