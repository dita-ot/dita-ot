<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">



<!-- Do index terms as FrameMaker markers, not elements. -->
<!-- Index markers in FrameMaker are "Level1:Level2:Level3". -->

<!-- Once the current node is not an indexterm, spit out the assembled string as
     a processing instruction. -->
<xsl:template match="node()[not(contains(@class, ' topic/indexterm '))]" mode="indexterm-leaf">
  <xsl:param name="inner"/>
  <xsl:processing-instruction name="Fm">
    <xsl:text>MARKER [Index] </xsl:text>
    <xsl:value-of select="$inner"/>
  </xsl:processing-instruction>
</xsl:template>

<!-- Prepend this indexterm onto child indexterms as we leave recursion. -->
<xsl:template match="*[contains(@class, ' topic/indexterm ')]" mode="indexterm-leaf">
  <xsl:param name="inner"/>
  <xsl:variable name="this">
    <!-- This loses formatting.  Do we care? -->
    <xsl:call-template name="translate-string-in-pi">
      <xsl:with-param name="s" select="node()[not(contains(@class, ' topic/indexterm '))]"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:apply-templates select=".." mode="indexterm-leaf">
    <xsl:with-param name="inner">
      <xsl:value-of select="$this"/>
      <xsl:if test="string-length($inner) &gt; 0">
        <!-- Append inner indexterm, if it exists. -->
        <xsl:text>:</xsl:text>
        <xsl:value-of select="$inner"/>
      </xsl:if>
    </xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

<!-- Indexterms can nest one-to-many.  Cater for this by drilling to leaf node and
     doing work on the way back up. -->
<xsl:template match="*[contains(@class, ' topic/indexterm ')]" mode="replace-tag">
  <xsl:choose>
    <xsl:when test="*[contains(@class, ' topic/indexterm ')]">
      <xsl:apply-templates select="*[contains(@class, ' topic/indexterm ')]" mode="replace-tag"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="." mode="indexterm-leaf"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
