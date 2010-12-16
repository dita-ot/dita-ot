<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:template
    match="*[contains(@class, ' topic/topic ')][
    not(ancestor::*[contains(@class, ' topic/topic ')]
    or ancestor::*[contains(@class, ' mapgroup-d/topichead ')])
    ]" mode="replace-tag">
    <xsl:call-template name="topic-content"/>
  </xsl:template>

  <xsl:template name="topic-content">
    <xsl:call-template name="wrap-fm-pi-begin"/>
    <xsl:if test="$config-book = 'yes' and $config-chapter-grouping = 'each'">
      <xsl:processing-instruction name="Fm">
        <xsl:choose>
          <!-- If outputclass defined on topic then use this as filename for FrameMaker file in book -->
          <xsl:when test="$config-outputclass-as-filename = 'yes' and @outputclass">
            <xsl:text>document "</xsl:text>
            <xsl:value-of select="@outputclass"/>
            <xsl:text>.fm"</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>document "part</xsl:text>
            <xsl:value-of
              select="count(preceding-sibling::*[contains(@class, ' topic/topic ')
              or contains(@class, ' mapgroup-d/topichead ')]) + 1"/>
            <xsl:text>.fm" </xsl:text>

          </xsl:otherwise>
        </xsl:choose>

      </xsl:processing-instruction>
    </xsl:if>
    <topic>
      <xsl:apply-templates select="@*[name() != 'id']"/>
      <xsl:apply-templates select="@id"/>

      <!-- Insert element to count chapter numbers. -->
      <xsl:if test="$config-chapter-head = 'yes'">
        <chapter-head/>
      </xsl:if>
      <!-- Use Header/Footer $1 marker for chapter title. -->
      <xsl:processing-instruction name="Fm">
        <xsl:text>MARKER [Header/Footer $1] </xsl:text>
        <!-- This kills formatting in the footer.  Probably can't be helped with the processing instruction approach. -->
        <xsl:choose>
          <xsl:when test="self::node()[contains(@class, ' mapgroup-d/topichead ')]">
            <xsl:call-template name="translate-string-in-pi">
              <xsl:with-param name="s" select="string(@navtitle)"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="translate-string-in-pi">
              <xsl:with-param name="s"
                select="string(*[contains(@class, ' topic/title ')][1]/node())"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:processing-instruction>
      <xsl:if test="self::node()[contains(@class, ' mapgroup-d/topichead ')]">
        <title>
          <xsl:apply-templates select="@navtitle" mode="topichead-title"/>
        </title>
      </xsl:if>
      <xsl:apply-templates select="." mode="process-children"/>
    </topic>
    <xsl:call-template name="wrap-fm-pi-end"/>

  </xsl:template>

  <xsl:template match="@navtitle" mode="topichead-title">
    <xsl:call-template name="translate-string"/>
  </xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2005. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->
