<?xml version="1.0" encoding="UTF-8" ?>

<!-- An adaptation of the Toolkit topicmerge.xsl for FO plugin use. -->

<xsl:stylesheet version="1.1"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exsl="http://exslt.org/common"
 	            extension-element-prefixes="exsl">

    <xsl:output indent="yes"/>

    <xsl:key name="topic" match="dita-merge/*[contains(@class,' topic/topic ')]" use="concat('#',@id)"/>
    <xsl:key name="topicref" match="//*[contains(@class,' map/topicref ')]" use="generate-id()"/>

    <xsl:template match="dita-merge">
        <bookmap>
            <xsl:copy-of select="*[contains(@class,' map/map ')]/@*"/>
            <xsl:apply-templates select="*[contains(@class,' map/map ')]" mode="build-tree"/>
        </bookmap>
    </xsl:template>

    <xsl:template match="dita-merge/*[contains(@class,' map/map ')]" mode="build-tree">
        <opentopic:map xmlns:opentopic="http://www.idiominc.com/opentopic">
            <xsl:apply-templates/>
        </opentopic:map>
        <xsl:apply-templates mode="build-tree"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' map/topicref ')][not(normalize-space(@href) = '')]" mode="build-tree">
        <xsl:apply-templates select="key('topic',@href)">
            <xsl:with-param name="parentId" select="generate-id()"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/topic ')]">
        <xsl:param name="parentId"/>
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates/>
            <xsl:apply-templates select="key('topicref',$parentId)/*" mode="build-tree"/>
        </xsl:copy>
    </xsl:template>

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
    
</xsl:stylesheet>
