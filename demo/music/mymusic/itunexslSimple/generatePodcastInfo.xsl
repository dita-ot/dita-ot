<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2008-2009 by Robert D. Anderson -->
<!--
  Create a topic for each podcast.
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

  <xsl:key name="artists"
           match="/plist/dict/dict[1]/podcast"
           use="@artist"/>

  <xsl:template match="/">
    <xsl:processing-instruction name="xml-stylesheet"> type="text/xsl" href="musicmap2bandlist.xsl"</xsl:processing-instruction>
    <map class="- map/map " id="podcasts">
      <title class="- topic/title ">Podcasts</title>
      <xsl:for-each select="/plist/dict/dict[1]/podcast">
        <xsl:sort select="@artist" case-order="lower-first"/>
        <xsl:variable name="hideThis"><xsl:apply-templates select="." mode="hideThisArtist"/></xsl:variable>
        <xsl:choose>
          <xsl:when test="$hideThis='yes'"/>
          <xsl:when test="generate-id(.)!=generate-id((key('artists',@artist))[1])">
            <!-- Already appeared; skip it -->
          </xsl:when>
          <xsl:otherwise>
            <!-- First time this artist has appeared; add to map and create topic -->
            <xsl:variable name="filename">
              <xsl:call-template name="podcastFilename"/>
            </xsl:variable>

            <!-- First, create map entry -->
            <topicref
                      href="{$filename}"
                      navtitle="{@artist}"
                      class="- map/topicref ">
              <xsl:apply-templates select="." mode="setCustomTopicrefAttrs"/>
            </topicref>

            <!-- Next, create the topic -->
            <xsl:result-document href="{$filename}">
              <xsl:processing-instruction name="xml-stylesheet">type="text/xsl" href="../../view.xsl"</xsl:processing-instruction>
              <songCollection id="a{@id}" class="- topic/topic reference/reference songCollection/songCollection ">
                <title class="- topic/title "><xsl:value-of select="@artist"/></title>
                <songBody class="- topic/body reference/refbody songCollection/songBody ">
                  <songList class="- topic/simpletable reference/simpletable songCollection/songList ">
                    <xsl:for-each select="key('artists',@artist)">
                      <!-- Genre is always Podcast, so leave it out -->
                      <xsl:apply-templates select="." mode="addRow">
                        <xsl:with-param name="includeGenre" select="'no'"/>
                      </xsl:apply-templates>
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
