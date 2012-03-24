<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2009 by Robert D. Anderson -->
<!--
  Create a topic for each playlist.
-->
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">

  <!-- Customize is used to hide songs or albums, if desired -->
  <xsl:import href="customize.xsl"/>

  <!-- Common code used by many modules -->
  <xsl:import href="commonOutputProcessing.xsl"/>

  <xsl:param name="INCLUDE-RECENTLY-PLAYED">yes</xsl:param>
  <xsl:param name="INCLUDE-MOST-PLAYED">yes</xsl:param>

  <xsl:param name="RECENTLY-PLAYED-FILE">
    <xsl:value-of select="$OUTDIR"/>
<!--    <xsl:value-of select="concat($PLAYLIST-DIRECTORY,$CURRENT-DATE,'/',$PLAYLIST-FILE-PREFIX,'recent',$DITAEXT)"/>-->
    <xsl:value-of select="concat($PLAYLIST-FILE-PREFIX,'recent',$DITAEXT)"/>
  </xsl:param>
  <xsl:param name="RECENTLY-PLAYED-DATE"/>
  <xsl:param name="MOST-PLAYED-FILE">
    <xsl:value-of select="$OUTDIR"/>
<!--    <xsl:value-of select="concat($PLAYLIST-DIRECTORY,$CURRENT-DATE,'/',$PLAYLIST-FILE-PREFIX,'mostplayed',$DITAEXT)"/>-->
    <xsl:value-of select="concat($PLAYLIST-FILE-PREFIX,'mostplayed',$DITAEXT)"/>
  </xsl:param>
  <xsl:param name="MOST-PLAYED-COUNT" select="'25'"/>

  <!--<xsl:output method="xml" doctype-system="../../dtd/songs.dtd"/>-->
  <xsl:output method="xml"/>

  <xsl:key name="items"
           match="/plist/dict/dict[1]/dict|/plist/dict/dict[1]/podcast"
           use="@id"/>

  <xsl:template match="/">
    <!--<xsl:if test="/plist/dict/playlists/playlist">-->
      <xsl:processing-instruction name="xml-stylesheet"> type="text/xsl" href="musicmap2bandlist.xsl"</xsl:processing-instruction>
      <map class="- map/map " id="artists">
        <title class="- topic/title ">Playlists</title>
        <xsl:if test="$INCLUDE-MOST-PLAYED='no' and $INCLUDE-RECENTLY-PLAYED='no' and not(/plist/dict/playlists/playlist)">
          <topicref href="overview.html" navtitle="No playlists specified" class="- map/topicref "/>
        </xsl:if>
        <xsl:if test="$INCLUDE-MOST-PLAYED='yes'">
          <xsl:call-template name="addMostPlayed"/>
        </xsl:if>
        <xsl:if test="$INCLUDE-RECENTLY-PLAYED='yes'">
          <xsl:call-template name="addRecent"/>
        </xsl:if>
        <xsl:for-each select="/plist/dict/playlists/playlist">
          <xsl:sort select="@name" case-order="lower-first"/>
          <xsl:variable name="filename">
            <xsl:call-template name="playlistFilename"/>
          </xsl:variable>
          <topicref
                    href="{$filename}"
                    navtitle="{@name}"
                    class="- map/topicref ">
          </topicref>
          <xsl:result-document href="{$filename}">
            <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="../view.xsl"</xsl:processing-instruction>
            <songCollection id="l{generate-id(.)}" class="- topic/topic reference/reference songCollection/songCollection ">
              <title class="- topic/title ">Playlist: <xsl:value-of select="@name"/></title>
              <songBody class="- topic/body reference/refbody songCollection/songBody ">
                <songList class="- topic/simpletable reference/simpletable songCollection/songList ">
                  <xsl:for-each select="item">
                    <xsl:apply-templates select="key('items',@refid)" mode="addRow"/>
                  </xsl:for-each>
                </songList>
              </songBody>
            </songCollection>
          </xsl:result-document>
        </xsl:for-each>
      </map>
    <!--</xsl:if>-->
  </xsl:template>

  <xsl:template name="addRecent">
    <topicref href="{$RECENTLY-PLAYED-FILE}"
              navtitle="Recently Played"
              class="- map/topicref "/>
    <xsl:result-document href="{$RECENTLY-PLAYED-FILE}">
      <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="view.xsl"</xsl:processing-instruction>
      <songCollection id="l{generate-id(.)}" class="- topic/topic reference/reference songCollection/songCollection ">
        <title class="- topic/title ">Songs played since <xsl:value-of select="$RECENTLY-PLAYED-DATE"/> (last updated <xsl:value-of select="$CURRENT-DATE"/>)</title>
        <songBody class="- topic/body reference/refbody songCollection/songBody ">
          <songList class="- topic/simpletable reference/simpletable songCollection/songList ">
            <xsl:for-each select="/plist/dict/dict[1]/dict[@played &gt;= $RECENTLY-PLAYED-DATE] |
                                  /plist/dict/dict[1]/podcast[@played &gt;= $RECENTLY-PLAYED-DATE]">
              <xsl:sort select="@played" order="descending"/>
              <xsl:apply-templates select="." mode="addRow">
                <xsl:with-param name="includeDate" select="'yes'"/>
              </xsl:apply-templates>
            </xsl:for-each>
          </songList>
        </songBody>
      </songCollection>
    </xsl:result-document>
  </xsl:template>

  <xsl:template name="addMostPlayed">
    <topicref href="{$MOST-PLAYED-FILE}"
              navtitle="Most Played"
              class="- map/topicref "/>
    <xsl:result-document href="{$MOST-PLAYED-FILE}">
      <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="view.xsl"</xsl:processing-instruction>
      <songCollection id="l{generate-id(.)}" class="- topic/topic reference/reference songCollection/songCollection ">
        <title class="- topic/title ">Songs played at least <xsl:value-of select="$MOST-PLAYED-COUNT"/> times</title>
        <songBody class="- topic/body reference/refbody songCollection/songBody ">
          <songList class="- topic/simpletable reference/simpletable songCollection/songList ">
            <xsl:for-each select="/plist/dict/dict[1]/dict[number(@playcount) &gt;= number($MOST-PLAYED-COUNT)] |
                                  /plist/dict/dict[1]/podcast[number(@playcount) &gt;= number($MOST-PLAYED-COUNT)]">
              <xsl:sort select="@playcount" order="descending" data-type="number"/>
              <xsl:apply-templates select="." mode="addRow">
                <xsl:with-param name="includeCount" select="'yes'"/>
                <xsl:with-param name="includeDate" select="'yes'"/>
              </xsl:apply-templates>
            </xsl:for-each>
          </songList>
        </songBody>
      </songCollection>
    </xsl:result-document>
  </xsl:template>


</xsl:stylesheet>
