<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2025 Jason Coleman

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:topicpull="http://dita-ot.sourceforge.net/ns/200704/topicpull"
                exclude-result-prefixes="dita-ot topicpull">
  
  <xsl:key name="count.topic.equations"
    match="*[contains(@class, ' equation-d/equation-block ')][*[contains(@class, ' equation-d/equation-number ')]]"
    use="'include'"/>
  
  <!-- set up keys based on xref's "type" attribute: %info-types;|hd|fig|table|li|fn -->
  <xsl:key name="equation" match="*[contains(@class, ' equation-d/equation-block ')]" use="@id"/> <!-- uses "equation-number"? -->
  
  <xsl:template match="*[contains(@class,' equation-d/equation-inline ')]" name="topic.equation-d.equation-inline">
    <span>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setid"/>
      <xsl:apply-templates/>
    </span>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' equation-d/equation-block ')]" name="topic.equation-d.equation-block">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setid"/>
      <xsl:apply-templates select="*[not(self::*[contains(@class,' equation-d/equation-number ')])] | text()"/>
      <!-- process equation number -->
      <xsl:apply-templates select="*[contains(@class,' equation-d/equation-number ')]"/>
    </div>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' equation-d/equation-number ')]" name="topic.equation-d.equation-number">
    <span>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setid"/>
      <xsl:text>(</xsl:text>
      <xsl:choose>
        <xsl:when test="child::* or (child::text() and not(normalize-space(child::text())=''))">
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <!--<xsl:value-of select="$eqn-count-actual"/>-->
          <xsl:call-template name="compute-number">
            <xsl:with-param name="all">
              <xsl:number from="/*" count="key('count.topic.equations','include')" level="any"/>
            </xsl:with-param>
            <xsl:with-param name="except">
              <xsl:number from="/*" count="key('count.topic.equations','exclude')" level="any"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
      <xsl:text>)</xsl:text>
    </span>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' equation-d/equation-number ')]/text()" name="topic.equation-d.equation-number.text">
    <xsl:value-of select="normalize-space(.)"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' equation-d/equation-figure ')]" name="topic.equation-d.equation-figure">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setid"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
  <!-- Determine the number of the equation being linked to -->
  <xsl:template match="." mode="topicpull:eqnnumber">
    <xsl:call-template name="compute-number">
      <xsl:with-param name="all">
        <xsl:number from="/*" count="key('count.topic.equations','include')" level="any"/>
      </xsl:with-param>
      <xsl:with-param name="except">
        <xsl:number from="/*" count="key('count.topic.equations','exclude')" level="any"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <!-- If a link is to an equation number, assume the parent is the real target, process accordingly -->
  <xsl:template match="*[contains(@class,' equation-d/equation-number ')]" mode="topicpull:resolvelinktext">
    <xsl:apply-templates select=".." mode="#current"/>
  </xsl:template>
  
  <xsl:template name="compute-number">
    <xsl:param name="except"/>
    <xsl:param name="all"/>
    
    <xsl:choose>
      <xsl:when test="$except != ''">
        <xsl:value-of select="number($all) - number($except)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$all"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
