<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corporation 2010. All Rights Reserved. -->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  exclude-result-prefixes="dita-ot"
  >
  
  <xsl:template match="*[contains(@class, ' ui-d/uicontrol ')]" mode="dita-ot:text-only">
    <xsl:if test="parent::*[contains(@class,' ui-d/menucascade ')] and preceding-sibling::*[contains(@class, ' ui-d/uicontrol ')]">
      <xsl:text> > </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*|text()" mode="dita-ot:text-only"/>
  </xsl:template>

</xsl:stylesheet>
