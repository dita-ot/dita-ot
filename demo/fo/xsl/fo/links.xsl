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
    xmlns:opentopic-mapmerge="http://www.idiominc.com/opentopic/mapmerge"
    xmlns:exsl="http://exslt.org/common"
    xmlns:exslf="http://exslt.org/functions"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    exclude-result-prefixes="opentopic-mapmerge opentopic-func exslf exsl"
    version="1.1">

    <xsl:include href="../../cfg/fo/attrs/links-attr.xsl"/>

    <xsl:key name="key_anchor" match="*[@id][not(contains(@class,' map/topicref '))]" use="@id"/>
<!--[not(contains(@class,' map/topicref '))]-->
    <xsl:template name="insertLinkShortDesc">
        <xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>
        <xsl:variable name="element" select="key('key_anchor',$destination)[1]"/>
        <xsl:if test="$element/*[contains(@class, ' topic/shortdesc ')]">
            <fo:block xsl:use-attribute-sets="link__shortdesc">
                <xsl:apply-templates select="$element/*[contains(@class, ' topic/shortdesc ')]"/>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="insertLinkDesc">
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Link description'"/>
            <xsl:with-param name="theParameters">
                <desc>
                    <fo:inline>
                        <xsl:apply-templates select="*[contains(@class,' topic/desc ')]" mode="insert-description"/>
                    </fo:inline>
                </desc>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/xref ') or contains(@class, ' topic/link ')]/*[contains(@class,' topic/desc ')]" priority="1"/>
    <xsl:template match="*[contains(@class,' topic/desc ')]" mode="insert-description">
        <xsl:apply-templates/>
    </xsl:template>



    <xsl:template name="insertReferenceTitle">
        <xsl:param name="href"/>
        <xsl:param name="titlePrefix"/>

        <xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>

        <xsl:if test="not($titlePrefix = '')">
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="$titlePrefix"/>
            </xsl:call-template>
        </xsl:if>

        <xsl:variable name="element" select="key('key_anchor',$destination)[1]"/>

        <xsl:choose>
            <xsl:when test="not($element) or ($destination = '')">
                <xsl:choose>
                    <xsl:when test="* | text()">
                        <xsl:apply-templates/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$href"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>

            <xsl:when test="$element[contains(@class, ' topic/fig ')]/*[contains(@class, ' topic/title ')]">
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'Figure'"/>
                    <xsl:with-param name="theParameters">
                        <number>
                            <xsl:for-each select="$element">
                                <xsl:value-of select="count(preceding::*[contains(@class, ' topic/fig ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                            </xsl:for-each>
                        </number>
                        <title>
                            <xsl:apply-templates select="$element/*[contains(@class, ' topic/title ')]" mode="process-title"/>
                        </title>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$element[contains(@class, ' topic/section ')]">
                <xsl:choose>
                    <xsl:when test="$element/*[contains(@class, ' topic/title ')]">
                        <xsl:variable name="title">
                            <xsl:apply-templates select="$element/*[contains(@class, ' topic/title ')]" mode="process-title"/>
                        </xsl:variable>
                        <xsl:value-of select="normalize-space($title)"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:when>

            <xsl:when test="$element[contains(@class, ' topic/li ')]">
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'List item'"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$element[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]">
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'Table'"/>
                    <xsl:with-param name="theParameters">
                        <number>
                            <xsl:for-each select="$element">
                                <xsl:value-of select="count(preceding::*[contains(@class, ' topic/table ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                            </xsl:for-each>
                        </number>
                        <title>
                            <xsl:apply-templates select="$element/*[contains(@class, ' topic/title ')]" mode="process-title"/>
                        </title>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$element[contains(@class, ' topic/fn ')]">
                <xsl:call-template name="insertVariable">
                    <xsl:with-param name="theVariableID" select="'Foot note'"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$element/*[contains(@class, ' topic/title ')]">
                <xsl:value-of select="string($element/*[contains(@class, ' topic/title ')])"/>
            </xsl:when>

            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="* | text()">
                        <xsl:apply-templates/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$href"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/xref ')]">
        <fo:inline id="{@id}"/>

        <xsl:variable name="referenceTitle">
            <xsl:call-template name="insertReferenceTitle">
                <xsl:with-param name="href" select="@href"/>
                <xsl:with-param name="titlePrefix" select="''"/>
            </xsl:call-template>
        </xsl:variable>

        <fo:basic-link xsl:use-attribute-sets="xref">
            <xsl:call-template name="buildBasicLinkDestination">
                <xsl:with-param name="scope" select="@scope"/>
                <xsl:with-param name="href" select="@href"/>
            </xsl:call-template>

            <xsl:choose>
                <xsl:when test="not(@scope = 'external') and not($referenceTitle = '')">
                    <xsl:copy-of select="$referenceTitle"/>
                </xsl:when>
                <xsl:when test="not(@scope = 'external')">
                    <xsl:call-template name="insertPageNumberCitation">
                        <xsl:with-param name="isTitleEmpty" select="'yes'"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:choose>
                        <xsl:when test="* | text()">
                            <xsl:apply-templates select="*[not(contains(@class,' topic/desc '))] | text()" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="@href"/> 
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </fo:basic-link>
<!--
        Disable because of the CQ#8102 bug
        <xsl:if test="*[contains(@class,' topic/desc ')]">
            <xsl:call-template name="insertLinkDesc"/>
        </xsl:if>
-->
        <xsl:if test="not(@scope = 'external') and not($referenceTitle = '')">
            <xsl:call-template name="insertPageNumberCitation"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/related-links ')]">
        <xsl:if test="$disableRelatedLinks = 'no'">
            <fo:block xsl:use-attribute-sets="related-links">
                <fo:block xsl:use-attribute-sets="related-links__content">
                    <xsl:apply-templates/>
                </fo:block>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template name="getLinkScope">
        <xsl:choose>
            <xsl:when test="@scope">
                <xsl:value-of select="@scope"/>
            </xsl:when>
            <xsl:when test="contains(@class, ' topic/related-links ')">
                <xsl:value-of select="'local'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="..">
                    <xsl:call-template name="getLinkScope"/>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/link ')]">
        <xsl:variable name="referenceTitle">
            <xsl:call-template name="insertReferenceTitle">
                <xsl:with-param name="href" select="@href"/>
                <xsl:with-param name="titlePrefix" select="''"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="linkScope">
            <xsl:call-template name="getLinkScope"/>
        </xsl:variable>

        <fo:block xsl:use-attribute-sets="link">
            <xsl:text>&#x2022; </xsl:text>
            <fo:inline xsl:use-attribute-sets="link__content">
                <fo:basic-link>
                    <xsl:call-template name="buildBasicLinkDestination">
                        <xsl:with-param name="scope" select="$linkScope"/>
                        <xsl:with-param name="href" select="@href"/>
                    </xsl:call-template>
                    <xsl:choose>
                        <xsl:when test="not($linkScope = 'external') and not($referenceTitle = '')">
                            <xsl:copy-of select="$referenceTitle"/>
                        </xsl:when>
                        <xsl:when test="not($linkScope = 'external')">
                            <xsl:call-template name="insertPageNumberCitation">
                                <xsl:with-param name="isTitleEmpty" select="'yes'"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:basic-link>
            </fo:inline>
            <xsl:if test="not($linkScope = 'external') and not($referenceTitle = '')">
                <xsl:call-template name="insertPageNumberCitation"/>
            </xsl:if>
<!--
            Disable because of the CQ#8102 bug
            <xsl:if test="*[contains(@class, ' topic/desc ')]">
                <xsl:call-template name="insertLinkDesc"/>
            </xsl:if>
-->
            <xsl:if test="not($linkScope = 'external')">
                <xsl:call-template name="insertLinkShortDesc"/>
            </xsl:if>
        </fo:block>
    </xsl:template>

    <xsl:template name="buildBasicLinkDestination">
        <xsl:param name="scope"/>
        <xsl:param name="href"/>
        <xsl:choose>
            <xsl:when test="$scope = 'external'">
                <xsl:attribute name="external-destination">
                    <xsl:value-of select="concat('url(', $href, ')')"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="internal-destination">
                    <xsl:value-of select="opentopic-func:getDestinationId($href)"/>
                </xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="insertPageNumberCitation">
        <xsl:param name="isTitleEmpty"/>
        <xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>
        <xsl:variable name="element" select="ancestor::*[last()]//*[@id = $destination]"/>
        <xsl:choose>
            <xsl:when test="not($element) or ($destination = '')"/>
            <xsl:when test="$isTitleEmpty">
                <fo:inline>
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Page'"/>
                        <xsl:with-param name="theParameters">
                            <pagenum>
                                <fo:inline>
                                    <fo:page-number-citation ref-id="{$destination}"/>
                                </fo:inline>
                            </pagenum>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:inline>
            </xsl:when>
            <xsl:otherwise>
                <fo:inline>
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'On the page'"/>
                        <xsl:with-param name="theParameters">
                            <pagenum>
                                <fo:inline>
                                    <fo:page-number-citation ref-id="{$destination}"/>
                                </fo:inline>
                            </pagenum>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:inline>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linktext ')]">
        <fo:inline xsl:use-attribute-sets="linktext">
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/linklist ')]">
        <fo:block xsl:use-attribute-sets="linklist">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linkinfo ')]">
        <fo:block xsl:use-attribute-sets="linkinfo">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linkpool ')]">
        <fo:block xsl:use-attribute-sets="linkpool">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <exslf:function name="opentopic-func:getDestinationId">
        <xsl:param name="href"/>
        <xsl:variable name="destination">
            <xsl:variable name="topic-id">
                <xsl:value-of select="substring-after($href, '#')"/>
            </xsl:variable>

            <xsl:variable name="element-id">
                <xsl:value-of select="substring-after($topic-id, '/')"/>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="$element-id = ''">
                    <xsl:value-of select="$topic-id"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$element-id"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <exslf:result select="$destination"/>
    </exslf:function>


</xsl:stylesheet>