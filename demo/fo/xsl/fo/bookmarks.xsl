<?xml version='1.0'?>

<!-- 
Copyright © 2004-2005 by Idiom Technologies, Inc. All rights reserved. 
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
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:rx="http://www.renderx.com/XSL/Extensions"
                xmlns:exsl="http://exslt.org/common"
                xmlns:opentopic="http://www.idiominc.com/opentopic"
                xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
                extension-element-prefixes="exsl"
                exclude-result-prefixes="opentopic-index opentopic"
                version='1.1'>

    <xsl:variable name="map" select="//opentopic:map"/>

    <xsl:template name="createBookmarks">
        <xsl:variable name="bookmarks">
            <xsl:apply-templates select="/" mode="bookmark"/>
        </xsl:variable>

        <xsl:if test="count(exsl:node-set($bookmarks)/*) > 0">
            <rx:outline>
                <rx:bookmark internal-destination="ID_TOC_00-0F-EA-40-0D-4D">
                    <rx:bookmark-label>
                        <xsl:call-template name="insertVariable">
                            <xsl:with-param name="theVariableID" select="'Table of Contents'"/>
                        </xsl:call-template>
                    </rx:bookmark-label>
                </rx:bookmark>
                <xsl:copy-of select="exsl:node-set($bookmarks)"/>
                <!-- CC #6163  -->
                <xsl:if test="//opentopic-index:index.groups//opentopic-index:index.entry">
                    <rx:bookmark internal-destination="ID_INDEX_00-0F-EA-40-0D-4D">
                        <rx:bookmark-label>
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Index'"/>
                            </xsl:call-template>
                        </rx:bookmark-label>
                    </rx:bookmark>
                </xsl:if>
            </rx:outline>
        </xsl:if>
    </xsl:template>

    <xsl:template match="/" mode="bookmark">
        <xsl:apply-templates mode="bookmark">
            <xsl:with-param name="include" select="'true'"/>  
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo '))]" mode="bookmark">
        <xsl:param name="include"/>
        <xsl:variable name="topicTitle">
            <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                <xsl:call-template name="getTitle"/>
            </xsl:for-each>
        </xsl:variable>
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="gid" select="generate-id()"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="mapTopic">
            <xsl:copy-of select="$map//*[@id = $id]"/>
        </xsl:variable>

        <xsl:if test="($mapTopic/*[position() = $topicNumber][@toc = 'yes' or not(@toc)]) or (not($mapTopic/*) and $include = 'true')">
        <rx:bookmark internal-destination="{concat('_OPENTOPIC_TOC_PROCESSING_', generate-id())}">
            <rx:bookmark-label>
                <xsl:value-of select="$topicTitle"/>
            </rx:bookmark-label>
            <xsl:apply-templates mode="bookmark">
                <xsl:with-param name="include" select="'true'"/>
            </xsl:apply-templates>
        </rx:bookmark>
        </xsl:if>
    </xsl:template>

    <xsl:template match="node()" mode="bookmark">
        <xsl:param name="include"/>
        <xsl:apply-templates mode="bookmark">
            <xsl:with-param name="include" select="$include"/>
        </xsl:apply-templates>
    </xsl:template>

</xsl:stylesheet>
