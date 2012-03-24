<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  topic2fo.xsl
 | DITA topic to FO; "single topic to single printed 'chapter'"-level view
 *-->

<!-- BUGS:
1. Excluded footnotes still get generated as endnotes.
   Footnotes located within required cleanup will be rendered whether the doc is output in draft mode or not.
   This is due to pull-based processing of endnotes, which gets content that would ordinarily be excluded by 
   particular processing settings.  This is actually a general problem faced by ANY XSLT processor that
   has pull-based processors intermingled with push-based exclusion logic.  Ideally, exclusion should remove
   an element from the DOM, then you can depend on pull not to find things it shouldn't find!  Two-passes!

 |Bugs:
 | FO processors are anything but interoperable at the time of this
 | release of the DITA FO demo (August 2002). There are possibly
 | other things to work on as well for this FO implementation, but
 | we know about these:
 |
 |  - this *demo application* has not been fully localization-enabled
 |  - something causes a page overflow error with XEP
 |  - column numbers do not total correctly for FOP (collapses some tables
 |    to the left, although others do appear to work okay)
 |  - "inherit" as an indent value does not work for FOP
 |  - white-space-collapse="false" is provided as an FOP equivalent to
 |    white-space="pre" but doesn't appear to work
 |  - leader-patter="use-content" is ignored by FOP
 |  - FOP starts a blank initial page; AH does not. layout defs may be the cause
 |  - Antenna House requires changing the "dflt-ext" variable to ".jpg"
 |    (and you must have equivalent graphics in the JPEG format)
 |  - the new simpletable element currently spreads evenly across the number of
 |    defined entries; it has a @relativecols attribute to use for future
 |    methods of passing relative widths to its processor (possibly "* * 3*")
 |  - can't get left indent into "lq" so using surround box instead for now
 |
 +  added  start-indent="2pt" into table-cell procs to eliminate deep offsets w/in cells
 |
 +-->

<!DOCTYPE xsl:transform [
<!-- entities for use in the generated output (must produce correctly in FO) -->
  <!ENTITY gt            "&gt;"> 
  <!ENTITY lt            "&lt;"> 
  <!ENTITY rbl           "&#160;">
  <!ENTITY nbsp          "&#160;">
  <!ENTITY quot          "&#34;">
  <!ENTITY quotedblleft  "&#x201C;">
  <!ENTITY quotedblright "&#x201D;">
  <!ENTITY bullet        "&#x2022;"><!--check these two for better assignments -->
  <!ENTITY middot        "&#x2023;">

  <!ENTITY section       "&#xA7;">
  <!ENTITY endash        "&#x2013;">
  <!ENTITY emdash        "&#x2014;">

  <!ENTITY copyr         "&#xA9;">
  <!ENTITY trademark     "&#x2122;">
  <!ENTITY registered    "&#xAE;">
]>
<xsl:transform version="1.0" xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:saxon="http://icl.com/saxon" extension-element-prefixes="saxon">
  
  <xsl:import href="../../../xsl/common/output-message.xsl"/>
    
  <!-- Page setup - used by simple-master-set -->
  <xsl:param name="paper.type" select="'A4'"/>
  <xsl:param name="page.orientation" select="'portrait'"/>
  <xsl:param name="page.margin.bottom" select="'0cm'"/>
  <xsl:param name="page.margin.top" select="'0cm'"/>
  <xsl:param name="page.margin.inner">2cm</xsl:param>
  <xsl:param name="page.margin.outer">2cm</xsl:param>
  <xsl:param name="body.margin.bottom" select="'2cm'"/>
  <xsl:param name="body.margin.top" select="'2cm'"/>
  <xsl:param name="body.font.family" select="'Helvetica'"/>
  <xsl:param name="body.font.size">9pt</xsl:param>
  <xsl:include href="dita-page-setup.xsl"/>
  <!-- Whitespace stripping policy -->
  <xsl:strip-space elements="*"/>
  <xsl:preserve-space elements="pre lines"/>
  <!-- this XSL directive insensitive to DITA specialized elements -->
  <!-- Here we define default block properties. -->
  <xsl:attribute-set name="block.properties">
    <xsl:attribute name="border-bottom-width">0.5em</xsl:attribute>
  </xsl:attribute-set>
  <!-- Newline character (capture the native file newline) -->
  <xsl:variable name="newline"/>
  <!--null out things that don't apply to PDF -->
  <xsl:template match="*[contains(@class,' topic/prolog ')]"/>
  <!-- OTHER STYLESHEET INCLUDES/IMPORTS -->
  <!-- local, output-specific routines -->
  <xsl:include href="dita2fo-parms.xsl"/>
  <!--xsl:include href="dita2fo-prolog.xsl"/-->
  <xsl:include href="dita2fo-titles.xsl"/>
  <xsl:include href="dita2fo-elems.xsl"/>
  <xsl:include href="dita2fo-lists.xsl"/>
  <xsl:include href="dita2fo-links.xsl"/>
  <xsl:include href="dita2fo-simpletable.xsl"/>
  <xsl:include href="dita2fo-calstable.xsl"/>
  <xsl:include href="dita2fo-subroutines.xsl"/>
  <!-- common routines -->
  <xsl:include href="../../../xsl/common/dita-utilities.xsl"/>
  
  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <!-- null some things that won't be used for now -->
  <!--xsl:template name="output-message"/-->
  <!--
 <xsl:include href="dita2fo-stubs.xsl"/>
 <xsl:include href="toc2fo.xsl"/>
