<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<xsl:param name="ACTION" select="'CONVERT'"/>
<xsl:variable name="ActionParam" select="translate($ACTION,
        'abcdefghijklmnopqrstuvwxyz',
        'ABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>

<xsl:template match="*[contains(@class,' classify-d/topicsubject ') or
      contains(@class,' classify-d/subjectref ')]">
  <xsl:choose>
  <xsl:when test="$ActionParam='CONVERT'">
    <topicref>
      <xsl:apply-templates select="@*[local-name()!='locktitle']"/>
      <xsl:apply-templates/>
    </topicref>
  </xsl:when>
  <xsl:when test="$ActionParam='HIDE'"/>
  <xsl:otherwise>
    <xsl:message>
      <xsl:text>Unknown ACTION parameter </xsl:text>
      <xsl:value-of select="$ActionParam"/>
    </xsl:message>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' classify-d/topicSubjectTable ')]">
  <xsl:choose>
  <xsl:when test="$ActionParam='CONVERT'">
    <reltable>
      <xsl:apply-templates select="@*[local-name()!='locktitle']"/>
      <xsl:apply-templates/>
    </reltable>
  </xsl:when>
  <xsl:when test="$ActionParam='HIDE'"/>
  <xsl:otherwise>
    <xsl:message>
      <xsl:text>Unknown ACTION parameter </xsl:text>
      <xsl:value-of select="$ActionParam"/>
    </xsl:message>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' classify-d/topicSubjectHeader ') or
      contains(@class,' classify-d/topicSubjectRow ')]">
  <relrow>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </relrow>
</xsl:template>

<xsl:template match="*[contains(@class,' classify-d/topicCell ') or
      contains(@class,' classify-d/subjectCell ')]">
  <relcell>
    <xsl:apply-templates select="@*"/>
    <xsl:apply-templates/>
  </relcell>
</xsl:template>

</xsl:stylesheet>
