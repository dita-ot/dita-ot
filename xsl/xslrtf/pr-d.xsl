<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="text"/>

<xsl:template match="*[contains(@class,' pr-d/codeph ')]">
{\f5 <xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/codeblock ')]">
<xsl:call-template name="gen-id"/><xsl:if test="@spectitle and not(@spectitle='')">\par
  \plain\f4\fs24\b <xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="@spectitle"/></xsl:with-param></xsl:call-template></xsl:if>
\par {\plain\f5\fs24
<xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/var ')]">
{\i <xsl:apply-templates/>}
</xsl:template>

</xsl:stylesheet>
