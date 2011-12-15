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
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    xmlns:exsl="http://exslt.org/common"
    xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
    extension-element-prefixes="exsl"
    exclude-result-prefixes="opentopic exsl opentopic-index dita2xslfo"
    version="2.0">

    <xsl:key name="map-id" match="opentopic:map//*[@id]" use="@id"/>

    <xsl:variable name="msgprefix" select="'PDFX'"/>
  
    <xsl:variable name="id.toc" select="'ID_TOC_00-0F-EA-40-0D-4D'"/>
    <xsl:variable name="id.index" select="'ID_INDEX_00-0F-EA-40-0D-4D'"/>
    <xsl:variable name="id.lot" select="'ID_LOT_00-0F-EA-40-0D-4D'"/>
    <xsl:variable name="id.lof" select="'ID_LOF_00-0F-EA-40-0D-4D'"/>
    <xsl:variable name="id.glossary" select="'ID_GLOSSARY_00-0F-EA-40-0D-4D'"/>

    <!--  In order to not process any data under opentopic:map  -->
    <xsl:template match="opentopic:map"/>
    
    <!-- added by William on 2009-07-07 for bug:2815492 start -->
    <!-- get the max chars for shortdesc-->
    <xsl:variable name="maxCharsInShortDesc">
        <xsl:call-template name="getMaxCharsForShortdescKeep"/>
    </xsl:variable>
    <!-- added by William on 2009-07-07 for bug:2815492 end -->
    
    <xsl:template match="*[@conref]" priority="99">
        <fo:block xsl:use-attribute-sets="__unresolved__conref">
            <xsl:call-template name="insertReferenceTitle">
                <xsl:with-param name="href" select="@conref"/>
                <xsl:with-param name="titlePrefix" select="'Content-Reference'"/>
            </xsl:call-template>
        </fo:block>
    </xsl:template>

    <!-- note that bkinfo provides information for a cover in a bookmap application,
     hence bkinfo does not need to be instanced.  In an application that processes
     only maps, bkinfo should process as a topic based on default processing. -->
    <xsl:template match="*[contains(@class,' bkinfo/bkinfo ')]" priority="2">
        <!-- no operation for bkinfo -->
    </xsl:template>

    <!-- RDA: Modified with RFE 2882109. Can now modify results or add new types by matching an element
              with mode="determineTopicType", without overriding the entire determineTopicType template. -->
    <xsl:template name="determineTopicType">
        <xsl:variable name="id" select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/@id"/>
        <xsl:variable name="gid" select="generate-id(ancestor-or-self::*[contains(@class, ' topic/topic ')][1])"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="mapTopic">
            <xsl:copy-of select="$map//*[@id = $id]"/>
        </xsl:variable>
        <xsl:variable name="foundTopicType">
            <xsl:apply-templates select="$mapTopic/*[position() = $topicNumber]" mode="determineTopicType"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$foundTopicType!=''"><xsl:value-of select="$foundTopicType"/></xsl:when>
            <xsl:otherwise>topicSimple</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="determineTopicType">
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
    <xsl:template match="*[contains(@class, ' bookmap/abstract ')]" mode="determineTopicType">
        <xsl:text>topicAbstract</xsl:text>
    </xsl:template>

    <xsl:template name="startPageNumbering">
        <!--BS: uncomment if you need reset page numbering at first chapter-->
<!--
        <xsl:variable name="id" select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/@id"/>
        <xsl:variable name="gid" select="generate-id(ancestor-or-self::*[contains(@class, ' topic/topic ')][1])"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="mapTopic" select="($map//*[@id = $id])[position() = $topicNumber]"/>

        <xsl:if test="not(($mapTopic/preceding::*[contains(@class, ' bookmap/chapter ') or contains(@class, ' bookmap/part ')])
            or ($mapTopic/ancestor::*[contains(@class, ' bookmap/chapter ') or contains(@class, ' bookmap/part ')]))">
            <xsl:attribute name="initial-page-number">1</xsl:attribute>
        </xsl:if>
