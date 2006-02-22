<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet version="1.0" 
                xmlns="http://www.w3.org/1999/xhtml"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output
    method="xml"
    encoding="UTF-8"
    indent="no"
/>

<xsl:template match="/">
  <xsl:apply-templates select="*"/>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ') and @format='ditamap']">
  <xsl:apply-templates select="document(@href,/)/*[contains(@class,' map/map ')]">
    <xsl:with-param name="startingFile" select="@href"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*">
  <xsl:param name="startingFile" select="''"/>
  <xsl:copy>
    <xsl:if test="$startingFile != '' and not(@xtrf)">
      <xsl:attribute name="xtrf"><xsl:value-of select="$startingFile"/></xsl:attribute>
    </xsl:if>
    <xsl:apply-templates select="@*|*|text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="@*|text()">
  <xsl:copy/>
</xsl:template>

</xsl:stylesheet>
