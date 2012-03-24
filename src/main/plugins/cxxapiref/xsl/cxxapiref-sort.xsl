<?xml version="1.0" encoding="UTF-8"?>
<!--
  (C) Copyright Nokia Corporation and/or its subsidiary(-ies) 2009  - 2010. All rights reserved.
 *-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:import href="../../../xsl/common/output-message.xsl"/>
  <xsl:import href="../../../xsl/common/dita-utilities.xsl"/>

  <xsl:variable name="msgprefix">CXXAPIREFTX</xsl:variable>

  <xsl:template match="*[contains(@class, ' map/topicref ')]
                        [*[contains(@class, ' cxxAPIMap/cxxClassRef ') or
                           contains(@class, ' cxxAPIMap/cxxStructRef ') or
                           contains(@class, ' cxxAPIMap/cxxUnionRef ') or
                           contains(@class, ' cxxAPIMap/cxxFileRef ')]]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*[not(contains(@class, ' map/topicref '))]"/>
      <xsl:processing-instruction name="cxxapiref">sort</xsl:processing-instruction>
      <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
        <xsl:sort select="translate(normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]),
                                   'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
                                   'abcdefghijklmnopqrstuvwxyz')"/>
        <xsl:sort select="translate(normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]),
                                   'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
                                   'abcdefghijklmnopqrstuvwxyz')"/>                  
        <xsl:sort select="translate(normalize-space(@navtitle),
                                   'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
                                   'abcdefghijklmnopqrstuvwxyz')"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
