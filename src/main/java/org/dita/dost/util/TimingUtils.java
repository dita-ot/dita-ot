/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/**
 * Copyright (c) 2009 Really Strategies, Inc.
 */
package org.dita.dost.util;

import java.util.Calendar;
import java.util.Date;

/**
 *
 */
public final class TimingUtils {

    /**
     * Private default constructor to make class uninstantiable.
     */
    private TimingUtils() {
        
    }
    
    public static String reportElapsedTime(final Date startTime) {
        final long elapsedTime = Calendar.getInstance().getTime().getTime() - startTime.getTime();
        if (elapsedTime < 500) {
            return elapsedTime + " milliseconds";
        } else if (elapsedTime < 60000) {
            return (elapsedTime/1000.0) + " seconds";
        } else {
            return (elapsedTime/60000) + " minutes";
        }
    }

    /**
     * Get current time.
     * 
     * @return current time
     */
    public static Date getNowTime() {
        return Calendar.getInstance().getTime();
    }


}
