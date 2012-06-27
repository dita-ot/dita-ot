<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2006 All Rights Reserved. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>

<xsl:template match="*[contains(@class, ' ui-d/screen ')]">
  <block xml:space="preserve">
    <xsl:apply-templates/>
  </block>
</xsl:template>

<xsl:template match="*[contains(@class, ' ui-d/shortcut ')]">
  <text style="underline"><xsl:apply-templates/></text>
</xsl:template>

<xsl:template match="*[contains(@class, ' ui-d/uicontrol ')]">
  <xsl:if test="parent::*[contains(@class,' ui-d/menucascade ')] and preceding-sibling::*[contains(@class, ' ui-d/uicontrol ')]">
    <xsl:text> -> </xsl:text>
  </xsl:if>
  <text style="bold"><xsl:apply-templates/></text>
</xsl:template>

<!--<xsl:template match="*[contains(@class, ' ui-d/uicontrol ')]" mode="text-only">
  <xsl:if test="parent::*[contains(@class,' ui-d/menucascade ')] and preceding-sibling::*[contains(@class, ' ui-d/uicontrol ')]">
    <xsl:text> -> </xsl:text>
  </xsl:if>
  <xsl:apply-templates select="*|text()" mode="text-only"/>
</xsl:template>-->

</xsl:stylesheet>
