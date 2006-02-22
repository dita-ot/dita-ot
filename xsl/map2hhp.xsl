<?xml version="1.0"?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--
     Conversion from DITA map to HTML Help project.
     Input = one DITA map file
     Output = one HHP project file for use with the HTML Help compiler.

     Options:
        /OUTEXT  = XHTML output extension (default is '.html')
        /WORKDIR = The working directory that contains the document being transformed.
                   Needed as a directory prefix for the @href "document()" function calls.
                   Default is './'
        /HHCNAME = The name of the contents file associated with this help project
        /HELPALIAS = adds the ALIAS header & the #include
        /HELPMAP = adds the MAP header & the #include


-->


<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns:saxon="http://icl.com/saxon"
                xmlns:xalanredirect="org.apache.xalan.xslt.extensions.Redirect"
                xmlns:exsl="http://exslt.org/common"
                extension-element-prefixes="saxon xalanredirect exsl">

<!-- Include error message template -->
<xsl:include href="common/output-message.xsl"/>

<xsl:output method="text"/>

<!-- Set the prefix for error message numbers -->
<xsl:variable name="msgprefix">DOTX</xsl:variable>

<!-- *************************** Command line parameters *********************** -->
<xsl:param name="OUTEXT" select="'.html'"/>
<xsl:param name="WORKDIR" select="'./'"/>
<xsl:param name="HHCNAME" select="'help'"/>
<xsl:param name="USEINDEX" select="'yes'"/>  <!-- to turn on, use 'yes' -->
<xsl:param name="HELPALIAS" />
<xsl:param name="HELPMAP" />
<xsl:param name="DITAEXT" select="'.xml'"/>


<!-- Is there a way to prevent re-issuing the same filename, using keys? Doubt it... -->
<!-- <xsl:key name="amap" match="topicref" use="@href"/>
<xsl:key name="manymaps" match="map/document($WORKDIR@file)//topicref" use="@href"/> -->

<!-- *********************************************************************************
     Template to set up the HHP file. It should only be called once; it sets
     standard HHP options. The complex sections set the default topic that shows
     when you open the file, and the title of the HTML Help file. The default topic
     is the first topic used for navigation in the first processed map; the title is
     the title of the first map. If the first (or only) map does not have a title, none
     is used. NOTE - only non-external references to DITA or HTM/HTML files are
     considered valid for inclusion in the project, so only those will be evaluated to
     find the default topic.

     TBD: Need to figure out a way to set the language for non-English output?
     ********************************************************************************* -->
<xsl:template name="setup-options">
<xsl:text>[OPTIONS]
Compiled file=</xsl:text><xsl:value-of select="substring-before($HHCNAME,'.hhc')"/><xsl:text>.chm
</xsl:text>
<xsl:if test="/*[contains(@class, ' map/map ')]">   <!-- Only reference HHC if there is valid navigation -->
  <xsl:text>Contents file=</xsl:text><xsl:value-of select="$HHCNAME"/><xsl:text>
</xsl:text>
</xsl:if>
<xsl:text>Default Window=default
Full-text search=Yes
Display compile progress=No
</xsl:text>
<xsl:if test="$USEINDEX='yes'">
<xsl:text>Index file=</xsl:text><xsl:value-of select="substring-before($HHCNAME,'.hhc')"/><xsl:text>.hhk
Binary Index=No
</xsl:text>
</xsl:if>
<xsl:text>Language=0x409 English (United States)
Default topic=</xsl:text>
<!-- in a single map, get the first valid topic -->
<xsl:text/><xsl:apply-templates select="descendant::*[contains(@class, ' map/topicref ')][@href][contains(@href,$DITAEXT) or contains(@href,'.htm')][not(contains(@toc,'no'))][1]" mode="defaulttopic"/><xsl:text/>

<!-- Get the title, if possible -->
<!-- Using a single map, so get the title from that map -->
<xsl:if test="/*[contains(@class, ' map/map ')]/@title">
  <xsl:text>Title=</xsl:text><xsl:value-of select="/*[contains(@class, ' map/map ')]/@title"/>
</xsl:if>
</xsl:template>


<!-- *********************************************************************************
     Output the list of files that will be included in the output.
     ********************************************************************************* -->
