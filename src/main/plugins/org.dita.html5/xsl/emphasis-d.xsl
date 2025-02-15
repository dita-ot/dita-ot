<?xml version="1.0" encoding="UTF-8" ?>
<!-- 
    This file is part of the DITA Open Toolkit project. 
    See the accompanying LICENSE file for applicable license. 
-->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:template match="*[contains(@class,' emphasis-d/strong ')]">
   <strong>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setidaname"/>
    <xsl:apply-templates/>
    </strong>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' emphasis-d/em ')]">
   <em>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setidaname"/>
    <xsl:apply-templates/>
    </em>
  </xsl:template>

</xsl:stylesheet>
