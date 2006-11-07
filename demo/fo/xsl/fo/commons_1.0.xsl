<?xml version='1.0'?>

<!--
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
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    xmlns:exsl="http://exslt.org/common"
    xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
    xmlns:exslf="http://exslt.org/functions"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    extension-element-prefixes="exsl"
    exclude-result-prefixes="opentopic exsl opentopic-index exslf opentopic-func"
    version="1.1">


    <xsl:include href="../../cfg/fo/attrs/commons-attr.xsl"/>
    <xsl:include href="../../cfg/fo/attrs/lists-attr.xsl"/>

    <!-- BS: Template owerwrited to define new topic types (List's),
    to create special processing for any of list you should use <template name="processUnknowTopic"/>
    example below.-->
    <xsl:template name="determineTopicType">
        <xsl:variable name="id" select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/@id"/>
        <xsl:variable name="gid" select="generate-id(ancestor-or-self::*[contains(@class, ' topic/topic ')][1])"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="mapTopic">
            <xsl:copy-of select="$map//*[@id = $id]"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/chapter ')]">
                <xsl:text>topicChapter</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/appendix ')]">
                <xsl:text>topicAppendix</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/preface ')]">
                <xsl:text>topicPreface</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/part ')]">
                <xsl:text>topicPart</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/abbrevlist ')]">
                <xsl:text>topicAbbrevList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/bibliolist ')]">
                <xsl:text>topicBiblioList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/booklist ')]">
                <xsl:text>topicBookList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/figurelist ')]">
                <xsl:text>topicFigureList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/indexlist ')]">
                <xsl:text>topicIndexList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/toc ')]">
                <xsl:text>topicTocList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/glossarylist ')]">
                <xsl:text>topicGlossaryList</xsl:text>
            </xsl:when>
            <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/trademarklist ')]">
                <xsl:text>topicTradeMarkList</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>topicSimple</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processUnknowTopic">
        <xsl:param name="topicType"/>
        <xsl:choose>
            <xsl:when test="$topicType = 'topicTocList'">
                <xsl:call-template name="processTocList"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicIndexList'">
                <xsl:call-template name="processIndexList"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="not(ancestor::*[contains(@class,' topic/topic ')])">
                        <fo:page-sequence master-reference="{$page-sequence-reference}" xsl:use-attribute-sets="__force__page__count">
                            <xsl:call-template name="insertBodyStaticContents"/>
                            <fo:flow flow-name="xsl-region-body">
                                <xsl:call-template name="processTopic"/>
                            </fo:flow>
                        </fo:page-sequence>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="processTopic"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/data ')]">
        <xsl:apply-templates/>
    </xsl:template>

    <exslf:function name="opentopic-func:determineTopicType">
        <xsl:variable name="id" select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/@id"/>
        <xsl:variable name="gid" select="generate-id(ancestor-or-self::*[contains(@class, ' topic/topic ')][1])"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="mapTopic">
            <xsl:copy-of select="$map//*[@id = $id]"/>
        </xsl:variable>

        <xsl:variable name="topicType">
            <xsl:choose>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/chapter ')]">
                    <xsl:text>topicChapter</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/appendix ')]">
                    <xsl:text>topicAppendix</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/preface ')]">
                    <xsl:text>topicPreface</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/part ')]">
                    <xsl:text>topicPart</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/abbrevlist ')]">
                    <xsl:text>topicAbbrevList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/bibliolist ')]">
                    <xsl:text>topicBiblioList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/booklist ')]">
                    <xsl:text>topicBookList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/figurelist ')]">
                    <xsl:text>topicFigureList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/indexlist ')]">
                    <xsl:text>topicIndexList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/toc ')]">
                    <xsl:text>topicTocList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/glossarylist ')]">
                    <xsl:text>topicGlossaryList</xsl:text>
                </xsl:when>
                <xsl:when test="$mapTopic/*[position() = $topicNumber][contains(@class, ' bookmap/trademarklist ')]">
                    <xsl:text>topicTradeMarkList</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>topicSimple</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <exslf:result select="$topicType"/>
    </exslf:function>


</xsl:stylesheet>