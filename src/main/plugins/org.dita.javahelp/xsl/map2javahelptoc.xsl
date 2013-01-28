<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- stylesheet imports -->
<xsl:import href="plugin:org.dita.xhtml:xsl/xslhtml/map2TOC.xsl"/>

<!-- default "output extension" processing parameter ('.html')-->
<xsl:param name="OUTEXT" select="'.html'"/><!-- "htm" and "html" are valid values -->

<xsl:output
    method="xml"
    omit-xml-declaration="no"
    encoding="UTF-8"
    doctype-public="-//Sun Microsystems Inc.//DTD JavaHelp TOC Version 1.0//EN"
    doctype-system="http://java.sun.com/products/javahelp/toc_1_0.dtd"
    indent="yes"
/>

<xsl:template match="*[contains(@class,' map/map ')]" mode="toctop">
  <toc version="1.0">
    <xsl:apply-templates select="*[contains(@class,' map/topicref ')]"/>
  </toc>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" mode="tocentry">
  <xsl:param name="infile"/>
  <xsl:param name="outroot"/>
  <xsl:param name="outfile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="isFirst"/>
  <xsl:param name="subtopicNodes"/>
  <xsl:param name="title"/>
  <xsl:choose>
  <xsl:when test="(@toc and translate(@toc, 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='no')
  					or (@processing-role='resource-only')">
    <!-- <xsl:variable name="results">  -->
    <!-- Process children nodes. -->
<!--     <xsl:apply-templates select="$subtopicNodes"/> -->
    <xsl:apply-templates select="./*[contains(@class, ' map/topicref ')]"/>
    <!-- </xsl:variable>  -->
    <!-- <xsl:text/>  -->
  </xsl:when>
  <xsl:when test="$title">
    <tocitem text="{$title}">
      <xsl:if test="$outroot and $outroot!=''">
        <xsl:attribute name="target">
          <xsl:value-of select="translate($outroot, '\/.', '___')"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="$subtopicNodes"/>
    </tocitem>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates select="$subtopicNodes"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- do nothing when meeting with reltable -->
<xsl:template match="*[contains(@class,' map/reltable ')]"/>

</xsl:stylesheet>