-->
  <!-- OUTPUT METHOD -->
  <!-- Should this be specified in the including shell? -->
  <!-- XSLFO output is simple XML syntax) -->
  <xsl:output method="xml" version="1.0" indent="no"/>
  <!-- USER SPECIFIC DECLARED VALUES  (declared in dit2fo-parms.xsl) -->
  <!-- "GLOBAL" DECLARATIONS -->
  <!-- Setup for translation/localization  (declared in dit2fo-parms.xsl -->
  <!-- ROOT RULE -->
  <xsl:template match="/" priority="3">
    <xsl:apply-templates mode="toplevel"/>
  </xsl:template>
  <!-- This first template rule generates the outer-level shell for a delivery context. -->
  <!-- In an override stylesheet, the same call to "chapter-setup" must be issued to
     maintain the consistency of overall look'n'feel of the output FO. -->
  <xsl:template match="*[contains(@class,' topic/topic ')]" name="toptopic" mode="toplevel">
    <!-- this is an "h1div" context for standalone documents -->
    <xsl:call-template name="chapter-setup"/>
  </xsl:template>
  <!-- =============== start of contextual topic titles ================= -->
  <!-- this should be a lower-priority match for all non-toplevel topics in an aggregate -->
  <xsl:template match="*[contains(@class,' topic/topic ')]">
    <fo:block>
      <!-- delete the line which call "gen-toc-id" template to fix bug#1304859 -->
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <!-- =================== start of processors  ====================== -->
  <!-- SCRIPT SUPPORT -->
  <xsl:template name="script-sample">
    <!-- not applicable for FO output -->
  </xsl:template>
  <!--  NAMED TEMPLATES (call by name, only)  -->
  <xsl:template name="gen-att-label">
    <xsl:if test="@spectitle">
      <fo:block font-weight="bold">
        <xsl:value-of select="@spectitle"/>
      </fo:block>
    </xsl:if>
  </xsl:template>
  <!-- named templates that can be used anywhere -->
  <!-- this replaces newlines with the BR element, forcing non-concatenation even in flow contexts -->
  <xsl:template name="br-replace">
    <xsl:param name="word"/>
    <!-- capture an actual newline within the xsl:text element -->
    <xsl:variable name="cr">
      <xsl:text/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="contains($word,$cr)">
        <xsl:value-of select="substring-before($word,$cr)"/>
        <!--br class="br"/-->
        <xsl:call-template name="br-replace">
          <xsl:with-param name="word" select="substring-after($word,$cr)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$word"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- diagnostic: call this to generate a path-like view of an element's ancestry! -->
  <xsl:template name="breadcrumbs">
    <xsl:variable name="full-path">
      <xsl:for-each select="ancestor-or-self::*">
        <xsl:value-of select="concat('/',name())"/>
      </xsl:for-each>
    </xsl:variable>
    <fo:block font-weight="bold">
      <xsl:value-of select="$full-path"/>
    </fo:block>
  </xsl:template>
  <!-- the following named templates generate inline content for the delivery context -->
  <!-- generate null filler if the phrase is evidently empty -->
  <xsl:template name="apply-for-phrases">
    <xsl:choose>
      <xsl:when test="not(text()[normalize-space(.)] | *)">
        <!--xsl:comment>null</xsl:comment-->
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!--  "FORMAT" GLOBAL DECLARATIONS  -->
  <xsl:variable name="fmt-fig-lbl-loc">over</xsl:variable>
  <!-- values: over, under -->
  <xsl:variable name="fmt-tbl-lbl-loc">over</xsl:variable>
  <!-- values: over, under -->
  <xsl:variable name="link-top-section">no</xsl:variable>
  <!-- values: yes, no (or any not "yes") -->
  <xsl:variable name="do-place-ing">no</xsl:variable>
  <!-- values: yes, no (or any not "yes") -->
  <!-- this value should be created in a named template since it needs to increment per call -->
  <!-- for now, the static value is null until we can redo this intent -->
  <xsl:variable name="fig-pfx-txt"/>
  <xsl:variable name="xfig-pfx-txt">
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="'Figure'"/>
    </xsl:call-template>
    <xsl:number level="any" count="label" from="/"/> of <xsl:value-of
    select="count(//fig/label)"/>. </xsl:variable>
  <!-- values: '' or custom design -->
  <xsl:variable name="tbl-pfx-txt"/>
  <!--  "FORMAT" MACROS  -->
  <!--
 | These macros support globally-defined formatting constants for
 | document content.  Some elements have attributes that permit local
 | control of formatting; such logic is part of the pertinent template rule.
 +-->
  <xsl:template name="place-tbl-width">
    <xsl:variable name="twidth-fixed">100%</xsl:variable>
    <xsl:if test="$twidth-fixed != ''">
      <xsl:attribute name="width">
        <xsl:value-of select="$twidth-fixed"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>
  <xsl:template name="proc-ing">
    <xsl:if test="$do-place-ing = 'yes'">
      <!-- set in a global variable, as with label placement, etc. -->
      <fo:external-graphic src="url(image/tip-ing.jpg)"/>
      <!-- this should be an xsl:choose with the approved list and a selection method-->

      <!-- add any other required positioning controls, if needed, but must be valid in the location
         from which the call to this template was made -->
      &nbsp; </xsl:if>
  </xsl:template>
  <!-- =================== end of processors  ====================== -->
  <!-- =================== start of override stubs ====================== -->
  <!--  STUBS FOR USER PROVIDED OVERRIDE EXTENSIONS  -->
  <xsl:template name="gen-user-header">
    <!-- to customize: copy this to your override transform, add whatever content you want! -->
    <!-- for example, to display logos, search/nav widgets, etc. -->
  </xsl:template>
  <xsl:template name="gen-user-footer">
    <!-- to customize: copy this to your override transform, add whatever content you want! -->
    <!-- for example, to display compliances for: XHTML, accessibility, content ratings, etc. -->
  </xsl:template>
  <xsl:template name="gen-user-sidetoc">
    <!-- to customize: copy this to your override transform, add whatever content you want! -->
    <!-- Common implementations use a table with align=right to place generated content
       adjacent to the start of the body content -->
  </xsl:template>
  <xsl:template name="gen-user-scripts">
    <!-- to customize: copy this to your override transform, add whatever content you want! -->
    <!-- see (or enable) the named template "script-sample" for an example -->
    <!--xsl:call-template name="script-sample"/-->
  </xsl:template>
  <xsl:template name="gen-user-styles">
    <!-- to customize: copy this to your override transform, add whatever content you want! -->
  </xsl:template>
  <xsl:template name="gen-user-panel-title-pfx">
    <!-- to customize: copy this to your override transform, add whatever content you want! -->
    <!-- Generate content based on run-time parameter value, with local logic here -->
    <!-- This is overrideable -->
  </xsl:template>
  <xsl:template name="gen-main-panel-title">
    <!-- use the searchtitle unless there's no value - else use title -->
    <xsl:variable name="schtitle">
      <xsl:value-of select="/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')]"/>
    </xsl:variable>
    <xsl:variable name="ditaschtitle">
      <xsl:value-of select="/dita/*[contains(@class,' topic/topic ')][1]/*[contains(@class,' topic/titlealts ')]/*[contains(@class,' topic/searchtitle ')]"/>
    </xsl:variable>
    <xsl:variable name="maintitle">
      <xsl:value-of select="/*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]"/>
    </xsl:variable>
    <xsl:variable name="ditamaintitle">
      <xsl:value-of select="/dita/*[contains(@class,' topic/topic ')][1]/*[contains(@class,' topic/title ')]"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="string-length($schtitle) &gt; 0">
        <xsl:value-of select="$schtitle"/>
      </xsl:when>
      <xsl:when test="string-length($ditaschtitle) &gt; 0">
        <xsl:value-of select="$ditaschtitle"/>
      </xsl:when>
      <xsl:when test="string-length($maintitle) &gt; 0">
        <xsl:value-of select="$maintitle"/>
      </xsl:when>
      <xsl:when test="string-length($ditamaintitle) &gt; 0">
        <xsl:value-of select="$ditamaintitle"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>***</xsl:text>
        <xsl:call-template name="output-message">          
          <xsl:with-param name="msgnum">037</xsl:with-param>
          <xsl:with-param name="msgsev">W</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="gen-user-metadata"/>
  <!-- not applicable yet within FO -->
  <!-- =================== end of override stubs ====================== -->
  <!-- =================== DEFAULT PAGE LAYOUT ====================== -->
  <xsl:template name="chapter-setup">
    <!-- Newline character (capture the native file newline) -->
    <xsl:variable name="newline"/>
    <fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="cover" page-height="792pt"
          page-width="612pt" margin-top="36pt" margin-bottom="36pt"
          margin-left="36pt" margin-right="36pt">
          <fo:region-body margin-top="120pt"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="leftPage" page-height="792pt"
          page-width="612pt" margin-top="36pt" margin-bottom="36pt"
          margin-left="72pt" margin-right="60pt">
          <fo:region-body margin-bottom="36pt" margin-top="24pt"/>
          <fo:region-before extent="24pt"/>
          <fo:region-after extent="36pt"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="rightPage" page-height="792pt"
          page-width="612pt" margin-top="36pt" margin-bottom="36pt"
          margin-left="60pt" margin-right="72pt">
          <fo:region-body margin-bottom="36pt" margin-top="24pt"/>
          <fo:region-before extent="24pt"/>
          <fo:region-after extent="36pt"/>
        </fo:simple-page-master>
        <fo:simple-page-master master-name="common-page" page-height="792pt"
          page-width="612pt" margin-top="36pt" margin-bottom="36pt"
          margin-left="60pt" margin-right="60pt">
          <fo:region-body margin-bottom="36pt" margin-top="24pt"/>
          <fo:region-before extent="24pt"/>
          <fo:region-after extent="36pt"/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <!--xsl:call-template name="gen-cover-page"-->
      <!-- initiate content processing within basic page "shell" -->
      <fo:page-sequence master-reference="common-page" force-page-count="no-force">
        <!-- header -->
        <fo:static-content flow-name="xsl-region-before" font-size="9pt" font-family="Helvetica">
          <fo:block linefeed-treatment="ignore" text-align-last="justify">
            <fo:inline text-align="start">
              <xsl:text>Stub for header content</xsl:text>
            </fo:inline>
            <!-- remove use-content for FOP -->
            <!--<fo:leader rule-style="solid" leader-length.maximum="100%" leader-length.optimum="60%" leader-length.minimum="10pt" leader-pattern="use-content">&nbsp;</fo:leader>-->
            <fo:leader rule-style="solid" leader-length.maximum="100%"
              leader-length.optimum="60%" leader-length.minimum="10pt">&nbsp;</fo:leader>
            <fo:inline text-align="end" font-weight="bold">
              <xsl:text>right side content</xsl:text>
            </fo:inline>
          </fo:block>
          <!--fo:block><fo:leader leader-pattern="rule" leader-length="100%"/></fo:block-->
        </fo:static-content>
        <!-- footer -->
        <fo:static-content flow-name="xsl-region-after">
          <fo:block linefeed-treatment="ignore" text-align-last="justify">
            <fo:inline text-align="start" font-size="10pt" font-family="Helvetica">
              <xsl:value-of select="//*/*[contains(@class, ' topic/title ')]"/>
            </fo:inline>
            <!--fo:leader rule-style="solid" leader-length.maximum="100%" leader-length.optimum="60%" leader-length.minimum="10pt" leader-pattern="use-content">&nbsp;</fo:leader-->
            <fo:inline text-align="end" font-size="10pt" font-weight="bold" font-family="Helvetica">
              <xsl:text>Page </xsl:text>
              <fo:page-number/>
            </fo:inline>
          </fo:block>
        </fo:static-content>
        <!-- body -->
        <fo:flow flow-name="xsl-region-body">
          <fo:block line-height="10pt" font-size="9pt" font-family="Helvetica" id="page1-1">
            <xsl:apply-templates/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>
  <xsl:template name="gen-cover-page">
    <!-- generate an "outside-front cover" page -->
    <fo:page-sequence master-reference="cover">
      <fo:flow flow-name="xsl-region-body">
        <fo:block text-align="center">
          <fo:block space-after.optimum="40pt" space-before.optimum="40pt"
            line-height="80pt" font-size="32pt" font-family="Helvetica">
            <xsl:value-of select="/topic/title"/>
          </fo:block>
          <fo:block line-height="50pt" font-size="24pt" font-family="Helvetica">
            <fo:block font-style="italic">
              <xsl:value-of select="*[contains(@class, ' topic/title ')]"/>
            </fo:block>
          </fo:block>
        </fo:block>
      </fo:flow>
    </fo:page-sequence>
    <!-- generate an "inside front cover" page (left side) -->
    <fo:page-sequence master-reference="cover">
      <fo:flow flow-name="xsl-region-body">
        <fo:block xsl:use-attribute-sets="p" color="purple" text-align="center"/>
      </fo:flow>
    </fo:page-sequence>
  </xsl:template>
  <!-- domains (should be external) -->
</xsl:transform>
