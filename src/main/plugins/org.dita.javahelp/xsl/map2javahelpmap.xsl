<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2004, 2005 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->

<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- stylesheet imports -->
<xsl:import href="plugin:org.dita.xhtml:xsl/xslhtml/map2TOC.xsl"/>
<xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
<xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>

<!-- default "output extension" processing parameter ('.html')-->
<xsl:param name="OUTEXT" select="'.html'"/><!-- "htm" and "html" are valid values -->
<!-- Deprecated since 2.3 -->
<xsl:variable name="msgprefix">DOTX</xsl:variable>

<xsl:output
    method="xml"
    omit-xml-declaration="no"
    encoding="UTF-8"
    indent="yes"
/>

<xsl:template match="*[contains(@class,' map/map ')]" mode="toctop">
  <map version="2.0">
    <xsl:apply-templates select="*[contains(@class,' map/topicref ')]"/>
  </map>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" mode="tocentry">
  <xsl:param name="infile"/>
  <xsl:param name="outroot"/>
  <xsl:param name="outfile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="isFirst"/>
  <xsl:param name="subtopicNodes"/>
  <xsl:param name="title"/>
  <xsl:if test="$isFirst">
    <xsl:if test="$outfile and $outfile!=''">
      <xsl:variable name="targetID" select="translate($outroot, '\/.', '___')"/>
      <mapID target="{$targetID}" url="{$outfile}"/>
    </xsl:if>
    <xsl:apply-templates select="$subtopicNodes"/>
  </xsl:if>
  
 <xsl:if test="$title">
    <xsl:if test="@copy-to">
      <xsl:variable name="copyToWithoutExtension">
        <xsl:call-template name="replace-extension">
          <xsl:with-param name="filename" select="@copy-to"/>
          <xsl:with-param name="extension" select="''"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="targetID" select="translate($copyToWithoutExtension, '\/.', '___')"/>   
      <xsl:variable name="copyToWithHTMLExtension">
        <xsl:call-template name="replace-extension">
          <xsl:with-param name="filename" select="@copy-to"/>
          <xsl:with-param name="extension" select="$OUTEXT"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="urlID" select="$copyToWithHTMLExtension"/>      
      <mapID target="{$targetID}" url="{$urlID}"/>
    </xsl:if>
  </xsl:if>
</xsl:template>

<!-- suppress default processing because title not used in JavaHelp map -->
<xsl:template match="*[contains(@class,' map/topicref ')]" mode="title">
  <xsl:param name="isFirst"/>
  <xsl:param name="infile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="outfile"/>
</xsl:template>

</xsl:stylesheet>
