package com.idiominc.ws.opentopic.fo.index2.configuration;

import com.idiominc.ws.opentopic.fo.index2.IndexCollator;

import java.util.ArrayList;


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
 */class ConfigEntryImpl
 implements ConfigEntry {
     private final String label;
     private final String key;
     private final String[] members;
     private CharRange[] ranges = new CharRange[0];



     public ConfigEntryImpl(final String theLabel, final String theKey, final String[] theMembers) {
         this.label = theLabel;
         this.key = theKey;
         this.members = theMembers;
     }

     public void addRange(final CharRange range) {
         final ArrayList<CharRange> rangeList = new ArrayList<CharRange>();
         for (final CharRange range2 : ranges) {
             rangeList.add(range2);
         }
         rangeList.add(range);
         ranges = (CharRange[]) rangeList.toArray(new CharRange[rangeList.size()]);
     }


     public String getLabel() {
         return this.label;
     }


     public String getKey() {
         return this.key;
     }

     public String[] getGroupMembers() {
         return this.members;
     }

     public boolean isInRange(final String value, final IndexCollator collator) {
         if (value.length() > 0) {
             for (final String member : members) {
                 if (value.startsWith(member) || member.startsWith(value)) {
                     return true;
                 }
             }
             for (final CharRange range : ranges) {
                 if (range.isInRange(value, collator)) {
                     return true;
                 }
             }
         }
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }

 }
