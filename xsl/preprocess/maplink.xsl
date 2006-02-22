<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:import href="../common/output-message.xsl"/>
  <xsl:output method="xml" encoding="utf-8" indent="no" />
  <!-- =========== DEFAULT VALUES FOR EXTERNALLY MODIFIABLE PARAMETERS =========== -->
  <!-- output type -->
  <xsl:param name="FINALOUTPUTTYPE" select="''"/>
  <xsl:param name="INPUTMAP" select="''"/>
  <xsl:param name="DITAEXT" select="'.xml'"/>
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
  <!-- Match a topicref. Create all of the hierarchy links associated with the topicref. -->
  <xsl:template 
    match="*[@href][not(@href='')][not(@linking='none' or @linking='targetonly' or @scope='external' or @scope='peer' or @type='external')][not(@format) or @format='dita' or @format='DITA']">
    <!-- Href that points from this map to the topic this href references. -->
    <xsl:variable name="hrefFromOriginalMap">
      <xsl:choose>
        <xsl:when test="@copy-to and contains(@copy-to,$DITAEXT)">
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
    <!-- Path from the topic back to the map's directory (with map): for ref/abc.dita, will be "../" -->
    <xsl:variable name="pathBackToMapDirectory">
      <xsl:call-template name="pathBackToMapDirectory">
        <xsl:with-param name="path">
          <xsl:choose>
            <xsl:when test="contains($hrefFromOriginalMap,'#')">
              <xsl:value-of 
                select="substring-before($hrefFromOriginalMap,'#')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$hrefFromOriginalMap"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!-- If going to print, and @print=no, do not create links for this topicref -->
    <xsl:if 
      test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no')">
      <maplinks href="{$hrefFromOriginalMap}">
        <linkpool class="- topic/linkpool ">
          <xsl:if test="@xtrf">
            <xsl:attribute name="xtrf">
              <xsl:value-of select="@xtrf"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@xtrc">
            <xsl:attribute name="xtrc">
              <xsl:value-of select="@xtrc"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="/*[@id]">
            <xsl:attribute name="mapkeyref">
              <xsl:value-of select="/*/@id"/>
            </xsl:attribute>
          </xsl:if>
          <!--parent-->
          <xsl:apply-templates mode="link" 
            select="ancestor::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][1]">
            <xsl:with-param name="role">parent</xsl:with-param>
            <xsl:with-param name="pathBackToMapDirectory" 
              select="$pathBackToMapDirectory"/>
          </xsl:apply-templates>
          <!--prereqs - preceding with importance=required and in a sequence, but leaving the immediately previous one alone to avoid duplication with prev/next generation-->
          <xsl:if test="parent::*[@collection-type='sequence']">
            <xsl:apply-templates mode="link" 
              select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][position()>1][@importance='required']">
              <xsl:with-param name="pathBackToMapDirectory" 
                select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
          </xsl:if>
          <!--family-->
          <xsl:if test="parent::*[@collection-type='family']">
            <xsl:apply-templates mode="link" 
              select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
              <xsl:with-param name="role">sibling</xsl:with-param>
              <xsl:with-param name="pathBackToMapDirectory" 
                select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
            <xsl:apply-templates mode="link" 
              select="following-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
              <xsl:with-param name="role">sibling</xsl:with-param>
              <xsl:with-param name="pathBackToMapDirectory" 
                select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
          </xsl:if>
          <!--next/prev-->
          <xsl:if test="parent::*[@collection-type='sequence']">
            <xsl:apply-templates mode="link" 
              select="preceding-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][1]">
              <xsl:with-param name="role">previous</xsl:with-param>
              <xsl:with-param name="pathBackToMapDirectory" 
                select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
            <xsl:apply-templates mode="link" 
              select="following-sibling::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')][1]">
              <xsl:with-param name="role">next</xsl:with-param>
              <xsl:with-param name="pathBackToMapDirectory" 
                select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
          </xsl:if>
          <!--children-->
          <!--???TO DO: should be linking to appropriate descendants, not just children - ie grandchildren of eg topicgroup (non-href/non-title topicrefs) children-->
          <xsl:if 
            test="child::*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
            <linkpool class="- topic/linkpool ">
              <xsl:if test="@xtrf">
                <xsl:attribute name="xtrf">
                  <xsl:value-of select="@xtrf"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="@xtrc">
                <xsl:attribute name="xtrc">
                  <xsl:value-of select="@xtrc"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="@collection-type">
                <xsl:attribute name="collection-type">
                  <xsl:value-of select="@collection-type"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:apply-templates mode="link" 
                select="*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
                <xsl:with-param name="role">child</xsl:with-param>
                <xsl:with-param name="pathBackToMapDirectory" 
                  select="$pathBackToMapDirectory"/>
              </xsl:apply-templates>
            </linkpool>
          </xsl:if>
          <!--friends-->
          <xsl:if test="ancestor::*[contains(@class, ' map/relcell ')]">
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
          </xsl:if>
        </linkpool>
      </maplinks>
    </xsl:if>
    <xsl:apply-templates />
  </xsl:template>
  <xsl:template mode="link" 
    match="*[@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]">
    <xsl:param name="role">#none#</xsl:param>
    <xsl:param name="pathBackToMapDirectory"/>
    <!-- If going to print, and @print=no, do not create links for this topicref -->
    <xsl:if 
      test="not(($FINALOUTPUTTYPE='PDF' or $FINALOUTPUTTYPE='IDD') and @print='no')">
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
                          starts-with(@href,'https://') or starts-with(@href,'ftp:/')">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <!-- If the target has a copy-to value, link to that -->
            <xsl:when test="@copy-to">
              <xsl:call-template name="simplifyLink">
                <xsl:with-param name="originalLink">
                  <xsl:value-of select="$pathBackToMapDirectory"/><xsl:value-of select="@copy-to"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <!--ref between two local paths - adjust normally-->
            <xsl:otherwise>
              <xsl:call-template name="simplifyLink">
                <xsl:with-param name="originalLink"><xsl:value-of 
                  select="$pathBackToMapDirectory"/><xsl:value-of 
                  select="@href"/></xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:if test="not($role='#none#')">
          <xsl:attribute name="role">
            <xsl:value-of select="$role"/>
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
              <xsl:value-of 
                select="normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/linktext ')])"/>
            </linktext>
          </xsl:if>
        </xsl:if>
        <xsl:if 
          test="*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')]">
          <desc class="- topic/desc ">
            <xsl:value-of 
              select="normalize-space(*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' map/shortdesc ')])"/>
          </desc>
        </xsl:if>
      </link>
    </xsl:if>
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
          <xsl:with-param name="originalLink"> <xsl:value-of 
            select="substring-before($originalLink,'\')"/>/<xsl:value-of 
            select="substring-after($originalLink,'\')"/> </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="not(contains($originalLink,'../'))">
        <xsl:value-of select="$buildLink"/>
        <xsl:value-of select="$originalLink"/>
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
    <xsl:value-of select="$backToCommon"/><xsl:value-of select="$moveToBase"/>
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
  
</xsl:stylesheet>
