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
  <xsl:variable name="maxCharsInShortDesc" as="xs:integer">0</xsl:variable>
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
  <xsl:attribute-set name="topic"/>
  <xsl:attribute-set name="topic.title"/>
  <xsl:attribute-set name="topic.topic.title"/>
  <xsl:attribute-set name="topic.topic.topic.title"/>
  <xsl:attribute-set name="topic.topic.topic.topic.title"/>
  <xsl:attribute-set name="topic.topic.topic.topic.topic.title"/>
  <xsl:attribute-set name="topic.topic.topic.topic.topic.topic.title"/>
  <xsl:attribute-set name="topic.title__content"/>
  <xsl:attribute-set name="topic.topic.title__content"/>
  <xsl:attribute-set name="topic.topic.topic.title__content"/>
  <xsl:attribute-set name="topic.topic.topic.topic.title__content"/>
  <xsl:attribute-set name="topic.topic.topic.topic.topic.title__content"/>
  <xsl:attribute-set name="topic.topic.topic.topic.topic.topic.title__content"/>
  <xsl:attribute-set name="lq_simple"/>
  <xsl:attribute-set name="__align__left"/>
  <xsl:attribute-set name="__align__right"/>
  <xsl:attribute-set name="__align__center"/>
  <xsl:attribute-set name="__align__justify"/>
  <xsl:attribute-set name="section.title"/>
  <xsl:attribute-set name="example.title"/>
  <xsl:attribute-set name="tm__content__service"/>
  <xsl:attribute-set name="tm__content"/>
  <xsl:attribute-set name="tm__content"/>
  <xsl:attribute-set name="tm"/>
  <xsl:attribute-set name="titlealts"/>
  <xsl:attribute-set name="navtitle__label"/>
  <xsl:attribute-set name="navtitle"/>
  <xsl:attribute-set name="searchtitle__label"/>
  <xsl:attribute-set name="searchtitle"/>
  <xsl:attribute-set name="abstract"/>
  <xsl:attribute-set name="topic__shortdesc"/>
  <xsl:attribute-set name="shortdesc"/>
  <xsl:attribute-set name="body__toplevel"/>
  <xsl:attribute-set name="body__secondLevel"/>
  <xsl:attribute-set name="body"/>
  <xsl:attribute-set name="section.title"/>
  <xsl:attribute-set name="section__content"/>
  <xsl:attribute-set name="section"/>
  <xsl:attribute-set name="example__content"/>
  <xsl:attribute-set name="example"/>
  <xsl:attribute-set name="div"/>
  <xsl:attribute-set name="p"/>
  <xsl:attribute-set name="note__label__note"/>
  <xsl:attribute-set name="note__label__notice"/>
  <xsl:attribute-set name="note__label__tip"/>
  <xsl:attribute-set name="note__label__fastpath"/>
  <xsl:attribute-set name="note__label__restriction"/>
  <xsl:attribute-set name="note__label__important"/>
  <xsl:attribute-set name="note__label__remember"/>
  <xsl:attribute-set name="note__label__attention"/>
  <xsl:attribute-set name="note__label__caution"/>
  <xsl:attribute-set name="note__label__danger"/>
  <xsl:attribute-set name="note__label__danger"/>
  <xsl:attribute-set name="note__label__trouble"/>
  <xsl:attribute-set name="note__label__warning"/>
  <xsl:attribute-set name="note__label__other"/>
  <xsl:attribute-set name="note__label"/>
  <xsl:attribute-set name="note"/>
  <xsl:attribute-set name="note__image__column"/>
  <xsl:attribute-set name="note__text__column"/>
  <xsl:attribute-set name="note__image"/>
  <xsl:attribute-set name="note__image__entry"/>
  <xsl:attribute-set name="note__text__entry"/>
  <xsl:attribute-set name="note__table"/>
  <xsl:attribute-set name="lq"/>
  <xsl:attribute-set name="lq_link"/>
  <xsl:attribute-set name="lq_title"/>
  <xsl:attribute-set name="q"/>
  <xsl:attribute-set name="figgroup"/>
  <xsl:attribute-set name="pre"/>
  <xsl:attribute-set name="lines"/>
  <xsl:attribute-set name="ph"/>
  <xsl:attribute-set name="boolean"/>
  <xsl:attribute-set name="state"/>
  <xsl:attribute-set name="image.artlabel"/>
  <xsl:attribute-set name="image"/>
  <xsl:attribute-set name="alt"/>
  <xsl:attribute-set name="object"/>
  <xsl:attribute-set name="param"/>
  <xsl:attribute-set name="draft-comment__label"/>
  <xsl:attribute-set name="draft-comment"/>
  <xsl:attribute-set name="required-cleanup__label"/>
  <xsl:attribute-set name="required-cleanup"/>
  <xsl:attribute-set name="fn__callout"/>
  <xsl:attribute-set name="fn__callout"/>
  <xsl:attribute-set name="fn__body"/>
  <xsl:attribute-set name="indextermref"/>
  <xsl:attribute-set name="cite"/>
  <xsl:key name="map-id" match="map-id">map-id</xsl:key>
  <xsl:key name="enumerableByClass" match="enumerableByClass">enumerableByClass</xsl:key>
  <xsl:template name="buildBasicLinkDestination">
  <xsl:param name="scope" />
  <xsl:param name="format" />
  <xsl:param name="href" />
  </xsl:template>
  <xsl:template name="get-id"/>
  <xsl:template name="determineTopicType"/>
  <xsl:template name="get-attributes" as="attribute()*">
  <xsl:param name="element" as="element()"/>
  </xsl:template>
  <xsl:template name="topic-title-mock" match="*[contains(@class,' topic/title ')]"><topic-title/></xsl:template>
  <xsl:template name="topic-desc-mock" match="*[contains(@class,' topic/desc ')]"><topic-desc/></xsl:template>
  <xsl:template name="topic-image-mock" match="*[contains(@class,' topic/image ')]"><topic-image/></xsl:template>
  <xsl:template name="getVariable">
  <xsl:param name="id"/>
  <xsl:param name="params"/>
  </xsl:template>
  
</xsl:stylesheet>