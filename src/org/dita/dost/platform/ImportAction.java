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
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.util.Constants;

/**
 * ImportAction implements IAction and import the resource 
 * provided by plug-ins into the xsl files and ant scripts.
 * @author Zhang, Yuan Peng
 */
public abstract class ImportAction implements IAction {
	
	/** Action values. */
	protected final Set<String> valueSet;
	/** Action parameters. */
	protected final Hashtable<String,String> paramTable;
	
	/**
	 * Default Constructor.
	 */
	public ImportAction() {
		valueSet = new LinkedHashSet<String>(Constants.INT_16);
		paramTable = new Hashtable<String,String>();
	}
	
	/**
	 * get result.
	 * @return result
	 */
	public abstract String getResult();

	/**
	 * set input.
	 * @param input input
	 */
	public void setInput(final String input) {
		final StringTokenizer inputTokenizer = new StringTokenizer(input, Integrator.FEAT_VALUE_SEPARATOR);
		while(inputTokenizer.hasMoreElements()){
			valueSet.add(inputTokenizer.nextToken());
		}
	}

	public void addParam(final String name, final String value) {
		paramTable.put(name, value);
	}
	/**
	 * Set the feature table.
	 * @param h hastable
	 */
	public void setFeatures(final Hashtable<String,String> h) {
		
	}

}
