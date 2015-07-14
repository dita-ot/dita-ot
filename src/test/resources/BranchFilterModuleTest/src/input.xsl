<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:x="x"
  exclude-result-prefixes="xs x"
  version="2.0">
  
  <xsl:output indent="yes"/>
  
  <xsl:strip-space elements="*"/>
  
  <xsl:template match="*[ditavalref]">
    <xsl:param name="resourcePrefix" tunnel="yes"/>
    <xsl:param name="resourceSuffix" tunnel="yes"/>
    <xsl:param name="keynamePrefix" tunnel="yes"/>
    <xsl:param name="keynameSuffix" tunnel="yes"/>
    <xsl:variable name="parent" select="."/>
    <xsl:for-each select="ditavalref">
      <xsl:variable name="ditavalref" select="."/>
      <xsl:variable name="current-resourcePrefix" select="concat($resourcePrefix, ditavalmeta/dvr-resourcePrefix)"/>
      <xsl:variable name="current-resourceSuffix" select="concat($resourceSuffix, ditavalmeta/dvr-resourceSuffix)"/>
      <xsl:for-each select="$parent">
        <xsl:copy>
          <xsl:if test="string($current-resourcePrefix) or string($current-resourceSuffix)">
            <xsl:attribute name="copy-to" select="x:addSuffix($parent/@href, $current-resourcePrefix, $current-resourceSuffix)"/>
          </xsl:if>
          <xsl:apply-templates select="@* | node()[(self::ditavalref and . is $ditavalref) or not(self::ditavalref)]">
            <xsl:with-param name="resourcePrefix" select="$current-resourcePrefix" tunnel="yes"/>
            <xsl:with-param name="resourceSuffix" select="$current-resourceSuffix" tunnel="yes"/>
            <xsl:with-param name="keynamePrefix" select="concat($keynamePrefix, ditavalmeta/dvr-keynamePrefix)" tunnel="yes"/>
            <xsl:with-param name="keynameSuffix" select="concat($keynameSuffix, ditavalmeta/dvr-keynameSuffix)" tunnel="yes"/>
          </xsl:apply-templates>
        </xsl:copy>  
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:function name="x:addSuffix">
    <xsl:param name="href"/>
    <xsl:param name="prefix"/>
    <xsl:param name="suffix"/>
    <xsl:variable name="suffixTokens" select="tokenize($href, '\.')"/>
    <xsl:variable name="buf"
                  select="concat(string-join($suffixTokens[position() lt count($suffixTokens)], '.'),
                                 $suffix,
                                 '.',
                                 $suffixTokens[last()])"/>
    <xsl:variable name="prefixTokens" select="tokenize($buf, '/')"/>
    <xsl:for-each select="$prefixTokens[position() ne last()]">
      <xsl:value-of select="."/>
      <xsl:text>/</xsl:text>
    </xsl:for-each>
    <xsl:value-of select="$prefixTokens[last()]"/>
  </xsl:function>
    
  <xsl:template match="@keys">
    <xsl:param name="keynamePrefix" tunnel="yes"/>
    <xsl:param name="keynameSuffix" tunnel="yes"/>
    <xsl:attribute name="{local-name()}">
      <xsl:for-each select="tokenize(normalize-space(.), '\s+')">
        <xsl:if test="position() ne 1">
          <xsl:text> </xsl:text>
        </xsl:if>
        <xsl:value-of select="concat($keynamePrefix, ., $keynameSuffix)"/>
      </xsl:for-each>
    </xsl:attribute>
  </xsl:template>
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>