<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2006 All Rights Reserved. -->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                >

<xsl:template match="*[contains(@class, ' pr-d/syntaxdiagram ')]">
  <!-- Needs to be implemented. Could import the textual syntax diagram code for XHTML
       and include it as a pre-formatted block. -->
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/codeph ')]">
  <text style="tt"><xsl:call-template name="debug"/><xsl:apply-templates/></text>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/parmname ')]">
  <xsl:choose>
    <xsl:when test="@importance='default'">
      <text style="bold"><text style="underlined"><xsl:call-template name="debug"/><xsl:apply-templates/></text></text>
    </xsl:when>
    <xsl:otherwise>
      <text style="bold"><xsl:call-template name="debug"/><xsl:apply-templates/></text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/var ')]">
  <xsl:choose>
    <xsl:when test="@importance='default'">
      <text style="italic"><text style="underlined"><xsl:call-template name="debug"/><xsl:apply-templates/></text></text>
    </xsl:when>
    <xsl:otherwise>
      <text style="italic"><xsl:call-template name="debug"/><xsl:apply-templates/></text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
