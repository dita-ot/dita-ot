package com.idiominc.ws.opentopic.fo.index2.util;

import static org.dita.dost.util.Constants.*;
import static com.idiominc.ws.opentopic.fo.index2.IndexPreprocessor.*;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;

import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

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
public final class IndexDitaProcessor {
    
    private static String elIndexRangeStartName = "start";
    private static String elIndexRangeEndName = "end";
    private static final String SO = "<so>";
    private static final String LT = "<";
    private static final String GT = ">";
    private static final String sortStart = "[";
    private static final String sortEnd = "]"; 
    
    private DITAOTLogger logger;
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Read index terms from source XML.
     * 
     * @param theNode source indexterm element
     * @param theParentValue parent value
     * @return index entries
     */
    public IndexEntry[] processIndexDitaNode(final Node theNode, final String theParentValue) {

        final NodeList childNodes = theNode.getChildNodes();
        final StringBuffer textValueBuffer = new StringBuffer();
        final List<Node> contents = new ArrayList<Node>();
        final StringBuffer sortStringBuffer = new StringBuffer();
        final boolean startRange = theNode.getAttributes().getNamedItem(elIndexRangeStartName) != null;
        final boolean endRange = theNode.getAttributes().getNamedItem(elIndexRangeEndName) != null;
        final ArrayList<IndexEntry> childEntrys = new ArrayList<IndexEntry>();
        final ArrayList<IndexEntry> seeEntry = new ArrayList<IndexEntry>();
        final ArrayList<IndexEntry> seeAlsoEntry = new ArrayList<IndexEntry>();

        for (int i = 0; i < childNodes.getLength(); i++) { //Go through child nodes to find text nodes
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                contents.add(child);
                final String val = child.getNodeValue();
                if (null != val) {
                    textValueBuffer.append(val);
                }
            } else if (TOPIC_INDEXTERM.matches(child)) {

                final String currentTextValue = normalizeTextValue(textValueBuffer.toString());
                String currentRefId;
                if (currentTextValue.equals("")) {
                    currentRefId="";
                } else {
                    currentRefId = currentTextValue + VALUE_SEPARATOR;
                }
                final IndexEntry[] childs = processIndexDitaNode(child,theParentValue+currentRefId);
                for (final IndexEntry child2 : childs) {
                    childEntrys.add(child2);
                }

            } else if (INDEXING_D_INDEX_SORT_AS.matches(child)) {

                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    final Node sortChildNode = child.getChildNodes().item(j);
                    if (sortChildNode.getNodeType() == Node.TEXT_NODE) {
                        final String text = sortChildNode.getNodeValue();
                        if (text != null) {
                            sortStringBuffer.append(text);
                        }
                    }
                }
            } else if (INDEXING_D_INDEX_SEE.matches(child)) {
                final IndexEntry[] childs = processIndexDitaNode(child,"");
                for (final IndexEntry child2 : childs) {
                    seeEntry.add(child2);
                }
            } else if (INDEXING_D_INDEX_SEE_ALSO.matches(child)) {
                final IndexEntry[] childs = processIndexDitaNode(child,"");
                for (final IndexEntry child2 : childs) {
                    seeAlsoEntry.add(child2);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                contents.add(child);
                textValueBuffer.append(XMLUtils.getStringValue((Element) child));
            }

        }
        /*
        if (normalizeTextValue(textValueBuffer.toString()).length() == 0) {
            if (startRange) {
                textValueBuffer.append(theNode.getAttributes().getNamedItem(elIndexRangeStartName).getNodeValue());
            } else if (endRange) {
                textValueBuffer.append(theNode.getAttributes().getNamedItem(elIndexRangeEndName).getNodeValue());
            }
        }
         */
        String textValue = normalizeTextValue(textValueBuffer.toString());
        String sortString = sortStringBuffer.toString();
        if (textValue.indexOf(sortStart) > -1 && textValue.indexOf(sortEnd) > -1 && sortString.length() == 0) {
            if (textValue.indexOf(sortStart) < textValue.indexOf(sortEnd)) {
                sortString = textValue.substring(textValue.indexOf(sortStart)+1,textValue.indexOf(sortEnd));
                textValue = textValue.substring(0,textValue.indexOf(sortStart));
            }
        }

