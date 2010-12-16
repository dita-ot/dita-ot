<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created 2008 by Robert D. Anderson -->

<!-- ***************************************
     This file is a sample of how to update customize.xsl.
     *************************************** -->

<!--
     This file should be used to customize the output
     from other iTunes -> DITA xsl scripts. It allows
     you to:
     *) Determine which playlists appear in your playlist DITAMAP (default: none)
     *) Exclude any specific song from the generated DITA
     *) Exclude any specific album from the generated DITA
     *) Exclude any specific artist from the generated DITA
     *) Customize generated topicref elements (to add @otherprops, or something else)
  -->

<xsl:stylesheet version="2.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  

  <!-- Set this to 'yes' or 'no' to determine whether all playlists
       should be included by default. -->
  <xsl:param name="INCLUDE-PLAYLISTS-BY-DEFAULT" select="'no'"/>

  <xsl:template match="string" mode="evaluatePlaylist">
    <xsl:choose>
      <!-- Add a rule for each playlist you want to share. -->
      <xsl:when test=".='Icelandic Moon Rising'">yes</xsl:when>
      <xsl:when test=".='2007 Year in Review'">yes</xsl:when>
      <xsl:when test=".='2008 Year In Review: ARE YOU READY FOR WALTZ?'">yes</xsl:when>
      <xsl:when test=".='2008 Bonus Disc: Something Going On Upstairs'">yes</xsl:when>
      <xsl:when test=".='Xmas2006'">yes</xsl:when>
      <xsl:when test=".='Lesson'">yes</xsl:when>
      <xsl:when test=".='Nordic Roots'">yes</xsl:when>
      <xsl:otherwise><xsl:value-of select="$INCLUDE-PLAYLISTS-BY-DEFAULT"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- The following templates should be updated to include any
       album, artist, or single song that SO embarrasses you
       that you do not want it listed in the generated content.
       For each item to remove, return 'yes'. -->
  <xsl:template match="*" mode="hideThisArtist">
    <xsl:choose>
      <xsl:when test="@artist='Joe the Singer'">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="hideThisAlbum">
    <xsl:choose>
      <xsl:when test="@album='Oscar the Grouch Sings the Hits'">yes</xsl:when>
      <xsl:otherwise>no</xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="hideThisSong">
    <xsl:choose>
      <xsl:when test="@name='Kumbaya'">yes</xsl:when>
      <xsl:otherwise>
        <xsl:variable name="hideArtist"><xsl:apply-templates select="." mode="hideThisArtist"/></xsl:variable>
        <xsl:variable name="hideAlbum"><xsl:apply-templates select="." mode="hideThisAlbum"/></xsl:variable>
        <xsl:choose>
          <xsl:when test="$hideArtist='yes' or $hideAlbum='yes'">yes</xsl:when>
          <xsl:otherwise>no</xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:key name="cedaralbums"
           match="/plist/dict/dict[1]/dict[contains(concat(' ',@grouping,' '),' Cedar ')]"
           use="@album"/>
  <xsl:key name="cedarartists"
           match="/plist/dict/dict[1]/dict[contains(concat(' ',@grouping,' '),' Cedar ')]"
           use="@artist"/>

  <!-- The following templates are called when creating a topicref element.
       It is a hook that allows one to place an otherprops attribute,
       or any other attribute really, onto the topicref. -->

  <!-- This sample customization will set otherprops="cedar" when
       the grouping attribute for any track by an artist includes "Cedar". -->
  <xsl:template match="dict" mode="setCustomTopicrefAttrs-artist">
    <xsl:if test="key('cedarartists',@artist)">
      <xsl:attribute name="otherprops">Cedar</xsl:attribute>
    </xsl:if>
  </xsl:template>
  <!-- This sample customization will set otherprops="cedar" when
       the grouping attribute for any track on an album includes "Cedar". -->
  <xsl:template match="dict" mode="setCustomTopicrefAttrs-album">
    <xsl:if test="key('cedaralbums',@album)">
      <xsl:attribute name="otherprops">Cedar</xsl:attribute>
    </xsl:if>
  </xsl:template>


</xsl:stylesheet>