/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.util.Random;

/**
 * Radom utility to generate random long integer as list id in WORD RTF 
 * transformation.
 * 
 * @author Zhang, Yuan Peng
 *
 */
public class RandomUtils {

	/**
	 * @return long -
	 * 				Long random integer generated.
	 */
	public static long getRandomNum(){
		Random generator = new Random();
		return generator.nextLong();
	}
}
