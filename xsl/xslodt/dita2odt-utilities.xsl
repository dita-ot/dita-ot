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
  xmlns:stringUtils="org.dita.dost.util.StringUtils" exclude-result-prefixes="stringUtils">
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
    <!-- 
    <xsl:variable name="level_p">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')]) - 
        count(ancestor::*[contains(@class, ' topic/p ')][1]/ancestor::*[contains(@class,' topic/li ')])"/>
    </xsl:variable>
    
    <xsl:variable name="level_note">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')]) - 
        count(ancestor::*[contains(@class, ' topic/note ')][1]/ancestor::*[contains(@class,' topic/li ')])"/>
    </xsl:variable>
    
    <xsl:variable name="level_table">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')]) - 
        count(ancestor::*[contains(@class, ' topic/table ')][1]/ancestor::*[contains(@class,' topic/li ')])"/>
    </xsl:variable>
    
    <xsl:variable name="level_simpletable">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')]) - 
        count(ancestor::*[contains(@class, ' topic/simpletable ')][1]/ancestor::*[contains(@class,' topic/li ')])"/>
    </xsl:variable>
    
    
    <xsl:variable name="tmp1">
      <xsl:choose>
        <xsl:when test="$level_p &lt; $level_note">
          <xsl:value-of select="$level_p"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$level_note"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="tmp2">
      <xsl:choose>
        <xsl:when test="$tmp1 &lt; $level_table">
          <xsl:value-of select="$tmp1"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$level_table"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="tmp3">
      <xsl:choose>
        <xsl:when test="$tmp2 &lt; $level_simpletable">
          <xsl:value-of select="$tmp2"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$level_simpletable"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$tmp3"/>
    -->
    
    <xsl:value-of select="count(ancestor::*[contains(@class, $list_class)])"/>
    
  </xsl:template>
  
  <xsl:template name="calculate_span_depth_for_table">
    <xsl:param name="isDlist" select="0"/>
    <!-- entry's children that can be parent of a list-->
    <xsl:variable name="fig_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/lq ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/lq ')])"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/note ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/note ')])"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/p ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/p ')])"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, ' topic/entry ')][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])"/>
    </xsl:variable>
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
    $note_count + $p_count + $draft-comment_count"/>
    
    <xsl:choose>
      <!-- not dlist -->
      <xsl:when test="$isDlist = 0">
        <!-- remove the first one rendered by text:p -->
        <xsl:value-of select="$total_count - 1"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$total_count"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="calculate_span_depth_for_simpletable">
    <xsl:param name="isDlist" select="0"/>
    
    <!-- stentry's children that can be parent of a list-->
    <xsl:variable name="fig_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/lq ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/lq ')])"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/note ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/note ')])"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/p ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/p ')])"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, ' topic/stentry ')][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])"/>
    </xsl:variable>
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
    $note_count + $p_count + $draft-comment_count"/>
    
    <xsl:choose>
      <!-- not dlist -->
      <xsl:when test="$isDlist = 0">
        <!-- remove the first one rendered by text:p -->
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
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/lq ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/lq ')])"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/note ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/note ')])"/>
    </xsl:variable>
    
    <xsl:variable name="itemgroup_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/itemgroup ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/itemgroup ')])"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/p ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/p ')])"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, $list_class)][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])"/>
    </xsl:variable>
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
    $note_count + $itemgroup_count + $p_count + $draft-comment_count"/>
    
    
    <xsl:choose>
      <!-- dlist has dt & dd-->
      <xsl:when test="$list_class = ' topic/dlentry '">
        <xsl:value-of select="$total_count"/>
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
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fig ')])"/>
    </xsl:variable>
    
    <xsl:variable name="figgroup_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/figgroup ')])"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/lq ')])"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/note ')])"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/p ')])"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/draft-comment ')])"/>
    </xsl:variable>
    
    <xsl:variable name="itemgroup_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/itemgroup ')])"/>
    </xsl:variable>
    
    <xsl:variable name="dd_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/dd ')])"/>
    </xsl:variable>
    
    <xsl:variable name="fn_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/fn ')])"/>
    </xsl:variable>
    
    <xsl:variable name="abstract_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/abstract ')])"/>
    </xsl:variable>
    
    <xsl:variable name="bodydiv_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/bodydiv ')])"/>
    </xsl:variable>
    
    <xsl:variable name="section_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/section ')])"/>
    </xsl:variable>
    
    <xsl:variable name="sectiondiv_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/sectiondiv ')])"/>
    </xsl:variable>
    
    <xsl:variable name="example_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/example ')])"/>
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
      + $lq_count + $note_count + $p_count + $draft-comment_count + $itemgroup_count 
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
  
  <xsl:template name="render_table">
    <xsl:variable name="dlentry_count_for_list" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/li ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    <!-- if the table is under p(direct child) -->
    <xsl:choose>
      <!-- parent tag is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <xsl:apply-templates/>
      </xsl:when>
      <!-- nested by list -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/li ')] and $dlentry_count_for_list = 0">
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
        <!--  span tags span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nested by dlist -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/dlentry ')]">
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
        <!--  span tags span tags again-->
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
        <!--  span tags span tags again-->
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
        <!--  span tags span tags again-->
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
        <!--  span tags span tags again-->
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
        <!--  span tags span tags again-->
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
        <!--  span tags span tags again-->
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
        <!--  span tags span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="'1'"/>
        </xsl:call-template>
        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
</xsl:stylesheet>