<?xml version="1.0" encoding="UTF-8" ?>

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

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
See the accompanying license.txt file for applicable licenses.
-->

<!-- An adaptation of the Toolkit topicmerge.xsl for FO plugin use. -->

<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exsl="http://exslt.org/common"
 	            extension-element-prefixes="exsl">

    <xsl:output indent="yes"/>

	<xsl:key name="topic" match="dita-merge/*[contains(@class,' topic/topic ')]" use="concat('#',@id)"/>
	<xsl:key name="topic" match="dita-merge/dita" use="concat('#',@id)"/>
    <xsl:key name="topicref" match="//*[contains(@class,' map/topicref ')]" use="generate-id()"/>

<!--
	<xsl:template match="/">
		<xsl:copy-of select="."/>
	</xsl:template>
-->

	<xsl:template match="dita-merge">
        <xsl:element name="{name(*[contains(@class,' map/map ')])}">
            <xsl:copy-of select="*[contains(@class,' map/map ')]/@*"/>
            <xsl:apply-templates select="*[contains(@class,' map/map ')]" mode="build-tree"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="dita-merge/*[contains(@class,' map/map ')]" mode="build-tree">
        <opentopic:map xmlns:opentopic="http://www.idiominc.com/opentopic">
            <xsl:apply-templates/>
        </opentopic:map>
        <xsl:apply-templates mode="build-tree"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/topicref ')]" mode="build-tree">
		<xsl:choose>
			<xsl:when test="not(normalize-space(@href) = '')">
				<xsl:apply-templates select="key('topic',@href)">
					<xsl:with-param name="parentId" select="generate-id()"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:when test="(normalize-space(@href) = '') and not(normalize-space(@navtitle) = '')">
				<xsl:variable name="isNotTopicRef">
					<xsl:call-template name="isNotTopicRef">
						<xsl:with-param name="class" select="@class"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:if test="contains($isNotTopicRef,'false')">
					<topic id="{generate-id()}" class="- topic/topic ">
						<title class=" topic/title ">
							<xsl:value-of select="@navtitle"/>
						</title>
						<body class=" topic/body "/>
						<xsl:apply-templates mode="build-tree"/>
					</topic>
				</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates mode="build-tree"/>
			</xsl:otherwise>
		</xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/topic ')] | dita-merge/dita">
        <xsl:param name="parentId"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="key('topicref',$parentId)/*" mode="build-tree"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/topicref ')]/@id"/>

	<xsl:template match="*[contains(@class,' map/topicref ')]/@href">
        <xsl:copy-of select="."/>
        <xsl:attribute name="id">
            <xsl:value-of select="substring-after(.,'#')"/>
        </xsl:attribute>
    </xsl:template>


    <xsl:template match="*" mode="build-tree" priority="-1">
        <xsl:apply-templates mode="build-tree"/>
    </xsl:template>

    <xsl:template match="text()" mode="build-tree" priority="-1"/>

    <xsl:template match="*" priority="-1">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*" priority="-1">
        <xsl:copy-of select="."/>
    </xsl:template>

	<xsl:template name="isNotTopicRef">
		<xsl:param name="class"/>
		<xsl:choose>
			<xsl:when test="contains($class,' bookmap/abbrevlist ')"/>
			<xsl:when test="contains($class,' bookmap/amendments ')"/>
			<xsl:when test="contains($class,' bookmap/backmatter ')"/>
			<xsl:when test="contains($class,' bookmap/bookabstract ')"/>
			<xsl:when test="contains($class,' bookmap/booklist ')"/>
			<xsl:when test="contains($class,' bookmap/booklists ')"/>
			<xsl:when test="contains($class,' bookmap/colophon ')"/>
			<xsl:when test="contains($class,' bookmap/dedication ')"/>
			<xsl:when test="contains($class,' bookmap/figurelist ')"/>
			<xsl:when test="contains($class,' bookmap/frontmatter ')"/>
			<xsl:when test="contains($class,' bookmap/glossarylist ')"/>
			<xsl:when test="contains($class,' bookmap/indexlist ')"/>
			<xsl:when test="contains($class,' bookmap/tablelist ')"/>
			<xsl:when test="contains($class,' bookmap/toc ')"/>
			<xsl:when test="contains($class,' bookmap/trademarklist ')"/>
			<xsl:otherwise>
				<xsl:value-of select="'false'"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

    <xsl:template match="*[contains(@class, ' map/reltable ')]" mode="build-tree"/>

</xsl:stylesheet>
