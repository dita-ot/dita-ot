/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.Random;

/**
 * Radom utility to generate random long integer as list id in WORD RTF
 * transformation. Used from RTF and OTD transtype's XSTL stylesheets.
 * 
 * @author Zhang, Yuan Peng
 *
 */
public final class RandomUtils {
    
    /**
     * Private default constructor to make class uninstantiable.
     */
    private RandomUtils(){
    }

    /**
     * Return a random long number.
     * @return long -
     * 				Long random integer generated.
     */
    public static long getRandomNum(){
        final Random generator = new Random();
        return generator.nextLong();
    }
}
