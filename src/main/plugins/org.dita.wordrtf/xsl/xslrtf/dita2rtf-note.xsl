<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- Get rid of whitespace only nodes -->
  <xsl:strip-space elements="*"/>

  <xsl:template match="*[contains(@class,' topic/note ')]" name="topic.note">
    <xsl:choose>
      <!-- Generic solution for all defined note types -->
      <xsl:when test="not(@type='other')">
        <xsl:text>{\pard </xsl:text>
        <xsl:variable name="mytype">
          <xsl:call-template name="firstUpperCase" />
        </xsl:variable>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="$mytype"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par}</xsl:text>
      </xsl:when>

      <!-- Other -->
      <xsl:when test="@type='other'">
        <xsl:text>{\pard </xsl:text>
        <xsl:choose>
          <xsl:when test="@othertype and not(@othertype='')">
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="@othertype"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>[other]</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par}</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Capitalize first character of note type.
(If keys for translation functions were lower case by default this function wouldn't be needed.)
-->
  <xsl:template name="firstUpperCase">
    <xsl:variable name="note_type" >
      <xsl:value-of select="@type" />
    </xsl:variable>
    <xsl:value-of select="concat(translate(substring($note_type, 1, 1),
      'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'),
      substring($note_type, 2))"
    />
  </xsl:template>

</xsl:stylesheet>