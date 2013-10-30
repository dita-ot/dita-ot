<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- Get rid of whitespace only nodes -->
  <xsl:strip-space elements="*"/>

  <!-- Note -->
  <xsl:template match="*[contains(@class,' topic/note ')]" name="topic.note">
    <xsl:choose>
      <!-- Note -->
      <xsl:when test="@type='note'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Note'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Tip -->
      <xsl:when test="@type='tip'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Tip'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Fastpath -->
      <xsl:when test="@type='fastpath'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Fastpath'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Important -->
      <xsl:when test="@type='important'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Important'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Remember -->
      <xsl:when test="@type='remember'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Remember'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Restriction -->
      <xsl:when test="@type='restriction'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Restriction'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24</xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Attention -->
      <xsl:when test="@type='attention'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Attention'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Caution -->
      <xsl:when test="@type='caution'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Caution'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Danger -->
      <xsl:when test="@type='danger'">
        <xsl:text>\par \s0\f1\fs24\b </xsl:text>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'Danger'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringRTF">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <!-- Other -->
      <xsl:when test="@type='other'">
        <xsl:text>\par \s0\f1\fs24\b  </xsl:text>
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
        <xsl:text>\pard \s0\f1\fs24 </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
          <xsl:text>\intbl </xsl:text>
        </xsl:if>
        <xsl:apply-templates/>
        <xsl:text>\par \s0\f0\fs24 </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>