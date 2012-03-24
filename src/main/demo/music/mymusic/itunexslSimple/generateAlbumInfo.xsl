<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2006-2009 by Robert D. Anderson -->
<!--
  Create a topic for each album.
-->
<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">

  <!-- Customize is used to hide songs or albums, if desired -->
  <xsl:import href="customize.xsl"/>

  <!-- Common code used by many modules -->
  <xsl:import href="commonOutputProcessing.xsl"/>

  <!--<xsl:output method="xml" doctype-system="../../dtd/songs.dtd"/>-->
  <xsl:output method="xml"/>

  <xsl:key name="albums"
           match="/plist/dict/dict[1]/dict"
           use="@album"/>

  <xsl:template match="/">
    <xsl:processing-instruction name="xml-stylesheet"> type="text/xsl" href="musicmap2bandlist.xsl"</xsl:processing-instruction>
    <map class="- map/map " id="albums">
      <title class="- topic/title ">Music library (by album)</title>
      <xsl:for-each select="/plist/dict/dict[1]/dict">
        <xsl:sort select="@album" case-order="lower-first"/>
        <xsl:variable name="hideThis"><xsl:apply-templates select="." mode="hideThisAlbum"/></xsl:variable>
        <xsl:choose>
          <xsl:when test="$hideThis='yes'"/>
          <xsl:when test="generate-id(.)!=generate-id((key('albums',@album))[1])">
            <!-- Already appeared; skip it -->
          </xsl:when>
          <xsl:otherwise>
            <!-- First time this album has appeared; add to map and create topic -->
            <xsl:variable name="filename">
              <xsl:call-template name="albumFilename"/>
            </xsl:variable>

            <!-- First, create map entry -->
            <topicref
                      href="{$filename}"
                      navtitle="{@album}"
                      class="- map/topicref ">
              <xsl:apply-templates select="." mode="setCustomTopicrefAttrs-album"/>
            </topicref>

            <!-- Next, create the topic -->
            <xsl:result-document href="{$filename}">
              <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="../../view.xsl"</xsl:processing-instruction>
              <songCollection id="a{@id}" class="- topic/topic reference/reference songCollection/songCollection ">
                <title class="- topic/title "><xsl:value-of select="@album"/></title>
                <songBody class="- topic/body reference/refbody songCollection/songBody ">
                  <songList class="- topic/simpletable reference/simpletable songCollection/songList ">
                    <xsl:for-each select="key('albums',@album)">
                      <xsl:apply-templates select="." mode="addRow"/>
                    </xsl:for-each>
                  </songList>
                </songBody>
              </songCollection>
            </xsl:result-document>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>

    </map>
  </xsl:template>


</xsl:stylesheet>
