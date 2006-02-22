<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="*[contains(@class,' scheme/subjectScheme ')]">
  <map>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </map>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectdef ')]">
  <topicref>
    <xsl:apply-templates select="@*[local-name()!='locktitle']"/>
    <xsl:apply-templates/>
  </topicref>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/hasNarrower ') or
      contains(@class,' scheme/hasKind ') or
      contains(@class,' scheme/hasPart ') or
      contains(@class,' scheme/hasInstance ')]">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectHead ')]">
  <topichead>
    <xsl:apply-templates select="@*[local-name()!='locktitle']"/>
    <xsl:apply-templates/>
  </topichead>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]">
  <topicmeta>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </topicmeta>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjPrefLabel ') or
      contains(@class,' scheme/subjAltLabel '   ) or
      contains(@class,' scheme/subjHiddenLabel ') or
      contains(@class,' scheme/subjPrefSymbol ' ) or
      contains(@class,' scheme/subjAltSymbol '  )]">
  <othermeta>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </othermeta>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/relatedSubjects ')]">
  <topicgroup>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </topicgroup>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectRelTable ')]">
  <reltable>
    <xsl:apply-templates select="@*[local-name()!='locktitle']"/>
    <xsl:apply-templates/>
  </reltable>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectRelHeader ') or
      contains(@class,' scheme/subjectRel ')]">
  <relrow>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </relrow>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectRole ')]">
  <relcell>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </relcell>
</xsl:template>

</xsl:stylesheet>
