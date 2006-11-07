<?xml version="1.0" encoding="UTF-8" ?>

<!-- An adaptation of the Toolkit topicmerge.xsl for FO plugin use. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
<xsl:import href="../../../../xsl/topicmerge.xsl"/>

<!-- The merged file will contain an opentopic:map element that holds a copy of -->
<!-- the map. -->
<xsl:template match="/*">
   <xsl:element name="{name()}">
     <xsl:apply-templates select="@*" mode="copy-element"/>
     <opentopic:map xmlns:opentopic="http://www.idiominc.com/opentopic">
       <xsl:apply-templates select="*" mode="map-copy-element"/>
     </opentopic:map>
     <xsl:apply-templates select="*"/>
   </xsl:element>
</xsl:template>

<!-- in opentopic:map, topicref/@id gets the ID of the topic it points to -->
<xsl:template match="*[contains(@class, ' map/topicref ')]" 
     mode="map-copy-element">
  <xsl:choose>
    <xsl:when test="@format and not(@format='dita')">
      <!-- Topicref to non-dita files will be ingored in PDF transformation -->
      <xsl:call-template name="output-message">
        <xsl:with-param name="msgnum">049</xsl:with-param>
        <xsl:with-param name="msgsev">I</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="contains(@href, '#')">
       <xsl:variable name="sourcefile">
         <xsl:value-of select="substring-before(@href,'#')"/>
       </xsl:variable>
       <xsl:variable name="sourcetopic">
         <xsl:value-of select="substring-after(@href,'#')"/>
       </xsl:variable>
       <xsl:variable name="new_id">
	 <xsl:value-of select="generate-id(document($sourcefile,/)//*[@id=$sourcetopic][contains(@class,' topic/topic ')][1]/@id)"/>
       </xsl:variable>
       <xsl:call-template name="write-topicref">
         <xsl:with-param name="new_id"><xsl:value-of select="$new_id"/></xsl:with-param>
       </xsl:call-template>
     </xsl:when>
     <xsl:when test="document(@href,/)/*[contains(@class,' topic/topic ')]">
       <!-- If the target is a topic, as opposed to a ditabase mixed file -->
       <xsl:variable name="new_id">
         <xsl:value-of select="generate-id(document(@href,/)/*[contains(@class,' topic/topic ')]/@id)"/>
       </xsl:variable>
       <xsl:call-template name="write-topicref">
         <xsl:with-param name="new_id"><xsl:value-of select="$new_id"/></xsl:with-param>
       </xsl:call-template>
     </xsl:when>
     <xsl:otherwise>
       <!-- a ditabase container. Point to the first topic. -->
       <xsl:variable name="new_id">
         <xsl:value-of select="generate-id(document(@href,/)/*/*[contains(@class,' topic/topic ')][1]/@id)"/>
       </xsl:variable>
       <xsl:call-template name="write-topicref">
         <xsl:with-param name="new_id"><xsl:value-of select="$new_id"/></xsl:with-param>
       </xsl:call-template>
     </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="write-topicref">
  <xsl:param name="new_id"/>
  <xsl:element name="{name()}">
    <xsl:attribute name="id"><xsl:value-of select="$new_id"/></xsl:attribute>
    <xsl:apply-templates select="@*[name() != 'id']" mode="copy-element"/>
    <xsl:apply-templates select="*" mode="map-copy-element"/>
  </xsl:element>
</xsl:template>

<!-- Copy map topicmeta only into the map copy. Drop it from main body. -->
<xsl:template match="*[contains(@class, ' map/topicmeta ')]" 
              mode="map-copy-element">
   <xsl:element name="{name()}">
     <xsl:apply-templates select="@*" mode="copy-element"/>
     <xsl:apply-templates select="*" mode="copy-element"/>
   </xsl:element>
</xsl:template>
<!-- Use increased priority to override original topicmerge's copying. -->
<xsl:template match="*[contains(@class, ' map/topicmeta ')]" priority="2" />

</xsl:stylesheet>
