package com.idiominc.ws.opentopic.fo.index2.configuration;

/**
 * User: Ivan Luzyanin
 * Date: 21.06.2005
 * Time: 16:20:31
 */
public interface ConfigEntry {

	/**
	 * @return group label
	 */
	String getLabel();

	/**
	 * @return group key. this key is being used to check if some string belongs to this group by comparing it with
	 *         two keys of near by config entries
	 */
	String getKey();

	/**
	 * @return specifies group member characters. The meaning of these characters is that if some string starts with the
	 *         character from this array then it(string) belongs to this group
	 */
	char[] getGroupMembers();
}
