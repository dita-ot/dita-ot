<?xml version="1.0" encoding="UTF-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/"
    xmlns:saxon="http://icl.com/saxon"
    xmlns:lxslt="http://xml.apache.org/xslt"
    xmlns:xalanredirect="org.apache.xalan.xslt.extensions.Redirect"
    extension-element-prefixes="saxon xalanredirect lxslt"
	exclude-result-prefixes="ditaarch"
>

<xsl:import href="defaultCopy.xsl"/>

<xsl:output
    method="xml"
    encoding="UTF-8"
    doctype-public="-//OASIS//DTD DITA Concept//EN"
    doctype-system="../../../dtd/concept.dtd"
    indent="no"
/>

<xsl:param name="outext"    select="'.dita'"/>
<xsl:param name="outputdir" select="'temp'"/>

<xsl:template match="/">
  <xsl:variable name="scheme"
      select="*[contains(@class,' scheme/subjectScheme ')]"/>
  <xsl:choose>
  <xsl:when test="$scheme">
    <xsl:apply-templates select="$scheme"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' scheme/subjectScheme ')]">
  <xsl:for-each select=".//*[contains(@class,' scheme/subjectdef ')]">
    <xsl:if test="@href and (not(@format) or @format='dita')">
      <xsl:call-template name="write">
        <xsl:with-param name="filename" select="@href"/>
        <xsl:with-param name="topic">
          <xsl:apply-templates select="document(@href)"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:if>
  </xsl:for-each>
</xsl:template>

<xsl:template match="*[contains(@class,' subject-d/subjectDetail ')]"/>

<xsl:template name="write">
  <xsl:param name="filename"/>
  <xsl:param name="topic"/>
  <xsl:variable name="fullname" select="concat($outputdir,'/',$filename)"/>
  <xsl:choose>
  <xsl:when test="element-available('saxon:output')">
    <saxon:output href="{$fullname}"
        method="xml"
        omit-xml-declaration="no"
        encoding="UTF-8"
        doctype-public="-//IBM//DTD DITA Concept//EN"
        doctype-system="../../../dtd/concept.dtd"
        indent="no">
      <xsl:copy-of select="$topic"/>
    </saxon:output>
  </xsl:when>
  <xsl:when test="element-available('xalanredirect:write')">
    <!-- Xalan preserves relative path to output file,
         so don't prepend output directory -->
    <xalanredirect:write file="{$filename}">
      <xsl:copy-of select="$topic"/>
    </xalanredirect:write>
  </xsl:when>
  <xsl:otherwise>
    <xsl:message terminate="yes">
      <xsl:text>Cannot generate files with XSLT processor from </xsl:text>
      <xsl:value-of select="system-property('xsl:vendor')"/>
    </xsl:message>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
