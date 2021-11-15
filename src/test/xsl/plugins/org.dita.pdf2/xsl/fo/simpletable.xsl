<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  exclude-result-prefixes="xs dita-ot"
  version="2.0">
  
  <!--xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/basic-settings.xsl"/>
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/commons-attr.xsl"/>
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/tables-attr.xsl"/-->
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/xsl/fo/tables.xsl"/>
  
  <!-- Mocks -->
  
  <xsl:variable name="locale" select="'en'"/>

  <xsl:attribute-set name="dl__body"/>
  <xsl:attribute-set name="dl.dlhead__row"/>
  <xsl:attribute-set name="dl.dlhead"/>
  <xsl:attribute-set name="dl"/>
  <xsl:attribute-set name="dlentry.dd__content"/>
  <xsl:attribute-set name="dlentry.dd"/>
  <xsl:attribute-set name="dlentry.dt__content"/>
  <xsl:attribute-set name="dlentry.dt"/>
  <xsl:attribute-set name="dlentry"/>
  <xsl:attribute-set name="dlhead.ddhd__cell"/>
  <xsl:attribute-set name="dlhead.ddhd__content"/>
  <xsl:attribute-set name="dlhead.dthd__cell"/>
  <xsl:attribute-set name="dlhead.dthd__content"/>
  <xsl:attribute-set name="relcell"/>
  <xsl:attribute-set name="relcolspec"/>
  <xsl:attribute-set name="relheader"/>
  <xsl:attribute-set name="relrow"/>
  <xsl:attribute-set name="reltable__title"/>
  <xsl:attribute-set name="reltable"/>
  <xsl:attribute-set name="simpletable__body"/>
  <xsl:attribute-set name="simpletable"/>
  <xsl:attribute-set name="sthead__row"/>
  <xsl:attribute-set name="sthead.stentry__content"/>
  <xsl:attribute-set name="sthead.stentry__keycol-content"/>
  <xsl:attribute-set name="sthead.stentry"/>
  <xsl:attribute-set name="sthead"/>
  <xsl:attribute-set name="strow.stentry__content"/>
  <xsl:attribute-set name="strow.stentry__keycol-content"/>
  <xsl:attribute-set name="strow.stentry"/>
  <xsl:attribute-set name="strow"/>
  <xsl:attribute-set name="table__container"/>
  <xsl:attribute-set name="table.tgroup"/>
  <xsl:attribute-set name="table.title"/>
  <xsl:attribute-set name="table"/>
  <xsl:attribute-set name="tbody.row.entry__content"/>
  <xsl:attribute-set name="tbody.row.entry__content"/>
  <xsl:attribute-set name="tbody.row.entry__firstcol"/>
  <xsl:attribute-set name="tbody.row.entry"/>
  <xsl:attribute-set name="tbody.row"/>
  <xsl:attribute-set name="tgroup.tbody"/>
  <xsl:attribute-set name="tgroup.thead"/>
  <xsl:attribute-set name="thead.row.entry__content"/>
  <xsl:attribute-set name="thead.row.entry__content"/>
  <xsl:attribute-set name="thead.row.entry"/>
  <xsl:attribute-set name="thead.row"/>
  <xsl:attribute-set name="thead__tableframe__bottom"/>
  <xsl:attribute-set name="__tableframe__bottom"/>
  <xsl:attribute-set name="__tableframe__top"/>
  <xsl:attribute-set name="__tableframe__right"/>
  <xsl:attribute-set name="__tableframe__right"/>
  <xsl:attribute-set name="table__tableframe__all"/>
  <xsl:attribute-set name="table__tableframe__topbot"/>
  <xsl:attribute-set name="table__tableframe__top"/>
  <xsl:attribute-set name="table__tableframe__bottom"/>
  <xsl:attribute-set name="table__tableframe__sides"/>
  <xsl:attribute-set name="__tableframe__top"/>
  <xsl:attribute-set name="__tableframe__left"/>
  <xsl:attribute-set name="__tableframe__right"/>
  <xsl:attribute-set name="__tableframe__bottom"/>
  <xsl:attribute-set name="__tableframe__right"/>
  
  <xsl:template name="get-id"/>
  
  <xsl:key name="enumerableByClass"
    match="*[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]] |
           *[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]] |
           *[contains(@class, ' topic/simpletable ')][*[contains(@class, ' topic/title ')]] |
           *[contains(@class,' topic/fn ') and empty(@callout)]"
    use="tokenize(@class, ' ')"/>
  
  <xsl:template match="*[contains(@class,' topic/title ')]" mode="customTitleAnchor"/>
  
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
  </xsl:template>
  
  <xsl:template name="setExpanse"/>
  
  <xsl:template match="*" mode="ancestor-start-flag"/>
  <xsl:template match="*" mode="ancestor-end-flag"/>
  
</xsl:stylesheet>