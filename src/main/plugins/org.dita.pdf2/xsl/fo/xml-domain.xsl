<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2014 Jarno Elovirta

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="3.0">
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlelement ')]">
    <fo:inline xsl:use-attribute-sets="xmlelement">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&lt;</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>&gt;</xsl:text>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlatt ')]">
    <fo:inline xsl:use-attribute-sets="xmlatt">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:text>@</xsl:text>
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/textentity ')]">
    <fo:inline xsl:use-attribute-sets="textentity">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&amp;</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>;</xsl:text>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/parameterentity ')]">
    <fo:inline xsl:use-attribute-sets="parameterentity">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:text>%</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>;</xsl:text>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/numcharref ')]">
    <fo:inline xsl:use-attribute-sets="numcharref">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&amp;#</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>;</xsl:text>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlnsname ')]">
    <fo:inline xsl:use-attribute-sets="xmlnsname">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlpi ')]">
    <fo:inline xsl:use-attribute-sets="xmlpi">
      <!-- TODO: Replace with mode="commonattributes" -->
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&lt;?</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>?&gt;</xsl:text>
    </fo:inline>
  </xsl:template>
  
</xsl:stylesheet>
