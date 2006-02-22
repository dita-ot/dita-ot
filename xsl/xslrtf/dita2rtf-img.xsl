<?xml version="1.0" encoding="UTF-8"?>
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="*[contains(@class,' topic/fig ')]">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]">
<xsl:call-template name="gen-id"/>\pard \plain\s9 \qc\f4\fs24\b <xsl:text>Figure. </xsl:text><xsl:value-of select="."/>\par \plain\s0 \qj\f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/desc ')]">
<xsl:call-template name="gen-id"/>\pard \plain\s0 \f2\fs24 <xsl:apply-templates/>\par \plain\s0 \f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/image ')]">
<xsl:call-template name="gen-id"/><xsl:if test="@href and not(@href='')">{\field{\*\fldinst {\s8 \f2\fs24\ul\cf1 HYPERLINK "<xsl:value-of select="@href"/>"}}{\fldrslt {\s8 \f2\fs24\ul\cf1 <xsl:call-template name="gen-img-txt"/>\s8 \f2\fs24\ul\cf1}}}</xsl:if>
</xsl:template>

<xsl:template name="gen-img-txt">
<xsl:choose>
<xsl:when test="*[contains(@class,' topic/alt ')]">
<xsl:text>[PIC]</xsl:text><xsl:value-of select="*[contains(@class,' topic/alt ')]"/>
</xsl:when>
<xsl:when test="@alt and not(@alt='')"><xsl:text>[PIC]</xsl:text><xsl:value-of select="@alt"/></xsl:when>
<xsl:when test="text() or *"><xsl:text>[PIC]</xsl:text><xsl:apply-templates/></xsl:when>
<xsl:otherwise><xsl:text>[PIC]</xsl:text><xsl:value-of select="@href"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

</xsl:stylesheet>