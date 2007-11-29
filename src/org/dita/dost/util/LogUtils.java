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
	private static final String FATAL="FATAL";
	private static final String WARN="WARN";
	private static final String ERROR="ERROR";
	private static final String INFO="INFO";
	/** Line separator */
	private static final String LINE_SEP = System.getProperty("line.separator");
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
	
	/**
	 * Initial the class
	 */
	public static void clear(){
		numOfFatals=0;
		numOfErrors=0;
		numOfWarnings=0;
		numOfInfo=0;
	}
	
	/**
	 * Check whether error exists in the whole transforming process
	 * @return boolean
	 */
	public  static boolean haveFatalOrError(){
		if(numOfFatals>0 || numOfErrors>0) 
			return true;
		else 
			return false;
	} 
	
	/**
	 * print the statics message
	 * @return String
	 */
	public static void print(){
		System.out.println("Number of Fatals : " + numOfFatals );
		System.out.println("Number of Errors : " + numOfErrors );
		System.out.println("Number of Warnings : " + numOfWarnings );
		System.out.println("Number of Info : " + numOfInfo );
	}
	
	/**
	 * Get the statics message
	 * @return String
	 */
	public static String getLogStatisticInfo(){
		String logStaticticInfo;
		logStaticticInfo="Number of Fatals : " + getNumOfFatals() +LINE_SEP;
		logStaticticInfo=logStaticticInfo+"Number of Errors : " + getNumOfErrors() +LINE_SEP;
		logStaticticInfo=logStaticticInfo+"Number of Warnings : " + getNumOfWarnings() +LINE_SEP;
		return logStaticticInfo;
	}
	
	/**
	 * Increase the number of Exceptions by severity level 
	 * @param msgType
	 */
	public static void increaseNumOfExceptionByType(String msgType){
		
		if (msgType==null){
			increaseNumOfErrors();
			return;
		}
		
		String type=msgType.toUpperCase();
		
		if(FATAL.equals(type)){
			LogUtils.increaseNumOfFatals();
			return;
		}
		if(ERROR.equals(type)){
			increaseNumOfErrors();
			return;
		}
		if(WARN.equals(type)){
			increaseNumOfWarnings();
			return;
		}
		if(INFO.equals(type)){
			increaseNumOfInfo();
			return;
		}
		//TODO
		increaseNumOfErrors();

	}
}
