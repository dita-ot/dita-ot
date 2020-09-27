<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2004, 2005 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <!-- CONTENT: Type -->
  <xsl:template match="*" mode="gen-type-metadata">
    <meta name="DC.type" content="{name(.)}"/>
  </xsl:template>

  <!-- CONTENT: Source - prolog/source/@href -->
  <xsl:template match="*[contains(@class,' topic/source ')]/@href" mode="gen-metadata">
    <meta name="DC.source" content="{normalize-space(.)}"/>
  </xsl:template>

  <!-- CONTENT: Coverage prolog/metadata/category -->
  <xsl:template match="*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/category ')]"
                mode="gen-metadata">
    <meta name="DC.coverage" content="{normalize-space(.)}"/>
  </xsl:template>

  <!-- CONTENT: Subject - prolog/metadata/keywords -->
  <xsl:template match="*" mode="gen-keywords-metadata">
    <xsl:variable name="keywords-content">
      <!-- for each item inside keywords (including nested index terms) -->
      <xsl:for-each
          select="descendant::*[contains(@class,' topic/prolog ')]/*[contains(@class,' topic/metadata ')]/*[contains(@class,' topic/keywords ')]/descendant-or-self::*">
        <!-- If this is the first term or keyword with this value -->
        <xsl:if test="generate-id(key('meta-keywords', text()[1])[1]) = generate-id(.)">
          <xsl:if test="position() > 2">
            <xsl:text>,</xsl:text>
          </xsl:if>
          <xsl:value-of select="normalize-space(text()[1])"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <xsl:if test="string-length($keywords-content) > 0">
      <meta name="DC.subject" content="{$keywords-content}"/>
    </xsl:if>
    <xsl:next-match/>
  </xsl:template>

  <!-- CONTENT: Relation - related-links -->
  <xsl:template match="*[contains(@class,' topic/link ')]/@href" mode="gen-metadata">
    <xsl:variable name="linkmeta" select="normalize-space(.)"/>
    <xsl:choose>
      <xsl:when test="substring($linkmeta,1,1)='#'"/>  <!-- ignore internal file links -->
      <xsl:otherwise>
        <xsl:variable name="linkmeta_ext">
          <xsl:choose>
            <xsl:when test="not(../@format) or ../@format = 'dita'">
              <xsl:call-template name="replace-extension">
                <xsl:with-param name="filename" select="$linkmeta"/>
                <xsl:with-param name="extension" select="$OUTEXT"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$linkmeta"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <meta name="DC.relation" scheme="URI" content="{$linkmeta_ext}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- INTELLECTUAL PROPERTY: Contributor - prolog/author -->
  <!-- INTELLECTUAL PROPERTY: Creator -->
  <!-- Default is type='creator' -->
  <xsl:template match="*[contains(@class,' topic/author ')]" mode="gen-metadata">
    <xsl:choose>
      <xsl:when test="@type= 'contributor'">
        <meta name="DC.contributor" content="{normalize-space(.)}"/>
      </xsl:when>
      <xsl:otherwise>
        <meta name="DC.creator" content="{normalize-space(.)}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- INTELLECTUAL PROPERTY: Publisher - prolog/publisher -->
  <xsl:template match="*[contains(@class,' topic/publisher ')]" mode="gen-metadata">
    <meta name="DC.publisher" content="{normalize-space(.)}"/>
  </xsl:template>

  <!-- Usage Rights - prolog/permissions -->
  <xsl:template match="*[contains(@class,' topic/permissions ')]" mode="gen-metadata">
    <meta name="DC.rights.usage" content="{@view}"/>
  </xsl:template>

  <!-- = = = = = = = = = = = Product - Audience = = = = = = = = = = = -->
  <!-- Audience -->
  <xsl:template match="*[contains(@class,' topic/audience ')]/@experiencelevel" mode="gen-metadata">
    <meta name="DC.audience.experiencelevel" content="{.}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/audience ')]/@importance" mode="gen-metadata">
    <meta name="DC.audience.importance" content="{.}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/audience ')]/@name" mode="gen-metadata">
    <meta name="DC.audience.name" content="{.}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/audience ')]/@job" mode="gen-metadata">
    <xsl:choose>
      <xsl:when test=". = 'other'">
        <meta name="DC.audience.job" content="{normalize-space(../@otherjob)}"/>
      </xsl:when>
      <xsl:otherwise>
        <meta name="DC.audience.job" content="{.}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/audience ')]/@type" mode="gen-metadata">
    <xsl:choose>
      <xsl:when test=". = 'other'">
        <meta name="DC.audience.type" content="{normalize-space(../@othertype)}"/>
      </xsl:when>
      <xsl:otherwise>
        <meta name="DC.audience.type" content="{.}"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- INSTANTIATION: Date - prolog/critdates/created -->
  <xsl:template match="*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/created ')]"
                mode="gen-metadata">
    <meta name="DC.date.created" content="{@date}"/>
  </xsl:template>

  <!-- prolog/critdates/revised/@modified -->
  <xsl:template match="*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@modified"
                mode="gen-metadata">
    <meta name="DC.date.modified" content="{.}"/>
  </xsl:template>

  <!-- prolog/critdates/revised/@golive -->
  <xsl:template match="*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@golive"
                mode="gen-metadata">
    <meta name="DC.date.issued" content="{.}"/>
    <meta name="DC.date.available" content="{.}"/>
  </xsl:template>

  <!-- prolog/critdates/revised/@expiry -->
  <xsl:template match="*[contains(@class,' topic/critdates ')]/*[contains(@class,' topic/revised ')]/@expiry"
                mode="gen-metadata">
    <meta name="DC.date.expiry" content="{.}"/>
  </xsl:template>

  <!-- INSTANTIATION: Format -->
  <!-- this value is based on output format used for DC indexing, not source.
       Put in this odd template for easy overriding, if creating another output format. -->
  <xsl:template match="*" mode="gen-format-metadata">
    <meta name="DC.format" content="HTML5"/>
  </xsl:template>

  <!-- INSTANTIATION: Identifier --> <!-- id is an attribute on Topic -->
  <xsl:template match="@id" mode="gen-metadata">
    <meta name="DC.identifier" content="{.}"/>
  </xsl:template>

  <!-- INSTANTIATION: Language -->
  <!-- ideally, take the first token of the language attribute value -->
  <xsl:template match="@xml:lang" mode="gen-metadata">
    <meta name="DC.language" content="{.}"/>
  </xsl:template>

  <xsl:template name="generateDefaultCopyright">
    <xsl:if test="not(//*[contains(@class, ' topic/copyright ')])">
      <meta name="copyright">
        <xsl:attribute name="content">
          <xsl:text>(C) </xsl:text>
          <xsl:call-template name="getVariable">
            <xsl:with-param name="id" select="'Copyright'"/>
          </xsl:call-template>
          <xsl:text> </xsl:text>
          <xsl:value-of select="$YEAR"/>
        </xsl:attribute>
      </meta>
      <meta name="DC.rights.owner">
        <xsl:attribute name="content">
          <xsl:text>(C) </xsl:text>
          <xsl:call-template name="getVariable">
            <xsl:with-param name="id" select="'Copyright'"/>
          </xsl:call-template>
          <xsl:text> </xsl:text><xsl:value-of select="$YEAR"/>
        </xsl:attribute>
      </meta>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@* | node()" mode="generateDefaultCopyright">
    <xsl:if test="empty(//*[contains(@class, ' topic/copyright ')])">
      <meta name="DC.rights.owner">
        <xsl:attribute name="content">
          <xsl:text>(C) </xsl:text>
          <xsl:call-template name="getVariable">
            <xsl:with-param name="id" select="'Copyright'"/>
          </xsl:call-template>
          <xsl:text> </xsl:text><xsl:value-of select="$YEAR"/>
        </xsl:attribute>
      </meta>
    </xsl:if>
    <xsl:next-match/>
  </xsl:template>

</xsl:stylesheet>
