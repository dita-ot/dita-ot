<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

   <xsl:template match="*[contains(@class, ' map/map ')]" mode="replace-content">
     <xsl:choose>
       <xsl:when test="$config-book = 'yes' and $config-chapter-grouping = 'one'">
         <xsl:processing-instruction name="Fm">
           <xsl:text> document "body.fm" </xsl:text>
         </xsl:processing-instruction>
         <fm_topicgroup>
           <xsl:apply-templates select="." mode="process-children"/>
         </fm_topicgroup>
       </xsl:when>
       <xsl:when test="$config-book = 'yes' and $config-chapter-grouping = 'type'">
         <xsl:call-template name="group-bookmap-sections-by-type">
           <xsl:with-param name="class" select="'preface'"/>
         </xsl:call-template>
         <xsl:call-template name="group-bookmap-sections-by-type">
           <xsl:with-param name="class" select="'chapter'"/>
         </xsl:call-template>
         <xsl:call-template name="group-bookmap-sections-by-type">
           <xsl:with-param name="class" select="'appendix'"/>
         </xsl:call-template>
       </xsl:when>
       <xsl:otherwise>
         <xsl:apply-templates select="." mode="process-children"/>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

  <xsl:template name="group-bookmap-sections-by-type">
    <xsl:param name="class"/>
    <xsl:if test="*[contains(@class, ' topic/topic ')][contains(@refclass, concat(' bookmap/', $class, ' '))]">
      <xsl:processing-instruction name="Fm">
        <xsl:text>document "</xsl:text>
        <xsl:value-of select="$class"/>
        <xsl:text>.fm" </xsl:text>
      </xsl:processing-instruction>
      <fm_topicgroup>
        <xsl:for-each select="*[contains(@class, ' topic/topic ')][contains(@refclass, concat(' bookmap/', $class, ' '))]">
          <xsl:apply-templates select="."/>
        </xsl:for-each>
      </fm_topicgroup>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
