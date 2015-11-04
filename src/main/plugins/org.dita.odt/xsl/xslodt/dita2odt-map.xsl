<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
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
  xmlns:opentopic="http://www.idiominc.com/opentopic"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  version="2.0" 
  xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
  exclude-result-prefixes="related-links dita-ot opentopic">
  
  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>
  
  
  <xsl:variable name="mapType">
    <xsl:choose>
      <xsl:when test="/*[contains(@class, ' map/map ') and contains(@class, ' bookmap/bookmap ')]">
        <xsl:value-of select="'bookmap'"/>
      </xsl:when>
      <xsl:when test="/*[contains(@class, ' map/map ')]">
        <xsl:value-of select="'ditamap'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'topic'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="map" select="//opentopic:map"/>
  
  <xsl:variable name="topicNumbers">
    <xsl:for-each select="//*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo '))]">
      <topic id="{@id}" guid="{generate-id()}"/>
    </xsl:for-each>
  </xsl:variable>
  
  <xsl:variable name="relatedTopicrefs" select="//*[contains(@class, ' map/reltable ')]//*[contains(@class, ' map/topicref ')]"/>
  
  
  <xsl:template name="create_toc">
    <text:table-of-content text:style-name="Sect1" text:protected="true" text:name="Table of Contents1">
      <text:table-of-content-source text:outline-level="10" text:use-index-marks="false" text:use-index-source-styles="true">
        <text:index-title-template text:style-name="Contents_20_Heading_TOC">Table of
          Contents</text:index-title-template>
        <text:table-of-content-entry-template text:outline-level="1" text:style-name="Contents_20_1_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="2" text:style-name="Contents_20_2_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="3" text:style-name="Contents_20_3_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="4" text:style-name="Contents_20_4_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="5" text:style-name="Contents_20_5_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="6" text:style-name="Contents_20_6_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="7" text:style-name="Contents_20_7_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="8" text:style-name="Contents_20_8_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="9" text:style-name="Contents_20_9_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:table-of-content-entry-template text:outline-level="10" text:style-name="Contents_20_10_a">
          <text:index-entry-link-start/>
          <text:index-entry-chapter/>
          <text:index-entry-span> </text:index-entry-span>
          <text:index-entry-text/>
          <text:index-entry-tab-stop style:type="right" style:leader-char="."/>
          <text:index-entry-page-number/>
          <text:index-entry-link-end/>
        </text:table-of-content-entry-template>
        <text:index-source-styles text:outline-level="1">
          <text:index-source-style text:style-name="Appendix_20_Heading"/>
        </text:index-source-styles>
      </text:table-of-content-source>
      <text:index-body>
        <text:index-title text:style-name="Sect1" text:name="Table of Contents1_Head">
          <text:p text:style-name="Contents_20_Heading">Table of Contents</text:p>
        </text:index-title>
        <xsl:choose>
          <xsl:when test="$map and not($map = '')">
            <xsl:apply-templates select="$map/child::*[contains(@class, ' map/topicref ')]" mode="toc"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="child::*[contains(@class, ' topic/topic ')]" mode="toc"/>
          </xsl:otherwise>
        </xsl:choose>
      </text:index-body>
    </text:table-of-content>
    <!-- page break. -->
    <text:p text:style-name="PB"/>
  </xsl:template>
  
  
  <!-- compress the opentopic:map tag since it has alreay been parsed for creating toc -->
  <xsl:template match="opentopic:map"/>


  <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="toc">
    <xsl:if test="@href">
      <!-- topicref depth -->
      <xsl:variable name="depth" select="count(ancestor-or-self::*[contains(@class, ' map/topicref ')])"/>
      <!-- navtitle value -->
      <xsl:variable name="navtitle">
        <!-- 
        <xsl:value-of select="child::*[contains(@class, ' map/topicmeta ')]/child::*[contains(@class, ' topic/navtitle ')]"/>
        -->
        <xsl:apply-templates select="child::*[contains(@class, ' map/topicmeta ')]/child::*[contains(@class, ' topic/navtitle ')]" mode="dita-ot:text-only"/>
      </xsl:variable>
      <!-- href value -->
      <xsl:variable name="href" select="@href"/>
      <xsl:variable name="level">
        <xsl:choose>
          <xsl:when test="$depth &gt; 10">
            <xsl:value-of select="10"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$depth"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <text:p text:style-name="{concat('P', $level)}">
        <text:a xlink:type="simple" text:style-name="underline_none" xlink:href="{concat($href, '')}">
          <xsl:value-of select="$navtitle"/>
          <text:tab/>
          <text:bookmark-ref text:reference-format="page" text:ref-name="{substring-after($href, '#')}">
          </text:bookmark-ref>
        </text:a>
      </text:p>
    </xsl:if>
    <xsl:apply-templates select="child::*[contains(@class, ' map/topicref ')]" mode="toc"/>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="toc">
    <xsl:if test="*[contains(@class, ' topic/title ')]">
      <!-- topic depth -->
      <xsl:variable name="depth" select="count(ancestor-or-self::*[contains(@class, ' topic/topic ')])"/>
      <!-- title value -->
      <xsl:variable name="title">
        <!-- 
        <xsl:value-of select="child::*[contains(@class, ' topic/title ')]"/>
        -->
        <xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="dita-ot:text-only"/>
      </xsl:variable>
      <!-- href value -->
      <xsl:variable name="href" select="concat('#', @id)"/>
      <xsl:variable name="level">
        <xsl:choose>
          <xsl:when test="$depth &gt; 10">
            <xsl:value-of select="10"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$depth"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <text:p text:style-name="{concat('P', $level)}">
        <text:a xlink:type="simple" text:style-name="underline_none" xlink:href="{concat($href, '')}">
          <xsl:value-of select="$title"/>
          <text:tab/>
          <text:bookmark-ref text:reference-format="page" text:ref-name="{substring-after($href, '#')}">
          </text:bookmark-ref>
        </text:a>
      </text:p>
    </xsl:if>
    <xsl:apply-templates select="child::*[contains(@class, ' topic/topic ')]" mode="toc"/>
  </xsl:template>
  
  <!-- create map title -->
  <xsl:template name="create_map_title">
    <xsl:apply-templates
      select="($map/*[contains(@class, ' topic/title ')],
               /*/@title,
               //opentopic:map/*[contains(@class, ' map/topicref ')][1]
                              /*[contains(@class, ' map/topicmeta ')]
                              /*[contains(@class, ' topic/navtitle ')])[1]"
      mode="create_title"/>
  </xsl:template>
  
  <!-- create topic title -->
  <xsl:template name="create_topic_title">
    <xsl:apply-templates select="/*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]" mode="create_title"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/title ')] | @title | *[contains(@class, ' topic/navtitle ')]" mode="create_title">
    <text:p text:style-name="Title">
      <xsl:apply-templates select="." mode="dita-ot:text-only"/>
    </text:p>
    <!-- page break. -->
    <text:p text:style-name="PB"/>
  </xsl:template>
  
  <!-- create book title -->
  <xsl:template name="create_book_title">
    <xsl:apply-templates select="//opentopic:map/*[contains(@class, ' bookmap/booktitle ')]" mode="create_book_title"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' bookmap/booktitle ')]" mode="create_book_title">
    <text:p text:style-name="Title">
      <xsl:apply-templates select="*[contains(@class, ' bookmap/booklibrary ')]" mode="dita-ot:text-only"/>
      <text:line-break/>
      <xsl:apply-templates select="*[contains(@class, ' bookmap/mainbooktitle ')]" mode="dita-ot:text-only"/>
      <text:line-break/>
      <xsl:apply-templates select="*[contains(@class, ' bookmap/booktitlealt ')]" mode="dita-ot:text-only"/>
    </text:p>
    <!-- page break. -->
    <text:p text:style-name="PB"/>
  </xsl:template>
  
  <xsl:template name="create_book_abstract">
    <xsl:apply-templates select="*[contains(@class, ' bookmap/bookmap ')]
      /*[contains(@class, ' topic/topic ')]" mode="create_book_abstract"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="create_book_abstract">
    <xsl:variable name="topicType">
      <xsl:call-template name="determineTopicType"/>
    </xsl:variable>
    <xsl:if test="$topicType = 'topicAbstract'">
      <xsl:apply-templates/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="create_book_notices">
    <xsl:apply-templates select="*[contains(@class, ' bookmap/bookmap ')]
      /*[contains(@class, ' topic/topic ')]" mode="create_book_notices"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="create_book_notices">
    <xsl:variable name="topicType">
      <xsl:call-template name="determineTopicType"/>
    </xsl:variable>
    <xsl:if test="$topicType = 'topicNotices'">
      <xsl:apply-templates/>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
