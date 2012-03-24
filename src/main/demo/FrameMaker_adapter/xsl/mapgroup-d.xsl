<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template
    match="*[contains(@class, ' mapgroup-d/topichead ')]
    [not(ancestor::*[contains(@class, ' topic/topic ')]
    or ancestor::*[contains(@class, ' mapgroup/topichead ')])]"
    mode="replace-tag">
    <xsl:call-template name="topic-content"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' mapgroup-d/topichead ')]" mode="replace-tag">
    <xsl:call-template name="wrap-fm-pi-begin"/>
    <topic>
      <xsl:apply-templates select="@*[name() != 'id']"/>
      <xsl:apply-templates select="@id"/>
      <title>
        <xsl:apply-templates select="@navtitle" mode="topichead-title"/>
      </title>
      <xsl:apply-templates select="." mode="process-children"/>
    </topic>
    <xsl:call-template name="wrap-fm-pi-end"/>
  </xsl:template>
</xsl:stylesheet>
