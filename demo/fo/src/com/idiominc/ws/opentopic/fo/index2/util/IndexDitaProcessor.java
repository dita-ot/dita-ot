package com.idiominc.ws.opentopic.fo.index2.util;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/*
Copyright © 2004-2006 by Idiom Technologies, Inc. All rights reserved. 
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
public abstract class IndexDitaProcessor {
    private static String elIndexName = "indexterm";
	private static String elIndexSortName = "index-sort-as";
	private static String elIndexSeeName = "index-see";
	private static String elIndexSeeAlsoName = "index-see-also";
    private static String elIndexRangeStartName = "index-range-start";
	private static String elIndexRangeEndName = "index-range-end";
    private static final String SO = "<so>";
    private static final String LT = "<";
    private static final String GT = ">";

    public static IndexEntry[] processIndexDitaNode(Node theNode, String theParentValue) {

        final NodeList childNodes = theNode.getChildNodes();
        StringBuffer textValueBuffer = new StringBuffer();;
        StringBuffer sortStringBuffer = new StringBuffer();
        boolean startRange = false;
        boolean endRange = false;
        ArrayList childEntrys = new ArrayList();
        ArrayList seeEntry = new ArrayList();
        ArrayList seeAlsoEntry = new ArrayList();

        for (int i = 0; i < childNodes.getLength(); i++) { //Go through child nodes to find text nodes
            final Node child = childNodes.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                final String val = child.getNodeValue();
                if (null != val) {
                    textValueBuffer.append(val);
                }
            } else if (elIndexName.equals(child.getNodeName())) {

                String currentTextValue = normalizeTextValue(textValueBuffer.toString());
                String currentRefId;
                if (currentTextValue.equals("")) currentRefId="";
                else currentRefId = currentTextValue+":";
                IndexEntry[] childs = processIndexDitaNode(child,theParentValue+currentRefId);
                for (int j = 0;j < childs.length;j++)
                    childEntrys.add(childs[j]);

            } else if (elIndexSortName.equals(child.getNodeName())) {

                for (int j = 0; j < child.getChildNodes().getLength(); j++) {
                    final Node sortChildNode = child.getChildNodes().item(j);
                    if (sortChildNode.getNodeType() == Node.TEXT_NODE) {
                        String text = sortChildNode.getNodeValue();
                        if (text != null) sortStringBuffer.append(text);
                    }
                }
            } else if (elIndexRangeStartName.equals(child.getNodeName())) {
                startRange = true;
            } else if (elIndexRangeEndName.equals(child.getNodeName())) {
                endRange = true;
            } else if (elIndexSeeName.equals(child.getNodeName())) {
                IndexEntry[] childs = processIndexDitaNode(child,"");
                for (int j = 0;j < childs.length;j++)
                    seeEntry.add(childs[j]);
            } else if (elIndexSeeAlsoName.equals(child.getNodeName())) {
                IndexEntry[] childs = processIndexDitaNode(child,"");
                for (int j = 0;j < childs.length;j++)
                    seeAlsoEntry.add(childs[j]);
            }

        }
        String textValue = normalizeTextValue(textValueBuffer.toString());
        String sortString = sortStringBuffer.toString();
        IndexEntry result = createIndexEntry(textValue,sortString);
        if (result.getValue().length() > 0) {
            result.setStartRange(startRange);
            result.setEndsRange(endRange);
            result.addRefID(normalizeTextValue(theParentValue + textValue + ":"));
            if (!seeEntry.isEmpty()) {
                for (int j = 0; j < seeEntry.size(); j++) {
                    IndexEntry seeIndexEntry = (IndexEntry) seeEntry.get(j);
                    result.addSeeChild(seeIndexEntry);
                }
                result.setSuppressesThePageNumber(true);
            }
            if (!seeAlsoEntry.isEmpty()) {
                for (int j = 0; j < seeAlsoEntry.size(); j++) {
                    IndexEntry seeAlsoIndexEntry = (IndexEntry) seeAlsoEntry.get(j);
                    result.addSeeAlsoChild(seeAlsoIndexEntry);
                }
            }
            for (int i = 0; i < childEntrys.size(); i++) {
                IndexEntry child = (IndexEntry) childEntrys.get(i);
                result.addChild(child);
            }
            IndexEntry[] resultArray = new IndexEntry[1];
            resultArray[0] = result;
            return resultArray;
        } else return (IndexEntry[]) childEntrys.toArray(new IndexEntry[childEntrys.size()]);
    }

    private static IndexEntry createIndexEntry(String theValue, String theSortString) {
        String soString;
        int soIdxOf = theValue.indexOf(SO);
        if (soIdxOf > 0) {
            soString = theValue.substring(soIdxOf + SO.length());
            theValue = theValue.substring(0, soIdxOf);
        } else {
            soString = null;
        }

        String strippedFormatting = stripFormatting(theValue);

        IndexEntryImpl indexEntry = new IndexEntryImpl(strippedFormatting, soString, theSortString, theValue);
        if (!theSortString.equals("")) indexEntry.setSortString(theSortString);
            else indexEntry.setSortString(null);
        return indexEntry;
    }

    private static String stripFormatting(String theValue) {
        int ltPos = theValue.indexOf(LT);
        int gtPos = theValue.indexOf(GT);
        if ((ltPos == -1) && (gtPos == -1)) {
            return theValue;
        } else if (ltPos == -1 || gtPos == -1 || (ltPos > gtPos)) {
            System.err.println("Possibly bad formatting in string \"" + theValue + "\"");
            return theValue;
        }
        String value = theValue.substring(0, ltPos) + theValue.substring(gtPos + 1);
        return stripFormatting(value);
    }

    public static String normalizeTextValue(final String theString) {
        if (null != theString && theString.length() > 0) {
            String res = theString.replaceAll("[\\s\\n]+", " ");
            res = res.replaceAll("[\\s]+$", ""); //replace in the end of string
            res = res.replaceAll("[\\s]+:", ":"); //replace spaces before ':'
            return res.replaceAll(":[\\s]+", ":"); //replace spaces after ':'
        }
        return theString;
    }


}
