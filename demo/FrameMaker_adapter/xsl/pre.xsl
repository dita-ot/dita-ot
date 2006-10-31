<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Space inside "pre" and similar elements is preserved. -->
<xsl:template match="*[contains(@class, ' topic/pre ')]//text()">
  <xsl:call-template name="newline-to-entity">
    <xsl:with-param name="s">
      <!-- space into &#xA0; non-breaking space.
           hyphen into &#x2011; non-breaking hyphen. -->
      <xsl:value-of select="translate(.,' -','&#xA0;&#x2011;')"/>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- "lines" is treated as normal except that newlines are significant. -->
<xsl:template match="*[contains(@class, ' topic/lines ')]//text()">
  <xsl:call-template name="newline-to-entity">
    <xsl:with-param name="s">
      <xsl:value-of select="."/>
    </xsl:with-param>
  </xsl:call-template>
</xsl:template>

<!-- Output "hardreturn" entity (defined in the DTD to be an empty string)
     where newlines are found.  FrameMaker read/write rules do the rest. -->
<xsl:template name="newline-to-entity">
  <xsl:param name="s"/>
  <xsl:choose>
    <xsl:when test="contains($s, '&#x0a;')">
      <xsl:value-of select="substring-before($s, '&#x0a;')"/>
      <xsl:text disable-output-escaping="yes">&amp;hardreturn;</xsl:text>
      <xsl:call-template name="newline-to-entity">
        <xsl:with-param name="s" select="substring-after($s, '&#x0a;')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$s"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
