<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2006 All Rights Reserved. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">
  <xsl:output method="xml" indent="no" encoding="utf-8" 
       doctype-public="-//RDA//DTD DITA Song Library//EN"
       doctype-system="../dtd/songLibrary.dtd"/>

<!--  <key>Playlists</key>
  <array>
      <dict>
          <key>Name</key><string>Library</string>
          <key>Master</key><true/>
          <key>Playlist ID</key><integer>12517</integer>
          <key>Playlist Persistent ID</key><string>B3385E38C362913C</string>
          <key>All Items</key><true/>
          <key>Playlist Items</key>
          <array>
              <dict>
                  <key>Track ID</key><integer>37</integer> -->
  <xsl:key match="/plist/
                    dict/
                     array[1]/
                      dict[not(string[1]='Library')]/
                       array/
                        dict/
                         integer" use="." name="playlists"/>
  <xsl:template match="/">
    <xsl:for-each select="/plist/
                    dict/
                     array[1]/
                      dict[not(string[1]='Library')]">
      <xsl:variable name="playlistId" select="normalize-space(string[1])"/>
      <saxon:output href="{$playlistId}.dita">
        <songLibrary id="playlist{integer[1]}">
          <title><xsl:value-of select="string[1]"/></title>
          <songlibrarybody>
            <songlist>
              <xsl:apply-templates select="array/dict" mode="playlistContents"/>
            </songlist>
          </songlibrarybody>
        </songLibrary>
      </saxon:output>
    </xsl:for-each>

    <songLibrary id="library">
      <title>This is my ENTIRE collection</title>
      <songLibraryBody>
        <xsl:apply-templates select="/plist/dict"/>
      </songLibraryBody>
    </songLibrary>

    <!-- Generate files for each playlist -->
    <!--
      <xsl:for-each select="/plist/dict/following-sibling::key[.='Playlists']/
    -->
  </xsl:template>

<!--
  <dict>
	<key>Major Version</key><integer>1</integer>
	<key>Minor Version</key><integer>1</integer>
	<key>Application Version</key><string>6.0.1</string>
	<key>Features</key><integer>1</integer>
	<key>Music Folder</key><string>file://localhost/F:/</string>
	<key>Library Persistent ID</key><string>B3385E38C362913B</string>
	<key>Tracks</key>
	<dict>
		<key>37</key>
		<dict>
			<key>Track ID</key><integer>37</integer>
			<key>Name</key><string>Carolina</string>
            -->
  <xsl:template match="/plist/dict">
    <songList>
      <xsl:apply-templates select="key[.='Tracks']/following-sibling::dict[1]/dict" mode="songs"/>
    </songList>
  </xsl:template>

  <xsl:template match="/plist/dict/dict/dict" mode="songs">
    <xsl:variable name="songid" select="key[.='Track ID']/following-sibling::integer[1]"/>
    <xsl:text>
</xsl:text>
    <song id="s{$songid}">
      <artist>
        <xsl:value-of select="key[.='Artist']/following-sibling::string[1]"/>
      </artist>
      <songTitle><xsl:value-of select="key[.='Name']/following-sibling::string[1]"/></songTitle>
      <album>
        <xsl:value-of select="key[.='Album']/following-sibling::string[1]"/>
      </album>
      <genre>
        <xsl:value-of select="key[.='Genre']/following-sibling::string[1]"/>
      </genre>
      <inPlaylist>
        <!--<xsl:for-each select="key('playlists',$songid)">
          <ph>
            <xsl:value-of select="ancestor::array[1]/preceding-sibling::key[.='Name']/following-sibling::string[1]"/>
          </ph>
        </xsl:for-each>-->
        <xsl:apply-templates select="/plist/
                    dict/
                     array[1]/
                      dict[not(string[1]='Library')]/
                       array" mode="inPlaylist">
          <xsl:with-param name="songid" select="$songid"/>
        </xsl:apply-templates>
      </inPlaylist>
    </song>
  </xsl:template>

  <xsl:template match="*" mode="inPlaylist">
    <xsl:param name="songid"/>
    <xsl:if test="dict/integer[.=$songid]">
      <ph><xsl:value-of select="preceding-sibling::key[.='Name']/following-sibling::string[1]"/></ph>
    </xsl:if>
  </xsl:template>

  <xsl:template match="dict" mode="playlistContents">
    <xsl:text>
</xsl:text>
    <song conref="library.dita#library/s{integer[1]}">
      <artist/><songtitle/>
    </song>
  </xsl:template>

  <xsl:template match="text()"/>
</xsl:stylesheet>
