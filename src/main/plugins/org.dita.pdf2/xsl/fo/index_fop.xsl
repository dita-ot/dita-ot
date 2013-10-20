<?xml version="1.0" encoding="UTF-8"?>
<!--
  This file is part of the DITA Open Toolkit project.
  See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
                version="2.0"
                exclude-result-prefixes="opentopic-index">
  <xsl:variable name="index.continued-enabled" select="false()"/>
  
  <xsl:template match="opentopic-index:index.entry">
    <xsl:if test="opentopic-index:refID/@value">
      <!--Insert simple index entry marker-->
      <xsl:choose>
        <xsl:when test="opentopic-index:index.entry">
          <xsl:for-each select="child::opentopic-index:refID[last()]">
            <fo:wrapper id="{@value}_unique_{generate-id()}"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:for-each select="child::opentopic-index:refID[last()]">
            <fo:wrapper id="{@value}_unique_{generate-id()}"/>
          </xsl:for-each>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="make-index-ref">
    <xsl:param name="idxs" select="()"/>
    <xsl:param name="inner-text" select="()"/>
    <xsl:param name="no-page"/>
    <fo:block id="{generate-id(.)}" xsl:use-attribute-sets="index.term">
      <xsl:if test="empty(preceding-sibling::opentopic-index:index.entry)">
        <xsl:attribute name="keep-with-previous">always</xsl:attribute>
      </xsl:if>
      <fo:inline>
        <xsl:apply-templates select="$inner-text/node()"/>
      </fo:inline>
      <xsl:if test="not($no-page)">
        <xsl:if test="$idxs">
          <xsl:copy-of select="$index.separator"/>
          <xsl:variable name="links" as="element()*">
            <xsl:for-each select="$idxs">
              <xsl:variable name="currentValue" select="@value"/>
              <xsl:for-each select="//opentopic-index:refID[not(ancestor::opentopic-index:index.groups)][@value]">
                <xsl:variable name="value" select="concat(@value, '_unique_', generate-id(.))"/>
                <xsl:variable name="refValue" select="concat($currentValue, '_unique_', generate-id(.))"/>
                <xsl:if test="$value = $refValue">
                  <fo:basic-link internal-destination="{$value}" xsl:use-attribute-sets="common.link">
                    <fo:page-number-citation ref-id="{$value}"/>
                  </fo:basic-link> 
                </xsl:if>
              </xsl:for-each>
            </xsl:for-each>
          </xsl:variable>
          <xsl:for-each select="$links">
            <xsl:if test="not(position() eq 1)">
              <xsl:text>, </xsl:text>
            </xsl:if>
            <xsl:copy-of select="."/>
          </xsl:for-each>
        </xsl:if>
      </xsl:if>
      <xsl:if test="@no-page = 'true'">
        <xsl:apply-templates select="opentopic-index:see-childs" mode="index-postprocess"/>
      </xsl:if>
      <xsl:if test="empty(opentopic-index:index.entry)">
        <xsl:apply-templates select="opentopic-index:see-also-childs" mode="index-postprocess"/>
      </xsl:if>
    </fo:block>
  </xsl:template>
  
</xsl:stylesheet>