<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- Screen -->
<xsl:template match="*[contains(@class,' ui-d/screen ')]" name="topic.ui-d.screen">
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
     <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
     <div class="{@rev}"><xsl:apply-templates select="."  mode="screen-fmt" /></div>
   </xsl:when>
   <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
     <xsl:apply-templates select="."  mode="screen-fmt" />
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>
<xsl:template match="*[contains(@class,' ui-d/screen ')]" mode="screen-fmt">
<xsl:call-template name="flagit"/>
<xsl:call-template name="start-revflag"/>
<xsl:call-template name="spec-title-nospace"/>
<pre class="screen">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setscale"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
</pre>
<xsl:call-template name="end-revflag"/>
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
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/wintitle ')]" name="topic.ui-d.wintitle">
 <span class="wintitle">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/menucascade ')]" name="topic.ui-d.menucascade">
 <span class="menucascade">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/shortcut ')]" name="topic.ui-d.shortcut">
 <span class="shortcut">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext"/></span>
</xsl:template>

<xsl:template match="*[contains(@class, ' ui-d/menucascade ')]" mode="text-only">
  <xsl:apply-templates select="*" mode="text-only"/>
</xsl:template>

<xsl:template match="*[contains(@class, ' ui-d/uicontrol ')]" mode="text-only">
  <xsl:if test="parent::*[contains(@class,' ui-d/menucascade ')] and preceding-sibling::*[contains(@class, ' ui-d/uicontrol ')]">
    <xsl:text> > </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="*|text()" mode="text-only"/>
</xsl:template>

</xsl:stylesheet>
