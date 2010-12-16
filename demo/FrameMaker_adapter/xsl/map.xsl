<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

   <xsl:template match="*[contains(@class, ' map/map ')]" mode="replace-content">
     <xsl:choose>
       <xsl:when test="$config-book = 'yes' and ($config-chapter-grouping = 'one' or $config-chapter-grouping = 'type')">
         <xsl:processing-instruction name="Fm">
           <xsl:text> document "body.fm" </xsl:text>
         </xsl:processing-instruction>
         <fm_topicgroup>
           <xsl:apply-templates select="." mode="process-children"/>
         </fm_topicgroup>
       </xsl:when>
       <xsl:otherwise>
         <xsl:apply-templates select="." mode="process-children"/>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="*[contains(@class, ' map/topicmeta ')]" mode="replace-tag">
     <xsl:apply-templates select="*[contains(@class, ' topic/keywords ')]"></xsl:apply-templates>
   </xsl:template>

   <!-- To do: topicmerge destroys sub-topic topicmeta elements.
        re-visit this when topicmerge is fixed so that indexterm/@end is dealt with. -->
   <xsl:template match="*[contains(@class, ' topic/keywords ')]" mode="replace-tag">
     <xsl:apply-templates select="*[contains(@class, ' topic/indexterm ')]"></xsl:apply-templates>
   </xsl:template>
</xsl:stylesheet>
