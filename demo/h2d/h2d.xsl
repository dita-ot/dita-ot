<?xml version="1.0" encoding="utf-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!--  h2d.xsl 
 | Migrate XHTML content into DITA topics
 |
 | (C) Copyright IBM Corporation 2001, 2002, 2003, 2004, 2005, 2006. All Rights Reserved.
 +
 | Udates:
 | 2002/06/11 DRD: changed BR trap from PI to comment
 | 2003/02/13 DRD: removed xhtml: namespace prefix baggage (use tidy's -doctype omit)
 |                 added support for text nodes in task content pulls
 |                 added renaming of related-links extensions to ".dita" instead of ".htm(l)"
 |                 if any name/content metas (except generators), open a prolog and populate metadata
 | 2003/03/27 DRD: extended meta trap to include "GENERATOR" (uc)
 |                 added default xml:lang="en-us"
 |                 added genidattribute to provide single place to generate topic id (needs work)
 | 2003/03/28 RDA: Place <title> into <searchtitle> instead of <shortdesc>
 |                 Do not create an XREF for <a> without @href
 |                 Only create "Collected links" when links exist
 |                 Do not add links to "Collected links" if they are within this file
 |                 Add support for @format, @scope, and <desc> inside link
 |                 Add variables to check for first heading level, to make sections later 
 | 2003/04/04 RDA: Add FILENAME parameter to determine more unique file IDs
 | 2003/04/05 RDA: Add support for multi-column tables, and for spanned rows (not yet spanned columns)
 | 2003/05/07 RDA: Add support for thead, tbody, and caption within tables
 | 2003/07/17 RDA: Allow underscores to appear in the topic ID, also period and dash if not
 |                              the first character
 | 2003/08/21 RDA: Allow spanned columns within tables
 |                 Allow single paragraphs in table entries, to ignore the <p> tag
 | 2003/10/07 RDA: Process span classes that were moved by tidy (like class=c1)
 | 2003/10/13 RDA: Pre-process the HTML, if it contains lists that stop and start
 | 2004/10/13 RDA: Pass through @compact, plus table/row attributes
 |                 If a link starts with http:, https:, or ftp:, do not change extension
 | 2004/11/22 RDA: Update to pass through comments
 | 2005/01/08 RDA: Revised for external publication
 | 2006/01/04 RDA: Added various bug fixes, mostly for tables and class attributes
 |                 Updated public IDs to use the OASIS standard
 +-->


<!-- If you wish to set the doctype manually, you will need to comment out the
     following 5 lines, and uncomment the xsl:stylesheet lines below. See the 
     comments before that section for details. -->
<!--<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
>
<xsl:output method="xml" indent="no" encoding="utf-8" />
<xsl:param name="infotype">topic</xsl:param>-->

<!-- If you wish to set the doctype dynamically, you will need to uncomment
     the following section. The section sets the XSLT version as 1.1, which
     allows some engines to use variables for system and public IDs.
     Those variables are set here based on the infotype parameter, or they
     can be passed in directly from the command line. -->
<xsl:stylesheet version="1.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:saxon="http://icl.com/saxon"
                extension-element-prefixes="saxon">
<xsl:param name="infotype">topic</xsl:param>
<xsl:variable name="systemid">
    <xsl:choose>
        <xsl:when test="$infotype='concept'">../dtd/concept.dtd</xsl:when>
        <xsl:when test="$infotype='task'">../dtd/task.dtd</xsl:when>
        <xsl:when test="$infotype='reference'">../dtd/reference.dtd</xsl:when>
        <xsl:otherwise>../dtd/topic.dtd</xsl:otherwise>
    </xsl:choose>
</xsl:variable>
<xsl:variable name="publicid">
    <xsl:choose>
        <xsl:when test="$infotype='concept'">-//OASIS//DTD DITA Concept//EN</xsl:when>
        <xsl:when test="$infotype='task'">-//OASIS//DTD DITA Task//EN</xsl:when>
        <xsl:when test="$infotype='reference'">-//OASIS//DTD DITA Reference//EN</xsl:when>
        <xsl:otherwise>-//OASIS//DTD DITA Topic//EN</xsl:otherwise>
    </xsl:choose>
</xsl:variable>
<xsl:output method="xml" indent="no" encoding="utf-8" 
    doctype-system="{$systemid}" doctype-public="{$publicid}"/>


<!-- ========== PARAMETERS ============== -->

<!-- what kind of topic to generate?  set up default of 'topic' but allow external override -->
<!-- sample call: 
     saxon tasktest.html h2d.xsl infotype=task > tasktest.dita
  -->

<!-- What extension should be used for links that go to other DITA topics?
     Assumption is that local HTML targets will be converted to DITA. -->
<xsl:param name="dita-extension">.dita</xsl:param>

<!-- Create a parameter for the defualt language -->
<xsl:param name="default-lang">en-us</xsl:param>

<!-- Take the filename as an input parameter to determine the main topic's ID -->
<xsl:param name="FILENAME">
    <xsl:choose>
        <xsl:when test="$infotype='concept' or $infotype='reference' or $infotype='task' or $infotype='topic'">
            <xsl:value-of select="$infotype"/>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="'topic'"/>
        </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="string-length()"/>
    <xsl:value-of select="'.htm'"/>
</xsl:param>

<!-- Use the FILENAME to determine the ID for the output topic. Invalid ID characters
     must be removed (replaced with generic D character). If a filename starts with
     a number, which cannot start an ID, all numbers will be replaced with letters. -->
<xsl:variable name="filename-id">
  <xsl:choose>
    <xsl:when test="starts-with($FILENAME,'0') or starts-with($FILENAME,'1') or
                    starts-with($FILENAME,'2') or starts-with($FILENAME,'3') or
                    starts-with($FILENAME,'4') or starts-with($FILENAME,'5') or
                    starts-with($FILENAME,'6') or starts-with($FILENAME,'7') or
                    starts-with($FILENAME,'8') or starts-with($FILENAME,'9') or
                    starts-with($FILENAME,'.') or starts-with($FILENAME,'-')">
      <xsl:value-of select="translate(substring-before($FILENAME,'.htm'),
                                      '0123456789.-,!@#$%^()=+[]{}/\;&amp;',
                                      'ABCDEFGHIJDDDDDDDDDDDDDDDDDDDDDD')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="translate(substring-before($FILENAME,'.htm'),
                                      ',!@#$%^()=+[]{}/\;&amp;',
                                      'DDDDDDDDDDDDDDDDDDDDDD')"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<!-- eliminate whitespace in body as a migration concern -->
<xsl:strip-space elements="body"/>

<!-- RDA: use the 2 variables below, instead of the 6 starter files -->
<xsl:variable name="main-head-level">
  <xsl:choose>
    <xsl:when test="/html/body/descendant::h1[1][not(preceding::h2|preceding::h3|preceding::h4|preceding::h5|preceding::h6)]">h1</xsl:when>
    <xsl:when test="/html/body/descendant::h2[1][not(preceding::h3|preceding::h4|preceding::h5|preceding::h6)]">h2</xsl:when>
    <xsl:when test="/html/body/descendant::h3[1][not(preceding::h4|preceding::h5|preceding::h6)]">h3</xsl:when>
    <xsl:when test="/html/body/descendant::h4[1][not(preceding::h5|preceding::h6)]">h4</xsl:when>
    <xsl:when test="/html/body/descendant::h5[1][not(preceding::h6)]">h5</xsl:when>
    <xsl:when test="/html/body/descendant::h6[1]">h6</xsl:when>
    <xsl:otherwise>h1</xsl:otherwise>
  </xsl:choose>
</xsl:variable>

<xsl:template name="output-message">
    <xsl:param name="msg" select="***"/>
    <xsl:message><xsl:value-of select="$msg"/></xsl:message>
</xsl:template>

<!-- if needed, add the dita wrapper here -->
<xsl:template match="/">
  <xsl:call-template name="validate-parameters"/>
<!-- Some HTML editors store ordered lists as sequential lists, with all but the first
     using @start to resume numbering. If a topic uses this, the lists will be pulled together.
     They are placed in a variable that contains a cleaned up version of the original HTML. The
     standard templates are then used to format the modified HTML. Templates for pre-processing
     the HTML Are all at the bottom of the file.
     If there are no lists with abnormal numbering, just start processing. -->
  <xsl:choose>
    <xsl:when test="not(//ol[@start])"><xsl:apply-templates select="*|comment()|text()|processing-instruction()"/></xsl:when>
    <xsl:otherwise>
      <!-- Process the entire file. Most elements are copied straight into the variable; ordered
           lists and elements between them are modified slightly. --> 
      <xsl:variable name="shift-lists">
        <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="shift-lists"/>
      </xsl:variable>
      <!-- For some reason, if I do this without a mode, I get a Java overflow error. -->
      <xsl:apply-templates select="html/preceding-sibling::comment()"/>
      <xsl:apply-templates select="$shift-lists" mode="redirect"/>
      <xsl:apply-templates select="html/following-sibling::comment()"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Process the HTML file that was placed in a variable using normal routines. -->
<xsl:template match="*" mode="redirect">
  <xsl:apply-templates select="."/>
</xsl:template>

<!-- general the overall topic container and pull content for it -->

<xsl:template match="*[local-name()='html']">
  <xsl:choose>
    <xsl:when test="$infotype='topic'"><xsl:call-template name="gen-topic"/></xsl:when>
    <xsl:when test="$infotype='concept'"><xsl:call-template name="gen-concept"/></xsl:when>
    <xsl:when test="$infotype='task'"><xsl:call-template name="gen-task"/></xsl:when>
    <xsl:when test="$infotype='reference'"><xsl:call-template name="gen-reference"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="gen-topic"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- named templates for various infotyped topic shells -->

<!-- Generic topic template -->

