<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Count occurrences of "tm" for each trademark, so FrameMaker can label just the first one. -->
<xsl:template match="*[contains(@class, ' topic/tm ')]" mode="replace-tag">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <tm>
    <xsl:attribute name="previous-count">
      <xsl:value-of select="count(preceding::*[contains(@class, ' topic/tm ')][normalize-space(.) = normalize-space(current())])"/>
    </xsl:attribute>
    <xsl:apply-templates select="@*[name() != 'id']"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="." mode="process-children"/>
  </tm>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

</xsl:stylesheet>