-->

    </xsl:template>

    <xsl:template name="getTopicrefShortdesc">
        <xsl:variable name="id" select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/@id"/>
        <xsl:variable name="gid" select="generate-id(ancestor-or-self::*[contains(@class, ' topic/topic ')][1])"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="mapTopic">
            <xsl:copy-of select="$map//*[@id = $id]"/>
        </xsl:variable>

        <xsl:if test="$mapTopic/*[position() = $topicNumber]/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]">
            <xsl:copy-of select="$mapTopic/*[position() = $topicNumber]/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]"/>
        </xsl:if>
    </xsl:template>

    <xsl:template name="processTopic">
        <xsl:apply-templates select="." mode="processTopic"/>
    </xsl:template>
    <xsl:template match="*" mode="processTopic">
        <fo:block xsl:use-attribute-sets="topic">
            <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                <fo:marker marker-class-name="current-topic-number">
                    <xsl:number format="1"/>
                </fo:marker>
            </xsl:if>
            <xsl:apply-templates select="." mode="commonTopicProcessing"/>
		</fo:block>
    </xsl:template>

    <xsl:template match="*" mode="commonTopicProcessing">
        <xsl:variable name="topicrefShortdesc">
            <xsl:call-template name="getTopicrefShortdesc"/>
        </xsl:variable>
        <xsl:apply-templates select="*[contains(@class, ' topic/title ')]"/>
        <xsl:apply-templates select="*[contains(@class, ' topic/prolog ')]"/>
        
        <xsl:choose>
            <!-- When topic has an abstract, we cannot override shortdesc -->
            <xsl:when test="*[contains(@class, ' topic/abstract ')]">
              <xsl:apply-templates select="*[not(contains(@class, ' topic/title ')) and
                    not(contains(@class, ' topic/prolog ')) and
                    not(contains(@class, ' topic/shortdesc ')) and
                    not(contains(@class, ' topic/topic '))]"/>
            </xsl:when>
            <xsl:when test="$topicrefShortdesc/*">
                <xsl:apply-templates select="$topicrefShortdesc/*"/>
                <xsl:apply-templates select="*[not(contains(@class, ' topic/title ')) and
                    not(contains(@class, ' topic/prolog ')) and
                    not(contains(@class, ' topic/shortdesc ')) and
                    not(contains(@class, ' topic/topic '))]"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="*[not(contains(@class, ' topic/title ')) and
                    not(contains(@class, ' topic/prolog ')) and
                    not(contains(@class, ' topic/topic '))]"/>
            </xsl:otherwise>
        </xsl:choose>
        
        <xsl:call-template name="buildRelationships"/>
        <xsl:apply-templates select="*[contains(@class,' topic/topic ')]"/>
        <xsl:apply-templates select="." mode="topicEpilog"/>
    </xsl:template>

    <xsl:template match="*" mode="topicEpilog">
      <!-- Hook that allows common end-of-topic processing (after nested topics).
           See SourceForge RFE 2928584: Add general model for end-of-topic processing in PDF -->
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
    </xsl:template>

    <!-- RDA: From RFE 2882109, combining this rule with existing rules for
              concept, task, reference later in this file to reduce duplicated
              code. Continue calling the named process* templates in order to
              ensure backwards compatibility; ideally though, a single template would
              be called for all types, deferring the override decision to match rules. -->
    <xsl:template match="*[contains(@class, ' topic/topic ')]">
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$topicType = 'topicChapter'">
                <xsl:call-template name="processTopicChapter"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicAppendix'">
                <xsl:call-template name="processTopicAppendix"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicPart'">
                <xsl:call-template name="processTopicPart"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicPreface'">
                <xsl:call-template name="processTopicPreface"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicNotices'">
                <xsl:if test="$retain-bookmap-order">
                  <xsl:call-template name="processTopicNotices"/>
                </xsl:if>
            </xsl:when>
            <xsl:when test="$topicType = 'topicSimple'">
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
                <xsl:choose>
                    <xsl:when test="not(ancestor::*[contains(@class,' topic/topic ')])">
                        <fo:page-sequence master-reference="{$page-sequence-reference}" xsl:use-attribute-sets="__force__page__count">
                            <xsl:call-template name="insertBodyStaticContents"/>
                            <fo:flow flow-name="xsl-region-body">
                                <xsl:choose>
                                    <xsl:when test="contains(@class,' concept/concept ')"><xsl:call-template name="processConcept"/></xsl:when>
                                    <xsl:when test="contains(@class,' task/task ')"><xsl:call-template name="processTask"/></xsl:when>
                                    <xsl:when test="contains(@class,' reference/reference ')"><xsl:call-template name="processReference"/></xsl:when>
                                    <xsl:otherwise><xsl:call-template name="processTopic"/></xsl:otherwise>
                                </xsl:choose>
                            </fo:flow>
                        </fo:page-sequence>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="contains(@class,' concept/concept ')"><xsl:call-template name="processConcept"/></xsl:when>
                            <xsl:when test="contains(@class,' task/task ')"><xsl:call-template name="processTask"/></xsl:when>
                            <xsl:when test="contains(@class,' reference/reference ')"><xsl:call-template name="processReference"/></xsl:when>
                            <xsl:otherwise><xsl:call-template name="processTopic"/></xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
			<!--BS: skipp abstract (copyright) from usual content. It will be processed from the front-matter-->
			<xsl:when test="$topicType = 'topicAbstract'"/>
			<xsl:otherwise>
                <xsl:call-template name="processUnknowTopic">
                    <xsl:with-param name="topicType" select="$topicType"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--  Bookmap Chapter processing  -->
    <xsl:template name="processTopicChapter">
        <fo:page-sequence master-reference="body-sequence" xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="startPageNumbering"/>
            <xsl:call-template name="insertBodyStaticContents"/>
            <fo:flow flow-name="xsl-region-body">
                <fo:block xsl:use-attribute-sets="topic">
                    <xsl:call-template name="commonattributes"/>
                    <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                        <fo:marker marker-class-name="current-topic-number">
                            <xsl:number format="1"/>
                        </fo:marker>
                        <fo:marker marker-class-name="current-header">
                            <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                                <xsl:call-template name="getTitle"/>
                            </xsl:for-each>
                        </fo:marker>
                    </xsl:if>

                    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]"/>

                    <xsl:call-template name="insertChapterFirstpageStaticContent">
                        <xsl:with-param name="type" select="'chapter'"/>
                    </xsl:call-template>

                    <fo:block xsl:use-attribute-sets="topic.title">
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 start-->
                        <xsl:call-template name="pullPrologIndexTerms"/>
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 end-->
                        
                        <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                            <xsl:call-template name="getTitle"/>
                        </xsl:for-each>
                    </fo:block>

                    <xsl:choose>
                      <xsl:when test="$chapterLayout='BASIC'">
                          <xsl:apply-templates select="*[not(contains(@class, ' topic/topic ') or contains(@class, ' topic/title ') or
                                                             contains(@class, ' topic/prolog '))]"/>
                          <xsl:call-template name="buildRelationships"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:call-template name="createMiniToc"/>
                      </xsl:otherwise>
                    </xsl:choose>

                    <xsl:apply-templates select="*[contains(@class,' topic/topic ')]"/>
                </fo:block>
            </fo:flow>
        </fo:page-sequence>
    </xsl:template>

    <!--  Bookmap Appendix processing  -->
    <xsl:template name="processTopicAppendix">
        <fo:page-sequence master-reference="body-sequence" xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="insertBodyStaticContents"/>
            <fo:flow flow-name="xsl-region-body">
                <fo:block xsl:use-attribute-sets="topic">
                    <xsl:call-template name="commonattributes"/>
                    <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                        <fo:marker marker-class-name="current-topic-number">
                            <xsl:number format="1"/>
                        </fo:marker>
                        <fo:marker marker-class-name="current-header">
                            <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                                <xsl:call-template name="getTitle"/>
                            </xsl:for-each>
                        </fo:marker>
                    </xsl:if>

                    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]"/>

                    <xsl:call-template name="insertChapterFirstpageStaticContent">
                        <xsl:with-param name="type" select="'appendix'"/>
                    </xsl:call-template>

                    <fo:block xsl:use-attribute-sets="topic.title">
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 start-->
                        <xsl:call-template name="pullPrologIndexTerms"/>
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 end-->
                        <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                            <xsl:call-template name="getTitle"/>
                        </xsl:for-each>
                    </fo:block>

                    <xsl:choose>
                      <xsl:when test="$appendixLayout='BASIC'">
                          <xsl:apply-templates select="*[not(contains(@class, ' topic/topic ') or contains(@class, ' topic/title ') or
                                                             contains(@class, ' topic/prolog '))]"/>
                          <xsl:call-template name="buildRelationships"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:call-template name="createMiniToc"/>
                      </xsl:otherwise>
                    </xsl:choose>

                    <xsl:apply-templates select="*[contains(@class,' topic/topic ')]"/>
                </fo:block>
            </fo:flow>
        </fo:page-sequence>
    </xsl:template>

    <!--  Bookmap Part processing  -->
    <xsl:template name="processTopicPart">
        <fo:page-sequence master-reference="body-sequence" xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="startPageNumbering"/>
            <xsl:call-template name="insertBodyStaticContents"/>
            <fo:flow flow-name="xsl-region-body">
                <fo:block xsl:use-attribute-sets="topic">
                    <xsl:call-template name="commonattributes"/>
                    <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                        <fo:marker marker-class-name="current-topic-number">
                            <xsl:number format="I"/>
                        </fo:marker>
                        <fo:marker marker-class-name="current-header">
                            <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                                <xsl:call-template name="getTitle"/>
                            </xsl:for-each>
                        </fo:marker>
                    </xsl:if>

                    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]"/>

                    <xsl:call-template name="insertChapterFirstpageStaticContent">
                        <xsl:with-param name="type" select="'part'"/>
                    </xsl:call-template>

                    <fo:block xsl:use-attribute-sets="topic.title">
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 start-->
                        <xsl:call-template name="pullPrologIndexTerms"/>
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 end-->
                        <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                            <xsl:call-template name="getTitle"/>
                        </xsl:for-each>
                    </fo:block>

                    <xsl:choose>
                      <xsl:when test="$partLayout='BASIC'">
                          <xsl:apply-templates select="*[not(contains(@class, ' topic/topic ') or contains(@class, ' topic/title ') or
                                                             contains(@class, ' topic/prolog '))]"/>
                          <xsl:call-template name="buildRelationships"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:call-template name="createMiniToc"/>
                      </xsl:otherwise>
                    </xsl:choose>

