<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- hi-d.ent Phrase domain: b | i | u | tt | sup | sub -->

<xsl:template match="*[contains(@class,' hi-d/b ')]" name="topic.hi-d.b">
  <xsl:variable name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:variable>
 <strong>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </strong>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/i ')]" name="topic.hi-d.i">
  <xsl:variable name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:variable>
 <em>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </em>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/u ')]" name="topic.hi-d.u">
  <xsl:variable name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:variable>
 <u>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </u>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/tt ')]" name="topic.hi-d.tt">
  <xsl:variable name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:variable>
 <tt>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </tt>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sup ')]" name="topic.hi-d.sup">
  <xsl:variable name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:variable>
 <sup>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </sup>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sub ')]" name="topic.hi-d.sub">
  <xsl:variable name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:variable>
 <sub>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </sub>
</xsl:template>

</xsl:stylesheet>
