<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="text"/>
    
    <xsl:template match="/">
        <xsl:text>
            function treediagram_Constants() { }
            var treediagram_Dispatch = new Array;
        </xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="scalar">
        <xsl:text>treediagram_Constants.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>=</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
        <xsl:text>;&#x0a;</xsl:text>
    </xsl:template>

    <xsl:template match="string">
        <xsl:text>treediagram_Constants.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>="</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
        <xsl:text>";&#x0a;</xsl:text>
    </xsl:template>
    
    <xsl:template match="array">
        <xsl:text>treediagram_Constants.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>= new Array;</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="array/scalar">
        <xsl:text>treediagram_Constants.</xsl:text>
        <xsl:value-of select="../@name"/>
        <xsl:text>["</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>"] =</xsl:text>
        <xsl:apply-templates></xsl:apply-templates>
        <xsl:text>;&#x0a;</xsl:text>
    </xsl:template>
</xsl:stylesheet>
