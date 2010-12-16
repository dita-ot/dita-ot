<?xml version="1.0" encoding="UTF-8" ?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<xsl:param name="APIREFCSS" select="'ibmapiref.css'"/>
<xsl:param name="APIREFCSSRTL" select="'ibmapirefrtl.css'"/>

<xsl:param name="FILEREF">file:/</xsl:param>
<xsl:param name="WORKDIR" select="'./'"/>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Suppressed processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' apiRef/apiDef ')]">
  <xsl:call-template name="output-message">
   <xsl:with-param name="msg">apiDef element found. Please either use apiSyntax or provide an XSLT extension to format apiDef for the specific programming language</xsl:with-param>
  </xsl:call-template>
  <div style="background-color: #FFFF99; color:#CC3333; border: 1pt black solid;">
    <xsl:text>apiDef element found. Please either use apiSyntax or provide an XSLT extension to format apiDef for the specific programming language</xsl:text>
  </div>
</xsl:template>
<!-- Until P019897, the following rule in the javaRef code always overrode the previous rule.
     It does not belong in JavaRef, adding it here so we do not start breaking people. -->
<xsl:template match="*[contains(@class,' apiRef/apiDef ')]" priority="1"/>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Alternatives to base processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="/*[contains(@class,' apiRef/apiRef ')]">
  <xsl:call-template name="api-chapter-setup"/>
</xsl:template>

<xsl:template name="api-chapter-setup">
  <html>
    <xsl:call-template name="setTopicLanguage"/>
    <xsl:value-of select="$newline"/>
    <xsl:call-template name="apiChapterHead"/>
    <xsl:call-template name="apiChapterBody"/>
  </html>
</xsl:template>

<xsl:template name="apiChapterHead">
  <head><xsl:value-of select="$newline"/>
    <!-- initial meta information -->
    <xsl:call-template name="generateCharset"/>   <!-- Set the character set to UTF-8 -->
    <xsl:call-template name="generateDefaultCopyright"/> <!-- Generate a default copyright, if needed -->
    <xsl:call-template name="generateDefaultMeta"/> <!-- Standard meta for security, robots, etc -->
    <xsl:call-template name="getMeta"/>           <!-- Process metadata from topic prolog -->
    <xsl:call-template name="apiGenerateCssLinks"/>  <!-- Generate links to CSS files -->
    <xsl:call-template name="generateChapterTitle"/> <!-- Generate the <title> element -->
    <xsl:call-template name="api-gen-user-head" />    <!-- include user's XSL HEAD processing here -->
    <xsl:call-template name="api-gen-user-scripts" /> <!-- include user's XSL javascripts here -->
    <xsl:call-template name="api-gen-user-styles" />  <!-- include user's XSL style element and content here -->
    <xsl:call-template name="processHDF"/>        <!-- Add user HDF file, if specified -->
  </head>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template name="apiGenerateCssLinks">
  <xsl:variable name="childlang"><xsl:call-template name="getLowerCaseLang"/></xsl:variable>
  <xsl:variable name="urltest">
    <xsl:call-template name="url-string">
      <xsl:with-param name="urltext" select="$CSSPATH"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:call-template name="generateCssLinks"/>  <!-- Generate links to CSS files -->
  <xsl:choose>
    <xsl:when test="($childlang='ar-eg' or $childlang='ar' or $childlang='he' or $childlang='he-il') and ($urltest='url')">
      <link rel="stylesheet" type="text/css" href="{$CSSPATH}{$APIREFCSSRTL}" />
    </xsl:when>
    <xsl:when test="($childlang='ar-eg' or $childlang='ar' or $childlang='he' or $childlang='he-il') and ($urltest='')">
      <link rel="stylesheet" type="text/css" href="{$PATH2PROJ}{$CSSPATH}{$APIREFCSSRTL}" />
    </xsl:when>
    <xsl:when test="not($childlang='ar-eg' or $childlang='ar' or $childlang='he' or $childlang='he-il') and ($urltest='url')">
      <link rel="stylesheet" type="text/css" href="{$CSSPATH}{$APIREFCSS}" />
    </xsl:when>
    <xsl:otherwise>
      <link rel="stylesheet" type="text/css" href="{$PATH2PROJ}{$CSSPATH}{$APIREFCSS}" />
    </xsl:otherwise>
  </xsl:choose>
  <xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template name="apiChapterBody">
  <body>
    <!-- Already put xml:lang on <html>; do not copy to body with commonattributes -->
    <xsl:call-template name="setidaname"/>
    <xsl:value-of select="$newline"/>
    <xsl:call-template name="flagit"/>
    <xsl:call-template name="start-revflag"/>
    <xsl:call-template name="generateBreadcrumbs"/>
    <xsl:call-template name="api-gen-user-header"/>  <!-- include user's XSL running header here -->
    <xsl:call-template name="processHDR"/>
    <!-- Include a user's XSL call here to generate a toc based on what's a child of topic -->
    <xsl:call-template name="api-gen-user-sidetoc"/>
    <xsl:apply-templates/> <!-- this will include all things within topic; therefore, -->
                           <!-- title content will appear here by fall-through -->
                           <!-- followed by prolog (but no fall-through is permitted for it) -->
                           <!-- followed by body content, again by fall-through in document order -->
                           <!-- followed by related links -->
                           <!-- followed by child topics by fall-through -->

    <xsl:call-template name="gen-endnotes"/>    <!-- include footnote-endnotes -->
    <xsl:call-template name="api-gen-user-footer"/> <!-- include user's XSL running footer here -->
    <xsl:call-template name="processFTR"/>      <!-- Include XHTML footer, if specified -->
    <xsl:call-template name="end-revflag"/>
  </body>
  <xsl:value-of select="$newline"/>
