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
    xmlns:rx="http://www.renderx.com/XSL/Extensions"
    exclude-result-prefixes="opentopic-index opentopic opentopic-i18n opentopic-func"
    version="1.1">
    
    <xsl:variable name="layout-masters">
        <xsl:value-of select="'cfg:fo/layout-masters.xml'"/>
    </xsl:variable>

    <xsl:variable name="index-configuration">
        <xsl:value-of select="concat('cfg:common/index/', $locale, '.xml')"/>
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
        <xsl:variable name="mapProdname" select="/*/opentopic:map//*[contains(@class, ' topic/prodname ')]"/>
        <xsl:variable name="bkinfoProdname" select="/*/*[contains(@class, ' bkinfo/bkinfo ')]//*[contains(@class, ' topic/prodname ')]"/>
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
            <topic id="{@id}" guid="{generate-id()}"/>
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
            <xsl:message>[ERROR] Empty href was specified for some topic reference !</xsl:message>
            <xsl:message terminate="yes">[ERROR] Please correct your ditamap or bookmap file.</xsl:message>
        </xsl:if>
        <xsl:if test="@href and @id">
            <xsl:variable name="searchId" select="@id"/>
            <xsl:if test="not(//*[contains(@class, ' topic/topic ')][@id = $searchId]) and not($searchId = '')">
                <xsl:message>[ERROR] Topic reference (href : <xsl:value-of select="@href"/>) not found !</xsl:message>
                <xsl:message terminate="yes">[ERROR] Reference may be incorrect. Please correct your ditamap or bookmap file.</xsl:message>
            </xsl:if>
        </xsl:if>
        <xsl:apply-templates mode="topicref-validation"/>
    </xsl:template>

    <xsl:template match="*" mode="topicref-validation"/>

    <xsl:template name="rootTemplate">
        <xsl:call-template name="validateTopicRefs"/>

        <fo:root xsl:use-attribute-sets="__fo__root">

            <xsl:comment>
                <xsl:text>Layout masters = </xsl:text>
                <xsl:value-of select="$layout-masters"/>
            </xsl:comment>

            <xsl:apply-templates select="document($layout-masters)/*" mode="layout-masters-processing"/>

            <xsl:call-template name="createBookmarks"/>

            <xsl:call-template name="createFrontMatter"/>

            <xsl:call-template name="createToc"/>

<!--            <xsl:call-template name="createPreface"/>-->

            <xsl:apply-templates/>

            <xsl:call-template name="createIndex"/>

        </fo:root>
    </xsl:template>

</xsl:stylesheet>