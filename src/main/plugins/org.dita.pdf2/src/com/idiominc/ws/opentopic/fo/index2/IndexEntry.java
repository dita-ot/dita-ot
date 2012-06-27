package com.idiominc.ws.opentopic.fo.index2;

import java.util.List;

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
     * Get index term markup content.
     * 
     * @return DITA markup content, {@code null} if not available
     */
    public List<Node> getContents();


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


    void addSeeChild(IndexEntry theEntry);

    void addSeeAlsoChild(IndexEntry theEntry);

    public IndexEntry[] getSeeChildIndexEntries();

    public IndexEntry[] getSeeAlsoChildIndexEntries();
}
