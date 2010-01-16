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
 * Version class, show the version of dita-ot.
 * @author William
 *
 */
public class Version {
	
	private static final String fversion = "@@VERSION@@";
	
	

	/**
	 * main function.
	 * @param args input arguments from command line
	 */
	public static void main(String[] args) {
		System.out.println (fversion);

	}


	/**
	 * @return the fversion
	 */
	public static String getVersion() {
		return fversion;
	}

}
