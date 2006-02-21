<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="fm_generalize.xsl"/>

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
    <!-- *** Moldflow-specific *** -->
    <!-- Insert element to count chapter numbers. -->
    <moldflow_chapterhead/>
    <!-- Use Header/Footer $1 marker for chapter title. -->
    <xsl:processing-instruction name="Fm" >
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
            <xsl:with-param name="s" select="string(*[contains(@class, ' topic/title ')][1]/node())"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:processing-instruction>
    <!-- *** End Moldflow-specific code *** -->
    <xsl:if test="self::node()[contains(@class, ' mapgroup-d/topichead ')]">
      <title>
        <xsl:apply-templates select="@navtitle" mode="topichead-title"/>
      </title>
    </xsl:if>
    <xsl:apply-templates select="." mode="process-children"/>
  </topic>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

</xsl:stylesheet>