<xsl:template name="output-filenames">
  <!-- Place all of the file names in a temp file. Then process the temp file,
       removing dupliates. -->
  <xsl:variable name="temp">
    <filelist>
      <xsl:apply-templates/>
    </filelist>
  </xsl:variable>
    
<xsl:text>

[FILES]
</xsl:text>
  <xsl:apply-templates select="exsl:node-set($temp)/filelist/*" mode="tempfile">
    <xsl:sort select="@href"/>
  </xsl:apply-templates>
</xsl:template>

<!-- *********************************************************************************
     If asked, create the alias and map sections.
     The HTML Help program creates an empty [INFOTYPES] tag at the bottom of each
     project, so we will also create one.
     ********************************************************************************* -->
<xsl:template name="end-hhp">
<xsl:if test="string-length($HELPALIAS)>0">
<xsl:text>
[ALIAS]
#include </xsl:text><xsl:value-of select="$HELPALIAS"/><xsl:text>
</xsl:text>
</xsl:if>
<xsl:if test="string-length($HELPMAP)>0">
<xsl:text>
[MAP]
#include </xsl:text><xsl:value-of select="$HELPMAP"/><xsl:text>
</xsl:text>
</xsl:if>
<xsl:text>

[INFOTYPES]

</xsl:text>
</xsl:template>

<!-- *********************************************************************************
     Set up the HHP file, and send filenames to the proper section.
     ********************************************************************************* -->
<xsl:template match="/">
  <xsl:call-template name="setup-options"/>
  <xsl:call-template name="output-filenames"/>
  <xsl:call-template name="end-hhp"/>
</xsl:template>

<!-- *********************************************************************************
     If this is one map from a list, process the contents. Otherwise, output the HHP
     wrapper around the contents. When the contents are processed, they will generate
     a list of all XHTML files referenced by this map.
     ********************************************************************************* -->
<xsl:template match="/*[contains(@class, ' map/map ')]">
  <xsl:param name="pathFromMaplist"/>
  <xsl:apply-templates>
    <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
  </xsl:apply-templates>
</xsl:template>

<!-- *********************************************************************************
     If this topic should be included in navigation, output the referenced file,
     and process the children; otherwise, skip the topicref. Topics are considered
     invalid when @scope=external, or when the href does not point to a DITA or HTML file.
     ********************************************************************************* -->
<xsl:template match="*[contains(@class, ' map/topicref ')]">
  <xsl:param name="pathFromMaplist"/>
  <xsl:variable name="thisFilename">
    <xsl:if test="@href and not ((ancestor-or-self::*/@type)[last()]='external') and not((ancestor-or-self::*/@scope)[last()]='external')">
      <xsl:choose>
        <!-- For dita files, change the extension; for HTML files, output the name as-is. Use the copy-to value first. -->
        <xsl:when test="contains(@copy-to,$DITAEXT)"><xsl:value-of select="$pathFromMaplist"/><xsl:value-of select="substring-before(@copy-to,$DITAEXT)"/><xsl:value-of select="$OUTEXT"/></xsl:when>
        <xsl:when test="contains(@href,$DITAEXT)"><xsl:value-of select="$pathFromMaplist"/><xsl:value-of select="substring-before(@href,$DITAEXT)"/><xsl:value-of select="$OUTEXT"/></xsl:when>
        <!-- For local HTML files, add any path from the maplist -->
        <xsl:when test="contains(@href,'.htm') and not(@scope='external')"><xsl:value-of select="$pathFromMaplist"/><xsl:value-of select="@href"/></xsl:when>
        <xsl:when test="contains(@href,'.htm')"><xsl:value-of select="$pathFromMaplist"/><xsl:value-of select="@href"/></xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>
  <xsl:if test="string-length($thisFilename)>0">
    <file>
      <xsl:attribute name="href">
        <xsl:call-template name="removeExtraRelpath">
          <xsl:with-param name="remainingPath" select="$thisFilename"/>
        </xsl:call-template>
      </xsl:attribute>
    </file>
  </xsl:if>
  <xsl:apply-templates select="*[contains(@class, ' map/topicref ')]">
    <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
  </xsl:apply-templates>
</xsl:template>

<!-- *********************************************************************************
     Process the default topic for this HHP file to get the filename. Same as above,
     except that we know @href is specified, and we do not process children.
     ********************************************************************************* -->
