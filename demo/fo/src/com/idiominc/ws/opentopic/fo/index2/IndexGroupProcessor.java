package com.idiominc.ws.opentopic.fo.index2;

import com.ibm.icu.text.Collator;
import com.idiominc.ws.opentopic.fo.index2.configuration.ConfigEntry;
import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

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
public class IndexGroupProcessor {
	/**
	 * Puts index entries to the group they are belongs
	 *
	 * @param theIndexEntries       index entries
	 * @param theIndexConfiguration index configuration
	 * @param theLocale             locale used to sort and compare index entries
	 * @return groups with sorted index entries inside
	 */
	public static IndexGroup[] process(IndexEntry[] theIndexEntries, IndexConfiguration theIndexConfiguration,
									   Locale theLocale) {
		final IndexCollator collator = new IndexCollator(theLocale);

		ArrayList result = new ArrayList();

		final ConfigEntry[] entries = theIndexConfiguration.getEntries();

		HashMap indexMap = createMap(theIndexEntries);

        //Creating array of index groups
        for (int i = 0; i < entries.length; i++) {
            ConfigEntry configEntry = entries[i];
            final String label = configEntry.getLabel();
            MyIndexGroup group = new MyIndexGroup(label,configEntry);
            result.add(group);
        }
        MyIndexGroup[] IndexGroups = (MyIndexGroup[]) result.toArray(new MyIndexGroup[result.size()]);

        //Adding dependecies to group array
        for (int i = 0; i < IndexGroups.length; i++) {
            MyIndexGroup thisGroup = IndexGroups[i];
            String[] thisGroupMembers = thisGroup.getConfigEntry().getGroupMembers();
            for (int j = 0; j < IndexGroups.length; j++) {
                if (j != i) {
                    MyIndexGroup compGroup = IndexGroups[j];
                    String[] compGroupMembers = compGroup.getConfigEntry().getGroupMembers();
                    if (doesStart(compGroupMembers, thisGroupMembers)) {
                        thisGroup.addChild(compGroup);
                    }
                }
            }
        }

/*
        for (int i = 0; i < IndexGroups.length; i++) {
            IndexGroups[i].printDebug();
        }
*/

		for (int i = 0; i < IndexGroups.length; i++) {
            MyIndexGroup group = IndexGroups[i];
			ConfigEntry configEntry = group.getConfigEntry();

			final String[] groupMembers = configEntry.getGroupMembers();

			if (groupMembers.length > 0) {
				//Find entries by comaping first letter with a chars in current config entry
				final Set set = indexMap.keySet();
				final Object[] keys = (Object[]) set.toArray(new Object[set.size()]);
				for (int j = 0; j < keys.length; j++) {
					String key = (String) keys[j];
					if (key.length() > 0) {
						String value = getValue((IndexEntry) indexMap.get(key));
//						final char c = value.charAt(0);
						if (configEntry.isInRange(value,collator)) {
							final IndexEntry entry = (IndexEntry) indexMap.remove(key);
							group.addEntry(entry);
						}
					}
				}
			} else {
				//Get index entries by range specified by two keys
				final String key1 = configEntry.getKey();
				String key2 = null;
				if ((i + 1) < entries.length) {
					final ConfigEntry nextEntry = entries[i + 1];
					key2 = nextEntry.getKey();
				}
				String[] indexMapKeys = getIndexKeysOfIndexesInRange(key1, key2, collator, indexMap);

				for (int j = 0; j < indexMapKeys.length; j++) {
					//Add entry to the group
					String mapKey = indexMapKeys[j];
					final IndexEntry entry = (IndexEntry) indexMap.remove(mapKey);
					group.addEntry(entry);
				}
			}
/*
			if (group.getEntries().length > 0) {
				result.add(group);
			}
*/
		}

        if (!indexMap.isEmpty()) {
            final Set set = indexMap.keySet();
            final Object[] keys = (Object[]) set.toArray(new Object[set.size()]);
            for (int j = 0; j < keys.length; j++) {
                String key = (String) keys[j];
                if (key.length() > 0) {
                    final IndexEntry entry = (IndexEntry) indexMap.get(key);
                    System.out.println("[ERROR] Index entry '"+entry.toString()+"' is dropped, because corresponding group is not found");
                }
            }
            if (IndexPreprocessorTask.failOnError) {
                System.out.println("[ERROR] Build stopped. Problems occured during Index preprocess task. Please check the messages above.");
                IndexPreprocessorTask.processingFaild=true;
            }
        }

        ArrayList cleanResult = new ArrayList();
        for (int i = 0; i < IndexGroups.length; i++) {
            if (IndexGroups[i].getEntries().length > 0) {
                cleanResult.add(IndexGroups[i]);
            }
        }
        MyIndexGroup[] cleanIndexGroups = (MyIndexGroup[]) cleanResult.toArray(new MyIndexGroup[cleanResult.size()]);
		return (IndexGroup[]) cleanIndexGroups;
	}


	private static String getValue(IndexEntry theEntry) {
		//The so-value has higher priority
		final String soValue = theEntry.getSoValue();
		final String sortValue = theEntry.getSortString();
		if (sortValue != null && sortValue.length() > 0) {
			return sortValue;
		} else if (soValue != null && soValue.length() > 0) {
			return soValue;
		} else return theEntry.getValue();
	}


