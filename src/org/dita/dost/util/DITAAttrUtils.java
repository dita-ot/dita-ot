/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */

package org.dita.dost.util;

import java.util.ArrayList;
import java.util.List;
/**
 * This util is used for check attributes of elements.
 * @author william
 *
 */
public class DITAAttrUtils {
	
	//List to store non-Print transtypes.
	private List<String>nonPrintTranstype;
	
	//Depth inside element for @print.
	/*e.g for <a print="yes">
	 *            <b/>
	 *        </a>
	 * tag b's printLevel is 2  
	 */
    private int printLevel;
	
	private static DITAAttrUtils util = new DITAAttrUtils();
	/**
	 * Constructor.
	 */
	private DITAAttrUtils() {
		
		nonPrintTranstype = new ArrayList<String>();
		nonPrintTranstype.add(Constants.TRANS_TYPE_ECLIPSECONTENT);
		nonPrintTranstype.add(Constants.TRANS_TYPE_ECLIPSEHELP);
		nonPrintTranstype.add(Constants.TRANS_TYPE_HTMLHELP);
		nonPrintTranstype.add(Constants.TRANS_TYPE_JAVAHELP);
		nonPrintTranstype.add(Constants.TRANS_TYPE_XHTML);
		
		printLevel = 0;
		
	}
	/**
	 * Get an instance.
	 * @return an instance.
	 */
	public static DITAAttrUtils getInstance(){
		
		return util;
	}
	/**
	 * Increase print level.
	 * @param printValue value of print attribute.
	 * @return whether the level is increased.
	 */
	public boolean increasePrintLevel(String printValue){
		
		if(printValue != null){
			//@print = "printonly"
			if(Constants.ATTR_PRINT_VALUE_PRINT_ONLY.equals(printValue)){
				printLevel ++ ;
				return true;
			//descendant elements
			}else if(printLevel > 0){
				printLevel ++ ;
				return true;
			}
		//@print not set but is descendant tag of "printonly"
		}else if(printLevel > 0){
			printLevel ++ ;
			return true;
		}
		
		return false;
		
	}
	/**
	 * Decrease print level. 
	 * @return boolean
	 */
	public boolean decreasePrintLevel(){
		if(printLevel > 0){
			printLevel --;
			return true;
		}else{
			return false;
		}
	}
	/**
	 * Check whether need to skip for @print.
	 * @param transtype String
	 * @return boolean
	 */
	public boolean needExcludeForPrintAttri(String transtype){
		
		if(printLevel > 0 && nonPrintTranstype.contains(transtype)){
			return true;
		}else{
			return false;
		}
		
	}
	/**
	 * Reset the utils.
	 */
	public void reset(){
		
		printLevel = 0;
		
	}
}
