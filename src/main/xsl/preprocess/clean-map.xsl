<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                version="2.0"
                exclude-result-prefixes="xs">
  
  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:import href="../common/output-message.xsl"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' ditaot-d/submap ')]">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' ditaot-d/keydef ')]"/>
  
</xsl:stylesheet>