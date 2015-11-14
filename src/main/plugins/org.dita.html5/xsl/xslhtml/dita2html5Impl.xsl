<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                version="2.0"
                exclude-result-prefixes="xs dita-ot">

  <xsl:variable name="newline" select="()" as="xs:string?"/>

  <xsl:template name="generateCharset">
    <meta charset="UTF-8"/>
  </xsl:template>  
  
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
    <xsl:param name="previous" select="/.."/>
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
  
  <xsl:template match="*[contains(@class,' topic/title ')]" mode="gen-metadata"/>
  <xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="gen-metadata">
    <xsl:variable name="shortmeta">
      <xsl:apply-templates select="*|text()" mode="text-only"/>
    </xsl:variable>
    <meta name="description">
      <xsl:attribute name="content">
        <xsl:value-of select="normalize-space($shortmeta)"/>
      </xsl:attribute>
    </meta>
  </xsl:template>
  
  <!-- Notes -->
  
  <xsl:template match="*" mode="process.note.common-processing">
    <xsl:param name="type" select="@type"/>
    <xsl:param name="title">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="concat(upper-case(substring($type, 1, 1)), substring($type, 2))"/>
      </xsl:call-template>
    </xsl:param>
    <div>
      <xsl:call-template name="commonattributes">
        <xsl:with-param name="default-output-class" select="string-join(($type, concat('note_', $type)), ' ')"/>
      </xsl:call-template>
      <xsl:call-template name="setidaname"/>
      <!-- Normal flags go before the generated title; revision flags only go on the content. -->
      <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]/prop" mode="ditaval-outputflag"/>
      <!-- Deprecated since 2.2: {$type}title is for backwards compatibility -->
      <span class="note__title {$type}title">
        <xsl:copy-of select="$title"/>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
      </span>
      <xsl:text> </xsl:text>
      <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]/revprop" mode="ditaval-outputflag"/>
      <xsl:apply-templates/>
      <!-- Normal end flags and revision end flags both go out after the content. -->
      <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
    </div>
  </xsl:template>
  
  <xsl:template match="*" mode="process.note.caution">
    <xsl:apply-templates select="." mode="process.note.common-processing"/>
  </xsl:template>
  
  <xsl:template match="*" mode="process.note.danger">
    <xsl:apply-templates select="." mode="process.note.common-processing"/>
  </xsl:template>
  
  <xsl:include href="tables.xsl"/>
  <xsl:include href="nav.xsl"/>
  
</xsl:stylesheet>