<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- hi-d.ent Phrase domain: b | i | u | tt | sup | sub -->

<xsl:template match="*[contains(@class,' hi-d/b ')]" name="topic.hi-d.b">
 <strong>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></strong>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/i ')]" name="topic.hi-d.i">
 <em>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></em>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/u ')]" name="topic.hi-d.u">
 <u>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></u>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/tt ')]" name="topic.hi-d.tt">
 <tt>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></tt>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sup ')]" name="topic.hi-d.sup">
 <sup>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></sup>
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sub ')]" name="topic.hi-d.sub">
 <sub>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></sub>
</xsl:template>

</xsl:stylesheet>
