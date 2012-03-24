<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml" />

<xsl:template match="*" priority="10">
    <xsl:element name="{local-name()}">
        <xsl:for-each select="@*">
            <xsl:attribute name="{local-name()}">
                <xsl:value-of select="." />
            </xsl:attribute>
        </xsl:for-each>
        <xsl:apply-templates />
    </xsl:element>
</xsl:template>

<xsl:template match="comment()">
    <xsl:comment><xsl:value-of select="."/></xsl:comment>
</xsl:template>

<xsl:template match="*|processing-instruction()|text()">
    <xsl:copy>
        <xsl:apply-templates select="*|processing-instruction()|text()"/>
    </xsl:copy>
</xsl:template>

</xsl:stylesheet>
