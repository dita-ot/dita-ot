<?xml version='1.0'?>

<!--
Copyright ? 2004-2006 by Idiom Technologies, Inc. All rights reserved.
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
    version="2.0">

    <!-- BS: Template owerwrited to define new topic types (List's),
    to create special processing for any of list you should use <template name="processUnknowTopic"/>
    example below.-->
    <!-- RDA: Modified with RFE 2882109. Can now modify results or add new types by matching an element
              with mode="determineTopicType", without overriding the entire determineTopicType template. -->
    <xsl:template name="determineTopicType">
      <xsl:variable name="foundTopicType">
        <xsl:variable name="topic" select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]"/>
        <xsl:variable name="id" select="$topic/@id"/>
        <xsl:variable name="mapTopics" select="key('map-id', $id)"/>
        <xsl:choose>
          <xsl:when test="count($mapTopics) = 1">
            <xsl:apply-templates select="$mapTopics[1]" mode="determineTopicType"/>
          </xsl:when>
          <!-- topicmerge.xsl should already rewrite all duplicate IDs, keep for backwards compatibility -->
          <xsl:when test="count($mapTopics) > 1">
            <xsl:variable name="gid" select="generate-id($topic)"/>
            <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
            <xsl:variable name="mapTopic">
              <xsl:copy-of select="$mapTopics"/>
            </xsl:variable>
            <xsl:apply-templates select="$mapTopic/*[position() = $topicNumber]" mode="determineTopicType"/>
          </xsl:when>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$foundTopicType!=''">
          <xsl:value-of select="$foundTopicType"/>
        </xsl:when>
        <xsl:otherwise>topicSimple</xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="determineTopicType">
        <!-- Default, when not matching a bookmap type, is topicSimple -->
        <xsl:text>topicSimple</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/chapter ')]" mode="determineTopicType">
        <xsl:text>topicChapter</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/appendix ')]" mode="determineTopicType">
        <xsl:text>topicAppendix</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/preface ')]" mode="determineTopicType">
        <xsl:text>topicPreface</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/part ')]" mode="determineTopicType">
        <xsl:text>topicPart</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/abbrevlist ')]" mode="determineTopicType">
        <xsl:text>topicAbbrevList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/bibliolist ')]" mode="determineTopicType">
        <xsl:text>topicBiblioList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/booklist ')]" mode="determineTopicType">
        <xsl:text>topicBookList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/figurelist ')]" mode="determineTopicType">
        <xsl:text>topicFigureList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/indexlist ')]" mode="determineTopicType">
        <xsl:text>topicIndexList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/toc ')]" mode="determineTopicType">
        <xsl:text>topicTocList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/glossarylist ')]" mode="determineTopicType">
        <xsl:text>topicGlossaryList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/trademarklist ')]" mode="determineTopicType">
        <xsl:text>topicTradeMarkList</xsl:text>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' bookmap/notices ')]" mode="determineTopicType">
        <xsl:text>topicNotices</xsl:text>
    </xsl:template>


    <xsl:template name="processUnknowTopic">
        <xsl:param name="topicType"/>
        <xsl:apply-templates select="." mode="processUnknowTopic">
            <xsl:with-param name="topicType" select="$topicType"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="*" mode="processUnknowTopic">
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
                        <xsl:variable name="page-sequence-reference">
                            <xsl:choose>
                                <xsl:when test="$mapType = 'bookmap'">
                                    <xsl:value-of select="'body-sequence'"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="'ditamap-body-sequence'"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>

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
    <!-- edited by William on 2009-09-18 for output bug #2860168 start-->
    <!--xsl:template match="*[contains(@class, ' topic/data ')]">
        <xsl:apply-templates/>
      </xsl:template-->
    <xsl:template match="*[contains(@class, ' topic/data ')]"/>
    <xsl:template match="*[contains(@class, ' topic/data ')]" mode="insert-text"/>
    <xsl:template match="*[contains(@class, ' topic/data-about ')]"/>
    <!-- edited by William on 2009-09-18 for output bug #2860168 end-->

    <exslf:function name="opentopic-func:determineTopicType">
      <xsl:variable name="topicType">
        <xsl:call-template name="determineTopicType"/>
      </xsl:variable>
      <exslf:result select="string($topicType)"/>
    </exslf:function>
    
</xsl:stylesheet>