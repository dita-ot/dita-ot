package com.idiominc.ws.opentopic.fo.index2;

import com.ibm.icu.text.Collator;

import java.util.Comparator;
import java.util.Locale;

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
class IndexComparator implements Comparator<IndexEntry> {

    private final IndexCollator Collator;


    public IndexComparator(final Locale theLocale) {
        this.Collator = new IndexCollator(theLocale);
    }


    public int compare(final IndexEntry o1, final IndexEntry o2) {
        final String value1 = getSortString(o1);
        final String value2 = getSortString(o2);

        return this.Collator.compare(value1,value2);
    }


    private String getSortString(final IndexEntry theEntry1) {
        String result;
        if (theEntry1.getSortString() != null) {
            result = theEntry1.getSortString();
        } else if (theEntry1.getSoValue() != null) {
            result = theEntry1.getSoValue();
        } else {
            result = theEntry1.getValue();
        }
        return result;
    }
}