<!--                    <xsl:apply-templates select="*[not(contains(@class, ' topic/topic '))]"/>-->

                    <xsl:for-each select="*[contains(@class,' topic/topic ')]">
                        <xsl:variable name="topicType">
                            <xsl:call-template name="determineTopicType"/>
                        </xsl:variable>
                        <xsl:if test="$topicType = 'topicSimple'">
                            <xsl:apply-templates select="."/>
                        </xsl:if>
                    </xsl:for-each>
                </fo:block>
            </fo:flow>
        </fo:page-sequence>
        <xsl:for-each select="*[contains(@class,' topic/topic ')]">
            <xsl:variable name="topicType">
                <xsl:call-template name="determineTopicType"/>
            </xsl:variable>
            <xsl:if test="not($topicType = 'topicSimple')">
                <xsl:apply-templates select="."/>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="processTopicNotices">
        <fo:page-sequence master-reference="body-sequence" xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="insertBodyStaticContents"/>
            <fo:flow flow-name="xsl-region-body">
                <fo:block xsl:use-attribute-sets="topic">
                    <xsl:call-template name="commonattributes"/>
                    <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                        <fo:marker marker-class-name="current-topic-number">
                            <xsl:number format="1"/>
                        </fo:marker>
                        <fo:marker marker-class-name="current-header">
                            <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                                <xsl:call-template name="getTitle"/>
                            </xsl:for-each>
                        </fo:marker>
                    </xsl:if>

                    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]"/>

                    <xsl:call-template name="insertChapterFirstpageStaticContent">
                        <xsl:with-param name="type" select="'notices'"/>
                    </xsl:call-template>

                    <fo:block xsl:use-attribute-sets="topic.title">
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 start-->
                        <xsl:call-template name="pullPrologIndexTerms"/>
                        <!-- added by William on 2009-07-02 for indexterm bug:2815485 end-->
                        <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                            <xsl:call-template name="getTitle"/>
                        </xsl:for-each>
                    </fo:block>

                    <xsl:choose>
                      <xsl:when test="$noticesLayout='BASIC'">
                          <xsl:apply-templates select="*[not(contains(@class, ' topic/topic ') or contains(@class, ' topic/title ') or
                                                             contains(@class, ' topic/prolog '))]"/>
                          <xsl:call-template name="buildRelationships"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:call-template name="createMiniToc"/>
                      </xsl:otherwise>
                    </xsl:choose>

                    <xsl:apply-templates select="*[contains(@class,' topic/topic ')]"/>
                </fo:block>
            </fo:flow>
        </fo:page-sequence>
   </xsl:template>


    <xsl:template name="insertChapterFirstpageStaticContent">
        <xsl:param name="type"/>
        <fo:block>
            <xsl:attribute name="id">
                <xsl:call-template name="generate-toc-id"/>
            </xsl:attribute>
            <xsl:choose>
                <xsl:when test="$type = 'chapter'">
                    <fo:block xsl:use-attribute-sets="__chapter__frontmatter__name__container">
                        <xsl:call-template name="insertVariable">
                            <xsl:with-param name="theVariableID" select="'Chapter with number'"/>
                            <xsl:with-param name="theParameters">
                                <number>
                                    <fo:block xsl:use-attribute-sets="__chapter__frontmatter__number__container">
                                        <xsl:for-each select="key('map-id', @id)[1]">
                                          <xsl:number format="1" count="*[contains(@class, ' bookmap/chapter ')]"/>
                                        </xsl:for-each>
                                    </fo:block>
                                </number>
                            </xsl:with-param>
                        </xsl:call-template>
                    </fo:block>
                </xsl:when>
                <xsl:when test="$type = 'appendix'">
                        <fo:block xsl:use-attribute-sets="__chapter__frontmatter__name__container">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Appendix with number'"/>
                                <xsl:with-param name="theParameters">
                                    <number>
                                        <fo:block xsl:use-attribute-sets="__chapter__frontmatter__number__container">
                                            <xsl:for-each select="key('map-id', @id)[1]">
                                              <xsl:number format="A" count="*[contains(@class, ' bookmap/appendix ')]"/>
                                            </xsl:for-each>
                                        </fo:block>
                                    </number>
                                </xsl:with-param>
                            </xsl:call-template>
                        </fo:block>
                </xsl:when>

                <xsl:when test="$type = 'part'">
                        <fo:block xsl:use-attribute-sets="__chapter__frontmatter__name__container">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Part with number'"/>
                                <xsl:with-param name="theParameters">
                                    <number>
                                        <fo:block xsl:use-attribute-sets="__chapter__frontmatter__number__container">
                                            <xsl:for-each select="key('map-id', @id)[1]">
                                              <xsl:number format="I" count="*[contains(@class, ' bookmap/part ')]"/>
                                            </xsl:for-each>
                                        </fo:block>
                                    </number>
                                </xsl:with-param>
                            </xsl:call-template>
                        </fo:block>
                </xsl:when>
                <xsl:when test="$type = 'preface'">
                        <fo:block xsl:use-attribute-sets="__chapter__frontmatter__name__container">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Preface title'"/>
                            </xsl:call-template>
                        </fo:block>
                </xsl:when>
                <xsl:when test="$type = 'notices'">
                        <fo:block xsl:use-attribute-sets="__chapter__frontmatter__name__container">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Notices title'"/>
                            </xsl:call-template>
                        </fo:block>
                </xsl:when>
            </xsl:choose>
        </fo:block>
    </xsl:template>

    <xsl:template name="createMiniToc">
        <xsl:apply-templates select="." mode="createMiniToc"/>
    </xsl:template>
    <xsl:template match="*" mode="createMiniToc">
        <fo:table xsl:use-attribute-sets="__toc__mini__table">
            <fo:table-column xsl:use-attribute-sets="__toc__mini__table__column_1"/>
            <fo:table-column xsl:use-attribute-sets="__toc__mini__table__column_2"/>
            <fo:table-body xsl:use-attribute-sets="__toc__mini__table__body">
                <fo:table-row>
                    <fo:table-cell>
                        <fo:block xsl:use-attribute-sets="__toc__mini">
                            <xsl:if test="*[contains(@class, ' topic/topic ')]">
                                <fo:block xsl:use-attribute-sets="__toc__mini__header">
                                    <xsl:call-template name="insertVariable">
                                        <xsl:with-param name="theVariableID" select="'Mini Toc'"/>
                                    </xsl:call-template>
                                </fo:block>
                                <fo:list-block xsl:use-attribute-sets="__toc__mini__list">
                                    <xsl:apply-templates select="*[contains(@class, ' topic/topic ')]" mode="in-this-chapter-list"/>
                                </fo:list-block>
                            </xsl:if>
                        </fo:block>
                    </fo:table-cell>
                    <fo:table-cell xsl:use-attribute-sets="__toc__mini__summary">
                        <!--Really, it would be better to just apply-templates, but the attribute sets for shortdesc, body
                        and abstract might indent the text.  Here, the topic body is in a table cell, and should
                        not be indented, so each element is handled specially.-->
                        <fo:block>
                            <xsl:apply-templates select="*[contains(@class,' topic/titlealts ')]"/>
                            <xsl:if test="*[contains(@class,' topic/shortdesc ')
                                  or contains(@class, ' topic/abstract ')]/node()">
                              <fo:block xsl:use-attribute-sets="p">
                                <xsl:apply-templates select="*[contains(@class,' topic/shortdesc ')
                                  or contains(@class, ' topic/abstract ')]/node()"/>
                              </fo:block>
                            </xsl:if>
                            <xsl:apply-templates select="*[contains(@class,' topic/body ')]/*"/>

                            <!-- Added with RFE 2976463 to fix dropped links in topics with a mini-TOC. -->
                            <xsl:if test="*[contains(@class,' topic/related-links ')]//
                                          *[contains(@class,' topic/link ')][not(@role) or @role!='child']">
                                <xsl:apply-templates select="*[contains(@class,' topic/related-links ')]"/>
                            </xsl:if>

						</fo:block>
                    </fo:table-cell>
                </fo:table-row>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="in-this-chapter-list">
        <fo:list-item xsl:use-attribute-sets="ul.li">
            <fo:list-item-label xsl:use-attribute-sets="ul.li__label">
                <fo:block xsl:use-attribute-sets="ul.li__label__content">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Unordered List bullet'"/>
                    </xsl:call-template>
                </fo:block>
            </fo:list-item-label>

            <fo:list-item-body xsl:use-attribute-sets="ul.li__body">
                <fo:block xsl:use-attribute-sets="ul.li__content">
                    <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                        <xsl:value-of select="child::*[contains(@class, ' topic/title ')]"/>
                    </fo:basic-link>
                </fo:block>
            </fo:list-item-body>
        </fo:list-item>
    </xsl:template>


    <!-- h[n] -->
    <xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>
        <xsl:choose>
            <!--  Disable chapter title processing when mini TOC is created -->
            <xsl:when test="(topicType = 'topicChapter') or (topicType = 'topicAppendix')" />
            <!--   Normal processing         -->
            <xsl:otherwise>
                <xsl:call-template name="processTopicTitle"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processTopicTitle">
        <xsl:apply-templates select="." mode="processTopicTitle"/>
    </xsl:template>
    <xsl:template match="*" mode="processTopicTitle">
        <xsl:variable name="level" select="count(ancestor::*[contains(@class,' topic/topic ')])"/>
        <xsl:variable name="attrSet1">
            <xsl:call-template name="createTopicAttrsName">
                <xsl:with-param name="theCounter" select="$level"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="attrSet2" select="concat($attrSet1, '__content')"/>
        <fo:block>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="$attrSet1"/>
                <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
            </xsl:call-template>
            <fo:block>
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="$attrSet2"/>
                    <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
                </xsl:call-template>
                <xsl:if test="$level = 1">
                    <fo:marker marker-class-name="current-header">
                        <xsl:call-template name="getTitle"/>
                    </fo:marker>
                </xsl:if>
                <xsl:if test="$level = 2">
                    <fo:marker marker-class-name="current-h2">
                        <xsl:call-template name="getTitle"/>
                    </fo:marker>
                </xsl:if>
                <fo:inline id="{parent::node()/@id}"/>
                <fo:inline>
                    <xsl:attribute name="id">
                        <xsl:call-template name="generate-toc-id">
                            <xsl:with-param name="element" select=".."/>
                        </xsl:call-template>
                    </xsl:attribute>
                </fo:inline>
                <!-- added by William on 2009-07-02 for indexterm bug:2815485 start-->
                <xsl:call-template name="pullPrologIndexTerms"/>
                <!-- added by William on 2009-07-02 for indexterm bug:2815485 end-->
                <xsl:call-template name="getTitle"/>
            </fo:block>
        </fo:block>
    </xsl:template>

    <xsl:template name="createTopicAttrsName">
        <xsl:param name="theCounter"/>
        <xsl:param name="theName" select="''"/>
        <xsl:apply-templates select="." mode="createTopicAttrsName">
          <xsl:with-param name="theCounter" select="$theCounter"/>
          <xsl:with-param name="theName" select="$theName"/>
        </xsl:apply-templates>
    </xsl:template>
    <xsl:template match="*" mode="createTopicAttrsName">
      <xsl:param name="theCounter"/>
      <xsl:param name="theName" select="''"/>
        <xsl:choose>
            <xsl:when test="number($theCounter) > 0">
                <xsl:call-template name="createTopicAttrsName">
                    <xsl:with-param name="theCounter" select="number($theCounter) - 1"/>
                    <xsl:with-param name="theName" select="concat($theName, 'topic.')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="concat($theName, 'title')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/section ')]/*[contains(@class,' topic/title ')]">
        <fo:block xsl:use-attribute-sets="section.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="getTitle"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]">
        <fo:block xsl:use-attribute-sets="example.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]">
        <fo:block xsl:use-attribute-sets="fig.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="'Figure'"/>
                <xsl:with-param name="theParameters">
                    <number>
                        <xsl:number level="any" count="*[contains(@class, ' topic/fig ')][child::*[contains(@class, ' topic/title ')]]" from="/"/>
                    </number>
                    <title>
                        <xsl:apply-templates/>
                    </title>
                </xsl:with-param>
            </xsl:call-template>
        </fo:block>
    </xsl:template>

    <!-- The following three template matches are based on class attributes
         that do not exist. They have been commented out starting with
         the DITA-OT 1.5 release, with SourceForge tracker #2882085. -->
    <!--<xsl:template match="*[contains(@class, ' topic/dita ')]">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' topic/topichead ')]">
        <fo:block xsl:use-attribute-sets="topichead" id="{@id}">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>
    <xsl:template match="*[contains(@class, ' topic/topicgroup ')]">
        <fo:block xsl:use-attribute-sets="topicgroup" id="{@id}">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>-->

    <xsl:template match="*[contains(@class, ' topic/topicmeta ')]">
