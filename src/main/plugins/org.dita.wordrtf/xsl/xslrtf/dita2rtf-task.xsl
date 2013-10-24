<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

<!-- Named templates -->
<xsl:template name="ordered-steps">
<xsl:for-each select="step">
{\pard <xsl:number count="step" level="single" format="1" />) <xsl:apply-templates/>\par}
</xsl:for-each>
</xsl:template>

<!-- Match templates -->
<xsl:template match="*[contains(@class,' task/context ')]">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class, 'task/steps ')]">
{\pard  <xsl:value-of select="stepsection" />\par}
<xsl:call-template name="ordered-steps" />
</xsl:template>

<xsl:template match="*[contains(@class,' task/cmd ')]">
<xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>