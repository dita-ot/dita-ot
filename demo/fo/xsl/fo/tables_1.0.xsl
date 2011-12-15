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
    xmlns:exsl="http://exslt.org/common"
    extension-element-prefixes="exsl"
    version="2.0">

    <xsl:variable name="tableAttrs" select="'../../cfg/fo/attrs/tables-attr.xsl'"/>

    <xsl:param name="tableSpecNonProportional" select="'false'"/>

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

                <xsl:apply-templates/>
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

    <xsl:template match="*[contains(@class, ' topic/colspec ')]">
        <fo:table-column>
            <xsl:attribute name="column-number">
                <xsl:number count="colspec"/>
            </xsl:attribute>
			<xsl:if test="normalize-space(@colwidth) != ''"> 
				<xsl:attribute name="column-width">
					<xsl:choose>
						<xsl:when test="$tableSpecNonProportional = 'true'">
							<xsl:call-template name="calculateColumnWidth.nonProportional">
								<xsl:with-param name="colwidth" select="@colwidth"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="calculateColumnWidth.Proportional">
								<xsl:with-param name="colwidth" select="@colwidth"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:attribute>
			</xsl:if>

			<xsl:call-template name="applyAlignAttrs"/>
        </fo:table-column>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/thead ')]">
        <fo:table-header xsl:use-attribute-sets="tgroup.thead">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-header>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]">
        <fo:table-body xsl:use-attribute-sets="tgroup.tbody">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-body>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/thead ')]/*[contains(@class, ' topic/row ')]">
        <fo:table-row xsl:use-attribute-sets="thead.row">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]/*[contains(@class, ' topic/row ')]">
        <fo:table-row xsl:use-attribute-sets="tbody.row">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:table-row>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/thead ')]/*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')]">
        <fo:table-cell xsl:use-attribute-sets="thead.row.entry">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="applySpansAttrs"/>
            <xsl:call-template name="applyAlignAttrs"/>
            <xsl:call-template name="generateTableEntryBorder"/>
            <fo:block xsl:use-attribute-sets="thead.row.entry__content">
                <xsl:call-template name="processEntryContent"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/tbody ')]/*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')]">
        <fo:table-cell xsl:use-attribute-sets="tbody.row.entry">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="applySpansAttrs"/>
            <xsl:call-template name="applyAlignAttrs"/>
            <xsl:call-template name="generateTableEntryBorder"/>
            <fo:block xsl:use-attribute-sets="tbody.row.entry__content">
                <xsl:call-template name="processEntryContent"/>
            </fo:block>
        </fo:table-cell>
    </xsl:template>

    <xsl:template name="processEntryContent">
        <xsl:variable name="entryNumber">
            <xsl:call-template name="countEntryNumber"/>
        </xsl:variable>
        <xsl:variable name="char">
            <xsl:choose>
                <xsl:when test="@char">
                    <xsl:value-of select="@char"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][position() = number($entryNumber)]/@char">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][position() = $entryNumber]/@char"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="charoff">
            <xsl:choose>
                <xsl:when test="@charoff">
                    <xsl:value-of select="@charoff"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][position() = number($entryNumber)]/@charoff">
                    <xsl:value-of select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][position() = $entryNumber]/@charoff"/>
                </xsl:when>
                <xsl:otherwise>50</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <xsl:choose>
            <xsl:when test="not($char = '')">
                <xsl:call-template name="processCharAlignment">
                    <xsl:with-param name="char" select="$char"/>
                    <xsl:with-param name="charoff" select="$charoff"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="processCharAlignment">
        <xsl:param name="char"/>
        <xsl:param name="charoff"/>
        <xsl:choose>
            <xsl:when test="not(descendant::*)">
                <xsl:variable name="text-before" select="substring-before(text(),$char)"/>
                <xsl:variable name="text-after" select="substring-after(text(),$text-before)"/>
                <fo:list-block start-indent="0in"
                    provisional-label-separation="0pt"
                    provisional-distance-between-starts="{concat($charoff,'%')}">
                    <fo:list-item>
                        <fo:list-item-label end-indent="label-end()">
                            <fo:block text-align="right">
                                <xsl:copy-of select="$text-before"/>
                            </fo:block>
                        </fo:list-item-label>
                        <fo:list-item-body start-indent="body-start()">
                            <fo:block text-align="left">
                                <xsl:copy-of select="$text-after"/>
                            </fo:block>
                        </fo:list-item-body>
                    </fo:list-item>
                </fo:list-block>
<!--
                <fo:block text-align="right">
                    <xsl:copy-of select="text-before"/>
                    <fo:leader leader-pattern="use-content"
                        leader-length="{concat(string(100 - $charoff),'%')}"
                        leader-pattern-width="use-font-metrics">
                        <xsl:copy-of select="$text-after"/>
                    </fo:leader>
                </fo:block>
-->
<!--
                <fo:table>
                    <fo:table-column column-number="1" >
                        <xsl:attribute name="column-width">proportional-column-width(
                        <xsl:value-of select="$charoff"/>
                        )</xsl:attribute>
                    </fo:table-column>
                    <fo:table-column column-number="2" >
                    </fo:table-column>
                    <fo:table-column column-number="3" >
                        <xsl:attribute name="column-width">proportional-column-width(
                        <xsl:value-of select="100 - number($charoff)"/>
                        )</xsl:attribute>
                    </fo:table-column>
                    <fo:table-body>
                        <fo:table-row>
                            <fo:table-cell text-align="right">
                                <fo:block>
                                    <xsl:copy-of select="$text-before"/>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell text-align="center">
                                <fo:block>
                                    <xsl:choose>
                                        <xsl:when test="($text-before='') and ($text-after='')"/>
                                        <xsl:otherwise>
                                            <xsl:copy-of select="$char"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:block>
                            </fo:table-cell>
                            <fo:table-cell text-align="left">
                                <fo:block>
                                    <xsl:choose>
                                        <xsl:when test="($text-before='') and ($text-after='')">
                                            <xsl:copy-of select="text()"/>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:copy-of select="$text-after"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </fo:block>
                            </fo:table-cell>
                        </fo:table-row>
                    </fo:table-body>
                </fo:table>
-->
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="countEntryNumber">
        <xsl:choose>
            <xsl:when test="@colname">
                <xsl:variable name="colname" select="@colname"/>
                <xsl:if test="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][@colname = $colname]">
                  <xsl:number select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][@colname = $colname]"/>
                </xsl:if>
            </xsl:when>
            <xsl:when test="@colnum">
                <xsl:value-of select="@colnum"/>
            </xsl:when>
            <xsl:otherwise>
<!--  TODO Count of the entry Position              -->
<!--
                <xsl:variable name="cols" select="ancestor::*[contains(@class, ' topic/tgroup ')][1]/@cols"/>
                <xsl:variable name="colsInCurentRow" select="count(preceding-sibling::*[contains(@class,' topic/entry ')])+count(following-sibling::*[contains(@class,' topic/entry ')])+1"/>
                <xsl:variable name="precedingHorizontalSpan">
                    <xsl:value-of select="number(preceding-sibling::*[contains(@class,' topic/entry ')]/@nameend) - number(preceding-sibling::*[contains(@class,' topic/entry ')]/@namest)"/>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$colsInCurentRow = $cols">
                        <xsl:value-of select="count(preceding-sibling::*[contains(@class,' topic/entry ')])+1"/>
                    </xsl:when>
                    <xsl:when test=""/>
                </xsl:choose>
-->
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="calculateColumnWidth.Proportional">
        <xsl:param name="colwidth" >1*</xsl:param>

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
        <xsl:param name="colwidth" >1*</xsl:param>

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

    <xsl:template name="getEntryNumber">
        <xsl:param name="colname"/>
        <xsl:param name="optionalName" select="''"/>

        <xsl:choose>
            <xsl:when test="not(string(number($colname))='NaN')">
                <xsl:value-of select="$colname"/>
            </xsl:when>

            <xsl:when test="ancestor::*[contains(@class,' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][@colname = $colname]">
                <xsl:for-each select="ancestor::*[contains(@class,' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][@colname = $colname]">
                    <xsl:choose>
                        <xsl:when test="@colnum">
                            <xsl:value-of select="@colnum"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="count(preceding-sibling::*[contains(@class,' topic/colspec ')])+1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>

            <xsl:when test="not($optionalName = '') and ancestor::*[contains(@class,' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][@colname = $optionalName]">
                <xsl:for-each select="ancestor::*[contains(@class,' topic/tgroup ')][1]/*[contains(@class,' topic/colspec ')][@colname = $optionalName]">
                    <xsl:choose>
                        <xsl:when test="@colnum">
                            <xsl:value-of select="@colnum"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="count(preceding-sibling::*[contains(@class,' topic/colspec ')])+1"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:when>

            <xsl:when test="not(string(number(translate($colname,'+-0123456789.abcdefghijklmnopqrstuvwxyz','0123456789')))='NaN')">
                <xsl:value-of select="number(translate($colname,'0123456789.abcdefghijklmnopqrstuvwxyz','0123456789'))"/>
            </xsl:when>

            <xsl:otherwise>
                <xsl:value-of select="'-1'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="applySpansAttrs">
        <xsl:if test="(@morerows) and (number(@morerows) &gt; 0)">
            <xsl:attribute name="number-rows-spanned">
                <xsl:value-of select="number(@morerows)+1"/>
            </xsl:attribute>
        </xsl:if>

        <xsl:if test="(@nameend) and (@namest)">
            <xsl:variable name="startNum">
                <xsl:call-template name="getEntryNumber">
                    <xsl:with-param name="colname" select="@namest"/>
                    <xsl:with-param name="optionalName" select="@colname"/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="endNum">
                <xsl:call-template name="getEntryNumber">
                    <xsl:with-param name="colname" select="@nameend"/>
                </xsl:call-template>
            </xsl:variable>

            <xsl:if test="($startNum &gt; '-1') and ($endNum &gt; '-1') and ((number($endNum) - number($startNum)) &gt; 0)">
                <xsl:attribute name="number-columns-spanned">
                    <xsl:value-of select="(number($endNum) - number($startNum))+1"/>
                </xsl:attribute>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template name="applyAlignAttrs">
        <xsl:variable name="align">
            <xsl:choose>
                <xsl:when test="@align">
                    <xsl:value-of select="@align"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class,' topic/tbody ')][1][@align]">
                    <xsl:value-of select="ancestor::*[contains(@class,' topic/tbody ')][1]/@align"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class,' topic/thead ')][1][@align]">
                    <xsl:value-of select="ancestor::*[contains(@class,' topic/tbody ')][1]/@align"/>
                </xsl:when>
                <xsl:when test="ancestor::*[contains(@class,' topic/tgroup ')][1][@align]">
                    <xsl:value-of select="ancestor::*[contains(@class,' topic/tbody ')][1]/@align"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="valign">
            <xsl:choose>
                <xsl:when test="@valign">
                    <xsl:value-of select="@valign"/>
                </xsl:when>
                <xsl:when test="parent::*[contains(@class,' topic/row ')][@valign]">
                    <xsl:value-of select="parent::*[contains(@class,' topic/row ')]/@valign"/>
                </xsl:when>
            </xsl:choose>
        </xsl:variable>

        <xsl:choose>
            <xsl:when test="not($align = '')">
                <xsl:attribute name="text-align">
                    <xsl:value-of select="$align"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="($align='') and contains(@class, ' topic/colspec ')"/>
            <xsl:otherwise>
                <xsl:attribute name="text-align">from-table-column()</xsl:attribute>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test="$valign='top'">
                <xsl:attribute name="display-align">
                    <xsl:value-of select="'before'"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$valign='middle'">
                <xsl:attribute name="display-align">
                    <xsl:value-of select="'center'"/>
                </xsl:attribute>
            </xsl:when>
            <xsl:when test="$valign='bottom'">
                <xsl:attribute name="display-align">
                    <xsl:value-of select="'after'"/>
                </xsl:attribute>
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

    <xsl:template name="getTableColsep">
        <xsl:variable name="spanname" select="@spanname"/>
        <xsl:variable name="colname" select="@colname"/>
        <xsl:choose>
            <xsl:when test="@colsep">
                <xsl:value-of select="@colsep"/>
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


    <xsl:template name="getTableScale">
        <xsl:value-of select="ancestor-or-self::*[contains(@class, ' topic/table ')][1]/@scale"/>
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



</xsl:stylesheet>
