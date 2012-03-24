<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2008 by Robert D. Anderson -->
<!--
  Common processing used for each output target (albums, artists, playlists)
-->
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">

  <xsl:param name="OUTDIR">./</xsl:param>
  <xsl:param name="DITAEXT">.xml</xsl:param>
  <xsl:param name="CURRENT-DATE"></xsl:param>
  <xsl:param name="PLAYLIST-DIRECTORY">playlists</xsl:param>
  <xsl:param name="PODCAST-DIRECTORY">podcasts</xsl:param>
  <xsl:param name="ARTIST-DIRECTORY">artists</xsl:param>
  <xsl:param name="ALBUM-DIRECTORY">albums</xsl:param>
  <xsl:param name="PLAYLIST-FILE-PREFIX">l</xsl:param>
  <xsl:param name="PODCAST-FILE-PREFIX">p</xsl:param>
  <xsl:param name="ARTIST-FILE-PREFIX">t</xsl:param>
  <xsl:param name="ALBUM-FILE-PREFIX">a</xsl:param>

  <xsl:template match="dict|podcast" mode="addRow">
    <xsl:param name="includeGenre" select="'yes'"/>
    <xsl:param name="includeCount" select="'no'"/>
    <xsl:param name="includeDate" select="'no'"/>
    <xsl:variable name="hideThis">
      <xsl:apply-templates select="." mode="hideThisSong"/>
    </xsl:variable>
    <xsl:if test="$hideThis!='yes'">
      <songRow class="- topic/strow reference/strow songCollection/songRow ">
        <song class="- topic/stentry reference/stentry songCollection/song "><xsl:value-of select="@name"/></song>
        <album class="- topic/stentry reference/stentry songCollection/album "><xsl:value-of select="@album"/></album>
        <artist class="- topic/stentry reference/stentry songCollection/artist "><xsl:value-of select="@artist"/></artist>
        <xsl:if test="$includeGenre!='no' and @genre">
          <genre class="- topic/stentry reference/stentry songCollection/genre "><xsl:value-of select="@genre"/></genre>
        </xsl:if>
        <xsl:if test="number(@rating)>0">
          <!-- Maybe make use of stars? &#x2605; -->
          <rating class="- topic/stentry reference/stentry songCollection/rating ">
            <xsl:value-of select="@rating"/>
          </rating>
        </xsl:if>
        <xsl:if test="$includeCount='yes' and @playcount">
          <count class="- topic/stentry reference/stentry songCollection/count "><xsl:value-of select="@playcount"/></count>
        </xsl:if>
        <xsl:if test="$includeDate='yes' and @played">
          <playdate class="- topic/stentry reference/stentry songCollection/playdate "><xsl:value-of select="substring(@played,1,10)"/></playdate>
        </xsl:if>
      </songRow>
    </xsl:if>
  </xsl:template>

  <xsl:template name="setFirstchar">
    <xsl:param name="firstchar"/>
              <xsl:choose>
                <xsl:when test="($firstchar >= '0' and $firstchar &lt;= '9') or
                                ($firstchar >= 'a' and $firstchar &lt;= 'z') or
                                ($firstchar >= 'A' and $firstchar &lt;= 'Z')">
                  <xsl:value-of select="upper-case($firstchar)"/>
                </xsl:when>
                <xsl:otherwise>chars</xsl:otherwise>
              </xsl:choose>
  </xsl:template>

  <xsl:template name="podcastFilename">
    <xsl:value-of select="$CURRENT-DATE"/>
    <xsl:value-of select="$PODCAST-DIRECTORY"/>
    <xsl:text>/</xsl:text>
    <xsl:call-template name="setFirstchar">
      <xsl:with-param name="firstchar" select="substring(@artist,1,1)"/>
    </xsl:call-template>
    <xsl:text>/</xsl:text>
    <xsl:value-of select="$PODCAST-FILE-PREFIX"/>
    <xsl:value-of select="@id"/>
    <xsl:value-of select="$DITAEXT"/>
  </xsl:template>

  <xsl:template name="artistFilename">
    <xsl:value-of select="$CURRENT-DATE"/>
    <xsl:value-of select="$ARTIST-DIRECTORY"/>
    <xsl:text>/</xsl:text>
    <xsl:call-template name="setFirstchar">
      <xsl:with-param name="firstchar" select="substring(@artist,1,1)"/>
    </xsl:call-template>
    <xsl:text>/</xsl:text>
    <xsl:value-of select="$ARTIST-FILE-PREFIX"/>
    <xsl:value-of select="@id"/>
    <xsl:value-of select="$DITAEXT"/>
  </xsl:template>

  <xsl:template name="albumFilename">
    <xsl:value-of select="$CURRENT-DATE"/>
    <xsl:value-of select="$ALBUM-DIRECTORY"/>
    <xsl:text>/</xsl:text>
    <xsl:call-template name="setFirstchar">
      <xsl:with-param name="firstchar" select="substring(@album,1,1)"/>
    </xsl:call-template>
    <xsl:text>/</xsl:text>
    <xsl:value-of select="$ALBUM-FILE-PREFIX"/>
    <xsl:value-of select="@id"/>
    <xsl:value-of select="$DITAEXT"/>
  </xsl:template>

  <xsl:template name="playlistFilename">
    <xsl:value-of select="$CURRENT-DATE"/>
    <xsl:value-of select="$PLAYLIST-DIRECTORY"/>
    <xsl:text>/</xsl:text>
    <xsl:value-of select="$PLAYLIST-FILE-PREFIX"/>
    <xsl:value-of select="generate-id(.)"/>
    <xsl:value-of select="$DITAEXT"/>
  </xsl:template>

</xsl:stylesheet>
