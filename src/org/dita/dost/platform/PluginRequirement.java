/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2008 All Rights Reserved.
 */

package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

public class PluginRequirement {
	private ArrayList<String> plugins;
	private boolean required;
	
	public PluginRequirement() {
		plugins = new ArrayList<String>();
		required = true;
	}

	public void addPlugins(String s) {
		StringTokenizer t = new StringTokenizer(s, "|");
		while (t.hasMoreTokens()) {
			plugins.add(t.nextToken());
		}
	}
	
	public void setRequired(boolean r) {
		required = r;
	}
	
	public Iterator<String> getPlugins() {
		return plugins.iterator();
	}
	
	public boolean getRequired() {
		return required;
	}
	
	public String toString() {
		return plugins.toString();
	}
}