        if (!childEntrys.isEmpty() && !seeEntry.isEmpty()) {
            for (final IndexEntry e: seeEntry) {
                final Properties prop = new Properties();
                prop.put("%1", e.getFormattedString());
                prop.put("%2", textValue);
                logger.logWarn(MessageUtils.getMessage("DOTA067W", prop).toString());
            }
            seeEntry.clear();
        }
        if (!childEntrys.isEmpty() && !seeAlsoEntry.isEmpty()) {
            for (final IndexEntry e: seeAlsoEntry) {
                final Properties prop = new Properties();
                prop.put("%1", e.getFormattedString());
                prop.put("%2", textValue);
                logger.logWarn(MessageUtils.getMessage("DOTA068W", prop).toString());
            }
            seeAlsoEntry.clear();
        }
        
        final IndexEntry result = createIndexEntry(contents, textValue,sortString);
        if (result.getValue().length() > 0 || endRange || startRange) {
            result.setStartRange(startRange);
            result.setEndsRange(endRange);
            if (startRange) {
                result.addRefID(theNode.getAttributes().getNamedItem(elIndexRangeStartName).getNodeValue());
            } else if (endRange) {
                result.addRefID(theNode.getAttributes().getNamedItem(elIndexRangeEndName).getNodeValue());
            } else {
                result.addRefID(normalizeTextValue(theParentValue + textValue + VALUE_SEPARATOR));
            }
            if (!seeEntry.isEmpty()) {
                for (int j = 0; j < seeEntry.size(); j++) {
                    final IndexEntry seeIndexEntry = seeEntry.get(j);
                    result.addSeeChild(seeIndexEntry);
                }
                result.setSuppressesThePageNumber(true);
            }
            if (!seeAlsoEntry.isEmpty()) {
                for (int j = 0; j < seeAlsoEntry.size(); j++) {
                    final IndexEntry seeAlsoIndexEntry = seeAlsoEntry.get(j);
                    result.addSeeAlsoChild(seeAlsoIndexEntry);
                }
            }
            for (int i = 0; i < childEntrys.size(); i++) {
                final IndexEntry child = childEntrys.get(i);
                result.addChild(child);
            }
            final IndexEntry[] resultArray = new IndexEntry[1];
            resultArray[0] = result;
            return resultArray;
        } else {
            return (IndexEntry[]) childEntrys.toArray(new IndexEntry[childEntrys.size()]);
        }
    }

    private static IndexEntry createIndexEntry(final List<Node> contents, String theValue, final String theSortString) {
        String soString;
        final int soIdxOf = theValue.indexOf(SO);
        if (soIdxOf > 0 && USES_FRAME_MARKUP) {
            soString = theValue.substring(soIdxOf + SO.length());
            theValue = theValue.substring(0, soIdxOf);
        } else {
            soString = null;
        }

        final String strippedFormatting = USES_FRAME_MARKUP ? stripFormatting(theValue) : theValue;

        final IndexEntryImpl indexEntry = new IndexEntryImpl(strippedFormatting, soString, theSortString, theValue, contents);
        if (!theSortString.equals("")) {
            indexEntry.setSortString(theSortString);
        } else {
            indexEntry.setSortString(null);
        }
        return indexEntry;
    }

    private static String stripFormatting(final String theValue) {
        final int ltPos = theValue.indexOf(LT);
        final int gtPos = theValue.indexOf(GT);
        if ((ltPos == -1) && (gtPos == -1)) {
            return theValue;
        } else if (ltPos == -1 || gtPos == -1 || (ltPos > gtPos)) {
            System.err.println("Possibly bad formatting in string \"" + theValue + "\"");
            return theValue;
        }
        final String value = theValue.substring(0, ltPos) + theValue.substring(gtPos + 1);
        return stripFormatting(value);
    }

    public static String normalizeTextValue(final String theString) {
        if (null != theString && theString.length() > 0) {
            String res = theString.replaceAll("[\\s\\n]+", " ").trim();
            res = res.replaceAll("[\\s]+$", ""); //replace in the end of string
            if (!USES_FRAME_MARKUP) {
                return res;
            }
            res = res.replaceAll("[\\s]+:", ":"); //replace spaces before ':'
            return res.replaceAll(":[\\s]+", ":"); //replace spaces after ':'
        }
        return theString;
    }


}
