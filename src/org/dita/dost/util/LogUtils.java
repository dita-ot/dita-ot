/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.util;

/**
 * LogUtils : To anylyse the information from dita & xslt ,supply
 * fatal , error , warn , info to the DITAOTBuildLogger,
 * which to complement the ant's shortage.
 * @author wxzhang
 *
 */
public class LogUtils {
	private static int numOfFatals=0;
	private static int numOfErrors=0;
	private static int numOfWarnings=0;
	private static int numOfInfo=0;
	/** Line separator */
	protected static final String LINE_SEP = System.getProperty("line.separator");
	private LogUtils(){
	}
	public static void increaseNumOfFatals(){
		numOfFatals++;
	}
	public static void increaseNumOfErrors(){
		numOfErrors++;
	}
	public static void increaseNumOfWarnings(){
		numOfWarnings++;
	}
	public static void increaseNumOfInfo(){
		numOfInfo++;
	}
	public static int getNumOfFatals(){
		return numOfFatals;
	}
	public static int getNumOfErrors(){
		return numOfErrors;
	}
	public static int getNumOfWarnings(){
		return numOfWarnings;
	}
	public static int getNumOfInfo(){
		return numOfInfo;
	}
	public static void clear(){
		numOfFatals=0;
		numOfErrors=0;
		numOfWarnings=0;
		numOfInfo=0;
	}
	public  static boolean haveFatalOrError(){
		if(numOfFatals>0 || numOfErrors>0) 
			return true;
		else 
			return false;
	} 
	public static void print(){
		System.out.println("Number of Fatals : " + numOfFatals );
		System.out.println("Number of Errors : " + numOfErrors );
		System.out.println("Number of Warnings : " + numOfWarnings );
		System.out.println("Number of Info : " + numOfInfo );
	}
	public static String getLogStatisticInfo(){
		String logStaticticInfo;
		logStaticticInfo="Number of Fatals : " + getNumOfFatals() +LINE_SEP;
		logStaticticInfo=logStaticticInfo+"Number of Errors : " + getNumOfErrors() +LINE_SEP;
		logStaticticInfo=logStaticticInfo+"Number of Warnings : " + getNumOfWarnings() +LINE_SEP;
		return logStaticticInfo;
	}
}
