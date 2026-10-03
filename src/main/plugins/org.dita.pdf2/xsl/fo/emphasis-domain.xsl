<?xml version='1.0'?>
<!-- 
    This file is part of the DITA Open Toolkit project. 
    See the accompanying LICENSE file for applicable license. 
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="3.0">

    <xsl:template match="*[contains(@class,' emphasis-d/strong ')]">
        <fo:inline xsl:use-attribute-sets="strong">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' emphasis-d/em ')]">
      <fo:inline xsl:use-attribute-sets="em">
        <xsl:call-template name="commonattributes"/>
        <xsl:apply-templates/>
      </fo:inline>
    </xsl:template>

</xsl:stylesheet>
