<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2006 All Rights Reserved. -->

<!-- 
     This file is used to override the DITA to XHTML transform. It
     overrides the cdList element in order to produce new generated text.
     All other elements fall back to normal topic processing. 
-->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- XHTML output with XML syntax -->
<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>



<!--<xsl:template match="*[contains(@class,' songCollection/songList ')]">
  <h3>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'My Discs'"/>
    </xsl:call-template>
  </h3>
  <xsl:call-template name="topic.simpletable"/>
</xsl:template>-->

<!-- Add a header for each column that exists in the table -->
<xsl:template match="*" mode="add-song-headers">
  <tr>
    <xsl:value-of select="$newline"/>
    <xsl:if test="*/*[contains(@class,' songCollection/song ')]">
      <th id="{generate-id(.)}-song"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Song'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
    <xsl:if test="*/*[contains(@class,' songCollection/album ')]">
      <th id="{generate-id(.)}-album"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Album'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
    <xsl:if test="*/*[contains(@class,' songCollection/artist ')]">
      <th id="{generate-id(.)}-artist"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Artist'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
    <xsl:if test="*/*[contains(@class,' songCollection/genre ')]">
      <th id="{generate-id(.)}-genre"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Genre'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
    <xsl:if test="*/*[contains(@class,' songCollection/rating ')]">
      <th id="{generate-id(.)}-rating"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Rating'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
    <xsl:if test="*/*[contains(@class,' songCollection/count ')]">
      <th id="{generate-id(.)}-count"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Count'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
    <xsl:if test="*/*[contains(@class,' songCollection/playdate ')]">
      <th id="{generate-id(.)}-playdate"><xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="'Play date'"/>
      </xsl:call-template></th><xsl:value-of select="$newline"/>
    </xsl:if>
  </tr>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' songCollection/songRow ')]">
  <!-- If there was no header, then this is the first child of cdList; create default headers -->
  <xsl:if test="not(preceding-sibling::*)">
    <xsl:apply-templates select=".." mode="add-song-headers"/>
  </xsl:if>
  <tr>
    <xsl:call-template name="setid"/>
    <xsl:call-template name="commonattributes"/>
    <xsl:value-of select="$newline"/>
     <!-- For each of the entry types:
          - If it is in this row, apply
          - Otherwise, if it is in the table at all, create empty entry -->
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/song ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/song ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/song ')]">
        <td id="generate-id(..)-song">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/album ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/album ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/album ')]">
        <td id="generate-id(..)-album">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/artist ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/artist ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/artist ')]">
        <td id="generate-id(..)-artist">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/genre ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/genre ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/genre ')]">
        <td id="generate-id(..)-genre">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/rating ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/rating ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/rating ')]">
        <td id="generate-id(..)-rating">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/count ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/count ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/count ')]">
        <td id="generate-id(..)-count">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="*[contains(@class,' songCollection/playdate ')]">
        <xsl:apply-templates select="*[contains(@class,' songCollection/playdate ')]"/>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class,' songCollection/playdate ')]">
        <td id="generate-id(..)-playdate">&#xA0;</td>
      </xsl:when>
    </xsl:choose>
  </tr>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' songCollection/song ')]">
  <td headers="{generate-id(../..)}-song">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>
<xsl:template match="*[contains(@class,' songCollection/artist ')]">
  <td headers="{generate-id(../..)}-artist">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>
<xsl:template match="*[contains(@class,' songCollection/album ')]">
  <td headers="{generate-id(../..)}-album">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>
<xsl:template match="*[contains(@class,' songCollection/genre ')]">
  <td headers="{generate-id(../..)}-genre">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>
<xsl:template match="*[contains(@class,' songCollection/rating ')]">
  <td headers="{generate-id(../..)}-rating">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>
<xsl:template match="*[contains(@class,' songCollection/count ')]">
  <td headers="{generate-id(../..)}-count">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>
<xsl:template match="*[contains(@class,' songCollection/playdate ')]">
  <td headers="{generate-id(../..)}-playdate">
    <xsl:call-template name="commonattributes"/>
    <xsl:apply-templates/>
  </td>
</xsl:template>

</xsl:stylesheet>
