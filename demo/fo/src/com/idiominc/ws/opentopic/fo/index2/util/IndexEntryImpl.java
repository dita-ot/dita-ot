package com.idiominc.ws.opentopic.fo.index2.util;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/*
Copyright � 2004-2006 by Idiom Technologies, Inc. All rights reserved.
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other
trademarks are the property of their respective owners.

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
See the accompanying license.txt file for applicable licenses.
*/
class IndexEntryImpl
		implements IndexEntry {
	private String value;
	private String formattedString;
	private String soString;
	private String sortString;

	private HashMap childs = new HashMap();
	private HashMap seeChilds = new HashMap();
	private HashMap seeAlsoChilds = new HashMap();

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


    public void addSeeChild(IndexEntry theEntry) {
		final String entryValue = theEntry.getValue();
		if (!this.seeChilds.containsKey(entryValue)) {
			this.seeChilds.put(entryValue, theEntry);
			return;
		}
		//The index with same value already exists
		//Add seeChilds of given entry to existing entry
		final IndexEntry existingEntry = (IndexEntry) this.seeChilds.get(entryValue);

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

    public void addSeeAlsoChild(IndexEntry theEntry) {
		final String entryValue = theEntry.getValue();
		if (!this.seeAlsoChilds.containsKey(entryValue)) {
			this.seeAlsoChilds.put(entryValue, theEntry);
			return;
		}
		//The index with same value already exists
		//Add seeAlsoChilds of given entry to existing entry
		final IndexEntry existingEntry = (IndexEntry) this.seeAlsoChilds.get(entryValue);

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

    public IndexEntry[] getSeeChildIndexEntries() {
        if (!seeChilds.isEmpty()) {
            Collection collection = seeChilds.values();
	    	return (IndexEntry[]) collection.toArray(new IndexEntry[collection.size()]);
        } else return null;
    }

    public IndexEntry[] getSeeAlsoChildIndexEntries() {
        if (!seeAlsoChilds.isEmpty()) {
            Collection collection = seeAlsoChilds.values();
            return (IndexEntry[]) collection.toArray(new IndexEntry[collection.size()]);
        } else return null;
    }

    public String toString() {
        String result = "";
        result+=getValue();
        if (this.getSoValue() != null && this.getSoValue().length() > 0) {
            result+="<so>"+getSoValue();
        }
        if (this.isSuppressesThePageNumber()) {
            result+="<$nopage>";
        }
        if (this.isRestoresPageNumber()) {
            result+="<$singlepage>";
        }
        if (this.isStartingRange()) {
            result+="<$startrange>";
        }
        if (this.isEndingRange()) {
            result+="<$endrange>";
        }
        if (this.getSortString() != null && this.getSortString().length() > 0) {
            result+="["+getSortString()+"]";
        }
        return result;
    }

}
