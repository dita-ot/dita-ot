package com.idiominc.ws.opentopic.fo.index2.configuration;

/**
 * User: Ivan Luzyanin
 * Date: 21.06.2005
 * Time: 16:20:31
 */
class ConfigEntryImpl
		implements ConfigEntry {
	private String label;
	private String key;
	private char[] members;


	public ConfigEntryImpl(String theLabel, String theKey, char[] theMembers) {
		this.label = theLabel;
		this.key = theKey;
		this.members = theMembers;
	}


	public String getLabel() {
		return this.label;
	}


	public String getKey() {
		return this.key;
	}

	public char[] getGroupMembers() {
		return this.members;
	}
}
