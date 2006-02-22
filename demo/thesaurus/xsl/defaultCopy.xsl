<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
	exclude-result-prefixes="ditaarch"
>

<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*">
  <xsl:copy>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>

<xsl:template match="@*">
  <xsl:if test="local-name()!='class' and
      local-name()!='ditaarch' and
      local-name()!='domains'">
    <xsl:copy/>
  </xsl:if>
</xsl:template>

<xsl:template match="text()">
  <xsl:copy/>
</xsl:template>

</xsl:stylesheet>
