<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

  <xsl:template match="*[contains(@class,' hi-d/tt ')]" name="topic.hi-d.tt">
    <span style="font-family: monospace">
      <xsl:call-template name="commonattributes"/>
      <xsl:if test="*[contains(@class,' ditaot-d/ditaval-startprop ')]/@outputclass">
        <!-- Combine TT style with style from ditaval, if present -->
        <xsl:attribute name="style">
          <xsl:text>font-family: monospace; </xsl:text>
          <xsl:value-of select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/@outputclass"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="setidaname"/>
      <xsl:apply-templates/>
    </span>
  </xsl:template>

</xsl:stylesheet>