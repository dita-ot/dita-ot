<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2016 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  version="2.0"
  exclude-result-prefixes="xs dita-ot">
  
  <xsl:template match="*[contains(@class, ' topic/thead ')]/*[contains(@class, ' topic/row ')]/*[contains(@class, ' topic/entry ')]">
    <xsl:apply-templates select="." mode="validate-entry-position"/>
    <fo:table-cell xsl:use-attribute-sets="thead.row.entry">
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="applySpansAttrs"/>
      <xsl:call-template name="applyAlignAttrs"/>
      <xsl:call-template name="generateTableEntryBorder"/>
      <fo:block xsl:use-attribute-sets="thead.row.entry__content">
        <xsl:call-template name="processEntryContent"/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>
  
  <xsl:template match="*" mode="processTableEntry">
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="applySpansAttrs"/>
    <xsl:call-template name="applyAlignAttrs"/>
    <xsl:call-template name="generateTableEntryBorder"/>
    <fo:block xsl:use-attribute-sets="tbody.row.entry__content">
      <xsl:call-template name="processEntryContent"/>
    </fo:block>
  </xsl:template>

</xsl:stylesheet>
