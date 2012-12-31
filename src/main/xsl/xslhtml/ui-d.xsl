<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- Screen -->
<xsl:template match="*[contains(@class,' ui-d/screen ')]" name="topic.ui-d.screen">
     <xsl:apply-templates select="."  mode="screen-fmt" />
</xsl:template>
<xsl:template match="*[contains(@class,' ui-d/screen ')]" mode="screen-fmt">
  <!-- This template is deprecated in DITA-OT 1.7. Processing will moved into the main element rule. -->
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
  <xsl:call-template name="spec-title-nospace"/>
  <pre class="screen">
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setscale"/>
    <xsl:call-template name="setidaname"/>
    <xsl:apply-templates/>
  </pre>
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  <xsl:value-of select="$newline"/>
</xsl:template>

<!-- ui-domain.ent domain: uicontrol | wintitle | menucascade | shortcut -->

<xsl:template match="*[contains(@class,' ui-d/uicontrol ')]" name="topic.ui-d.uicontrol">
<!-- insert an arrow with leading/trailing spaces before all but the first uicontrol in a menucascade -->
<xsl:if test="ancestor::*[contains(@class,' ui-d/menucascade ')]">
 <xsl:variable name="uicontrolcount"><xsl:number count="*[contains(@class,' ui-d/uicontrol ')]"/></xsl:variable>
  <xsl:if test="$uicontrolcount&gt;'1'">
    <xsl:text> > </xsl:text>
  </xsl:if>
</xsl:if>
 <span class="uicontrol">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/wintitle ')]" name="topic.ui-d.wintitle">
 <span class="wintitle">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/menucascade ')]" name="topic.ui-d.menucascade">
 <span class="menucascade">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>
<!-- Ignore text inside menucascade -->
<xsl:template match="*[contains(@class,' ui-d/menucascade ')]/text()"/>

<xsl:template match="*[contains(@class,' ui-d/shortcut ')]" name="topic.ui-d.shortcut">
 <span class="shortcut">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

</xsl:stylesheet>
