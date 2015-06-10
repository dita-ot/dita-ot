<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0" >

  <!-- Deprecated: Write output directly instead -->
  <xsl:template name="get-ascii">
    <xsl:param name="txt"/>
    <xsl:value-of select="$txt"/>
  </xsl:template>

  <xsl:template name="getStringRTF">
    <xsl:param name="stringName"/>
    <xsl:call-template name="getVariable">
      <xsl:with-param name="id" select="$stringName"/>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>