<xsl:template name="gen-topic">
  <topic xml:lang="{$default-lang}">
    <xsl:call-template name="genidattribute"/>
    <xsl:call-template name="gentitle"/>
    <xsl:call-template name="gentitlealts"/>
    <xsl:call-template name="genprolog"/>
    <body>
      <xsl:apply-templates select="(body/*|body/text()|body/comment())[1]" mode="creating-content-before-section"/>
      <xsl:choose>
        <xsl:when test="$main-head-level='h1'">
          <xsl:apply-templates select="body/h1[preceding-sibling::h1]|body/h2|body/h3|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:when test="$main-head-level='h2'">
          <xsl:apply-templates select="body/h1|body/h2[preceding-sibling::h2]|body/h3|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:when test="$main-head-level='h3'">
          <xsl:apply-templates select="body/h1|body/h2|body/h3[preceding-sibling::h3]|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:when test="$main-head-level='h4'">
          <xsl:apply-templates select="body/h1|body/h2|body/h3|body/h4[preceding-sibling::h4]|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:when test="$main-head-level='h5'">
          <xsl:apply-templates select="body/h1|body/h2|body/h3|body/h4|body/h5[preceding-sibling::h5]|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:otherwise> <!-- Otherwise, level is h6 -->
          <xsl:apply-templates select="body/h1|body/h2|body/h3|body/h4|body/h5|body/h6[preceding-sibling::h6]|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:otherwise>
      </xsl:choose>
    </body>
    <xsl:call-template name="genrellinks"/>
  </topic>
</xsl:template>


<!-- Implementation note: except for topic, DITA infotypes have content models with strong
     containment rules.  These implementations try to separate allowed body content from
     contexts required by the target formats. This may need additional work.  With XHTML 2.0,
     the tests for contextually introduced containment are eased and these templates can be
     generalized and possibly made more robust. -->

<!-- Concept topic template -->

<!-- See task for ideas implemented here for separating regular body content from a first heading, which
     ordinarily denotes one or more sections with NO following text.  We put EVERYTHING after the
     first h2 into a section as a strong-arm way to enforce the concept model, but users will have
     to check for intended scoping afterwards. -->

<xsl:template name="gen-concept">
  <concept xml:lang="{$default-lang}">
    <xsl:call-template name="genidattribute"/>
    <xsl:if test="@id"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute></xsl:if>
    <xsl:call-template name="gentitle"/>
    <xsl:call-template name="gentitlealts"/>
    <xsl:call-template name="genprolog"/>

    <conbody>
      <!-- Anything up to the first heading (except for whatever heading was pulled into <title>) will
           be processed as it would for a topic. After a heading is encountered, a section will be created
           for that and all following headings. Content up to the next heading will go into the section. -->
      <xsl:apply-templates select="(body/*|body/text()|body/comment())[1]" mode="creating-content-before-section"/>
      <xsl:choose>
        <xsl:when test="$main-head-level='h1'">
          <xsl:apply-templates select="body/h1[preceding-sibling::h1]|body/h2|body/h3|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:when test="$main-head-level='h2'">
          <xsl:apply-templates select="body/h1|body/h2[preceding-sibling::h2]|body/h3|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:when test="$main-head-level='h3'">
          <xsl:apply-templates select="body/h1|body/h2|body/h3[preceding-sibling::h3]|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:when test="$main-head-level='h4'">
          <xsl:apply-templates select="body/h1|body/h2|body/h3|body/h4[preceding-sibling::h4]|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:when test="$main-head-level='h5'">
          <xsl:apply-templates select="body/h1|body/h2|body/h3|body/h4|body/h5[preceding-sibling::h5]|body/h6|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:when>
        <xsl:otherwise> <!-- Otherwise, level is h6 -->
          <xsl:apply-templates select="body/h1|body/h2|body/h3|body/h4|body/h5|body/h6[preceding-sibling::h6]|body/h7" mode="create-section-with-following-content"/>                              
        </xsl:otherwise>
      </xsl:choose>
      
    </conbody>
    <xsl:call-template name="genrellinks"/>
  </concept>
</xsl:template>

<xsl:template match="*|text()|comment()" mode="creating-content-before-section">
  <xsl:apply-templates select="."/>
  <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
</xsl:template>
<xsl:template match="h1|h2|h3|h4|h5|h6" mode="creating-content-before-section">
  <xsl:choose>
    <xsl:when test="$main-head-level='h1' and self::h1 and not(preceding::h1)">
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h2' and self::h2 and not(preceding::h2)">
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h3' and self::h3 and not(preceding::h3)">
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h4' and self::h4 and not(preceding::h4)">
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h5' and self::h5 and not(preceding::h5)">
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h6' and self::h6 and not(preceding::h6)">
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="creating-content-before-section"/>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<!-- Reference topic template -->

