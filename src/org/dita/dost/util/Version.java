/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2009 All Rights Reserved.
 */
package org.dita.dost.util;
/**
 * Version class, show the version of dita-ot in java code.
 * @author William
 *
 */
public final class Version {

    private static final String fversion = "@@VERSION@@";

    /**
     * Private default constructor to make class uninstantiable.
     */
    private Version() {
    }

    /**
     * main function.
     * @param args input arguments from command line
     */
    public static void main(final String[] args) {
        System.out.println (fversion);

    }


    /**
     * @return the fversion
     */
    public static String getVersion() {
        return fversion;
    }

}
