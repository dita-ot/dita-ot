<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="*[contains(@class, ' topic/topic ')][
                                                          contains(@refclass, ' bookmap/preface ')
                                                       or contains(@refclass, ' bookmap/chapter ')
                                                       or contains(@refclass, ' bookmap/appendix ')
                                                       or not(ancestor::*[contains(@class, ' topic/topic ')]
                                                           or ancestor::*[contains(@class, ' mapgroup-d/topichead ')])
                                                         ]
                   | *[contains(@class, ' mapgroup-d/topichead ')][not(ancestor::*[contains(@class, ' topic/topic ')]
                                                                    or ancestor::*[contains(@class, ' mapgroup/topichead ')])]" mode="replace-tag" priority="20">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <xsl:if test="$config-book = 'yes' and $config-chapter-grouping = 'each'">
    <xsl:processing-instruction name="Fm">
      <xsl:choose>
        <!-- If outputclass defined on topic then use this as filename for FrameMaker file in book -->
        <xsl:when test="@outputclass">
          <xsl:text>document "</xsl:text>
          <xsl:value-of select="@outputclass"/>
          <xsl:text>.fm"</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>document "part</xsl:text>
          <xsl:value-of select="count(preceding-sibling::*[contains(@class, ' topic/topic ')
            or contains(@class, ' mapgroup-d/topichead ')]) + 1"/>
          <xsl:text>.fm" </xsl:text>
          
        </xsl:otherwise>
      </xsl:choose>
      
    </xsl:processing-instruction>
  </xsl:if>
  <topic>
    <xsl:apply-templates select="@*[name() != 'id']"/>
    <xsl:apply-templates select="@id"/>
    <xsl:if test="self::node()[contains(@class, ' mapgroup-d/topichead ')]">
      <title>
        <xsl:apply-templates select="@navtitle" mode="topichead-title"/>
      </title>
    </xsl:if>
    <xsl:apply-templates select="." mode="process-children"/>
  </topic>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<xsl:template match="*[contains(@class, ' mapgroup-d/topichead ')]" mode="replace-tag" priority="10">
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

<xsl:template match="@navtitle" mode="topichead-title">
  <xsl:call-template name="translate-string" />
</xsl:template>

</xsl:stylesheet>
