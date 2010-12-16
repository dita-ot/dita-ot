<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2008-2009 by Robert D. Anderson -->
<!--
  Create a map that lists all of the recently added albums.
-->
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">

  <!-- Customize is used to hide songs or albums, if desired -->
  <xsl:import href="customize.xsl"/>

  <!-- Common code used by many modules -->
  <xsl:import href="commonOutputProcessing.xsl"/>

  <xsl:param name="DATE"></xsl:param>

  <!--<xsl:output method="xml" doctype-system="../../dtd/songs.dtd"/>-->
  <xsl:output method="xml"/>

  <xsl:key name="albums"
           match="/plist/dict/dict[1]/dict"
           use="@album"/>

  <xsl:template match="/">
    <xsl:processing-instruction name="xml-stylesheet"> type="text/xsl" href="musicmap2bandlist.xsl"</xsl:processing-instruction>
    <map class="- map/map " id="albums">
      <title class="- topic/title ">Albums added since <xsl:value-of select="$DATE"/></title>
      <xsl:for-each select="/plist/dict/dict[1]/dict">
        <xsl:sort select="@added" order="descending"/>
        <xsl:variable name="hideThis"><xsl:apply-templates select="." mode="hideThisAlbum"/></xsl:variable>
        <xsl:choose>
          <xsl:when test="@added &lt; $DATE">
            <!-- Older album -->
          </xsl:when>
          <xsl:when test="generate-id(.)!=generate-id((key('albums',@album))[1])">
            <!-- Already appeared; skip it -->
          </xsl:when>
          <xsl:when test="$hideThis='yes'"/>
          <xsl:otherwise>
            <!-- First time this album has appeared; add to map and create topic -->
            <xsl:variable name="filename">
              <xsl:call-template name="albumFilename"/>
            </xsl:variable>
            <xsl:variable name="thisArtist">
              <xsl:choose>
                <xsl:when test="@comp='yes'">Various Artists</xsl:when>
                <xsl:otherwise><xsl:value-of select="@artist"/></xsl:otherwise>
              </xsl:choose>
            </xsl:variable>
            <xsl:variable name="titleWithDate">
              <xsl:value-of select="substring(@added,6,5)"/>: <xsl:value-of select="$thisArtist"/> - <xsl:value-of select="@album"/>
            </xsl:variable>
            <!-- First, create map entry -->
            <topicref
                      href="{$filename}"
                      navtitle="{$titleWithDate}"
                      class="- map/topicref ">
              <xsl:apply-templates select="." mode="setCustomTopicrefAttrs-album"/>
            </topicref>
         </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </map>
  </xsl:template>

</xsl:stylesheet>
