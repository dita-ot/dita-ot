<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0"
  xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
  xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
  xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
  xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
  xmlns:exsl="http://exslt.org/common" 
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:styleUtils="org.dita.dost.util.StyleUtils"
  xmlns:stringUtils="org.dita.dost.util.StringUtils" 
  xmlns:imgUtils="org.dita.dost.util.ImgUtils"
  exclude-result-prefixes="exsl ditamsg styleUtils imgUtils stringUtils">
  
  <!-- =========== TEMPLATES FOR ODT FLAGGING =========== -->
  <xsl:template name="create_flagging_styles">
    <xsl:apply-templates select="$FILTERDOC/val/prop[@action='flag']" mode="create_flagging_styles"/>
    
    <xsl:apply-templates select="$FILTERDOC/val/revprop[@action='flag']" mode="create_flagging_styles">
      <xsl:with-param name="att" select="'rev'"/>
    </xsl:apply-templates>
    
    <xsl:apply-templates select="$FILTERDOC/val/style-conflict" mode="create_conflict_flagging_styles"/>
  </xsl:template>
  
  <xsl:template match="prop[@action='flag']|revprop[@action='flag']" mode="create_flagging_styles">
    <xsl:param name="att" select="@att"/>
    <xsl:param name="val" select="@val"/>
    
    <xsl:variable name="styleName" select="styleUtils:insertFlagStyleName(concat($att, $val))"/>
    <!-- text/p flagging style -->
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
    <!-- table flagging style -->
    <xsl:element name="style:style">
        <xsl:attribute name="style:name">
          <xsl:value-of select="concat($styleName, '_table_attr')"/>
        </xsl:attribute>
        <xsl:attribute name="style:family">table</xsl:attribute>
         <xsl:element name="style:table-properties">
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
    <!-- for subject schema -->
    <!-- get the location of schemekeydef.xml -->
    <xsl:variable name="KEYDEF-FILE">
      <xsl:value-of select="concat($WORKDIR,$PATH2PROJ,'schemekeydef.xml')"/>
    </xsl:variable>
    <!--keydef.xml contains the val  -->
    <xsl:if test="(document($KEYDEF-FILE, /)//*[@keys=$val])">
      <xsl:apply-templates select="(document($KEYDEF-FILE, /)//*[@keys=$val])" mode="create_subject_scheme_style">
        <xsl:with-param name="att" select="$att"/>
        <xsl:with-param name="val" select="$val"/>
        <xsl:with-param name="prop" select="."/>
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="style-conflict" mode="create_conflict_flagging_styles">
    <!-- common styles -->
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
    <!-- table styles -->
    <xsl:element name="style:style">
      <xsl:attribute name="style:name">
        <xsl:value-of select="'conflict_style_table'"/>
      </xsl:attribute>
      <xsl:attribute name="style:family">table</xsl:attribute>
      <xsl:element name="style:table-properties">
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
  
  <xsl:template match="*" mode="create_subject_scheme_style">
    <xsl:param name="att"/>
    <xsl:param name="val"/>
    <xsl:param name="prop"/>
    <xsl:param name="cvffilename" select="@source"/>
    <!--get the location of subject_scheme.dictionary-->
    <xsl:variable name="INITIAL-PROPERTIES-FILE">
      <xsl:value-of select="translate(concat($WORKDIR , $PATH2PROJ , 'subject_scheme.dictionary'), '\', '/')"/>
    </xsl:variable>
    
    <xsl:variable name="PROPERTIES-FILE">
      <xsl:choose>
        <xsl:when test="starts-with($INITIAL-PROPERTIES-FILE,'/')">
          <xsl:text>file://</xsl:text><xsl:value-of select="$INITIAL-PROPERTIES-FILE"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>file:/</xsl:text><xsl:value-of select="$INITIAL-PROPERTIES-FILE"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- get the scheme list -->
    <!-- check CURRENT File -->
    <xsl:variable name="editedFileName">
      <xsl:call-template name="checkFile">
        <xsl:with-param name="in" select="$CURRENTFILE"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="schemeList">
      <xsl:apply-templates select="document($PROPERTIES-FILE,/)//*[@key=$editedFileName]" mode="check"/>
    </xsl:variable>
    <xsl:if test="contains($schemeList, $cvffilename)">
      <!-- get the path of scheme file -->
      <xsl:variable name="submfile">
        <xsl:value-of select="$cvffilename"/><xsl:text>.subm</xsl:text>
      </xsl:variable>
      <xsl:variable name="cvffilepath">
        <xsl:value-of select="concat($WORKDIR,$PATH2PROJ,$submfile)"/>
      </xsl:variable>
      <xsl:if test="document($cvffilepath,/)//*[@keys=$val]">
        <!-- copy the child node for flag and just copy the first element whose keys=$flag-->
        <!--xsl:for-each select="document($cvffilepath,/)//*[@keys=$value]/*"-->
        <xsl:for-each select="document($cvffilepath,/)//*[@keys=$val]//*">
          <xsl:variable name="styleName" select="styleUtils:insertFlagStyleName(concat($att, @keys))"/>
          <!-- text/p flagging style -->
          <xsl:element name="style:style">
            <xsl:attribute name="style:name">
              <xsl:value-of select="$styleName"/>
            </xsl:attribute>
            <xsl:attribute name="style:family">text</xsl:attribute>
            <xsl:choose>
              <xsl:when test="exsl:node-set($prop)/@style = 'underline'">
                <xsl:attribute name="style:parent-style-name">underline</xsl:attribute>
              </xsl:when>
              <xsl:when test="exsl:node-set($prop)/@style = 'bold'">
                <xsl:attribute name="style:parent-style-name">bold</xsl:attribute>
              </xsl:when>
              <xsl:when test="exsl:node-set($prop)/@style = 'italics'">
                <xsl:attribute name="style:parent-style-name">italic</xsl:attribute>
              </xsl:when>
              <xsl:when test="exsl:node-set($prop)/@style = 'double-underline'">
                <xsl:attribute name="style:parent-style-name">double-underline</xsl:attribute>
              </xsl:when>
              <xsl:when test="exsl:node-set($prop)/@style = 'overline'">
                <xsl:attribute name="style:parent-style-name">overline</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="style:parent-style-name">indent_text_style</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            
            <xsl:element name="style:text-properties">
              <xsl:if test="exsl:node-set($prop)/@backcolor and not(exsl:node-set($prop)/@backcolor = '')">
                <xsl:attribute name="fo:background-color">
                  <xsl:value-of select="styleUtils:getColor(exsl:node-set($prop)/@backcolor)"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="exsl:node-set($prop)/@color and not(exsl:node-set($prop)/@color = '')">
                <xsl:attribute name="fo:color">
                  <xsl:value-of select="styleUtils:getColor(exsl:node-set($prop)/@color)"/>
                </xsl:attribute>
              </xsl:if>
            </xsl:element>
          </xsl:element>
          <!-- table flagging style -->
          <xsl:element name="style:style">
            <xsl:attribute name="style:name">
              <xsl:value-of select="concat($styleName, '_table_attr')"/>
            </xsl:attribute>
            <xsl:attribute name="style:family">table</xsl:attribute>
            <xsl:element name="style:table-properties">
              <xsl:if test="exsl:node-set($prop)/@backcolor and not(exsl:node-set($prop)/@backcolor = '')">
                <xsl:attribute name="fo:background-color">
                  <xsl:value-of select="styleUtils:getColor(exsl:node-set($prop)/@backcolor)"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="exsl:node-set($prop)/@color and not(exsl:node-set($prop)/@color = '')">
                <xsl:attribute name="fo:color">
                  <xsl:value-of select="styleUtils:getColor(exsl:node-set($prop)/@color)"/>
                </xsl:attribute>
              </xsl:if>
            </xsl:element>
          </xsl:element>
        </xsl:for-each>
      </xsl:if>
    </xsl:if>
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
  
  
  <!-- add flagging images, rev images and flagging styles -->
  <xsl:template match="*" mode="start-add-odt-flags">
    <!-- param for table background color -->
    <xsl:param name="family" select="''"/>
    <!-- param for text under list/entry tags -->
    <xsl:param name="type" select="''"/>
    
    
    <!-- get flagging style name. -->
    <xsl:variable name="flagStyleName">
      <xsl:apply-templates select="." mode="getFlaggingStyleName"/>
      <!-- 
      <xsl:call-template name="getFlagStyleName"/>
      -->
    </xsl:variable>
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:apply-templates select="." mode="getFlaggingRules"/>
      <!-- 
      <xsl:call-template name="getrules"/>
      -->
    </xsl:variable>
    <!-- check style conflict -->
    <xsl:variable name="conflictexist">
      <xsl:call-template name="conflict-check">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
    </xsl:variable>
      
    <!-- ordinary tag(not list, or render table background color) -->
    <xsl:if test="$family = '' or $family = '_table_attr'">    
      <!-- add flagging styles -->
      <xsl:choose>
        <!-- no conflict -->
        <xsl:when test="$conflictexist = 'false' and $flagStyleName != ''">
          <xsl:choose>
            <!-- ordinary style -->
            <xsl:when test="$family = ''">
              <xsl:attribute name="text:style-name">
                <xsl:value-of select="concat($flagStyleName, $family)"/>
              </xsl:attribute>
            </xsl:when>
            <!-- table background style -->
            <xsl:otherwise>
              <xsl:attribute name="table:style-name">
                <xsl:value-of select="concat($flagStyleName, $family)"/>
              </xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <!-- there are conflict -->
        <xsl:when test="$conflictexist = 'true'">
          <xsl:apply-templates select="." mode="ditamsg:conflict-text-style-applied"/>
          <xsl:choose>
            <!-- ordinary conflict style -->
            <xsl:when test="$family = ''">
              <xsl:attribute name="text:style-name">
                <xsl:value-of select="'conflict_style'"/>
              </xsl:attribute>
            </xsl:when>
            <!-- table conflict style -->
            <xsl:otherwise>
              <xsl:attribute name="text:style-name">
                <xsl:value-of select="'conflict_style_table'"/>
              </xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
    
    <xsl:variable name="revtest">
      <xsl:call-template name="find-active-rev-flag">
        <xsl:with-param name="allrevs" select="@rev"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:variable name="flagRulesOfCurrentNode">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- current node has flagging attribute add images-->
    <xsl:if test="exsl:node-set($flagRulesOfCurrentNode)">
      <!-- for table/list flagging styles images should be rendered in p tag-->
      <xsl:if test="$family != '' and $family != '_table_attr' and 
        (exsl:node-set($flagrules)/prop[1]/startflag/@imageref or $revtest = 1)">
        <!-- render p and span tag -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
      </xsl:if>
      
      <!-- avoid text under li/entry and table attr styles-->
      <xsl:if test="($type = '' or $type = 'note' or $type = 'keyword') and $family != '_table_attr'">
        <!-- add images -->
        <!-- keyword do not have flagging images -->
        <xsl:if test="$type != 'keyword'">
          <xsl:call-template name="start-flagit">
            <xsl:with-param name="flagrules" select="$flagrules"/>     
          </xsl:call-template>
        </xsl:if>
        <!-- add rev style -->
        <!-- note tag is specail -->
        <xsl:if test="$type != 'note'">
          <xsl:call-template name="start-add-odt-revflags">
            <xsl:with-param name="flagrules" select="$flagrules"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:if>
      
      <!-- for table/list flagging styles images should be rendered in p tag-->
      <xsl:if test="$family != '' and $family != '_table_attr' and 
        (exsl:node-set($flagrules)/prop[1]/startflag/@imageref or $revtest = 1)">
        <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
      </xsl:if>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="*" mode="end-add-odt-flags">
    <xsl:param name="family" select="''"/>
    <xsl:param name="type" select="''"/>
    
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    
    <xsl:variable name="revtest">
      <xsl:call-template name="find-active-rev-flag">
        <xsl:with-param name="allrevs" select="@rev"/>
      </xsl:call-template>
    </xsl:variable>
    
    <!-- for table/list flagging styles -->
    <xsl:if test="$family != '' and (exsl:node-set($flagrules)/prop[last()]/endflag/@imageref or $revtest = 1)">
      <!-- render p and span tag -->
      <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
    </xsl:if>
    
    <!-- avoid text under li/entry-->
    <xsl:if test="$type = '' or $type = 'note' or $type = 'keyword'">
      <!-- add rev style -->
      <!-- note tag is special -->
      <xsl:if test="$type != 'note'">
        <xsl:call-template name="end-add-odt-revflags">
          <xsl:with-param name="flagrules" select="$flagrules"/>
        </xsl:call-template>
      </xsl:if>
      <!-- add images -->
      <!-- keyword do not have flagging images -->
      <xsl:if test="$type != 'keyword'">
        <xsl:call-template name="end-flagit">
          <xsl:with-param name="flagrules" select="$flagrules"/> 
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
    
    <!-- for table/list flagging styles -->
    <xsl:if test="$family != '' and (exsl:node-set($flagrules)/prop[last()]/endflag/@imageref or $revtest = 1)">
      <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
    </xsl:if>
    
  </xsl:template>
  
  
  <!-- add flagging images and rev images in a seperate paragraph -->
  <xsl:template match="*" mode="start-add-odt-imgrevflags">
    
      <!-- get style rules -->
      <xsl:variable name="flagrules">
        <xsl:call-template name="getrules"/>
      </xsl:variable>
      
      <xsl:variable name="revtest">
        <xsl:call-template name="find-active-rev-flag">
          <xsl:with-param name="allrevs" select="@rev"/>
        </xsl:call-template>
      </xsl:variable>
      
      <!-- for table/list flagging styles -->
      <xsl:if test="(exsl:node-set($flagrules)/prop[1]/startflag/@imageref or $revtest = 1)">
        <!-- render p and span tag -->
        <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
      </xsl:if>
      
      <!-- add images -->
      <xsl:call-template name="start-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"/>     
      </xsl:call-template>
      <!-- add rev style -->
      <xsl:call-template name="start-add-odt-revflags">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
    
    <!-- for table/list flagging styles -->
    <xsl:if test="(exsl:node-set($flagrules)/prop[1]/startflag/@imageref or $revtest = 1)">
      <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template match="*" mode="end-add-odt-imgrevflags">
    
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    
    <xsl:variable name="revtest">
      <xsl:call-template name="find-active-rev-flag">
        <xsl:with-param name="allrevs" select="@rev"/>
      </xsl:call-template>
    </xsl:variable>
    
    <!-- for table/list flagging styles -->
    <xsl:if test="(exsl:node-set($flagrules)/prop[last()]/endflag/@imageref or $revtest = 1)">
      <!-- render p and span tag -->
      <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
    </xsl:if>
    
      <!-- add rev style -->
      <xsl:call-template name="end-add-odt-revflags">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
      <!-- add images -->
      <xsl:call-template name="end-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
    
    <!-- for table/list flagging styles -->
    <xsl:if test="(exsl:node-set($flagrules)/prop[last()]/endflag/@imageref or $revtest = 1)">
      <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
    </xsl:if>
    
  </xsl:template>
  
  <!-- only add flagging styles -->
  <xsl:template match="*" mode="add-odt-flagging">
    
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
            <!-- ordinary style -->
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
    
  </xsl:template>
  
  <!-- only add rev images -->
  <xsl:template match="*" mode="start-add-odt-revflags" name="start-add-odt-revflags">
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <!-- add rev style -->
    <xsl:choose>
      <!-- draft rev mode, add div w/ rev attr value -->
      <xsl:when test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> 
        <xsl:variable name="revtest"> 
          <xsl:call-template name="find-active-rev-flag">
            <xsl:with-param name="allrevs" select="@rev"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$revtest=1">
            <xsl:call-template name="start-mark-rev">
              <xsl:with-param name="revvalue" select="@rev"/>
              <xsl:with-param name="flagrules" select="$flagrules"/> 
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="@rev and not($FILTERFILE='')">    <!-- normal rev mode -->
        <xsl:call-template name="start-mark-rev">
          <xsl:with-param name="revvalue" select="@rev"/>
          <xsl:with-param name="flagrules" select="$flagrules"/> 
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>

  </xsl:template>
  
  <xsl:template match="*" mode="end-add-odt-revflags" name="end-add-odt-revflags">
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <!-- add rev style -->
    <xsl:choose>
      <!-- draft rev mode, add div w/ rev attr value -->
      <xsl:when test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> 
        <xsl:variable name="revtest"> 
          <xsl:call-template name="find-active-rev-flag">
            <xsl:with-param name="allrevs" select="@rev"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$revtest=1">
            <xsl:call-template name="end-mark-rev">
              <xsl:with-param name="revvalue" select="@rev"/>
              <xsl:with-param name="flagrules" select="$flagrules"/> 
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="@rev and not($FILTERFILE='')">    <!-- normal rev mode -->
        <xsl:call-template name="end-mark-rev">
          <xsl:with-param name="revvalue" select="@rev"/>
          <xsl:with-param name="flagrules" select="$flagrules"/> 
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>
  
  
  
  
  <xsl:template match="prop" mode="start-flagit">  
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="startflag/@imageref">
        <xsl:variable name="imgsrc" select="startflag/@imageref"/>
        
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="imgUtils:getType(string($imgsrc))"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeightODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidthODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
          <xsl:with-param name="imgsrc" select="$imgsrc"/>
          <xsl:with-param name="alttext" select="startflag/alt-text"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:when test="startflag/alt-text">
        <xsl:value-of select="startflag/alt-text"/>
      </xsl:when>
      
      <xsl:otherwise/> <!-- that flag not active -->
    </xsl:choose>
    <xsl:apply-templates select="following-sibling::prop[1]" mode="start-flagit"/>
  </xsl:template>
  
  <xsl:template match="prop" mode="end-flagit">  
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="endflag/@imageref">
        <xsl:variable name="imgsrc" select="endflag/@imageref"/>
        
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="imgUtils:getType(string($imgsrc))"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeightODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidthODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
          <xsl:with-param name="imgsrc" select="$imgsrc"/>
          <xsl:with-param name="alttext" select="endflag/alt-text"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="endflag/alt-text">
        <xsl:value-of select="endflag/alt-text"/>
      </xsl:when>
      <!-- not necessary to add logic for @img. original ditaval does not support end flag. -->
      <xsl:otherwise/> <!-- that flag not active -->
    </xsl:choose>
    <xsl:apply-templates select="preceding-sibling::prop[1]" mode="end-flagit"/>
  </xsl:template>
  
  <xsl:template name="start-revflagit">
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:apply-templates select="exsl:node-set($flagrules)/revprop[1]" mode="start-revflagit"/>
  </xsl:template>
  
  <xsl:template match="revprop" mode="start-revflagit">
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="startflag/@imageref">
        <xsl:variable name="imgsrc" select="startflag/@imageref"/>
        
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="imgUtils:getType(string($imgsrc))"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeightODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidthODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
          <xsl:with-param name="imgsrc" select="$imgsrc"/>
          <xsl:with-param name="alttext" select="startflag/alt-text"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="startflag/alt-text">
        <xsl:value-of select="startflag/alt-text"/>
      </xsl:when>
      <xsl:otherwise/> <!-- that flag not active -->
    </xsl:choose>
    <xsl:apply-templates select="following-sibling::revprop[1]" mode="start-revflagit"/>
  </xsl:template>
  
  <!-- output the beginning revision graphic & ALT text -->
  <xsl:template name="start-rev-art">
    <xsl:param name="deltaname"/>
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="$deltaname">
        <xsl:variable name="imgsrc" select="$deltaname"/>
        
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="imgUtils:getType(string($imgsrc))"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeightODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidthODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
          <xsl:with-param name="imgsrc" select="$imgsrc"/>
          <xsl:with-param name="alttext" select="'Start of change'"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:otherwise>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Start of change'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="revprop" mode="end-revflagit">
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="endflag/@imageref">
        <xsl:variable name="imgsrc" select="endflag/@imageref"/>
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="imgUtils:getType(string($imgsrc))"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeightODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidthODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
          <xsl:with-param name="imgsrc" select="$imgsrc"/>
          <xsl:with-param name="alttext" select="endflag/alt-text"/>
        </xsl:call-template>
        
      </xsl:when>
      <xsl:when test="endflag/alt-text">
        <xsl:value-of select="endflag/alt-text"/>
      </xsl:when>
      <xsl:otherwise/> <!-- that flag not active -->
    </xsl:choose>
    <xsl:apply-templates select="preceding-sibling::revprop[1]" mode="end-revflagit"/>
  </xsl:template>
  
  <!-- output the ending revision graphic & ALT text -->
  <xsl:template name="end-rev-art">
    <xsl:param name="deltaname"/>
    
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="$deltaname">
        <xsl:variable name="imgsrc" select="$deltaname"/>
        
        <xsl:variable name="type">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="imgUtils:getType(string($imgsrc))"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeightODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidthODT($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
          <xsl:with-param name="imgsrc" select="$imgsrc"/>
          <xsl:with-param name="alttext" select="'End of change'"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:otherwise>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'End of change'"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="start_flagging_text_of_table_or_list">
    
    <xsl:variable name="ul_depth" select="count(ancestor::*[contains(@class, ' topic/ul ')][1]/ancestor::*)"/>
    <xsl:variable name="ol_depth" select="count(ancestor::*[contains(@class, ' topic/ol ')][1]/ancestor::*)"/>
    <xsl:variable name="sl_depth" select="count(ancestor::*[contains(@class, ' topic/sl ')][1]/ancestor::*)"/>
    <xsl:variable name="dl_depth" select="count(ancestor::*[contains(@class, ' topic/dl ')][1]/ancestor::*)"/>
    <xsl:variable name="table_depth" select="count(ancestor::*[contains(@class, ' topic/table ')][1]/ancestor::*)"/>
    <xsl:variable name="stable_depth" select="count(ancestor::*[contains(@class, ' topic/simpletable ')][1]/ancestor::*)"/>
    <!-- get closest tag -->
    <xsl:variable name="max_depth">
      <xsl:value-of select="stringUtils:getMax(string($ul_depth), string($ol_depth), string($sl_depth), 
        string($dl_depth), string($table_depth), string($stable_depth))"/>
    </xsl:variable>
    <xsl:if test="$max_depth != 0 ">
      <xsl:choose>
        <!-- closest tag is ul -->
        <xsl:when test="$max_depth = $ul_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/ul ')][1]" 
            mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is ol -->
        <xsl:when test="$max_depth = $ol_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/ol ')][1]" 
            mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is sl -->
        <xsl:when test="$max_depth = $sl_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/sl ')][1]" 
            mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is dl -->
        <xsl:when test="$max_depth = $dl_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/dl ')][1]" 
            mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is table -->
        <xsl:when test="$max_depth = $table_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/table ')][1]" 
            mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'table'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is stable -->
        <xsl:when test="$max_depth = $stable_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/simpletable ')][1]" 
            mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'stable'"/>
          </xsl:apply-templates>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <!-- flagging text in the table cell or list item 
  e.g
  <ul>
    <li>text</li>
  </ul>
  -->
  <xsl:template name="end_flagging_text_of_table_or_list">
    
    <xsl:variable name="ul_depth" select="count(ancestor::*[contains(@class, ' topic/ul ')][1]/ancestor::*)"/>
    <xsl:variable name="ol_depth" select="count(ancestor::*[contains(@class, ' topic/ol ')][1]/ancestor::*)"/>
    <xsl:variable name="sl_depth" select="count(ancestor::*[contains(@class, ' topic/sl ')][1]/ancestor::*)"/>
    <xsl:variable name="dl_depth" select="count(ancestor::*[contains(@class, ' topic/dl ')][1]/ancestor::*)"/>
    <xsl:variable name="table_depth" select="count(ancestor::*[contains(@class, ' topic/table ')][1]/ancestor::*)"/>
    <xsl:variable name="stable_depth" select="count(ancestor::*[contains(@class, ' topic/simpletable ')][1]/ancestor::*)"/>
    <!-- get closest tag -->
    <xsl:variable name="max_depth">
      <xsl:value-of select="stringUtils:getMax(string($ul_depth), string($ol_depth), string($sl_depth), 
        string($dl_depth), string($table_depth), string($stable_depth))"/>
    </xsl:variable>
    <xsl:if test="$max_depth != 0 ">
      <xsl:choose>
        <!-- closest tag is ul -->
        <xsl:when test="$max_depth = $ul_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/ul ')][1]" 
            mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is ol -->
        <xsl:when test="$max_depth = $ol_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/ol ')][1]" 
            mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is sl -->
        <xsl:when test="$max_depth = $sl_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/sl ')][1]" 
            mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is dl -->
        <xsl:when test="$max_depth = $dl_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/dl ')][1]" 
            mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'list'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is table -->
        <xsl:when test="$max_depth = $table_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/table ')][1]" 
            mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'table'"/>
          </xsl:apply-templates>
        </xsl:when>
        <!-- closest tag is stable -->
        <xsl:when test="$max_depth = $stable_depth">
          <xsl:apply-templates select="ancestor::*[contains(@class, ' topic/simpletable ')][1]" 
            mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'stable'"/>
          </xsl:apply-templates>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  
  <!-- recusive template to get flagging rules -->
  <xsl:template  match="*" mode="getFlaggingRules" name="getFlaggingRules">
    
    <xsl:variable name="flaggingRules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="exsl:node-set($flaggingRules) or not(parent::*)">
        <xsl:copy-of select="exsl:node-set($flaggingRules)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="parent::*" mode="getFlaggingRules"/>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <!-- recusive template to get flagging name -->
  <xsl:template match="*" name="getFlaggingStyleName" mode="getFlaggingStyleName">
    <xsl:variable name="flaggingStyleName">
      <xsl:call-template name="getFlagStyleName"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$flaggingStyleName != '' or not(parent::*)">
        <xsl:value-of select="$flaggingStyleName"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="parent::*" mode="getFlaggingStyleName"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
</xsl:stylesheet>