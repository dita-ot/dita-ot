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
    xmlns:exsl="http://exslt.org/common"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    extension-element-prefixes="exsl"
    exclude-result-prefixes="opentopic"
    version="2.0">

    <xsl:variable name="map" select="//opentopic:map"/>

    <xsl:template name="createTocHeader">
        <fo:block xsl:use-attribute-sets="__toc__header" id="{$id.toc}">
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="'Table of Contents'"/>
            </xsl:call-template>
        </fo:block>
    </xsl:template>

    <xsl:template name="createToc">

        <xsl:variable name="toc">
            <xsl:apply-templates select="/" mode="toc"/>
        </xsl:variable>

        <xsl:if test="count(exsl:node-set($toc)/*) > 0">
            <fo:page-sequence master-reference="toc-sequence" format="i" xsl:use-attribute-sets="__force__page__count">

                <xsl:call-template name="insertTocStaticContents"/>

                <fo:flow flow-name="xsl-region-body">
                    <xsl:call-template name="createTocHeader"/>
                    <fo:block>
                        <xsl:copy-of select="exsl:node-set($toc)"/>
                    </fo:block>
                </fo:flow>

            </fo:page-sequence>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/" mode="toc">
        <xsl:apply-templates mode="toc">
            <xsl:with-param name="include" select="'true'"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo '))]" mode="toc">
        <xsl:param name="include"/>
        <xsl:variable name="topicLevel" select="count(ancestor-or-self::*[contains(@class, ' topic/topic ')])"/>
        <xsl:if test="$topicLevel &lt; $tocMaximumLevel">
            <xsl:variable name="topicTitle">
                <xsl:call-template name="getNavTitle" />
            </xsl:variable>
            <xsl:variable name="id" select="@id"/>
            <xsl:variable name="gid" select="generate-id()"/>
            <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
            <xsl:variable name="mapTopic">
                <xsl:copy-of select="$map//*[@id = $id]"/>
            </xsl:variable>
            <xsl:variable name="topicType">
                <xsl:call-template name="determineTopicType"/>
            </xsl:variable>

            <!-- Removing this variable - it is not used and matches on an invalid class attribute. -->
            <!--<xsl:variable name="parentTopicHead">
                <xsl:copy-of select="$map//*[@id = $id]/parent::*[contains(@class, ' mapgroup/topichead ')]"/>
            </xsl:variable>-->

            <!--        <xsl:if test="(($mapTopic/*[position() = $topicNumber][@toc = 'yes' or not(@toc)]) or (not($mapTopic/*) and $include = 'true')) and not($parentTopicHead/*[position() = $topicNumber]/@toc = 'no')">-->
            <!-- added by William on 2009-05-11 for toc bug start -->
            <xsl:choose>
            	<xsl:when test="($mapTopic/*[position() = $topicNumber][@toc = 'yes' or not(@toc)]) or (not($mapTopic/*) and $include = 'true')">
                    <fo:block xsl:use-attribute-sets="__toc__indent">
                        <xsl:variable name="tocItemContent">
                          <fo:basic-link xsl:use-attribute-sets="__toc__link">
                            <xsl:attribute name="internal-destination">
                              <xsl:call-template name="generate-toc-id"/>
                            </xsl:attribute>
                            <xsl:apply-templates select="$topicType" mode="toc-prefix-text">
                                <xsl:with-param name="id" select="@id"/>
                            </xsl:apply-templates>
                            <fo:inline xsl:use-attribute-sets="__toc__title">
                                <xsl:value-of select="$topicTitle"/>
                            </fo:inline>
                            <fo:inline xsl:use-attribute-sets="__toc__page-number">
                                <fo:leader xsl:use-attribute-sets="__toc__leader"/>
                                <fo:page-number-citation>
                                  <xsl:attribute name="ref-id">
                                    <xsl:call-template name="generate-toc-id"/>
                                  </xsl:attribute>
                                </fo:page-number-citation>
                            </fo:inline>
                        </fo:basic-link>
                        </xsl:variable>
                        <xsl:apply-templates select="$topicType" mode="toc-topic-text">
                            <xsl:with-param name="tocItemContent" select="$tocItemContent"/>
                            <xsl:with-param name="currentNode" select="."/>
                        </xsl:apply-templates>
                    </fo:block>
                    <!-- In a future version, suppressing Notices in the TOC should not be hard-coded. -->
                    <xsl:if test="not($topicType = 'topicNotices')">
                        <xsl:apply-templates mode="toc">
                            <xsl:with-param name="include" select="'true'"/>
                        </xsl:apply-templates>
                    </xsl:if>
            	</xsl:when>
            	<xsl:otherwise>
	            	<xsl:apply-templates mode="toc">
		                    <xsl:with-param name="include" select="'true'"/>
		            </xsl:apply-templates>
            	</xsl:otherwise>
            </xsl:choose>
            <!-- added by William on 2009-05-11 for toc bug end -->
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="text()[.='topicChapter']" mode="toc-prefix-text">
        <xsl:param name="id"/>
        <xsl:variable name="topicChapters">
            <xsl:copy-of select="$map//*[contains(@class, ' bookmap/chapter ')]"/>
        </xsl:variable>
        <xsl:variable name="chapterNumber">
            <xsl:number format="1" value="count($topicChapters/*[@id = $id]/preceding-sibling::*) + 1"/>
        </xsl:variable>
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table of Contents Chapter'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="$chapterNumber"/>
                </number>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="text()[.='topicAppendix']" mode="toc-prefix-text">
        <xsl:param name="id"/>
        <xsl:variable name="topicAppendixes">
            <xsl:copy-of select="$map//*[contains(@class, ' bookmap/appendix ')]"/>
        </xsl:variable>
        <xsl:variable name="appendixNumber">
            <xsl:number format="A" value="count($topicAppendixes/*[@id = $id]/preceding-sibling::*) + 1"/>
        </xsl:variable>
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table of Contents Appendix'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="$appendixNumber"/>
                </number>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="text()[.='topicPart']" mode="toc-prefix-text">
        <xsl:param name="id"/>
        <xsl:variable name="topicParts">
            <xsl:copy-of select="$map//*[contains(@class, ' bookmap/part ')]"/>
        </xsl:variable>
        <xsl:variable name="partNumber">
            <xsl:number format="I" value="count($topicParts/*[@id = $id]/preceding-sibling::*) + 1"/>
        </xsl:variable>
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table of Contents Part'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="$partNumber"/>
                </number>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="text()[.='topicPreface']" mode="toc-prefix-text">
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table of Contents Preface'"/>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template match="text()[.='topicNotices']" mode="toc-prefix-text">
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table of Contents Notices'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="node()" mode="toc-prefix-text" />


    <xsl:template match="text()[. = 'topicChapter']" mode="toc-topic-text">
        <xsl:param name="tocItemContent"/>
        <xsl:param name="currentNode"/>
        <xsl:for-each select="$currentNode">
        <fo:block xsl:use-attribute-sets="__toc__chapter__content">
            <xsl:copy-of select="$tocItemContent"/>
        </fo:block>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="text()[. = 'topicAppendix']" mode="toc-topic-text">
        <xsl:param name="tocItemContent"/>
        <xsl:param name="currentNode"/>
        <xsl:for-each select="$currentNode">
        <fo:block xsl:use-attribute-sets="__toc__appendix__content">
            <xsl:copy-of select="$tocItemContent"/>
        </fo:block>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="text()[. = 'topicPart']" mode="toc-topic-text">
        <xsl:param name="tocItemContent"/>
        <xsl:param name="currentNode"/>
        <xsl:for-each select="$currentNode">
        <fo:block xsl:use-attribute-sets="__toc__part__content">
            <xsl:copy-of select="$tocItemContent"/>
        </fo:block>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="text()[. = 'topicPreface']" mode="toc-topic-text">
        <xsl:param name="tocItemContent"/>
        <xsl:param name="currentNode"/>
        <xsl:for-each select="$currentNode">
        <fo:block xsl:use-attribute-sets="__toc__preface__content">
            <xsl:copy-of select="$tocItemContent"/>
        </fo:block>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template match="text()[. = 'topicNotices']" mode="toc-topic-text">
        <!-- Disabled, because now the Notices appear before the TOC -->
        <!--<xsl:param name="tocItemContent"/>
        <fo:block xsl:use-attribute-sets="__toc__notices__content">
            <xsl:copy-of select="$tocItemContent"/>
        </fo:block>-->
    </xsl:template>
    
    <xsl:template match="node()" mode="toc-topic-text">
        <xsl:param name="tocItemContent"/>
        <xsl:param name="currentNode"/>
        <xsl:for-each select="$currentNode">
        <fo:block xsl:use-attribute-sets="__toc__topic__content">
            <xsl:copy-of select="$tocItemContent"/>
        </fo:block>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="node()" mode="toc">
        <xsl:param name="include"/>
        <xsl:apply-templates mode="toc">
            <xsl:with-param name="include" select="$include"/>
        </xsl:apply-templates>
    </xsl:template>

</xsl:stylesheet>
