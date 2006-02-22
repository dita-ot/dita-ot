package com.idiominc.ws.opentopic.fo.index2.util;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * User: Ivan Luzyanin
 * Date: 21.06.2005
 * Time: 11:16:59
 */
class IndexEntryImpl
		implements IndexEntry {
	private String value;
	private String formattedString;
	private String soString;
	private String sortString;

	private HashMap childs = new HashMap();

	private boolean startRange = false;
	private boolean endsRange = false;
	private boolean suppressesThePageNumber = false;
	private boolean restoresPageNumber = false;

	private ArrayList refIDs = new ArrayList();


	public IndexEntryImpl(String theValue, String theSoString, String theSortString, String theFormattedString) {
		this.value = theValue;
		this.soString = theSoString;
		this.sortString = theSortString;
		this.formattedString = theFormattedString;
	}


	public String[] getRefIDs() {
		return (String[]) refIDs.toArray(new String[refIDs.size()]);
	}


	public String getValue() {
		return this.value;
	}


	public String getSoValue() {
		return this.soString;
	}


	public String getFormattedString() {
		return this.formattedString;
	}


	public String getSortString() {
		return this.sortString;
	}


	public IndexEntry[] getChildIndexEntries() {
		Collection collection = childs.values();
		return (IndexEntry[]) collection.toArray(new IndexEntry[collection.size()]);
	}


	public boolean isStartingRange() {
		return this.startRange;
	}


	public boolean isEndingRange() {
		return this.endsRange;
	}


	public boolean isSuppressesThePageNumber() {
		return this.suppressesThePageNumber;
	}


	public boolean isRestoresPageNumber() {
		return this.restoresPageNumber;
	}


	public void addRefID(final String theID) {
		if (!this.refIDs.contains(theID)) {
			this.refIDs.add(theID);
		}
	}


	public void addChild(IndexEntry theEntry) {
		final String entryValue = theEntry.getValue();
		if (!this.childs.containsKey(entryValue)) {
			this.childs.put(entryValue, theEntry);
			return;
		}
		//The index with same value already exists
		//Add childs of given entry to existing entry
		final IndexEntry existingEntry = (IndexEntry) this.childs.get(entryValue);

		final IndexEntry[] childIndexEntries = theEntry.getChildIndexEntries();
		for (int i = 0; i < childIndexEntries.length; i++) {
			IndexEntry childIndexEntry = childIndexEntries[i];
			existingEntry.addChild(childIndexEntry);
		}
		//supress some attributes of given entry to the existing one
		if (theEntry.isRestoresPageNumber()) {
			existingEntry.setRestoresPageNumber(true);
		}
		if (!theEntry.isSuppressesThePageNumber()) {
			existingEntry.setSuppressesThePageNumber(false);
		}
		if (theEntry.isStartingRange()) {
			existingEntry.setStartRange(true);
		}
		if (theEntry.getSortString() != null) {
			existingEntry.setSortString(theEntry.getSortString());
		}
		if (theEntry.getSoValue() != null) {
			existingEntry.setSoValue(theEntry.getSoValue());
		}
	}

	public void setSortString(String theSortString) {
		this.sortString = theSortString;
	}

	public void setSoValue(String theSoValue) {
		this.soString = theSoValue;
	}

	public void setStartRange(boolean theStartRange) {
		if (theStartRange && this.endsRange) {
			this.endsRange = false;
		}
		this.startRange = theStartRange;
	}


	public void setEndsRange(boolean theEndsRange) {
		if (theEndsRange && this.startRange) {
			this.startRange = false;
		}
		this.endsRange = theEndsRange;
	}


	public void setSuppressesThePageNumber(boolean theSuppressesThePageNumber) {
		if (theSuppressesThePageNumber && this.restoresPageNumber) {
			this.restoresPageNumber = false;
		}

		this.suppressesThePageNumber = theSuppressesThePageNumber;
	}


	public void setRestoresPageNumber(boolean theRestoresPageNumber) {
		if (theRestoresPageNumber && this.suppressesThePageNumber) {
			this.suppressesThePageNumber = false;
		}
		this.restoresPageNumber = theRestoresPageNumber;
	}
}
