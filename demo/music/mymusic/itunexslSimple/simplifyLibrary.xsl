<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2008 by Robert D. Anderson -->
<!--
  Simplify the iTunes XML format.
-->
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">

  <!-- Customize defines which playlists stay in the simplified library -->
  <xsl:import href="customize.xsl"/>

  <xsl:key name="podcasts"
           match="/plist/dict/array/dict[string[1]='Podcasts']/array/dict"
           use="integer"/>
  <xsl:key name="audiobooks"
           match="/plist/dict/array/dict[string[1]='Audiobooks']/array/dict"
           use="integer"/>


  <xsl:output method="xml"/>

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="/plist | /plist/dict | /plist/dict/dict[1]">
    <xsl:copy><xsl:apply-templates/></xsl:copy>
  </xsl:template>

  <xsl:template match="/plist/dict/array[preceding-sibling::*[1][.='Playlists']]">
    <playlists><xsl:apply-templates/></playlists>
  </xsl:template>

  <xsl:template match="/plist/dict/array/dict">
    <!-- Match a playlist. Keep it? -->
    <xsl:variable name="keepPlaylist">
      <xsl:apply-templates select="string[1]" mode="evaluatePlaylist"/>
    </xsl:variable>
    <xsl:if test="$keepPlaylist='yes'">
      <playlist name="{string[1]}"><xsl:apply-templates select="array/*"/></playlist>
    </xsl:if>
  </xsl:template>
  <xsl:template match="/plist/dict/array/dict/array/dict">
    <item refid="{integer}"/>
  </xsl:template>

  <xsl:template match="* | text()"/>

  <xsl:template match="/plist/dict/dict[1]/dict">
    <xsl:variable name="thisid" select="integer[1]"/>
    <xsl:choose>
      <xsl:when test="key('podcasts',$thisid)">
        <podcast><xsl:apply-templates/></podcast>
      </xsl:when>
      <!-- Some podcasts aren't added to the list (?) -->
      <xsl:when test="*[.='Genre']/following-sibling::*[1]='Podcast'">
        <podcast><xsl:apply-templates/></podcast>
      </xsl:when>
      <xsl:when test="key('audiobooks',$thisid)">
        <audiobook><xsl:apply-templates/></audiobook>
      </xsl:when>
      <xsl:otherwise>
        <dict><xsl:apply-templates/></dict>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="/plist/dict/dict[1]/dict/*">
    <xsl:choose>
      <xsl:when test=".='Track ID'"><xsl:attribute name="id"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Name'"><xsl:attribute name="name"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Artist'"><xsl:attribute name="artist"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Album'"><xsl:attribute name="album"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Genre'"><xsl:attribute name="genre"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Play Count'"><xsl:attribute name="playcount"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Play Date UTC'"><xsl:attribute name="played"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Rating'"><xsl:attribute name="rating"><xsl:value-of select="number(following-sibling::*[1]) div 20"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Grouping'"><xsl:attribute name="grouping"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Date Added'"><xsl:attribute name="added"><xsl:value-of select="following-sibling::*[1]"/></xsl:attribute></xsl:when>
      <xsl:when test=".='Compilation'"><xsl:if test="following-sibling::*[1][self::true]"><xsl:attribute name="comp">yes</xsl:attribute></xsl:if></xsl:when>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
