<?xml version="1.0" encoding="UTF-8" ?>
<!--
 (C) Copyright Nokia Corporation and/or its subsidiary(-ies) 2009  - 2010. All rights reserved.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">  

  <xsl:template name="strip-url-fragment">
    <xsl:param name="url"/>
    <xsl:choose>
      <xsl:when test="contains($url, '#')">
        <xsl:value-of select="substring-before($url, '#')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$url"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
