<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2006 All Rights Reserved. -->

<!-- 
     This file defines an override for a transform that converts from
     DITA to the IBMIDDoc SGML DTD. This is not used by the DITA Open Toolkit. 
-->
<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- XHTML output with XML syntax -->
<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- == REFERENCE UNIQUE SUBSTRUCTURES == -->
<xsl:template name="musicGetString">
  <xsl:param name="stringName"/>
  <xsl:call-template name="getString">
    <xsl:with-param name="stringFileList">../../demo/music/xsl/musicstrings.xml</xsl:with-param>
    <xsl:with-param name="stringName" select="$stringName"/>
  </xsl:call-template>
</xsl:template>


<xsl:template match="*[contains(@class,' musicCollection/cdList ')]">
  <xsl:variable name="cols">
    <xsl:choose>
      <xsl:when test="*/*[contains(@class,' musicCollection/band ') or contains(@class,' musicCollection/bandHeader ')] and
                      */*[contains(@class,' musicCollection/albums ') or contains(@class,' musicCollection/albumsHeader ')] and
                      */*[contains(@class,' musicCollection/comments ') or contains(@class,' musicCollection/commentsHeader ')]">3</xsl:when>
      <xsl:when test="*/*[contains(@class,' musicCollection/band ') or contains(@class,' musicCollection/bandHeader ')] and
                      */*[contains(@class,' musicCollection/albums ') or contains(@class,' musicCollection/albumsHeader ')]">2</xsl:when>
      <xsl:when test="*/*[contains(@class,' musicCollection/band ') or contains(@class,' musicCollection/bandHeader ')] and
                      */*[contains(@class,' musicCollection/comments ') or contains(@class,' musicCollection/commentsHeader ')]">2</xsl:when>
      <xsl:when test="*/*[contains(@class,' musicCollection/albums ') or contains(@class,' musicCollection/albumsHeader ')] and
                      */*[contains(@class,' musicCollection/comments ') or contains(@class,' musicCollection/commentsHeader ')]">2</xsl:when>
      <xsl:when test="*/*">1</xsl:when>
      <xsl:otherwise><!--ERROR condition, not allowed by DTD-->1</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:call-template name="checkPageBreak"/>
  <xsl:call-template name="addFlag"/>
  <table><xsl:call-template name="commonAttributesWithId"/>
    <xsl:apply-templates select="@outputclass"/>
    <xsl:if test="@frame">
      <xsl:attribute name="frame"><xsl:value-of select="@frame"/></xsl:attribute>
      <xsl:if test="@frame='none'">
        <xsl:attribute name="colsep">0</xsl:attribute>
        <xsl:attribute name="rowsep">0</xsl:attribute>
      </xsl:if>
    </xsl:if>
    <desc>
      <xsl:call-template name="musicGetString">
        <xsl:with-param name="stringName" select="'My Discs'"/>
      </xsl:call-template>
    </desc>
    <tgroup cols="{$cols}"><xsl:call-template name="debugAttributes"/>
      <xsl:if test="@relcolwidth"><xsl:call-template name="processRelcolwidth"/></xsl:if>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' musicCollection/cdHeader ')]">
          <xsl:apply-templates select="*[contains(@class,' musicCollection/cdHeader ')]"/>
        </xsl:when>
        <xsl:otherwise>
          <thead><xsl:call-template name="debugAttributes"/>
            <row><xsl:call-template name="debugAttributes"/>
              <xsl:if test="*/*[contains(@class,' musicCollection/band ')]">
                <entry><xsl:call-template name="debugAttributes"/>
                  <xsl:call-template name="musicGetString">
                    <xsl:with-param name="stringName" select="'Band'"/>
                  </xsl:call-template>
                </entry>
              </xsl:if>
              <xsl:if test="*/*[contains(@class,' musicCollection/albums ')]">
                <entry><xsl:call-template name="debugAttributes"/>
                  <xsl:call-template name="musicGetString">
                    <xsl:with-param name="stringName" select="'Albums'"/>
                  </xsl:call-template>
                </entry>
              </xsl:if>
              <xsl:if test="*/*[contains(@class,' musicCollection/comments ')]">
                <entry><xsl:call-template name="debugAttributes"/>
                  <xsl:call-template name="musicGetString">
                    <xsl:with-param name="stringName" select="'Comments'"/>
                  </xsl:call-template>
                </entry>
              </xsl:if>
            </row>
          </thead>
        </xsl:otherwise>
      </xsl:choose>
      <tbody><xsl:call-template name="debugAttributes"/>
        <xsl:apply-templates select="*[contains(@class,' musicCollection/cdRow ')]"/>
      </tbody>
    </tgroup>
  </table>
</xsl:template>

