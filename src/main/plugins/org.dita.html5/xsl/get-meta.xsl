<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2004, 2005 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs">

  <!-- Get each value in each <keywords>. Nested indexterms should have unique entries. Other
       elements (based on keyword) cannot nest. -->
  <!-- Deprecated since 4.2.1 -->
  <xsl:key name="meta-keywords" match="*[ancestor::*[contains(@class,' topic/keywords ')]]" use="text()[1]"/>

  <!-- Deprecated since 3.6 -->
  <xsl:template name="getMeta">
    <xsl:apply-templates select="." mode="getMeta"/>
  </xsl:template>

  <xsl:template match="*" mode="getMeta">
    <!-- Processing note:
     getMeta is issued from the topic/topic context, therefore it is looking DOWN
     for most data except for attributes on topic, which will be current context.
    -->

    <meta name="generator" content="DITA-OT"/>

    <!-- = = = = = = = = = = = CONTENT = = = = = = = = = = = -->

    <!-- CONTENT: Type -->
    <xsl:apply-templates select="." mode="gen-type-metadata"/>

    <!-- CONTENT: Title - title -->
    <xsl:apply-templates select="*[contains(@class,' topic/title ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/title ')]" mode="gen-metadata"/>

    <!-- CONTENT: Description - shortdesc -->
    <xsl:apply-templates select="*[contains(@class,' topic/shortdesc ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' map/shortdesc ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/shortdesc ')]" mode="gen-metadata"/>

    <!-- CONTENT: Abstract - abstract -->
    <xsl:apply-templates select="*[contains(@class,' topic/abstract ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/abstract ')]" mode="gen-shortdesc-metadata"/>

    <!-- CONTENT: Source - prolog/source/@href -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/source ')]/@href |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/source ')]/@href |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/source ')]/@href" mode="gen-metadata"/>

    <!-- CONTENT: Coverage prolog/metadata/category -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/category ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/category ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/category ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/category ')]" mode="gen-metadata"/>

    <!-- CONTENT: Subject - prolog/metadata/keywords -->
    <xsl:apply-templates select="." mode="gen-keywords-metadata"/>

    <!-- CONTENT: Relation - related-links -->
    <xsl:apply-templates select="*[contains(@class,' topic/related-links ')]/descendant::*/@href |
                                 self::dita/*/*[contains(@class,' topic/related-links ')]/descendant::*/@href" mode="gen-metadata"/>

    <!-- = = = = = = = = = = = Product - Audience = = = = = = = = = = = -->
    <!-- Audience -->
    <!-- prolog/metadata/audience/@experiencelevel and other attributes -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@experiencelevel |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/audience ')]/@experiencelevel |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@experiencelevel |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@experiencelevel" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@importance |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/audience ')]/@importance |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@importance |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@importance" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@job |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/audience ')]/@job |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@job |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@job" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@name |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/audience ')]/@name |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@name |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@name" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@type |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/audience ')]/@type |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@type |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@type" mode="gen-metadata"/>


    <!-- <prodname> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')]" mode="gen-metadata"/>

    <!-- <vrmlist><vrm modification="3" release="2" version="5"/></vrmlist> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@version |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@version |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@version |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@version" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@release |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@release |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@release |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@release" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@modification |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@modification |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@modification |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@modification" mode="gen-metadata"/>

    <!-- <brand> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/brand ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/brand ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/brand ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/brand ')]" mode="gen-metadata"/>
    <!-- <component> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/component ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/component ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/component ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/component ')]" mode="gen-metadata"/>
    <!-- <featnum> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/featnum ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/featnum ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/featnum ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/featnum ')]" mode="gen-metadata"/>
    <!-- <prognum> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prognum ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prognum ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prognum ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prognum ')]" mode="gen-metadata"/>
    <!-- <platform> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/platform ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/platform ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/platform ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/platform ')]" mode="gen-metadata"/>
    <!-- <series> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/series ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/series ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/series ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/series ')]" mode="gen-metadata"/>

    <!-- = = = = = = = = = = = INTELLECTUAL PROPERTY = = = = = = = = = = = -->

    <!-- INTELLECTUAL PROPERTY: Contributor - prolog/author -->
    <!-- INTELLECTUAL PROPERTY: Creator -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/author ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/author ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/author ')]" mode="gen-metadata"/>

    <!-- INTELLECTUAL PROPERTY: Publisher - prolog/publisher -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/publisher ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/publisher ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/publisher ')]" mode="gen-metadata"/>

    <!-- INTELLECTUAL PROPERTY: Rights - prolog/copyright -->
    <!-- Put primary first, then secondary, then remainder -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='primary'] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/copyright ')][@type='primary'] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='primary']" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='secondary'] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/copyright ')][@type='secondary'] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='seconday']" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][not(@type)] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/copyright ')][not(@type)] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][not(@type)]" mode="gen-metadata"/>

    <!-- Usage Rights - prolog/permissions -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/permissions ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/permissions ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/permissions ')]" mode="gen-metadata"/>

    <!-- = = = = = = = = = = = INSTANTIATION = = = = = = = = = = = -->

    <!-- INSTANTIATION: Date - prolog/critdates/created -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/created ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/created ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/created ')]" mode="gen-metadata"/>

    <!-- prolog/critdates/revised/@modified -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@modified |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@modified |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@modified" mode="gen-metadata"/>

    <!-- prolog/critdates/revised/@golive -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@golive |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@golive |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@golive" mode="gen-metadata"/>

    <!-- prolog/critdates/revised/@expiry -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@expiry |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@expiry |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@expiry" mode="gen-metadata"/>

    <!-- prolog/metadata/othermeta -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/othermeta ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/othermeta ')] |
                                 *[contains(@class,' map/topicmeta ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/othermeta ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/othermeta ')]" mode="gen-metadata"/>

    <!-- INSTANTIATION: Format -->
    <xsl:apply-templates select="." mode="gen-format-metadata"/>

    <!-- INSTANTIATION: Identifier --> <!-- id is an attribute on Topic -->
    <xsl:apply-templates select="@id | self::dita/*[1]/@id" mode="gen-metadata"/>

    <!-- INSTANTIATION: Language -->
    <xsl:apply-templates select="@xml:lang | self::dita/*[1]/@xml:lang" mode="gen-metadata"/>
  </xsl:template>


  <!-- CONTENT: Type -->
  <xsl:template match="dita" mode="gen-type-metadata">
    <xsl:apply-templates select="*[1]" mode="#current"/>
  </xsl:template>

  <xsl:template match="*" mode="gen-type-metadata">
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/abstract ')]" mode="gen-shortdesc-metadata">
    <xsl:variable name="shortmeta" as="xs:string">
      <xsl:value-of>
        <xsl:for-each select="*[contains(@class,' topic/shortdesc ')]">
          <xsl:text> </xsl:text>
          <xsl:apply-templates select="*|text()" mode="text-only"/>
        </xsl:for-each>
      </xsl:value-of>
    </xsl:variable>
    <xsl:if test="normalize-space($shortmeta)">
      <meta name="abstract" content="{normalize-space($shortmeta)}"/>
      <meta name="description" content="{normalize-space($shortmeta)}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*[contains(@class,' map/shortdesc ')]" mode="gen-metadata">
    <xsl:variable name="content" as="xs:string">
      <xsl:value-of>
        <xsl:apply-templates mode="text-only"/>
      </xsl:value-of>
    </xsl:variable>
    <xsl:if test="normalize-space($content)">
      <meta name="description" content="{normalize-space($content)}"/>
    </xsl:if>
  </xsl:template>

  <!-- CONTENT: Subject - prolog/metadata/keywords -->
  <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="gen-keywords-metadata">
    <xsl:variable name="keywords" as="element()*"
                  select="descendant::*[contains(@class,' topic/prolog ')]/
                            *[contains(@class,' topic/metadata ')]/
                              *[contains(@class,' topic/keywords ')]/
                                *[contains(@class,' topic/keyword ')][normalize-space()]"/>
    <xsl:if test="exists($keywords)">
      <meta name="keywords" content="{string-join(distinct-values($keywords/normalize-space()), ', ')}"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' map/map ')]" mode="gen-keywords-metadata">
    <xsl:variable name="topicmeta" as="element()*"
                  select="*[contains(@class,' map/topicmeta ')]"/>
    <xsl:variable name="keywords" as="element()*"
                  select="($topicmeta | $topicmeta/*[contains(@class,' topic/metadata ')])/
                            *[contains(@class,' topic/keywords ')]/
                              *[contains(@class,' topic/keyword ')][normalize-space()]"/>
    <xsl:if test="exists($keywords)">
      <meta name="keywords" content="{string-join(distinct-values($keywords/normalize-space()), ', ')}"/>
    </xsl:if>
  </xsl:template>

  <!--  Rights - prolog/copyright -->
  <xsl:template match="*[contains(@class,' topic/copyright ')]" mode="gen-metadata">
    <meta name="rights">
      <xsl:attribute name="content">
        <xsl:text>&#xA9; </xsl:text>
        <xsl:apply-templates select="*[contains(@class,' topic/copyryear ')][1]" mode="#current"/>
        <xsl:text> </xsl:text>
        <xsl:if test="*[contains(@class,' topic/copyrholder ')]">
          <xsl:value-of select="*[contains(@class,' topic/copyrholder ')]"/>
        </xsl:if>
      </xsl:attribute>
    </meta>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/copyryear ')]" mode="gen-metadata">
    <xsl:param name="previous" select="()"/>
    <xsl:param name="open-sequence" select="false()"/>
    <xsl:variable name="next" select="following-sibling::*[contains(@class,' topic/copyryear ')][1]"/>
    <xsl:variable name="begin-sequence" select="@year + 1 = number($next/@year)"/>
    <xsl:choose>
      <xsl:when test="$begin-sequence">
        <xsl:if test="not($open-sequence)">
          <xsl:value-of select="@year"/>
          <xsl:text>&#x2013;</xsl:text>
        </xsl:if>
      </xsl:when>
      <xsl:when test="$next">
        <xsl:value-of select="@year"/>
        <xsl:text>, </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@year"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="$next" mode="#current">
      <xsl:with-param name="previous" select="."/>
      <xsl:with-param name="open-sequence" select="$begin-sequence"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- = = = = = = = = = = = Product - Audience = = = = = = = = = = = -->

  <xsl:template match="*[contains(@class,' topic/prodname ')]" mode="gen-metadata">
    <xsl:variable name="prodnamemeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="prodname" content="{normalize-space($prodnamemeta)}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/vrm ')]/@version" mode="gen-metadata">
    <meta name="version" content="{.}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/vrm ')]/@release" mode="gen-metadata">
    <meta name="release" content="{.}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/vrm ')]/@modification" mode="gen-metadata">
    <meta name="modification" content="{.}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/brand ')]" mode="gen-metadata">
    <xsl:variable name="brandmeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="brand" content="{normalize-space($brandmeta)}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/component ')]" mode="gen-metadata">
    <xsl:variable name="componentmeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="component" content="{normalize-space($componentmeta)}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/featnum ')]" mode="gen-metadata">
    <xsl:variable name="featnummeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="featnum" content="{normalize-space($featnummeta)}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/prognum ')]" mode="gen-metadata">
    <xsl:variable name="prognummeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="prognum" content="{normalize-space($prognummeta)}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/platform ')]" mode="gen-metadata">
    <xsl:variable name="platformmeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="platform" content="{normalize-space($platformmeta)}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/series ')]" mode="gen-metadata">
    <xsl:variable name="seriesmeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="series" content="{normalize-space($seriesmeta)}"/>
  </xsl:template>

  <!-- prolog/metadata/othermeta -->
  <xsl:template match="*[contains(@class,' topic/othermeta ')]" mode="gen-metadata">
    <meta name="{@name}" content="{@content}"/>
  </xsl:template>

  <xsl:template match="@* | node()" mode="gen-metadata gen-format-metadata" priority="0"/>

</xsl:stylesheet>
