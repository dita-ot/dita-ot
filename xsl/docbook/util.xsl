<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">


<xsl:param name="deflatePrefix" select="'deflate'"/>
<xsl:param name="deflateShow"   select="false()"/>

<xsl:output
    method="xml"
    indent="yes"
    omit-xml-declaration="no"
    standalone="no"
    doctype-public="-//OASIS//DTD DocBook XML V4.1.2//EN"
    doctype-system="docbookx.dtd"/>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - TURN ELEMENTS, ATTRIBUTES, AND TEXT INTO PROCESSING INSTRUCTIONS
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="deflateNode">
  <xsl:apply-templates select="." mode="deflate">
    <xsl:with-param name="descendentsOkay" select="true()"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*" mode="deflate" name="deflate">
  <xsl:param name="parentID"        select="''"/>
  <xsl:param name="descendentsOkay" select="false()"/>
  <xsl:param name="textOkay"        select="false()"/>
  <xsl:variable name="id">
    <xsl:choose>
    <xsl:when test="$parentID=''">
      <xsl:value-of select="concat('elem', generate-id())"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="concat($parentID, '.', position())"/>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="element" select="local-name(.)"/>
  <xsl:if test="$deflateShow">
    <xsl:call-template name="deflateElementStart">
      <xsl:with-param name="id"       select="$id"/>
      <xsl:with-param name="element"  select="$element"/>
      <xsl:with-param name="parentID" select="$parentID"/>
    </xsl:call-template>
  </xsl:if>
  <xsl:choose>
  <xsl:when test="$descendentsOkay=true()">
    <xsl:apply-templates select="*|text()"/>
  </xsl:when>
  <xsl:when test="$textOkay=true()">
    <xsl:apply-templates select="*|text()" mode="deflatetext">
      <xsl:with-param name="parentID" select="$id"/>
    </xsl:apply-templates>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates select="*|text()" mode="deflate">
      <xsl:with-param name="parentID" select="$id"/>
    </xsl:apply-templates>
  </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="$deflateShow">
    <xsl:call-template name="deflateElementEnd">
      <xsl:with-param name="id"      select="$id"/>
      <xsl:with-param name="element" select="$element"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="text()" mode="deflatetext">
  <xsl:apply-templates select="."/>
</xsl:template>

<xsl:template match="*" mode="deflatetext">
  <xsl:param name="parentID" select="''"/>
  <xsl:call-template name="deflate">
    <xsl:with-param name="parentID" select="$parentID"/>
    <xsl:with-param name="textOkay" select="true()"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="deflateElementName">
  <xsl:param name="element" select="local-name(.)"/>
  <xsl:if test="$deflateShow">
    <xsl:processing-instruction name="{$deflatePrefix}-element-name">
      <xsl:text>name="</xsl:text>
      <xsl:value-of select="$element"/>
      <xsl:text>"</xsl:text>
    </xsl:processing-instruction>
  </xsl:if>
</xsl:template>

<xsl:template name="deflateElementStart">
  <xsl:param name="element"  select="local-name(.)"/>
  <xsl:param name="parentID" select="''"/>
  <xsl:param name="id"/>
  <xsl:if test="$deflateShow">
    <xsl:processing-instruction name="{$deflatePrefix}-element-start">
      <xsl:text>id="</xsl:text>
      <xsl:value-of select="$id"/>
      <xsl:text>" parentID="</xsl:text>
      <xsl:value-of select="$parentID"/>
      <xsl:text>" name="</xsl:text>
      <xsl:value-of select="$element"/>
      <xsl:text>"</xsl:text>
    </xsl:processing-instruction>
    <xsl:apply-templates select="@*" mode="deflate">
      <xsl:with-param name="parentID" select="$id"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<xsl:template name="deflateElementEnd">
  <xsl:param name="element" select="local-name(.)"/>
  <xsl:param name="id"/>
  <xsl:if test="$deflateShow">
    <!-- no parent ID attribute so ignored when reconstituting -->
    <xsl:processing-instruction name="{$deflatePrefix}-element-end">
      <xsl:text>id="</xsl:text>
      <xsl:value-of select="$id"/>
      <xsl:text>" name="</xsl:text>
      <xsl:value-of select="$element"/>
      <xsl:text>"</xsl:text>
    </xsl:processing-instruction>
  </xsl:if>
</xsl:template>

<!-- turn an attribute into a processing instruction -->
<xsl:template match="@*" mode="deflate">
  <xsl:param name="parentID" select="''"/>
  <xsl:if test="$deflateShow">
    <!-- Because the value attribute is last, the value can contain quotes. -->
    <xsl:processing-instruction name="{$deflatePrefix}-attribute">
      <xsl:text>parentID="</xsl:text>
      <xsl:value-of select="$parentID"/>
      <xsl:text>" name="</xsl:text>
      <xsl:value-of select="local-name(.)"/>
      <xsl:text>" value="</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>"</xsl:text>
    </xsl:processing-instruction>
  </xsl:if>
</xsl:template>

<!-- turn text into a processing instruction -->
<xsl:template match="text()" mode="deflate">
  <xsl:param name="parentID" select="''"/>
  <xsl:if test="$deflateShow">
    <!-- The final quote prevents errors if the text ends with a question mark.
         The text can contain quotes. -->
    <xsl:processing-instruction name="{$deflatePrefix}-text">
      <xsl:text>parentID="</xsl:text>
      <xsl:value-of select="$parentID"/>
      <xsl:text>" value="</xsl:text>
      <xsl:value-of select="."/>
      <xsl:text>"</xsl:text>
    </xsl:processing-instruction>
  </xsl:if>
</xsl:template>


</xsl:stylesheet>
