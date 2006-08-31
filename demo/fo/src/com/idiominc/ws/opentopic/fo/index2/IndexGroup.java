package com.idiominc.ws.opentopic.fo.index2;


/**
 * User: Ivan Luzyanin
 * Date: 21.06.2005
 * Time: 10:38:48
 * <br><br>
 * Respresents index group.
 */
public interface IndexGroup {

	/**
	 * @return group label
	 */
	String getLabel();


	/**
	 * @return group entries
	 */
	IndexEntry[] getEntries();


	/**
	 * Adds entry to the index group
	 *
	 * @param theEntry index entry
	 */
	void addEntry(IndexEntry theEntry);
}
