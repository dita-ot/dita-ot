/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.Hashtable;

/**
 * Interface 
 * @author Zhang, Yuan Peng
 */
public interface IAction {
	/**
	 * Set the input string
	 * @param input
	 */
	void setInput(String input);
	/**
	 * Set the input parameters
	 * @param param
	 */
	void setParam(String param);
	/**
	 * Return the result
	 * @return
	 */
	String getResult();
	/**
	 * Set the feature table
	 * @param h
	 */
	void setFeatures(Hashtable h);
}
