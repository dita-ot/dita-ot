package com.idiominc.ws.opentopic.fo.index2;

/**
 * User: Ivan Luzyanin
 * Date: 21.06.2005
 * Time: 11:14:25
 * <br><br>
 * Respresents the Adobe Framemaker's index entry<br>
 * <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
 */
public interface IndexEntry {

	/**
	 * @return index reference ids
	 */
	public String[] getRefIDs();


	/**
	 * @return index entry value
	 */
	public String getValue();


	/**
	 * @return index entry "so value".<br> "So value" is a value used in Japanese entries to help computer to sort the index
	 *         strings properly. In other words it specifies string used to sort entries. It called "so value" because the syntax
	 *         to specify it within strings is following: "index string value<so>index string pronunciation"
	 *         (h.e. after the "<so>" tag there is a "so value"). These so strings is being inserted either by the authors
	 *         or by the automatic translation tools.
	 */
	public String getSoValue();


	/**
	 * @return The string with formatting<br>
	 *         <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 */
	public String getFormattedString();


	/**
	 * @return the sort string for the entry<br>
	 *         <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 */
	public String getSortString();


	/**
	 * @return child entries of this entry
	 */
	public IndexEntry[] getChildIndexEntries();


	/**
	 * @return if this entry starts range<br>
	 *         <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 */
	public boolean isStartingRange();


	/**
	 * @return if this entry ends range<br>
	 *         <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 */
	public boolean isEndingRange();


	/**
	 * @return if this entry suppresses page number<br>
	 *         <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 */
	public boolean isSuppressesThePageNumber();


	/**
	 * @return if this entry restores page number<br>
	 *         <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 */
	public boolean isRestoresPageNumber();


	/**
	 * Adds reference id to the index entry
	 *
	 * @param theID reference id
	 */
	void addRefID(String theID);


	/**
	 * Adds child to the entry
	 *
	 * @param theEntry index entry
	 */
	void addChild(IndexEntry theEntry);


	/**
	 * Sets if the index entry restores page number
	 * <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 *
	 * @param theRestoresPageNumber
	 */
	void setRestoresPageNumber(boolean theRestoresPageNumber);


	/**
	 * Sets if the index entry suppresses page number
	 * <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 *
	 * @param theSuppressesThePageNumber
	 */
	void setSuppressesThePageNumber(boolean theSuppressesThePageNumber);


	/**
	 * Sets if the index entry starts range
	 * <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 *
	 * @param theStartRange
	 */
	void setStartRange(boolean theStartRange);


	/**
	 * Sets if the index entry ends range
	 * <code>See "Adobe Framemaker 7.1" help, topic "Adding index markers" (page is "1_15_8_0.html") for details</code>
	 *
	 * @param theEndsRange
	 */
	void setEndsRange(boolean theEndsRange);


	/**
	 * Sets so value
	 *
	 * @param theSoValue
	 */
	void setSoValue(String theSoValue);


	/**
	 * Sets sort string for the value
	 *
	 * @param theSortString
	 */
	void setSortString(String theSortString);
}
