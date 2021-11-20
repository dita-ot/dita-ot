<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2011 Jarno Elovirta

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version="2.0">
    
  <xsl:attribute-set name="simple-page-master">
    <xsl:attribute name="page-width" select="$page-width"/>
    <xsl:attribute name="page-height" select="$page-height"/>
  </xsl:attribute-set>
  
  <!-- legacy attribute set -->
  <xsl:attribute-set name="region-body" use-attribute-sets="region-body.odd"/>
  
  <xsl:attribute-set name="region-body.odd">
    <xsl:attribute name="margin-top" select="$page-margin-top"/>
    <xsl:attribute name="margin-bottom" select="$page-margin-bottom"/>
    <xsl:attribute name="{if ($writing-mode = 'lr') then 'margin-left' else 'margin-right'}" select="$page-margin-inside"/>
    <xsl:attribute name="{if ($writing-mode = 'lr') then 'margin-right' else 'margin-left'}" select="$page-margin-outside"/>
  </xsl:attribute-set>

  <xsl:attribute-set name="region-body.even">
    <xsl:attribute name="margin-top" select="$page-margin-top"/>
    <xsl:attribute name="margin-bottom" select="$page-margin-bottom"/>
    <xsl:attribute name="{if ($writing-mode = 'lr') then 'margin-left' else 'margin-right'}" select="$page-margin-outside"/>
    <xsl:attribute name="{if ($writing-mode = 'lr') then 'margin-right' else 'margin-left'}" select="$page-margin-inside"/>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="region-body__frontmatter.odd" use-attribute-sets="region-body.odd">
  </xsl:attribute-set>
  <xsl:attribute-set name="region-body__frontmatter.even" use-attribute-sets="region-body.even">
  </xsl:attribute-set>

  <xsl:attribute-set name="region-body__backcover.odd" use-attribute-sets="region-body.odd">
  </xsl:attribute-set>
  <xsl:attribute-set name="region-body__backcover.even" use-attribute-sets="region-body.even">
  </xsl:attribute-set>

  <!-- legacy attribute set -->
  <xsl:attribute-set name="region-body__index" use-attribute-sets="region-body__index.odd"/>

  <xsl:attribute-set name="region-body__index.odd" use-attribute-sets="region-body.odd">
    <xsl:attribute name="column-count">2</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="region-body__index.even" use-attribute-sets="region-body.even">
    <xsl:attribute name="column-count">2</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="region-before">
    <xsl:attribute name="extent" select="$page-margin-top"/>
    <xsl:attribute name="display-align">before</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name="region-after">
    <xsl:attribute name="extent" select="$page-margin-bottom"/>
    <xsl:attribute name="display-align">after</xsl:attribute>
  </xsl:attribute-set>
    
</xsl:stylesheet>
