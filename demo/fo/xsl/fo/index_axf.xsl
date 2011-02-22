<?xml version="1.0" encoding="UTF-8"?>
<!--
    ============================================================
    Copyright (c) 2006 Antenna House, Inc. All rights reserved.
    Antenna House is a trademark of Antenna House, Inc.
    URL    : http://www.antennahouse.com/
    E-mail : info@antennahouse.com
    ============================================================
-->
<xsl:stylesheet version="1.1" 
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format" xmlns:exsl="http://exslt.org/common"
xmlns:opentopic="http://www.idiominc.com/opentopic"
xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
extension-element-prefixes="exsl"
exclude-result-prefixes="opentopic-index opentopic">

<xsl:template name="createIndex" >
    <xsl:comment>Antenna House Index support</xsl:comment>
    <xsl:if test="//opentopic-index:index.groups//opentopic-index:index.entry">
        <fo:page-sequence master-reference="index-sequence" xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="insertIndexStaticContents"/>
            <fo:flow flow-name="xsl-region-body" page-number-treatment="link">
                <xsl:apply-templates select="/" mode="index-postprocess"/>
                <fo:block span="all"/>
            </fo:flow>
        </fo:page-sequence>
    </xsl:if>
</xsl:template>

<xsl:template match="opentopic-index:index.entry">
    <xsl:if test="opentopic-index:refID/@value">
        <xsl:choose>
            <xsl:when test="self::opentopic-index:index.entry[@start-range='true']">
                <!--Insert ranged index entry start marker-->
                <xsl:variable name="selfIDs" select="descendant-or-self::opentopic-index:index.entry[last()]/opentopic-index:refID/@value"/>
                <xsl:for-each select="$selfIDs">
                    <xsl:variable name="selfID" select="."/>
                    <xsl:variable name="followingMarkers" select="following::opentopic-index:index.entry[descendant-or-self::opentopic-index:index.entry[last()]/opentopic-index:refID/@value = $selfID]"/>
                    <xsl:variable name="followingMarker" select="$followingMarkers[@end-range='true'][1]"/>
                    <xsl:variable name="followingStartMarker" select="$followingMarkers[@start-range='true'][1]"/>
                    <xsl:choose>
                        <xsl:when test="not($followingMarker)">
                            <xsl:if test="$warn-enabled">
                                <xsl:message>
                                    <xsl:text>[WARNING] There is no index entry found which closing range for ID="</xsl:text>
                                    <xsl:value-of select="$selfID"/>
                                    <xsl:text>"</xsl:text>
                                </xsl:message>
                             </xsl:if>
                         </xsl:when>
                         <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="$followingStartMarker and $followingStartMarker[following::*[generate-id() = generate-id($followingMarker)]]">
                                    <xsl:if test="$warn-enabled">
                                        <xsl:message>
                                            <xsl:text>[WARNING] There are multiple index entry found which is opening range for ID="</xsl:text>
                                            <xsl:value-of select="$selfID"/>
                                            <xsl:text>"</xsl:text> but there is only one which close it or ranges are overlapping. 
                                        </xsl:message>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <fo:index-range-begin id="{$selfID}_{generate-id()}" index-key="{$selfID}" />
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="self::opentopic-index:index.entry[@end-range='true']">
                <!--Insert ranged index entry end marker-->
                <xsl:variable name="selfIDs" select="descendant-or-self::opentopic-index:index.entry[last()]/opentopic-index:refID/@value"/>
                <xsl:for-each select="$selfIDs">
                    <xsl:variable name="selfID" select="."/>
                    <xsl:variable name="precMarkers" select="preceding::opentopic-index:index.entry[(@start-range or @end-range) and descendant-or-self::opentopic-index:index.entry[last()]/opentopic-index:refID/@value = $selfID]"/>
                    <xsl:variable name="precMarker" select="$precMarkers[@start-range='true'][last()]"/>
                    <xsl:variable name="precEndMarker" select="$precMarkers[@end-range='true'][last()]"/>
                    <xsl:choose>
                        <xsl:when test="not($precMarker)">
                            <xsl:if test="$warn-enabled">
                                <xsl:message>
                                    <xsl:text>[WARNING] There is no index entry found which opening range for ID="</xsl:text>
                                    <xsl:value-of select="$selfID"/>
                                    <xsl:text>"</xsl:text>
                                </xsl:message>
                            </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:choose>
                                <xsl:when test="$precEndMarker and $precEndMarker[preceding::*[generate-id() = generate-id($precMarker)]]">
                                    <xsl:if test="$warn-enabled">
                                        <xsl:message>
                                            <xsl:text>[WARNING] There are multiple index entry found which closing range for ID="</xsl:text>
                                            <xsl:value-of select="$selfID"/>
                                            <xsl:text>"</xsl:text>
                                        </xsl:message>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:for-each select="$precMarker//opentopic-index:refID[@value = $selfID]/@value">
                                        <fo:index-range-end ref-id="{$selfID}_{generate-id()}" />
                                    </xsl:for-each>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>
        </xsl:choose>
        <!--Insert simple index entry marker-->
        <!-- edited by william on 2009-07-13 for bug:2819853 start -->
        <!--xsl:for-each select="descendant::opentopic-index:refID[last()]">
            <fo:inline index-key="{@value}"/>
        </xsl:for-each-->
        <xsl:choose>
            <!--xsl:when test="opentopic-index:index.entry"/-->
            <xsl:when test="opentopic-index:index.entry">
                <xsl:for-each select="child::opentopic-index:refID[last()]">
                    <fo:inline index-key="{@value}"/>
                </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="child::opentopic-index:refID[last()]">
                    <fo:inline index-key="{@value}"/>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
        <!-- edited by william on 2009-07-13 for bug:2819853 end -->
        <xsl:apply-templates/>
    </xsl:if>
