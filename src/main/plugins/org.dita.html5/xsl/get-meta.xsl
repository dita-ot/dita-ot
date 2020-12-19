<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2004, 2005 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- Get each value in each <keywords>. Nested indexterms should have unique entries. Other
       elements (based on keyword) cannot nest. -->
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
                                 self::dita/*[1]/*[contains(@class,' topic/shortdesc ')]" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/abstract ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/abstract ')]" mode="gen-shortdesc-metadata"/>

    <!-- CONTENT: Source - prolog/source/@href -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/source ')]/@href |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/source ')]/@href" mode="gen-metadata"/>

    <!-- CONTENT: Coverage prolog/metadata/category -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/category ')] |
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
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@experiencelevel" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@importance |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@importance" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@job |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@job" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@name |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@name" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@type |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/audience ')]/@type" mode="gen-metadata"/>


    <!-- <prodname> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prodname ')]" mode="gen-metadata"/>

    <!-- <vrmlist><vrm modification="3" release="2" version="5"/></vrmlist> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@version |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@version" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@release |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@release" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@modification |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/vrmlist ')]/*[contains(@class,' topic/vrm ')]/@modification" mode="gen-metadata"/>

    <!-- <brand> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/brand ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/brand ')]" mode="gen-metadata"/>
    <!-- <component> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/component ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/component ')]" mode="gen-metadata"/>
    <!-- <featnum> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/featnum ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/featnum ')]" mode="gen-metadata"/>
    <!-- <prognum> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prognum ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/prognum ')]" mode="gen-metadata"/>
    <!-- <platform> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/platform ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/platform ')]" mode="gen-metadata"/>
    <!-- <series> -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/series ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/prodinfo ')]/*[contains(@class,' topic/series ')]" mode="gen-metadata"/>

    <!-- = = = = = = = = = = = INTELLECTUAL PROPERTY = = = = = = = = = = = -->

    <!-- INTELLECTUAL PROPERTY: Contributor - prolog/author -->
    <!-- INTELLECTUAL PROPERTY: Creator -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/author ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/author ')]" mode="gen-metadata"/>

    <!-- INTELLECTUAL PROPERTY: Publisher - prolog/publisher -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/publisher ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/publisher ')]" mode="gen-metadata"/>

    <!-- INTELLECTUAL PROPERTY: Rights - prolog/copyright -->
    <!-- Put primary first, then secondary, then remainder -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='primary'] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='primary']" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='secondary'] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][@type='seconday']" mode="gen-metadata"/>
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][not(@type)] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/copyright ')][not(@type)]" mode="gen-metadata"/>

    <!-- Usage Rights - prolog/permissions -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/permissions ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/permissions ')]" mode="gen-metadata"/>

    <!-- = = = = = = = = = = = INSTANTIATION = = = = = = = = = = = -->

    <!-- INSTANTIATION: Date - prolog/critdates/created -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/created ')] |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/created ')]" mode="gen-metadata"/>

    <!-- prolog/critdates/revised/@modified -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@modified |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@modified" mode="gen-metadata"/>

    <!-- prolog/critdates/revised/@golive -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@golive |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@golive" mode="gen-metadata"/>

    <!-- prolog/critdates/revised/@expiry -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@expiry |
                                 self::dita/*[1]/*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@expiry" mode="gen-metadata"/>

    <!-- prolog/metadata/othermeta -->
    <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/othermeta ')] |
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
    <xsl:apply-templates select="*[1]" mode="gen-type-metadata"/>
  </xsl:template>

  <xsl:template match="*" mode="gen-type-metadata">
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/abstract ')]" mode="gen-shortdesc-metadata">
    <xsl:variable name="shortmeta">
      <xsl:for-each select="*[contains(@class,' topic/shortdesc ')]">
        <xsl:text> </xsl:text>
        <xsl:apply-templates select="*|text()" mode="text-only"/>
      </xsl:for-each>
    </xsl:variable>
    <xsl:if test="normalize-space($shortmeta)!=''">
      <meta name="abstract" content="{normalize-space($shortmeta)}"/>
      <meta name="description" content="{normalize-space($shortmeta)}"/>
    </xsl:if>
  </xsl:template>

  <!-- CONTENT: Subject - prolog/metadata/keywords -->
  <xsl:template match="*" mode="gen-keywords-metadata">
    <xsl:variable name="keywords-content">
      <!-- for each item inside keywords (including nested index terms) -->
      <xsl:for-each select="descendant::*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/keywords ')]/descendant-or-self::*">
        <!-- If this is the first term or keyword with this value -->
        <xsl:if test="generate-id(key('meta-keywords',text()[1])[1]) = generate-id(.)">
          <xsl:if test="position() > 2">
            <xsl:text>, </xsl:text>
          </xsl:if>
          <xsl:value-of select="normalize-space(text()[1])"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:if test="string-length($keywords-content) > 0">
      <meta name="keywords" content="{$keywords-content}"/>
    </xsl:if>
  </xsl:template>

  <!--  Rights - prolog/copyright -->
  <xsl:template match="*[contains(@class,' topic/copyright ')]" mode="gen-metadata">
    <meta name="rights">
      <xsl:attribute name="content">
        <xsl:text>&#xA9; </xsl:text>
        <xsl:apply-templates select="*[contains(@class,' topic/copyryear ')][1]" mode="gen-metadata"/>
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
    <xsl:apply-templates select="$next" mode="gen-metadata">
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
