<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2025 Jason Coleman

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
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
    
    <xsl:template match="*[contains(@class, ' mathml-d/mathmlref ')]" mode="dita-ot:mathml" priority="10">
        <!--<xsl:variable name="mathml">
            <xsl:copy-of select="document(@href)/*"/>
        </xsl:variable>
        <xsl:call-template name="convert-mathml2svg-mathjax">
            <xsl:with-param name="mathml" select="document(@href)/*"/>
        </xsl:call-template>-->
    </xsl:template>
    
</xsl:stylesheet>