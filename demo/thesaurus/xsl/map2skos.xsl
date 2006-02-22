<?xml version="1.0"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/">

<xsl:output method="xml"/>


<!-- Match a topicref with subjects -->
<xsl:template match="*[contains(@class,' map/topicref ')][@href]">
  <xsl:if test="*[contains(@class,' classify-d/topicsubject ')] or
                ancestor::*[contains(@class,' classify-d/topicSubjectRow ')]/*[position()>1]/*">
    <xsl:variable name="contentID">
      <xsl:apply-templates select="." mode="contentID"/>
    </xsl:variable>
    <foaf:Document rdf:about="{$contentID}">
      <xsl:call-template name="setLanguage"/>
      <xsl:apply-templates select="*[contains(@class,' classify-d/topicsubject ')]" />
   
      <xsl:apply-templates select="ancestor::*[contains(@class,' classify-d/topicSubjectRow ')]/*[position()>1]/*"/>
    </foaf:Document><xsl:text>
</xsl:text>
  </xsl:if>

  <xsl:apply-templates select="*[not(contains(@class,' classify-d/topicsubject ') or contains(@class,' map/topicmeta '))]"/>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" mode="contentID">
  <xsl:variable name="outputfile">
    <xsl:apply-templates select="." mode="outputfile"/>
  </xsl:variable>
  <xsl:choose>
  <xsl:when test="not($outputfile)"/>
  <xsl:otherwise>
    <xsl:variable name="protocol"
        select="translate(substring-before($outputfile,'://'),
            'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
            'abcdefghijklmnopqrstuvwxyz')"/>
    <xsl:if test="(not($protocol) or $protocol='') and
        substring($outputfile,1,1)!='/'">
      <xsl:value-of select="$CONTENTBASE"/>
    </xsl:if>
    <xsl:value-of select="$outputfile"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' map/topicref ')]" mode="outputfile">
  <xsl:choose>
  <xsl:when test="not(@href)"/>
  <xsl:when test="not(@format) or @format='dita'">
    <xsl:value-of select="substring-before(@href,$DITAEXT)"/>
    <xsl:text>.html</xsl:text>
  </xsl:when>
  <xsl:when test="@format='html' or @format='pdf'">
    <xsl:value-of select="@href"/>
  </xsl:when>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
