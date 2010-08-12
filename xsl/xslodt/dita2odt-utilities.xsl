<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
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
  version="1.0"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:stringUtils="org.dita.dost.util.StringUtils" 
  xmlns:styleUtils="org.dita.dost.util.StyleUtils" 
  exclude-result-prefixes="stringUtils styleUtils ditamsg">
    <xsl:template name="get-ascii">
      <xsl:param name="txt"></xsl:param>
      <xsl:variable name="ancestorlang">
        <xsl:call-template name="getLowerCaseLang"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'zh-cn')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'zh')) )">
          <xsl:value-of select="stringUtils:getAscii(string($txt))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$txt"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
  <xsl:template name="getStringODT">
    <xsl:param name="stringName"></xsl:param>
    <xsl:variable name="translatedStr">
      <xsl:call-template name="getString">
        <xsl:with-param name="stringName" select="$stringName"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="get-ascii">
      <xsl:with-param name="txt" select="$translatedStr"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template name="calculate_list_depth">
    <xsl:param name="list_class" select="' topic/li '"/>
    
    <xsl:value-of select="count(ancestor::*[contains(@class, $list_class)])"/>
    
  </xsl:template>
  
  <xsl:template name="calculate_span_depth_for_table">
    <xsl:param name="isDlist" select="0"/>
    <!-- entry's children that can be parent of a list-->
    <xsl:variable name="fig_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/lq ')]) - 
        count(ancestor::*[contains(@class, ' topic/entry ')][1]
        /ancestor::*[contains(@class, ' topic/lq ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/note ')]) - 
        count(ancestor::*[contains(@class, ' topic/entry ')][1]
        /ancestor::*[contains(@class, ' topic/note ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/p ')]) - 
        count(ancestor::*[contains(@class, ' topic/entry ')][1]
        /ancestor::*[contains(@class, ' topic/p ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="required-cleanup_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/required-cleanup ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/required-cleanup ')])) * 2"/>
    </xsl:variable>
    
    <!-- thead count important! -->
    <xsl:variable name="thead_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/thead ')]) - 
        count(ancestor::*[contains(@class, ' topic/tentry ')][1]
        /ancestor::*[contains(@class, ' topic/thead ')])"/>
    </xsl:variable>
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
      $note_count + $p_count + $draft-comment_count + $required-cleanup_count + $thead_count"/>
    
    <xsl:choose>
      <!-- not dlist -->
      <xsl:when test="$isDlist = 0">
        <!-- remove the immediate child after tentry since it is rendered by text:p -->
        <xsl:value-of select="$total_count - 1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$total_count"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="calculate_span_depth_for_simpletable">
    <xsl:param name="isDlist" select="0"/>
    <!-- take care of flagging span -->
    <!-- stentry's children that can be parent of a list-->
    <xsl:variable name="fig_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/lq ')]) - 
        count(ancestor::*[contains(@class, ' topic/stentry ')][1]
        /ancestor::*[contains(@class, ' topic/lq ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/note ')]) - 
        count(ancestor::*[contains(@class, ' topic/stentry ')][1]
        /ancestor::*[contains(@class, ' topic/note ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/p ')]) - 
        count(ancestor::*[contains(@class, ' topic/stentry ')][1]
        /ancestor::*[contains(@class, ' topic/p ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="required-cleanup_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/required-cleanup ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/required-cleanup ')])) * 2"/>
    </xsl:variable>
    
    <!-- sthead count important! -->
    <xsl:variable name="sthead_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/sthead ')]) - 
        count(ancestor::*[contains(@class, ' topic/stentry ')][1]
        /ancestor::*[contains(@class, ' topic/sthead ')])"/>
    </xsl:variable>
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
      $note_count + $p_count + $draft-comment_count + $required-cleanup_count + $sthead_count"/>
    
    <xsl:choose>
      <!-- not dlist -->
      <xsl:when test="$isDlist = 0">
        <!-- remove the immediate child after tentry since it is rendered by text:p -->
        <xsl:value-of select="$total_count - 1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$total_count"/>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template name="calculate_span_depth_for_list">
    <xsl:param name="list_class" select="' topic/li '"/>
    <xsl:variable name="fig_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/lq ')]) - 
        count(ancestor::*[contains(@class, $list_class)][1]
        /ancestor::*[contains(@class, ' topic/lq ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/note ')]) - 
        count(ancestor::*[contains(@class, $list_class)][1]
        /ancestor::*[contains(@class, ' topic/note ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="itemgroup_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/itemgroup ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/itemgroup ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/p ')]) - 
        count(ancestor::*[contains(@class, $list_class)][1]
        /ancestor::*[contains(@class, ' topic/p ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="required-cleanup_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/required-cleanup ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/required-cleanup ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="dd_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/dd ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/dd ')])) * 2"/>
    </xsl:variable>
    
    
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
      $note_count + $itemgroup_count + $p_count + $draft-comment_count + $required-cleanup_count + $dd_count"/>
    
    
    <xsl:choose>
      <!-- remove the first one rendered by text:p-->
      <xsl:when test="$list_class = ' topic/dlentry '">
        <xsl:value-of select="$total_count - 1"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- remove the first one rendered by text:p -->
        <xsl:value-of select="$total_count - 1"/>
      </xsl:otherwise>
    </xsl:choose>
    
    
  </xsl:template>
  
  <xsl:template name="calculate_span_depth">
    <xsl:variable name="desc_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/desc ')])"/>
    </xsl:variable>
    
    <xsl:variable name="fig_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fig ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="figgroup_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/figgroup ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/lq ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <!-- Add 1 for flagging sytles -->
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/note ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/p ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/draft-comment ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="required-cleanup_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/required-cleanup ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="itemgroup_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/itemgroup ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="dd_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/dd ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="fn_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fn ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="abstract_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/abstract ')])"/>
    </xsl:variable>
    
    <xsl:variable name="bodydiv_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/bodydiv ')])"/>
    </xsl:variable>
    
    <xsl:variable name="section_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/section ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="sectiondiv_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/sectiondiv ')])"/>
    </xsl:variable>
    
    <xsl:variable name="example_count">
      <xsl:choose>
        <xsl:when test="ancestor::*[contains(@class, ' topic/example ')]">
          <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/example ')]) * 2"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <xsl:variable name="linkinfo_count">
      <!-- 
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/linkinfo ')])"/>
      -->
      <xsl:value-of select="0"/>
    </xsl:variable>
    
    
    <xsl:variable name="related-links_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/related-links ')])"/>
    </xsl:variable>
    
    <xsl:variable name="pd_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/pd ')])"/>
    </xsl:variable>
    
    <xsl:variable name="total_count" select="$desc_count + $fig_count + $figgroup_count 
      + $lq_count + $note_count + $p_count + $draft-comment_count + $required-cleanup_count + $itemgroup_count 
      + $dd_count + $fn_count + $abstract_count + $bodydiv_count + $section_count + $sectiondiv_count 
      + $example_count + $linkinfo_count + $related-links_count + $pd_count"/>
    
    <!-- remove the first one rendered by text:p -->
    <xsl:value-of select="$total_count - 1"/>
    
  </xsl:template>
  
  <xsl:template name="break_span_tags">
    <xsl:param name="depth" select="0"/>
    <xsl:param name="order" select="0"/>
    <!--1st: move out -->
    <xsl:if test="$depth &gt; 0 and $order = 0">
      <xsl:text disable-output-escaping="yes">&lt;/text:span&gt;</xsl:text>
      <xsl:call-template name="break_span_tags">
        <xsl:with-param name="depth" select="$depth - 1"/>
        <xsl:with-param name="order" select="$order"/>
      </xsl:call-template>
    </xsl:if>
    <!--2nd: move in -->
    <xsl:if test="$depth &gt; 0 and $order &gt; 0">
      <xsl:text disable-output-escaping="yes">&lt;text:span&gt;</xsl:text>
      <xsl:call-template name="break_span_tags">
        <xsl:with-param name="depth" select="$depth - 1"/>
        <xsl:with-param name="order" select="$order"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="create_items_for_list">
    <xsl:param name="depth" select="0"/>
    <xsl:param name="order" select="0"/>
    <!--1st: move out -->
    <xsl:if test="$depth &gt; 0 and $order = 0">
      <xsl:text disable-output-escaping="yes">&lt;/text:list-item&gt;</xsl:text>
      <xsl:text disable-output-escaping="yes">&lt;/text:list&gt;</xsl:text>
      <xsl:call-template name="create_items_for_list">
        <xsl:with-param name="depth" select="$depth - 1"/>
        <xsl:with-param name="order" select="$order"/>
      </xsl:call-template>
    </xsl:if>
    <!--2nd: move in -->
    <xsl:if test="$depth &gt; 0 and $order &gt; 0">
      <!-- for ol, we should keep the item number correctly. -->
      <xsl:choose>
        <xsl:when test="ancestor::*[contains(@class, ' topic/ol ')]">
          <xsl:text disable-output-escaping="yes">&lt;text:list text:continue-numbering="true" text:style-name="ordered_list_style"&gt;</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text disable-output-escaping="yes">&lt;text:list text:continue-numbering="true" text:style-name="list_style"&gt;</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
      <!-- render list item -->
      <xsl:text disable-output-escaping="yes">&lt;text:list-item&gt;</xsl:text>
      
      <xsl:call-template name="create_items_for_list">
        <xsl:with-param name="depth" select="$depth - 1"/>
        <xsl:with-param name="order" select="$order"/>
      </xsl:call-template>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template name="set_align_value">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/@align = 'left'">
        <xsl:attribute name="text:style-name">left</xsl:attribute>
      </xsl:when>
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/@align = 'right'">
        <xsl:attribute name="text:style-name">right</xsl:attribute>
      </xsl:when>
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/@align = 'center'">
        <xsl:attribute name="text:style-name">center</xsl:attribute>
      </xsl:when>
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/@align = 'justify'">
        <xsl:attribute name="text:style-name">justify</xsl:attribute>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="text:style-name">left</xsl:attribute>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="render_simpletable">
    <xsl:variable name="dlentry_count_for_list" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/li ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    <xsl:variable name="fn_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fn ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="list_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/li ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="dlist_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/dlentry ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="table_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/entry ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="stable_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/stentry ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="max_depth">
      <xsl:value-of select="stringUtils:getMax($fn_depth, $list_depth, $dlist_depth, $table_depth, $stable_depth)"/>
    </xsl:variable>
    
    <!-- if the table is under p(direct child) -->
    <xsl:choose>
      <!-- parent tag is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <xsl:call-template name="create_simpletable"/>
      </xsl:when>
      <!-- nested by list -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/li ')] and $dlentry_count_for_list = 0">
        
        <xsl:choose>
          <!-- nearest tag is list -->
          <xsl:when test="$max_depth = $list_depth">
            <!-- caculate list depth -->
            <xsl:variable name="depth">
              <xsl:call-template name="calculate_list_depth"/>
            </xsl:variable>
            <!-- caculate span tag depth -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_list"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is fn -->
          <xsl:when test="$max_depth = $fn_depth">
            <!-- normal process -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_table"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_simpletable"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        
      </xsl:when>
      
      <!-- nested by dlist -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/dlentry ')]">
        
        <xsl:choose>
          <!-- nearest tag is dlist -->
          <xsl:when test="$max_depth = $dlist_depth">
            <!-- caculate list depth -->
            <xsl:variable name="depth">
              <xsl:call-template name="calculate_list_depth">
                <xsl:with-param name="list_class" select="' topic/dlentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- caculate span tag depth -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_list">
                <xsl:with-param name="list_class" select="' topic/dlentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is fn -->
          <xsl:when test="$max_depth = $fn_depth">
            <!-- normal process -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_simpletable"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_table"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <!-- nested by simpletable -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/simpletable ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_simpletable"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag if there are span tags -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render table -->
        <xsl:call-template name="create_simpletable"/>
        <!-- start p tag again if there are span tags-->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      <!-- nested by table -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/table ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_table"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag if there are span tags -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render table -->
        <xsl:call-template name="create_simpletable"/>
        <!-- start p tag again if there are span tags-->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:otherwise>
        <!-- normal process -->
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag if there are span tags -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render table -->
        <xsl:call-template name="create_simpletable"/>
        <!-- start p tag again if there are span tags-->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  
  <xsl:template name="render_table">
    <xsl:variable name="dlentry_count_for_list" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/li ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    <xsl:variable name="fn_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fn ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="list_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/li ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="dlist_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/dlentry ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="table_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/entry ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="stable_depth">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/stentry ')][1]/ancestor::*)"/>
    </xsl:variable>
    
    <xsl:variable name="max_depth">
      <xsl:value-of select="stringUtils:getMax($fn_depth, $list_depth, $dlist_depth, $table_depth, $stable_depth)"/>
    </xsl:variable>
    
    
    <!-- if the table is under p(direct child) -->
    <xsl:choose>
      <!-- parent tag is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <xsl:apply-templates/>
      </xsl:when>
      <!-- nested by list -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/li ')] and $dlentry_count_for_list = 0">
        <!-- nearest tag is list -->
        <xsl:choose>
          <xsl:when test="$max_depth = $list_depth">
            <!-- caculate list depth -->
            <xsl:variable name="depth">
              <xsl:call-template name="calculate_list_depth"/>
            </xsl:variable>
            <!-- caculate span tag depth -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_list"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is fn -->
          <xsl:when test="$max_depth = $fn_depth">
            <!-- normal process -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start first p tag again -->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_table"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_simpletable"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      
      <!-- nested by dlist -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/dlentry ')]">
        
        <!-- nearest tag is dlist -->
        <xsl:choose>
          <!-- nearest tag is dlist -->
          <xsl:when test="$max_depth &gt; $dlist_depth">
            <!-- caculate list depth -->
            <xsl:variable name="depth">
              <xsl:call-template name="calculate_list_depth">
                <xsl:with-param name="list_class" select="' topic/dlentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- caculate span tag depth -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_list">
                <xsl:with-param name="list_class" select="' topic/dlentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is fn -->
          <xsl:when test="$max_depth = $fn_depth">
            <!-- normal process -->
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start first p tag again -->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is  table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_table"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearst tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_simpletable"/>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'0'"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start p tag again if there are span tags-->
            <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="'1'"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        
      </xsl:when>
      <!-- nested by simpletable -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/simpletable ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_simpletable"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag if there are span tags -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render table -->
        <xsl:apply-templates/>
        <!-- start p tag again if there are span tags-->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      <!-- nested by table -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/table ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_table"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag if there are span tags -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render table -->
        <xsl:apply-templates/>
        <!-- start p tag again if there are span tags-->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:otherwise>
        <!-- normal process -->
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render table -->
        <xsl:apply-templates/>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template name="render_list">
    <xsl:param name="list_style"/>

    <xsl:variable name="li_count_for_table" select="count(ancestor::*[contains(@class, ' topic/li ')]) - 
      count(ancestor::*[contains(@class, ' topic/entry ')][1]
      /ancestor::*[contains(@class, ' topic/li ')])"/>
    
    <xsl:variable name="li_count_for_simpletable" select="count(ancestor::*[contains(@class, ' topic/li ')]) - 
      count(ancestor::*[contains(@class, ' topic/stentry ')][1]
      /ancestor::*[contains(@class, ' topic/li ')])"/>
    
    <xsl:variable name="dlentry_count_for_table" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/entry ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    <xsl:variable name="dlentry_count_for_simpletable" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/stentry ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    <xsl:variable name="dlentry_count_for_list" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/li ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    
    
    <xsl:choose>
      <!-- parent tag is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <!-- parent by list -->
      <xsl:when test="parent::*[contains(@class, ' topic/li ')]">
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <!-- parent by  entry-->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <!-- parent by  stentry-->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <!-- parent tag is fn -->
      <xsl:when test="parent::*[contains(@class, ' topic/fn ')]">
        <!-- break p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- start p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
      </xsl:when>
      
      <!-- nearest ancestor tag is table -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/entry ')] and $li_count_for_table = 0 and $dlentry_count_for_table = 0">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_table"/>
        </xsl:variable>
        
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is simpletable -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/stentry ')] and $li_count_for_simpletable = 0 
                      and $dlentry_count_for_simpletable = 0">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_simpletable"/>
        </xsl:variable>
        
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is li -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/li ')] and $dlentry_count_for_list = 0">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_list"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is dlentry -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/dlentry ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_list">
            <xsl:with-param name="list_class" select="' topic/dlentry '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:otherwise>
        
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'0'"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- functions for related-links. -->
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
  
  <xsl:template name="create_flagging_styles">
    <xsl:apply-templates select="$FILTERDOC/val/prop[@action='flag']" mode="create_flagging_styles"/>
    
    <xsl:apply-templates select="$FILTERDOC/val/style-conflict" mode="create_conflict_flagging_styles"/>
  </xsl:template>
  
  <xsl:template match="style-conflict" mode="create_conflict_flagging_styles">
    
    <xsl:element name="style:style">
      <xsl:attribute name="style:name">
        <xsl:value-of select="'conflict_style'"/>
      </xsl:attribute>
      <xsl:attribute name="style:family">text</xsl:attribute>
      <xsl:attribute name="style:parent-style-name">indent_text_style</xsl:attribute>
      <xsl:element name="style:text-properties">
        <xsl:if test="@background-conflict-color and not(@background-conflict-color = '')">
          <xsl:attribute name="fo:background-color">
            <xsl:value-of select="styleUtils:getColor(@background-conflict-color)"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@foreground-conflict-color and not(@foreground-conflict-color = '')">
          <xsl:attribute name="fo:color">
            <xsl:value-of select="styleUtils:getColor(@foreground-conflict-color)"/>
          </xsl:attribute>
        </xsl:if>
      </xsl:element>
    </xsl:element>
    
  </xsl:template>
  
  
  <xsl:template  match="prop[@action='flag']" mode="create_flagging_styles">
    
    <xsl:variable name="styleName" select="styleUtils:insertFlagStyleName(concat(@att, @val))"/>
    
    <xsl:element name="style:style">
        <xsl:attribute name="style:name">
          <xsl:value-of select="$styleName"/>
        </xsl:attribute>
        <xsl:attribute name="style:family">text</xsl:attribute>
        <xsl:choose>
          <xsl:when test="@style = 'underline'">
            <xsl:attribute name="style:parent-style-name">underline</xsl:attribute>
          </xsl:when>
          <xsl:when test="@style = 'bold'">
            <xsl:attribute name="style:parent-style-name">bold</xsl:attribute>
          </xsl:when>
          <xsl:when test="@style = 'italics'">
            <xsl:attribute name="style:parent-style-name">italic</xsl:attribute>
          </xsl:when>
          <xsl:when test="@style = 'double-underline'">
            <xsl:attribute name="style:parent-style-name">double-underline</xsl:attribute>
          </xsl:when>
          <xsl:when test="@style = 'overline'">
            <xsl:attribute name="style:parent-style-name">overline</xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="style:parent-style-name">indent_text_style</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        
        <xsl:element name="style:text-properties">
          <xsl:if test="@backcolor and not(@backcolor = '')">
            <xsl:attribute name="fo:background-color">
              <xsl:value-of select="styleUtils:getColor(@backcolor)"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@color and not(@color = '')">
            <xsl:attribute name="fo:color">
              <xsl:value-of select="styleUtils:getColor(@color)"/>
            </xsl:attribute>
          </xsl:if>
        </xsl:element>
    </xsl:element>
    
  </xsl:template>
  
  <!-- get flagging style name -->
  <xsl:template name="getFlagStyleName">
    
    <xsl:variable name="domains">
      <xsl:value-of select="normalize-space(ancestor-or-self::*[contains(@class,' topic/topic ')][1]/@domains)"/>
    </xsl:variable>
    <xsl:variable name="tmp_props">
      <xsl:call-template name="getExtProps">
        <xsl:with-param name="domains" select="$domains"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="props">
      <xsl:value-of select="substring-after($tmp_props, ',')"/>
    </xsl:variable>
    <!-- Test for the flagging attributes. If found, call 'gen-prop' with the values to use. Otherwise return -->
    <xsl:if test="@audience and not($FILTERFILE='')">
      
      <xsl:value-of select="styleUtils:getFlagStyleName(concat('audience', @audience))"/>
      
    </xsl:if>
    <xsl:if test="@platform and not($FILTERFILE='')">
      
      <xsl:value-of select="styleUtils:getFlagStyleName(concat('platform', @platform))"/>
      
    </xsl:if>
    <xsl:if test="@product and not($FILTERFILE='')">
      
      <xsl:value-of select="styleUtils:getFlagStyleName(concat('product', @product))"/>
      
    </xsl:if>
    <xsl:if test="@otherprops and not($FILTERFILE='')">
      <xsl:value-of select="styleUtils:getFlagStyleName(concat('otherprops', @otherprops))"/>
    </xsl:if>
    
    <xsl:if test="@rev and not($FILTERFILE='')">
      <xsl:value-of select="styleUtils:getFlagStyleName(concat('rev', @rev))"/>
    </xsl:if>
    
    <xsl:if test="not($props='') and not($FILTERFILE='')">
      <xsl:call-template name="ext-getFlagStyleName">
        <xsl:with-param name="props" select="$props"/>
      </xsl:call-template>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template name="ext-getFlagStyleName">
    <xsl:param name="props"/>
    <xsl:choose>
      <xsl:when test="contains($props,',')">
        <xsl:variable name="propsValue">
          <xsl:call-template name="getPropsValue">
            <xsl:with-param name="propsPath" select="substring-before($props,',')"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="propName">
          <xsl:call-template name="getLastPropName">
            <xsl:with-param name="propsPath" select="substring-before($props,',')"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="not($propsValue='')">
          <xsl:value-of select="styleUtils:getFlagStyleName(concat($propName, $propsValue))"/>
        </xsl:if>
        <xsl:call-template name="ext-getFlagStyleName">
          <xsl:with-param name="props" select="substring-after($props,',')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="propsValue">
          <xsl:call-template name="getPropsValue">
            <xsl:with-param name="propsPath" select="$props"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="propName">
          <xsl:call-template name="getLastPropName">
            <xsl:with-param name="propsPath" select="$props"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="not($propsValue='')">
          <xsl:value-of select="styleUtils:getFlagStyleName(concat($propName, $propsValue))"/>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*" mode="start-add-odt-flags">
    <!-- get flagging style name. -->
    <xsl:variable name="flagStyleName">
      <xsl:call-template name="getFlagStyleName"/>
    </xsl:variable>
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- check style conflict -->
    <xsl:variable name="conflictexist">
      <xsl:call-template name="conflict-check">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
    </xsl:variable>
    
    <!-- add flagging styles -->
    <xsl:choose>
      <!-- no conflict -->
      <xsl:when test="$conflictexist = 'false' and $flagStyleName != ''">
        <xsl:attribute name="text:style-name">
          <xsl:value-of select="$flagStyleName"/>
        </xsl:attribute>
      </xsl:when>
      <!-- there are conflict -->
      <xsl:when test="$conflictexist = 'true'">
        <xsl:apply-templates select="." mode="ditamsg:conflict-text-style-applied"/>
        <xsl:attribute name="text:style-name">
          <xsl:value-of select="'conflict_style'"/>
        </xsl:attribute>
      </xsl:when>
    </xsl:choose>
    <!-- add images -->
    <xsl:call-template name="start-flagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>     
    </xsl:call-template>
    <!-- add rev style -->
    <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes') ">
      <xsl:call-template name="start-mark-rev">
        <xsl:with-param name="revvalue" select="@rev"/>
        <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="*" mode="end-add-odt-flags">
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- add rev style -->
    <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
      <xsl:call-template name="end-mark-rev">
        <xsl:with-param name="revvalue" select="@rev"/>
        <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
    </xsl:if>
    <!-- add images -->
    <xsl:call-template name="end-flagit">
      <xsl:with-param name="flagrules" select="$flagrules"/> 
    </xsl:call-template>
    
  </xsl:template>
  
  <xsl:template match="*" mode="start-add-odt-revflags">
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- add rev style -->
    <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes') ">
      <xsl:call-template name="start-mark-rev">
        <xsl:with-param name="revvalue" select="@rev"/>
        <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
    </xsl:if>
    
    
  </xsl:template>
  
  <xsl:template match="*" mode="end-add-odt-revflags">
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- add rev style -->
    <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes') ">
      <xsl:call-template name="end-mark-rev">
        <xsl:with-param name="revvalue" select="@rev"/>
        <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
    </xsl:if>
    
  </xsl:template>
  
</xsl:stylesheet>