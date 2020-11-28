package com.idiominc.ws.opentopic.fo.index2.util;

import static com.idiominc.ws.opentopic.fo.index2.IndexPreprocessor.*;

import com.idiominc.ws.opentopic.fo.index2.IndexEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dita.dost.util.Configuration;
import org.w3c.dom.Node;

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
public abstract class IndexStringProcessor {

    /**
     * Parse the index marker string and create IndexEntry object from one.
     *
     * @param theIndexMarkerString index marker string
     * @param contents IndexPreprocessorTask instance
     * @return IndexEntry objects created from the index string
     */
    public static IndexEntry[] processIndexString(final String theIndexMarkerString, final List<Node> contents) {
        final IndexEntryImpl indexEntry = createIndexEntry(theIndexMarkerString, contents, null, false);
        final StringBuffer referenceIDBuf = new StringBuffer();
        referenceIDBuf.append(indexEntry.getValue());
        referenceIDBuf.append(VALUE_SEPARATOR);
        indexEntry.addRefID(referenceIDBuf.toString());

        return new IndexEntry[] { indexEntry };
    }


    /**
     * Method equals to the normalize-space xslt function
     *
     * @param theString string to normalize
     * @return normalized string
     */
    public static String normalizeTextValue(final String theString) {
        if (null != theString && theString.length() > 0) {
            return theString.replaceAll("[\\s\\n]+", " ").trim();
        }
        return theString;
    }


    private static IndexEntryImpl createIndexEntry(String theValue, final List<Node> contents, final String theSortString, final boolean theIsParentNoPage) {
        final boolean suppressesThePageNumber = theIsParentNoPage;
        final boolean restoresPageNumber = false;
        final boolean startsRange = false;
        final boolean endsRange = false;
        final String strippedFormatting = theValue;

        final IndexEntryImpl indexEntry = new IndexEntryImpl(strippedFormatting, theSortString, theValue, contents);
        indexEntry.setSuppressesThePageNumber(suppressesThePageNumber);
        indexEntry.setRestoresPageNumber(restoresPageNumber);
        indexEntry.setStartRange(startsRange);
        indexEntry.setEndsRange(endsRange);
        indexEntry.setSortString(theSortString);
        return indexEntry;
    }

}
