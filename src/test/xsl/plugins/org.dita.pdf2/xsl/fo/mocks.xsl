<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot" exclude-result-prefixes="xs dita-ot" version="3.0">

  <xsl:variable name="writing-mode" select="'lr'"/>
  <xsl:variable name="locale" select="'en'"/>

  <xsl:template name="get-id"/>

  <xsl:key name="enumerableByClass" match="
      *[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/simpletable ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/fn ') and empty(@callout)]" use="tokenize(@class, ' ')"/>

  <xsl:template match="*[contains(@class, ' topic/title ')]" mode="customTitleAnchor"/>

  <xsl:function name="dita-ot:notExcludedByDraftElement">
    <xsl:param name="ctx" as="element()"/>
    <xsl:sequence select="true()"/>
  </xsl:function>

  <xsl:template name="getVariable">
    <xsl:param name="id"/>
    <xsl:param name="params"/>
    <xsl:value-of select="$id"/>
    <xsl:for-each select="$params/*">
      <xsl:text> </xsl:text>
      <xsl:copy-of select="node()"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="output-message">
    <xsl:param name="id"/>
  </xsl:template>

  <xsl:template name="commonattributes"/>

  <xsl:template name="get-attributes" as="attribute()*">
    <xsl:param name="element" as="element()"/>
    <xsl:sequence select="$element/@*"/>
  </xsl:template>
  
  <xsl:template name="setExpanse"/>

  <xsl:template match="*" mode="ancestor-start-flag"/>
  <xsl:template match="*" mode="ancestor-end-flag"/>

</xsl:stylesheet>