<!--
        <fo:block xsl:use-attribute-sets="topicmeta">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tm ')]">
        <fo:inline xsl:use-attribute-sets="tm">
            <xsl:apply-templates/>
            <xsl:choose>
                <xsl:when test="@tmtype='service'">
                    <fo:inline xsl:use-attribute-sets="tm__content__service">SM</fo:inline>
                </xsl:when>
                <xsl:when test="@tmtype='tm'">
                    <fo:inline xsl:use-attribute-sets="tm__content">&#8482;</fo:inline>
                </xsl:when>
                <xsl:when test="@tmtype='reg'">
                    <fo:inline xsl:use-attribute-sets="tm__content">&#174;</fo:inline>
                </xsl:when>
                <xsl:otherwise>
                    <fo:inline xsl:use-attribute-sets="tm__content"><xsl:text>Error in tm type.</xsl:text></fo:inline>
                </xsl:otherwise>
            </xsl:choose>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/term ')]">
        <fo:inline xsl:use-attribute-sets="term">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/author ')]">
<!--
        <fo:block xsl:use-attribute-sets="author">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/source ')]">
<!--
        <fo:block xsl:use-attribute-sets="source">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>


    <xsl:template match="*[contains(@class, ' topic/publisher ')]">
<!--
        <fo:block xsl:use-attribute-sets="publisher">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/copyright ')]">
<!--
        <fo:block xsl:use-attribute-sets="copyright">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/copyryear ')]">
<!--
        <fo:block xsl:use-attribute-sets="copyryear">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/copyrholder ')]">
<!--
        <fo:block xsl:use-attribute-sets="copyrholder">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/critdates ')]">
<!--
        <fo:block xsl:use-attribute-sets="critdates">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/created ')]">
<!--
        <fo:block xsl:use-attribute-sets="created">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/revised ')]">
<!--
        <fo:block xsl:use-attribute-sets="revised">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/permissions ')]">
<!--
        <fo:block xsl:use-attribute-sets="permissions">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/category ')]">
<!--
        <fo:block xsl:use-attribute-sets="category">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/audience ')]">
<!--
        <fo:block xsl:use-attribute-sets="audience">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/keywords ')]">
<!--
        <fo:block xsl:use-attribute-sets="keywords">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/prodinfo ')]">
<!--
        <fo:block xsl:use-attribute-sets="prodinfo">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/prodname ')]">
<!--
        <fo:block xsl:use-attribute-sets="prodname">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/vrmlist ')]">
<!--
        <fo:block xsl:use-attribute-sets="vrmlist">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/vrm ')]">
<!--
        <fo:block xsl:use-attribute-sets="vrm">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/brand ')]">
<!--
        <fo:block xsl:use-attribute-sets="brand">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/series ')]">
<!--
        <fo:block xsl:use-attribute-sets="series">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/platform ')]">
<!--
        <fo:block xsl:use-attribute-sets="platform">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/prognum ')]">
<!--
        <fo:block xsl:use-attribute-sets="prognum">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/featnum ')]">
<!--
        <fo:block xsl:use-attribute-sets="featnum">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/component ')]">
<!--
        <fo:block xsl:use-attribute-sets="component">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/othermeta ')]">
<!--
        <fo:block xsl:use-attribute-sets="othermeta">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/resourceid ')]">
<!--
        <fo:block xsl:use-attribute-sets="resourceid">
            <xsl:apply-templates/>
        </fo:block>
