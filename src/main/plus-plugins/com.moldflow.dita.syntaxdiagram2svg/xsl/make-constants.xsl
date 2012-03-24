<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="text"/>
    
    <xsl:template match="/">
        <xsl:text>
            function syntaxdiagram_Constants() { }
            var syntaxdiagram_Dispatch = new Array;
        </xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="scalar">
        <xsl:text>syntaxdiagram_Constants.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>=</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
        <xsl:text>;&#x0a;</xsl:text>
    </xsl:template>

    <xsl:template match="string">
        <xsl:text>syntaxdiagram_Constants.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>="</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
        <xsl:text>";&#x0a;</xsl:text>
    </xsl:template>
    
    <xsl:template match="array">
        <xsl:text>syntaxdiagram_Constants.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>= new Array;</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="array/scalar">
        <xsl:text>syntaxdiagram_Constants.</xsl:text>
        <xsl:value-of select="../@name"/>
        <xsl:text>["</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>"] =</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
        <xsl:text>;&#x0a;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
