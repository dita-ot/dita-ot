<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
  <!--
    | (C) Copyright IBM Corporation 2006. All Rights Reserved.
    *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="TRANSTYPE"/>

  <xsl:template match="*|@*|text()|comment()|processing-instruction()" mode="createPlugin">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|text()|comment()|processing-instruction()" mode="createPlugin"/>
    </xsl:copy>
  </xsl:template>

  <!-- Should inherit these from map, if they are not specified locally. If local, the local one will 
       copy over these values. -->
  <xsl:template name="inheritMapAttributesForEclipse">
    <xsl:copy-of select="../@format|../@linking|../@audience|../@platform|../@product|
                         ../@otherprops|../@importance|../@scope|../@toc|../@print|../@type"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' map/topicref ')]" mode="createPlugin">
    <xsl:copy>
      <xsl:call-template name="inheritMapAttributesForEclipse"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*|text()|comment()|processing-instruction()" mode="createPlugin"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/indexExtension ')] |
                       *[contains(@class,' eclipsemap/contentExtension ')] |
                       *[contains(@class,' eclipsemap/contextExtension ')] |
                       *[contains(@class,' eclipsemap/extension ')]">
    <xsl:copy>
      <xsl:call-template name="inheritMapAttributesForEclipse"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*|text()|comment()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/tocref ') or contains(@class,' eclipsemap/primarytocref ')][@href][@format!='ditamap']">
    <!-- Do not try to process a reference to an XML file or other file -->
    <xsl:copy>
      <xsl:call-template name="inheritMapAttributesForEclipse"/>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="*|text()|comment()|processing-instruction()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="/">
    <xsl:choose>
      <xsl:when test="contains($TRANSTYPE,'eclipse') and *[contains(@class,' eclipsemap/plugin ')]">
        <xsl:apply-templates mode="createPlugin"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
