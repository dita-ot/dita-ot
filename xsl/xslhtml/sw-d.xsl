<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- software-domain.ent domain: filepath | msgph | userinput | systemoutput | cmdname | msgnum | varname -->

<xsl:template match="*[contains(@class,' sw-d/filepath ')]" name="topic.sw-d.filepath">
 <span class="filepath">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/msgph ')]" name="topic.sw-d.msgph">
 <tt class="msgph">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></tt>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/userinput ')]" name="topic.sw-d.userinput">
 <kbd class="userinput">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></kbd>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/systemoutput ')]" name="topic.sw-d.systemoutput">
 <tt class="sysout">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></tt>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/cmdname ')]" name="topic.sw-d.cmdname">
 <span class="cmdname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/msgnum ')]" name="topic.sw-d.msgnum">
 <span class="msgnum">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/varname ')]" name="topic.sw-d.varname">
 <var class="varname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></var>
</xsl:template>

</xsl:stylesheet>
