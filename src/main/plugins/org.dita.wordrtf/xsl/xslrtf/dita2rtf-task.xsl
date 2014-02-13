<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- Get rid of whitespace only nodes -->
  <xsl:strip-space elements="*"/>

  <!-- Named templates -->
  <xsl:template name="ordered-steps">
    <xsl:for-each select="step">
      <xsl:text>{\pard </xsl:text>
      <xsl:number count="step" level="single" format="1" />
      <xsl:text>) </xsl:text>
      <xsl:apply-templates/>
      <xsl:text>\par}</xsl:text>
    </xsl:for-each>
  </xsl:template>

  <!-- Match templates -->
  <xsl:template match="*[contains(@class,' task/context ')]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*[contains(@class, 'task/steps ')]">
    <xsl:text>{\pard </xsl:text>
    <xsl:value-of select="stepsection" />
    <xsl:text>\par}</xsl:text>
    <xsl:call-template name="ordered-steps" />
  </xsl:template>

  <xsl:template match="*[contains(@class,' task/cmd ')]">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>