<xsl:template name="gen-reference">
  <reference xml:lang="{$default-lang}">
    <xsl:call-template name="genidattribute"/>
    <xsl:call-template name="gentitle"/>
    <xsl:call-template name="gentitlealts"/>
    <xsl:call-template name="genprolog"/>
    <refbody>
      <!-- Processing is similar to concept, except that everything before the second heading must also be
           placed into a section. Also, any tables can be outside of the section. -->
      <xsl:choose>
        <xsl:when test="$main-head-level='h1'">
          <!-- First process anything that comes before any subheadings, or a second h1 -->
          <xsl:if test="body/text()[not(preceding::table or preceding::h1[2] or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                        body/comment()[not(preceding::table or preceding::h1[2] or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                        body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                   preceding::table or preceding::h1[2] or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]">
            <section>
              <xsl:apply-templates select="body/text()[not(preceding::table or preceding::h1[2] or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                                           body/comment()[not(preceding::table or preceding::h1[2] or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                                           body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                                      preceding::table[parent::body] or preceding::h1[2] or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]"/>
            </section>
          </xsl:if>
          <!-- Now turn any other headings into sections, with following stuff -->
          <xsl:apply-templates select="body/table|body/h1[preceding-sibling::h1]|body/h2|body/h3|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:when test="$main-head-level='h2'">
          <!-- First process anything that comes before any subheadings, or a second h2 -->
          <xsl:if test="body/text()[not(preceding::table or preceding::h1 or preceding::h2[2] or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                        body/comment()[not(preceding::table or preceding::h1 or preceding::h2[2] or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                        body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                   preceding::table or preceding::h1 or preceding::h2[2] or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]">
            <section>
              <xsl:apply-templates select="body/text()[not(preceding::table or preceding::h1 or preceding::h2[2] or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                                           body/comment()[not(preceding::table or preceding::h1 or preceding::h2[2] or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]|
                                           body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                                      preceding::table[parent::body] or preceding::h1 or preceding::h2[2] or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6)]"/>
            </section>
          </xsl:if>
          <!-- Now turn any other headings into sections, with following stuff -->
          <xsl:apply-templates select="body/table|body/h1|body/h2[preceding-sibling::h2]|body/h3|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:when test="$main-head-level='h3'">
          <!-- First process anything that comes before any subheadings, or a second h3 -->
          <xsl:if test="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3[2] or preceding::h4 or preceding::h5 or preceding::h6)]|
                        body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3[2] or preceding::h4 or preceding::h5 or preceding::h6)]|
                        body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                   preceding::table or preceding::h1 or preceding::h2 or preceding::h3[2] or preceding::h4 or preceding::h5 or preceding::h6)]">
            <section>
              <xsl:apply-templates select="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3[2] or preceding::h4 or preceding::h5 or preceding::h6)]|
                                           body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3[2] or preceding::h4 or preceding::h5 or preceding::h6)]|
                                           body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                                      preceding::table[parent::body] or preceding::h1 or preceding::h2 or preceding::h3[2] or preceding::h4 or preceding::h5 or preceding::h6)]"/>
            </section>
          </xsl:if>
          <!-- Now turn any other headings into sections, with following stuff -->
          <xsl:apply-templates select="body/table|body/h1|body/h2|body/h3[preceding-sibling::h3]|body/h4|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:when test="$main-head-level='h4'">
          <!-- First process anything that comes before any subheadings, or a second h4 -->
          <xsl:if test="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4[2] or preceding::h5 or preceding::h6)]|
                        body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4[2] or preceding::h5 or preceding::h6)]|
                        body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                   preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4[2] or preceding::h5 or preceding::h6)]">
            <section>
              <xsl:apply-templates select="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4[2] or preceding::h5 or preceding::h6)]|
                                           body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4[2] or preceding::h5 or preceding::h6)]|
                                           body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                                      preceding::table[parent::body] or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4[2] or preceding::h5 or preceding::h6)]"/>
            </section>
          </xsl:if>
          <!-- Now turn any other headings into sections, with following stuff -->
          <xsl:apply-templates select="body/table|body/h1|body/h2|body/h3|body/h4[preceding-sibling::h4]|body/h5|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:when test="$main-head-level='h5'">
          <!-- First process anything that comes before any subheadings, or a second h5 -->
          <xsl:if test="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5[2] or preceding::h6)]|
                        body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5[2] or preceding::h6)]|
                        body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                   preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5[2] or preceding::h6)]">
            <section>
              <xsl:apply-templates select="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5[2] or preceding::h6)]|
                                           body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5[2] or preceding::h6)]|
                                           body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                                      preceding::table[parent::body] or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5[2] or preceding::h6)]"/>
            </section>
          </xsl:if>
          <!-- Now turn any other headings into sections, with following stuff -->
          <xsl:apply-templates select="body/table|body/h1|body/h2|body/h3|body/h4|body/h5[preceding-sibling::h5]|body/h6|body/h7" mode="create-section-with-following-content"/>
        </xsl:when>
        <xsl:otherwise>
          <!-- First process anything that comes before any subheadings, or a second heading -->
          <xsl:if test="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6[2])]|
                        body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6[2])]|
                        body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                   preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6[2])]">
            <section>
              <xsl:apply-templates select="body/text()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6[2])]|
                                           body/comment()[not(preceding::table or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6[2])]|
                                           body/*[not(self::table or self::h1 or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or
                                                      preceding::table[parent::body] or preceding::h1 or preceding::h2 or preceding::h3 or preceding::h4 or preceding::h5 or preceding::h6[2])]"/>
            </section>
          </xsl:if>
          <!-- Now turn any other headings into sections, with following stuff -->
          <xsl:apply-templates select="body/table|body/h1|body/h2|body/h3|body/h4|body/h5|body/h6[preceding-sibling::h6]|body/h7" mode="create-section-with-following-content"/>
        </xsl:otherwise>
      </xsl:choose>
    </refbody>
    <xsl:call-template name="genrellinks"/>
  </reference>
</xsl:template>


<!-- Task topic template -->

<xsl:template name="gen-task">
  <task xml:lang="{$default-lang}">
    <xsl:call-template name="genidattribute"/>
    <xsl:call-template name="gentitle"/>
    <xsl:call-template name="gentitlealts"/>
    <xsl:call-template name="genprolog"/>
    <taskbody>
      <!--Optional prereq section goes here-->

      <!--context [any child elements with no preceding ol]-->
      <xsl:if test="body/text()[not(preceding-sibling::ol)]|body/comment()[not(preceding-sibling::ol)]|body/*[not(preceding-sibling::ol)][not(self::ol)]">
        <context>
          <xsl:apply-templates select="body/text()[not(preceding-sibling::ol)]|body/comment()[not(preceding-sibling::ol)]|body/*[not(preceding-sibling::ol)][not(self::ol)]"/>
        </context>
      </xsl:if>

      <!--steps [first ol within a body = steps!] -->
      <xsl:if test="body/ol">
        <steps>
          <xsl:apply-templates select="body/ol[1]/li|body/ol[1]/comment()" mode="steps"/>
        </steps>
      </xsl:if>

      <!--result [any children with a preceding ol]-->
      <xsl:if test="body/text()[preceding-sibling::ol]|body/comment()[preceding-sibling::ol]|body/*[preceding-sibling::ol]">
        <result>
          <xsl:apply-templates select="body/text()[preceding-sibling::ol]|body/comment()[preceding-sibling::ol]|body/*[preceding-sibling::ol]"/>
        </result>
      </xsl:if>

      <!--Optional example section-->
      <!--Optional postreq section-->

    </taskbody>
    <xsl:call-template name="genrellinks"/>
  </task>
</xsl:template>

<!-- this template handle ol/li processing within a task -->
<!-- The default behavior is to put each <li> into a <step>. If this is being
     used to create substeps, the $steptype parameter is passed in as "substep".
     If the <li> does not contain blocklike info, put everything in <cmd>. Otherwise,
     put everything up to the first block into <cmd>. Everything from the first block
     on will be placed in substeps (if it is an OL) or in <info> (everything else).  -->
<xsl:template match="li" mode="steps">
  <xsl:param name="steptype">step</xsl:param>
  <xsl:element name="{$steptype}">
    <xsl:apply-templates select="@class"/>
    <xsl:choose>
      <xsl:when test="not(p|div|ol|ul|table|dl|pre)">
        <cmd><xsl:apply-templates select="*|comment()|text()"/></cmd>
      </xsl:when>
      <xsl:otherwise>
        <cmd><xsl:apply-templates select="(./*|./text())[1]" mode="step-cmd"/></cmd>
        <xsl:apply-templates select="(p|div|ol|ul|table|dl|pre|comment())[1]" mode="step-child"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:element>
</xsl:template>
<xsl:template match="comment()" mode="steps">
  <xsl:apply-templates select="."/>
</xsl:template>

<!-- Add content to a <cmd>. If this is block like, stop iterating and return to the li.
     Otherwise, output the current node using normal processing, and move to the next
     text or element node. -->
<xsl:template match="p|div|ol|ul|table|dl|pre" mode="step-cmd"/>
<xsl:template match="text()|*" mode="step-cmd">
  <xsl:apply-templates select="."/>
  <xsl:apply-templates select="(following-sibling::*|following-sibling::text())[1]" mode="step-cmd"/>
</xsl:template>

<!-- If an ol is inside a step, convert it to substeps. If it is inside substeps, put it in info.
     For any other elements, create an info, and output the current node. Also output the
     following text or element node, which will work up to any <ol>. -->
<xsl:template match="ol" mode="step-child">
  <xsl:choose>
    <!-- If already in substeps -->
    <xsl:when test="parent::li/parent::ol/parent::li/parent::ol">
      <info><xsl:apply-templates select="."/></info>
    </xsl:when>
    <xsl:otherwise>
      <substeps>
        <xsl:apply-templates select="li" mode="steps">
          <xsl:with-param name="steptype">substep</xsl:with-param>
        </xsl:apply-templates>
      </substeps>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="(following-sibling::*|following-sibling::text())[1]" mode="step-child"/>
</xsl:template>
<xsl:template match="text()|*|comment()" mode="step-child">
  <xsl:choose>
    <xsl:when test="self::* or string-length(normalize-space(.))>0">
      <info>
        <xsl:apply-templates select="."/>
        <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="add-to-info"/>
      </info>
    </xsl:when>
    <xsl:otherwise>
      <!-- Ignore empty text nodes and empty comments, move on to the next node -->
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="step-child"/>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:apply-templates select="following-sibling::ol[1]" mode="step-child"/>
</xsl:template>

<!-- When adding to <info>, if an ol is found, stop: it will become substeps, or its own info.
     Anything else: output the element, and then output the following text or element node,
     remaining inside <info>. -->
<xsl:template match="ol" mode="add-to-info"/>
<xsl:template match="*|text()|comment()" mode="add-to-info">
    <xsl:apply-templates select="."/>
    <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="add-to-info"/>
</xsl:template>

<!-- Support for generating contextually dependent ID for topics. -->
<!-- This will need to be improved; no HTML will have an id, so only the
     otherwise will trigger. Better test: use the filename or first a/@name
 +-->
<!-- NOTE: this is only to be used for the topic element -->
<xsl:template name="genidattribute">
 <xsl:attribute name="id">
  <xsl:choose>
    <xsl:when test="string-length($filename-id)>0"><xsl:value-of select="$filename-id"/></xsl:when>
    <xsl:when test="/html/@id"><xsl:value-of select="/html/@id"/></xsl:when>
    <xsl:otherwise><xsl:value-of select="generate-id(/html)"/></xsl:otherwise>
  </xsl:choose>
</xsl:attribute>
</xsl:template>



<!-- named templates for out of line pulls -->

<!-- 02/12/03 drd: mp says to leave this as linklist, not linkpool, for now -->
<xsl:template name="genrellinks">
<xsl:if test=".//a[@href][not(starts-with(@href,'#'))]">
<related-links>
<linklist><title>Collected links</title>
  <xsl:for-each select=".//a[@href][not(starts-with(@href,'#'))]">
    <link>
      <xsl:call-template name="genlinkattrs"/>
      <linktext><xsl:value-of select="."/></linktext>
      <xsl:if test="@title">
        <desc><xsl:value-of select="normalize-space(@title)"/></desc>
      </xsl:if>
    </link>
  </xsl:for-each>
</linklist>
</related-links>
</xsl:if>
</xsl:template>

<xsl:template name="genlinkattrs">
  <xsl:variable name="newfn">
    <xsl:value-of select="substring-before(@href,'.htm')"/>
  </xsl:variable>
  <xsl:choose>
    <!-- If the target is a web site, do not change extension to .dita -->
    <xsl:when test="starts-with(@href,'http:') or starts-with(@href,'https:') or
                    starts-with(@href,'ftp:')">
      <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
      <xsl:attribute name="scope">external</xsl:attribute>
      <xsl:attribute name="format">
        <xsl:choose>
          <xsl:when test="contains(@href,'.pdf') or contains(@href,'.PDF')">pdf</xsl:when>
          <xsl:otherwise>html</xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
    </xsl:when>
    <xsl:when test="string-length($newfn)>0">
      <xsl:attribute name="href"><xsl:value-of select="$newfn"/><xsl:value-of select="$dita-extension"/></xsl:attribute>
    </xsl:when>
    <xsl:when test="starts-with(@href,'#')">
      <xsl:variable name="infile-reference">
        <xsl:text>#</xsl:text>
        <!-- Need to udpate this if genidattribute changes -->
        <xsl:choose>
          <xsl:when test="string-length($filename-id)>0"><xsl:value-of select="$filename-id"/></xsl:when>
          <xsl:when test="/html/@id"><xsl:value-of select="/html/@id"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="generate-id(/html)"/></xsl:otherwise>
        </xsl:choose>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="substring-after(@href,'#')"/>
      </xsl:variable>
      <!-- output-message? -->
      <xsl:attribute name="href"><xsl:value-of select="$infile-reference"/></xsl:attribute>
    </xsl:when>
    <xsl:otherwise>
      <xsl:attribute name="href"><xsl:value-of select="@href"/></xsl:attribute>
      <xsl:attribute name="format">
        <xsl:choose>
          <xsl:when test="contains(@href,'.pdf') or contains(@href,'.PDF')">pdf</xsl:when>
          <xsl:otherwise>html</xsl:otherwise>  <!-- Default to html -->
        </xsl:choose>
      </xsl:attribute>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="@target='_blank'">
    <xsl:attribute name="scope">external</xsl:attribute>
  </xsl:if>
</xsl:template>

<!-- gentitle was here -->

<xsl:template name="genprolog">
<xsl:if test=".//meta[@name][not(@name='generator' or @name='GENERATOR')]|head/comment()"><!-- produce only if qualifiend meta is extant -->
  <prolog>
    <!--xsl:comment>author, copyright, critdates, permissions, publisher, source</xsl:comment-->
    <metadata>
      <xsl:apply-templates select="head/comment()"/>
      <xsl:apply-templates select=".//meta[not(@name='generator' or @name='GENERATOR')]" mode="outofline"/>
    </metadata>
  </prolog>
</xsl:if>
</xsl:template>



<!-- TBD: do anything rational with scripts or styles in the head? elsewhere? -->
<!-- 05232002 drd: null out scripts, flat out (script in head was nulled out before, 
                   but scripts in body were coming through)
-->
<xsl:template match="script"/>
<xsl:template match="style"/>


<!-- take out some other interactive, non-content gadgets that are not part of the DITA source model -->
<!-- TBD: consider adding messages within these -->
<xsl:template match="textarea"/>
<xsl:template match="input"/>
<xsl:template match="isindex"/>
<xsl:template match="select"/>
<xsl:template match="optgroup"/>
<xsl:template match="option"/>
<xsl:template match="label"/>
<xsl:template match="fieldset"/>
<xsl:template match="basefont"/>
<xsl:template match="col"/>
<xsl:template match="colgroup"/>



<!-- ========== Start of heading-aware code =============== -->


<!-- Generic treatment for all headings (1-9!).  The main title and section level code -->
<!-- have higher priorities that override getting triggered by this generic rule. -->

<xsl:template name="cleanup-heading">
  <xsl:call-template name="output-message">
      <xsl:with-param name="msg">A <xsl:value-of select="name()"/> heading could not be converted into DITA.
The heading has been placed in a required-cleanup element.</xsl:with-param>
  </xsl:call-template>
  <required-cleanup>
    <p>
      <b>[deprecated heading <xsl:value-of select="name()"/> ]: </b>
      <xsl:apply-templates select="*|comment()|text()"/>
    </p>
  </required-cleanup>
</xsl:template>

<xsl:template match="h1" priority="5">
  <xsl:choose>
    <xsl:when test="not(preceding::h1)"/>
    <xsl:when test="$infotype='task'"><xsl:call-template name="cleanup-heading"/></xsl:when>
    <xsl:when test="$main-head-level='h1'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h2" priority="5">
  <xsl:choose>
    <xsl:when test="$main-head-level='h2' and not(preceding::h2)"/>
    <xsl:when test="$infotype='task'"><xsl:call-template name="cleanup-heading"/></xsl:when>
    <xsl:when test="$main-head-level='h1' or $main-head-level='h2'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h3" priority="5">
  <xsl:choose>
    <xsl:when test="$main-head-level='h3' and not(preceding::h3)"/>
    <xsl:when test="$infotype='task'"><xsl:call-template name="cleanup-heading"/></xsl:when>
    <xsl:when test="$main-head-level='h2' or $main-head-level='h3'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h4" priority="5">
  <xsl:choose>
    <xsl:when test="$main-head-level='h4' and not(preceding::h4)"/>
    <xsl:when test="$infotype='task'"><xsl:call-template name="cleanup-heading"/></xsl:when>
    <xsl:when test="$main-head-level='h3' or $main-head-level='h4'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h5" priority="5">
  <xsl:choose>
    <xsl:when test="$main-head-level='h5' and not(preceding::h5)"/>
    <xsl:when test="$infotype='task'"><xsl:call-template name="cleanup-heading"/></xsl:when>
    <xsl:when test="$main-head-level='h4' or $main-head-level='h5'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h6" priority="5">
  <xsl:choose>
    <xsl:when test="$main-head-level='h6' and not(preceding::h6)"/>
    <xsl:when test="$infotype='task'"><xsl:call-template name="cleanup-heading"/></xsl:when>
    <xsl:when test="$main-head-level='h5' or $main-head-level='h6'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h7" priority="5">
  <xsl:choose>
    <xsl:when test="$main-head-level='h6'"><xsl:call-template name="gensection"/></xsl:when>
    <xsl:otherwise><xsl:call-template name="cleanup-heading"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template match="h8|h9">
  <xsl:call-template name="cleanup-heading"/>
</xsl:template>

<!-- Templates used to pull content following headings into the generated section -->
<xsl:template match="h1|h2|h3|h4|h5|h6|h7" mode="add-content-to-section"/>
<xsl:template match="*|text()|comment()" mode="add-content-to-section">
  <xsl:choose>
  <!-- For reference, tables also create a section, so leave them out. Otherwise, they go inside sections. -->
    <xsl:when test="self::table and $infotype='reference'"/>
    <xsl:otherwise>
      <xsl:apply-templates select="."/>
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="add-content-to-section"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="convert-heading-to-section">
  <section>
    <title><xsl:apply-templates select="@class"/><xsl:apply-templates select="*|comment()|text()"/></title>
    <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="add-content-to-section"/>
  </section>
</xsl:template>
<xsl:template match="h1|h2|h3|h4|h5|h6|h7" mode="create-section-with-following-content">
  <xsl:choose>
    <xsl:when test="$main-head-level='h1' and (self::h1 or self::h2)">
      <xsl:call-template name="convert-heading-to-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h2' and (self::h2 or self::h3)">
      <xsl:call-template name="convert-heading-to-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h3' and (self::h3 or self::h4)">
      <xsl:call-template name="convert-heading-to-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h4' and (self::h4 or self::h5)">
      <xsl:call-template name="convert-heading-to-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h5' and (self::h5 or self::h6)">
      <xsl:call-template name="convert-heading-to-section"/>
    </xsl:when>
    <xsl:when test="$main-head-level='h6' and (self::h6 or self::h7)">
      <xsl:call-template name="convert-heading-to-section"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">A <xsl:value-of select="name()"/> heading could not be converted into DITA.
The heading has been placed in a required-cleanup element.</xsl:with-param>
      </xsl:call-template>
      <section>
        <required-cleanup>
          <title><xsl:apply-templates select="@class"/><xsl:apply-templates select="*|text()|comment()"/></title>
          <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="add-content-to-section"/>
        </required-cleanup>
      </section>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
<!-- The next template can only be called when processing items in the reference body -->
<xsl:template match="table" mode="create-section-with-following-content">
  <xsl:apply-templates select="."/>
  <xsl:if test="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1][not(self::table or self::h2 or self::h3 or self::h4 or self::h5 or self::h6 or self::h7)]">
    <section>
      <xsl:apply-templates select="(following-sibling::*|following-sibling::text()|following-sibling::comment())[1]" mode="add-content-to-section"/>
    </section>
  </xsl:if>
</xsl:template>

<!-- Special treatment for headings that occur at a section level -->
<xsl:template name="gensection">
  <section>
    <xsl:variable name="hcnt"><xsl:number/></xsl:variable>
    <!--<xsl:value-of select="$hcnt"/>-->
    <title><xsl:apply-templates select="@class"/><xsl:apply-templates select="*|text()|comment()"/></title>
    <!-- call recursively for subsequent chunks -->
    <xsl:call-template name="output-message">
      <xsl:with-param name="msg">A <xsl:value-of select="name()"/> heading was mapped to an empty section.
Move any content that belongs with that heading into the section.</xsl:with-param>
    </xsl:call-template>
  </section>
</xsl:template>


<!-- ========== Start of overrideable heading level code =============== -->

<!-- Default: h1=topic title; h2=section title; all others=bold text -->
<!-- For plain text pull (no problems with content in headings!), use xsl:value-of -->
<!-- (ie, if you use xsl:apply-templates select, you might get unwanted elements in title) -->
<!-- These templates will be overridden by heading-level aware front ends -->
<!-- Note: The generic heading processor treats all headings as priority=1;
           priority=2 in this master transform will override the generic heading processor
           priority=3 in the overrides will override this h1/h2 default setup
 +--> 


<!-- === initially define the defaults for h1/h2 topic/section mappings === -->

<xsl:template name="gentitle">
  <title>
    <xsl:choose>
      <xsl:when test="$main-head-level='h1'"><xsl:value-of select=".//h1[1]"/></xsl:when>
      <xsl:when test="$main-head-level='h2'"><xsl:value-of select=".//h2[1]"/></xsl:when>
      <xsl:when test="$main-head-level='h3'"><xsl:value-of select=".//h3[1]"/></xsl:when>
      <xsl:when test="$main-head-level='h4'"><xsl:value-of select=".//h4[1]"/></xsl:when>
      <xsl:when test="$main-head-level='h5'"><xsl:value-of select=".//h5[1]"/></xsl:when>
      <xsl:when test="$main-head-level='h6'"><xsl:value-of select=".//h6[1]"/></xsl:when>
    </xsl:choose>
  </title>
</xsl:template>

<xsl:template name="gentitlealts">
  <xsl:variable name="create-searchtitle">
    <xsl:choose>
      <xsl:when test="not(/html/head/title)">NO</xsl:when>
      <xsl:when test="$main-head-level='h1' and normalize-space(string(//h1[1]))=normalize-space(string(/html/head/title))">NO</xsl:when>
      <xsl:when test="$main-head-level='h2' and normalize-space(string(//h2[1]))=normalize-space(string(/html/head/title))">NO</xsl:when>
      <xsl:when test="$main-head-level='h3' and normalize-space(string(//h3[1]))=normalize-space(string(/html/head/title))">NO</xsl:when>
      <xsl:when test="$main-head-level='h4' and normalize-space(string(//h4[1]))=normalize-space(string(/html/head/title))">NO</xsl:when>
      <xsl:when test="$main-head-level='h5' and normalize-space(string(//h5[1]))=normalize-space(string(/html/head/title))">NO</xsl:when>
      <xsl:when test="$main-head-level='h6' and normalize-space(string(//h6[1]))=normalize-space(string(/html/head/title))">NO</xsl:when>
      <xsl:otherwise>YES</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:if test="$create-searchtitle='YES'">
    <titlealts>
      <searchtitle>
        <xsl:value-of select="/html/head/title"/>
      </searchtitle>
    </titlealts>
  </xsl:if>
</xsl:template>

<!-- null out some things pulled later -->
<!--<xsl:template match="h1" priority="2"/>

<xsl:template match="h2" priority="2">
  <xsl:call-template name="gensection"/>
</xsl:template> -->

<!-- ========== End of overrideable heading level code =============== -->



<!-- null out some things pulled later -->
<xsl:template match="head"/>
<xsl:template match="title"/>

<!-- Clear up faux "related-links" that are already pulled into collected links:
      eg, template match="br-with-nothing but imgs, links, and brs after it"-->
<!-- These rules attempt to leave behind any images or links that are part of a
     discourse context, and use the rule "br followed by image or anchor" as what
     to interpret as a hand-built "related links" construct. -->
<!-- 03/28/2003: Moved <br> processing into a single "br" template -->     
<!-- 03/28/2003: Removal of <a> does not work, because body overrides; move into
                 "a" template for now, though it does not work well -->     

<!--eliminate a br following an img or an a element -->
<!-- <xsl:template match="*[self::br][following-sibling::*[1][self::img|self::a]]"/> -->
<!--eliminate an a-link preceded by a br-->
<!--  <xsl:template match="a[@href][preceding-sibling::br]"/> -->
<!--eliminate an img preceded by a br-->
<!--  <xsl:template match="img[preceding-sibling::br]"/> -->
<!--eliminate a final br in a context (usually just for vertical space)-->
<!-- <xsl:template match="br[name(following-sibling::*)='']"/> -->

<!-- body: fall through, since its contexts (refbody, conbody, etc.) will be
     generated by templates above -->

<xsl:template match="body">
  <xsl:apply-templates/>
</xsl:template>


<!-- divs: if we can base transform on a class, do so. -->
<!-- generic divs will fall through with no associated transform -->

<xsl:template match="div">
  <xsl:apply-templates select="*|text()|comment()"/>
</xsl:template>


<!-- some characteristics of HTML coming from DocBook tools -->
<xsl:template match="div[@class='footnote']">
<lq>
  <xsl:if test="@id"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute></xsl:if>
  <xsl:apply-templates select="*|text()|comment()"/>
</lq>
</xsl:template>

<!-- this comes from IDWB XHTML output... content that replicates existing structure, therefore null out -->
<xsl:template match="div[@class='toc']">
</xsl:template>


<!-- map these common elements straight through -->
<xsl:template match="cite|p|dl|ol|ul|li|pre|sub|sup|b|u|i">
<xsl:variable name="giname"><xsl:value-of select="name()"/></xsl:variable>
<xsl:variable name="outgi"><xsl:value-of select="translate($giname,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz')"/></xsl:variable>
<xsl:element name="{$outgi}">
  <xsl:if test="@compact and (self::ol|self::ul|self::dl)">
      <xsl:attribute name="compact">yes</xsl:attribute>
  </xsl:if>
  <xsl:apply-templates select="@class"/>
  <xsl:apply-templates select="*|text()|comment()"/>
</xsl:element>
</xsl:template>
<!-- @outputclass is not allowed on these in DITA, so do not process @class-->
<!-- @outputclass is now allowed, so move these back into the rule above -->
<!--<xsl:template match="b|u|i">
<xsl:variable name="giname"><xsl:value-of select="name()"/></xsl:variable>
<xsl:variable name="outgi"><xsl:value-of select="translate($giname,'BITU','bitu')"/></xsl:variable>
<xsl:element name="{$outgi}">
  <xsl:apply-templates select="*|text()|comment()"/>
</xsl:element>
</xsl:template>-->

<xsl:template match="@class">
  <xsl:attribute name="outputclass"><xsl:value-of select="."/></xsl:attribute>
</xsl:template>

<!-- empty elements  -->

<!-- This template will return true() if there is nothing left in this topic except
     a series of related links. Those links will be gathered in the <related-links> section.
     If this is in the related links, return true(). Otherwise, return false(). 
     The tests are:
     If not a child of body, return false (keep this in output)
     If there are text nodes following, return false
     If there are no nodes following, return true (part of the links, so drop it)
     If there are following elements OTHER than br or a, return false
     Otherwise, this is a br or a at the end -->
<xsl:template name="only-related-links-remain">
  <xsl:choose>
    <xsl:when test="not(parent::body)">false</xsl:when>
    <xsl:when test="following-sibling::text()">false</xsl:when>
    <xsl:when test="not(following-sibling::*)">true</xsl:when>
    <xsl:when test="following-sibling::*[not(self::br or self::a)]">false</xsl:when>
    <xsl:otherwise>true</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="br">
  <xsl:variable name="skip-related-links">
    <xsl:call-template name="only-related-links-remain"/>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="$skip-related-links='true'"/>
    <xsl:when test="following-sibling::*[1][self::img]/following-sibling::*[1][self::br]"/>
    <xsl:when test="preceding-sibling::*[1][self::img]/preceding-sibling::*[1][self::br]"/>
    <xsl:when test="following-sibling::text()|following-sibling::*[not(self::a)]">
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">CLEANUP ACTION: Determine the original intent for a BR tag.</xsl:with-param>
      </xsl:call-template>
      <xsl:comment>A BR tag was used here in the original source.</xsl:comment>
    </xsl:when>
    <xsl:otherwise/> <!-- Skip br if it ends a section, or only has links following -->
  </xsl:choose>
</xsl:template>


<xsl:template match="meta[@name]" mode="outofline">
  <othermeta name="{@name}" content="{@content}"/>
</xsl:template>

<xsl:template match="img[@usemap][@src]">
  <xsl:variable name="mapid"><xsl:value-of select="substring-after(@usemap,'#')"/></xsl:variable>
  <imagemap>
    <xsl:apply-templates select="@class"/>
    <image href="{@src}">
      <xsl:apply-templates select="@alt"/>
      </image>
    <xsl:apply-templates select="//map[@id=$mapid or @name=$mapid]" mode="usemap"/>
  </imagemap>
</xsl:template>

<xsl:template match="map"/>
<xsl:template match="map" mode="usemap">
  <xsl:apply-templates/>
</xsl:template>
<xsl:template match="area">
  <area>
      <shape><xsl:value-of select="@shape"/></shape>
      <coords><xsl:value-of select="@coords"/></coords>
      <xref>
          <xsl:call-template name="genlinkattrs"/>
          <xsl:value-of select="@alt"/>
      </xref>
  </area>
</xsl:template>

<xsl:template match="img">
      <image href="{@src}">
        <!-- 03/28/2003 RDA: inline is default, so only worry about break -->
        <!-- <xsl:if test="name(parent::*)='p'"><xsl:attribute name="placement">inline</xsl:attribute></xsl:if>
        <xsl:if test="name(parent::*)='li'"><xsl:attribute name="placement">inline</xsl:attribute></xsl:if> -->
        <xsl:if test="preceding-sibling::*[1][self::br]|following-sibling::*[1][self::br]">
          <xsl:attribute name="placement">break</xsl:attribute>
        </xsl:if>
        <xsl:apply-templates select="@class"/>
        <xsl:apply-templates select="@alt"/>
      </image>
</xsl:template>

<xsl:template match="img/@alt">
  <alt><xsl:value-of select="."/></alt>
</xsl:template>

<xsl:template match="hr">
<xsl:comment> ===================== horizontal rule ===================== </xsl:comment>
</xsl:template>


<!-- renames -->

<xsl:template match="code">
  <codeph><xsl:apply-templates select="@class|*|text()|comment()"/></codeph>
</xsl:template>

<xsl:template match="var">
  <varname><xsl:apply-templates select="@class"/><xsl:value-of select="."/></varname>
</xsl:template>

<xsl:template match="samp">
  <systemoutput><xsl:apply-templates select="@class|*|text()|comment()"/></systemoutput>
</xsl:template>

<xsl:template match="kbd">
  <userinput><xsl:apply-templates select="@class|*|text()|comment()"/></userinput>
</xsl:template>


<xsl:template match="em">
  <i><xsl:apply-templates select="@class|*|text()|comment()"/></i>
</xsl:template>

<xsl:template match="strong">
  <b><xsl:apply-templates select="@class|*|text()|comment()"/></b>
</xsl:template>

<xsl:template match="blockquote">
  <lq><xsl:apply-templates select="@class|*|text()|comment()"/></lq>
</xsl:template>

<!-- <lq> in <lq> is invalid in DITA, so make it valid (though it is a bit strange) -->
<xsl:template match="blockquote/blockquote">
  <p><lq><xsl:apply-templates select="@class|*|text()|comment()"/></lq></p>
</xsl:template>

<xsl:template match="pre" priority="3">
  <codeblock><xsl:apply-templates select="@class|*|text()|comment()"/></codeblock>
</xsl:template>

<!-- assume that these elements are used in tech docs with a semantic intent... -->
<xsl:template match="tt">
  <codeph><xsl:apply-templates select="@class|*|text()|comment()"/></codeph>
</xsl:template>

<xsl:template match="i" priority="3">
  <varname><xsl:apply-templates select="@class"/><xsl:value-of select="."/></varname>
</xsl:template>


<!-- Linking -->

<!-- May try to eliminate groups of related links at the end; if there is a <br>
     followed only by links, ignore them, and let the Collected Links get them.
     Doesn't work now: if a title is entirely a link, it's the last link, so it's ignored... -->
<xsl:template match="a">
  <xsl:variable name="skip-related-links">
    <xsl:call-template name="only-related-links-remain"/>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="@name and $skip-related-links!='true'">
      <xsl:comment>Removed anchor point <xsl:value-of select="@name"/></xsl:comment>
    </xsl:when>
    <xsl:when test="@id and $skip-related-links!='true'">
      <xsl:comment>Removed anchor point <xsl:value-of select="@id"/></xsl:comment>
    </xsl:when>
  </xsl:choose>
  <xsl:choose>
    <xsl:when test="$skip-related-links='true'"/>
    <!-- If a heading is a link, do not create an XREF or it will be out of context -->
    <xsl:when test="parent::h1|parent::h2|parent::h3|parent::h4|parent::h5|parent::h6|parent::h7">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:choose>
        <xsl:when test="@href and parent::body">
          <p><xref>
            <xsl:call-template name="genlinkattrs"/>
            <xsl:apply-templates select="@class"/>
            <xsl:apply-templates select="*|text()|comment()"/>
          </xref></p>
        </xsl:when>
        <xsl:when test="@href">
          <xref>
            <xsl:call-template name="genlinkattrs"/>
            <xsl:apply-templates select="@class"/>
            <xsl:apply-templates select="*|text()|comment()"/>
          </xref>
        </xsl:when>
        <xsl:when test="parent::body and text()">
          <p><xsl:apply-templates select="@class"/><xsl:apply-templates select="*|text()|comment()"/></p>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="*|text()|comment()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- HTML table to CALS table -->

<xsl:template match="td|th" mode="count-cols">
  <xsl:param name="current-count">1</xsl:param>
  <xsl:variable name="current-span">
    <xsl:choose>
      <xsl:when test="@colspan"><xsl:value-of select="@colspan"/></xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="following-sibling::th or following-sibling::td">
      <xsl:apply-templates select="(following-sibling::th|following-sibling::td)[1]" mode="count-cols">
        <xsl:with-param name="current-count"><xsl:value-of select="number($current-span) + number($current-count)"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:when test="@colspan">
      <xsl:value-of select="number($current-span) + number($current-count) - 1"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="$current-count"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
      

<xsl:template match="table">
<xsl:variable name="cols-in-first-row">
  <xsl:choose>
    <xsl:when test="tbody/tr">
      <xsl:apply-templates select="(tbody[1]/tr[1]/td[1]|tbody[1]/tr[1]/th[1])[1]" mode="count-cols"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="(tr[1]/td[1]|tr[1]/th[1])[1]" mode="count-cols"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:variable>
<xsl:variable name="width">
    <xsl:if test="@width"><xsl:value-of select="substring-before(@width,'%')"/></xsl:if>
</xsl:variable>
<xsl:if test="@summary and not(@summary='')">
    <xsl:comment><xsl:value-of select="@summary"/></xsl:comment>
    <xsl:call-template name="output-message">
        <xsl:with-param name="msg">The summary attribute on tables cannot be converted to DITA.
The attribute's contents were placed in a comment before the table.</xsl:with-param>
    </xsl:call-template>
</xsl:if>
<table>
    <xsl:apply-templates select="@class"/>
    <xsl:if test="@align"><xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute></xsl:if>
    <xsl:choose>
        <xsl:when test="number($width) &lt; 100"><xsl:attribute name="pgwide">0</xsl:attribute></xsl:when>
        <xsl:when test="string-length($width)"><xsl:attribute name="pgwide">1</xsl:attribute></xsl:when>
    </xsl:choose>
    <xsl:choose>
        <xsl:when test="@rules='none' and @border='0'">
            <xsl:attribute name="frame">none</xsl:attribute>
            <xsl:attribute name="rowsep">0</xsl:attribute>
            <xsl:attribute name="colsep">0</xsl:attribute>
        </xsl:when>
        <xsl:when test="@border='0'">
            <xsl:attribute name="rowsep">0</xsl:attribute>
            <xsl:attribute name="colsep">0</xsl:attribute>
        </xsl:when>
        <xsl:when test="@rules='cols'">
            <xsl:attribute name="rowsep">0</xsl:attribute>
        </xsl:when>
        <xsl:when test="@rules='rows'">
            <xsl:attribute name="colsep">0</xsl:attribute>
        </xsl:when>
    </xsl:choose>
    <xsl:choose>
      <xsl:when test="@frame='void'"><xsl:attribute name="frame">none</xsl:attribute></xsl:when>
      <xsl:when test="@frame='above'"><xsl:attribute name="frame">top</xsl:attribute></xsl:when>
      <xsl:when test="@frame='below'"><xsl:attribute name="frame">bottom</xsl:attribute></xsl:when>
      <xsl:when test="@frame='border'"><xsl:attribute name="frame">all</xsl:attribute></xsl:when>
      <xsl:when test="@frame='box'"><xsl:attribute name="frame">all</xsl:attribute></xsl:when>
      <xsl:when test="@frame='hsides'"><xsl:attribute name="frame">topbot</xsl:attribute></xsl:when>
      <xsl:when test="@frame='lhs'"><xsl:attribute name="frame">sides</xsl:attribute></xsl:when>
      <xsl:when test="@frame='rhs'"><xsl:attribute name="frame">sides</xsl:attribute></xsl:when>
      <xsl:when test="@frame='vsides'"><xsl:attribute name="frame">sides</xsl:attribute></xsl:when>
    </xsl:choose>

  <xsl:apply-templates select="caption"/>
<tgroup>
<!-- add colspan data here -->
<xsl:attribute name="cols"><xsl:value-of select="$cols-in-first-row"/></xsl:attribute>
<xsl:call-template name="create-colspec">
  <xsl:with-param name="total-cols"><xsl:value-of select="$cols-in-first-row"/></xsl:with-param>
</xsl:call-template>
<xsl:choose>
  <xsl:when test="thead">
    <thead><xsl:apply-templates select="thead/tr"/></thead>
  </xsl:when>
  <xsl:when test="tr[th and not(td)]">
    <thead><xsl:apply-templates select="tr[th and not(td)]">
    <!--ideally, do for-each only for rows that contain TH, and place within THEAD;
        then open up the TBODY for the rest of the rows -->
    <!-- unforch, all the data will go into one place for now -->
    </xsl:apply-templates></thead>
  </xsl:when>
</xsl:choose>
<tbody>
  <xsl:apply-templates select="tbody/tr[td]|tr[td]"/>
</tbody></tgroup></table>
</xsl:template>

<xsl:template name="create-colspec">
  <xsl:param name="total-cols">0</xsl:param>
  <xsl:param name="on-column">1</xsl:param>
  <xsl:if test="$on-column &lt;= $total-cols">
    <colspec>
      <xsl:attribute name="colname">col<xsl:value-of select="$on-column"/></xsl:attribute>
      <xsl:if test="@align"><xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute></xsl:if>
    </colspec>
    <xsl:call-template name="create-colspec">
      <xsl:with-param name="total-cols"><xsl:value-of select="$total-cols"/></xsl:with-param>
      <xsl:with-param name="on-column"><xsl:value-of select="$on-column + 1"/></xsl:with-param>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<xsl:template match="table/caption">
  <title><xsl:apply-templates select="@class|*|text()|comment()"/></title>
</xsl:template>

<xsl:template match="tr">
<row>
    <xsl:if test="@valign"><xsl:attribute name="valign"><xsl:value-of select="@valign"/></xsl:attribute></xsl:if>
    <xsl:apply-templates select="@class"/>
    <xsl:apply-templates/>
</row>
</xsl:template>

<xsl:template match="td|th">
<entry>
  <xsl:if test="@rowspan">
    <xsl:attribute name="morerows"><xsl:value-of select="number(@rowspan)-1"/></xsl:attribute>
  </xsl:if>
  <xsl:if test="@colspan">  <!-- Allow entries to span columns -->
    <xsl:variable name="current-cell"><xsl:call-template name="current-cell-position"/></xsl:variable>
    <xsl:attribute name="namest">col<xsl:value-of select="$current-cell"/></xsl:attribute>
    <xsl:attribute name="nameend">col<xsl:value-of select="$current-cell + number(@colspan) - 1"/></xsl:attribute>
  </xsl:if>
  <xsl:choose>
      <xsl:when test="@align"><xsl:attribute name="align"><xsl:value-of select="@align"/></xsl:attribute></xsl:when>
      <xsl:when test="../@align"><xsl:attribute name="align"><xsl:value-of select="../@align"/></xsl:attribute></xsl:when>
  </xsl:choose>
  <xsl:apply-templates select="@class"/>
  <xsl:choose>
    <xsl:when test="table"><p><xsl:apply-templates select="*|text()|comment()"/></p></xsl:when>
    <xsl:otherwise><xsl:apply-templates select="*|text()|comment()"/></xsl:otherwise>
  </xsl:choose>
</entry>
</xsl:template>

<!-- Determine which column the current entry sits in. Count the current entry,
     plus every entry before it; take spanned rows and columns into account.
     If any entries in this table span rows, we must examine the entire table to
     be sure of the current column. Use mode="find-matrix-column".
     Otherwise, we just need to examine the current row. Use mode="count-cells". -->
<xsl:template name="current-cell-position">
  <xsl:choose>
    <xsl:when test="parent::tr/parent::thead">
      <xsl:apply-templates select="(ancestor::table[1]/thead/tr/*[1])[1]"
                           mode="find-matrix-column">
        <xsl:with-param name="stop-id"><xsl:value-of select="generate-id(.)"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:when test="ancestor::table[1]//*[@rowspan][1]">
      <xsl:apply-templates select="(ancestor::table[1]/tbody/tr/*[1]|ancestor::table[1]/tr/*[1])[1]"
                           mode="find-matrix-column">
        <xsl:with-param name="stop-id"><xsl:value-of select="generate-id(.)"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:when>
    <xsl:when test="not(preceding-sibling::td|preceding-sibling::th)">1</xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="(preceding-sibling::th|preceding-sibling::td)[last()]" mode="count-cells"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Count the number of cells in the current row. Move backwards from the test cell. Add one
     for each entry, plus the number of spanned columns. -->
<xsl:template match="*" mode="count-cells">
  <xsl:param name="current-count">1</xsl:param>
  <xsl:variable name="new-count">
    <xsl:choose>
      <xsl:when test="@colspan"><xsl:value-of select="$current-count + number(@colspan)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="$current-count + 1"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="not(preceding-sibling::td|preceding-sibling::th)"><xsl:value-of select="$new-count"/></xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates select="(preceding-sibling::th|preceding-sibling::td)[last()]" mode="count-cells">
        <xsl:with-param name="current-count"><xsl:value-of select="$new-count"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Set up a pseudo-matrix to find the column of the current entry. Start with the first entry
     in the first row. Progress to the end of the row, then start the next row; go until we find
     the test cell (with id=$stop-id).
     If an entry spans rows, add the cells that will be covered to $matrix.
     If we get to an entry and its position is already filled in $matrix, then the entry is pushed
     to the side. Add one to the column count and re-try the entry. -->
<xsl:template match="*" mode="find-matrix-column">
  <xsl:param name="stop-id"/>
  <xsl:param name="matrix"/>
  <xsl:param name="row-count">1</xsl:param>
  <xsl:param name="col-count">1</xsl:param>
  <!-- $current-position has the format [1:3] for row 1, col 3. Use to test if this cell is covered. -->
  <xsl:variable name="current-position">[<xsl:value-of select="$row-count"/>:<xsl:value-of select="$col-count"/>]</xsl:variable>
  
  <xsl:choose>
    <!-- If the current value is already covered, increment the column number and try again. -->
    <xsl:when test="contains($matrix,$current-position)">
      <xsl:apply-templates select="." mode="find-matrix-column">
        <xsl:with-param name="stop-id"><xsl:value-of select="$stop-id"/></xsl:with-param>
        <xsl:with-param name="matrix"><xsl:value-of select="$matrix"/></xsl:with-param>
        <xsl:with-param name="row-count"><xsl:value-of select="$row-count"/></xsl:with-param>
        <xsl:with-param name="col-count"><xsl:value-of select="$col-count + 1"/></xsl:with-param>
      </xsl:apply-templates>
    </xsl:when>
    <!-- If this is the cell we are testing, return the current column number. -->
    <xsl:when test="generate-id(.)=$stop-id">
      <xsl:value-of select="$col-count"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Figure out what the next column value will be. -->
      <xsl:variable name="next-col-count">
        <xsl:choose>
          <xsl:when test="not(following-sibling::*)">1</xsl:when>
          <xsl:when test="@colspan"><xsl:value-of select="$col-count + number(@colspan) - 1"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="$col-count + 1"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <!-- Determine any values that need to be added to the matrix, if this entry spans rows. -->
      <xsl:variable name="new-matrix-values">
        <xsl:if test="@rowspan">
          <xsl:call-template name="add-to-matrix">
            <xsl:with-param name="start-row"><xsl:value-of select="number($row-count)"/></xsl:with-param>
            <xsl:with-param name="end-row"><xsl:value-of select="number($row-count) + number(@rowspan) - 1"/></xsl:with-param>
            <xsl:with-param name="start-col"><xsl:value-of select="number($col-count)"/></xsl:with-param>
            <xsl:with-param name="end-col">
              <xsl:choose>
                <xsl:when test="@colspan"><xsl:value-of select="number($col-count) + number(@colspan) - 1"/></xsl:when>
                <xsl:otherwise><xsl:value-of select="number($col-count)"/></xsl:otherwise>
              </xsl:choose>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:variable>
      <xsl:choose>
        <!-- If there are more entries in this row, move to the next one. -->
        <xsl:when test="following-sibling::*">
          <xsl:apply-templates select="following-sibling::*[1]" mode="find-matrix-column">
            <xsl:with-param name="stop-id"><xsl:value-of select="$stop-id"/></xsl:with-param>
            <xsl:with-param name="matrix"><xsl:value-of select="$matrix"/><xsl:value-of select="$new-matrix-values"/></xsl:with-param>
            <xsl:with-param name="row-count"><xsl:value-of select="$row-count"/></xsl:with-param>
            <xsl:with-param name="col-count"><xsl:value-of select="$next-col-count"/></xsl:with-param>
          </xsl:apply-templates>
        </xsl:when>
        <!-- Otherwise, move to the first entry in the next row. -->
        <xsl:otherwise>
          <xsl:apply-templates select="../following-sibling::tr[1]/*[1]" mode="find-matrix-column">
            <xsl:with-param name="stop-id"><xsl:value-of select="$stop-id"/></xsl:with-param>
            <xsl:with-param name="matrix"><xsl:value-of select="$matrix"/><xsl:value-of select="$new-matrix-values"/></xsl:with-param>
            <xsl:with-param name="row-count"><xsl:value-of select="$row-count + 1"/></xsl:with-param>
            <xsl:with-param name="col-count"><xsl:value-of select="1"/></xsl:with-param>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- This template returns values that must be added to the table matrix. Every cell in the box determined
     by start-row, end-row, start-col, and end-col will be added. First add every value from the first
     column. When past $end-row, move to the next column. When past $end-col, every value is added. -->
<xsl:template name="add-to-matrix">
  <xsl:param name="start-row"/>       
  <xsl:param name="end-row"/>
  <xsl:param name="current-row"><xsl:value-of select="$start-row"/></xsl:param>
  <xsl:param name="start-col"/>
  <xsl:param name="end-col"/>
  <xsl:param name="current-col"><xsl:value-of select="$start-col"/></xsl:param>
  <xsl:choose>
    <xsl:when test="$current-col > $end-col"/>   <!-- Out of the box; every value has been added -->
    <xsl:when test="$current-row > $end-row">    <!-- Finished with this column; move to next -->
      <xsl:call-template name="add-to-matrix">
        <xsl:with-param name="start-row"><xsl:value-of select="$start-row"/></xsl:with-param>
        <xsl:with-param name="end-row"><xsl:value-of select="$end-row"/></xsl:with-param>
        <xsl:with-param name="current-row"><xsl:value-of select="$start-row"/></xsl:with-param>
        <xsl:with-param name="start-col"><xsl:value-of select="$start-col"/></xsl:with-param>
        <xsl:with-param name="end-col"><xsl:value-of select="$end-col"/></xsl:with-param>
        <xsl:with-param name="current-col"><xsl:value-of select="$current-col + 1"/></xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <!-- Output the value for the current entry -->
      <xsl:text>[</xsl:text>
      <xsl:value-of select="$current-row"/>:<xsl:value-of select="$current-col"/>
      <xsl:text>]</xsl:text>
      <!-- Move to the next row, in the same column. -->
      <xsl:call-template name="add-to-matrix">
        <xsl:with-param name="start-row"><xsl:value-of select="$start-row"/></xsl:with-param>
        <xsl:with-param name="end-row"><xsl:value-of select="$end-row"/></xsl:with-param>
        <xsl:with-param name="current-row"><xsl:value-of select="$current-row + 1"/></xsl:with-param>
        <xsl:with-param name="start-col"><xsl:value-of select="$start-col"/></xsl:with-param>
        <xsl:with-param name="end-col"><xsl:value-of select="$end-col"/></xsl:with-param>
        <xsl:with-param name="current-col"><xsl:value-of select="$current-col"/></xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="tbody|tfoot|thead">
  <xsl:apply-templates/>
</xsl:template>

<!-- If a table entry contains a paragraph, and nothing but a paragraph, do not
     create the <p> tag in the <entry>. Let everything fall through into <entry>. -->
<xsl:template match="td/p|th/p">
  <xsl:choose>
    <xsl:when test="following-sibling::*|preceding-sibling::*">
      <p><xsl:apply-templates select="@class|*|text()|comment()"/></p>
    </xsl:when>
    <xsl:when test="normalize-space(following-sibling::text()|preceding-sibling::text())=''">
      <xsl:apply-templates select="*|text()|comment()"/>
    </xsl:when>
    <xsl:otherwise>
      <p><xsl:apply-templates select="@class|*|text()|comment()"/></p>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="span[@class='bold']">
  <b>
    <xsl:apply-templates select="*|text()|comment()"/>
  </b>
</xsl:template>

<xsl:template match="span[@class='italic']">
  <i>
    <xsl:apply-templates select="*|text()|comment()"/>
  </i>
</xsl:template>

<xsl:template match="span[@class='bold-italic']">
  <b><i>
    <xsl:apply-templates select="*|text()|comment()"/>
  </i></b>
</xsl:template>


<!-- case of span with no attributes at all -->

<xsl:template match="span[not(string(@*))]">
  <ph>
    <xsl:apply-templates select="*|text()|comment()"/>
  </ph>
</xsl:template>

<!-- Search for span styles that Tidy moved into /html/head/style
     Each known value adds something to the return value, such as [b] for bold.
     The returned value is parsed to determine which wrappers to create.
     New values can be added here; processing for the new value will need
     to be merged into the sequential b/i/u/tt processing below. -->
<xsl:template name="get-span-style">
  <xsl:variable name="classval"><xsl:value-of select="@class"/></xsl:variable>
  <xsl:variable name="searchval">span.<xsl:value-of select="$classval"/></xsl:variable>
  <xsl:variable name="span-style">
    <xsl:value-of select="substring-before(substring-after(/html/head/style/text(),$searchval),'}')"/>}<xsl:text/>
  </xsl:variable>
  <xsl:if test="contains($span-style,'font-weight:bold') or contains($span-style,'font-weight :bold') or
                contains($span-style,'font-weight: bold') or 
                contains($span-style,'font-weight : bold')">[b]</xsl:if>
  <xsl:if test="contains($span-style,'font-style:italic') or contains($span-style,'font-style :italic') or
                contains($span-style,'font-style: italic') or 
                contains($span-style,'font-style : italic')">[i]</xsl:if>
  <xsl:if test="contains($span-style,'text-decoration: underline') or contains($span-style,'text-decoration :underline') or
                contains($span-style,'text-decoration: underline') or 
                contains($span-style,'text-decoration : underline')">[u]</xsl:if>
  <xsl:if test="contains($span-style,'font-family:Courier') or contains($span-style,'font-family :Courier') or
                contains($span-style,'font-family: Courier') or 
                contains($span-style,'font-family : Courier')">[tt]</xsl:if>
  <xsl:if test="contains($span-style,'font-weight:normal') or contains($span-style,'font-weight :normal') or
                contains($span-style,'font-weight: normal') or 
                contains($span-style,'font-weight : normal')">[normal]</xsl:if>
</xsl:template>

<!-- Process a span with a tidy-created class. It is known to have one or more
     values from b, i, u, or tt. For each value, create the element if needed,
     and move on to the next one, passing the style value from /html/head/style -->
<xsl:template name="bold-span">
  <xsl:param name="span-style"/>
  <xsl:choose>
    <xsl:when test="contains($span-style,'[b]')">
      <b><xsl:call-template name="italic-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></b>
    </xsl:when>
    <xsl:otherwise><xsl:call-template name="italic-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template name="italic-span">
  <xsl:param name="span-style"/>
  <xsl:choose>
    <xsl:when test="contains($span-style,'[i]')">
      <i><xsl:call-template name="underline-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></i>
    </xsl:when>
    <xsl:otherwise><xsl:call-template name="underline-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template name="underline-span">
  <xsl:param name="span-style"/>
  <xsl:choose>
    <xsl:when test="contains($span-style,'[u]')">
      <u><xsl:call-template name="courier-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></u>
    </xsl:when>
    <xsl:otherwise><xsl:call-template name="courier-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template name="courier-span">
  <xsl:param name="span-style"/>
  <xsl:choose>
    <xsl:when test="contains($span-style,'[tt]')">
      <tt><xsl:call-template name="normal-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></tt>
    </xsl:when>
    <xsl:otherwise><xsl:call-template name="normal-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<xsl:template name="normal-span">
  <xsl:param name="span-style"/>
  <xsl:choose>
    <!-- If a span has "normal" style and nothing else, create <ph> -->
    <xsl:when test="contains($span-style,'[normal]') and 
                    substring-before($span-style,'[normal]')='' and
                    substring-after($span-style,'[normal]')=''">
      <ph><xsl:apply-templates select="*|text()|comment()"/></ph>
    </xsl:when>
    <xsl:otherwise><xsl:apply-templates select="*|text()|comment()"/></xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="span">
  <xsl:choose>
    <xsl:when test="@class='bold-italic'">
      <b><i><xsl:apply-templates select="*|text()|comment()"/></i></b>
    </xsl:when>
    <!-- If the span has a value created by tidy, parse /html/head/style -->
    <xsl:when test="@class='c1' or @class='c2' or @class='c3' or
                    @class='c4' or @class='c5' or @class='c6' or
                    @class='c7' or @class='c8' or @class='c9'">
      <xsl:variable name="span-style"><xsl:call-template name="get-span-style"/></xsl:variable>
      <xsl:choose>
        <xsl:when test="string-length($span-style)>0">
          <xsl:call-template name="bold-span"><xsl:with-param name="span-style" select="$span-style"/></xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:variable name="searchval">span.<xsl:value-of select="@class"/></xsl:variable>
          <xsl:variable name="orig-span-style">
            <xsl:value-of select="substring-before(substring-after(/html/head/style/text(),$searchval),'}')"/>}<xsl:text/>
          </xsl:variable>
          <xsl:call-template name="output-message">
            <xsl:with-param name="msg">CLEANUP ACTION: provide a better phrase markup for a SPAN tag.
The element's contents have been placed in a phrase element.
There is a comment next to the phrase with the span's class value.</xsl:with-param>
          </xsl:call-template>
          <xsl:comment>Original: &lt;span @class=<xsl:value-of select="@class"/>&gt;, <xsl:value-of select="@class"/>=<xsl:value-of select="$orig-span-style"/></xsl:comment>
          <ph><xsl:apply-templates select="*|text()|comment()"/></ph>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="output-message">
        <xsl:with-param name="msg">CLEANUP ACTION: provide a better phrase markup for a SPAN tag.
The element's contents have been placed in a phrase element.
There is a comment next to the phrase with the span's class value.</xsl:with-param>
      </xsl:call-template>
      <xsl:comment>Original: &lt;span @class=<xsl:value-of select="@class"/>&gt;</xsl:comment>
      <ph><xsl:apply-templates select="@class"/><xsl:apply-templates select="*|text()|comment()"/></ph>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- generate dlentry wrapper for DL/DD/DT -->

<xsl:template match="dt">
<dlentry>
 <dt><xsl:apply-templates select="@class|*|text()|comment()"/></dt>
 <xsl:apply-templates select="following-sibling::*[1]" mode="indirect"/>
</dlentry>
</xsl:template>

<xsl:template match="dd"/>

<xsl:template match="dt" mode="indirect"/>
<xsl:template match="dd" mode="indirect">
  <dd>
    <xsl:apply-templates select="@class|*|text()|comment()"/>
  </dd>
  <xsl:apply-templates select="following-sibling::*[1]" mode="indirect"/>
</xsl:template>


<!-- named templates -->

<!--
<xsl:template name="sect1topic">
<topic xml:lang="{$default-lang}">
  <xsl:if test="@id"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute></xsl:if>
  <xsl:apply-templates/>
</topic>
</xsl:template>
-->


<!-- things noted for disambiguation -->

<!-- encapsulate text within body -->
<xsl:template match="body/text()|body/div/text()">
  <xsl:variable name="bodytxt"><xsl:value-of select="normalize-space(.)"/></xsl:variable>
  <xsl:if test="string-length($bodytxt)>0">
    <!-- issue a message here? Not EVERY time, puleeeze. test for first node if we must... -->
    <p>
      <xsl:value-of select="."/>
    </p>
  </xsl:if>
  <!-- text nodes get wrapped; blanks fall through -->
</xsl:template>

<!-- encapsulate phrases within body -->
<xsl:template match="body/i|body/div/i" priority="4">
  <p><i><xsl:apply-templates select="@class|*|text()|comment()"/></i></p>
</xsl:template>
<xsl:template match="body/b|body/div/b" priority="4">
  <p><b><xsl:apply-templates select="@class|*|text()|comment()"/></b></p>
</xsl:template>
<xsl:template match="body/u|body/div/u" priority="4">
  <p><u><xsl:apply-templates select="@class|*|text()|comment()"/></u></p>
</xsl:template>

<!-- 03/28/2003 RDA: consolidate all <a> processing into single template -->
<!-- <xsl:template match="body/a" priority="4">
  <xsl:choose>
    <xsl:when test="@name">
      <xsl:comment>Removed anchor point <xsl:value-of select="@name"/></xsl:comment>
    </xsl:when>
    <xsl:when test="@id">
      <xsl:comment>Removed anchor point <xsl:value-of select="@id"/></xsl:comment>
    </xsl:when>
  </xsl:choose>
  <xsl:if test="@href">
    <p>
      <xref>
        <xsl:call-template name="genlinkattrs"/>
        <xsl:apply-templates/>
      </xref>
    </p>
  </xsl:if>
</xsl:template> -->


<!-- case of deprecated elements with no clear migrational intent -->

<xsl:template match="small|big">
  <xsl:call-template name="output-message">
      <xsl:with-param name="msg">CLEANUP ACTION: provide a better phrase markup for a BIG or SMALL tag.
The element's contents have been placed in a required-cleanup element.</xsl:with-param>
  </xsl:call-template>
  <required-cleanup>
    <xsl:attribute name="remap"><xsl:value-of select="name()"/></xsl:attribute>
    <ph>
      <xsl:apply-templates select="@class|*|text()|comment()"/>
    </ph>
  </required-cleanup>
</xsl:template>


<xsl:template match="s|strike">
  <xsl:call-template name="output-message">
      <xsl:with-param name="msg">CLEANUP ACTION: provide a better phrase markup for a strikethrough tag.
The element's contents have been placed in a required-cleanup element.</xsl:with-param>
  </xsl:call-template>
  <required-cleanup>
    <xsl:attribute name="remap"><xsl:value-of select="name()"/></xsl:attribute>
    <ph>
      <xsl:apply-templates select="@class|*|text()|comment()"/>
    </ph>
  </required-cleanup>
</xsl:template>

<!-- set of rules for faux-pre sections (paragraphs with br, using samp for font effect)-->

<xsl:template match="p[samp][not(text())]">
  <pre>
   <xsl:apply-templates mode="re-pre"/>
  </pre>
</xsl:template>

<xsl:template match="samp" mode="re-pre">
  <xsl:apply-templates mode="re-pre"/>
</xsl:template>

<xsl:template match="samp/br" mode="re-pre"/><!-- won't need introduced space if original source has it -->

<xsl:template match="comment()">
  <xsl:comment><xsl:value-of select="."/></xsl:comment>
</xsl:template>

<!-- =========== CATCH UNDEFINED ELEMENTS (for stylesheet maintainers) =========== -->

<!-- (this rule should NOT produce output in production setting) -->
<xsl:template match="*">
  <xsl:call-template name="output-message">
    <xsl:with-param name="msg">CLEANUP ACTION: no DITA equivalent for HTML element '<xsl:value-of select="name()"/>'.
The element has been placed in a required-cleanup element.</xsl:with-param>
  </xsl:call-template>
  <required-cleanup>
    <xsl:attribute name="remap"><xsl:value-of select="name()"/></xsl:attribute>
    <ph>
      <xsl:apply-templates select="*|text()|comment()"/>
    </ph>
  </required-cleanup>
</xsl:template>

<!-- ====================================================================================== -->
<!-- Special templates for pre-processing an XHTML file, in order to merge sequential lists -->

<!-- If there are not any split lists surrounding this element, copy it as is. If this is between
     two parts of a split list, it will be pulled in to the preceding list item, so ignore it now. -->
<xsl:template match="*|@*|comment()|processing-instruction()|text()" mode="shift-lists">
  <xsl:choose>
    <xsl:when test="not(following-sibling::ol[1][@start]) or not(preceding-sibling::ol)">
      <xsl:copy>
        <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="shift-lists"/>
      </xsl:copy>
    </xsl:when>
    <xsl:otherwise>
      <!-- There is a re-started list after this. Check to make sure the numbers are correct. -->
      <xsl:variable name="supposed-next-start">
        <xsl:choose>
          <xsl:when test="preceding-sibling::ol[1]/@start">
            <xsl:value-of select="number(preceding-sibling::ol[1]/@start) + count(preceding-sibling::ol[1]/li)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="count(preceding-sibling::ol[1]/li) + 1"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <!-- If the next list continues the previous, this element was pulled in to the previous list. -->
        <xsl:when test="$supposed-next-start=following-sibling::ol[1]/@start"/>
        <xsl:otherwise>
          <xsl:copy>
            <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="shift-lists"/>
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Process ordered lists individually. -->
<xsl:template match="ol" mode="shift-lists">
  <xsl:choose>
    <!-- If this could be a continuation, check the @start value against the previous list -->
    <xsl:when test="@start and preceding-sibling::ol">
      <xsl:variable name="supposed-start">
        <xsl:choose>
          <xsl:when test="preceding-sibling::ol[1]/@start">
            <xsl:value-of select="number(preceding-sibling::ol[1]/@start) + count(preceding-sibling::ol[1]/li)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="count(preceding-sibling::ol[1]/li) + 1"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>
        <!-- If this continues the previous list, it was pulled in when processing that list. -->
        <xsl:when test="$supposed-start=@start"/>
        <!-- Otherwise, there's a goof-up somewhere, just copy it to the output -->
        <xsl:otherwise>
          <xsl:copy>
            <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="shift-lists"/>
          </xsl:copy>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <!-- The list does not continue a previous list (though it may start a new one) -->
    <xsl:otherwise>
      <xsl:copy>
        <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="shift-lists"/>
      </xsl:copy>
    </xsl:otherwise>
  </xsl:choose> 
</xsl:template>

<!-- All list items but the last one are copied as-is. The last one is also copied, but we need to check
     for continued lists. If this list is continued, everything between this item and the next list should
     be copied in to the end of this list item. After the item is processed, add all of the list items from
     the continue-ing list. -->
<xsl:template match="ol/li[not(following-sibling::li)]" mode="shift-lists">
  <xsl:variable name="supposed-next-start">
    <xsl:choose>
      <xsl:when test="../@start"><xsl:value-of select="number(../@start) + count(../li)"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="count(../li) + 1"/></xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="shift-lists"/>
    <xsl:if test="../following-sibling::ol[1]/@start and $supposed-next-start=../following-sibling::ol[1]/@start">
      <xsl:variable name="next-id"><xsl:value-of select="generate-id(../following-sibling::ol[1])"/></xsl:variable>
      <xsl:apply-templates 
           select="../following-sibling::text()[generate-id(following-sibling::ol[1])=$next-id]|
                   ../following-sibling::*[generate-id(following-sibling::ol[1])=$next-id]"
           mode="add-to-list"/>
    </xsl:if>
  </xsl:copy>
  <xsl:if test="../following-sibling::ol[1]/@start and $supposed-next-start=../following-sibling::ol[1]/@start">
    <xsl:apply-templates select="../following-sibling::ol[1]/*" mode="shift-lists"/>
  </xsl:if>
</xsl:template>

<!-- Matches anything between 2 lists that are being merged. -->
<xsl:template match="*|@*|comment()|processing-instruction()|text()" mode="add-to-list">
  <xsl:copy>
    <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="add-to-list"/>
  </xsl:copy>
</xsl:template>

<!-- Validate the input parameters -->
<xsl:template name="validate-parameters">
    <xsl:if test="not($infotype='topic' or $infotype='concept' or $infotype='reference' or $infotype='task')">
        <xsl:call-template name="output-message">
            <xsl:with-param name="msg">'<xsl:value-of select="$infotype"/>' is an invalid infotype, use 'topic' as the default infotype.</xsl:with-param>
        </xsl:call-template>
    </xsl:if>
    <xsl:if test="not($dita-extension='.dita' or $dita-extension='.xml')">
        <xsl:call-template name="output-message">
            <xsl:with-param name="msg">'<xsl:value-of select="$dita-extension"/>' is an invalid dita extension, please use '.dita' or '.xml' as the dita extension.</xsl:with-param>
        </xsl:call-template>
    </xsl:if>
    <xsl:if test="not(contains($FILENAME, '.htm'))">
        <xsl:call-template name="output-message">
            <xsl:with-param name="msg">The parameter FILENAME should ends with '.htm' or '.html'.</xsl:with-param>
        </xsl:call-template>
    </xsl:if>
</xsl:template>

</xsl:stylesheet>