<xsl:template match="*[contains(@class,' musicCollection/cdHeader ')]">
  <thead><xsl:call-template name="debugAttributes"/>
    <row><xsl:call-template name="commonAttributesWithId"/>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' musicCollection/bandHeader ')]"><xsl:apply-templates select="*[contains(@class,' musicCollection/bandHeader ')]"/></xsl:when>
        <xsl:when test="../*/*[contains(@class,' musicCollection/band ')]">
          <entry><xsl:call-template name="debugAttributes"/>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Band'"/>
            </xsl:call-template>
          </entry>
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' musicCollection/albumsHeader ')]"><xsl:apply-templates select="*[contains(@class,' musicCollection/albumsHeader ')]"/></xsl:when>
        <xsl:when test="../*/*[contains(@class,' musicCollection/albums ')]">
          <entry><xsl:call-template name="debugAttributes"/>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Albums'"/>
            </xsl:call-template>
          </entry>
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class,' musicCollection/commentsHeader ')]"><xsl:apply-templates select="*[contains(@class,' musicCollection/commentsHeader ')]"/></xsl:when>
        <xsl:when test="../*/*[contains(@class,' musicCollection/comments ')]">
          <entry><xsl:call-template name="debugAttributes"/>
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Comments'"/>
            </xsl:call-template>
          </entry>
        </xsl:when>
      </xsl:choose>
    </row>
  </thead>
</xsl:template>

<xsl:template match="*[contains(@class,' musicCollection/cdRow ')]">
  <row><xsl:call-template name="commonAttributesWithId"/>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' musicCollection/band ')]"><xsl:apply-templates select="*[contains(@class,' musicCollection/band ')]"/></xsl:when>
      <xsl:when test="../*/*[contains(@class,' musicCollection/band ')] |
                      ../*[1]/*[contains(@class,' musicCollection/bandHeader ')]">
        <entry><xsl:call-template name="debugAttributes"/></entry>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' musicCollection/albums ')]"><xsl:apply-templates select="*[contains(@class,' musicCollection/albums ')]"/></xsl:when>
      <xsl:when test="../*/*[contains(@class,' musicCollection/albums ')] |
                      ../*[1]/*[contains(@class,' musicCollection/albumsHeader ')]">
        <entry><xsl:call-template name="debugAttributes"/></entry>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' musicCollection/comments ')]"><xsl:apply-templates select="*[contains(@class,' musicCollection/comments ')]"/></xsl:when>
      <xsl:when test="../*/*[contains(@class,' musicCollection/comments ')] |
                      ../*[1]/*[contains(@class,' musicCollection/commentsHeader ')]">
        <entry><xsl:call-template name="debugAttributes"/></entry>
      </xsl:when>
    </xsl:choose>
  </row>
</xsl:template>

<xsl:template match="*[contains(@class,' musicCollection/band ')]|*[contains(@class,' musicCollection/bandHeader ')]">
  <xsl:param name="keycol">
    <xsl:choose>
      <xsl:when test="parent::*/parent::*/@keycol='1'">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:call-template name="topic.stentry">
    <xsl:with-param name="keycol" select="$keycol"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' musicCollection/albums ')]|*[contains(@class,' musicCollection/albumsHeader ')]">
  <xsl:param name="keycol">
    <xsl:choose>
      <!-- If there is a first column, and keycol=2, then set to yes.
           Otherwise, no first column, if keycol=1, set to yes.
           Otherwise, set to no. -->
      <xsl:when test="parent::*/parent::*/*/*[contains(@class,' musicCollection/albums ') or contains(@class,' musicCollection/albumsHeader ')]">
        <xsl:if test="parent::*/parent::*/@keycol='2'">yes</xsl:if>
        <xsl:if test="not(parent::*/parent::*/@keycol='2')">no</xsl:if>
      </xsl:when>
      <xsl:when test="parent::*/parent::*/@keycol='1'">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:call-template name="topic.stentry">
    <xsl:with-param name="keycol" select="$keycol"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' musicCollection/comments ')]|*[contains(@class,' musicCollection/commentsHeader ')]">
  <xsl:param name="keycol">
    <xsl:choose>
      <!-- If there is a first AND second column, and keycol=3, then set to yes.
           Otherwise, if there is a first column, and keycol=2, then set to yes
           Otherwise, no first or second column, if keycol=1, set to yes.
           Otherwise, set to no. -->
      <xsl:when test="(parent::*/parent::*/*/*[contains(@class,' musicCollection/albums ') or contains(@class,' musicCollection/albumsHeader ')])
                      and
                      (parent::*/parent::*/*/*[contains(@class,' musicCollection/band ') or contains(@class,' musicCollection/bandHeader ')])">
        <xsl:if test="parent::*/parent::*/@keycol='3'">yes</xsl:if>
        <xsl:if test="not(parent::*/parent::*/@keycol='3')">no</xsl:if>
      </xsl:when>
      <xsl:when test="(parent::*/parent::*/*/*[contains(@class,' musicCollection/albums ') or contains(@class,' musicCollection/albumsHeader ')])
                      or
                      (parent::*/parent::*/*/*[contains(@class,' musicCollection/band ') or contains(@class,' musicCollection/bandHeader ')])">
        <xsl:if test="parent::*/parent::*/@keycol='2'">yes</xsl:if>
        <xsl:if test="not(parent::*/parent::*/@keycol='2')">no</xsl:if>
      </xsl:when>
      <xsl:when test="parent::*/parent::*/@keycol='1'">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:call-template name="topic.stentry">
    <xsl:with-param name="keycol" select="$keycol"/>
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
