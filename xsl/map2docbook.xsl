<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="dita2docbook.xsl"/>

<xsl:output
    method="xml"
    indent="yes"
    omit-xml-declaration="no"
    standalone="no"
    doctype-public="-//OASIS//DTD DocBook XML V4.2//EN"
    doctype-system="http://www.oasis-open.org/docbook/xml/4.2/docbookx.dtd"/>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - MAP
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="/">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' map/map ')]">
  <article>
    <xsl:copy-of select="@id"/>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' topic/title ')]">
        <title>
          <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
        </title>
      </xsl:when>
      <xsl:when test="@title">
        <xsl:value-of select="@title"/>
      </xsl:when>
    </xsl:choose>
    <para/>
    <!-- doesn't handle reltables or topicgroups -->
    <xsl:apply-templates select="*[contains(@class,' map/topicref ')]"/>
  </article>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" name="topicref">
  <xsl:param name="element" select="'section'"/>
  <xsl:variable name="hrefValue">
    <xsl:choose>
      <xsl:when test="contains(@href, '#')">
        <xsl:value-of select="substring-before(@href, '#')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@href"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:choose>
  <xsl:when test="$hrefValue and not($hrefValue='')">
    <xsl:apply-templates select="document($hrefValue, /)/*">
      <xsl:with-param name="element" select="$element"/>
      <xsl:with-param name="childrefs"
          select="*[contains(@class,' map/topicref ')]"/>
    </xsl:apply-templates>
  </xsl:when>
  <xsl:when test="@navtitle">
    <xsl:element name="{$element}">
      <title>
        <xsl:value-of select="@navtitle"/>
      </title>
      <xsl:apply-templates select="*[contains(@class,' map/topicref ')]"/>
    </xsl:element>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates select="*[contains(@class,' map/topicref ')]"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
