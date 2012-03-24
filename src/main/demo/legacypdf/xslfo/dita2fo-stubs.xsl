<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>

<!-- Define the error message prefix identifier -->
<xsl:variable name="msgprefix">DOTX</xsl:variable>

<!-- =================== start of override stubs ====================== -->

<!--  STUBS FOR USER PROVIDED OVERRIDE EXTENSIONS  -->

<xsl:template name="gen-user-header">
  <!-- to customize: copy this to your override transform, add whatever content you want! -->
  <!-- for example, to display logos, search/nav widgets, etc. -->
</xsl:template>

<xsl:template name="gen-user-footer">
  <!-- to customize: copy this to your override transform, add whatever content you want! -->
  <!-- for example, to display compliances for: validation, accessibility, content ratings, etc. -->
</xsl:template>

<xsl:template name="gen-user-sidetoc">
  <!-- to customize: copy this to your override transform, add whatever content you want! -->
  <!-- Common implementations use a table with align=right to place generated content
       adjacent to the start of the body content -->
</xsl:template>

<xsl:template name="gen-user-scripts">
  <!-- to customize: copy this to your override transform, add whatever content you want! -->
  <!-- see (or enable) the named template "script-sample" for an example -->
  <!--xsl:call-template name="script-sample"/-->
</xsl:template>

<xsl:template name="gen-user-styles">
  <!-- to customize: copy this to your override transform, add whatever content you want! -->
</xsl:template>


<xsl:template name="gen-user-panel-title-pfx">
  <!-- to customize: copy this to your override transform, add whatever content you want! -->
  <!-- Generate content based on run-time parameter value, with local logic here -->
  <!-- This is overrideable -->
</xsl:template>

<xsl:template name="gen-main-panel-title">
  <!-- use the searchtitle unless there's no value - else use title -->
  <xsl:variable name="schtitle"><xsl:value-of select="/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')]"/></xsl:variable>
  <xsl:variable name="ditaschtitle"><xsl:value-of select="/dita/*[contains(@class,' topic/topic ')][1]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')]"/></xsl:variable>
  <xsl:variable name="maintitle"><xsl:value-of select="/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]"/></xsl:variable>
  <xsl:variable name="ditamaintitle"><xsl:value-of select="/dita/*[contains(@class,' topic/topic ')][1]/*[contains(@class,' topic/title ')]"/></xsl:variable>
  <xsl:choose>
    <xsl:when test="string-length($schtitle) > 0"><xsl:value-of select="$schtitle"/></xsl:when>
    <xsl:when test="string-length($ditaschtitle) > 0"><xsl:value-of select="$ditaschtitle"/></xsl:when>
    <xsl:when test="string-length($maintitle) > 0"><xsl:value-of select="$maintitle"/></xsl:when>
    <xsl:when test="string-length($ditamaintitle) > 0"><xsl:value-of select="$ditamaintitle"/></xsl:when>
    <xsl:otherwise>
      <xsl:text>***</xsl:text>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msgnum">037</xsl:with-param>
        <xsl:with-param name="msgsev">W</xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="gen-user-metadata"/>
<!-- not applicable yet within FO -->

<!-- =================== end of override stubs ====================== -->



</xsl:stylesheet>