<xsl:template match="*[contains(@class, ' map/topicref ')]" mode="defaulttopic">
  <xsl:param name="pathFromMaplist"/>
  <xsl:choose>
    <!-- If copy-to is specified, that copy should be used in place of the original -->
    <xsl:when test="contains(@copy-to,$DITAEXT)">
      <xsl:if test="not(@scope='external')"><xsl:value-of select="$pathFromMaplist"/></xsl:if>
      <xsl:value-of select="substring-before(@copy-to,$DITAEXT)"/><xsl:value-of select="$OUTEXT"/><xsl:text>
</xsl:text></xsl:when>
    <!-- For dita files, change the extension to OUTEXT -->
    <xsl:when test="contains(@href,$DITAEXT)">
      <xsl:if test="not(@scope='external')"><xsl:value-of select="$pathFromMaplist"/></xsl:if>
      <xsl:value-of select="substring-before(@href,$DITAEXT)"/><xsl:value-of select="$OUTEXT"/><xsl:text>
</xsl:text></xsl:when>
    <!-- For local HTML files, add any path from the maplist -->
    <xsl:when test="contains(@href,'.htm') and not(@scope='external')">
      <xsl:value-of select="$pathFromMaplist"/><xsl:value-of select="@href"/><xsl:text>
</xsl:text></xsl:when>
    <!-- For external HTML files, output the name as-is -->
    <xsl:when test="contains(@href,'.htm')"><xsl:value-of select="@href"/><xsl:text>
</xsl:text></xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class, ' map/reltable ')]">
  <xsl:param name="pathFromMaplist"/>
  <xsl:apply-templates select="*[contains(@class, ' map/relrow ')]/*[contains(@class, ' map/relcell ')]/*[contains(@class, ' map/topicref ')]">
    <xsl:with-param name="pathFromMaplist" select="$pathFromMaplist"/>
  </xsl:apply-templates>
</xsl:template>

<!-- Process the temp file that creates each name; remove duplicates -->
<xsl:template match="/filelist/file" mode="tempfile">
  <xsl:variable name="testhref" select="@href"/>
  <xsl:if test="not(preceding-sibling::*[@href=$testhref])">
    <xsl:value-of select="@href"/><xsl:text>
</xsl:text>
  </xsl:if>
</xsl:template>

<!-- These are here just to prevent accidental fallthrough -->
<xsl:template match="*[contains(@class, ' map/navref ')]"/>
<xsl:template match="*[contains(@class, ' map/anchor ')]"/>
<xsl:template match="*[contains(@class, ' map/topicmeta ')]"/>
<xsl:template match="text()"/>

<xsl:template match="*">
  <xsl:apply-templates/>
</xsl:template>

<!-- Template to get the relative path to a map -->
<xsl:template name="getRelativePath">
  <xsl:param name="remainingPath" select="@file"/>
  <xsl:choose>
    <xsl:when test="contains($remainingPath,'/')">
      <xsl:value-of select="substring-before($remainingPath,'/')"/><xsl:text>/</xsl:text>
      <xsl:call-template name="getRelativePath">
        <xsl:with-param name="remainingPath" select="substring-after($remainingPath,'/')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="contains($remainingPath,'\')">
      <xsl:value-of select="substring-before($remainingPath,'\')"/><xsl:text>/</xsl:text>
      <xsl:call-template name="getRelativePath">
        <xsl:with-param name="remainingPath" select="substring-after($remainingPath,'\')"/>
      </xsl:call-template>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!-- Remove extra relpaths (as in abc/../def) -->
<xsl:template name="removeExtraRelpath">
  <xsl:param name="remainingPath"><xsl:value-of select="@href"/></xsl:param>
  <xsl:choose>
    <xsl:when test="not(contains($remainingPath,'../'))"><xsl:value-of select="$remainingPath"/></xsl:when>
    <xsl:when test="not(starts-with($remainingPath,'../')) and
                    starts-with(substring-after($remainingPath,'/'),'../')">
      <xsl:call-template name="removeExtraRelpath">
        <xsl:with-param name="remainingPath" select="substring-after($remainingPath,'../')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:when test="contains($remainingPath,'/')">
      <xsl:value-of select="substring-before($remainingPath,'/')"/>/<xsl:text/>
      <xsl:call-template name="removeExtraRelpath">
        <xsl:with-param name="remainingPath" select="substring-after($remainingPath,'/')"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="$remainingPath"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
