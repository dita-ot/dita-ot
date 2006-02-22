package com.idiominc.ws.opentopic.fo.index2;


/**
 * User: Ivan Luzyanin
 * Date: 30.06.2005
 * Time: 11:29:36
 */
public interface IndexEntryFoundListener {
	/**
	 * Notifies that the new index entry was found
	 *
	 * @param theEntry index entry
	 */
	void foundEntry(IndexEntry theEntry);
}
