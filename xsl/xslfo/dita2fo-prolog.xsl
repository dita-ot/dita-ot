<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>


<!-- =================== start of prolog and metadata ====================== -->

<!-- prolog data is placed all in brown background indicating content that
     will not show when output to print. -->

<xsl:template match="*[contains(@class,' topic/prolog ')]">
  <fo:block background-color="#f0f0d0">
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="border-width">thin</xsl:attribute>
    <xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/></xsl:attribute>
    <xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/></xsl:attribute>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!-- metadata in prolog is displayed using element names as field labels, and
     indented boxes for nested values.  Good for 2 deep, at least. -->

<xsl:template match="*[contains(@class,' topic/prolog ')]/*">
  <fo:block>
    <fo:inline font-weight="bold"><xsl:value-of select="name()"/>: </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]/*/*">
  <fo:block background-color="white"
           start-indent="{$basic-start-indent} + 1em">
    <xsl:attribute name="end-indent">6pt</xsl:attribute>
    <fo:inline font-weight="bold"><xsl:value-of select="name()"/>: </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]/*/*/*">
  <fo:block background-color="#fafafa"
           start-indent="{$basic-start-indent} + 2em">
    <xsl:attribute name="end-indent">6pt</xsl:attribute>
    <fo:inline font-weight="bold"><xsl:value-of select="name()"/>: </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]/*/*/*/*">
  <fo:block background-color="#f0f0f0"
           start-indent="{$basic-start-indent} + 3em">
    <xsl:attribute name="end-indent">6pt</xsl:attribute>
    <fo:inline font-weight="bold"><xsl:value-of select="name()"/>: </fo:inline>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<!-- not applicable yet within FO -->

<!-- =================== end of prolog and metadata ====================== -->



</xsl:stylesheet>
