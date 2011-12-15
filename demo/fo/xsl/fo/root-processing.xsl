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
    xmlns:opentopic-i18n="http://www.idiominc.com/opentopic/i18n"
    xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    exclude-result-prefixes="opentopic-index opentopic opentopic-i18n opentopic-func"
    version="2.0">
    
    <xsl:param name="bookmap-order" select="'discard'"/>
  
    <xsl:variable name="retain-bookmap-order" select="*[contains(@class,' bookmap/bookmap ')] and $bookmap-order eq 'retain'"/>
    <xsl:variable name="writing-mode">
      <xsl:variable name="lang" select="if (contains($locale, '_')) then substring-before($locale, '_') else $locale"/>
      <xsl:choose>
        <xsl:when test="some $l in ('ar', 'fa', 'he', 'ps', 'ur') satisfies $l eq $lang">rl</xsl:when>
        <xsl:otherwise>lr</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="layout-masters">
        <xsl:value-of select="'cfg:fo/layout-masters.xml'"/>
    </xsl:variable>

    <xsl:variable name="mapType">
        <xsl:choose>
            <xsl:when test="/*[contains(@class, ' map/map ') and contains(@class, ' bookmap/bookmap ')]">
                <xsl:value-of select="'bookmap'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'ditamap'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="productName">
        <xsl:variable name="mapProdname" select="(/*/opentopic:map//*[contains(@class, ' topic/prodname ')])[1]"/>
        <xsl:variable name="bkinfoProdname" select="(/*/*[contains(@class, ' bkinfo/bkinfo ')]//*[contains(@class, ' topic/prodname ')])[1]"/>
        <xsl:choose>
            <xsl:when test="$mapProdname">
                <xsl:value-of select="$mapProdname"/>
            </xsl:when>
            <xsl:when test="$bkinfoProdname">
                <xsl:value-of select="$bkinfoProdname"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'Product Name'"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:variable>

    <xsl:variable name="map" select="//opentopic:map"/>

    <xsl:variable name="topicNumbers">
        <xsl:for-each select="//*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo '))]">
            <topic guid="{generate-id()}">
                <xsl:call-template name="commonattributes"/>
            </topic>
        </xsl:for-each>
    </xsl:variable>

	<xsl:variable name="relatedTopicrefs" select="//*[contains(@class, ' map/reltable ')]//*[contains(@class, ' map/topicref ')]"/>

<!-- Root template, and topicref validation mooved from topic2fo_shell.xsl to add ability for customizaing   -->

    <xsl:template name="validateTopicRefs">
        <xsl:apply-templates select="//opentopic:map" mode="topicref-validation"/>
    </xsl:template>

    <xsl:template match="opentopic:map" mode="topicref-validation">
        <xsl:apply-templates mode="topicref-validation"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="topicref-validation">
        <xsl:if test="@href = ''">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">004</xsl:with-param>
            <xsl:with-param name="msgsev">F</xsl:with-param>
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="@href and @id">
            <xsl:variable name="searchId" select="@id"/>
            <xsl:if test="not(//*[contains(@class, ' topic/topic ')][@id = $searchId]) and not($searchId = '')">
              <xsl:call-template name="output-message">
                <xsl:with-param name="msgnum">005</xsl:with-param>
                <xsl:with-param name="msgsev">F</xsl:with-param>
                <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
              </xsl:call-template>
            </xsl:if>
        </xsl:if>
        <xsl:apply-templates mode="topicref-validation"/>
    </xsl:template>

    <xsl:template match="*" mode="topicref-validation"/>

    <xsl:template name="rootTemplate">
        <xsl:call-template name="validateTopicRefs"/>

        <fo:root xsl:use-attribute-sets="__fo__root">

            <xsl:call-template name="createLayoutMasters"/>

            <xsl:call-template name="createBookmarks"/>

            <xsl:call-template name="createFrontMatter"/>

            <xsl:if test="not($retain-bookmap-order)">
                <xsl:call-template name="createToc"/>
            </xsl:if>

<!--            <xsl:call-template name="createPreface"/>-->

            <xsl:apply-templates/>

            <xsl:if test="not($retain-bookmap-order)">
                <xsl:call-template name="createIndex"/>
            </xsl:if>

        </fo:root>
    </xsl:template>

</xsl:stylesheet>