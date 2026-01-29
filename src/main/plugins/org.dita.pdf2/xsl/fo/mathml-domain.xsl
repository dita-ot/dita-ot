<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2026 Jason Coleman

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    xmlns:m="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="#all"
    version="3.0">
    
    <xsl:mode on-no-match="shallow-copy"/>
    
    <xsl:template match="*[contains(@class, ' mathml-d/mathml ')]">
        <fo:inline>
            <xsl:call-template name="apply-mathml"/>
        </fo:inline>
    </xsl:template>
    
    <xsl:template name="apply-mathml">
        <fo:instream-foreign-object alignment-baseline="mathematical">
            <xsl:if test="ancestor::equation-figure/@scale">
                <xsl:attribute name="content-width" select="ancestor::equation-figure/@scale || '%'"/>
            </xsl:if>
            <xsl:apply-templates mode="dita-ot:mathml"/>
        </fo:instream-foreign-object>
    </xsl:template>
    
    <xsl:template match="m:*" mode="dita-ot:mathml" priority="10">
        <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
            <xsl:apply-templates select="* | @* | text()" mode="#current"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="m:*/@*" mode="dita-ot:mathml" priority="10">
        <xsl:copy-of select="."/>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' mathml-d/mathmlref ')]" mode="dita-ot:mathml">
        <xsl:variable name="mml-href">
            <xsl:choose>
        <xsl:when test="contains(@href,'#')"><xsl:value-of select="substring-before(@href,'#')"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
            </xsl:choose>
    
</xsl:variable>
        <xsl:variable name="mml-id">
            <xsl:if test="contains(@href,'#')"><xsl:value-of select="substring-after(@href,'#')"/></xsl:if>
        </xsl:variable>
        <xsl:variable name="mml-href2">
            <xsl:choose>
                <xsl:when test="@scope = 'external' or opentopic-func:isAbsolute(@href)">
                    <xsl:value-of select="@href"/>
                </xsl:when>
                <xsl:when test="exists(key('jobFile', $mml-href, $job))">
                    <xsl:value-of select="key('jobFile', $mml-href, $job)/@src"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat($input.dir.url, $mml-href)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        
        <xsl:choose>
            <xsl:when test="not($mml-id ='')">
                <xsl:copy-of select="document($mml-href2)//*[@id=$mml-id]"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="document($mml-href2)/*"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>