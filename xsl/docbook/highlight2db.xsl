<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - FORMATTING PHRASES
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' hi-d/b ')]" name="bold">
  <xsl:param name="IDPrefix" select="'b'"/>
  <emphasis role="bold">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </emphasis>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/i ')]" name="italic">
  <xsl:param name="IDPrefix" select="'i'"/>
  <emphasis role="italic">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </emphasis>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/u ')]" name="underline">
  <xsl:param name="IDPrefix" select="'u'"/>
  <emphasis role="underline">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </emphasis>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/tt ')]" name="monospaced">
  <xsl:param name="IDPrefix" select="'tt'"/>
  <emphasis role="monospaced">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="$IDPrefix"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </emphasis>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sup ')]">
  <superscript>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'sup'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </superscript>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sub ')]">
  <subscript>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'sub'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </subscript>
</xsl:template>


</xsl:stylesheet>
