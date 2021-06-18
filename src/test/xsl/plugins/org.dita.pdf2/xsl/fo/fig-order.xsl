<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  exclude-result-prefixes="xs dita-ot"
  version="2.0">
  
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/cfg/fo/attrs/basic-settings.xsl"/>
  <xsl:import href="../../../../../../main/plugins/org.dita.pdf2/xsl/fo/topic.xsl"/>
  
  <!-- Mocks -->
  <xsl:variable name="writing-mode"/>
  <xsl:variable name="maxCharsInShortDesc "/>
  <xsl:function name="dita-ot:matches-searchtitle-class">
  <xsl:param name="argument"/>
  </xsl:function>
  <xsl:function name="dita-ot:matches-shortdesc-class">
  <xsl:param name="argument"/>
  </xsl:function>
  <xsl:function name="dita-ot:notExcludedByDraftElement">
  <xsl:param name="argument"/>
  </xsl:function>
  <xsl:attribute-set name="xref"/>
  <xsl:attribute-set name="fig"/>
  <xsl:attribute-set name="fig.title"/>
  <xsl:attribute-set name="desc"/>
  <xsl:attribute-set name="term"/>
  <xsl:attribute-set name="__spectitle"/>
  <xsl:attribute-set name="__border__top"/>
  <xsl:attribute-set name="__border__bot"/>
  <xsl:attribute-set name="__border__topbot"/>
  <xsl:attribute-set name="__border__sides"/>
  <xsl:attribute-set name="__border__all"/>
  <xsl:attribute-set name="__expanse__page"/>
  <xsl:attribute-set name="__expanse__column"/>
  <xsl:attribute-set name="__expanse__spread"/>
  <xsl:attribute-set name="__expanse__textline"/>
  <xsl:attribute-set name="keyword"/>
  <xsl:attribute-set name="image__block"/>
  <xsl:attribute-set name="image__inline"/>
  <xsl:key name="map-id" match="map-id">map-id</xsl:key>
  <xsl:key name="enumerableByClass" match="enumerableByClass">enumerableByClass</xsl:key>
  <xsl:template name="buildBasicLinkDestination"/>
  <xsl:template name="get-id"/>
  <xsl:template name="determineTopicType"/>
  <xsl:template name="processAttrSetReflection">
      <xsl:param name="attrSet"/>
      <xsl:param name="path"/>
  </xsl:template>
  <xsl:template name="topic-title-mock" match="*[contains(@class,' topic/title ')]"><topic-title/></xsl:template>
  <xsl:template name="topic-desc-mock" match="*[contains(@class,' topic/desc ')]"><topic-desc/></xsl:template>
  <xsl:template name="topic-image-mock" match="*[contains(@class,' topic/image ')]"><topic-image/></xsl:template>
  <xsl:template name="getVariable">
    <xsl:param name="id"/>
    <xsl:param name="params"/>
  </xsl:template>
  
</xsl:stylesheet>