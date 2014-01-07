<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2006 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:exsl="http://exslt.org/common"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                exclude-result-prefixes="exsl">
  <xsl:import href="../common/output-message.xsl"/>
  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:output method="xml" encoding="utf-8" indent="no" />
  <!-- =========== DEFAULT VALUES FOR EXTERNALLY MODIFIABLE PARAMETERS =========== -->
  <!-- output type -->
  <xsl:param name="FINALOUTPUTTYPE" select="''"/>
  <xsl:param name="INPUTMAP" select="''"/>
  <xsl:param name="DITAEXT" select="'.xml'"/>
  <!-- Deprecated -->
  <xsl:param name="FILEREF">file://</xsl:param>
  <xsl:param name="WORKDIR">
    <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
  </xsl:param>
  <xsl:param name="include.rellinks" select="'#default parent child sibling friend next previous cousin ancestor descendant sample external other'"/>
  <xsl:variable name="include.roles" select="concat(' ', normalize-space($include.rellinks), ' ')"/>
  <xsl:variable name="file-prefix" select="$WORKDIR"/>
  <xsl:variable name="PATHTOMAP">
    <xsl:call-template name="GetPathToMap">
      <xsl:with-param name="inputMap" select="$INPUTMAP"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:variable name="DIRS-IN-MAP-PATH">
    <xsl:call-template name="countDirectoriesInPath">
      <xsl:with-param name="path" select="$PATHTOMAP"/>
    </xsl:call-template>
  </xsl:variable>  

  
  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  <!-- Start by creating the collection element for the map being processed. -->
  <xsl:template match="/*[contains(@class, ' map/map ')]">    
    <mapcollection>
      <xsl:apply-templates/>
    </mapcollection>
  </xsl:template>
    
  <!-- Get the relative path that leads to a file. Used to find path from a maplist to a map. -->
  <xsl:template name="getRelativePath">
    <xsl:param name="filename"/>
    <xsl:param name="currentPath"/>
    <xsl:choose>
      <xsl:when test="contains($filename,'/')">
        <xsl:call-template name="getRelativePath">
          <xsl:with-param name="filename" select="substring-after($filename,'/')"/>
          <xsl:with-param name="currentPath" select="concat($currentPath, substring-before($filename,'/'), '/')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($filename,'\')">
        <xsl:call-template name="getRelativePath">
          <xsl:with-param name="filename" select="substring-after($filename,'\')"/>
          <xsl:with-param name="currentPath" select="concat($currentPath, substring-before($filename,'\'), '/')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$currentPath"/></xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Match a topicref. Create all of the hierarchy links associated with the topicref. -->
  <xsl:template 
    match="*[@href][not(@href='')][not(@linking='none' or @linking='targetonly' or @scope='external' or @scope='peer' or @type='external')][not(@format) or @format='dita' or @format='DITA']">
    <!-- Href that points from this map to the topic this href references. -->
    <xsl:param name="pathFromMaplist"/>
    <xsl:variable name="use-href">
      <xsl:choose>
        <xsl:when test="@copy-to and (not(@format) or @format = 'dita') and not(contains(@chunk, 'to-content'))">
          <xsl:call-template name="simplifyLink">
            <xsl:with-param name="originalLink">
              <xsl:value-of select="@copy-to"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="simplifyLink">
            <xsl:with-param name="originalLink">
              <xsl:value-of select="@href"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="hrefFromOriginalMap">
      <xsl:call-template name="simplifyLink">
        <xsl:with-param name="originalLink">
          <xsl:value-of select="concat($pathFromMaplist, $use-href)"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    
    <!-- Path from the topic back to the map's directory (with map): for ref/abc.dita, will be "../" -->
    <xsl:variable name="pathBackToMapDirectory">
      <xsl:call-template name="pathBackToMapDirectory">
        <xsl:with-param name="path">
          <xsl:choose>
            <xsl:when test="contains($use-href,'#')">
              <xsl:value-of 
                select="substring-before($use-href,'#')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$use-href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
        <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- If going to print, and @print=no, do not create links for this topicref -->
    <xsl:if 
      test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no')">
      <xsl:variable name="newlinks">
        <maplinks href="{$hrefFromOriginalMap}">
          <xsl:apply-templates select="." mode="generate-all-links">
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
        </maplinks>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="number(system-property('xsl:version')) >= 2.0">
          <xsl:apply-templates select="$newlinks" mode="add-links-to-temp-file"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="exsl:node-set($newlinks)" mode="add-links-to-temp-file"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:apply-templates>
      <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- "add-links-to-temp-file" mode added with SF Bug 2573681  -->
  <!-- If <maplinks> has any links in the linklist or linkpool, -->
  <!-- then add it to the temp file.                            -->
  <xsl:template match="maplinks" mode="add-links-to-temp-file">
    <xsl:if test="*/*">
      <xsl:copy>
        <xsl:copy-of select="@*"/>
        <xsl:apply-templates mode="add-links-to-temp-file"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>
  <!-- Match the linklist or linkpool. If it has any children, add it to the temp file. -->
  <!-- If the linklist or linkpool are empty, they will not be added. -->
  <xsl:template match="*" mode="add-links-to-temp-file">
    <xsl:if test="*">
      <xsl:copy-of select="."/>
    </xsl:if>
  </xsl:template>
  
  <!-- Generate both unordered <linkpool> and ordered <linklist> links. -->
  <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="generate-all-links">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:apply-templates select="." mode="generate-ordered-links">
      <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
    </xsl:apply-templates>
    <xsl:apply-templates select="." mode="generate-unordered-links">
      <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Generated ordered links to friends (with linklist) -->
  <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="generate-ordered-links">
    <xsl:param name="pathBackToMapDirectory"/>
    <!--linklist class="- topic/linklist ">
      <xsl:copy-of select="@xtrf | @xtrc"/>
      <xsl:if test="/*[@id]">
        <xsl:attribute name="mapkeyref">
          <xsl:value-of select="/*/@id"/>
        </xsl:attribute>
      </xsl:if-->
      <xsl:apply-templates select="." mode="link-to-friends">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
        <xsl:with-param name="linklist">true</xsl:with-param>
      </xsl:apply-templates>
    <!--/linklist-->
  </xsl:template>

  <!-- Generate unordered links (with linkpool) -->
  <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="generate-unordered-links">
    <xsl:param name="pathBackToMapDirectory"/>
    <linkpool class="- topic/linkpool ">
      <xsl:copy-of select="@xtrf | @xtrc"/>
      <xsl:if test="/*[@id]">
        <xsl:attribute name="mapkeyref">
          <xsl:value-of select="/*/@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="." mode="link-from">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </linkpool>
  </xsl:template>

  <!-- To do: When XSLT 2.0 is a minimum requirement, do this again with hearty use of xsl:next-match. -->
  <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="link-from">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:if test="contains($include.roles, ' parent ')">
      <xsl:apply-templates select="." mode="link-to-parent">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:apply-templates select="." mode="link-to-prereqs">
      <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
    </xsl:apply-templates>
    <xsl:if test="contains($include.roles, ' sibling ')">
      <xsl:apply-templates select="." mode="link-to-siblings">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="contains($include.roles, ' next ') or contains($include.roles, ' previous')">
      <xsl:apply-templates select="." mode="link-to-next-prev">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="contains($include.roles, ' child ')">
      <xsl:apply-templates select="." mode="link-to-children">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="contains($include.roles, ' friend ')">
      <xsl:apply-templates select="." mode="link-to-friends">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
        <xsl:with-param name="linklist">false</xsl:with-param>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="contains($include.roles, ' other ')">
      <xsl:apply-templates select="." mode="link-to-other">
        <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>

  <!--parent-->
  <xsl:template match="*" mode="link-to-parent"/>
  <xsl:template match="*[contains(@class, ' map/topicref ')]
    [not(ancestor::*[contains(concat(' ', @chunk, ' '), ' to-content ')])]" mode="link-to-parent" name="link-to-parent">
    <xsl:param name="pathBackToMapDirectory"/>
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')][1]">
        <xsl:with-param name="role">parent</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
  </xsl:template>
  
  <!--prereqs - preceding with importance=required and in a sequence, but leaving the immediately previous one alone to avoid duplication with prev/next generation-->
  <xsl:template match="*" mode="link-to-prereqs"/>
  <xsl:template match="*[@collection-type='sequence']/*[contains(@class, ' map/topicref ')]
    [not(ancestor::*[contains(concat(' ', @chunk, ' '), ' to-content ')])]" mode="link-to-prereqs" name="link-to-prereqs">
    <xsl:param name="pathBackToMapDirectory"/>
        <xsl:apply-templates mode="link" 
          select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')][position()>1][@importance='required']">
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
  </xsl:template>
  
  <!--family-->
  <xsl:template match="*" mode="link-to-siblings"/>
  <xsl:template match="*[@collection-type='family']/*[contains(@class, ' map/topicref ')]
    [not(ancestor::*[contains(concat(' ', @chunk, ' '), ' to-content ')])]" mode="link-to-siblings" name="link-to-siblings">
    <xsl:param name="pathBackToMapDirectory"/>
        <xsl:apply-templates mode="link" 
          select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
          <xsl:with-param name="role">sibling</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
        <xsl:apply-templates mode="link" 
          select="following-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
          <xsl:with-param name="role">sibling</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
  </xsl:template>
  
  <!--next/prev-->
  <xsl:template match="*" mode="link-to-next-prev"/>
  <xsl:template match="*[@collection-type='sequence']/*[contains(@class, ' map/topicref ')]
    [not(ancestor::*[contains(concat(' ', @chunk, ' '), ' to-content ')])]" mode="link-to-next-prev" name="link-to-next-prev">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:if test="contains($include.roles, ' previous ')">
        <xsl:apply-templates mode="link" 
          select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')][1]">
          <xsl:with-param name="role">previous</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="contains($include.roles, ' next ')">
        <xsl:apply-templates mode="link" 
          select="following-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')][1]">
          <xsl:with-param name="role">next</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  
  <!--children-->
  <xsl:template match="*" mode="link-to-children"/>
  <xsl:template match="*[contains(@class, ' map/topicref ')]
    [not(ancestor-or-self::*[contains(concat(' ', @chunk, ' '), ' to-content ')])]" mode="link-to-children" name="link-to-children">
    <xsl:param name="pathBackToMapDirectory"/>
      <!--???TO DO: should be linking to appropriate descendants, not just children - ie grandchildren of eg topicgroup (non-href/non-title topicrefs) children-->
      <xsl:if 
        test="not(@processing-role='resource-only')
              and descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
        <linkpool class="- topic/linkpool ">
          <xsl:copy-of select="@xtrf | @xtrc | @collection-type"/>
          <xsl:apply-templates select="child::*[contains(@class, ' map/topicref ')]" mode="recusive">
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
          <!--xsl:apply-templates mode="link" 
            select="child::*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
            <xsl:with-param name="role">child</xsl:with-param>
            <xsl:with-param name="pathBackToMapDirectory" 
              select="$pathBackToMapDirectory"/>
          </xsl:apply-templates-->
        </linkpool>
      </xsl:if>
  </xsl:template>
  
  <xsl:template match="*" mode="recusive" name="recusive">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:choose>
      <xsl:when test="self::*[contains(@class, ' mapgroup-d/mapref ')][local-name()='topicref']">
        <xsl:apply-templates mode="link" 
          select="self::*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
          <xsl:with-param name="role">child</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="self::*[contains(@class, ' mapgroup-d/mapref ')]">
        <xsl:apply-templates mode="link" 
          select="self::*[contains(@class, ' mapgroup-d/mapref ')]/descendant::*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
          <xsl:with-param name="role">child</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="link" 
          select="self::*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
          <xsl:with-param name="role">child</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!--friends-->
  <xsl:template match="*" mode="link-to-friends"/>
  <xsl:template match="*[contains(@class, ' map/relcell ')]//*[contains(@class, ' map/topicref ')]"
    mode="link-to-friends" name="link-to-friends">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:param name="linklist">false</xsl:param>
    
    <xsl:variable name="temp-position">
      <xsl:apply-templates mode="get-position" select="ancestor::*[contains(@class, ' map/relcell ')]"/>
    </xsl:variable>
    <xsl:variable name="position">
      <xsl:value-of select="string-length($temp-position)"/>
    </xsl:variable>
    <xsl:variable name="group-title">
      <xsl:apply-templates mode="grab-group-title" 
         select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]"/>
    </xsl:variable>
    
    <xsl:if test="$linklist='true' and not($group-title='#none#') and not($group-title='')">
      <xsl:if test="ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
          <xsl:apply-templates mode="generate-ordered-links-2" 
            select="ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*[contains(@class, ' map/relcell ')]">
            <xsl:with-param name="role">friend</xsl:with-param>
            <xsl:with-param name="pathBackToMapDirectory" 
              select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
      </xsl:if>
      <xsl:if test="ancestor::*[contains(@class, ' map/relcell ')]/following-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
          <xsl:apply-templates mode="generate-ordered-links-2" 
            select="ancestor::*[contains(@class, ' map/relcell ')]/following-sibling::*[contains(@class, ' map/relcell ')]">
            <xsl:with-param name="role">friend</xsl:with-param>
            <xsl:with-param name="pathBackToMapDirectory" 
              select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
      </xsl:if>
      <xsl:if test="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]/*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">  
          <xsl:apply-templates mode="generate-ordered-links-2"
            select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]">
            <xsl:with-param name="role">friend</xsl:with-param>
            <xsl:with-param name="pathBackToMapDirectory" 
              select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
      </xsl:if>  
    </xsl:if>
    <xsl:if test="$linklist='false' and ($group-title='#none#' or $group-title='')">
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/relcell ')]/following-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="link"
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]/*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
    <!--
    <xsl:variable name="temp-position">
      <xsl:apply-templates mode="get-position" select="ancestor::*[contains(@class, ' map/relcell ')]"/>
    </xsl:variable>
    <xsl:variable name="position">
      <xsl:value-of select="string-length($temp-position)"/>
    </xsl:variable>
    <xsl:variable name="group-title">
      <xsl:apply-templates mode="grab-group-title" 
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]/*[contains(@class, ' map/topicref ')][1]"/>
    </xsl:variable>
    -->
    <!--xsl:if test="$linklist='true' and not($group-title='#none#') and not($group-title='')">
      <title class="- topic/title ">
        <xsl:value-of select="$group-title"/>
      </title>
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/relcell ')]/following-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="link"
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]/*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if>
    <xsl:if test="$linklist='false' and ($group-title='#none#' or $group-title='')">
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/relcell ')]/preceding-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="link" 
        select="ancestor::*[contains(@class, ' map/relcell ')]/following-sibling::*[contains(@class, ' map/relcell ')]/descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
      <xsl:apply-templates mode="link"
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]/*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory" 
          select="$pathBackToMapDirectory"/>
      </xsl:apply-templates>
    </xsl:if-->
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' map/relcolspec ')]/*[contains(@class, ' map/topicref ')]"
   mode="link-to-friends" name="link-to-subfriends">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:param name="linklist">false</xsl:param>
    <xsl:variable name="temp-position">
      <xsl:apply-templates mode="get-position"
       select="ancestor::*[contains(@class, ' map/relcolspec ')]"/>
    </xsl:variable>
    <xsl:variable name="position">
      <xsl:value-of select="string-length($temp-position)"/>
    </xsl:variable>
    <xsl:variable name="group-title">
      <xsl:apply-templates mode="grab-group-title" select="."/>
    </xsl:variable>
    <xsl:if test="$linklist='true' and not($group-title='#none#') and not($group-title='')">
    <linklist class="- topic/linklist ">
    <xsl:copy-of select="@xtrf | @xtrc"/>
    <xsl:if test="/*[@id]">
    <xsl:attribute name="mapkeyref">
    <xsl:value-of select="/*/@id"/>
    </xsl:attribute>
    </xsl:if>
    <title class="- topic/title ">
      <xsl:value-of select="$group-title"/>
    </title>
    <xsl:apply-templates mode="link"
      select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relrow ')]/*[contains(@class, ' map/relcell ')][position()=$position]//*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
      <xsl:with-param name="role">friend</xsl:with-param>
      <xsl:with-param name="pathBackToMapDirectory"
       select="$pathBackToMapDirectory">
      </xsl:with-param>
    </xsl:apply-templates>
    </linklist>
    </xsl:if>
    <xsl:if test="$linklist='false' and ($group-title='#none#' or $group-title='')">
      <xsl:apply-templates mode="link"
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relrow ')]/*[contains(@class, ' map/relcell ')][position()=$position]//*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
        <xsl:with-param name="role">friend</xsl:with-param>
        <xsl:with-param name="pathBackToMapDirectory"
          select="$pathBackToMapDirectory">
        </xsl:with-param>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  
  <!-- Get the position of current element -->
  <xsl:template match="*[contains(@class, ' map/relheader ') or contains(@class, ' map/relrow ')]/*" mode="get-position">
    <xsl:value-of select="'a'"/>
    <xsl:for-each select="preceding-sibling::*">
      <xsl:value-of select="'a'"/>
    </xsl:for-each>
  </xsl:template>
  
  
  <!-- Grab the group title from the matching header of reltable. -->
  <xsl:template match="*[contains(@class, ' map/relcolspec ')]"
    mode="grab-group-title"> 
    <xsl:choose>
      <xsl:when test="*[contains(@class, ' topic/title ')][not(title='')]">
        <xsl:value-of select="*[contains(@class, ' topic/title ')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates mode="grab-group-title" 
          select="*[contains(@class, ' map/topicref ')][1]"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>  
  
  <xsl:template match="*[contains(@class, ' map/topicref ')]"
   mode="grab-group-title">
    <xsl:variable name="file-origin">
      <xsl:call-template name="get-file-uri">
        <xsl:with-param name="href" select="@href"/>
        <xsl:with-param name="file-prefix" select="$file-prefix"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="file">
      <xsl:call-template name="replace-blank">
        <xsl:with-param name="file-origin">
          <xsl:value-of select="translate($file-origin,'\','/')"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' map/relcolspec ')]/*[contains(@class, ' topic/title ')][not(title='')]">
        <xsl:value-of select="parent::*[contains(@class, ' map/relcolspec ')]/*[contains(@class, ' topic/title ')]"/>
      </xsl:when>
      <xsl:when test="descendant::*[contains(@class,' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]">
        <xsl:value-of select="descendant::*[contains(@class,' map/topicmeta ')]/*[contains(@class, ' topic/navtitle ')]"/>
      </xsl:when>
      <xsl:when test="@navtitle and not(@navtitle='')">
        <xsl:value-of select="@navtitle"/>
      </xsl:when>
      <xsl:when test="document($file,/)//*[contains(@class, ' topic/title ')]">
        <xsl:value-of select="document($file,/)//*[contains(@class, ' topic/title ')][1]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'#none#'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Override this moded template to add your own kinds of links. -->
  <xsl:template match="*" mode="link-to-other"/>
  
      <!--xsl:template mode="link" 
      match="*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]"-->
  <xsl:template mode="generate-ordered-links-2" match="*[contains(@class, ' map/relcell ')]">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:variable name="temp-position">
      <xsl:apply-templates mode="get-position" select="."/>
    </xsl:variable>
    <xsl:variable name="position">
      <xsl:value-of select="string-length($temp-position)"/>
    </xsl:variable>
    <xsl:variable name="group-title">
      <xsl:apply-templates mode="grab-group-title" 
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]"/>
    </xsl:variable>
    <linklist class="- topic/linklist ">
      <xsl:copy-of select="@xtrf | @xtrc"/>
      <xsl:if test="/*[@id]">
        <xsl:attribute name="mapkeyref">
          <xsl:value-of select="/*/@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="not($group-title='#none#') and not($group-title='')">
        <title class="- topic/title ">
          <xsl:value-of select="$group-title"/>
        </title>
        <xsl:apply-templates mode="link" 
          select="descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
          <xsl:with-param name="role">friend</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates> 
      </xsl:if>
    </linklist>
  </xsl:template>
  
  <xsl:template mode="generate-ordered-links-2" match="*[contains(@class, ' map/relcolspec ')]">
    <xsl:param name="pathBackToMapDirectory"/>
    <xsl:variable name="temp-position">
      <xsl:apply-templates mode="get-position" select="."/>
    </xsl:variable>
    <xsl:variable name="position">
      <xsl:value-of select="string-length($temp-position)"/>
    </xsl:variable>
    <xsl:variable name="group-title">
      <xsl:apply-templates mode="grab-group-title" 
        select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][position()=$position]"/>
    </xsl:variable>
    <linklist class="- topic/linklist ">
      <xsl:copy-of select="@xtrf | @xtrc"/>
      <xsl:if test="/*[@id]">
        <xsl:attribute name="mapkeyref">
          <xsl:value-of select="/*/@id"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:if test="not($group-title='#none#') and not($group-title='')">
        <title class="- topic/title ">
          <xsl:value-of select="$group-title"/>
        </title>
        <xsl:apply-templates mode="link" 
          select="descendant::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
          <xsl:with-param name="role">friend</xsl:with-param>
          <xsl:with-param name="pathBackToMapDirectory" 
            select="$pathBackToMapDirectory"/>
        </xsl:apply-templates> 
      </xsl:if>
    </linklist>
  </xsl:template>
  
  <xsl:template mode="link" 
              match="*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][not(@processing-role='resource-only')]">
    <xsl:param name="role">#none#</xsl:param>
    <xsl:param name="otherrole">#none#</xsl:param>
    <xsl:param name="pathBackToMapDirectory"/>
          <!-- child found tag -->
          <xsl:param name="found">found</xsl:param>
    <!-- If going to print, and @print=no, do not create links for this topicref -->
          <!--xsl:if 
          test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no')"--> 

    <xsl:if 
              test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no') and 
              not(@processing-role='resource-only') and ($found='found')">
      <link class="- topic/link ">
        <xsl:if test="@class">
          <xsl:attribute name="mapclass"><xsl:value-of select="@class"/></xsl:attribute>
        </xsl:if>
        <xsl:copy-of 
          select="@type|@scope|@importance|@format|@platform|@product|@audience|@otherprops|@rev|@xtrf|@xtrc"/>        
        <xsl:attribute name="href">
          <xsl:choose>
            <xsl:when 
              test="starts-with(@href,'http://') or starts-with(@href,'/') or
                              starts-with(@href,'https://') or starts-with(@href,'ftp:/') or @scope='external'">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <!-- If the target has a copy-to value, link to that -->
            <xsl:when test="@copy-to and not(contains(@chunk, 'to-content'))">
              <xsl:call-template name="simplifyLink">
                <xsl:with-param name="originalLink">
                  <xsl:value-of select="concat($pathBackToMapDirectory, @copy-to)"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!--ref between two local paths - adjust normally-->
            <xsl:otherwise>
              <xsl:call-template name="simplifyLink">
                <xsl:with-param name="originalLink" select="concat($pathBackToMapDirectory, @href)"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="not($role='#none#')">
          <xsl:attribute name="role">
            <xsl:value-of select="$role"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="not($otherrole='#none#')">
          <xsl:attribute name="otherrole">
            <xsl:value-of select="$otherrole"/>
          </xsl:attribute>
        </xsl:if>
        <!--figure out the linktext and desc-->
        <xsl:if 
          test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]">
          <!--Do not output linktext when The final output type is PDF or IDD
            The target of the HREF is a local DITA file
            The user has not specified locktitle to override the title -->
          <xsl:if 
            test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and (not(@scope) or @scope='local') and (not(@format) or @format='dita' or @format='DITA') and (not(@locktitle) or @locktitle='no'))">
            <linktext class="- topic/linktext ">
              <xsl:copy-of select="*[contains(@class, ' map/topicmeta ')]/processing-instruction()[name()='ditaot'][.='usertext' or .='gentext']"/>
              <!-- xsl:value-of 
                select="normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')])"/ -->
              <xsl:copy-of
                select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')]/node()"/>
            </linktext>
          </xsl:if>
        </xsl:if>
        <xsl:if 
          test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]">
          <!--desc class="- topic/desc "-->
          <!-- add desc node and text -->
          <xsl:apply-templates select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]"/>
            <!-- xsl:value-of 
              select="normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')])"/-->
          <!-- /desc-->
        </xsl:if>
      </link>
    </xsl:if>

  </xsl:template>
  
  <!-- create a template to get child nodes and text -->
  <xsl:template match="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]" name="node">
       <!--xsl:copy-of select="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]"/-->
       <xsl:copy-of select="../processing-instruction()[name()='ditaot'][.='usershortdesc' or .='genshortdesc']"/>
       <desc class="- topic/desc ">
       	<!-- get child node and text -->
       	<xsl:copy-of select="node()"/>
       </desc>	
  </xsl:template>
  
  <!-- Make sure that pathFromMaplist parameter gets passed down -->
  <xsl:template match="*">
    <xsl:param name="pathFromMaplist"/>
    <xsl:apply-templates>
      <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' map/topicmeta ')]">
    <!--ignore topicmeta content when walking topicref/reltable tree - otherwise linktext content gets literally output-->
  </xsl:template>
  <xsl:template match="*[contains(@class, ' map/topicmeta ')]" mode="link">
    <!--ignore topicmeta content when walking topicref/reltable tree - otherwise linktext content gets literally output-->
  </xsl:template>
  
  <!-- Get the path to map by removing the last filename from the inputMap.
       e.g. inputMap is 'aaa/bbb/ccc.ditamap' , output will be 'aaa/bbb' -->
  <xsl:template name="GetPathToMap">
    <xsl:param name="inputMap"/>
    <xsl:choose>
      <xsl:when test="contains($inputMap,'\')">
        <xsl:variable name="newInputMap" select="substring-after($inputMap, '\')"/>
        <xsl:value-of select="substring-before($inputMap,'\')"/>
        <xsl:text>/</xsl:text>
        <xsl:call-template name="GetPathToMap">
          <xsl:with-param name="inputMap" 
            select="substring-after($inputMap, '\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($inputMap,'/')">
        <xsl:variable name="newInputMap" select="substring-after($inputMap, '/')"/>
        <xsl:value-of select="substring-before($inputMap,'/')"/>
        <xsl:text>/</xsl:text>
        <xsl:call-template name="GetPathToMap">
          <xsl:with-param name="inputMap" 
            select="substring-after($inputMap, '/')"/>
        </xsl:call-template>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <!-- Get the number of directories in the given path -->
  <xsl:template name="countDirectoriesInPath">
    <xsl:param name="path"/>
    <xsl:param name="currentCount">0</xsl:param>
    <xsl:choose>
      <xsl:when test="contains($path,'/')">
        <xsl:call-template name="countDirectoriesInPath">
          <xsl:with-param name="path" select="substring-after($path,'/')"/>
          <xsl:with-param name="currentCount" 
            select="number($currentCount + 1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($path,'\')">
        <xsl:call-template name="countDirectoriesInPath">
          <xsl:with-param name="path" select="substring-after($path,'\')"/>
          <xsl:with-param name="currentCount" 
            select="number($currentCount + 1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$currentCount"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Reduce links of the form "plugin-one/../plugin-two/a.html" to "plugin-two/a.html"
     This makes the removal of duplicate links more reliable. -->
  <xsl:template name="simplifyLink">
    <!-- Valid portion so far -->
    <xsl:param name="buildLink"/>
    <!-- Link being evaluated -->
    <xsl:param name="originalLink"/>
    
    <xsl:choose>
      <xsl:when test="contains($originalLink,'\')">
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="originalLink" select="concat(substring-before($originalLink,'\'), '/', substring-after($originalLink,'\'))"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="starts-with($originalLink,'./')">
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="buildLink" select="$buildLink"/>
          <xsl:with-param name="originalLink" select="substring-after($originalLink,'./')"/>
        </xsl:call-template>
      </xsl:when>      
      <xsl:when test="not(contains($originalLink,'../'))">
        <xsl:value-of select="concat($buildLink, $originalLink)"/>
      </xsl:when>
      <xsl:when test="starts-with($originalLink,'../')">
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="buildLink" select="concat($buildLink,'../')"/>
          <xsl:with-param name="originalLink" 
            select="substring-after($originalLink,'../')"/>
        </xsl:call-template>
      </xsl:when>
      <!-- If it starts with a directory followed by ../ then skip both and keep going. -->
      <xsl:when test="starts-with(substring-after($originalLink,'/'),'../')">
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="buildLink" select="$buildLink"/>
          <xsl:with-param name="originalLink" 
            select="substring-after($originalLink,'/../')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="simplifyLink">
          <xsl:with-param name="buildLink" 
            select="concat($buildLink,substring-before($originalLink,'/'),'/')"/>
          <xsl:with-param name="originalLink" 
            select="substring-after($originalLink,'/')"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <!-- Compute the path back to the input ditamap directory 
       base on the given path. -->
  <xsl:template name="pathBackToMapDirectory">
    <xsl:param name="path"/>
    <!-- Portion of the href that still needs to be evaluated -->
    <xsl:param name="back"/>
    <!-- Relpath builds up as we go; add ../ here each time a directory is removed -->
    <xsl:param name="pathFromMaplist" select="''"/>
    <xsl:choose>
      <!-- If the path starts with ../ do not add to $back -->
      <xsl:when test="starts-with($path,'../') or starts-with($path,'..\')">
        <xsl:choose>
          <!-- For links such as plugin-one/../plugin-two/ref/a.dita, we have already
             gone up one by the time we get here. We can skip the ../ jump, and remove
             one of the pathBackToMapDirectory values we've already added. -->
          <xsl:when test="string-length($back)>0 and starts-with($path,'../')">
            <xsl:call-template name="pathBackToMapDirectory">
              <xsl:with-param name="path" 
                select="substring-after($path,'../')"/>
              <xsl:with-param name="back" 
                select="substring-after($back,'../')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="string-length($back)>0 and starts-with($path,'..\')">
            <xsl:call-template name="pathBackToMapDirectory">
              <xsl:with-param name="path" 
                select="substring-after($path,'..\')"/>
              <xsl:with-param name="back" 
                select="substring-after($back,'../')"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="getPathBackToBase">
              <xsl:with-param name="path" select="$path"/>
              <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <!-- It contains forward slash and backslash. Remove the first directory, and add ../ to $back -->
      <xsl:when test="contains($path,'/') and contains($path,'\')">
        <xsl:if test="contains(substring-before($path,'/'),'\')">
          <xsl:call-template name="pathBackToMapDirectory">
            <xsl:with-param name="path" select="substring-after($path,'\')"/>
            <xsl:with-param name="back" 
              select="normalize-space(concat($back,'../'))"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:if test="contains(substring-before($path,'\'),'/')">
          <xsl:call-template name="pathBackToMapDirectory">
            <xsl:with-param name="path" select="substring-after($path,'/')"/>
            <xsl:with-param name="back" 
              select="normalize-space(concat($back,'../'))"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:when>
      <!-- It contains a directory, with only one type of slash; remove the first dir, add ../ to $back -->
      <xsl:when test="contains($path,'/')">
        <xsl:call-template name="pathBackToMapDirectory">
          <xsl:with-param name="path" select="substring-after($path,'/')"/>
          <xsl:with-param name="back" 
            select="normalize-space(concat($back,'../'))"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($path,'\')">
        <xsl:call-template name="pathBackToMapDirectory">
          <xsl:with-param name="path" select="substring-after($path,'\')"/>
          <xsl:with-param name="back" 
            select="normalize-space(concat($back,'../'))"/>
        </xsl:call-template>
      </xsl:when>
      <!-- When there are no more directories in $path, return the current value of $back -->
      <xsl:otherwise>
        <xsl:value-of select="$back"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- If an href in this map starts with ../ then find the path back to the map -->
  <xsl:template name="getPathBackToBase">
    <xsl:param name="path"/>
    <xsl:param name="pathFromMaplist"/>
    <!-- The href value -->
    <xsl:variable name="directoriesBack">
      <!-- Number of directories above the map that $path travels -->
      <xsl:call-template name="countRelpaths">
        <xsl:with-param name="path" select="$path"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="moveToBase">
      <!-- Path from the closest common ancestor, back to the base -->
      <xsl:call-template name="MoveBackToBase">
        <xsl:with-param name="saveDirs">
          <xsl:value-of select="$directoriesBack"/>
        </xsl:with-param>
        <xsl:with-param name="dirsLeft">
          <xsl:call-template name="countDirectoriesInPath">
            <xsl:with-param name="path" select="concat($PATHTOMAP,$pathFromMaplist)"/>
          </xsl:call-template>
        </xsl:with-param>
        <xsl:with-param name="remainingPath" select="concat($PATHTOMAP, $pathFromMaplist)"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="pathWithoutRelPaths">
      <!-- Path from the common ancestor, to the target file -->
      <xsl:call-template name="removeRelPaths">
        <xsl:with-param name="path" select="$path"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="backToCommon">
      <!-- Path from the target file, to the common ancestor -->
      <xsl:call-template name="pathBackToMapDirectory">
        <xsl:with-param name="path" select="$pathWithoutRelPaths"/>
      </xsl:call-template>
    </xsl:variable>
    <!-- Now, to get from the target file to any other: it must go up until it hits the common dir.
       Then, it must travel back to the base directory containing the map. At that point, this
       path can be placed in front of any referenced topic, and it will get us to the right spot. -->
    <xsl:value-of select="concat($backToCommon, $moveToBase)"/>
  </xsl:template>  
  
  <!-- Count the number of paths removed from the base (1 for each ../ or ..\ at the start of the href) -->
  <xsl:template name="countRelpaths">
    <xsl:param name="path"/>
    <xsl:param name="currentCount">0</xsl:param>
    <xsl:choose>
      <xsl:when test="starts-with($path,'../')">
        <xsl:call-template name="countRelpaths">
          <xsl:with-param name="path" select="substring-after($path,'../')"/>
          <xsl:with-param name="currentCount" select="number($currentCount+1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="starts-with($path,'..\')">
        <xsl:call-template name="countRelpaths">
          <xsl:with-param name="path" select="substring-after($path,'..\')"/>
          <xsl:with-param name="currentCount" select="number($currentCount+1)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$currentCount"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Get the path from the common ancestor to the basedir of the input map -->
  <xsl:template name="MoveBackToBase">
    <xsl:param name="saveDirs"/>
    <xsl:param name="dirsLeft">
      <xsl:value-of select="$DIRS-IN-MAP-PATH"/>
    </xsl:param>
    <xsl:param name="remainingPath">
      <xsl:value-of select="$PATHTOMAP"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$saveDirs>=$dirsLeft">
        <xsl:value-of select="$remainingPath"/>
      </xsl:when>
      <xsl:when test="contains($remainingPath,'/')">
        <xsl:call-template name="MoveBackToBase">
          <xsl:with-param name="saveDirs" select="number($saveDirs)"/>
          <xsl:with-param name="dirsLeft" select="number($dirsLeft - 1)"/>
          <xsl:with-param name="remainingPath" 
            select="substring-after($remainingPath,'/')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="contains($remainingPath,'\')">
        <xsl:call-template name="MoveBackToBase">
          <xsl:with-param name="saveDirs" select="number($saveDirs)"/>
          <xsl:with-param name="dirsLeft" select="number($dirsLeft - 1)"/>
          <xsl:with-param name="remainingPath" 
            select="substring-after($remainingPath,'\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$remainingPath"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>  
  
  <!-- Remove the ../ or ..\ relpaths from the start of a path. The remainder can then be evaluated. -->
  <xsl:template name="removeRelPaths">
    <xsl:param name="path"/>
    <xsl:choose>
      <xsl:when test="starts-with($path,'../')">
        <xsl:call-template name="removeRelPaths">
          <xsl:with-param name="path" select="substring-after($path,'../')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="starts-with($path,'..\')">
        <xsl:call-template name="removeRelPaths">
          <xsl:with-param name="path" select="substring-after($path,'..\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$path"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Do nothing when meet the title -->
  <xsl:template match="*[contains(@class, ' topic/title ')]"/>
  
  <xsl:template name="get-file-uri">
    <xsl:param name="href"/>
    <xsl:param name="file-prefix"/>
    <xsl:value-of select="$file-prefix"/>    
    <xsl:choose>
      <xsl:when test="contains($href,'#')">
        <xsl:value-of select="substring-before($href,'#')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$href"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>
