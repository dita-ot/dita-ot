<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project. 
See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:attribute-set name="hazardstatement">
    <xsl:attribute name="width">100%</xsl:attribute>
    <xsl:attribute name="space-before">8pt</xsl:attribute>
    <xsl:attribute name="space-after">10pt</xsl:attribute>
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="border-width">5pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="hazardstatement.cell">
    <xsl:attribute name="start-indent">0pt</xsl:attribute>
    <xsl:attribute name="border-style">solid</xsl:attribute>
    <xsl:attribute name="border-color">black</xsl:attribute>
    <xsl:attribute name="border-width">2pt</xsl:attribute>
    <xsl:attribute name="padding">3pt</xsl:attribute>
    <xsl:attribute name="keep-together">always</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="hazardstatement.title" use-attribute-sets="hazardstatement.cell common.title">
    <xsl:attribute name="number-columns-spanned">2</xsl:attribute>
    <xsl:attribute name="text-transform">uppercase</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
    <xsl:attribute name="font-size">1.5em</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="hazardstatement.title.danger">
    <xsl:attribute name="color">white</xsl:attribute>
    <xsl:attribute name="background-color">red</xsl:attribute>
    <xsl:attribute name="font-style">normal</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="hazardstatement.title.warning">
    <xsl:attribute name="background-color">orange</xsl:attribute>
    <xsl:attribute name="font-style">normal</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="hazardstatement.title.caution">
    <xsl:attribute name="background-color">yellow</xsl:attribute>
    <xsl:attribute name="font-style">normal</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="hazardstatement.title.notice">
    <xsl:attribute name="color">white</xsl:attribute>
    <xsl:attribute name="font-style">italic</xsl:attribute>
    <xsl:attribute name="background-color">blue</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="hazardstatement.image" use-attribute-sets="hazardstatement.cell">
    <xsl:attribute name="text-align">center</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="hazardstatement.image.column">
    <xsl:attribute name="column-width">6em</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="hazardstatement.content" use-attribute-sets="hazardstatement.cell">
    
  </xsl:attribute-set>

  <xsl:attribute-set name="hazardstatement.content.column">
    
  </xsl:attribute-set>
  
  <xsl:attribute-set name="messagepanel">
    
  </xsl:attribute-set>
  
  <xsl:attribute-set name="consequence">
    
  </xsl:attribute-set>
  
  <xsl:attribute-set name="howtoavoid">
    
  </xsl:attribute-set>
  
  <xsl:attribute-set name="typeofhazard">
    
  </xsl:attribute-set>
  
  <xsl:attribute-set name="hazardsymbol" use-attribute-sets="image">
    <xsl:attribute name="content-width">4em</xsl:attribute>
    <xsl:attribute name="width">4em</xsl:attribute>
  </xsl:attribute-set>
  
</xsl:stylesheet>