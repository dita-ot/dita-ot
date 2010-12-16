<!-- ............................................................. -->
<!-- File:  tutorialspeclink.xsl                                   -->
<!--                                                               -->
<!-- Purpose:  DITA tutoral specialization linking overrides.      -->
<!--                                                               -->
<!-- ............................................................. -->
<!-- Notes:                                                        -->
<!-- 27.Aug.2005    Created.                                       -->
<!-- ............................................................. -->

<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->

<!-- Turn off validation in jEdit. -->
<!-- :xml.validate=false: -->

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
  exclude-result-prefixes="related-links">

<!-- ............................................................. -->
<!-- Top level elements .......................................... -->

  <xsl:output method="xml"
              encoding="utf-8"
              indent="no"
              />


  <!-- Ensure that tutorial breadcrumb links do not show up in Related Information section -->
  <xsl:key name="omit-from-unordered-links" match="*[@role='tutbreadcrumb' or @role='tutmodulelesson'][contains(@class,' topic/link ')]" use="1"/>

<!-- ............................................................. -->
<!-- Templates ................................................... -->

<!-- Required call to maintain consistency of 'look and feel'. -->

  <xsl:template match="/*[contains(@class, ' tutorialLesson/tutorialLesson ') or contains(@class, ' tutorialSummary/tutorialSummary ') or contains(@class, ' tutorialModule/tutorialModule ') or contains(@class, ' tutorial/tutorial ') or contains(@class, ' tutorialIntro/tutorialIntro ') or contains(@class,' authorInfo/authorInfo ')]" priority="1" name="tutorial_element">
    <xsl:call-template name="tutorial-setup"/>
  </xsl:template>

  <!-- Modify default page layout. -->
  <xsl:template name="tutorial-setup">
    <html>
      <xsl:call-template name="setTopicLanguage"/>
      <xsl:value-of select="$newline"/>
      <xsl:call-template name="chapterHead"/>
      <xsl:call-template name="tutorialBody"/>
    </html>
  </xsl:template>

  <xsl:template name="tutorialBody">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <body>
      <xsl:call-template name="setidaname"/>
      <xsl:value-of select="$newline"/>
      <xsl:call-template name="start-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
      <xsl:call-template name="start-revflag">
        <xsl:with-param name="flagrules" select="$flagrules"/>  
      </xsl:call-template>

      <!-- TutSpec :: breadcrumb -->
      <xsl:choose>
        <xsl:when test="boolean(//*[contains(@class,' topic/link ')][@role='tutbreadcrumb'])">
          <div class="breadcrumb">
            <xsl:call-template name="tutbreadcrumb" />
          </div>
        </xsl:when>
        <xsl:otherwise>
          <div class="breadcrumb"><xsl:comment>&#xA0;</xsl:comment></div>
        </xsl:otherwise>
      </xsl:choose>

      <xsl:call-template name="gen-user-header"/>  <!-- include user's XSL running header here -->
      <xsl:call-template name="processHDR"/>
      <!-- Include a user's XSL call here to generate a toc based on what's a child of topic -->
      <xsl:call-template name="gen-user-sidetoc"/>

      <!-- Insert previous-next links at the top of each topic. -->
      <xsl:call-template name="tutprevnext" />

      <xsl:apply-templates/> <!-- this will include all things within topic; therefore, -->
       <!-- title content will appear here by fall-through -->
       <!-- followed by prolog (but no fall-through is permitted for it) -->
       <!-- followed by body content, again by fall-through in document order -->
       <!-- followed by related links -->
       <!-- followed by child topics by fall-through -->

      <!-- Insert previous-home-next links at the bottom of each topic. -->
      <xsl:call-template name="tutprevnext" />

      <xsl:call-template name="gen-endnotes"/>    <!-- include footnote-endnotes -->
      <xsl:call-template name="gen-user-footer"/> <!-- include user's XSL running footer here -->
      <xsl:call-template name="processFTR"/>      <!-- Include XHTML footer, if specified -->

      <xsl:call-template name="end-revflag">
        <xsl:with-param name="flagrules" select="$flagrules"/>  
      </xsl:call-template>
      <xsl:call-template name="end-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>

    </body>
    <xsl:value-of select="$newline"/>

  </xsl:template>

  <!-- Added new template to match related links. You can adjust this to remove next-prev-parent-links, or to call your own version of child links, related information, etc. -->
  <xsl:template match="*[contains(@class, ' tutorialLesson/tutorialLesson ') or contains(@class, ' tutorialSummary/tutorialSummary ') or contains(@class, ' tutorialModule/tutorialModule ') or contains(@class, ' tutorial/tutorial ') or contains(@class, ' tutorialIntro/tutorialIntro ') or contains(@class,' authorInfo/authorInfo ')]/*[contains(@class,' topic/related-links ')]">
    <div>
       <xsl:call-template name="commonattributes"/>

     <xsl:call-template name="tutorial-ul-child-links"/><!--handle child/descendants outside of linklists in collection-type=unordered or choice-->

     <!--<xsl:call-template name="ol-child-links"/>--><!--handle child/descendants outside of linklists in collection-type=ordered/sequence-->

     <xsl:apply-templates select="." mode="related-links:group-unordered-links">
         <xsl:with-param name="nodes" select="descendant::*[contains(@class, ' topic/link ')]
           [count(. | key('omit-from-unordered-links', 1)) != count(key('omit-from-unordered-links', 1))]
           [generate-id(.)=generate-id((key('hideduplicates', concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ',@href,@scope,@audience,@platform,@product,@otherprops,@rev,@type,normalize-space(child::*))))[1])]"/>
     </xsl:apply-templates>  

     <!-- P021450: Updated tutorial to use latest link grouping code from DITA-OT. Replaced
          the following templates with the single call above. -->
     <!--<xsl:call-template name="concept-links"/>
     <xsl:call-template name="task-links"/>
     <xsl:call-template name="reference-links"/>
     <xsl:call-template name="tutorial-relinfo-links"/>-->

     <!--linklists - last but not least, create all the linklists and their links, with no sorting or re-ordering-->
     <xsl:apply-templates select="*[contains(@class,' topic/linklist ')]"/>
    </div>
  </xsl:template>

  <!-- Override -->
  <!-- Previous link -->
  <xsl:template name="prevlink" match="*[contains(@class, ' topic/link ')][@role='previous'][/*[contains(@class, ' tutorialLesson/tutorialLesson ') or contains(@class, ' tutorialSummary/tutorialSummary ') or contains(@class, ' tutorialModule/tutorialModule ') or contains(@class, ' tutorial/tutorial ') or contains(@class, ' tutorialIntro/tutorialIntro ') or contains(@class,' authorInfo/authorInfo ')]]" priority="2" >
    <xsl:param name="prevnext">
      <xsl:text>Previous</xsl:text>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="(@type='concept' or @type='task' or @type='reference' or @scope='external')">
        <!-- Do nothing. -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="maketutlink">
          <xsl:with-param name="prevnext" select="$prevnext" />
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:choose>
      <xsl:when test="//*[@role='next'][@type!='tutorial'][@type!='concept'][@type!='task'][@type!='reference']" >
        <xsl:text> | </xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <!-- Do nothing. -->
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- Override -->
  <!-- Next link -->
  <xsl:template name="nextlink" match="*[contains(@class, ' topic/link ')][@role='next'][/*[contains(@class, ' tutorialLesson/tutorialLesson ') or contains(@class, ' tutorialSummary/tutorialSummary ') or contains(@class, ' tutorialModule/tutorialModule ') or contains(@class, ' tutorial/tutorial ') or contains(@class, ' tutorialIntro/tutorialIntro ') or contains(@class,' authorInfo/authorInfo ')]]" priority="2" >
    <xsl:param name="prevnext">
      <xsl:text>Next</xsl:text>
    </xsl:param>
      <xsl:choose>
        <xsl:when test="@type='concept' or @type='task' or @type='reference' or @scope='external'">
          <!-- Do nothing. -->
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="maketutlink">
            <xsl:with-param name="prevnext" select="$prevnext" />
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

  <!-- Override -->
  <!-- Make link (original template was 'makelink').-->
  <xsl:template name="maketutlink">
    <xsl:param name="prevnext" />
    <xsl:param name="relInfo" />
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <xsl:call-template name="linkdupinfo"/>
    <xsl:call-template name="start-flagit">
      <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
    </xsl:call-template>
    <xsl:call-template name="start-revflag">
      <xsl:with-param name="flagrules" select="$flagrules"/>  
    </xsl:call-template>
    <xsl:choose>
      <xsl:when test="$prevnext='Previous'">
        <a class="tutorialPrev">
           <xsl:call-template name="commonattributes">
             <xsl:with-param name="default-output-class">tutorialPrev</xsl:with-param>
           </xsl:call-template>
           <xsl:attribute name="href">
             <xsl:call-template name="href"/>
           </xsl:attribute>
           <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
              <xsl:attribute name="target">_blank</xsl:attribute>
           </xsl:if>
          <!--create hover help if desc exists-->
          <xsl:if test="*[contains(@class, ' topic/linktext ')]">
            <xsl:variable name="hoverhelp"><xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]" mode="text-only"/></xsl:variable>
            <xsl:attribute name="title">
              <xsl:value-of select="normalize-space($hoverhelp)"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:text>&lt; </xsl:text>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'tutorialPrevious'"/>
          </xsl:call-template>
        </a>
      </xsl:when>
      <xsl:when test="$prevnext='Next'">
        <a class="tutorialNext">
           <xsl:call-template name="commonattributes">
             <xsl:with-param name="default-output-class">tutorialNext</xsl:with-param>
           </xsl:call-template>
           <xsl:attribute name="href">
             <xsl:call-template name="href"/>
           </xsl:attribute>
           <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
              <xsl:attribute name="target">_blank</xsl:attribute>
           </xsl:if>
          <!--create hover help if desc exists-->
          <xsl:if test="*[contains(@class, ' topic/linktext ')]">
            <xsl:variable name="hoverhelp"><xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]" mode="text-only"/></xsl:variable>
            <xsl:attribute name="title">
              <xsl:value-of select="normalize-space($hoverhelp)"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'tutorialNext'"/>
          </xsl:call-template>
          <xsl:text> &gt;</xsl:text>
        </a>
      </xsl:when>
      <xsl:when test="$relInfo='relInfo'">
        <a>
          <xsl:call-template name="commonattributes"/>
          <xsl:attribute name="href">
            <xsl:call-template name="href"/>
          </xsl:attribute>
          <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
            <xsl:attribute name="target">_blank</xsl:attribute>
          </xsl:if>
          <!--create hover help if desc exists-->
          <xsl:if test="*[contains(@class, ' topic/desc ')]">
            <xsl:variable name="hoverhelp"><xsl:apply-templates select="*[contains(@class, ' topic/desc ')]" mode="text-only"/></xsl:variable>
            <xsl:attribute name="title"><xsl:value-of select="normalize-space($hoverhelp)"/></xsl:attribute>
          </xsl:if>
          <!--use linktext as linktext if it exists, otherwise use href as linktext-->
          <xsl:choose>
            <xsl:when test="*[contains(@class, ' topic/linktext ')]"><xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]"/></xsl:when>
            <xsl:otherwise>
              <!--use href-->
              <xsl:call-template name="href"/>
            </xsl:otherwise>
          </xsl:choose>
       </a>
      </xsl:when>
      <xsl:otherwise>
        <a>
           <xsl:call-template name="commonattributes"/>
           <xsl:attribute name="href">
             <xsl:call-template name="href"/>
           </xsl:attribute>
           <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
              <xsl:attribute name="target">_blank</xsl:attribute>
           </xsl:if>
          <!--create hover help if desc exists-->
          <xsl:if test="*[contains(@class, ' topic/linktext ')]">
            <xsl:variable name="hoverhelp"><xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]" mode="text-only"/></xsl:variable>
            <xsl:attribute name="title">
              <xsl:value-of select="normalize-space($hoverhelp)"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'Previous'"/>
          </xsl:call-template>
        </a>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:call-template name="end-revflag">
      <xsl:with-param name="flagrules" select="$flagrules"/>  
    </xsl:call-template>
    <xsl:call-template name="end-flagit">
      <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="tutorial-relinfo-links">
    <xsl:if test="descendant::*[contains(@class, ' topic/link ')]
      [not(ancestor::*[contains(@class,' topic/linklist ')])]
      [not(@role='child' or @role='descendant' or @role='ancestor' or @role='parent' or @role='next' or @role='previous' or @role='tutbreadcrumb' or @role='tutmodulelesson' or @type='concept' or @type='task' or @type='reference')]
      [not(@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='cousin'))]">
      <div class="relinfo">
        <strong>
          <xsl:call-template name="getString">
            <xsl:with-param name="stringName" select="'Related information'"/>
          </xsl:call-template>
        </strong>
          <xsl:value-of select="$newline"/>
          <!--once section is created, create the links, using the same rules as above plus a uniqueness check-->
          <xsl:for-each select="descendant::*
            [not(ancestor::*[contains(@class,' topic/linklist ')])]
            [generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])]
            [contains(@class, ' topic/link ')]
            [not(@role='child' or @role='descendant' or @role='ancestor' or @role='parent' or @role='next' or @role='previous' or @role='tutbreadcrumb' or @role='tutmodulelesson' or @type='concept' or @type='task' or @type='reference')]
            [not(@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='cousin'))]">
            <div class="tutorialRelatedLink">
              <xsl:call-template name="maketutlink">
                <xsl:with-param name="relInfo" select="'relInfo'" />
              </xsl:call-template>
            </div>
          </xsl:for-each>
        </div>
      <xsl:value-of select="$newline"/>
    </xsl:if>
  </xsl:template>

  <!-- Add class attribute to div element for related topics. -->
  <xsl:template match="*[contains(@class, ' tutorialLesson/tutorialLesson ') or contains(@class, ' tutorialSummary/tutorialSummary ') or contains(@class, ' tutorialModule/tutorialModule ') or contains(@class, ' tutorial/tutorial ') or contains(@class, ' tutorialIntro/tutorialIntro ') or contains(@class,' authorInfo/authorInfo ')]/*[contains(@class,' topic/related-links ')]/*[contains(@class, ' topic/link ')][@type='task' or @type='concept' or @type='reference']">
    <div class="tutorialRelatedLink"><xsl:call-template name="makelink"/></div>
  </xsl:template>

  <xsl:template name="tutorial-ul-child-links">
    <xsl:if test="descendant::*[contains(@class, ' topic/link ')][@role='child' or @role='descendant' or @role='tutmodulelesson'][not(@type='authorInfo')][not(parent::*/@collection-type='sequence')][not(ancestor::*[contains(@class, ' topic/linklist ')])]">
      <xsl:value-of select="$newline"/>
      <!-- Seth added this section for possibly adding a heading before children links -->
      <xsl:choose>
        <xsl:when test="self::*[parent::tutorialIntro]/descendant::*[contains(@class, ' topic/link ')][@type='tutorialModule'][@role='tutmodulelesson']">
          <h4 class="sectiontitle">
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Modules in this tutorial'"/>
            </xsl:call-template>
          </h4>
          <xsl:value-of select="$newline"/>
        </xsl:when>
        <xsl:when test="self::*[parent::tutorialIntro]/descendant::*[contains(@class, ' topic/link ')][@type='tutorialLesson'][@role='tutmodulelesson']">
          <h4 class="sectiontitle">
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Lessons in this tutorial'"/>
            </xsl:call-template>
          </h4>
          <xsl:value-of select="$newline"/>
        </xsl:when>
        <xsl:when test="self::*[parent::tutorialModule]/descendant::*[contains(@class, ' topic/link ')][@type='tutorialLesson'][@role='tutmodulelesson']">
          <h4 class="sectiontitle">
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Lessons in this module'"/>
            </xsl:call-template>
          </h4>
          <xsl:value-of select="$newline"/>
        </xsl:when>
        <xsl:otherwise />
      </xsl:choose>
      <!-- end of Seth section -->
      <ul class="ullinks">
        <xsl:value-of select="$newline"/>
          <xsl:apply-templates select="descendant::*
          [generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])]
          [contains(@class, ' topic/link ')]
          [@role='child' or @role='descendant' or @role='tutmodulelesson']
          [not(parent::*/@collection-type='sequence')]
          [not(ancestor::*[contains(@class, ' topic/linklist ')])]"/>
      </ul>
      <xsl:value-of select="$newline"/>
    </xsl:if>
  </xsl:template>

  <!-- Create child links short descriptions. -->
  <xsl:template match="*[contains(@class, ' topic/link ')]
          [@role='child' or @role='descendant' or @role='tutmodulelesson']
          [/*[contains(@class, ' tutorialLesson/tutorialLesson ') or contains(@class, ' tutorialSummary/tutorialSummary ') or contains(@class, ' tutorialModule/tutorialModule ') or contains(@class, ' tutorial/tutorial ') or contains(@class, ' tutorialIntro/tutorialIntro ') or contains(@class,' authorInfo/authorInfo ')]]" priority="2" name="topic.tutorial.link_child">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <xsl:variable name="el-name">
      <xsl:choose>
        <xsl:when test="contains(../@class,' topic/linklist ')">div</xsl:when>
         <xsl:otherwise>li</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:element name="{$el-name}">
      <xsl:attribute name="class">ulchildlink</xsl:attribute>
      <xsl:call-template name="commonattributes">
        <xsl:with-param name="default-output-class" select="'ulchildlink'"/>
      </xsl:call-template>
      <xsl:call-template name="start-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
      <xsl:call-template name="start-revflag">
        <xsl:with-param name="flagrules" select="$flagrules"/>  
      </xsl:call-template>
      <strong>
        <a>
          <xsl:attribute name="href">
            <xsl:call-template name="href"/>
          </xsl:attribute>
          <xsl:if test="@scope='external' or @type='external' or ((@format='PDF' or @format='pdf') and not(@scope='local'))">
            <xsl:attribute name="target">_blank</xsl:attribute>
          </xsl:if>

          <!--use linktext as linktext if it exists, otherwise use href as linktext-->
          <xsl:choose>
            <xsl:when test="*[contains(@class, ' topic/linktext ')]">
              <xsl:apply-templates select="*[contains(@class, ' topic/linktext ')]"/>
            </xsl:when>
            <xsl:otherwise>
              <!--use href-->
              <xsl:call-template name="href"/>
            </xsl:otherwise>
          </xsl:choose>
        </a>
      </strong>
      <xsl:call-template name="end-revflag">
        <xsl:with-param name="flagrules" select="$flagrules"/>  
      </xsl:call-template>
      <xsl:call-template name="end-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
      <br/><xsl:value-of select="$newline"/>
      <!--add the description on the next line, like a summary-->
      <xsl:apply-templates select="*[contains(@class, ' topic/desc ')]"/>
      <!--     <br clear="all"/> -->
    </xsl:element><xsl:value-of select="$newline"/>
  </xsl:template>

  <!-- TutSpec :: Template to create prev-next links. -->
  <xsl:template name="tutprevnext">
    <xsl:for-each select="descendant::*
    [contains(@class, ' topic/link ')]
    [(@role='next' and
      generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])
    ) or (@role='previous' and
      generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])
    )]/parent::*">

      <xsl:value-of select="$newline"/>

      <!--TutSpec :: prev-next linking -->
      <div class="tutorialPrevNext">
        <xsl:value-of select="$newline"/>

        <xsl:choose>
          <xsl:when test="*[@href][@role='previous']">
            <xsl:for-each select="*[@href][@role='previous']">
              <xsl:apply-templates select="." />
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <!-- Do nothing. -->
          </xsl:otherwise>
        </xsl:choose>

        <xsl:choose>
          <xsl:when test="*[@href][@role='next'][@type!='tutorial'] ">
            <xsl:for-each select="*[@href][@role='next']">
              <xsl:apply-templates select="." />
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <!-- Do nothing. -->
          </xsl:otherwise>
        </xsl:choose>
      </div>

      <xsl:value-of select="$newline"/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="tutbreadcrumb">
    <xsl:for-each select="//*[contains(@class,' topic/link ')][@role='tutbreadcrumb']">
      <xsl:element name="a">
        <xsl:attribute name="href">
          <xsl:value-of select="substring-before(@href, '.dita')"/>
          <xsl:text>.</xsl:text>
          <xsl:value-of select="$OUTEXT"/>   <!--RDA: using $OUTEXT instead of assuming html-->
        </xsl:attribute>
        <xsl:value-of select="linktext" />
      </xsl:element>
      <xsl:text> &gt; </xsl:text>
    </xsl:for-each>
    <!-- Current page
    <xsl:apply-templates select="/descendant::*[contains(@class,' topic/title ')][1]" mode="text-only"/>
    -->
  </xsl:template>

</xsl:stylesheet>

