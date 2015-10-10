<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                exclude-result-prefixes="xs">

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

  <!-- Tables -->
  
  <xsl:template name="dotable">
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
    <xsl:call-template name="setaname"/>
    <table>
      <xsl:call-template name="setid"/>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates select="." mode="generate-table-summary-attribute"/>
      <xsl:call-template name="setscale"/>
      <xsl:call-template name="place-tbl-lbl"/>
      <!-- title and desc are processed elsewhere -->
      <xsl:apply-templates select="*[contains(@class, ' topic/tgroup ')]"/>
    </table>
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/table ')]" mode="get-output-class">
    <xsl:variable name="colsep" select="(*[contains(@class, ' topic/tgroup ')]/@colsep, @colsep)[1]" as="xs:string?"/>
    <xsl:variable name="rowsep" select="(*[contains(@class, ' topic/tgroup ')]/@rowsep, @rowsep)[1]" as="xs:string?"/>
    <xsl:variable name="classes" as="xs:string*">
      <!-- When a table's width is set to page or column, force it's width to 100%. If it's in a list, use 90%.
       Otherwise, the table flows to the content -->
      <xsl:variable name="in-list" select="exists((ancestor::*[contains(@class, ' topic/li ')] or ancestor::*[contains(@class, ' topic/dd ')]))"/>
      <xsl:choose>
        <xsl:when test="(@expanse = 'page' or @pgwide = '1') and $in-list">width-90</xsl:when>
        <xsl:when test="(@expanse = 'column' or @pgwide = '0') and $in-list">width-90</xsl:when>
        <xsl:when test="(@expanse = 'page' or @pgwide = '1')">width-100</xsl:when>
        <xsl:when test="(@expanse = 'column' or @pgwide = '0')">width-100</xsl:when>
      </xsl:choose>
      <xsl:value-of>
        <xsl:text>table-rules-</xsl:text>
        <xsl:choose>
          <xsl:when test="@frame = 'all' and $colsep = '0' and $rowsep = '0'"/>
          <xsl:when test="not(@frame) and $colsep = '0' and $rowsep = '0'"/>
          <xsl:when test="$colsep = '0' and $rowsep = '0'">none</xsl:when>
          <xsl:when test="$colsep = '0'">rows</xsl:when>
          <xsl:when test="$rowsep = '0'">cols</xsl:when>
          <xsl:otherwise>all</xsl:otherwise>
        </xsl:choose>
      </xsl:value-of>
      <xsl:value-of>
        <xsl:text>table-frame-</xsl:text>
        <xsl:choose>
          <xsl:when test="@frame = 'all' and $colsep = '0' and $rowsep = '0'"/>
          <xsl:when test="not(@frame) and $colsep = '0' and $rowsep = '0'"/>
          <xsl:when test="exists(@frame)">
            <xsl:value-of select="@frame"/>
          </xsl:when>
          <xsl:otherwise>border</xsl:otherwise>
        </xsl:choose>
      </xsl:value-of>
      <xsl:choose>
        <xsl:when test="@frame = 'all' and $colsep = '0' and $rowsep = '0'"/>
        <xsl:when test="not(@frame) and $colsep = '0' and $rowsep = '0'"/>
        <xsl:when test="@frame = 'none'"/>
        <xsl:otherwise>table-border</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="string-join($classes, ' ')"/>
  </xsl:template>
  
</xsl:stylesheet>