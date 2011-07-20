<?xml version='1.0' encoding="UTF-8"?>

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
    xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
    xmlns:exsl="http://exslt.org/common"
    xmlns:exslf="http://exslt.org/functions"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    extension-element-prefixes="exsl"
    exclude-result-prefixes="opentopic-func exslf exsl dita2xslfo"
    version="2.0">

    <xsl:variable name="tableAttrs" select="'../../cfg/fo/attrs/tables-attr.xsl'"/>

    <xsl:param name="tableSpecNonProportional" select="'false'"/>

    <xsl:template name="blank.spans">
        <xsl:param name="cols" select="1"/>
        <xsl:if test="$cols &gt; 0">
            <xsl:text>0:</xsl:text>
            <xsl:call-template name="blank.spans">
                <xsl:with-param name="cols" select="$cols - 1"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="getTableAttrubute">
        <xsl:param name="entry"/>
        <xsl:param name="attribute"/>
        <xsl:param name="colnum"/>

        <xsl:variable name="parentRow" select="$entry/ancestor-or-self::*[contains(@class, ' topic/row ')][1]"/>
        <xsl:variable name="parentTgroup" select="$parentRow/ancestor::*[contains(@class, ' topic/tgroup ')][1]"/>
        <xsl:variable name="entryAttributeValue">
            <xsl:value-of select="$entry/@*[local-name() = $attribute]"/>
        </xsl:variable>
        <xsl:variable name="rowAttributeValue">
            <xsl:value-of select="$parentRow/@*[local-name() = $attribute]"/>
        </xsl:variable>
        <xsl:variable name="tgroupAttributeValue">
            <xsl:value-of select="$parentTgroup/@*[local-name() = $attribute]"/>
        </xsl:variable>
        <xsl:variable name="entrySpanAttributeValue">
            <xsl:if test="$entry/@spanname">
                <xsl:variable name="entrySpanName" select="$entry/@spanname"/>
                <xsl:variable name="entrySpanspec" select="$parentTgroup/*[contains(@class, ' topic/spanspec ')][@spanname=$entrySpanName]"/>
                <xsl:variable name="entrySpanColspec" select="$parentTgroup/*[contains(@class, ' topic/colspec ')][@colname=$entrySpanspec/@namest]"/>

                <xsl:variable name="entrySpanspecValue">
                    <xsl:value-of select="$entrySpanspec/@*[local-name() = $attribute]"/>
                </xsl:variable>
                <xsl:variable name="entrySpanColspecValue">
                    <xsl:value-of select="$entrySpanColspec/@*[local-name() = $attribute]"/>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="$entrySpanspecValue != ''">
                        <xsl:value-of select="$entrySpanspecValue"/>
                    </xsl:when>
                    <xsl:when test="$entrySpanColspecValue != ''">
                        <xsl:value-of select="$entrySpanColspecValue"/>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="entryNamestAttributeValue">
            <xsl:if test="$entry/@namest">
                <xsl:variable name="entryNamest" select="$entry/@namest"/>
                <xsl:variable name="entryColspec" select="$parentTgroup/*[contains(@class, ' topic/colspec ')][@colname=$entryNamest]"/>
                <xsl:variable name="entryNamestVal">
                    <xsl:value-of select="$entryColspec/@*[local-name() = $attribute]"/>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="$entryNamestVal">
                        <xsl:value-of select="$entryNamestVal"/>
                    </xsl:when>
                    <xsl:otherwise></xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="defaultAttributeValue">
            <xsl:choose>
                <xsl:when test="$tgroupAttributeValue != ''">
                    <xsl:value-of select="$tgroupAttributeValue"/>
                </xsl:when>
                <xsl:when test="$attribute = 'rowsep'">1</xsl:when>
                <xsl:when test="$attribute = 'colsep'">1</xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$entryAttributeValue != ''">
                <xsl:value-of select="$entryAttributeValue"/>
            </xsl:when>
            <xsl:when test="$rowAttributeValue != ''">
                <xsl:value-of select="$rowAttributeValue"/>
            </xsl:when>
            <xsl:when test="$entrySpanAttributeValue != ''">
                <xsl:value-of select="$entrySpanAttributeValue"/>
            </xsl:when>
            <xsl:when test="$entryNamestAttributeValue != ''">
                <xsl:value-of select="$entryNamestAttributeValue"/>
            </xsl:when>
            <xsl:when test="$colnum &gt; 0">
                <xsl:variable name="columnValue">
                    <xsl:call-template name="columnColspec">
                        <xsl:with-param name="colnum" select="$colnum"/>
                        <xsl:with-param name="attribute" select="$attribute"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$columnValue != ''">
                        <xsl:value-of select="$columnValue"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$defaultAttributeValue"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$defaultAttributeValue"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="columnColspec">
        <xsl:param name="colnum" select="0"/>
        <xsl:param name="attribute" select="'colname'"/>
        <xsl:param name="colspecs" select="ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/colspec ')]"/>
        <xsl:param name="count" select="1"/>

        <xsl:choose>
            <xsl:when test="not($colspecs) or $count &gt; $colnum"></xsl:when>
            <xsl:when test="$colspecs[1]/@colnum">
                <xsl:choose>
                    <xsl:when test="$colspecs[1]/@colnum = $colnum">
                        <xsl:value-of select="$colspecs[1]/@*[local-name() = $attribute]"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="columnColspec">
                            <xsl:with-param name="colnum" select="$colnum"/>
                            <xsl:with-param name="attribute" select="$attribute"/>
                            <xsl:with-param name="colspecs" select="$colspecs[position() &gt; 1]"/>
                            <xsl:with-param name="count" select="$colspecs[1]/@colnum + 1"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$count = $colnum">
                        <xsl:value-of select="$colspecs[1]/@*[local-name() = $attribute]"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="columnColspec">
                            <xsl:with-param name="colnum" select="$colnum"/>
                            <xsl:with-param name="attribute" select="$attribute"/>
                            <xsl:with-param name="colspecs" select="$colspecs[position() &gt; 1]"/>
                            <xsl:with-param name="count" select="$count + 1"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getTableColsep">
        <xsl:variable name="spanname" select="@spanname"/>
        <xsl:variable name="colname" select="@colname"/>
        <xsl:choose>
            <xsl:when test="@colsep">
                <xsl:value-of select="@colsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/spanspec ')][@spanname = $spanname]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/spanspec ')][@spanname = $spanname]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@colsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@colsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@colsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@colsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/table ')][1]/@colsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/table ')][1]/@colsep"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="getTableRowsep">
        <xsl:variable name="colname" select="@colname"/>
        <xsl:variable name="spanname" select="@spanname"/>
        <xsl:choose>
            <xsl:when test="@rowsep">
                <xsl:value-of select="@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/row ')][1]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/row ')][1]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/spanspec ')][@spanname = $spanname]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/spanspec ')][@spanname = $spanname]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/colspec ')][@colname = $colname]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@rowsep"/>
            </xsl:when>
            <xsl:when test="ancestor::*[contains(@class, ' topic/table ')][1]/@rowsep">
                <xsl:value-of select="ancestor::*[contains(@class, ' topic/table ')][1]/@rowsep"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="generateTableEntryBorder">
        <xsl:variable name="colsep">
            <xsl:call-template name="getTableColsep"/>
        </xsl:variable>
        <xsl:variable name="rowsep">
            <xsl:call-template name="getTableRowsep"/>
        </xsl:variable>
        <xsl:variable name="frame" select="ancestor::*[contains(@class, ' topic/table ')][1]/@frame"/>
        <xsl:variable name="needTopBorderOnBreak">
            <xsl:choose>
                <xsl:when test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
                    <xsl:choose>
                        <xsl:when test="../parent::node()[contains(@class, ' topic/thead ')]">
                            <xsl:value-of select="'true'"/>
                        </xsl:when>
                        <xsl:when test="(../parent::node()[contains(@class, ' topic/tbody ')]) and not(../preceding-sibling::*[contains(@class, ' topic/row ')])">
                            <xsl:value-of select="'true'"/>
                        </xsl:when>
                        <xsl:when test="../parent::node()[contains(@class, ' topic/tbody ')]">
                            <xsl:variable name="entryNum" select="count(preceding-sibling::*[contains(@class, ' topic/entry ')]) + 1"/>
                            <xsl:variable name="prevEntryRowsep">
                                <xsl:for-each select="../preceding-sibling::*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')][$entryNum]">
                                    <xsl:call-template name="getTableRowsep"/>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:choose>
                                <xsl:when test="number($prevEntryRowsep)">
                                    <xsl:value-of select="'true'"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="'false'"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="'false'"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'false'"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="number($rowsep) and (../parent::node()[contains(@class, ' topic/thead ')])">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'thead__tableframe__bottom'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="number($rowsep) and ((../following-sibling::*[contains(@class, ' topic/row ')]) or (../parent::node()[contains(@class, ' topic/tbody ')] and ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class, ' topic/tfoot ')]))">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__bottom'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="$needTopBorderOnBreak = 'true'">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="number($colsep) and following-sibling::*[contains(@class, ' topic/entry ')]">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="number($colsep) and not(following-sibling::*[contains(@class, ' topic/entry ')]) and ((count(preceding-sibling::*)+1) &lt; ancestor::*[contains(@class, ' topic/tgroup ')][1]/@cols)">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="emptyTableCell">
        <xsl:param name="colnum" select="0"/>
        <xsl:param name="parent"/>

        <fo:table-cell>
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="concat($parent, '.row.entry')"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>

            <xsl:call-template name="generateTableEntryBorder"/>

            <fo:block>
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="concat($parent, '.row.entry__content')"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="generate.colgroup">
        <xsl:param name="cols" select="1"/>
        <xsl:param name="count" select="1"/>

        <xsl:choose>
            <xsl:when test="$count &gt; $cols"></xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="generate.col">
                    <xsl:with-param name="countcol" select="$count"/>
                </xsl:call-template>
                <xsl:call-template name="generate.colgroup">
                    <xsl:with-param name="cols" select="$cols"/>
                    <xsl:with-param name="count" select="$count + 1"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="generate.col">
        <xsl:param name="countcol">1</xsl:param>
        <xsl:param name="colspecs" select="./*[contains(@class, ' topic/colspec ')]"/>
        <xsl:param name="count">1</xsl:param>
        <xsl:param name="colnum">1</xsl:param>

        <xsl:choose>
            <xsl:when test="$count &gt; count($colspecs)">
                <fo:table-column column-number="{$countcol}">
                    <xsl:variable name="colwidth">
                        <xsl:call-template name="calculateColumnWidth"/>
                    </xsl:variable>
                    <xsl:if test="$colwidth != 'proportional-column-width(1)' and normalize-space($colwidth) != ''">
                        <xsl:attribute name="column-width">
                            <xsl:value-of select="$colwidth"/>
                        </xsl:attribute>
                    </xsl:if>
                </fo:table-column>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="colspec" select="$colspecs[$count = position()]"/>

                <xsl:variable name="colspec.colnum">
                    <xsl:choose>
                        <xsl:when test="$colspec/@colnum">
                            <xsl:value-of select="$colspec/@colnum"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$colnum"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:variable name="colspec.colwidth">
                    <xsl:choose>
                        <xsl:when test="$colspec/@colwidth">
                            <xsl:value-of select="$colspec/@colwidth"/>
                        </xsl:when>
                        <xsl:otherwise>1*</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>

                <xsl:choose>
                    <xsl:when test="$colspec.colnum = $countcol">
                        <fo:table-column column-number="{$countcol}">
                            <xsl:variable name="colwidth">
                                <xsl:call-template name="calculateColumnWidth">
                                    <xsl:with-param name="colwidth">
                                        <xsl:value-of select="$colspec.colwidth"/>
                                    </xsl:with-param>
                                </xsl:call-template>
                            </xsl:variable>
                            <xsl:if test="$colwidth != 'proportional-column-width(1)'">
                                <xsl:attribute name="column-width">
                                    <xsl:value-of select="$colwidth"/>
                                </xsl:attribute>
                            </xsl:if>
                        </fo:table-column>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="generate.col">
                            <xsl:with-param name="countcol" select="$countcol"/>
                            <xsl:with-param name="colspecs" select="$colspecs"/>
                            <xsl:with-param name="count" select="$count + 1"/>
                            <xsl:with-param name="colnum">
                                <xsl:choose>
                                    <xsl:when test="$colspec/@colnum">
                                        <xsl:value-of select="$colspec/@colnum + 1"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$colnum + 1"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:with-param>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="calculateColumnWidth">
        <xsl:param name="colwidth">1*</xsl:param>

        <xsl:choose>
            <xsl:when test="$tableSpecNonProportional = 'true'">
                <xsl:call-template name="calculateColumnWidth.nonProportional">
                    <xsl:with-param name="colwidth" select="$colwidth"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="calculateColumnWidth.Proportional">
                    <xsl:with-param name="colwidth" select="$colwidth"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="calculateColumnWidth.Proportional">
        <xsl:param name="colwidth"/>

        <xsl:if test="contains($colwidth, '*')">
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:choose>
                <xsl:when test="substring-before($colwidth, '*') != ''">
                    <xsl:value-of select="substring-before($colwidth, '*')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>1.00</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>)</xsl:text>
        </xsl:if>

        <xsl:variable name="width-units">
            <xsl:choose>
                <xsl:when test="contains($colwidth, '*')">
                    <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($colwidth)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="width" select="normalize-space(translate($width-units, '+-0123456789.abcdefghijklmnopqrstuvwxyz', '+-0123456789.'))"/>

        <xsl:if test="$width != ''">
            <xsl:text>proportional-column-width(</xsl:text>
                <xsl:value-of select="$width"/>
            <xsl:text>)</xsl:text>
        </xsl:if>
    </xsl:template>

    <xsl:template name="calculateColumnWidth.nonProportional">
        <xsl:param name="colwidth"/>

        <xsl:if test="contains($colwidth, '*')">
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:choose>
                <xsl:when test="substring-before($colwidth, '*') != ''">
                    <xsl:value-of select="substring-before($colwidth, '*')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>1.00</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>)</xsl:text>
        </xsl:if>

        <xsl:variable name="width-units">
            <xsl:choose>
                <xsl:when test="contains($colwidth, '*')">
                    <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($colwidth)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="width" select="normalize-space(translate($width-units, '+-0123456789.abcdefghijklmnopqrstuvwxyz', '+-0123456789.'))"/>

        <xsl:variable name="units" select="normalize-space(translate($width-units, 'abcdefghijklmnopqrstuvwxyz+-0123456789.', 'abcdefghijklmnopqrstuvwxyz'))"/>

        <xsl:value-of select="$width"/>

        <xsl:choose>
            <xsl:when test="$units = 'pi'">pc</xsl:when>
            <xsl:when test="$units = '' and $width != ''">pt</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$units"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="entry.colnum">
        <xsl:param name="entry" select="."/>

        <xsl:choose>
            <xsl:when test="$entry/@spanname">
                <xsl:variable name="spanname" select="$entry/@spanname"/>
                <xsl:variable name="spanspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/spanspec ')][@spanname = $spanname]"/>
                <xsl:variable name="colspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/colspec ')][@colname = $spanspec/@namest]"/>
                <xsl:call-template name="colspec.colnum">
                    <xsl:with-param name="colspec" select="$colspec"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$entry/@colname">
                <xsl:variable name="colname" select="$entry/@colname"/>
                <xsl:variable name="colspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/colspec ')][@colname = $colname]"/>
                <xsl:call-template name="colspec.colnum">
                    <xsl:with-param name="colspec" select="$colspec"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$entry/@namest">
                <xsl:variable name="namest" select="$entry/@namest"/>
                <xsl:variable name="colspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/colspec ')][@colname = $namest]"/>
                <xsl:call-template name="colspec.colnum">
                    <xsl:with-param name="colspec" select="$colspec"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>0</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="colspec.colnum">
        <xsl:param name="colspec" select="."/>
        <xsl:choose>
            <xsl:when test="$colspec/@colnum">
                <xsl:value-of select="$colspec/@colnum"/>
            </xsl:when>
            <xsl:when test="$colspec/preceding-sibling::*[contains(@class, ' topic/colspec ')]">
                <xsl:variable name="precedingColspecColnum">
                    <xsl:call-template name="colspec.colnum">
                        <xsl:with-param name="colspec" select="$colspec/preceding-sibling::*[contains(@class, ' topic/colspec ')][1]"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:value-of select="$precedingColspecColnum + 1"/>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="calculate.colspan">
        <xsl:param name="entry" select="."/>
        <xsl:variable name="spanname" select="$entry/@spanname"/>
        <xsl:variable name="spanspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/spanspec ')][@spanname = $spanname]"/>

        <xsl:variable name="namest">
            <xsl:choose>
                <xsl:when test="@spanname">
                    <xsl:value-of select="$spanspec/@namest"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$entry/@namest"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="nameend">
            <xsl:choose>
                <xsl:when test="@spanname">
                    <xsl:value-of select="$spanspec/@nameend"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$entry/@nameend"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="startCol">
            <xsl:call-template name="colspec.colnum">
                <xsl:with-param name="colspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/colspec ')][@colname = $namest]"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="endCol">
            <xsl:call-template name="colspec.colnum">
                <xsl:with-param name="colspec" select="$entry/ancestor::*[contains(@class, ' topic/tgroup ')]/*[contains(@class, ' topic/colspec ')][@colname=$nameend]"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$namest != '' and $nameend != ''">
                <xsl:choose>
                    <xsl:when test="$endCol &gt;= $startCol">
                        <xsl:value-of select="$endCol - $startCol + 1"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$startCol - $endCol + 1"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>1</xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="calculate.following.spans">
        <xsl:param name="colspan" select="1"/>
        <xsl:param name="spans" select="''"/>

        <xsl:choose>
            <xsl:when test="$colspan &gt; 0">
                <xsl:call-template name="calculate.following.spans">
                    <xsl:with-param name="colspan" select="$colspan - 1"/>
                    <xsl:with-param name="spans" select="substring-after($spans,':')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$spans"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="finaltd">
        <xsl:param name="spans"/>
        <xsl:param name="col" select="0"/>
        <xsl:param name="parent"/>

        <xsl:if test="$spans != ''">
            <xsl:choose>
                <xsl:when test="starts-with($spans,'0:')">
                    <xsl:call-template name="emptyTableCell">
                        <xsl:with-param name="colnum" select="$col"/>
                        <xsl:with-param name="parent" select="$parent"/>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise></xsl:otherwise>
            </xsl:choose>

            <xsl:call-template name="finaltd">
                <xsl:with-param name="spans" select="substring-after($spans,':')"/>
                <xsl:with-param name="col" select="$col + 1"/>
                <xsl:with-param name="parent" select="$parent"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="sfinaltd">
        <xsl:param name="spans"/>

        <xsl:if test="$spans != ''">
            <xsl:choose>
                <xsl:when test="starts-with($spans,'0:')">0:</xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="substring-before($spans,':')-1"/>
                    <xsl:text>:</xsl:text>
                </xsl:otherwise>
            </xsl:choose>

            <xsl:call-template name="sfinaltd">
                <xsl:with-param name="spans" select="substring-after($spans,':')"/>
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <xsl:template name="copy-string">
        <xsl:param name="string"/>
        <xsl:param name="count" select="0"/>
        <xsl:param name="result"/>

        <xsl:choose>
            <xsl:when test="$count &gt; 0">
                <xsl:call-template name="copy-string">
                    <xsl:with-param name="string" select="$string"/>
                    <xsl:with-param name="count" select="$count - 1"/>
                    <xsl:with-param name="result">
                        <xsl:value-of select="$result"/>
                        <xsl:value-of select="$string"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$result"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--Table container-->
    <xsl:template name="getTableScale">
        <xsl:value-of select="ancestor-or-self::*[contains(@class, ' topic/table ')][1]/@scale"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/table ')]">
        <xsl:variable name="scale">
            <xsl:call-template name="getTableScale"/>
        </xsl:variable>

        <fo:block xsl:use-attribute-sets="table">
            <xsl:call-template name="commonattributes"/>
            <xsl:if test="not(@id)">
              <xsl:attribute name="id">
                <xsl:call-template name="get-id"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:if test="not($scale = '')">
                <xsl:attribute name="font-size"><xsl:value-of select="concat($scale, '%')"/></xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]">
        <fo:block xsl:use-attribute-sets="table.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="'Table'"/>
                <xsl:with-param name="theParameters">
                    <number>
                        <xsl:number level="any" count="*[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]" from="/"/>
                    </number>
                    <title>
                        <xsl:apply-templates/>
                    </title>
                </xsl:with-param>
            </xsl:call-template>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tgroup ')]" name="tgroup">
        <xsl:if test="not(@cols)">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">006</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
          </xsl:call-template>
        </xsl:if>

        <xsl:variable name="colspecs">
            <xsl:call-template name="generate.colgroup">
                <xsl:with-param name="cols" select="@cols"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="scale">
            <xsl:call-template name="getTableScale"/>
        </xsl:variable>

        <xsl:variable name="table">
            <fo:table xsl:use-attribute-sets="table.tgroup">
                <xsl:call-template name="commonattributes"/>

                <xsl:call-template name="displayAtts">
                    <xsl:with-param name="element" select=".."/>
                </xsl:call-template>

                <xsl:if test="(parent::*/@pgwide) = '1'">
                    <xsl:attribute name="start-indent">0</xsl:attribute>
                    <xsl:attribute name="end-indent">0</xsl:attribute>
                    <xsl:attribute name="width">auto</xsl:attribute>
                </xsl:if>

                <xsl:copy-of select="$colspecs"/>

                <xsl:apply-templates select="*[contains(@class, ' topic/thead ')]"/>
                <xsl:apply-templates select="*[contains(@class, ' topic/tfoot ')]"/>
                <xsl:apply-templates select="*[contains(@class, ' topic/tbody ')]"/>
            </fo:table>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="not($scale = '')">
                <xsl:apply-templates select="exsl:node-set($table)" mode="setTableEntriesScale"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="$table"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*" mode="setTableEntriesScale">
        <xsl:choose>
            <xsl:when test="name() = 'font-size'">
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="node() | text()" mode="setTableEntriesScale">
        <xsl:copy>
            <xsl:apply-templates select="node() | @* | text()" mode="setTableEntriesScale"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/colspec ')]"/>

    <xsl:template match="*[contains(@class,' topic/spanspec ')]"/>

    <xsl:template match="*[contains(@class, ' topic/thead ')]">
        <fo:table-header xsl:use-attribute-sets="tgroup.thead">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="*[contains(@class, ' topic/row ')][1]">
                <xsl:with-param name="spans">
                    <xsl:call-template name="blank.spans">
                        <xsl:with-param name="cols" select="../@cols"/>
                    </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="parent" select="'thead'"/>
            </xsl:apply-templates>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tfoot ')]">
        <fo:table-footer xsl:use-attribute-sets="tgroup.tfoot">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="*[contains(@class, ' topic/row ')][1]">
                <xsl:with-param name="spans">
                    <xsl:call-template name="blank.spans">
                        <xsl:with-param name="cols" select="../@cols"/>
                    </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="parent" select="'tfoot'"/>
            </xsl:apply-templates>
        </fo:table-footer>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]">
        <fo:table-body xsl:use-attribute-sets="tgroup.tbody">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="*[contains(@class, ' topic/row ')][1]">
                <xsl:with-param name="spans">
                    <xsl:call-template name="blank.spans">
                        <xsl:with-param name="cols" select="../@cols"/>
                    </xsl:call-template>
                </xsl:with-param>
                <xsl:with-param name="parent" select="'tbody'"/>
            </xsl:apply-templates>
        </fo:table-body>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/row ')]">
        <xsl:param name="spans"/>
        <xsl:param name="parent"/>

        <fo:table-row>
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="concat($parent, '.row')"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>

            <xsl:apply-templates select="*[contains(@class, ' topic/entry ')][1]">
                <xsl:with-param name="spans" select="$spans"/>
                <xsl:with-param name="parent" select="$parent"/>
            </xsl:apply-templates>
        </fo:table-row>

        <xsl:if test="following-sibling::*[contains(@class, ' topic/row ')]">
            <xsl:variable name="nextspans">
                <xsl:apply-templates select="*[contains(@class, ' topic/entry ')][1]" mode="span">
                    <xsl:with-param name="spans" select="$spans"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:apply-templates>
            </xsl:variable>

            <xsl:apply-templates select="following-sibling::*[contains(@class, ' topic/row ')][1]">
                <xsl:with-param name="spans" select="$nextspans"/>
                <xsl:with-param name="parent" select="$parent"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/entry ')]" name="entry">
        <xsl:param name="col" select="1"/>
        <xsl:param name="spans"/>
        <xsl:param name="parent"/>

        <xsl:variable name="row" select="parent::*[contains(@class, ' topic/row ')]"/>
        <xsl:variable name="group" select="$row/parent::node()"/>

        <xsl:variable name="isEmptyCell" select="count(node()) = 0"/>

        <xsl:variable name="named.colnum">
            <xsl:call-template name="entry.colnum"/>
        </xsl:variable>

        <xsl:variable name="entry.colnum">
            <xsl:choose>
                <xsl:when test="$named.colnum &gt; 0">
                    <xsl:value-of select="$named.colnum"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$col"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="entry.colspan">
            <xsl:choose>
                <xsl:when test="@spanname or @namest">
                    <xsl:call-template name="calculate.colspan"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="following.spans">
            <xsl:call-template name="calculate.following.spans">
                <xsl:with-param name="colspan" select="$entry.colspan"/>
                <xsl:with-param name="spans" select="$spans"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="valign">
            <xsl:call-template name="getTableAttrubute">
                <xsl:with-param name="entry" select="."/>
                <xsl:with-param name="colnum" select="$entry.colnum"/>
                <xsl:with-param name="attribute" select="'valign'"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="align">
            <xsl:call-template name="getTableAttrubute">
                <xsl:with-param name="entry" select="."/>
                <xsl:with-param name="colnum" select="$entry.colnum"/>
                <xsl:with-param name="attribute" select="'align'"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$spans != '' and not(starts-with($spans,'0:'))">
                <xsl:call-template name="entry">
                    <xsl:with-param name="col" select="$col + 1"/>
                    <xsl:with-param name="spans" select="substring-after($spans,':')"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$entry.colnum &gt; $col">
                <xsl:call-template name="emptyTableCell">
                    <xsl:with-param name="colnum" select="$col"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
                <xsl:call-template name="entry">
                    <xsl:with-param name="col" select="$col + 1"/>
                    <xsl:with-param name="spans" select="substring-after($spans,':')"/>
                    <xsl:with-param name="parent" select="$parent"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:otherwise>
                <xsl:variable name="cell.content">
                    <fo:block>
                        <xsl:call-template name="processAttrSetReflection">
                            <xsl:with-param name="attrSet" select="concat($parent, '.row.entry__content')"/>
                            <xsl:with-param name="path" select="$tableAttrs"/>
                        </xsl:call-template>
                        <xsl:choose>
                            <xsl:when test="$isEmptyCell">
                                <xsl:text>&#160;</xsl:text>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>
                </xsl:variable>

                <fo:table-cell>
                    <xsl:call-template name="commonattributes"/>
                    <xsl:call-template name="processAttrSetReflection">
                        <xsl:with-param name="attrSet" select="concat($parent, '.row.entry')"/>
                        <xsl:with-param name="path" select="$tableAttrs"/>
                    </xsl:call-template>

                    <xsl:call-template name="generateTableEntryBorder"/>

                    <xsl:if test="@morerows">
                        <xsl:attribute name="number-rows-spanned">
                            <xsl:value-of select="@morerows + 1"/>
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="$entry.colspan &gt; 1">
                        <xsl:attribute name="number-columns-spanned">
                            <xsl:value-of select="$entry.colspan"/>
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="$valign != ''">
                        <xsl:attribute name="display-align">
                            <xsl:choose>
                                <xsl:when test="$valign='top'">before</xsl:when>
                                <xsl:when test="$valign='middle'">center</xsl:when>
                                <xsl:when test="$valign='bottom'">after</xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="display-align">
                                        <xsl:value-of select="$valign"/>
                                    </xsl:attribute>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="$align != ''">
                        <xsl:attribute name="text-align">
                            <xsl:value-of select="normalize-space($align)"/>
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:copy-of select="$cell.content"/>
                </fo:table-cell>

                <xsl:choose>
                    <xsl:when test="following-sibling::*[contains(@class, ' topic/entry ')]">
                        <xsl:apply-templates select="following-sibling::*[contains(@class, ' topic/entry ')][1]">
                            <xsl:with-param name="col" select="$col + $entry.colspan"/>
                            <xsl:with-param name="spans" select="$following.spans"/>
                            <xsl:with-param name="parent" select="$parent"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="finaltd">
                            <xsl:with-param name="spans" select="$following.spans"/>
                            <xsl:with-param name="col" select="$col+$entry.colspan"/>
                            <xsl:with-param name="parent" select="$parent"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/entry ')]" name="sentry" mode="span">
        <xsl:param name="col" select="1"/>
        <xsl:param name="spans"/>
        <xsl:param name="parent"/>

        <xsl:variable name="entry.colnum">
            <xsl:call-template name="entry.colnum"/>
        </xsl:variable>

        <xsl:variable name="entry.colspan">
            <xsl:choose>
                <xsl:when test="@spanname or @namest">
                    <xsl:call-template name="calculate.colspan"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="following.spans">
            <xsl:call-template name="calculate.following.spans">
                <xsl:with-param name="colspan" select="$entry.colspan"/>
                <xsl:with-param name="spans" select="$spans"/>
            </xsl:call-template>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="$spans != '' and not(starts-with($spans,'0:'))">
                <xsl:value-of select="substring-before($spans,':') - 1"/>
                <xsl:text>:</xsl:text>
                <xsl:call-template name="sentry">
                    <xsl:with-param name="col" select="$col + 1"/>
                    <xsl:with-param name="spans" select="substring-after($spans,':')"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:when test="$entry.colnum &gt; $col">
                <xsl:text>0:</xsl:text>
                <xsl:call-template name="sentry">
                    <xsl:with-param name="col" select="$col + $entry.colspan"/>
                    <xsl:with-param name="spans" select="$following.spans"/>
                </xsl:call-template>
            </xsl:when>

            <xsl:otherwise>
                <xsl:call-template name="copy-string">
                    <xsl:with-param name="count" select="$entry.colspan"/>
                    <xsl:with-param name="string">
                        <xsl:choose>
                            <xsl:when test="@morerows">
                                <xsl:value-of select="@morerows"/>
                            </xsl:when>
                            <xsl:otherwise>0</xsl:otherwise>
                        </xsl:choose>
                        <xsl:text>:</xsl:text>
                    </xsl:with-param>
                </xsl:call-template>

                <xsl:choose>
                    <xsl:when test="following-sibling::*[contains(@class, ' topic/entry ')]">
                        <xsl:apply-templates select="following-sibling::*[contains(@class, ' topic/entry ')][1]" mode="span">
                            <xsl:with-param name="col" select="$col + $entry.colspan"/>
                            <xsl:with-param name="spans" select="$following.spans"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="sfinaltd">
                            <xsl:with-param name="spans" select="$following.spans"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!--Definition list-->
    <xsl:template match="*[contains(@class, ' topic/dl ')]">
        <fo:table xsl:use-attribute-sets="dl">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="*[contains(@class, ' topic/dlhead ')]"/>
            <fo:table-body xsl:use-attribute-sets="dl__body">
                <xsl:choose>
                    <xsl:when test="contains(@otherprops,'sortable')">
                        <xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]">
                            <xsl:sort select="opentopic-func:getSortString(normalize-space( opentopic-func:fetchValueableText(*[contains(@class, ' topic/dt ')]) ))" lang="{$locale}"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]"/>
                    </xsl:otherwise>
                </xsl:choose>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dl ')]/*[contains(@class, ' topic/dlhead ')]">
        <fo:table-header xsl:use-attribute-sets="dl.dlhead">
            <xsl:call-template name="commonattributes"/>
            <fo:table-row xsl:use-attribute-sets="dl.dlhead__row">
                <xsl:apply-templates/>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dlhead ')]/*[contains(@class, ' topic/dthd ')]">
        <fo:table-cell xsl:use-attribute-sets="dlhead.dthd__cell">
            <xsl:call-template name="commonattributes"/>
            <fo:block xsl:use-attribute-sets="dlhead.dthd__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dlhead ')]/*[contains(@class, ' topic/ddhd ')]">
        <fo:table-cell xsl:use-attribute-sets="dlhead.ddhd__cell">
            <xsl:call-template name="commonattributes"/>
            <fo:block xsl:use-attribute-sets="dlhead.ddhd__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dlentry ')]">
        <fo:table-row xsl:use-attribute-sets="dlentry">
            <xsl:call-template name="commonattributes"/>
            <fo:table-cell xsl:use-attribute-sets="dlentry.dt">
                <xsl:apply-templates select="*[contains(@class, ' topic/dt ')]"/>
            </fo:table-cell>
            <fo:table-cell xsl:use-attribute-sets="dlentry.dd">
                <xsl:apply-templates select="*[contains(@class, ' topic/dd ')]"/>
            </fo:table-cell>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dt ')]">
        <fo:block xsl:use-attribute-sets="dlentry.dt__content">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/dd ')]">
        <fo:block xsl:use-attribute-sets="dlentry.dd__content">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <!--  Map processing  -->
    <xsl:template match="*[contains(@class,' map/map ')]/*[contains(@class,' map/reltable ')]">
        <fo:table-and-caption>
            <fo:table-caption>
                <fo:block xsl:use-attribute-sets="reltable__title">
                    <xsl:value-of select="@title"/>
                </fo:block>
            </fo:table-caption>
            <fo:table xsl:use-attribute-sets="reltable">
                <xsl:call-template name="topicrefAttsNoToc"/>
                <xsl:call-template name="selectAtts"/>
                <xsl:call-template name="globalAtts"/>

                <xsl:apply-templates select="relheader"/>

                <fo:table-body>
                    <xsl:apply-templates select="relrow"/>
                </fo:table-body>

            </fo:table>
        </fo:table-and-caption>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/relheader ')]">
        <fo:table-header xsl:use-attribute-sets="relheader">
            <xsl:call-template name="globalAtts"/>
            <xsl:apply-templates/>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/relcolspec ')]">
        <fo:table-cell xsl:use-attribute-sets="relcolspec">
            <xsl:apply-templates/>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/relrow ')]">
        <fo:table-row xsl:use-attribute-sets="relrow">
            <xsl:call-template name="globalAtts"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/relcell ')]">
        <fo:table-cell xsl:use-attribute-sets="relcell">
            <xsl:call-template name="globalAtts"/>
            <xsl:call-template name="topicrefAtts"/>

            <xsl:apply-templates/>

        </fo:table-cell>
    </xsl:template>

    <!-- SourceForge bug tracker item 2872988:
         Count the max number of cells in any row of a simpletable -->
    <xsl:template match="*" mode="count-max-simpletable-cells">
      <xsl:param name="maxcount">0</xsl:param>
      <xsl:variable name="newmaxcount">
        <xsl:choose>
          <xsl:when test="count(*)>$maxcount"><xsl:value-of select="count(*)"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$maxcount"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="not(following-sibling::*)">
          <xsl:value-of select="$newmaxcount"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="following-sibling::*[1]" mode="count-max-simpletable-cells">
            <xsl:with-param name="maxcount" select="$newmaxcount"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!-- SourceForge bug tracker item 2872988:
         Count the number of values in @relcolwidth (to add values if one is missing) -->
    <xsl:template match="*" mode="count-colwidths">
      <xsl:param name="relcolwidth" select="@relcolwidth"/>
      <xsl:param name="count" select="0"/>
      <xsl:choose>
        <xsl:when test="not(contains($relcolwidth,' '))">
          <xsl:value-of select="$count + 1"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="count-colwidths">
            <xsl:with-param name="relcolwidth" select="substring-after($relcolwidth,' ')"/>
            <xsl:with-param name="count" select="$count + 1"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!-- SourceForge bug tracker item 2872988:
         If there are more cells in any row than there are relcolwidth values, 
         add 1* for each missing cell, otherwise the FO processor may crash. -->
    <xsl:template match="*" mode="fix-relcolwidth">
      <xsl:param name="update-relcolwidth" select="@relcolwidth"/>
      <xsl:param name="number-cells">
        <xsl:apply-templates select="*[1]" mode="count-max-simpletable-cells"/>
      </xsl:param>
      <xsl:param name="number-relwidths">
        <xsl:apply-templates select="." mode="count-colwidths"/>
      </xsl:param>
      <xsl:choose>
        <xsl:when test="$number-relwidths &lt; $number-cells">
          <xsl:apply-templates select="." mode="fix-relcolwidth">
            <xsl:with-param name="update-relcolwidth" select="concat($update-relcolwidth,' 1*')"/>
            <xsl:with-param name="number-cells" select="$number-cells"/>
            <xsl:with-param name="number-relwidths" select="$number-relwidths+1"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$update-relcolwidth"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!--  Simpletable processing  -->
    <xsl:template match="*[contains(@class, ' topic/simpletable ')]">
        <xsl:variable name="number-cells">
            <!-- Contains the number of cells in the widest row -->
            <xsl:apply-templates select="*[1]" mode="count-max-simpletable-cells"/>
        </xsl:variable>
        <fo:table xsl:use-attribute-sets="simpletable">
            <xsl:call-template name="commonattributes"/>
            <!-- <xsl:call-template name="univAttrs"/> -->
            <xsl:call-template name="globalAtts"/>
            <xsl:call-template name="displayAtts">
                <xsl:with-param name="element" select="."/>
            </xsl:call-template>

            <xsl:if test="@relcolwidth">
                <xsl:variable name="fix-relcolwidth">
                    <xsl:apply-templates select="." mode="fix-relcolwidth">
                        <xsl:with-param name="number-cells" select="$number-cells"/>
                    </xsl:apply-templates>
                </xsl:variable>
                <xsl:call-template name="createSimpleTableColumns">
                    <xsl:with-param name="theColumnWidthes" select="$fix-relcolwidth"/>
                </xsl:call-template>
            </xsl:if>

            <!-- Toss processing to another template to process the simpletable
                 heading, and/or create a default table heading row. -->
            <xsl:apply-templates select="." mode="dita2xslfo:simpletable-heading">
                <xsl:with-param name="number-cells" select="$number-cells"/>
            </xsl:apply-templates>

            <fo:table-body xsl:use-attribute-sets="simpletable__body">
                <xsl:apply-templates select="*[contains(@class, ' topic/strow ')]">
                    <xsl:with-param name="number-cells" select="$number-cells"/>
                </xsl:apply-templates>
            </fo:table-body>

        </fo:table>
    </xsl:template>

    <xsl:template name="createSimpleTableColumns">
        <xsl:param name="theColumnWidthes" select="'1*'"/>

        <xsl:choose>
            <xsl:when test="contains($theColumnWidthes, ' ')">
                <fo:table-column>
                    <xsl:attribute name="column-width">
                        <xsl:call-template name="xcalcColumnWidth">
                            <xsl:with-param name="theColwidth" select="substring-before($theColumnWidthes, ' ')"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </fo:table-column>

                <xsl:call-template name="createSimpleTableColumns">
                    <xsl:with-param name="theColumnWidthes" select="substring-after($theColumnWidthes, ' ')"/>
                </xsl:call-template>

            </xsl:when>
            <xsl:otherwise>
                <fo:table-column>
                    <xsl:attribute name="column-width">
                        <xsl:call-template name="xcalcColumnWidth">
                            <xsl:with-param name="theColwidth" select="$theColumnWidthes"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </fo:table-column>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <!-- SourceForge RFE 2874200:
         Fill in empty cells when one is missing from strow or sthead.
         Context for this call is strow or sthead. -->
    <xsl:template match="*" mode="fillInMissingSimpletableCells">
      <xsl:param name="fill-in-count" select="0"/>
      <xsl:if test="$fill-in-count > 0">
        <fo:table-cell xsl:use-attribute-sets="strow.stentry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="frame" select="../@frame"/>
            <xsl:if test="following-sibling::*[contains(@class, ' topic/strow ')]">
                <xsl:call-template name="generateSimpleTableHorizontalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides') or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__left'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:if>
            <fo:block><fo:inline>&#160;</fo:inline></fo:block> <!-- Non-breaking space -->
        </fo:table-cell>
        <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
            <xsl:with-param name="fill-in-count" select="$fill-in-count - 1"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:template>

    <!-- Specialized simpletable elements may override this rule to add
         default headings for the table. By default, the existing sthead
         element is used when specified. -->
    <xsl:template match="*[contains(@class,' topic/simpletable ')]" mode="dita2xslfo:simpletable-heading">
        <xsl:param name="number-cells">
            <xsl:apply-templates select="*[1]" mode="count-max-simpletable-cells"/>
        </xsl:param>
        <xsl:apply-templates select="*[contains(@class, ' topic/sthead ')]">
            <xsl:with-param name="number-cells" select="$number-cells"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/sthead ')]">
        <xsl:param name="number-cells">
            <xsl:apply-templates select="../*[1]" mode="count-max-simpletable-cells"/>
        </xsl:param>
        <fo:table-header xsl:use-attribute-sets="sthead">
            <xsl:call-template name="commonattributes"/>
            <fo:table-row xsl:use-attribute-sets="sthead__row">
                <xsl:apply-templates/>
                <xsl:if test="count(*) &lt; $number-cells">
                  <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
                      <xsl:with-param name="fill-in-count" select="$number-cells - count(*)"/>
                  </xsl:apply-templates>
                </xsl:if>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/strow ')]">
        <xsl:param name="number-cells">
            <xsl:apply-templates select="../*[1]" mode="count-max-simpletable-cells"/>
        </xsl:param>
        <fo:table-row xsl:use-attribute-sets="strow">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
            <xsl:if test="count(*) &lt; $number-cells">
                <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
                    <xsl:with-param name="fill-in-count" select="$number-cells - count(*)"/>
                </xsl:apply-templates>
            </xsl:if>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/sthead ')]/*[contains(@class, ' topic/stentry ')]">
        <fo:table-cell xsl:use-attribute-sets="sthead.stentry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="entryCol" select="count(preceding-sibling::*[contains(@class, ' topic/stentry ')]) + 1"/>
            <xsl:variable name="frame" select="ancestor::*[contains(@class, ' topic/simpletable ')][1]/@frame"/>

            <xsl:call-template name="generateSimpleTableHorizontalBorders">
                <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
            <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="following-sibling::*[contains(@class, ' topic/stentry ')]">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="number(ancestor::*[contains(@class, ' topic/simpletable ')][1]/@keycol) = $entryCol">
                    <fo:block xsl:use-attribute-sets="sthead.stentry__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="sthead.stentry__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/strow ')]/*[contains(@class, ' topic/stentry ')]">
        <fo:table-cell xsl:use-attribute-sets="strow.stentry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="entryCol" select="count(preceding-sibling::*[contains(@class, ' topic/stentry ')]) + 1"/>
            <xsl:variable name="frame" select="ancestor::*[contains(@class, ' topic/simpletable ')][1]/@frame"/>

            <xsl:if test="../following-sibling::*[contains(@class, ' topic/strow ')]">
                <xsl:call-template name="generateSimpleTableHorizontalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="following-sibling::*[contains(@class, ' topic/stentry ')]">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="number(ancestor::*[contains(@class, ' topic/simpletable ')][1]/@keycol) = $entryCol">
                    <fo:block xsl:use-attribute-sets="strow.stentry__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="strow.stentry__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <!--  Properties processing  -->

    <xsl:template match="*[contains(@class, ' reference/properties ')]">
        <fo:table xsl:use-attribute-sets="properties">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="univAttrs"/>
            <xsl:call-template name="globalAtts"/>
            <xsl:call-template name="displayAtts">
                <xsl:with-param name="element" select="."/>
            </xsl:call-template>

            <xsl:if test="@relcolwidth">
                <xsl:variable name="fix-relcolwidth">
                    <xsl:apply-templates select="." mode="fix-relcolwidth"/>
                </xsl:variable>
                <xsl:call-template name="createSimpleTableColumns">
                    <xsl:with-param name="theColumnWidthes" select="$fix-relcolwidth"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:if test="*[contains(@class, ' reference/prophead ')]">
                <xsl:apply-templates select="*[contains(@class, ' reference/prophead ')]"/>
            </xsl:if>

            <fo:table-body xsl:use-attribute-sets="properties__body">
                <xsl:apply-templates select="*[contains(@class, ' reference/property ')]"/>
            </fo:table-body>
        </fo:table>
    </xsl:template>

    <!-- If there is no "type" column, value is in column 1 -->
    <xsl:template match="*" mode="get-propvalue-position">
      <xsl:choose>
        <xsl:when test="../*[contains(@class, ' reference/property ')]/*[contains(@class, ' reference/proptype ')] |
                        ../*[contains(@class, ' reference/prophead ')]/*[contains(@class, ' reference/propheadhd ')]">
          <xsl:text>2</xsl:text>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!-- If there is a type and value column, desc is 3;
         Otherwise, if there is type or value, desc is 2;
         Otherwise, desc is the only column. -->
    <xsl:template match="*" mode="get-propdesc-position">
      <xsl:choose>
        <xsl:when test="../*/*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')] and
                        ../*/*[contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')]">
          <xsl:text>3</xsl:text>
        </xsl:when>
        <xsl:when test="../*/*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')] |
                        ../*/*[contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')]">
          <xsl:text>2</xsl:text>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' reference/property ')]">
        <fo:table-row xsl:use-attribute-sets="property">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="valuePos">
              <xsl:apply-templates select="." mode="get-propvalue-position"/>
            </xsl:variable>
            <xsl:variable name="descPos">
              <xsl:apply-templates select="." mode="get-propdesc-position"/>
            </xsl:variable>
            <xsl:variable name="frame" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>
            <xsl:variable name="keyCol" select="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol)"/>
            <xsl:variable name="hasHorisontalBorder">
                <xsl:choose>
                    <xsl:when test="following-sibling::*[contains(@class, ' reference/property ')]">
                        <xsl:value-of select="'yes'"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'no'"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="*[contains(@class, ' reference/proptype ')]">
                    <xsl:apply-templates select="*[contains(@class, ' reference/proptype ')]">
                        <xsl:with-param name="entryCol" select="1"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="../*/*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')]">
                    <xsl:call-template name="createEmptyPropertyEntry">
                        <xsl:with-param name="entryCol" select="1"/>
                        <xsl:with-param name="keyCol" select="$keyCol"/>
                        <xsl:with-param name="hasHorisontalBorder" select="$hasHorisontalBorder"/>
                        <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
                        <xsl:with-param name="frame" select="$frame"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="*[contains(@class, ' reference/propvalue ')]">
                    <xsl:apply-templates select="*[contains(@class, ' reference/propvalue ')]">
                        <xsl:with-param name="entryCol" select="$valuePos"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="../*/*[contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')]">
                    <xsl:call-template name="createEmptyPropertyEntry">
                        <xsl:with-param name="entryCol" select="$valuePos"/>
                        <xsl:with-param name="keyCol" select="$keyCol"/>
                        <xsl:with-param name="hasHorisontalBorder" select="$hasHorisontalBorder"/>
                        <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
                        <xsl:with-param name="frame" select="$frame"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
            <xsl:choose>
                <xsl:when test="*[contains(@class, ' reference/propdesc ')]">
                    <xsl:apply-templates select="*[contains(@class, ' reference/propdesc ')]">
                        <xsl:with-param name="entryCol" select="$descPos"/>
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:when test="../*/*[contains(@class, ' reference/propdesc ') or contains(@class, ' reference/propdeschd ')]">
                    <xsl:call-template name="createEmptyPropertyEntry">
                        <xsl:with-param name="entryCol" select="$descPos"/>
                        <xsl:with-param name="keyCol" select="$keyCol"/>
                        <xsl:with-param name="hasHorisontalBorder" select="$hasHorisontalBorder"/>
                        <xsl:with-param name="hasVerticalBorder" select="'no'"/>
                        <xsl:with-param name="frame" select="$frame"/>
                    </xsl:call-template>
                </xsl:when>
            </xsl:choose>
        </fo:table-row>
    </xsl:template>

    <xsl:template name="createEmptyPropertyEntry">
        <xsl:param name="entryCol"/>
        <xsl:param name="keyCol"/>
        <xsl:param name="hasHorisontalBorder"/>
        <xsl:param name="hasVerticalBorder"/>
        <xsl:param name="frame"/>

        <fo:table-cell xsl:use-attribute-sets="property.entry">
            <xsl:if test="$hasHorisontalBorder = 'yes'">
                <xsl:call-template name="generateSimpleTableHorizontalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$hasVerticalBorder = 'yes'">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="$keyCol = $entryCol">
                    <fo:block xsl:use-attribute-sets="property.entry__keycol-content">
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="property.entry__content">
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propdesc ')]">
        <xsl:param name="entryCol"/>
        <fo:table-cell xsl:use-attribute-sets="property.entry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="frame" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>

            <xsl:if test="../following-sibling::*[contains(@class, ' reference/property ')]">
                <xsl:call-template name="generateSimpleTableHorizontalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="following-sibling::*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propdesc ')]">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol) = $entryCol">
                    <fo:block xsl:use-attribute-sets="property.entry__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="property.entry__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' reference/prophead ')]">
        <fo:table-header xsl:use-attribute-sets="prophead">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="frame" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>
            <xsl:variable name="keyCol" select="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol)"/>

            <fo:table-row xsl:use-attribute-sets="prophead__row">
                <xsl:choose>
                    <xsl:when test="*[contains(@class, ' reference/proptypehd ')]">
                        <xsl:apply-templates select="*[contains(@class, ' reference/proptypehd ')]">
                            <xsl:with-param name="entryCol" select="1"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="createEmptyPropertyHeadEntry">
                            <xsl:with-param name="entryCol" select="1"/>
                            <xsl:with-param name="keyCol" select="$keyCol"/>
                            <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
                            <xsl:with-param name="frame" select="$frame"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="*[contains(@class, ' reference/propvaluehd ')]">
                        <xsl:apply-templates select="*[contains(@class, ' reference/propvaluehd ')]">
                            <xsl:with-param name="entryCol" select="2"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="createEmptyPropertyHeadEntry">
                            <xsl:with-param name="entryCol" select="2"/>
                            <xsl:with-param name="keyCol" select="$keyCol"/>
                            <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
                            <xsl:with-param name="frame" select="$frame"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:choose>
                    <xsl:when test="*[contains(@class, ' reference/propdeschd ')]">
                        <xsl:apply-templates select="*[contains(@class, ' reference/propdeschd ')]">
                            <xsl:with-param name="entryCol" select="3"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="createEmptyPropertyHeadEntry">
                            <xsl:with-param name="entryCol" select="3"/>
                            <xsl:with-param name="keyCol" select="$keyCol"/>
                            <xsl:with-param name="hasVerticalBorder" select="'no'"/>
                            <xsl:with-param name="frame" select="$frame"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <xsl:template name="createEmptyPropertyHeadEntry">
        <xsl:param name="entryCol"/>
        <xsl:param name="keyCol"/>
        <xsl:param name="hasVerticalBorder"/>
        <xsl:param name="frame"/>

        <fo:table-cell xsl:use-attribute-sets="prophead.entry">
            <xsl:call-template name="generateSimpleTableHorizontalBorders">
                <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
            <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="$hasVerticalBorder = 'yes'">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="$keyCol = $entryCol">
                    <fo:block xsl:use-attribute-sets="prophead.entry__keycol-content">
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="prophead.entry__content">
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' reference/proptypehd ') or contains(@class, ' reference/propvaluehd ') or contains(@class, ' reference/propdeschd ')]">
        <xsl:param name="entryCol"/>
        <fo:table-cell xsl:use-attribute-sets="prophead.entry">
            <xsl:call-template name="commonattributes"/>
            <xsl:variable name="frame" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>

            <xsl:call-template name="generateSimpleTableHorizontalBorders">
                <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
            <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:if>
            <xsl:if test="following-sibling::*[contains(@class, ' reference/proptypehd ') or contains(@class, ' reference/propvaluehd ') or contains(@class, ' reference/propdeschd ')]">
                <xsl:call-template name="generateSimpleTableVerticalBorders">
                    <xsl:with-param name="frame" select="$frame"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol) = $entryCol">
                    <fo:block xsl:use-attribute-sets="prophead.entry__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="prophead.entry__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <!--  Choicetable processing  -->
    <xsl:template match="*[contains(@class, ' task/choicetable ')]">
        <fo:table xsl:use-attribute-sets="choicetable">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="univAttrs"/>
            <xsl:call-template name="globalAtts"/>

            <xsl:if test="@relcolwidth">
                <xsl:variable name="fix-relcolwidth">
                    <xsl:apply-templates select="." mode="fix-relcolwidth"/>
                </xsl:variable>
                <xsl:call-template name="createSimpleTableColumns">
                    <xsl:with-param name="theColumnWidthes" select="$fix-relcolwidth"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:choose>
                <xsl:when test="*[contains(@class, ' task/chhead ')]">
                    <xsl:apply-templates select="*[contains(@class, ' task/chhead ')]"/>
                </xsl:when>
                <xsl:otherwise>
                    <fo:table-header xsl:use-attribute-sets="chhead">
                        <fo:table-row xsl:use-attribute-sets="chhead__row">
                            <fo:table-cell xsl:use-attribute-sets="chhead.choptionhd">
                                <fo:block xsl:use-attribute-sets="chhead.choptionhd__content">
                                    <xsl:text>Options</xsl:text>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell xsl:use-attribute-sets="chhead.chdeschd">
                                <fo:block xsl:use-attribute-sets="chhead.chdeschd__content">
                                    <xsl:text>Description</xsl:text>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-header>
                </xsl:otherwise>
            </xsl:choose>

            <fo:table-body xsl:use-attribute-sets="choicetable__body">
                <xsl:apply-templates select="*[contains(@class, ' task/chrow ')]"/>
            </fo:table-body>

        </fo:table>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/chhead ')]">
        <fo:table-header xsl:use-attribute-sets="chhead">
            <xsl:call-template name="commonattributes"/>
            <fo:table-row xsl:use-attribute-sets="chhead__row">
                <xsl:apply-templates/>
            </fo:table-row>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/chrow ')]">
        <fo:table-row xsl:use-attribute-sets="chrow">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/chhead ')]/*[contains(@class, ' task/choptionhd ')]">
        <fo:table-cell xsl:use-attribute-sets="chhead.choptionhd">
            <xsl:call-template name="commonattributes"/>
            <fo:block xsl:use-attribute-sets="chhead.choptionhd__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/chhead ')]/*[contains(@class, ' task/chdeschd ')]">
        <fo:table-cell xsl:use-attribute-sets="chhead.chdeschd">
            <xsl:call-template name="commonattributes"/>
            <fo:block xsl:use-attribute-sets="chhead.chdeschd__content">
                <xsl:apply-templates/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/chrow ')]/*[contains(@class, ' task/choption ')]">
        <xsl:variable name="keyCol" select="ancestor::*[contains(@class, ' task/choicetable ')][1]/@keycol"/>
        <fo:table-cell xsl:use-attribute-sets="chrow.choption">
            <xsl:call-template name="commonattributes"/>
            <xsl:choose>
                <xsl:when test="$keyCol = 1">
                    <fo:block xsl:use-attribute-sets="chrow.choption__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="chrow.choption__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' task/chrow ')]/*[contains(@class, ' task/chdesc ')]">
        <xsl:variable name="keyCol" select="number(ancestor::*[contains(@class, ' task/choicetable ')][1]/@keycol)"/>
        <fo:table-cell xsl:use-attribute-sets="chrow.chdesc">
            <xsl:call-template name="commonattributes"/>
            <xsl:choose>
                <xsl:when test="$keyCol = 2">
                    <fo:block xsl:use-attribute-sets="chrow.chdesc__keycol-content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:when>
                <xsl:otherwise>
                    <fo:block xsl:use-attribute-sets="chrow.chdesc__content">
                        <xsl:apply-templates/>
                    </fo:block>
                </xsl:otherwise>
            </xsl:choose>
        </fo:table-cell>
    </xsl:template>

    <!--WARNING: Following templates are imported from default implementation-->
    <xsl:template name="xcalcColumnWidth">
        <!-- see original support comments in the XSL spec, source of this fragment -->
        <xsl:param name="theColwidth">1*</xsl:param>

        <!-- Ok, the theColwidth could have any one of the following forms: -->
        <!--        1*       = proportional width -->
        <!--     1unit       = 1.0 units wide -->
        <!--         1       = 1pt wide -->
        <!--  1*+1unit       = proportional width + some fixed width -->
        <!--      1*+1       = proportional width + some fixed width -->

        <!-- If it has a proportional width, translate it to XSL -->
        <xsl:if test="contains($theColwidth, '*')">
            <xsl:variable name="colfactor">
                <xsl:value-of select="substring-before($theColwidth, '*')"/>
            </xsl:variable>
            <xsl:text>proportional-column-width(</xsl:text>
            <xsl:choose>
                <xsl:when test="not($colfactor = '')">
                    <xsl:value-of select="$colfactor"/>
                </xsl:when>
                <xsl:otherwise>1</xsl:otherwise>
            </xsl:choose>
            <xsl:text>)</xsl:text>
        </xsl:if>

        <!-- Now get the non-proportional part of the specification -->
        <xsl:variable name="width-units">
            <xsl:choose>
                <xsl:when test="contains($theColwidth, '*')">
                    <xsl:value-of
                        select="normalize-space(substring-after($theColwidth, '*'))"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="normalize-space($theColwidth)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <!-- Now the width-units could have any one of the following forms: -->
        <!--                 = <empty string> -->
        <!--     1unit       = 1.0 units wide -->
        <!--         1       = 1pt wide -->
        <!-- with an optional leading sign -->

        <!-- Get the width part by blanking out the units part and discarding -->
        <!-- whitespace. -->
        <xsl:variable name="width"
            select="normalize-space(translate($width-units,
                                              '+-0123456789.abcdefghijklmnopqrstuvwxyz',
                                              '+-0123456789.'))"/>

        <!-- Get the units part by blanking out the width part and discarding -->
        <!-- whitespace. -->
        <xsl:variable name="units"
            select="normalize-space(translate($width-units,
                                              'abcdefghijklmnopqrstuvwxyz+-0123456789.',
                                              'abcdefghijklmnopqrstuvwxyz'))"/>

        <!-- Output the width -->
        <xsl:value-of select="$width"/>

        <!-- Output the units, translated appropriately -->
        <xsl:choose>
            <xsl:when test="$units = 'pi'">pc</xsl:when>
            <xsl:when test="$units = '' and $width != ''">pt</xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$units"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="attAlign">
        <xsl:if test="string(@align)">
            <xsl:attribute name="text-align">
                <xsl:value-of select="normalize-space(@align)"/>
            </xsl:attribute>
        </xsl:if>
    </xsl:template>

    <xsl:template name="univAttrs">
        <xsl:apply-templates select="@platform | @product | @audience | @otherprops | @importance | @rev | @status"/>
    </xsl:template>

    <xsl:template name="topicrefAttsNoToc">
        <!--TODO-->
    </xsl:template>

    <xsl:template name="topicrefAtts">
        <!--TODO-->
    </xsl:template>

    <xsl:template name="selectAtts">
        <!--TODO-->
    </xsl:template>

    <xsl:template name="globalAtts">
        <!--TODO-->
    </xsl:template>

    <xsl:template name="displayAtts">
        <xsl:param name="element"/>

        <xsl:choose>
            <xsl:when test="$element/@frame='all' or not($element/@frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__all'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$element/@frame='topbot'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__topbot'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$element/@frame='top'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__top'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$element/@frame='bottom'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__bottom'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="$element/@frame='sides'">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'table__tableframe__sides'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="generateSimpleTableHorizontalBorders">
        <xsl:param name="frame"/>
        <xsl:choose>
            <xsl:when test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides') or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__bottom'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="generateSimpleTableVerticalBorders">
        <xsl:param name="frame"/>
        <xsl:choose>
            <xsl:when test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides') or not($frame)">
                <xsl:call-template name="processAttrSetReflection">
                    <xsl:with-param name="attrSet" select="'__tableframe__right'"/>
                    <xsl:with-param name="path" select="$tableAttrs"/>
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <exslf:function name="opentopic-func:getSortString">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text, '[') and contains($text, ']')">
                <exslf:result select="substring-before(substring-after($text, '['),']')"/>
            </xsl:when>
            <xsl:otherwise>
                <exslf:result select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </exslf:function>
    
    <xsl:function version="2.0" name="opentopic-func:getSortString">
        <xsl:param name="text"/>
        <xsl:choose>
            <xsl:when test="contains($text, '[') and contains($text, ']')">
                <xsl:value-of select="substring-before(substring-after($text, '['),']')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <exslf:function name="opentopic-func:fetchValueableText">
        <xsl:param name="node"/>

        <xsl:variable name="res">
            <xsl:apply-templates select="$node" mode="insert-text"/>
        </xsl:variable>

        <exslf:result select="$res"/>

    </exslf:function>
    
    <xsl:function version="2.0" name="opentopic-func:fetchValueableText">
        <xsl:param name="node"/>

        <xsl:variable name="res">
            <xsl:apply-templates select="$node" mode="insert-text"/>
        </xsl:variable>

        <xsl:value-of select="$res"/>

    </xsl:function>

    <xsl:template match="*" mode="insert-text">
        <xsl:apply-templates mode="insert-text"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/indexterm ')]" mode="insert-text"/>

    <xsl:template match="text()[contains(., '[') and contains(., ']')][ancestor::*[contains(@class, ' topic/dl ')][contains(@otherprops,'sortable')]]" priority="10">
        <xsl:value-of select="substring-before(.,'[')"/>
    </xsl:template>

</xsl:stylesheet>
