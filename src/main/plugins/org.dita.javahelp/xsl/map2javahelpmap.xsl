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
    indent="yes"
/>

<xsl:template match="*[contains(@class,' map/map ')]" mode="toctop">
  <map version="1.0">
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
</xsl:template>

<!-- suppress default processing because title not used in JavaHelp map -->
<xsl:template match="*[contains(@class,' map/topicref ')]" mode="title">
  <xsl:param name="isFirst"/>
  <xsl:param name="infile"/>
  <xsl:param name="nodeID"/>
  <xsl:param name="outfile"/>
</xsl:template>

</xsl:stylesheet>
