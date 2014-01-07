<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template name="generateCharset">
    <meta charset="UTF-8"/>
  </xsl:template>  
  
  <xsl:template match="*[contains(@class,' topic/copyright ')]" mode="gen-metadata">
    <meta name="rights">
      <xsl:attribute name="content">
        <xsl:text>&#xA9; </xsl:text>
        <xsl:apply-templates select="*[contains(@class,' topic/copyryear ')][1]" mode="gen-metadata"/>
        <xsl:text> </xsl:text>
        <xsl:if test="*[contains(@class,' topic/copyrholder ')]">
          <xsl:value-of select="*[contains(@class,' topic/copyrholder ')]"/>
        </xsl:if>                
      </xsl:attribute>
    </meta>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/copyryear ')]" mode="gen-metadata">
    <xsl:param name="previous" select="/.."/>
    <xsl:param name="open-sequence" select="false()"/>
    <xsl:variable name="next" select="following-sibling::*[contains(@class,' topic/copyryear ')][1]"/>
    <xsl:variable name="begin-sequence" select="@year + 1 = number($next/@year)"/>
    <xsl:choose>
      <xsl:when test="$begin-sequence">
        <xsl:if test="not($open-sequence)">
          <xsl:value-of select="@year"/>
          <xsl:text>&#x2013;</xsl:text>
        </xsl:if>
      </xsl:when>
      <xsl:when test="$next">
        <xsl:value-of select="@year"/>
        <xsl:text>, </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@year"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="$next" mode="gen-metadata">
      <xsl:with-param name="previous" select="."/>
      <xsl:with-param name="open-sequence" select="$begin-sequence"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/title ')]" mode="gen-metadata"/>
  <xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="gen-metadata">
    <xsl:variable name="shortmeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="description">
      <xsl:attribute name="content">
        <xsl:value-of select="normalize-space($shortmeta)"/>
      </xsl:attribute>
    </meta>
  </xsl:template>

</xsl:stylesheet>