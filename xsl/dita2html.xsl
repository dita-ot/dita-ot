<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- ereview.xsl
 | DITA topic to HTML for ereview & webreview

-->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                xmlns:xt="http://www.jclark.com/xt"
                extension-element-prefixes="saxon xt">

<!-- stylesheet imports -->
<!-- the main dita to xhtml converter -->
<xsl:import href="dita2xhtml.xsl"/>

<xsl:output method="html"
            encoding="iso-8859-1"
            doctype-public="-//IETF//DTD HTML 4.0//EN"
/>

<!-- DITAEXT file extension name of dita topic file -->
<xsl:param name="DITAEXT" select="'.xml'"/>

<!-- [41] ========== DEFAULT PAGE LAYOUT ========== -->

<xsl:template name="chapter-setup">
<html>
<head>

  <!-- Title processing - special handling for short descriptions -->
    <title>
      <xsl:call-template name="gen-user-panel-title-pfx"/> <!-- hook for a user-XSL title prefix -->
      <!-- use the searchtitle unless there's no value - else use title -->
      <xsl:variable name="schtitle"><xsl:value-of select="/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')]"/></xsl:variable>
      <xsl:variable name="ditaschtitle"><xsl:value-of select="/dita/*[contains(@class,' topic/topic ')][1]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')]"/></xsl:variable>
      <xsl:variable name="maintitle"><xsl:apply-templates select="/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]" mode="text-only"/></xsl:variable>
      <xsl:variable name="ditamaintitle"><xsl:apply-templates select="/dita/*[contains(@class,' topic/topic ')][1]/*[contains(@class,' topic/title ')]" mode="text-only"/></xsl:variable>
      <xsl:choose>
       <xsl:when test="string-length($schtitle)>'0'"><xsl:value-of select="$schtitle"/></xsl:when>
       <xsl:when test="string-length($ditaschtitle)>'0'"><xsl:value-of select="$ditaschtitle"/></xsl:when>
       <xsl:when test="string-length($maintitle)>'0'"><xsl:value-of select="$maintitle"/></xsl:when>
       <xsl:when test="string-length($ditamaintitle)>'0'"><xsl:value-of select="$ditamaintitle"/></xsl:when>
       <xsl:otherwise><xsl:text>***</xsl:text>
       <xsl:call-template name="output-message">
         <xsl:with-param name="msg">Topic contains no title; using "***".</xsl:with-param>
         <xsl:with-param name="msgnum">009</xsl:with-param>
         <xsl:with-param name="msgsev">W</xsl:with-param>
       </xsl:call-template>
       </xsl:otherwise>
      </xsl:choose>
    </title><xsl:value-of select="$newline"/>

  <!-- Add user's head XHTML code snippet if requested to -->
  <xsl:if test="string-length($HDF)>0">
   <xsl:copy-of select="document($HDF,/)"/>
  </xsl:if>
  </head><xsl:value-of select="$newline"/>

  <body>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:value-of select="$newline"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="start-revflag"/>

<!-- Insert previous/next/ancestor breadcrumbs links at the top of the xhtml. -->
  <xsl:apply-templates select="*[contains(@class,' topic/related-links ')]" mode="breadcrumb"/>

  <!-- include user's XSL running header here -->
  <xsl:call-template name="gen-user-header"/>
  <!-- Add user's running heading XHTML code snippet if requested to -->
  <xsl:if test="string-length($HDR)>0">
   <xsl:copy-of select="document($HDR,/)"/>
  </xsl:if>
  <xsl:value-of select="$newline"/>
  <!-- Include a user's XSL call here to generate a toc based on what's a child of topic -->
  <xsl:call-template name="gen-user-sidetoc"/>

    <xsl:apply-templates/> <!-- this will include all things within topic; therefore, -->
    <!-- title content will appear here by fall-through -->
    <!-- followed by prolog (but no fall-through is permitted for it) -->
    <!-- followed by body content, again by fall-through in document order -->
    <!-- followed by related links -->
    <!-- followed by child topics by fall-through -->

  <!-- include footnote-endnotes -->
  <xsl:call-template name="gen-endnotes"/>

  <!-- include user's XSL running footer here -->
  <xsl:call-template name="gen-user-footer"/>
  <!-- Add user's running footing XHTML code snippet if requested to -->
  <xsl:if test="string-length($FTR)>0">
   <xsl:copy-of select="document($FTR,/)"/>
  </xsl:if>
  <xsl:value-of select="$newline"/>
 <xsl:call-template name="end-revflag"/>

  </body>
</html>
</xsl:template>

<!-- Set typestyle overrides -->
<xsl:template match="*[contains(@class,' ui-d/uicontrol ')]" priority="2">
<!-- insert an arrow with leading/trailing spaces before all but the first uicontrol in a menucascade -->
<xsl:if test="ancestor::*[contains(@class,' ui-d/menucascade ')]">
 <xsl:variable name="uicontrolcount"><xsl:number count="*[contains(@class,' ui-d/uicontrol ')]"/></xsl:variable>
  <xsl:if test="$uicontrolcount&gt;'1'">
    <xsl:text> > </xsl:text>
  </xsl:if>
</xsl:if>
 <b>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="revtext"/></b>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/shortcut ')]" priority="2">
 <u>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="revtext"/></u>
</xsl:template>


<xsl:template match="*[contains(@class,' pr-d/kwd ')]" priority="2">
 <b>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:choose>
   <xsl:when test="(@importance='default')">
    <u>
     <xsl:call-template name="revtext"/>
    </u>
   </xsl:when>
   <xsl:otherwise>
     <xsl:call-template name="revtext"/>
   </xsl:otherwise>
  </xsl:choose>
</b>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/var ')]" priority="2">
 <i>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="revtext"/></i>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/parmname ')]" priority="2">
 <b>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagit"/>
  <xsl:call-template name="revtext"/></b>
</xsl:template>

</xsl:stylesheet>