-->
    </xsl:template>

    <xsl:template name="processConcept">
        <xsl:apply-templates select="." mode="processConcept"/>
    </xsl:template>
    <xsl:template match="*" mode="processConcept">
        <fo:block xsl:use-attribute-sets="concept">
            <xsl:comment> concept/concept </xsl:comment>
            <xsl:apply-templates select="." mode="commonTopicProcessing"/>
        </fo:block>
	</xsl:template>

    <!-- RFE 2882109: Combine this common code with topic/topic rule. -->
    <!--<xsl:template match="*[contains(@class, ' concept/concept ')]">
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$topicType = 'topicChapter'">
                <xsl:call-template name="processTopicChapter"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicAppendix'">
                <xsl:call-template name="processTopicAppendix"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicPart'">
                <xsl:call-template name="processTopicPart"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicPreface'">
                <xsl:call-template name="processTopicPreface"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicSimple'">
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
                <xsl:choose>
                    <xsl:when test="not(ancestor::*[contains(@class,' topic/topic ')])">
                        <fo:page-sequence master-reference="{$page-sequence-reference}" xsl:use-attribute-sets="__force__page__count">
                            <xsl:call-template name="insertBodyStaticContents"/>
                            <fo:flow flow-name="xsl-region-body">
                                <xsl:call-template name="processConcept"/>
                            </fo:flow>
                        </fo:page-sequence>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="processConcept"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
			<xsl:when test="$topicType = 'topicAbstract'"/>
			<xsl:otherwise>
                <xsl:call-template name="processUnknowTopic">
                    <xsl:with-param name="topicType" select="$topicType"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>-->

    <xsl:template match="*[contains(@class, ' concept/conbody ')]">
        <fo:block xsl:use-attribute-sets="conbody">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template name="processReference">
        <xsl:apply-templates select="." mode="processReference"/>
    </xsl:template>
    <xsl:template match="*" mode="processReference">
        <fo:block xsl:use-attribute-sets="reference">
            <xsl:apply-templates select="." mode="commonTopicProcessing"/>
        </fo:block>
	</xsl:template>

    <!-- RFE 2882109: Combine this common code with topic/topic rule. -->
    <!--<xsl:template match="*[contains(@class, ' reference/reference ')]">
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$topicType = 'topicChapter'">
                <xsl:call-template name="processTopicChapter"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicAppendix'">
                <xsl:call-template name="processTopicAppendix"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicPart'">
                <xsl:call-template name="processTopicPart"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicPreface'">
                <xsl:call-template name="processTopicPreface"/>
            </xsl:when>
            <xsl:when test="$topicType = 'topicSimple'">
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
                <xsl:choose>
                    <xsl:when test="not(ancestor::*[contains(@class,' topic/topic ')])">
                        <fo:page-sequence master-reference="{$page-sequence-reference}" xsl:use-attribute-sets="__force__page__count">
                            <xsl:call-template name="insertBodyStaticContents"/>
                            <fo:flow flow-name="xsl-region-body">
                                <xsl:call-template name="processReference"/>
                            </fo:flow>
                        </fo:page-sequence>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="processReference"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
			<xsl:when test="$topicType = 'topicAbstract'"/>
			<xsl:otherwise>
                <xsl:call-template name="processUnknowTopic">
                    <xsl:with-param name="topicType" select="$topicType"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>-->

    <xsl:template match="*[contains(@class, ' reference/refbody ')]">
        <fo:block xsl:use-attribute-sets="refbody">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' reference/refsyn ')]">
        <fo:block xsl:use-attribute-sets="refsyn">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!-- Gets navigation title of current topic, used for bookmarks/TOC -->    
    <xsl:template name="getNavTitle">
        <!-- topicNumber = the # of times this topic has appeared. When topicNumber=3,
             this copy of the topic is based on its third appearance in the map. -->
        <xsl:param name="topicNumber" select="number('NaN')"/>
        <xsl:variable name="topicref" select="key('map-id', @id)"/>
        <xsl:variable name="numTopicref" select="$topicref[position()=$topicNumber]"/>
        <xsl:choose>
            <xsl:when test="not(string(number($topicNumber)) = 'NaN') and 
                            $topicref and
                            $numTopicref/@locktitle='yes' and
                            $numTopicref/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]">
               <xsl:value-of select="$numTopicref/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]"/>
            </xsl:when>
            <xsl:when test="not(string(number($topicNumber)) = 'NaN') and
                            $topicref and
                            $numTopicref/@locktitle='yes' and
                            $numTopicref/@navtitle">
                <xsl:value-of select="$numTopicref/@navtitle"/>
            </xsl:when>
            <xsl:when test="string(number($topicNumber)) = 'NaN' and
                            $topicref and
                            $topicref/@locktitle='yes' and
                            $topicref/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]">
                <xsl:value-of select="$topicref/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]"/>
            </xsl:when>
            <xsl:when test="string(number($topicNumber)) = 'NaN' and
                            $topicref and
                            $topicref/@locktitle='yes' and
                            $topicref/@navtitle">
                <xsl:value-of select="$topicref/@navtitle"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                    <xsl:call-template name="getTitle"/>
                 </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="getTitle"><!-- get fully-processed title content by whatever mechanism -->
        <xsl:apply-templates select="." mode="getTitle"/>
    </xsl:template>
    <xsl:template match="*" mode="getTitle">
        <xsl:choose>