</xsl:template>

<xsl:template match="/" mode="index-postprocess">
    <fo:block xsl:use-attribute-sets="__index__label">
        <xsl:attribute name="id">ID_INDEX_00-0F-EA-40-0D-4D</xsl:attribute>
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Index'"/>
        </xsl:call-template>
    </fo:block>
    <xsl:apply-templates select="//opentopic-index:index.groups" mode="index-postprocess"/>
</xsl:template>

<xsl:template match="opentopic-index:index.entry" mode="index-postprocess">
    <xsl:variable name="value" select="@value"/>
    <xsl:choose>
        <xsl:when test="opentopic-index:index.entry">
            <fo:table>
                <fo:table-body>
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block xsl:use-attribute-sets="index-indents" keep-with-next="always">
                                <xsl:if test="count(ancestor::opentopic-index:index.entry) > 0">
                                    <xsl:attribute name="keep-together.within-page">always</xsl:attribute>
                                </xsl:if>
                                <xsl:variable name="following-idx" select="following-sibling::opentopic-index:index.entry[@value = $value and opentopic-index:refID]"/>
                                <xsl:if test="count(preceding-sibling::opentopic-index:index.entry[@value = $value]) = 0">
                                    <xsl:variable name="page-setting" select=" (ancestor-or-self::opentopic-index:index.entry/@no-page | ancestor-or-self::opentopic-index:index.entry/@start-page)[last()]"/>
                                    <xsl:variable name="isNoPage" select=" $page-setting = 'true' and name($page-setting) = 'no-page' "/>
                                    <xsl:variable name="refID" select="opentopic-index:refID/@value"/>
                                    <xsl:choose>
                                        <xsl:when test="$index-entries/opentopic-index:index.entry[(@value = $value) and (opentopic-index:refID/@value = $refID)][not(opentopic-index:index.entry)]">
                                            <xsl:call-template name="make-index-ref">
                                                <xsl:with-param name="idxs" select="opentopic-index:refID"/>
                                                <xsl:with-param name="inner-text" select="opentopic-index:formatted-value"/>
                                                <xsl:with-param name="no-page" select="$isNoPage"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:call-template name="make-index-ref">
                                                <xsl:with-param name="inner-text" select="opentopic-index:formatted-value"/>
                                                <xsl:with-param name="no-page" select="$isNoPage"/>
                                            </xsl:call-template>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:if>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
                <fo:table-body>
                    <fo:table-row>
                        <fo:table-cell>
                            <fo:block xsl:use-attribute-sets="index.entry__content">
                                <xsl:apply-templates mode="index-postprocess"/>
                            </fo:block>
                        </fo:table-cell>
                    </fo:table-row>
                </fo:table-body>
            </fo:table>
        </xsl:when>
        <xsl:otherwise>
            <fo:block xsl:use-attribute-sets="index-indents">
                <xsl:if test="count(ancestor::opentopic-index:index.entry) > 0">
                    <xsl:attribute name="keep-together.within-page">always</xsl:attribute>
                </xsl:if>
                <xsl:variable name="following-idx" select="following-sibling::opentopic-index:index.entry[@value = $value and opentopic-index:refID]"/>
                <xsl:if test="count(preceding-sibling::opentopic-index:index.entry[@value = $value]) = 0">
                    <xsl:variable name="page-setting" select=" (ancestor-or-self::opentopic-index:index.entry/@no-page | ancestor-or-self::opentopic-index:index.entry/@start-page)[last()]"/>
                    <xsl:variable name="isNoPage" select=" $page-setting = 'true' and name($page-setting) = 'no-page' "/>
                    <xsl:call-template name="make-index-ref">
                        <xsl:with-param name="idxs" select="opentopic-index:refID"/>
                        <xsl:with-param name="inner-text" select="opentopic-index:formatted-value"/>
                        <xsl:with-param name="no-page" select="$isNoPage"/>
                    </xsl:call-template>
                </xsl:if>
            </fo:block>
            <fo:block xsl:use-attribute-sets="index.entry__content">
                <xsl:apply-templates mode="index-postprocess"/>
            </fo:block>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="make-index-ref">
    <xsl:param name="idxs"/>
    <xsl:param name="inner-text"/>
    <xsl:param name="no-page"/>
    <fo:block>
        <xsl:if test="position() = 1">
            <xsl:attribute name="keep-with-previous">always</xsl:attribute>
        </xsl:if>
        <fo:inline>
            <xsl:call-template name="__formatText">
                <xsl:with-param name="text" select="$inner-text"/>
            </xsl:call-template>
        </fo:inline>
        <xsl:if test="not($no-page)">
            <xsl:if test="$idxs and count($idxs) &gt; 0">
                <xsl:text> </xsl:text>
                <fo:index-page-citation-list>
                    <fo:index-key-reference ref-index-key="{$idxs/@value}"/>
                </fo:index-page-citation-list>
            </xsl:if>
        </xsl:if>
    </fo:block>
</xsl:template>

</xsl:stylesheet>
