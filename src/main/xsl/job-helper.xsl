<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">
  
  <xsl:output method="text"/>
  
  <xsl:param name="property"/>
  
  <xsl:template match="/">
    <xsl:apply-templates select="job/property[@name = $property]/*"/>
  </xsl:template>
  
  <xsl:template match="set">
    <xsl:for-each select="string">
      <xsl:if test="not(position() = 1)"><xsl:text>&#xA;</xsl:text></xsl:if>
      <xsl:apply-templates select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="string">
    <xsl:value-of select="."/>
  </xsl:template>
  
</xsl:stylesheet>