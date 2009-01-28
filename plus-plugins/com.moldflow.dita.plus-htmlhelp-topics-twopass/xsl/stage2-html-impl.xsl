<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <!-- Remove XHTML namespace. -->
    <xsl:template match="*[namespace-uri() = 'http://www.w3.org/1999/xhtml']">
        <xsl:element name="{local-name()}">
            <xsl:apply-templates select="node()|@*"></xsl:apply-templates>
        </xsl:element>
    </xsl:template>

    <!-- Copy other nodes unchanged. -->
    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"></xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
