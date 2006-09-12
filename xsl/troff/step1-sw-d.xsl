<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2006 All Rights Reserved. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>

<xsl:template match="*[contains(@class,' sw-d/msgph ')]">
  <text style="tt"><xsl:apply-templates/></text>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/systemoutput ')]">
  <text style="tt"><xsl:apply-templates/></text>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/userinput ')]">
  <text style="tt"><xsl:apply-templates/></text>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/varname ')]">
  <text style="italics"><xsl:apply-templates/></text>
</xsl:template>

</xsl:stylesheet>
