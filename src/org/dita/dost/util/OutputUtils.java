/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

/**
 * OutputUtils to control the output behavior
 * @author wxzhang
 *
 */
public final class OutputUtils {
	private static int generatecopyouter=1;//default:only generate&copy the non-overflowing files 
	private static boolean onlytopicinmap=false;//default:only the topic files will be resolved in the map

	public static final String OUTTERCONTROL_FAIL="FAIL";
	public static final String OUTTERCONTROL_WARN="WARN";
	public static final String OUTTERCONTROL_QUIET="QUIET";
	
	private static String outercontrol=OUTTERCONTROL_WARN;
	
	public static final int NOT_GENERATEOUTTER=1;
	public static final int GENERATEOUTTER=2;
	public static final int OLDSOLUTION=3;
	
	public static String OutputDir=null;
	
	public static String InputMapDir=null;
	
	private OutputUtils(){
		
	}
	
	/**
	 * Retrieve the outercontrol
	 * @return String
	 *
	 */
	public static String getOutterControl(){
		return outercontrol;
	}
	
	/**
	 * Set the outercontrol
	 * @param control
	 */
	public static void setOutterControl(String control){
		if(control==null) {
			outercontrol=OUTTERCONTROL_WARN;
			return;
		}
		if(control.equalsIgnoreCase(OUTTERCONTROL_FAIL)){
			outercontrol=OUTTERCONTROL_FAIL;
			return;
		}
		if(control.equalsIgnoreCase(OUTTERCONTROL_WARN)){
			outercontrol=OUTTERCONTROL_WARN;
			return;
		}
		if(control.equalsIgnoreCase(OUTTERCONTROL_QUIET)){
			outercontrol=OUTTERCONTROL_QUIET;
			return;
		}
		outercontrol=OUTTERCONTROL_WARN;
	}
	
	/**
	 * Retrieve the flag of onlytopicinmap
	 * @return boolean
	 */
	public static boolean getOnlyTopicInMap(){
		return onlytopicinmap;
	}
	
	/**
	 * Set the onlytopicinmap
	 * @param flag
	 */
	public static void setOnlyTopicInMap(String flag){
		if(flag==null){
			onlytopicinmap=false;
			return;
		}
		if(flag.equalsIgnoreCase("true")){
			onlytopicinmap=true;
		}else{
			onlytopicinmap=false;
		}
	}
	
	/**
	 * Retrieve the flag of generatecopyouter
	 * @return int
	 */
	public static int getGeneratecopyouter(){
		return generatecopyouter;
	}
	
	/**
	 * Set the generatecopyouter
	 * @param flag
	 */
	public static void setGeneratecopyouter(String flag){
		if(flag==null){
			generatecopyouter=NOT_GENERATEOUTTER;
			return;
		}
		if(flag.equals("1")){
			generatecopyouter=NOT_GENERATEOUTTER;
			return;
		}
		if(flag.equals("2")){
			generatecopyouter=GENERATEOUTTER;
			return;
		}
		if(flag.equals("3")){
			generatecopyouter=OLDSOLUTION;
			return;
		}
		generatecopyouter=NOT_GENERATEOUTTER;
	}
	
	public static String getOutputDir(){
		return OutputDir;
	}
	
	public static void setOutputDir(String outputDir){
		OutputDir=outputDir;
	}
	
	public static String getInputMapPathName(){
		return InputMapDir;
	}
	
	public static void setInputMapPathName(String inputMapDir){
		InputMapDir=inputMapDir;
	}
}
