/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import org.dita.dost.log.DITAOTJavaLogger;

/**
 * Version Utility class, providing method of getting version 
 * information to AntVersion.java
 * @author william
 * 
 */
public class VersionUtil {

	private final String milestone = "@@MILESTONE@@";

	private final String otversion = "@@OTVERSION@@";

	private DITAOTJavaLogger logger = new DITAOTJavaLogger();
	

	/**
	 * @return the milestone
	 */
	public String getMilestone() {
		return "Milestone " + milestone;
	}

	/**
	 * @return the otversion
	 */
	public String getOtversion() {
		return "DITA Open Toolkit " + otversion;
	}
	
	public VersionUtil() {
		
	}

}
