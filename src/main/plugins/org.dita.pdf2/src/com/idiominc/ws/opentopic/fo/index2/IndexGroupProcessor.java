package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.configuration.ConfigEntry;
import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;

/*
Copyright (c) 2004-2006 by Idiom Technologies, Inc. All rights reserved.
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

This file is part of the DITA Open Toolkit project.
See the accompanying LICENSE file for applicable license.
 */
/**
 * @deprecated since 3.4
 */
@Deprecated
public final class IndexGroupProcessor {
    
    private DITAOTLogger logger;

    public static final String SPECIAL_CHARACTER_GROUP_KEY = "Specials";
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Puts index entries to the group they are belongs
     *
     * @param theIndexEntries       index entries
     * @param theIndexConfiguration index configuration
     * @param theLocale             locale used to sort and compare index entries
     * @return groups with sorted index entries inside
     */
    public IndexGroup[] process(final IndexEntry[] theIndexEntries, final IndexConfiguration theIndexConfiguration,
            final Locale theLocale) {
        final IndexCollator collator = new IndexCollator(theLocale);

        final ArrayList<MyIndexGroup> result = new ArrayList<>();

        final ConfigEntry[] entries = theIndexConfiguration.getEntries();

        final HashMap<String, IndexEntry> indexMap = createMap(theIndexEntries);

        //Creating array of index groups
        for (final ConfigEntry configEntry : entries) {
            final String label = configEntry.getLabel();
            final MyIndexGroup group = new MyIndexGroup(label,configEntry);
            result.add(group);
        }
        final MyIndexGroup[] IndexGroups = result.toArray(new MyIndexGroup[0]);

        //Adding dependecies to group array
        for (int i = 0; i < IndexGroups.length; i++) {
            final MyIndexGroup thisGroup = IndexGroups[i];
            final String[] thisGroupMembers = thisGroup.getConfigEntry().getGroupMembers();
            for (int j = 0; j < IndexGroups.length; j++) {
                if (j != i) {
                    final MyIndexGroup compGroup = IndexGroups[j];
                    final String[] compGroupMembers = compGroup.getConfigEntry().getGroupMembers();
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
            final MyIndexGroup group = IndexGroups[i];
            final ConfigEntry configEntry = group.getConfigEntry();

            final String[] groupMembers = configEntry.getGroupMembers();

            if (groupMembers.length > 0) {
                //Find entries by comaping first letter with a chars in current config entry
                for (final String key : new ArrayList<>(indexMap.keySet())) {
                    if (key.length() > 0) {
                        final String value = getValue(indexMap.get(key));
                        //                        final char c = value.charAt(0);
                        if (configEntry.isInRange(value,collator)) {
                            final IndexEntry entry = indexMap.remove(key);
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
                final String[] indexMapKeys = getIndexKeysOfIndexesInRange(key1, key2, collator, indexMap);

                for (final String mapKey : indexMapKeys) {
                    final IndexEntry entry = indexMap.remove(mapKey);
                    group.addEntry(entry);
                }
            }
            /*
            if (group.getEntries().length > 0) {
                result.add(group);
            }
             */
        }

        //If some terms remain uncategorized, and a recognized special character
        //group is available, place remaining terms in that group
        for (final MyIndexGroup group : IndexGroups) {
            final ConfigEntry configEntry = group.getConfigEntry();
            final String configKey = configEntry.getKey();
            if (configKey.equals(SPECIAL_CHARACTER_GROUP_KEY)) {
                for (final String key : new ArrayList<>(indexMap.keySet())) {
                    if (key.length() > 0) {
                        final String value = getValue(indexMap.get(key));
                        //                        final char c = value.charAt(0);
                        logger.info(MessageUtils.getMessage("PDFJ003I", value).toString());
                        final IndexEntry entry = indexMap.remove(key);
                        group.addEntry(entry);
                    }
                }
            }
        }

        //No recognized "Special characters" group; uncategorized terms have no place to go, must be dropped
        if (!indexMap.isEmpty()) {
            for (final String key : new ArrayList<>(indexMap.keySet())) {
                if (key.length() > 0) {
                    final IndexEntry entry = indexMap.get(key);
                    logger.error(MessageUtils.getMessage("PDFJ001E", entry.toString()).toString());
                }
            }
            if (IndexPreprocessorTask.failOnError) {
                logger.error(MessageUtils.getMessage("PDFJ002E").toString());
                IndexPreprocessorTask.processingFaild=true;
            }
        }

        final ArrayList<MyIndexGroup> cleanResult = new ArrayList<>();
        for (final MyIndexGroup indexGroup : IndexGroups) {
            if (indexGroup.getEntries().length > 0) {
                cleanResult.add(indexGroup);
            }
        }
        return cleanResult.toArray(new MyIndexGroup[0]);
    }


    private static String getValue(final IndexEntry theEntry) {
        final String sortValue = theEntry.getSortString();
        if (sortValue != null && sortValue.length() > 0) {
            return sortValue;
        } else {
            return theEntry.getValue();
        }
    }


    private static String[] getIndexKeysOfIndexesInRange(final String theKey1, final String theKey2, final IndexCollator theCollator, final HashMap<String, IndexEntry> theIndexEntryMap) {
        final ArrayList<String> res = new ArrayList<>();
        for (final Map.Entry<String, IndexEntry> e : theIndexEntryMap.entrySet()) {
            final int res1 = theCollator.compare(theKey1, getValue(e.getValue()));
            if (res1 <= 0) {
                if (theKey2 == null) {
                    //the right range is not specified
                    res.add(e.getKey());
                    continue;
                }
                final int res2 = theCollator.compare(theKey2, e.getKey());
                if (res2 > 0) {
                    res.add(e.getKey());
                }
            }
        }
        return res.toArray(new String[0]);
    }


    private static boolean doesStart(final String sourceString, final String[] compStrings) {
        for (final String compString : compStrings) {
            if (sourceString.startsWith(compString)) {
                return true;
            }
        }
        return false;
    }

    private static boolean doesStart(final String[] sourceStrings, final String[] compStrings) {
        for (final String sourceString2 : sourceStrings) {
            for (String compString : compStrings) {
                if (sourceString2.startsWith(compString) && !sourceString2.equals(compString)) {
                    return true;
                }
            }
        }
        return false;
    }


    private static HashMap<String, IndexEntry> createMap(final IndexEntry[] theIndexEntries) {
        final HashMap<String, IndexEntry> map = new HashMap<>();
        for (final IndexEntry theIndexEntrie : theIndexEntries) {
            final String value = theIndexEntrie.getValue();

            if (!map.containsKey(value)) {
                map.put(value, theIndexEntrie);
            } else {
                final IndexEntry existingEntry = map.get(value);
                final IndexEntry[] childIndexEntries = theIndexEntrie.getChildIndexEntries();
                for (final IndexEntry childIndexEntry : childIndexEntries) {
                    existingEntry.addChild(childIndexEntry);
                }
                final IndexEntry[] seeChildIndexEntries = theIndexEntrie.getSeeChildIndexEntries();
                for (int j = 0;seeChildIndexEntries != null && j < seeChildIndexEntries.length; j++) {
                    final IndexEntry seeChildIndexEntry = seeChildIndexEntries[j];
                    existingEntry.addSeeChild(seeChildIndexEntry);
                }
                final IndexEntry[] seeAlsoChildIndexEntries = theIndexEntrie.getSeeAlsoChildIndexEntries();
                for (int j = 0;seeAlsoChildIndexEntries != null && j < seeAlsoChildIndexEntries.length; j++) {
                    final IndexEntry seeAlsoChildIndexEntry = seeAlsoChildIndexEntries[j];
                    existingEntry.addSeeAlsoChild(seeAlsoChildIndexEntry);
                }
                //supress some attributes of given entry to the existing one
                if (theIndexEntrie.isRestoresPageNumber()) {
                    existingEntry.setRestoresPageNumber(true);
                }
                final String[] refIDs = theIndexEntrie.getRefIDs();
                for (final String refID : refIDs) {
                    existingEntry.addRefID(refID);
                }
                if (!theIndexEntrie.isSuppressesThePageNumber()) {
                    existingEntry.setSuppressesThePageNumber(false);
                }
                if (theIndexEntrie.isStartingRange()) {
                    existingEntry.setStartRange(true);
                }
            }
        }
        return map;
    }


    private static class MyIndexGroup
    implements IndexGroup {
        private final String label;
        private final ConfigEntry configEntry;
        private final ArrayList<IndexEntry> entries = new ArrayList<>();
        private final ArrayList<MyIndexGroup> childList = new ArrayList<>();

        public MyIndexGroup(final String theLabel, final ConfigEntry theConfigEntry) {
            this.label = theLabel;
            this.configEntry = theConfigEntry;
        }

        public String getLabel() {
            return this.label;
        }


        public IndexEntry[] getEntries() {
            return entries.toArray(new IndexEntry[0]);
        }

        public ConfigEntry getConfigEntry() {
            return this.configEntry;
        }

        public void addEntry(final IndexEntry theEntry) {
            boolean isInserted = false;
            if (!childList.isEmpty()) {
                //                MyIndexGroup[] childGroupList = (MyIndexGroup[]) childList.toArray(new MyIndexGroup[childList.size()]);
                for (int i = 0; i < childList.size() && !isInserted;i++) {
                    final MyIndexGroup thisChild = childList.get(i);
                    final String[] thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                    if (doesStart(theEntry.getValue(),thisGroupMembers)) {
                        thisChild.addEntry(theEntry);
                        isInserted = true;
                    }
                }
            }
            if (!isInserted) {
                this.entries.add(theEntry);
            }
        }

        public void addChild(final MyIndexGroup theIndexGroup) {
            if (!this.childList.contains(theIndexGroup)) {
                this.childList.add(theIndexGroup);
            }
            //            MyIndexGroup[] childGroupList = (MyIndexGroup[]) childList.toArray(new MyIndexGroup[childList.size()]);
            for (int i = 0; i < childList.size(); i++) {
                final MyIndexGroup thisChild = childList.get(i);
                for (int j = 0; j < childList.size(); j++) {
                    if (i != j) {
                        final MyIndexGroup compChild = childList.get(j);
                        final String[] thisGroupMembers = thisChild.getConfigEntry().getGroupMembers();
                        final String[] compGroupMembers = compChild.getConfigEntry().getGroupMembers();
                        if (doesStart(thisGroupMembers, compGroupMembers)) {
                            this.childList.remove(thisChild);
                            compChild.addChild(thisChild);
                        }
                    }
                }
            }
        }

        public void removeChild(final MyIndexGroup theIndexGroup) {
            this.childList.remove(theIndexGroup);
        }

    }
}
