<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

<xsl:template match="*[contains(@class, ' xml-d/xmlelement ')]">
{\f5 &lt;<xsl:apply-templates/>&gt;}
</xsl:template>

<xsl:template match="*[contains(@class, ' xml-d/xmlatt ')]">
{\f5 @<xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class, ' xml-d/textentity ')]">
{\f5 &amp;<xsl:apply-templates/>;}
</xsl:template>

<xsl:template match="*[contains(@class, ' xml-d/parameterentity ')]">
{\f5 %<xsl:apply-templates/>;}
</xsl:template>

<xsl:template match="*[contains(@class, ' xml-d/numcharref ')]">
{\f5 &amp;#<xsl:apply-templates/>;}
</xsl:template>

<xsl:template match="*[contains(@class, ' xml-d/xmlnsname ')]">
{\f5 <xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class, ' xml-d/xmlpi ')]">
{\f5 <xsl:apply-templates/>
</xsl:template>

</xsl:stylesheet>
