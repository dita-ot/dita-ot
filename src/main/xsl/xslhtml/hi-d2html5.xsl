<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="*[contains(@class,' hi-d/tt ')]" name="topic.hi-d.tt">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <span style="font-family: monospace">
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setidaname"/>
      <xsl:call-template name="flagcheck"/>
      <xsl:call-template name="revtext">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
    </span>
  </xsl:template>

</xsl:stylesheet>