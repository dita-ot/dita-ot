<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                version="2.0"
                exclude-result-prefixes="xs dita-ot">

  <xsl:import href="plugin:org.dita.xhtml:xsl/map2htmtoc/map2htmlImpl.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-textonly.xsl"/>

  <xsl:param name="CSS"/>
  <xsl:param name="CSSPATH"/>
  <xsl:param name="PATH2PROJ">
    <xsl:apply-templates select="/processing-instruction('path2project-uri')[1]" mode="get-path2project"/>
  </xsl:param>
  <xsl:param name="genDefMeta" select="'no'"/>
  <xsl:param name="YEAR" select="format-date(current-date(), '[Y]')"/>

  <xsl:template match="/">
    <xsl:variable name="title" as="node()*">
      <xsl:call-template name="generateMapTitle"/>
    </xsl:variable>
    <html>
      <head>
        <xsl:call-template name="generateCharset"/>
        <xsl:call-template name="generateDefaultCopyright"/>
        <xsl:call-template name="generateDefaultMeta"/>
        <xsl:call-template name="copyright"/>
        <xsl:call-template name="generateCssLinks"/>
        <xsl:if test="exists($title)">
          <title>
            <xsl:copy-of select="$title"/>
          </title>
        </xsl:if>
        <xsl:call-template name="gen-user-head"/>
        <xsl:call-template name="gen-user-scripts"/>
        <xsl:call-template name="gen-user-styles"/>
      </head>
      <body>
        <xsl:call-template name="commonattributes"/>
        <xsl:if test="exists($title)">
          <h1 class="title topictitle1">
            <xsl:copy-of select="$title"/>
          </h1>
        </xsl:if>
        <xsl:apply-templates select="*" mode="toc"/>
      </body>
    </html>
  </xsl:template>

  <xsl:template name="generateMapTitle">
    <xsl:if test="/*[contains(@class,' map/map ')]/*[contains(@class,' topic/title ')] or /*[contains(@class,' map/map ')]/@title">
      <xsl:call-template name="gen-user-panel-title-pfx"/>
      <xsl:choose>
        <xsl:when test="/*[contains(@class,' map/map ')]/*[contains(@class,' topic/title ')]">
          <xsl:value-of select="normalize-space(/*[contains(@class,' map/map ')]/*[contains(@class,' topic/title ')])"/>
        </xsl:when>
        <xsl:when test="/*[contains(@class,' map/map ')]/@title">
          <xsl:value-of select="/*[contains(@class,' map/map ')]/@title"/>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template name="gen-user-panel-title-pfx">
    <xsl:apply-templates select="." mode="gen-user-panel-title-pfx"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-panel-title-pfx">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- It will be placed immediately after TITLE tag, in the title -->
  </xsl:template>

  <xsl:function name="dita-ot:is-external" as="xs:boolean">
    <xsl:param name="urltext" as="xs:string"/>
    <xsl:sequence select="starts-with($urltext, 'http://') or
                          starts-with($urltext, 'https://') or
                          starts-with($urltext, '//')"/>
  </xsl:function>

  <!-- To be overridden by user shell. -->

  <xsl:template name="gen-user-head">
    <xsl:apply-templates select="." mode="gen-user-head"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-head">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- it will be placed in the HEAD section of the XHTML. -->
  </xsl:template>

  <xsl:template name="gen-user-header">
    <xsl:apply-templates select="." mode="gen-user-header"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-header">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- it will be placed in the running heading section of the XHTML. -->
  </xsl:template>

  <xsl:template name="gen-user-footer">
    <xsl:apply-templates select="." mode="gen-user-footer"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-footer">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- it will be placed in the running footing section of the XHTML. -->
  </xsl:template>

  <xsl:template name="gen-user-sidetoc">
    <xsl:apply-templates select="." mode="gen-user-sidetoc"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-sidetoc">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- Uncomment the line below to have a "freebie" table of contents on the top-right -->
  </xsl:template>

  <xsl:template name="gen-user-scripts">
    <xsl:apply-templates select="." mode="gen-user-scripts"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-scripts">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- It will be placed before the ending HEAD tag -->
    <!-- see (or enable) the named template "script-sample" for an example -->
  </xsl:template>

  <xsl:template name="gen-user-styles">
    <xsl:apply-templates select="." mode="gen-user-styles"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-styles">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- It will be placed before the ending HEAD tag -->
  </xsl:template>

  <xsl:template name="gen-user-external-link">
    <xsl:apply-templates select="." mode="gen-user-external-link"/>
  </xsl:template>
  <xsl:template match="/|node()|@*" mode="gen-user-external-link">
    <!-- to customize: copy this to your override transform, add the content you want. -->
    <!-- It will be placed after an external LINK or XREF -->
  </xsl:template>

  <dita:extension id="dita.xsl.html.cover" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>

</xsl:stylesheet>
