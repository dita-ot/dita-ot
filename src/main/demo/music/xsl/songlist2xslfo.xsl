<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- Copyright IBM Corporation 2010. All Rights Reserved. -->
<!-- Originally created Oct. 2009 by Robert D. Anderson -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
    version="1.0"
    exclude-result-prefixes="dita2xslfo">

  <!-- Music strings are already integrated in to the core OT set of strings,
       which are not used by PDF2. This import pulls in the default OT
       string handling, which allows getString to work. This requires the
       music plug-in to be nested 2 directories inside the DITA-OT base directory. -->
  <xsl:import href="../../../xsl/common/dita-utilities.xsl"/>

  <xsl:template match="*[contains(@class,' songCollection/songList ')]" mode="dita2xslfo:simpletable-heading">
    <fo:table-header xsl:use-attribute-sets="sthead" id="{@id}-header">
        <fo:table-row xsl:use-attribute-sets="sthead__row">
          <xsl:if test="*/*[contains(@class,' songCollection/song ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Song'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="*/*[contains(@class,' songCollection/album ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Album'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="*/*[contains(@class,' songCollection/artist ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Artist'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="*/*[contains(@class,' songCollection/genre ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Genre'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="*/*[contains(@class,' songCollection/rating ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Rating'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="*/*[contains(@class,' songCollection/count ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Count'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
          <xsl:if test="*/*[contains(@class,' songCollection/playdate ')]">
            <xsl:call-template name="add-music-table-header">
              <xsl:with-param name="column-header">
                <xsl:call-template name="getString">
                  <xsl:with-param name="stringName" select="'Play date'"/>
                </xsl:call-template>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:if>
        </fo:table-row>
    </fo:table-header>
  </xsl:template>

  <xsl:template name="add-music-table-header">
    <xsl:param name="column-header"/>
    <fo:table-cell xsl:use-attribute-sets="sthead.stentry">
        <xsl:variable name="entryCol" select="count(preceding-sibling::*[contains(@class, ' topic/stentry ')]) + 1"/>
        <xsl:variable name="frame" select="@frame"/>

        <xsl:call-template name="generateSimpleTableHorizontalBorders">
            <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
        <xsl:call-template name="generateSimpleTableVerticalBorders">
            <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
        <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <xsl:if test="($frame = 'all') or ($frame = 'topbot') or ($frame = 'sides') or not($frame)">
            <xsl:call-template name="processAttrSetReflection">
                <xsl:with-param name="attrSet" select="'__tableframe__left'"/>
                <xsl:with-param name="path" select="$tableAttrs"/>
            </xsl:call-template>
        </xsl:if>
        <fo:block>
          <fo:inline xsl:use-attribute-sets="b">
            <xsl:value-of select="$column-header"/>
          </fo:inline>
        </fo:block>
    </fo:table-cell>
  </xsl:template>


  <xsl:template match="*[contains(@class,' songCollection/songRow ')]">
     <fo:table-row xsl:use-attribute-sets="sthead__row" id="{@id}">
       <xsl:apply-templates select="*[contains(@class,' songCollection/song ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/song ')]) and ../*/*[contains(@class,' songCollection/song ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
       <xsl:apply-templates select="*[contains(@class,' songCollection/album ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/album ')]) and ../*/*[contains(@class,' songCollection/album ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
       <xsl:apply-templates select="*[contains(@class,' songCollection/artist ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/artist ')]) and ../*/*[contains(@class,' songCollection/artist ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
       <xsl:apply-templates select="*[contains(@class,' songCollection/genre ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/genre ')]) and ../*/*[contains(@class,' songCollection/genre ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
       <xsl:apply-templates select="*[contains(@class,' songCollection/rating ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/rating ')]) and ../*/*[contains(@class,' songCollection/rating ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
       <xsl:apply-templates select="*[contains(@class,' songCollection/count ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/count ')]) and ../*/*[contains(@class,' songCollection/count ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
       <xsl:apply-templates select="*[contains(@class,' songCollection/playdate ')]"/>
       <xsl:if test="not(*[contains(@class,' songCollection/playdate ')]) and ../*/*[contains(@class,' songCollection/playdate ')]">
         <xsl:apply-templates select="." mode="fillInMissingSimpletableCells">
             <xsl:with-param name="fill-in-count" select="1"/>
         </xsl:apply-templates>
       </xsl:if>
     </fo:table-row>
  </xsl:template>

</xsl:stylesheet>
