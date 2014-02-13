<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="text"/>

  <xsl:template match="*[contains(@class,' pr-d/codeph ')]">
    <xsl:text>{\f2 </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>}</xsl:text>
  </xsl:template>

  <xsl:template match="*[contains(@class,' pr-d/codeblock ')]">
    <xsl:call-template name="gen-id"/>
    <xsl:if test="@spectitle and not(@spectitle='')">
      <xsl:text>{\pard \f1\b </xsl:text>
      <xsl:call-template name="get-ascii">
        <xsl:with-param name="txt">
          <xsl:value-of select="@spectitle"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:text>\par}</xsl:text>
    </xsl:if>
    <xsl:text>{\pard \f2 </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>\par}</xsl:text>
  </xsl:template>

  <xsl:template match="*[contains(@class,' pr-d/var ')]">
{\i <xsl:apply-templates/>}
  </xsl:template>

</xsl:stylesheet>
