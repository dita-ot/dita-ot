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
  version="2.0"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg" 
  exclude-result-prefixes="ditamsg">
  
  <!-- =========== I18N RELATED TEMPLATES, ODT REUSES RESOURCE FILES OF XHTML===============-->
    <!-- Deprecated: generate text directly instead -->
    <xsl:template name="get-ascii">
      <xsl:param name="txt"></xsl:param>
      <xsl:value-of select="$txt"/>
    </xsl:template>
  <!-- Deprecated: use getString instead -->
  <xsl:template name="getStringODT">
    <xsl:param name="stringName"/>
    <xsl:call-template name="getString">
      <xsl:with-param name="stringName" select="$stringName"/>
    </xsl:call-template>
  </xsl:template>
  
  
  <!-- =========== TEMPLATES FOR CALCULATING NESTED TAGS 
      NOTE:SOME TAGS' NUMBER ARE MULTPLIED BY A NUMBER FOR FLAGGING STYLES.=========== -->
  <xsl:template name="calculate_list_depth">
    <xsl:param name="list_class" select="' topic/li '"/>
    
    <xsl:value-of select="count(ancestor::*[contains(@class, $list_class)])"/>
    
  </xsl:template>
  
  <xsl:template name="calculate_span_depth_for_tag">
    <xsl:param name="tag_class" select="' topic/fn '"/>
    
    <xsl:variable name="fig_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/fig ')]) - 
                             count(ancestor::*[contains(@class, $tag_class)][1]
                            /ancestor::*[contains(@class, ' topic/fig ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="lq_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/lq ')]) - 
        count(ancestor::*[contains(@class, $tag_class)][1]
        /ancestor::*[contains(@class, ' topic/lq ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/note ')]) - 
        count(ancestor::*[contains(@class, $tag_class)][1]
        /ancestor::*[contains(@class, ' topic/note ')])) * 3"/>
    </xsl:variable>
    
    <xsl:variable name="itemgroup_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/itemgroup ')]) - 
                            count(ancestor::*[contains(@class, $tag_class)][1]
                            /ancestor::*[contains(@class, ' topic/itemgroup ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/p ')]) - 
        count(ancestor::*[contains(@class, $tag_class)][1]
        /ancestor::*[contains(@class, ' topic/p ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="draft-comment_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/draft-comment ')]) - 
                            count(ancestor::*[contains(@class, $tag_class)][1]
                            /ancestor::*[contains(@class, ' topic/draft-comment ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="required-cleanup_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/required-cleanup ')]) - 
                            count(ancestor::*[contains(@class, $tag_class)][1]
                            /ancestor::*[contains(@class, ' topic/required-cleanup ')])) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="dd_count">
      <xsl:value-of select="(count(ancestor::*[contains(@class, ' topic/dd ')]) - 
                            count(ancestor::*[contains(@class, $tag_class)][1]
                            /ancestor::*[contains(@class, ' topic/dd ')])) * 2"/>
    </xsl:variable>
    
    <!-- sthead/thead count important! -->
    <xsl:variable name="thead_count">
      <xsl:choose>
        <xsl:when test="ancestor::*[contains(@class, ' topic/sthead ')]">
          <xsl:value-of select="1"/>
        </xsl:when>
        <xsl:when test="ancestor::*[contains(@class, ' topic/thead ')]">
          <xsl:value-of select="1"/>
        </xsl:when>
        <xsl:otherwise>0</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    
    <!-- thead count important! -->
    <!-- 
    <xsl:variable name="thead_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/thead ')]) - 
        count(ancestor::*[contains(@class, $tag_class)][1]
        /ancestor::*[contains(@class, ' topic/thead ')])"/>
    </xsl:variable>
    -->
    
    <xsl:variable name="total_count" select="$fig_count + $lq_count + 
      $note_count + $itemgroup_count + $p_count + $draft-comment_count + 
      $required-cleanup_count + $dd_count + $thead_count"/>
    
    
    <xsl:choose>
      <!-- fn is rendered as text:p plus flagging styles-->
      <xsl:when test="$tag_class = ' topic/fn '">
        <xsl:value-of select="$total_count + 1"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- 1st one is rendered as p -->
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
    
    <xsl:variable name="lines_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/lines ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="note_count">
      <!-- Add 1 for flagging sytles -->
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/note ')]) * 3"/>
    </xsl:variable>
    
    <xsl:variable name="p_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/p ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="ph_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/ph ')]) * 2"/>
    </xsl:variable>
    
    <xsl:variable name="pre_count">
      <xsl:value-of select="count(ancestor::*[contains(@class, ' topic/pre ')]) * 2"/>
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
      + $lq_count + $lines_count + $note_count + $p_count + $ph_count + $pre_count + $draft-comment_count + $required-cleanup_count 
      + $itemgroup_count + $dd_count + $fn_count + $abstract_count + $bodydiv_count + $section_count + $sectiondiv_count 
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
    
    <xsl:variable name="fn_depth" select="count(ancestor::*[contains(@class, ' topic/fn ')][1]/ancestor::*)"/>
    
    <xsl:variable name="list_depth" select="count(ancestor::*[contains(@class, ' topic/li ')][1]/ancestor::*)"/>
    
    <xsl:variable name="dlist_depth" select="count(ancestor::*[contains(@class, ' topic/dlentry ')][1]/ancestor::*)"/>
    
    <xsl:variable name="table_depth" select="count(ancestor::*[contains(@class, ' topic/entry ')][1]/ancestor::*)"/>
    
    <xsl:variable name="stable_depth" select="count(ancestor::*[contains(@class, ' topic/stentry ')][1]/ancestor::*)"/>
    
    <xsl:variable name="max_depth" select="max(($fn_depth, $list_depth, $dlist_depth, $table_depth, $stable_depth))"/>
    
    <!-- if the table is under p(direct child) -->
    <xsl:choose>
      <!-- parent tag is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <!-- start render table -->
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
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/li '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="1"/>
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
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/entry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/stentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
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
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/dlentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:call-template name="create_simpletable"/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="1"/>
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
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/stentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/entry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <!-- nested by simpletable -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/simpletable ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/stentry '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
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
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      <!-- nested by table -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/table ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/entry '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
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
          <xsl:with-param name="order" select="1"/>
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
          <xsl:with-param name="order" select="0"/>
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
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  
  <xsl:template name="render_table">
    <xsl:variable name="dlentry_count_for_list" select="count(ancestor::*[contains(@class, ' topic/dlentry ')]) - 
      count(ancestor::*[contains(@class, ' topic/li ')][1]
      /ancestor::*[contains(@class, ' topic/dlentry ')])"/>
    
    <xsl:variable name="fn_depth" select="count(ancestor::*[contains(@class, ' topic/fn ')][1]/ancestor::*)"/>
    
    <xsl:variable name="list_depth" select="count(ancestor::*[contains(@class, ' topic/li ')][1]/ancestor::*)"/>
    
    <xsl:variable name="dlist_depth" select="count(ancestor::*[contains(@class, ' topic/dlentry ')][1]/ancestor::*)"/>
    
    <xsl:variable name="table_depth" select="count(ancestor::*[contains(@class, ' topic/entry ')][1]/ancestor::*)"/>
    
    <xsl:variable name="stable_depth" select="count(ancestor::*[contains(@class, ' topic/stentry ')][1]/ancestor::*)"/>
    
    <xsl:variable name="max_depth" select="max(($fn_depth, $list_depth, $dlist_depth, $table_depth, $stable_depth))"/>
    
    
    <!-- if the table is under p(direct child) -->
    <xsl:choose>
      <!-- parent tag is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <!-- start render table -->
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
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/li '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="1"/>
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
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/entry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/stentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
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
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/dlentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- break first p tag if there are span tags -->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
            </xsl:if>
            <!-- break list tag -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="0"/>
            </xsl:call-template>
            <!-- start render table -->
            <xsl:apply-templates/>
            <!-- start list tag again -->
            <xsl:call-template name="create_items_for_list">
              <xsl:with-param name="depth" select="$depth"/>
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
            <!-- start p tag again if there are span tags-->
            <xsl:if test="$span_depth &gt;= 0">
              <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
            </xsl:if>
            <!--  start render span tags again-->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="1"/>
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
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearest tag is  table -->
          <xsl:when test="$max_depth = $table_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/entry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
          <!-- nearst tag is simpletable -->
          <xsl:when test="$max_depth = $stable_depth">
            <xsl:variable name="span_depth">
              <xsl:call-template name="calculate_span_depth_for_tag">
                <xsl:with-param name="tag_class" select="' topic/stentry '"/>
              </xsl:call-template>
            </xsl:variable>
            <!-- break span tags -->
            <xsl:call-template name="break_span_tags">
              <xsl:with-param name="depth" select="$span_depth"/>
              <xsl:with-param name="order" select="0"/>
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
              <xsl:with-param name="order" select="1"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        
      </xsl:when>
      <!-- nested by simpletable -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/simpletable ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/stentry '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
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
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      <!-- nested by table -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/table ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/entry '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
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
          <xsl:with-param name="order" select="1"/>
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
          <xsl:with-param name="order" select="0"/>
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
          <xsl:with-param name="order" select="1"/>
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
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- parent by list -->
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- parent by  entry-->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- parent by  stentry-->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- parent tag is fn -->
      <xsl:when test="parent::*[contains(@class, ' topic/fn ')]">
        <!-- break span tag(for flagging)-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="1"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p text:style-name="footnote"&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="1"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is table -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/entry ')] and $li_count_for_table = 0 and $dlentry_count_for_table = 0">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/entry '"/>
          </xsl:call-template>
        </xsl:variable>
        
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is simpletable -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/stentry ')] and $li_count_for_simpletable = 0 
                      and $dlentry_count_for_simpletable = 0">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/stentry '"/>
          </xsl:call-template>
        </xsl:variable>
        
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is li -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/li ')] and $dlentry_count_for_list = 0">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/li '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      
      <!-- nearest ancestor tag is dlentry -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/dlentry ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag">
            <xsl:with-param name="tag_class" select="' topic/dlentry '"/>
          </xsl:call-template>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      <!-- nearest ancestor tag is fn -->
      <xsl:when test="ancestor::*[contains(@class, ' topic/fn ')]">
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth_for_tag"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p text:style-name="footnote"&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:when>
      <!-- nested by other tags. -->
      <xsl:otherwise>
        <xsl:variable name="span_depth">
          <xsl:call-template name="calculate_span_depth"/>
        </xsl:variable>
        <!-- break span tags -->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="0"/>
        </xsl:call-template>
        <!-- break first p tag -->
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
        <!-- start flagging -->
        <xsl:apply-templates select="." mode="start-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start render list -->
        <xsl:element name="text:list">
          <xsl:attribute name="text:style-name"><xsl:value-of select="$list_style"/></xsl:attribute>
          <xsl:apply-templates/>
        </xsl:element>
        <!-- end flagging -->
        <xsl:apply-templates select="." mode="end-add-odt-flags">
          <xsl:with-param name="family" select="'_list'"/>
        </xsl:apply-templates>
        <!-- start first p tag again -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
        <!--  start render span tags again-->
        <xsl:call-template name="break_span_tags">
          <xsl:with-param name="depth" select="$span_depth"/>
          <xsl:with-param name="order" select="1"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- =========== FUNCTIONS FOR RELATED-LINKS =========== -->
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
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="contains(@class,' topic/link ')">
        <xsl:choose>
          <xsl:when test="*[contains(@class,' topic/linktext ')]">
            <xsl:value-of select="*[contains(@class,' topic/linktext ')]"/>
          </xsl:when>
          <xsl:when test="text()">
            <xsl:value-of select="text()"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*" mode="ditamsg:draft-comment-in-content">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">040</xsl:with-param>
      <xsl:with-param name="msgsev">I</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="*" mode="ditamsg:required-cleanup-in-content">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">039</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
</xsl:stylesheet>