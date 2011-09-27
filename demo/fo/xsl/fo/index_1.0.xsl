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

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:exsl="http://exslt.org/common"
    xmlns:exslf="http://exslt.org/functions"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    xmlns:comparer="com.idiominc.ws.opentopic.xsl.extension.CompareStrings"
    extension-element-prefixes="exsl"
    xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
    xmlns:ot-placeholder="http://suite-sol.com/namespaces/ot-placeholder"
    exclude-result-prefixes="opentopic-index exsl comparer opentopic-func exslf ot-placeholder">

    <xsl:template name="createIndex">
        <xsl:if test="(//opentopic-index:index.groups//opentopic-index:index.entry) and (count($index-entries//opentopic-index:index.entry) &gt; 0)">
            <xsl:variable name="index">
                <xsl:choose>
                    <xsl:when test="($ditaVersion &gt;= 1.1) and $map//*[contains(@class,' bookmap/indexlist ')][@href]"/>
                    <xsl:when test="($ditaVersion &gt;= 1.1) and $map//*[contains(@class,' bookmap/indexlist ')]">
                        <xsl:apply-templates select="/" mode="index-postprocess"/>
                    </xsl:when>
                    <xsl:when test="($ditaVersion &gt;= 1.1) and /*[contains(@class,' map/map ')][not(contains(@class,' bookmap/bookmap '))]">
                        <xsl:apply-templates select="/" mode="index-postprocess"/>
                    </xsl:when>
                    <xsl:when test="$ditaVersion &gt;= 1.1"/>
                    <xsl:otherwise>
                        <xsl:apply-templates select="/" mode="index-postprocess"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:if test="count(exsl:node-set($index)/*) > 0">
                <fo:page-sequence master-reference="index-sequence" xsl:use-attribute-sets="__force__page__count">

                    <xsl:call-template name="insertIndexStaticContents"/>

                    <fo:flow flow-name="xsl-region-body">
                        <xsl:copy-of select="exsl:node-set($index)"/>
                    </fo:flow>

                </fo:page-sequence>
            </xsl:if>
        </xsl:if>
    </xsl:template>

  <xsl:template match="ot-placeholder:indexlist[$retain-bookmap-order]">
    <xsl:call-template name="createIndex"/>
  </xsl:template>

    <xsl:template name="processIndexList">
        <fo:page-sequence master-reference="index-sequence" xsl:use-attribute-sets="__force__page__count">

            <xsl:call-template name="insertIndexStaticContents"/>

            <fo:flow flow-name="xsl-region-body">
                <fo:block xsl:use-attribute-sets="__index__label" id="{$id.index}">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Index'"/>
                    </xsl:call-template>
                </fo:block>

                <fo:block>
                    <xsl:apply-templates/>
                </fo:block>
            </fo:flow>

        </fo:page-sequence>
    </xsl:template>


</xsl:stylesheet>    