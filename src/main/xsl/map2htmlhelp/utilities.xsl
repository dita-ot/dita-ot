<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <!-- Convert URIs to system paths -->
  <xsl:template name="uri2file">
    <xsl:param name="uri"/>
    <xsl:message>uri2file: <xsl:value-of select="$uri"/></xsl:message>
    <xsl:choose>
      <xsl:when test="contains($uri, '%')">
        <xsl:value-of select="substring-before($uri, '%')"/>
        <xsl:variable name="rest" select="substring-after($uri, '%')"/>
        <xsl:variable name="esc" select="substring($rest, 1, 2)"/>
        <xsl:choose>
          <xsl:when test="$esc = '20'"><xsl:text> </xsl:text></xsl:when>
        </xsl:choose>
        <xsl:call-template name="uri2file">
          <xsl:with-param name="uri" select="substring($rest, 3)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$uri"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
