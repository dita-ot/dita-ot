<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  
  <!--xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/basic-settings.xsl"/>
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/commons-attr.xsl"/>
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/tables-attr.xsl"/-->
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/xsl/fo/tables.xsl"/>
  
  
  <!--xsl:variable name="writing-mode" select="'lr'"/-->
  <xsl:attribute-set name="table.tgroup"/>
  <xsl:attribute-set name="simpletable"/>
  <xsl:attribute-set name="simpletable__body"/>
  <xsl:attribute-set name="strow"/>
  <xsl:attribute-set name="strow.stentry__keycol-content"/>
  <xsl:attribute-set name="strow.stentry__content"/>
  <xsl:attribute-set name="strow.stentry"/>
  <xsl:attribute-set name="sthead__row"/>
  <xsl:attribute-set name="sthead"/>
  <xsl:attribute-set name="sthead.stentry__keycol-content"/>
  <xsl:attribute-set name="sthead.stentry__content"/>
  <xsl:attribute-set name="sthead.stentry"/>
  
  <xsl:template name="output-message">
    <xsl:param name="id"/>
  </xsl:template>
  
  <xsl:template name="commonattributes"/>
  
  <xsl:template name="processAttrSetReflection">
    <xsl:param name="attrSet"/>
    <xsl:param name="path"/>
  </xsl:template>
  
  <xsl:template name="setExpanse"/>
  
  <xsl:template match="*" mode="ancestor-start-flag"/>
  <xsl:template match="*" mode="ancestor-end-flag"/>
  
</xsl:stylesheet>