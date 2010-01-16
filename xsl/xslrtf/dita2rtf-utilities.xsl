<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" 
    xmlns:stringUtils="org.dita.dost.util.StringUtils" exclude-result-prefixes="stringUtils">
    <xsl:template name="get-ascii">
      <xsl:param name="txt"></xsl:param>
      <xsl:variable name="ancestorlang">
        <xsl:call-template name="getLowerCaseLang"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'zh-cn')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'zh')) )">
          <xsl:text>\f13 </xsl:text><xsl:value-of select="stringUtils:getAscii(string($txt))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$txt"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
  <xsl:template name="getStringRTF">
    <xsl:param name="stringName"></xsl:param>
    <xsl:variable name="translatedStr">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="$stringName"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="get-ascii">
      <xsl:with-param name="txt" select="$translatedStr"/>
    </xsl:call-template>
  </xsl:template>
</xsl:stylesheet>