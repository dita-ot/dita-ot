<?xml version="1.0" encoding="utf-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2011 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:import href="dita2html-base.xsl"/>
  
  <xsl:output method="xml"
              encoding="UTF-8"
              indent="no"
              doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
              doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>
  
  <!-- Add both lang and xml:lang attributes -->
  <xsl:template match="@xml:lang" name="generate-lang">
    <xsl:param name="lang" select="."/>
    <xsl:attribute name="xml:lang">
      <xsl:value-of select="$lang"/>
    </xsl:attribute>
    <xsl:attribute name="lang">
      <xsl:value-of select="$lang"/>
    </xsl:attribute>
  </xsl:template>
  

</xsl:stylesheet>