	private static String[] getIndexKeysOfIndexesInRange(String theKey1, String theKey2, IndexCollator theCollator, HashMap theIndexEntryMap) {
		final Set set = theIndexEntryMap.keySet();
		final String[] allKeys = (String[]) set.toArray(new String[0]);


		ArrayList res = new ArrayList();
		for (int i = 0; i < allKeys.length; i++) {
			String key = allKeys[i];
			String value = getValue((IndexEntry) theIndexEntryMap.get(key));
			final int res1 = theCollator.compare(theKey1, value);
			if (res1 <= 0) {
				if (theKey2 == null) {
					//the right range is not specified
					res.add(key);
					continue;
				}
				final int res2 = theCollator.compare(theKey2, key);
				if (res2 > 0) {
					res.add(key);
				}
			}
		}
		return (String[]) res.toArray(new String[res.size()]);
	}


	private static boolean doesStart(String sourceString, String[] compStrings) {
		for (int i = 0; i < compStrings.length; i++) {
            if (sourceString.startsWith(compStrings[i])) {
                return true;
            }
		}
		return false;
	}

	private static boolean doesStart(String[] sourceStrings, String[] compStrings) {
        for (int i = 0; i < sourceStrings.length; i++) {
            String sourceString = sourceStrings[i];
            for (int j = 0; j < compStrings.length; j++) {
                if (sourceString.startsWith(compStrings[j]) && !sourceString.equals(compStrings[j])) {
                    return true;
                }
            }
        }
		return false;
	}


	private static HashMap createMap(IndexEntry[] theIndexEntries) {
		HashMap map = new HashMap();
		for (int i = 0; i < theIndexEntries.length; i++) {
			IndexEntry indexEntry = theIndexEntries[i];
			String value = indexEntry.getValue();

			if (!map.containsKey(value)) {
				map.put(value, indexEntry);
			} else {
				final IndexEntry existingEntry = (IndexEntry) map.get(value);
				final IndexEntry[] childIndexEntries = indexEntry.getChildIndexEntries();
				for (int j = 0; j < childIndexEntries.length; j++) {
					IndexEntry childIndexEntry = childIndexEntries[j];
					existingEntry.addChild(childIndexEntry);
				}
				final IndexEntry[] seeChildIndexEntries = indexEntry.getSeeChildIndexEntries();
				for (int j = 0;seeChildIndexEntries != null && j < seeChildIndexEntries.length; j++) {
					IndexEntry seeChildIndexEntry = seeChildIndexEntries[j];
					existingEntry.addSeeChild(seeChildIndexEntry);
				}
				final IndexEntry[] seeAlsoChildIndexEntries = indexEntry.getSeeAlsoChildIndexEntries();
				for (int j = 0;seeAlsoChildIndexEntries != null && j < seeAlsoChildIndexEntries.length; j++) {
					IndexEntry seeAlsoChildIndexEntry = seeAlsoChildIndexEntries[j];
					existingEntry.addSeeAlsoChild(seeAlsoChildIndexEntry);
				}
				//supress some attributes of given entry to the existing one
				if (indexEntry.isRestoresPageNumber()) {
					existingEntry.setRestoresPageNumber(true);
				}
				final String[] refIDs = indexEntry.getRefIDs();
				if (refIDs.length > 0) {
					for (int j = 0; j < refIDs.length; j++) {
						String refID = refIDs[j];
						existingEntry.addRefID(refID);
					}
				}
				if (!indexEntry.isSuppressesThePageNumber()) {
					existingEntry.setSuppressesThePageNumber(false);
				}
				if (indexEntry.isStartingRange()) {
					existingEntry.setStartRange(true);
				}
			}
		}
		return map;
	}


	private static class MyIndexGroup
			implements IndexGroup {
		private String label;
        private ConfigEntry configEntry;
		private ArrayList entries = new ArrayList();
        private ArrayList childList = new ArrayList();

		public MyIndexGroup(String theLabel, ConfigEntry theConfigEntry) {
			this.label = theLabel;
            this.configEntry = theConfigEntry;
		}

		public String getLabel() {
			return this.label;
		}


		public IndexEntry[] getEntries() {
			return (IndexEntry[]) entries.toArray(new IndexEntry[entries.size()]);
		}

        public ConfigEntry getConfigEntry() {
            return this.configEntry;
        }

		public void addEntry(IndexEntry theEntry) {
            boolean isInserted = false;
            if (!childList.isEmpty()) {
//                MyIndexGroup[] childGroupList = (MyIndexGroup[]) childList.toArray(new MyIndexGroup[childList.size()]);
                for (int i = 0; i < childList.size() && !isInserted;i++) {
                    MyIndexGroup thisChild = (MyIndexGroup) childList.get(i);
                    String[] thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                    if (doesStart(theEntry.getValue(),thisGroupMembers)) {
                        thisChild.addEntry(theEntry);
                        isInserted = true;
                    }
                }
            }
            if (!isInserted) this.entries.add(theEntry);
		}

        public void addChild(MyIndexGroup theIndexGroup) {
            if (!this.childList.contains(theIndexGroup))
                this.childList.add(theIndexGroup);
//            MyIndexGroup[] childGroupList = (MyIndexGroup[]) childList.toArray(new MyIndexGroup[childList.size()]);
            for (int i = 0; i < childList.size(); i++) {
                MyIndexGroup thisChild = (MyIndexGroup) childList.get(i);
                for (int j = 0; j < childList.size(); j++) {
                    if (i != j) {
                        MyIndexGroup compChild = (MyIndexGroup) childList.get(j);
                        String[] thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                        String[] compGroupMembers = compChild.getConfigEntry().getGroupMembers();
                        if (doesStart(thisGroupMembers, compGroupMembers)) {
                            this.childList.remove(thisChild);
                            compChild.addChild(thisChild);
                        }
                    }
                }
            }
        }

        public void removeChild(MyIndexGroup theIndexGroup) {
            this.childList.remove(theIndexGroup);
        }

	}
}
