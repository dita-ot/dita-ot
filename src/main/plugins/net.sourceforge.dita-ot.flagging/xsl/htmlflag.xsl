<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
 Sourceforge.net. See the accompanying license.txt file for 
 applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2012 -->
<!-- PURPOSE: 
     Match DITAVAL information added by preprocessing and output pre-calculated flags.
     If a start or end flag is present, it is known to be active and should be generated.

     If processing the flag directly will cause out-of-context XHTML, the processing
     must be explicitly requested by the HTML code by processing with the "process-exception" mode.
              -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0">

  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')]/@outputclass" mode="add-ditaval-style">
    <!-- Add the pre-calculated CSS style for this element -->
    <xsl:attribute name="style"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*" mode="processFlagsInline">yes</xsl:template>
  <xsl:template match="*[contains(@class,' topic/ol ') or
                         contains(@class,' topic/ul ')]" mode="processFlagsInline">no</xsl:template>

  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')]">
    <xsl:variable name="processnow">
      <xsl:apply-templates select="parent::*" mode="processFlagsInline"/>
    </xsl:variable>
    <xsl:if test="$processnow='yes'">
      <xsl:apply-templates select="prop/startflag" mode="ditaval-outputflag"/>
      <xsl:apply-templates select="revprop/startflag" mode="ditaval-outputflag"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="out-of-line">
    <xsl:apply-templates select="prop/startflag" mode="ditaval-outputflag"/>
    <xsl:apply-templates select="revprop/startflag" mode="ditaval-outputflag"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-endprop ')]">
    <xsl:variable name="processnow">
      <xsl:apply-templates select="parent::*" mode="processFlagsInline"/>
    </xsl:variable>
    <xsl:if test="$processnow='yes'">
      <xsl:apply-templates select="revprop/endflag" mode="ditaval-outputflag"/>
      <xsl:apply-templates select="prop/endflag" mode="ditaval-outputflag"/>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="out-of-line">
    <xsl:apply-templates select="prop/endflag" mode="ditaval-outputflag"/>
    <xsl:apply-templates select="revprop/endflag" mode="ditaval-outputflag"/>
  </xsl:template>

  <xsl:template match="startflag|endflag" mode="ditaval-outputflag">
    <img src="{@imageref}">
      <xsl:apply-templates select="alt-text" mode="ditaval-outputflag"/>
    </img>
  </xsl:template>
  <xsl:template match="alt-text" mode="ditaval-outputflag">
    <xsl:attribute name="alt">
      <xsl:value-of select="."/>
    </xsl:attribute>
  </xsl:template>

</xsl:stylesheet>