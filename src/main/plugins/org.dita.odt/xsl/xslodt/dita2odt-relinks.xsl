<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
  xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
  xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
  xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
  xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
  xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
  xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
  xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
  xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
  xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
  xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
  xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
  xmlns:math="http://www.w3.org/1998/Math/MathML"
  xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
  xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
  xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms"
  xmlns:xsd="http://www.w3.org/2001/XMLSchema"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
  xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
  xmlns:prodtools="http://www.ibm.com/xmlns/prodtools"
  
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
  exclude-result-prefixes="related-links ditamsg"
  
  version="1.0">
  
  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>
  
  <xsl:key name="link" match="*[contains(@class, ' topic/link ')][not(ancestor::*[contains(@class, ' topic/linklist ')])]" use="concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*)"/>
  <xsl:key name="linkdup" match="*[contains(@class, ' topic/link ')][not(ancestor::*[contains(@class, ' topic/linklist ')])][not(@role='child' or @role='parent' or @role='previous' or @role='next' or @role='ancestor' or @role='descendant')]" use="concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href)"/>
  <xsl:key name="hideduplicates" match="*[contains(@class, ' topic/link ')][not(ancestor::*[contains(@class, ' topic/linklist ')])][not(@role) or @role='cousin' or @role='external' or @role='friend' or @role='other' or @role='sample' or @role='sibling']" use="concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ',@href,@scope,@audience,@platform,@product,@otherprops,@rev,@type,normalize-space(child::*))"/>
  
  <!-- ========== Hooks for common user customizations ============== -->
  <!-- The following two templates are available for anybody who needs
    to put out an token at the start or end of a link, such as an
    icon to indicate links to PDF files or external web addresses. -->
  <xsl:template match="*" mode="add-link-highlight-at-start"/>
  <xsl:template match="*" mode="add-link-highlight-at-end"/>
  <xsl:template match="*" mode="add-xref-highlight-at-start"/>
  <xsl:template match="*" mode="add-xref-highlight-at-end"/>
  
  <!-- Override this template to add any standard link attributes.
    Called for all links. -->
  <xsl:template match="*" mode="add-custom-link-attributes"/>
  
  <!-- Override these templates to place some a prefix before generated
    child links, such as "Optional" for optional child links. Called
    for all child links. -->
  <xsl:template match="*" mode="related-links:ordered.child.prefix"/>
  <xsl:template match="*" mode="related-links:unordered.child.prefix"/>
  
  
  
  <!-- xref tag -->
  <xsl:template match="*[contains(@class,' topic/xref ')]">
    <xsl:variable name="samefile">
      <xsl:call-template name="check_file_location"/>
    </xsl:variable>
    <xsl:variable name="href-value">
      <xsl:call-template name="format_href_value"/>
    </xsl:variable>
    <xsl:choose>
      <!-- for footnote -->
      <xsl:when test="@type = 'fn'">
        <!-- topic id -->
        <xsl:variable name="topicId" 
          select="substring-before(substring-after(@href, '#'), '/')"/>
        <!-- element id -->
        <xsl:variable name="elementId" select="substring-after(@href, '/')"/>
        <!-- get footnote text -->
        <xsl:variable name="fntext" select="//*[contains(@class, ' topic/topic ')][@id = $topicId]
          //*[contains(@class, ' topic/fn ')][@id = $elementId]/text()"/>
        
        <xsl:variable name="fnNumber">
          <xsl:choose>
            <xsl:when test="*[not(contains(@class,' topic/desc '))]|text()">
              <xsl:apply-templates select="*[not(contains(@class,' topic/desc '))]|text()"/>
            </xsl:when>
            <xsl:otherwise>
              <!--use xref content-->
              <!-- 
                <xsl:call-template name="href"/>
              -->
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:element name="text:note">
          <xsl:attribute name="text:note-class">footnote</xsl:attribute>
          <xsl:element name="text:note-citation">
            <xsl:attribute name="text:label">
              <xsl:value-of select="$fnNumber"/>
            </xsl:attribute>
            <xsl:value-of select="$fnNumber"/>
          </xsl:element>
          <xsl:element name="text:note-body">
            <xsl:element name="text:p">
              <xsl:attribute name="text:style-name">footnote</xsl:attribute>
              <xsl:value-of select="$fntext"/>
            </xsl:element>
          </xsl:element>
        </xsl:element> 
      </xsl:when>
      <!-- TODO -->
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="@href and not(@href='')">
            <xsl:element name="text:a">
              <xsl:choose>
                <xsl:when test="$samefile='true'">
                  <xsl:attribute name="xlink:href">
                    <xsl:value-of select="$href-value"/>
                  </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="NORMAMLIZEDOUTPUT" select="translate($OUTPUTDIR, '\', '/')"/>
                  <xsl:attribute name="xlink:href">
                    <xsl:value-of select="concat($FILEREF, $NORMAMLIZEDOUTPUT, '/', $href-value)"/>
                  </xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
              <xsl:call-template name="gen-linktxt"/>
              <xsl:if test="contains(@class,' topic/link ')">
                <xsl:apply-templates select="*[contains(@class,' topic/desc ')]"/>
                <xsl:element name="text:line-break"/>
              </xsl:if>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">028</xsl:with-param>
              <xsl:with-param name="msgsev">E</xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- ignore desc tag under xref or link -->
  <xsl:template match="*[contains(@class,' topic/xref ') or contains(@class, ' topic/link ')]/*[contains(@class,' topic/desc ')]" priority="1"/>
  
  <!--create breadcrumbs for each grouping of ancestor links; include previous, next, and ancestor links, sorted by linkpool/related-links parent. If there is more than one linkpool that contains ancestors, multiple breadcrumb trails will be generated-->
  <xsl:template match="*[contains(@class,' topic/related-links ')]" mode="breadcrumb">
    <xsl:for-each select="descendant-or-self::*[contains(@class,' topic/related-links ') or contains(@class,' topic/linkpool ')][child::*[@role='ancestor']]">
      <xsl:element name="text:p">
        <xsl:choose>
          <!--output previous link first, if it exists-->
          <xsl:when test="*[@href][@role='previous']">
            <xsl:apply-templates select="*[@href][@role='previous'][1]" mode="breadcrumb"/>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
        <!--if both previous and next links exist, output a separator bar-->
        <xsl:if test="*[@href][@role='next'] and *[@href][@role='previous']">
          <xsl:text> | </xsl:text>
        </xsl:if>
        <xsl:choose>
          <!--output next link, if it exists-->
          <xsl:when test="*[@href][@role='next']">
            <xsl:apply-templates select="*[@href][@role='next'][1]" mode="breadcrumb"/>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
        <!--if we have either next or previous, plus ancestors, separate the next/prev from the ancestors with a vertical bar-->
        <xsl:if test="(*[@href][@role='next'] or *[@href][@role='previous']) and *[@href][@role='ancestor']">
          <xsl:text> | </xsl:text>
        </xsl:if>
        <!--if ancestors exist, output them, and include a greater-than symbol after each one, including a trailing one-->
        <xsl:if test="*[@href][@role='ancestor']">
          <xsl:for-each select="*[@href][@role='ancestor']">
            <xsl:apply-templates select="."/> &gt;
          </xsl:for-each>
        </xsl:if>
      </xsl:element>
    </xsl:for-each>
  </xsl:template>
  
  <!--create prerequisite links with all dups eliminated. -->
  <!-- Omit prereq links from unordered related-links (handled by mode="prereqs" template). -->
  <xsl:key name="omit-from-unordered-links" match="*[@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='cousin')]" use="1"/>
  <xsl:template match="*[contains(@class,' topic/related-links ')]" mode="prereqs">
    
    <!--if there are any prereqs create a list with dups-->
    <xsl:if test="descendant::*[contains(@class, ' topic/link ')][not(ancestor::*[contains(@class, ' topic/linklist ')])][@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='previous' or @role='cousin')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">default_text_style</xsl:attribute>
          <xsl:call-template name="getStringODT">
            <xsl:with-param name="stringName" select="'Prerequisites'"/>
          </xsl:call-template>
        </xsl:element>
      </xsl:element>
      
      <!--only create link if there is an href, its importance is required, and the role is compatible (don't want a prereq showing up for a "next" or "parent" link, for example) - remove dups-->
      <xsl:apply-templates mode="prereqs" select="descendant::*[generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])]
        [@href]
        [@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='previous' or @role='cousin')]
        [not(ancestor::*[contains(@class, ' topic/linklist ')])]"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template mode="prereqs" match="*[contains(@class, ' topic/link ')]" priority="2">
    
    <!-- Allow for unknown metadata (future-proofing) -->
    <xsl:apply-templates select="*[contains(@class,' topic/data ') or contains(@class,' topic/foreign ')]"/>
    <xsl:variable name="samefile">
      <xsl:call-template name="check_file_location"/>
    </xsl:variable>
    <xsl:variable name="href-value">
      <xsl:call-template name="format_href_value"/>
    </xsl:variable>
    <xsl:element name="text:p">
      <xsl:call-template name="create_related_links">
        <xsl:with-param name="samefile" select="$samefile"/>
        <xsl:with-param name="href-value" select="$href-value"/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' topic/linkinfo ')]" name="topic.linkinfo">
    
    <!-- 
    <xsl:element name="text:line-break"/>
    <xsl:element name="text:span">
      <xsl:apply-templates/>
    </xsl:element>
    -->
    <xsl:element name="text:list-item">
      <xsl:element name="text:p">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:element>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/linklist ')]">
    <xsl:element name="text:list">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/linklist ')]/*[contains(@class, ' topic/title ')]" name="topic.linklist_title">
    <xsl:element name="text:list-item">
      <xsl:element name="text:p">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/linklist ')]/*[contains(@class, ' topic/desc ')]" name="topic.linklist_desc" priority="2">
    <xsl:element name="text:list-item">
      <xsl:element name="text:p">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- related-links -->
  <!--main template for setting up all links after the body - applied to the related-links container-->
  <xsl:template match="*[contains(@class,' topic/related-links ')]" name="topic.related-links">
    
    
    <xsl:if test="normalize-space($includeRelatedLinkRoles)">
      <xsl:element name="text:list">
        <xsl:attribute name="text:style-name">list_style_without_bullet</xsl:attribute>
        <xsl:element name="text:list-item">
        <!--handle child/descendants outside of linklists in collection-type=unordered or choice-->
      
        <xsl:call-template name="ul-child-links"/>
  
        <!--handle child/descendants outside of linklists in collection-type=ordered/sequence-->
  
        <xsl:call-template name="ol-child-links"/>
    
        <!--handle next and previous links-->
        <!-- 
        <xsl:call-template name="next-prev-parent-links"/>
        -->
        <!-- Group all unordered links (which have not already been handled by prior sections). Skip duplicate links. -->
        <!-- NOTE: The actual grouping code for related-links:group-unordered-links is common between
          transform types, and is located in ../common/related-links.xsl. Actual code for
          creating group titles and formatting links is located in XSL files specific to each type. -->
        <xsl:apply-templates select="." mode="related-links:group-unordered-links">
          <xsl:with-param name="nodes" select="descendant::*[contains(@class, ' topic/link ')]
            [count(. | key('omit-from-unordered-links', 1)) != count(key('omit-from-unordered-links', 1))]
            [generate-id(.)=generate-id((key('hideduplicates', concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ',@href,@scope,@audience,@platform,@product,@otherprops,@rev,@type,normalize-space(child::*))))[1])]"/>
        </xsl:apply-templates>  
        
        <!--linklists - last but not least, create all the linklists and their links, with no sorting or re-ordering-->
        <xsl:apply-templates select="*[contains(@class,' topic/linklist ')]"/>
        </xsl:element>
      </xsl:element>
    </xsl:if>
    
  </xsl:template>
  
  <!--children links - handle all child or descendant links except those in linklists or ordered collection-types.
    Each child is indented, the linktext is bold, and the shortdesc appears in normal text directly below the link, to create a summary-like appearance.-->
  <xsl:template name="ul-child-links">
    <xsl:if test="descendant::*[contains(@class, ' topic/link ')][@role='child' or @role='descendant'][not(parent::*/@collection-type='sequence')][not(ancestor::*[contains(@class, ' topic/linklist ')])]">
      
      <xsl:element name="text:line-break"/>
        <!--once you've tested that at least one child/descendant exists, apply templates to only the unique ones-->
        <xsl:apply-templates select="descendant::*
          [generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])]
          [contains(@class, ' topic/link ')]
          [@role='child' or @role='descendant']
          [not(parent::*/@collection-type='sequence')]
          [not(ancestor::*[contains(@class, ' topic/linklist ')])]"/>
    </xsl:if>
  </xsl:template>
  
  <!--children links - handle all child or descendant links in ordered collection-types.
    Children are displayed in a numbered list, with the target title as the cmd and the shortdesc as info, like a task.
  -->
  <xsl:template name="ol-child-links">
    <xsl:if test="descendant::*[contains(@class, ' topic/link ')][@role='child' or @role='descendant'][parent::*/@collection-type='sequence'][not(ancestor::*[contains(@class, ' topic/linklist ')])]">
      <xsl:element name="text:list">
        <xsl:attribute name="text:style-name">ordered_list_style</xsl:attribute>
        <!--once you've tested that at least one child/descendant exists, apply templates to only the unique ones-->
        <xsl:apply-templates select="descendant::*
          [generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])]
          [contains(@class, ' topic/link ')]
          [@role='child' or @role='descendant']
          [parent::*/@collection-type='sequence']
          [not(ancestor-or-self::*[contains(@class, ' topic/linklist ')])]"/>
      </xsl:element>
    </xsl:if>
  </xsl:template>
  
  <!-- Omit child and descendant links from unordered related-links (handled by ul-child-links and ol-child-links). -->
  <xsl:key name="omit-from-unordered-links" match="*[@role='child']" use="1"/>
  <xsl:key name="omit-from-unordered-links" match="*[@role='descendant']" use="1"/>
  
  
  <xsl:template name="gen-linktxt">
    <xsl:choose>
      <xsl:when test="contains(@class,' topic/xref ')">
        <xsl:choose>
          <xsl:when test="text() or *">
            <xsl:apply-templates/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="contains(@class,' topic/link ')">
        <xsl:choose>
          <xsl:when test="*[contains(@class,' topic/linktext ')]">
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="*[contains(@class,' topic/linktext ')]"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="text()">
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="text()"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <!--Get Related Information for topic type-->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='topic']" priority="1">
    <xsl:param name="links"/>
    
    <xsl:variable name="samefile">
      <xsl:call-template name="check_file_location"/>
    </xsl:variable>
    <xsl:variable name="href-value">
      <xsl:call-template name="format_href_value"/>
    </xsl:variable>

    
    <xsl:element name="text:p">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Related information'"/>
        </xsl:call-template>
      </xsl:element>
    </xsl:element>
    
    <xsl:element name="text:p">
      <xsl:call-template name="create_related_links">
        <xsl:with-param name="samefile" select="$samefile"/>
        <xsl:with-param name="href-value" select="$href-value"/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>
  
  <!--Get Related Information for topic type-->
  <xsl:template match="*[contains(@class, ' topic/link ')]" priority="0">
    <xsl:param name="links"/>
    
    <xsl:variable name="samefile">
      <xsl:call-template name="check_file_location"/>
    </xsl:variable>
    <xsl:variable name="href-value">
      <xsl:call-template name="format_href_value"/>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/linklist ')]">
        <xsl:element name="text:list-item">
          <xsl:element name="text:p">
            <xsl:call-template name="create_related_links">
              <xsl:with-param name="samefile" select="$samefile"/>
              <xsl:with-param name="href-value" select="$href-value"/>
            </xsl:call-template>
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text:line-break"/>
        <xsl:call-template name="create_related_links">
          <xsl:with-param name="samefile" select="$samefile"/>
          <xsl:with-param name="href-value" select="$href-value"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>
  
  
  <!-- same file or not -->
  <xsl:template name="check_file_location">
    <xsl:choose>
      <xsl:when test="@href and starts-with(@href,'#')">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="format_href_value">
    <xsl:choose>
      <xsl:when test="@href and starts-with(@href,'#')">
        <xsl:choose>
          <!-- get element id -->
          <xsl:when test="contains(@href,'/')">
            <xsl:value-of select="concat('#', substring-after(@href,'/'))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="@href and contains(@href,'#')">
        <xsl:value-of select="substring-before(@href,'#')"/>
      </xsl:when>
      <xsl:when test="@href and not(@href='')">
        <xsl:value-of select="@href"/>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  <!-- create related links -->
  <xsl:template name="create_related_links">
    <xsl:param name="samefile"/>
    <xsl:param name="text"/>
    <xsl:param name="href-value"/>
    
    <xsl:choose>
      <xsl:when test="@href and not(@href='')">
        <xsl:element name="text:a">
          <xsl:choose>
            <xsl:when test="$samefile='true'">
              <xsl:attribute name="xlink:href">
                <xsl:value-of select="$href-value"/>
              </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="NORMAMLIZEDOUTPUT" select="translate($OUTPUTDIR, '\', '/')"/>
              <xsl:attribute name="xlink:href">
                <xsl:value-of select="concat($FILEREF, $NORMAMLIZEDOUTPUT, '/', $href-value)"/>
              </xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:call-template name="gen-linktxt"/>
          <xsl:if test="contains(@class,' topic/link ')">
            <xsl:apply-templates select="*[contains(@class,' topic/desc ')]"/>
            <xsl:element name="text:line-break"/>
          </xsl:if>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgnum">028</xsl:with-param>
          <xsl:with-param name="msgsev">E</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>