<!--             add keycol here once implemented-->
            <xsl:when test="@spectitle">
                <xsl:value-of select="@spectitle"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/titlealts ')]">
      <xsl:if test="$DRAFT='yes'">
        <xsl:if test="*">
          <fo:block xsl:use-attribute-sets="titlealts">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
          </fo:block>
        </xsl:if>
      </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/navtitle ')]">
        <fo:block xsl:use-attribute-sets="navtitle">
            <xsl:call-template name="commonattributes"/>
            <fo:inline xsl:use-attribute-sets="navtitle__label">
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'Navigation title'"/>
                </xsl:call-template>
                <xsl:text>: </xsl:text>
            </fo:inline>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!-- Map uses map/searchtitle, topic uses topic/searchtitle. This will likely be changed
         to a single value in DITA 2.0, but for now, recognize both. -->
    <xsl:template match="*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')] | 
                         *[contains(@class,' topic/titlealts ')]/*[contains(@class,' map/searchtitle ')]">
        <fo:block xsl:use-attribute-sets="searchtitle">
            <xsl:call-template name="commonattributes"/>
            <fo:inline xsl:use-attribute-sets="searchtitle__label">
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'Search title'"/>
                </xsl:call-template>
                <xsl:text>: </xsl:text>
            </fo:inline>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/topicmeta ')]/*[contains(@class,' map/searchtitle ')]"/>

    <xsl:template match="*[contains(@class,' topic/abstract ')]">
        <fo:block xsl:use-attribute-sets="abstract">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!-- For SF Bug 2879171: modify so that shortdesc is inline when inside
         abstract with only other text or inline markup. -->
    <xsl:template match="*[contains(@class,' topic/shortdesc ')]">
        <xsl:variable name="format-as-block">
            <xsl:choose>
                <xsl:when test="not(parent::*[contains(@class,' topic/abstract ')])">yes</xsl:when>
                <xsl:when test="preceding-sibling::*[contains(@class,' topic/p ') or contains(@class,' topic/dl ') or
                                         contains(@class,' topic/fig ') or contains(@class,' topic/lines ') or
                                         contains(@class,' topic/lq ') or contains(@class,' topic/note ') or
                                         contains(@class,' topic/ol ') or contains(@class,' topic/pre ') or
                                         contains(@class,' topic/simpletable ') or contains(@class,' topic/sl ') or
                                         contains(@class,' topic/table ') or contains(@class,' topic/ul ')]">yes</xsl:when>
                <xsl:when test="following-sibling::*[contains(@class,' topic/p ') or contains(@class,' topic/dl ') or
                                         contains(@class,' topic/fig ') or contains(@class,' topic/lines ') or
                                         contains(@class,' topic/lq ') or contains(@class,' topic/note ') or
                                         contains(@class,' topic/ol ') or contains(@class,' topic/pre ') or
                                         contains(@class,' topic/simpletable ') or contains(@class,' topic/sl ') or
                                         contains(@class,' topic/table ') or contains(@class,' topic/ul ')]">yes</xsl:when>
                <xsl:otherwise>no</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$format-as-block='yes'">
                <xsl:apply-templates select="." mode="format-shortdesc-as-block"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="format-shortdesc-as-inline"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*" mode="format-shortdesc-as-block">
        <!--Edited by William on 2009-07-07 for bug:2815492 start-->
        <!--fo:block xsl:use-attribute-sets="shortdesc" id="{@id}">
            <xsl:apply-templates/>
        </fo:block-->
        <!--Bug 2928540: use attribute set topic__shortdesc instead of shortdesc -->
        <!--compare the length of shortdesc with the got max chars-->
        <fo:block xsl:use-attribute-sets="topic__shortdesc">
            <xsl:call-template name="commonattributes"/>
            <!-- If the shortdesc is sufficiently short, add keep-with-next. -->
            <xsl:if test="string-length(.) &lt;= $maxCharsInShortDesc">
                <!-- Low-strength keep to avoid conflict with keeps on titles. -->
                <xsl:attribute name="keep-with-next.within-page">5</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </fo:block>
        <!--Edited by William on 2009-07-07 for bug:2815492 end-->
    </xsl:template>

    <xsl:template match="*" mode="format-shortdesc-as-inline">
        <fo:inline xsl:use-attribute-sets="shortdesc">
            <xsl:call-template name="commonattributes"/>
            <xsl:if test="preceding-sibling::* | preceding-sibling::text()">
                <xsl:text> </xsl:text>
            </xsl:if>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>
    
    <xsl:template match="*[contains(@class,' map/shortdesc ')]">
        <xsl:apply-templates select="." mode="format-shortdesc-as-block"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class,' topic/shortdesc ')]" priority="1">
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>
        <xsl:choose>
            <!--  Disable chapter summary processing when mini TOC is created -->
            <xsl:when test="(topicType = 'topicChapter') or (topicType = 'topicAppendix')"/>
            <!--   Normal processing         -->
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="format-shortdesc-as-block"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--Added by William on 2009-07-07 for bug:2815492 start -->
    <xsl:template name="getMaxCharsForShortdescKeep">
    <!-- These values specify the length of a short description that will
        render with keep-with-next set, which should be (approximately) the
        character count in three lines of rendered shortdesc text. If you customize the
        default font, page margins, or shortdesc attribute sets, you may need
        to change these values. -->
        <xsl:choose>
            <xsl:when test="$locale = 'en_US' or $locale = 'fr_FR'">
                <xsl:value-of select="360"/>
            </xsl:when>
            <xsl:when test="$locale = 'ja_JP'">
                <xsl:value-of select="141"/>
            </xsl:when>
            <xsl:when test="$locale = 'zh_CN'">
                <xsl:value-of select="141"/>
            </xsl:when>
            <!-- Other languages require a template override to generate
            keep-with-next
            on shortdesc. Data was not available at the time this code released.
            -->
            <xsl:otherwise>
                <xsl:value-of select="0"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--Added by William on 2009-07-07 for bug:2815492 end -->
    
    <!-- this is the fallthrough body for nested topics -->
    <xsl:template match="*[contains(@class,' topic/body ')]">
        <xsl:variable name="level" select="count(ancestor::*[contains(@class,' topic/topic ')])"/>
        <xsl:choose>
                <xsl:when test="not(node())"/>
                <xsl:when test="$level = 1">
                    <fo:block xsl:use-attribute-sets="body__toplevel">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:when test="$level = 2">
                    <fo:block xsl:use-attribute-sets="body__secondLevel">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="body">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/bodydiv ')]">
        <fo:block>
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

  <xsl:template match="*[contains(@class,' topic/section ')][@spectitle != '' 
                         and not(*[contains(@class, ' topic/title ')])]" 
    mode="dita2xslfo:section-heading" 
    priority="10">
    <fo:block xsl:use-attribute-sets="section.title">
      <xsl:call-template name="commonattributes"/>
      <xsl:variable name="spectitleValue" as="xs:string" select="string(@spectitle)"/>
      <xsl:variable name="resolvedVariable">
        <xsl:call-template name="insertVariable">
          <xsl:with-param name="theVariableID" select="$spectitleValue"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:sequence
        select="if (not(normalize-space($resolvedVariable))) 
        then $spectitleValue
        else $resolvedVariable"
      />
    </fo:block>
    
  </xsl:template>
    <xsl:template match="*[contains(@class,' topic/section ')]" mode="dita2xslfo:section-heading">
      <!-- Specialized section elements may override this rule to add
           default headings for a section. By default, titles are processed
           where they exist within the section, so overrides may need to
           check for the existence of a title first. -->
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/section ')]">
        <fo:block xsl:use-attribute-sets="section">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="." mode="dita2xslfo:section-heading"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/sectiondiv ')]">
        <fo:block>
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/example ')]">
        <fo:block xsl:use-attribute-sets="example">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/desc ')]">
        <fo:inline xsl:use-attribute-sets="desc">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/prolog ')]"/>
<!--
        <fo:block xsl:use-attribute-sets="prolog">
            <xsl:apply-templates/>
        </fo:block>
-->
        <!--xsl:copy-of select="node()"/-->
        <!-- edited by William on 2009-07-02 for indexterm bug:2815485 start -->
        <!--xsl:apply-templates select="descendant::opentopic-index:index.entry[not(parent::opentopic-index:index.entry)]"/-->
        <!-- edited by William on 2009-07-02 for indexterm bug:2815485 end -->
    <!--/xsl:template-->
    
    <!-- added by William on 2009-07-02 for indexterm bug:2815485 start -->
    <xsl:template name="pullPrologIndexTerms">
        <xsl:apply-templates select="ancestor-or-self::*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/prolog ')]
            //opentopic-index:index.entry[not(parent::opentopic-index:index.entry)]"/>
    </xsl:template>
    <!-- added by William on 2009-07-02 for indexterm bug:2815485 end -->
    
    <xsl:template match="*[contains(@class, ' topic/metadata ')]">
<!--
        <fo:block xsl:use-attribute-sets="metadata">
            <xsl:apply-templates/>
        </fo:block>
-->
        <xsl:apply-templates select="descendant::opentopic-index:index.entry[not(parent::opentopic-index:index.entry)]"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/p ')]">
        <fo:block xsl:use-attribute-sets="p">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template name="placeNoteContent">
        <xsl:apply-templates select="." mode="placeNoteContent"/>
    </xsl:template>
    <xsl:template match="*" mode="placeNoteContent">
        <fo:block xsl:use-attribute-sets="note">
            <xsl:call-template name="commonattributes"/>
            <fo:inline xsl:use-attribute-sets="note__label">
                <xsl:choose>
                    <xsl:when test="@type='note' or not(@type)">
                        <fo:inline xsl:use-attribute-sets="note__label__note">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Note'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='notice'">
                        <fo:inline xsl:use-attribute-sets="note__label__notice">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Notice'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='tip'">
                        <fo:inline xsl:use-attribute-sets="note__label__tip">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Tip'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='fastpath'">
                        <fo:inline xsl:use-attribute-sets="note__label__fastpath">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Fastpath'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='restriction'">
                        <fo:inline xsl:use-attribute-sets="note__label__restriction">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Restriction'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='important'">
                        <fo:inline xsl:use-attribute-sets="note__label__important">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Important'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='remember'">
                        <fo:inline xsl:use-attribute-sets="note__label__remember">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Remember'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='attention'">
                        <fo:inline xsl:use-attribute-sets="note__label__attention">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Attention'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='caution'">
                        <fo:inline xsl:use-attribute-sets="note__label__caution">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Caution'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='danger'">
                        <fo:inline xsl:use-attribute-sets="note__label__danger">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Danger'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='warning'">
                        <fo:inline xsl:use-attribute-sets="note__label__danger">
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Warning'"/>
                            </xsl:call-template>
                        </fo:inline>
                    </xsl:when>
                    <xsl:when test="@type='other'">
                        <fo:inline xsl:use-attribute-sets="note__label__other">
                            <xsl:choose>
                                <xsl:when test="@othertype">
                                    <xsl:value-of select="@othertype"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>[</xsl:text>
                                    <xsl:value-of select="@type"/>
                                    <xsl:text>]</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </fo:inline>
                    </xsl:when>
                </xsl:choose>
                <xsl:text>: </xsl:text>
            </fo:inline>
            <xsl:text>  </xsl:text>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/note ')]">
        <xsl:variable name="noteType">
            <xsl:choose>
                <xsl:when test="@type = 'other' and @othertype">
                    <xsl:value-of select="@othertype"/>
                </xsl:when>
                <xsl:when test="@type">
                    <xsl:value-of select="@type"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'note'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="noteImagePath">
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="concat($noteType, ' Note Image Path')"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not($noteImagePath = '')">
                <fo:table xsl:use-attribute-sets="note__table">
                    <fo:table-column xsl:use-attribute-sets="note__image__column"/>
                    <fo:table-column xsl:use-attribute-sets="note__text__column"/>
                    <fo:table-body>
                        <fo:table-row>
                                <fo:table-cell xsl:use-attribute-sets="note__image__entry">
                                    <fo:block>
                                        <fo:external-graphic src="url({concat($artworkPrefix, $noteImagePath)})" xsl:use-attribute-sets="image"/>
                                    </fo:block>
                                </fo:table-cell>
                                <fo:table-cell xsl:use-attribute-sets="note__text__entry">
                                    <xsl:call-template name="placeNoteContent"/>
                                </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="placeNoteContent"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/lq ')]">
        <fo:block>
            <xsl:call-template name="commonattributes"/>
            <xsl:choose>
                <xsl:when test="@href or @reftitle">
                    <xsl:call-template name="processAttrSetReflection">
                        <xsl:with-param name="attrSet" select="'lq'"/>
                        <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="processAttrSetReflection">
                        <xsl:with-param name="attrSet" select="'lq_simple'"/>
                        <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates/>
        </fo:block>
        <xsl:choose>
            <xsl:when test="@href">
                <fo:block xsl:use-attribute-sets="lq_link">
                    <fo:basic-link>
                        <xsl:call-template name="buildBasicLinkDestination">
                            <xsl:with-param name="scope" select="@scope"/>
                            <xsl:with-param name="href" select="@href"/>
                        </xsl:call-template>

                        <xsl:choose>
                            <xsl:when test="@reftitle">
                                <xsl:value-of select="@reftitle"/>
                            </xsl:when>
                            <xsl:when test="not(@type = 'external' or @format = 'html')">
                                <xsl:call-template name="insertReferenceTitle">
                                    <xsl:with-param name="href" select="@href"/>
                                    <xsl:with-param name="titlePrefix" select="''"/>
                                </xsl:call-template>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@href"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:basic-link>
                </fo:block>
            </xsl:when>
            <xsl:when test="@reftitle">
                <fo:block xsl:use-attribute-sets="lq_title">
                    <xsl:value-of select="@reftitle"/>
                </fo:block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/q ')]">
        <fo:inline xsl:use-attribute-sets="q">
            <xsl:call-template name="commonattributes"/>
            <xsl:text>&#x201C;</xsl:text>
            <xsl:apply-templates/>
            <xsl:text>&#x201D;</xsl:text>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/fig ')]">
        <fo:block xsl:use-attribute-sets="fig">
            <xsl:call-template name="commonattributes"/>
            <xsl:if test="not(@id)">
              <xsl:attribute name="id">
                <xsl:call-template name="get-id"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="*[not(contains(@class,' topic/title '))]"/>
            <xsl:apply-templates select="*[contains(@class,' topic/title ')]"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/figgroup ')]">
        <fo:inline xsl:use-attribute-sets="figgroup">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/pre ')]">
        <xsl:call-template name="setSpecTitle"/>
        <xsl:variable name="attrSets">
            <xsl:call-template name="setFrame"/>
        </xsl:variable>
        <fo:block xsl:use-attribute-sets="pre">
            <xsl:call-template name="commonattributes"/>
            <!--TODO: $attrSets contains space-separated names!! Check if this actually works. -->
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="$attrSets"/>
                <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
            </xsl:call-template>
            <xsl:call-template name="setScale"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template name="setSpecTitle">
        <xsl:if test="@spectitle">
            <fo:block xsl:use-attribute-sets="__spectitle">
                <xsl:value-of select="@spectitle"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="setScale">
        <xsl:if test="@scale">
            <!-- For applications that do not yet take percentages. need to divide by 10 and use "pt" -->
            <xsl:attribute name="font-size">
                <xsl:value-of select="concat(@scale, '%')"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <!-- Process the frame attribute -->
    <!-- frame styles (setframe) must be called within a block that defines the content being framed -->
    <xsl:template name="setFrame">
        <xsl:if test="contains(@frame,'top')"> __border__top </xsl:if>
        <xsl:if test="contains(@frame,'bot')"> __border__bot </xsl:if>
        <xsl:if test="contains(@frame,'sides')"> __border__sides </xsl:if>
        <xsl:if test="contains(@frame,'all')"> __border__all </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/lines ')]">
        <xsl:call-template name="setSpecTitle"/>
        <xsl:variable name="attrSets">
            <xsl:call-template name="setFrame"/>
        </xsl:variable>
        <fo:block xsl:use-attribute-sets="lines">
            <xsl:call-template name="commonattributes"/>
            <!--TODO: $attrSets contains space-separated names!! Check if this actually works. -->
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="$attrSets"/>
                <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
            </xsl:call-template>
            <xsl:call-template name="setScale"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!-- The text element has no default semantics or formatting -->
    <xsl:template match="*[contains(@class,' topic/text ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/keyword ')]">
        <fo:inline xsl:use-attribute-sets="keyword">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/ph ')]">
        <fo:inline xsl:use-attribute-sets="ph">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/boolean ')]">
        <fo:inline xsl:use-attribute-sets="boolean">
            <xsl:call-template name="commonattributes"/>
            <xsl:value-of select="name()"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="@state"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/state ')]">
        <fo:inline xsl:use-attribute-sets="state">
            <xsl:call-template name="commonattributes"/>
            <xsl:value-of select="name()"/>
            <xsl:text>: </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text>=</xsl:text>
            <xsl:value-of select="@value"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' ut-d/imagemap ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:apply-templates select="*[contains(@class,' topic/image ')]"/>
        <fo:list-block xsl:use-attribute-sets="ol">
            <xsl:apply-templates select="*[contains(@class,' ut-d/area ')]"/>
        </fo:list-block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' ut-d/area ')]">
        <fo:list-item xsl:use-attribute-sets="ol.li">
            <xsl:call-template name="commonattributes"/>
            <fo:list-item-label xsl:use-attribute-sets="ol.li__label">
                <fo:block xsl:use-attribute-sets="ol.li__label__content">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Ordered List Number'"/>
                        <xsl:with-param name="theParameters">
                            <number>
                                <xsl:number/>
                            </number>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:block>
            </fo:list-item-label>

            <fo:list-item-body xsl:use-attribute-sets="ol.li__body">
                <fo:block xsl:use-attribute-sets="ol.li__content">
                    <xsl:apply-templates/>
                </fo:block>
            </fo:list-item-body>

        </fo:list-item>
    </xsl:template>

    <xsl:template match="*[contains(@class,' ut-d/shape ')]"/>

    <xsl:template match="*[contains(@class,' ut-d/coords ')]"/>

    <xsl:template match="*[contains(@class,' topic/image ')]">
        <!-- build any pre break indicated by style -->
        <xsl:choose>
            <xsl:when test="parent::fig">
                <!-- NOP if there is already a break implied by a parent property -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(@placement='inline')">
                    <!-- generate an FO break here -->
                    <fo:block>&#160;</fo:block>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
            <xsl:when test="not(@placement = 'inline')">
<!--                <fo:float xsl:use-attribute-sets="image__float">-->
                    <fo:block xsl:use-attribute-sets="image__block">
                        <xsl:call-template name="commonattributes"/>
                        <xsl:call-template name="placeImage">
                            <xsl:with-param name="imageAlign" select="@align"/>
                            <xsl:with-param name="href" select="concat($input.dir.url, @href)"/>
                            <xsl:with-param name="height" select="@height"/>
                            <xsl:with-param name="width" select="@width"/>
                        </xsl:call-template>
                    </fo:block>
<!--                </fo:float>-->
            </xsl:when>
            <xsl:otherwise>
                <fo:inline xsl:use-attribute-sets="image__inline">
                    <xsl:call-template name="commonattributes"/>
                    <xsl:call-template name="placeImage">
                        <xsl:with-param name="imageAlign" select="@align"/>
                        <xsl:with-param name="href" select="concat($input.dir.url, @href)"/>
                        <xsl:with-param name="height" select="@height"/>
                        <xsl:with-param name="width" select="@width"/>
                    </xsl:call-template>
                </fo:inline>
            </xsl:otherwise>
        </xsl:choose>

        <!-- build any post break indicated by style -->
        <xsl:choose>
            <xsl:when test="parent::fig">
                <!-- NOP if there is already a break implied by a parent property -->
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not(@placement='inline')">
                    <!-- generate an FO break here -->
                    <fo:block>&#160;</fo:block>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="placeImage">
        <xsl:param name="imageAlign"/>
        <xsl:param name="href"/>
        <xsl:param name="height"/>
        <xsl:param name="width"/>
        <xsl:apply-templates select="." mode="placeImage">
            <xsl:with-param name="imageAlign" select="$imageAlign"/>
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="height" select="$height"/>
            <xsl:with-param name="width" select="$width"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*" mode="placeImage">
        <xsl:param name="imageAlign"/>
        <xsl:param name="href"/>
        <xsl:param name="height"/>
        <xsl:param name="width"/>
<!--Using align attribute set according to image @align attribute-->
        <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="concat('__align__', $imageAlign)"/>
                <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
            </xsl:call-template>
        <fo:external-graphic src="url({$href})" xsl:use-attribute-sets="image">
            <!--Setting image height if defined-->
            <xsl:if test="$height">
                <xsl:attribute name="content-height">
                <!--The following test was commented out because most people found the behavior
                 surprising.  It used to force images with a number specified for the dimensions
                 *but no units* to act as a measure of pixels, *if* you were printing at 72 DPI.
                 Uncomment if you really want it. -->
                    <xsl:choose>
                      <!--xsl:when test="not(string(number($height)) = 'NaN')">
                        <xsl:value-of select="concat($height div 72,'in')"/>
                      </xsl:when-->
                      <xsl:when test="not(string(number($height)) = 'NaN')">
                        <xsl:value-of select="concat($height, 'px')"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$height"/>
                      </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <!--Setting image width if defined-->
            <xsl:if test="$width">
                <xsl:attribute name="content-width">
                    <xsl:choose>
                      <!--xsl:when test="not(string(number($width)) = 'NaN')">
                        <xsl:value-of select="concat($width div 72,'in')"/>
                      </xsl:when-->
                      <xsl:when test="not(string(number($width)) = 'NaN')">
                        <xsl:value-of select="concat($width, 'px')"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:value-of select="$width"/>
                      </xsl:otherwise>
                    </xsl:choose>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="not($width) and not($height) and @scale">
                <xsl:attribute name="content-width">
                    <xsl:value-of select="concat(@scale,'%')"/>
                </xsl:attribute>
            </xsl:if>
        </fo:external-graphic>
    </xsl:template>


    <xsl:template match="*[contains(@class,' topic/alt ')]">
        <fo:block xsl:use-attribute-sets="alt">
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/object ')]">
        <fo:inline xsl:use-attribute-sets="object">
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/param ')]">
        <fo:inline xsl:use-attribute-sets="param">
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/draft-comment ')]">
        <xsl:if test="$publishRequiredCleanup = 'yes' or $DRAFT='yes'">
            <fo:block xsl:use-attribute-sets="draft-comment">
                <xsl:call-template name="commonattributes"/>
                <fo:block xsl:use-attribute-sets="draft-comment__label">
            Disposition:
                    <xsl:value-of select="@disposition"/> /
            Status:
                    <xsl:value-of select="@status"/>
                </fo:block>
                <xsl:apply-templates/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/required-cleanup ')]">
        <xsl:if test="$publishRequiredCleanup = 'yes' or $DRAFT='yes'">
            <fo:inline xsl:use-attribute-sets="required-cleanup">
                <xsl:call-template name="commonattributes"/>
                <fo:inline xsl:use-attribute-sets="required-cleanup__label">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Required-Cleanup'"/>
                    </xsl:call-template>
                    <xsl:if test="string(@remap)">
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="@remap"/>
                        <xsl:text>)</xsl:text>
                    </xsl:if>
                    <xsl:text>: </xsl:text>
                </fo:inline>
                <xsl:apply-templates/>
            </fo:inline>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/fn ')]">

        <!--
                <fo:block xsl:use-attribute-sets="fn">
                    <xsl:if test="@id">
                        <fo:inline xsl:use-attribute-sets="fn__id">
                            <xsl:text>[Footnote: </xsl:text>
                            <xsl:value-of select="@id"/>
                            <xsl:text>]</xsl:text>
                        </fo:inline>
                    </xsl:if>
                    <xsl:if test="@callout">
                        <fo:inline xsl:use-attribute-sets="fn__callout">
                            <xsl:value-of select="@callout"/>
                        </fo:inline>
                    </xsl:if>
                    <xsl:apply-templates/>
                </fo:block>
        -->

        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <fo:footnote>
            <xsl:choose>
              <xsl:when test="not(@id)">
                <fo:inline xsl:use-attribute-sets="fn__callout">

                    <xsl:choose>
                        <xsl:when test="@callout">
                            <xsl:value-of select="@callout"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:number level="any" count="*[contains(@class,' topic/fn ') and not(@callout)]"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:inline>
              </xsl:when>
              <xsl:otherwise>
                <!-- Footnote with id does not generate its own callout. -->
                <fo:inline/>
              </xsl:otherwise>
            </xsl:choose>

            <fo:footnote-body>
                <fo:list-block xsl:use-attribute-sets="fn__body">
                    <fo:list-item>
                        <fo:list-item-label end-indent="label-end()">
                            <fo:block text-align="right">
                                <fo:inline xsl:use-attribute-sets="fn__callout">
                                    <xsl:choose>
                                        <xsl:when test="@callout">
                                            <xsl:value-of select="@callout"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:number level="any" count="*[contains(@class,' topic/fn ') and not(@callout)]"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:inline>
                            </fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()">
                            <fo:block>
<!--                                <xsl:value-of select="."/>-->
                                <xsl:apply-templates/>
                            </fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                </fo:list-block>
            </fo:footnote-body>
        </fo:footnote>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/indexterm ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/indextermref ')]">
        <fo:inline xsl:use-attribute-sets="indextermref">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/cite ')]">
        <fo:inline xsl:use-attribute-sets="cite">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <!--  Layout masters  -->

    <xsl:template match="*" mode="layout-masters-processing">
        <xsl:element name="{name()}">
            <xsl:apply-templates select="@*" mode="layout-masters-processing"/>
            <xsl:apply-templates select="*" mode="layout-masters-processing"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*" mode="layout-masters-processing">
        <xsl:copy-of select="."/>
    </xsl:template>


    <xsl:template match="@background-image" mode="layout-masters-processing">
        <xsl:attribute name="background-image">
            <xsl:value-of select="concat('url(',$artworkPrefix,substring-after(.,'artwork:'),')')"/>
        </xsl:attribute>
    </xsl:template>

    <!-- Template to copy original IDs -->

    <xsl:template match="@id">
        <xsl:attribute name="id">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <!-- Process common attributes -->
    <xsl:template name="commonattributes">
      <xsl:apply-templates select="@id"/>
    </xsl:template>

    <!-- Get ID for an element, generate ID if not explicitly set. -->
    <xsl:template name="get-id">
      <xsl:param name="element" select="."/>
      <xsl:choose>
        <xsl:when test="$element/@id">
          <xsl:value-of select="$element/@id"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="generate-id($element)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!-- Generate TOC ID -->
    <xsl:template name="generate-toc-id">
      <xsl:param name="element" select="."/>
      <xsl:value-of select="concat('_OPENTOPIC_TOC_PROCESSING_', generate-id($element))"/>
    </xsl:template>

</xsl:stylesheet>
