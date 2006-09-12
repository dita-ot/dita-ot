<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<!-- Screen -->
<xsl:template match="*[contains(@class,' ui-d/screen ')]">
{\plain \cb2\f5\fs24 <xsl:apply-templates/>}
</xsl:template>

<!-- ui-domain.ent domain: uicontrol | wintitle | menucascade | shortcut -->

<xsl:template match="*[contains(@class,' ui-d/uicontrol ')]">
<!-- insert an arrow with leading/trailing spaces before all but the first uicontrol in a menucascade -->
<xsl:if test="ancestor::*[contains(@class,' ui-d/menucascade ')]">
 <xsl:variable name="uicontrolcount"><xsl:number count="*[contains(@class,' ui-d/uicontrol ')]"/></xsl:variable>
  <xsl:if test="$uicontrolcount&gt;'1'">
    <xsl:text> > </xsl:text>
  </xsl:if>
</xsl:if>
{\b <xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/wintitle ')]">
{\f4\fs24\b <xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/shortcut ')]" name="topic.ui-d.shortcut">
{\ul <xsl:apply-templates/>}
</xsl:template>

</xsl:stylesheet>