</xsl:template>

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Hooks
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template name="api-gen-user-head">
  <!-- by default, execute the default user head -->
  <xsl:call-template name="gen-user-head"/>
</xsl:template>

<xsl:template name="api-gen-user-scripts">
  <!-- by default, execute the default user scripts -->
  <xsl:call-template name="gen-user-scripts"/>
</xsl:template>

<xsl:template name="api-gen-user-styles">
  <!-- by default, execute the default user styles -->
  <xsl:call-template name="gen-user-styles"/>
</xsl:template>

<xsl:template name="api-gen-user-header">
  <!-- by default, execute the default user header -->
  <xsl:call-template name="gen-user-header"/>
</xsl:template>

<xsl:template name="api-gen-user-sidetoc">
  <!-- by default, execute the default user sidetoc -->
  <xsl:call-template name="gen-user-sidetoc"/>
</xsl:template>

<xsl:template name="api-gen-user-footer">
  <!-- by default, execute the default user footer -->
  <xsl:call-template name="gen-user-footer"/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Section processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- adapted from dit2htm.xsl -->
<xsl:template match="*[contains(@class,' apiRef/apiSyntax ')]">
  <xsl:variable name="syntaxItems"
          select="*[contains(@class,' apiRef/apiSyntaxItem ')]"/>
  <xsl:call-template name="start-revflag"/>
  <xsl:apply-templates select="." mode="sectionTitle">
    <xsl:with-param name="titleType" select="'Syntax'"/>
  </xsl:apply-templates>
  <xsl:apply-templates select="*[contains(@class,' apiRef/apiSyntaxText ')]"/>
  <xsl:if test="$syntaxItems">
    <table class="itemdefs">
      <xsl:apply-templates select="$syntaxItems"/>
    </table>
  </xsl:if>
  <xsl:call-template name="end-revflag"/>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiDesc ')]"
      mode="section-fmt">
  <xsl:apply-templates select="." mode="sectionFormat">
    <xsl:with-param name="titleType" select="'Description'"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiImpl ')]"
      mode="section-fmt">
  <xsl:apply-templates select="." mode="sectionFormat">
    <xsl:with-param name="titleType" select="'Implementation'"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/example ')][ancestor::*[contains(@class,' apiRef/apiRef ')]]"
      mode="example-fmt">
  <xsl:apply-templates select="." mode="sectionFormat">
    <xsl:with-param name="titleType" select="'Example'"/>
  </xsl:apply-templates>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiDesc ') or
      contains(@class,' topic/example ')][ancestor::*[contains(@class,' apiRef/apiRef ')]]" mode="sectionFormat">
  <xsl:param name="titleType"/>
  <xsl:call-template name="start-revflag"/>
  <xsl:apply-templates select="." mode="sectionTitle">
    <xsl:with-param name="titleType" select="$titleType"/>
  </xsl:apply-templates>
  <xsl:apply-templates select="*[not(contains(@class,' topic/title '))] |
          text() | comment() | processing-instruction()"/>
  <xsl:call-template name="end-revflag"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/section ') or
      contains(@class,' topic/example ')][ancestor::*[contains(@class,' apiRef/apiRef ')]]" mode="sectionTitle">
  <xsl:param name="titleType"/>
  <xsl:variable name="platformTitle">
    <xsl:call-template name="apiGetPlatformTitle"/>
  </xsl:variable>
  <xsl:variable name="title"
      select="*[contains(@class,' topic/title ')][1]"/>
  <xsl:variable name="titleText">
    <xsl:choose>
    <xsl:when test="$title">
      <xsl:apply-templates select="$title"/>
    </xsl:when>
    <xsl:when test="@spectitle">
      <xsl:apply-templates select="@spectitle"/>
    </xsl:when>
    <!-- These 2 are in the default string list. Others come from the APIRef strings.-->
    <xsl:when test="$titleType='Syntax' or $titleType='Description'">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="$titleType"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="apiGetString">
        <xsl:with-param name="stringName" select="$titleType"/>
      </xsl:call-template>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <h4 class="sectiontitle">
    <xsl:if test="$platformTitle and string-length($platformTitle) &gt; 0">
      <span class="platformSyntax">
        <xsl:value-of select="$platformTitle"/>
      </span>
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:value-of select="$titleText"/>
  </h4>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiDetail ')]">
  <xsl:call-template name="topic.body"/>
  <hr/>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Syntax processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' apiRef/apiSyntaxText ')]">
  <pre class="apiSyntaxText">
    <xsl:apply-templates/>
  </pre>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiSyntaxText ')] /
      *[contains(@class,' apiRef/apiItemName ')]">
  <span class="itemname">
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiSyntaxItem ')]">
  <tr class="itemdef">
    <td class="itemdefname" valign="top">
      <xsl:apply-templates
          select="*[contains(@class,' apiRef/apiItemName ')]"/>
    </td>
    <td class="itemdefnote" valign="top">
      <xsl:apply-templates
          select="*[contains(@class,' apiRef/apiDefNote ')]"/>
    </td>
  </tr>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiSyntaxItem ')] /
      *[contains(@class,' apiRef/apiItemName ')]">
  <xsl:if test="preceding-sibling::*[contains(@class,' apiRef/apiItemName ')]">
    <br/>
  </xsl:if>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiDefNote ')]">
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' apiRef/apiDetail ')] /
      *[contains(@class,' topic/example ')] /
      *[contains(@class,' topic/pre ') and @platform]">
  <xsl:variable name="platformTitle">
    <xsl:call-template name="apiGetPlatformTitle"/>
  </xsl:variable>
  <xsl:if test="$platformTitle and string-length($platformTitle) &gt; 0">
    <p class="platformExample">
      <xsl:value-of select="$platformTitle"/>
    </p>
  </xsl:if>
  <pre>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setscale"/>
    <xsl:call-template name="setidaname"/>
    <xsl:apply-templates/>
  </pre>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - General API processing
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' apiRef/apiRelation ') and 
      not(@href)]">
  <span class="{@class}">
    <xsl:apply-templates/>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/xref ') and 
      contains(@class,' api-d/') and 
      not(@href)]">
  <span class="{@class}">
    <xsl:apply-templates/>
  </span>
