<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Index markers in FrameMaker are "Level1:Level2:Level3". -->
<xsl:template match="*[contains(@class, ' topic/indexterm ')]"  mode="reorder-terms">
  <xsl:variable name="outer">
    <xsl:apply-templates select="node()[not(contains(@class, ' topic/indexterm '))]"/>
  </xsl:variable>
  <xsl:call-template name="translate-string-in-pi">
    <!-- This approach kills formatting in the indexterm.  Do we care? -->
    <xsl:with-param name="s" select="string($outer)"/>
  </xsl:call-template>
  <xsl:if test="*[contains(@class, ' topic/indexterm ')]">
    <xsl:text>:</xsl:text>
    <xsl:apply-templates select="*[contains(@class, ' topic/indexterm ')]" mode="reorder-terms"/>
  </xsl:if>
</xsl:template>

<!-- Do index terms as FrameMaker markers, not elements. -->
<xsl:template match="*[contains(@class, ' topic/indexterm ')]" mode="replace-tag">
  <xsl:processing-instruction name="Fm">
    <xsl:text>MARKER [Index] </xsl:text>
    <xsl:apply-templates select="." mode="reorder-terms"/>
  </xsl:processing-instruction>
</xsl:template>

</xsl:stylesheet>
