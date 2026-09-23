<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2025 Jason Coleman

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
    xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    exclude-result-prefixes="xs ditaarch opentopic"
    version="3.0">
    
    <xsl:param name="EQN-NUM-BYCHAPTER" select="'yes'"/>
    
    <!-- Key based on chapter -->
    <xsl:key name="equations-by-chapter"
        match="*[contains(@class, ' equation-d/equation-number ')][not(ancestor::draft-comment) and not(child::* or child::text())]"
        use="generate-id(
        key('map-id', ancestor::*[contains(@class, ' topic/topic ')][1]/@id)
        /ancestor-or-self::*[contains(@class, ' map/topicref ')]
        [not(contains(@class, ' bookmap/part ')) and
        not(contains(@class, ' bookmap/appendices ')) and
        not(contains(@class, ' bookmap/backmatter '))]
        [parent::opentopic:map or
        parent::*[contains(@class, ' bookmap/part ')] or
        parent::*[contains(@class, ' bookmap/appendices ')]
        ][1]
        )"/>
    <xsl:key name="equations-by-document" 
        match="*[contains(@class, ' equation-d/equation-number ')][not(ancestor::draft-comment) and not(child::* or child::text())]"
        use="'include'"/>
    
    <xsl:variable name="math-indent">10mm</xsl:variable>
    
    <xsl:template match="*[contains(@class,' equation-d/equation-block ')]" name="topic.equation-d.equation-block">
        <fo:block xsl:use-attribute-sets="eqn-block">
            <xsl:if test="not(ancestor::*[contains(@class,' topic/dd ')] or ancestor::*[contains(@class,' topic/entry ')])">
                <xsl:attribute name="margin-left" select="$math-indent"/>
            </xsl:if>
            <!-- the following use the leading spaces to adjust the equation number to the right edge -->
            <xsl:if test="child::*[contains(@class, ' equation-d/equation-number ')]">
                <xsl:attribute name="text-align-last">justify</xsl:attribute>
            </xsl:if>
            <xsl:call-template name="commonattributes"/>
            <fo:inline text-align-last="start">
                <xsl:apply-templates select="*[not(contains(@class, ' equation-d/equation-number '))] | text()"/>
            </fo:inline>
            <fo:inline/>
            <xsl:apply-templates select="*[contains(@class, ' equation-d/equation-number ')]"/>
        </fo:block>
    </xsl:template>
    
    <xsl:template match="*[contains(@class,' equation-d/equation-figure ')]" name="topic.equation-d.equation-figure">
        <fo:block xsl:use-attribute-sets="eqn-fig">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>
    
    <xsl:template match="*[contains(@class,' equation-d/equation-inline ')]" name="topic.equation-d.equation-inline">
        <fo:inline xsl:use-attribute-sets="font.math">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>
    
    <xsl:template match="*[contains(@class,' equation-d/equation-number ')]" name="topic.equation-d.equation-number">
        <fo:leader leader-pattern="space" />
        <fo:inline xsl:use-attribute-sets="eqn-num">
            <xsl:text>(</xsl:text>
            <xsl:apply-templates select="." mode="eqn.title-number"/>
            <xsl:text>)</xsl:text>
        </fo:inline>
    </xsl:template>
    
    <!-- Numbering equation by chapter with reset in each chaper  [chapterprefix-Eq#] -->
    <xsl:template match="*[contains(@class,' equation-d/equation-number ')]" mode="eqn.title-number">
        <xsl:variable name="chapter-prefix">
            <!--<xsl:call-template name="getChapterPrefix"/>-->
        </xsl:variable>
        <xsl:variable name="chapter-topicref" as="element()?"
            select="key('map-id', ancestor::*[contains(@class, ' topic/topic ')][1]/@id)
            /ancestor-or-self::*[contains(@class, ' map/topicref ')]
            [not(contains(@class, ' bookmap/part ')) and
            not(contains(@class, ' bookmap/appendices ')) and
            not(contains(@class, ' bookmap/backmatter '))]
            [parent::opentopic:map or
            parent::*[contains(@class, ' bookmap/part ')] or
            parent::*[contains(@class, ' bookmap/appendices ')]
            ][1]"/>
        <xsl:variable name="chapter-id" select="generate-id($chapter-topicref)"/>
        <xsl:variable name="eqn-count-actual" as="xs:integer">
            <xsl:choose>
                <xsl:when test="$EQN-NUM-BYCHAPTER = 'yes'">
                    <xsl:value-of select="count(key('equations-by-chapter', $chapter-id)[. &lt;&lt; current()]) + 1"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="count(key('equations-by-document', 'include')[. &lt;&lt; current()]) + 1"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:choose>
            <xsl:when test="child::* or child::text()">
                <xsl:apply-templates/>
            </xsl:when>
            <xsl:when test="$EQN-NUM-BYCHAPTER = 'yes'">
                <xsl:value-of select="$chapter-prefix"/><xsl:value-of select="$eqn-count-actual"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$eqn-count-actual"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <!--<xsl:template match="*[contains(@class, ' equation-d/equation-figure ')][descendant::*[contains(@class, ' equation-d/equation-number ')]] | 
                         *[contains(@class, ' equation-d/equation-block ')][child::*[contains(@class, ' equation-d/equation-number ')]]" 
                  mode="retrieveReferenceTitle">
        <xsl:value-of select="$equationlink.lead"/>
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="current()//*[contains(@class, ' equation-d/equation-number ')][1]" mode="eqn.title-number"/>
    </xsl:template>-->
    
    <!-- attr sets -> Move to attr file -->
    
    <xsl:attribute-set name="eqn-fig">
    </xsl:attribute-set>
    
    <xsl:attribute-set name="eqn-block" use-attribute-sets="font.math">
    </xsl:attribute-set>
    
    <xsl:attribute-set name="eqn-num">
        <xsl:attribute name="font-style">normal</xsl:attribute>
        <xsl:attribute name="font-weight">normal</xsl:attribute>
    </xsl:attribute-set>
    
    <xsl:attribute-set name="font.math">
        <xsl:attribute name="font-family">Georgia</xsl:attribute>
        <xsl:attribute name="font-weight">400</xsl:attribute>
        <xsl:attribute name="font-style">italic</xsl:attribute>
    </xsl:attribute-set>
    
</xsl:stylesheet>