</xsl:template>


<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - Filename manipulation
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<!-- adapted from topicpull.xsl - should be shared utilities -->
<xsl:template name="getTopicFile">
  <xsl:param name="href" select="@href"/>
  <xsl:choose>
  <xsl:when test="contains($href,'://') and contains($href,'#')">
    <xsl:value-of select="substring-before($href,'#')"/>
  </xsl:when>
  <xsl:when test="contains($href,'://')">
    <xsl:value-of select="$href"/>
  </xsl:when>
  <xsl:when test="contains($href,'#')">
    <xsl:value-of select="$WORKDIR"/>
    <xsl:value-of select="substring-before($href,'#')"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="$WORKDIR"/>
    <xsl:value-of select="$href"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getTopicID">
  <xsl:param name="href" select="@href"/>
  <xsl:choose>
  <xsl:when test="contains($href,'#') and contains(substring-after($href,'#'),'/')">
    <xsl:value-of select="substring-before(substring-after($href,'#'),'/')"/>
  </xsl:when>
  <xsl:when test="contains($href,'#')">
    <xsl:value-of select="substring-after($href,'#')"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:text>#none#</xsl:text>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getBaseFile">
  <xsl:param name="file"/>
  <xsl:choose>
  <xsl:when test="not($file)"/>
  <xsl:when test="contains($file,'\')">
    <xsl:call-template name="getBaseFile">
      <xsl:with-param name="file" select="substring-after($file,'\')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($file,'/')">
    <xsl:call-template name="getBaseFile">
      <xsl:with-param name="file" select="substring-after($file,'/')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:value-of select="$file"/>
  </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="getFileRoot">
  <xsl:param name="fileroot"/>
  <xsl:param name="file"/>
  <xsl:choose>
  <xsl:when test="$file and contains($file,'.')">
    <xsl:variable name="infix" select="substring-before($file,'.')"/>
    <xsl:variable name="newroot">
      <xsl:choose>
      <xsl:when test="$fileroot and $infix">
        <xsl:value-of select="$fileroot"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="$infix"/>
      </xsl:when>
      <xsl:when test="$infix">
        <xsl:value-of select="$infix"/>
      </xsl:when>
      <xsl:when test="$fileroot">
        <xsl:value-of select="$fileroot"/>
      </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="getFileRoot">
      <xsl:with-param name="fileroot" select="$newroot"/>
      <xsl:with-param name="file"     select="substring-after($file,'.')"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="$fileroot">
    <xsl:value-of select="$fileroot"/>
  </xsl:when>
  <xsl:when test="$file">
    <xsl:value-of select="$file"/>
  </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="getBaseFileRoot">
  <xsl:param name="file"/>
  <xsl:variable name="basefile">
    <xsl:call-template name="getBaseFile">
      <xsl:with-param name="file" select="$file"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:call-template name="getFileRoot">
    <xsl:with-param name="file" select="$basefile"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="apiGetString">
  <xsl:param name="stringName"/>
  <xsl:call-template name="getString">
    <xsl:with-param name="stringName" select="$stringName"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="apiGetPlatformTitle">
  <xsl:param name="platformList" select="@platform"/>
  <xsl:param name="platform" select="concat(' ',translate($platformList,
      'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
      'abcdefghijklmnopqrstuvwxyz'),' ')"/>
  <xsl:choose>
  <xsl:when test="not($platformList)"/>
  <xsl:when test="contains($platform,' c ')">
    <xsl:call-template name="apiGetString">
      <xsl:with-param name="stringName" select="'platform.c'"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($platform,' cpp ')">
    <xsl:call-template name="apiGetString">
      <xsl:with-param name="stringName" select="'platform.cpp'"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($platform,' java ')">
    <xsl:call-template name="apiGetString">
      <xsl:with-param name="stringName" select="'platform.java'"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($platform,' javascript ')">
    <xsl:call-template name="apiGetString">
      <xsl:with-param name="stringName" select="'platform.javascript'"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($platform,' perl ')">
    <xsl:call-template name="apiGetString">
      <xsl:with-param name="stringName" select="'platform.perl'"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:when test="contains($platform,' vb ')">
    <xsl:call-template name="apiGetString">
      <xsl:with-param name="stringName" select="'platform.vb'"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise/>
  </xsl:choose>
</xsl:template>

</xsl:stylesheet>
