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
                xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
                extension-element-prefixes="exsl"
                exclude-result-prefixes="opentopic-index opentopic"
                version="2.0">

    <xsl:variable name="map" select="//opentopic:map"/>

    <xsl:template name="createBookmarks">
        <xsl:variable name="bookmarks">
            <xsl:apply-templates select="/" mode="bookmark"/>
        </xsl:variable>

        <xsl:if test="count(exsl:node-set($bookmarks)/*) > 0">
            <fo:bookmark-tree>
                <fo:bookmark internal-destination="{$id.toc}">
                    <xsl:if test="$bookmarkStyle!='EXPANDED'">
                        <xsl:attribute name="starting-state">hide</xsl:attribute>
                    </xsl:if>
                    <fo:bookmark-title>
                        <xsl:call-template name="insertVariable">
                            <xsl:with-param name="theVariableID" select="'Table of Contents'"/>
                        </xsl:call-template>
                    </fo:bookmark-title>
                </fo:bookmark>
                <xsl:copy-of select="exsl:node-set($bookmarks)"/>
                <!-- CC #6163  -->
                <xsl:if test="(//opentopic-index:index.groups//opentopic-index:index.entry) and (count($index-entries//opentopic-index:index.entry) &gt; 0) ">
                    <fo:bookmark internal-destination="{$id.index}">
                        <xsl:if test="$bookmarkStyle!='EXPANDED'">
                            <xsl:attribute name="starting-state">hide</xsl:attribute>
                        </xsl:if>
                        <fo:bookmark-title>
                            <xsl:call-template name="insertVariable">
                                <xsl:with-param name="theVariableID" select="'Index'"/>
                            </xsl:call-template>
                        </fo:bookmark-title>
                    </fo:bookmark>
                </xsl:if>
            </fo:bookmark-tree>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo '))]" mode="bookmark">
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="gid" select="generate-id()"/>
        <xsl:variable name="topicNumber" select="count(exsl:node-set($topicNumbers)/topic[@id = $id][following-sibling::topic[@guid = $gid]]) + 1"/>
        <xsl:variable name="topicTitle">
            <xsl:call-template name="getNavTitle">
              <xsl:with-param name="topicNumber" select="$topicNumber"/>
            </xsl:call-template>
        </xsl:variable>
        <!-- normalize the title bug:3065853 -->
        <xsl:variable name="normalizedTitle" select="normalize-space($topicTitle)"/>
        <xsl:variable name="mapTopic">
            <xsl:copy-of select="$map//*[@id = $id]"/>
        </xsl:variable>
        
        <!-- added by William on 2009-05-11 for toc bug start -->
        <xsl:choose>
        	<xsl:when test="($mapTopic/*[position() = $topicNumber][@toc = 'yes' or not(@toc)]) or (not($mapTopic/*))">
        		<fo:bookmark>
          		  <xsl:attribute name="internal-destination">
          		      <xsl:call-template name="generate-toc-id"/>
          		  </xsl:attribute>
                    <xsl:if test="$bookmarkStyle!='EXPANDED'">
                        <xsl:attribute name="starting-state">hide</xsl:attribute>
                    </xsl:if>
            		<fo:bookmark-title>
                		<xsl:value-of select="$normalizedTitle"/>
            		</fo:bookmark-title>
            		<xsl:apply-templates mode="bookmark"/>
        		</fo:bookmark>
        	</xsl:when>
        	<xsl:otherwise>
        		<xsl:apply-templates mode="bookmark"/>
        	</xsl:otherwise>
        </xsl:choose>
        <!-- added by William on 2009-05-11 for toc bug end -->
        
    </xsl:template>

    <xsl:template match="*" mode="bookmark">
        <xsl:apply-templates mode="bookmark"/>
    </xsl:template>

    <xsl:template match="text()" mode="bookmark"/>

</xsl:stylesheet>
