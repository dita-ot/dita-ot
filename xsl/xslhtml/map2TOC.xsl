<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:html="http://www.w3.org/1999/xhtml"
				exclude-result-prefixes="html">

<!-- stylesheet imports -->
<xsl:import href="mapwalker.xsl"/>

<xsl:template match="*[contains(@class,' map/map ')]">
  <xsl:apply-templates select="." mode="toctop"/>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" mode="process">
  <xsl:param name="infile"/>
  <xsl:param name="outroot"/>
  <xsl:param name="outfile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="isFirst"/>
  <xsl:variable name="subtopicNodes"
      select="*[contains(@class,' map/topicref ')]"/>
  <xsl:variable name="title">
    <xsl:apply-templates select="." mode="title">
      <xsl:with-param name="isFirst" select="$isFirst"/>
      <xsl:with-param name="infile"  select="$infile"/>
      <xsl:with-param name="nodeID"  select="$nodeID"/>
      <xsl:with-param name="outfile" select="$outfile"/>
    </xsl:apply-templates>
  </xsl:variable>
  <xsl:apply-templates select="." mode="tocentry">
    <xsl:with-param name="infile"        select="$infile"/>
    <xsl:with-param name="outroot"       select="$outroot"/>
    <xsl:with-param name="outfile"       select="$outfile"/>
    <xsl:with-param name="nodeID"        select="$nodeID"/>
    <xsl:with-param name="isFirst"       select="$isFirst"/>
    <xsl:with-param name="subtopicNodes" select="$subtopicNodes"/>
    <xsl:with-param name="title"         select="$title"/>
  </xsl:apply-templates>
</xsl:template>

<!-- required overrides -->
<xsl:template match="*[contains(@class,' map/map ')]" mode="toctop">
  <xsl:message terminate="yes">
    <xsl:text>no toctop rule for map</xsl:text>
  </xsl:message>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" mode="tocentry">
  <xsl:param name="infile"/>
  <xsl:param name="outroot"/>
  <xsl:param name="outfile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="isFirst"/>
  <xsl:param name="subtopicNodes"/>
  <xsl:param name="title"/>
  <xsl:message terminate="yes">
    <xsl:text>no tocentry rule for topicref</xsl:text>
  </xsl:message>
</xsl:template>

<!-- topic title -->
<xsl:template match="*[contains(@class,' map/topicref ')]" mode="title">
  <xsl:param name="isFirst"/>
  <xsl:param name="infile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="outfile"/>
  <xsl:choose>
  <xsl:when test="@navtitle">
    <xsl:choose>
    <xsl:when test="$isFirst and $infile and $infile!=''">
      <xsl:apply-templates select="document($infile, /)" mode="topic">
        <xsl:with-param name="outfile" select="$outfile"/>
        <xsl:with-param name="isFirst" select="$isFirst"/>
        <xsl:with-param name="title"   select="@navtitle"/>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="@navtitle"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="$isFirst and $infile and $infile!=''">
    <xsl:apply-templates select="document($infile, /)" mode="topic">
      <xsl:with-param name="outfile" select="$outfile"/>
      <xsl:with-param name="isFirst" select="$isFirst"/>
    </xsl:apply-templates>
  </xsl:when>
  <xsl:otherwise>
    <xsl:message>
      <xsl:text>neither title nor href</xsl:text>
    </xsl:message>
    <xsl:text></xsl:text>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- topic output -->
<xsl:template match="/" mode="topic">
  <xsl:param name="isFirst" select="false()"/>
  <xsl:param name="outfile"/>
  <xsl:param name="title">
    <xsl:value-of select="*[contains(@class,' topic/topic ')][1]/
        *[contains(@class,' topic/title ')]"/>
  </xsl:param>
  <!-- output only the first reference to a file -->
  <xsl:if test="$isFirst">
    <xsl:apply-templates select="." mode="write-topic">
      <xsl:with-param name="outfile" select="$outfile"/>
    </xsl:apply-templates>
  </xsl:if>
  <xsl:value-of select="$title"/>
</xsl:template>

<xsl:template match="/" mode="write-topic"/>

</xsl:stylesheet>
