<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0"
  xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
  xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
  xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
  xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0" 
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
  xmlns:styleUtils="org.dita.dost.util.StyleUtils" 
  xmlns:imgUtils="org.dita.dost.util.ImgUtils"
  exclude-result-prefixes="xs dita-ot ditamsg styleUtils imgUtils">
  
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
              <xsl:value-of select="dita-ot:getColor(@backcolor)"/>
            </xsl:attribute>
          </xsl:if>
          <xsl:if test="@color and not(@color = '')">
            <xsl:attribute name="fo:color">
              <xsl:value-of select="dita-ot:getColor(@color)"/>
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
                <xsl:value-of select="dita-ot:getColor(@backcolor)"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:if test="@color and not(@color = '')">
              <xsl:attribute name="fo:color">
                <xsl:value-of select="dita-ot:getColor(@color)"/>
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
            <xsl:value-of select="dita-ot:getColor(@background-conflict-color)"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@foreground-conflict-color and not(@foreground-conflict-color = '')">
          <xsl:attribute name="fo:color">
            <xsl:value-of select="dita-ot:getColor(@foreground-conflict-color)"/>
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
            <xsl:value-of select="dita-ot:getColor(@background-conflict-color)"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="@foreground-conflict-color and not(@foreground-conflict-color = '')">
          <xsl:attribute name="fo:color">
            <xsl:value-of select="dita-ot:getColor(@foreground-conflict-color)"/>
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
              <xsl:when test="$prop/@style = 'underline'">
                <xsl:attribute name="style:parent-style-name">underline</xsl:attribute>
              </xsl:when>
              <xsl:when test="$prop/@style = 'bold'">
                <xsl:attribute name="style:parent-style-name">bold</xsl:attribute>
              </xsl:when>
              <xsl:when test="$prop/@style = 'italics'">
                <xsl:attribute name="style:parent-style-name">italic</xsl:attribute>
              </xsl:when>
              <xsl:when test="$prop/@style = 'double-underline'">
                <xsl:attribute name="style:parent-style-name">double-underline</xsl:attribute>
              </xsl:when>
              <xsl:when test="$prop/@style = 'overline'">
                <xsl:attribute name="style:parent-style-name">overline</xsl:attribute>
              </xsl:when>
              <xsl:otherwise>
                <xsl:attribute name="style:parent-style-name">indent_text_style</xsl:attribute>
              </xsl:otherwise>
            </xsl:choose>
            
            <xsl:element name="style:text-properties">
              <xsl:if test="$prop/@backcolor and not($prop/@backcolor = '')">
                <xsl:attribute name="fo:background-color">
                  <xsl:value-of select="dita-ot:getColor($prop/@backcolor)"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="$prop/@color and not($prop/@color = '')">
                <xsl:attribute name="fo:color">
                  <xsl:value-of select="dita-ot:getColor($prop/@color)"/>
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
              <xsl:if test="$prop/@backcolor and not($prop/@backcolor = '')">
                <xsl:attribute name="fo:background-color">
                  <xsl:value-of select="dita-ot:getColor($prop/@backcolor)"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:if test="$prop/@color and not($prop/@color = '')">
                <xsl:attribute name="fo:color">
                  <xsl:value-of select="dita-ot:getColor($prop/@color)"/>
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
    <xsl:if test="$flagRulesOfCurrentNode">
      <!-- for table/list flagging styles images should be rendered in p tag-->
      <xsl:if test="$family != '' and $family != '_table_attr' and 
        ($flagrules/prop[1]/startflag/@imageref or $revtest = 1)">
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
        ($flagrules/prop[1]/startflag/@imageref or $revtest = 1)">
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
    <xsl:if test="$family != '' and ($flagrules/prop[last()]/endflag/@imageref or $revtest = 1)">
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
    <xsl:if test="$family != '' and ($flagrules/prop[last()]/endflag/@imageref or $revtest = 1)">
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
      <xsl:if test="($flagrules/prop[1]/startflag/@imageref or $revtest = 1)">
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
    <xsl:if test="($flagrules/prop[1]/startflag/@imageref or $revtest = 1)">
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
    <xsl:if test="($flagrules/prop[last()]/endflag/@imageref or $revtest = 1)">
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
    <xsl:if test="($flagrules/prop[last()]/endflag/@imageref or $revtest = 1)">
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
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeight($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidth($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
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
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeight($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidth($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
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
    <xsl:apply-templates select="$flagrules/revprop[1]" mode="start-revflagit"/>
  </xsl:template>
  
  <xsl:template match="revprop" mode="start-revflagit">
    <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
      <xsl:when test="startflag/@imageref">
        <xsl:variable name="imgsrc" select="startflag/@imageref"/>
                
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeight($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidth($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
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
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeight($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidth($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
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
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeight($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidth($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
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
        
        <xsl:variable name="height">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getHeight($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="width">
          <xsl:choose>
            <xsl:when test="not(contains($imgsrc,'://'))">
              <xsl:value-of select="number(imgUtils:getWidth($OUTPUTDIR, string($imgsrc)) div 96)"/>
            </xsl:when>
            <xsl:otherwise/>
          </xsl:choose>
        </xsl:variable>
        
        <xsl:call-template name="draw_image_odt">
          <xsl:with-param name="height" select="$height"/>
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
    <xsl:variable name="max_depth" select="max(($ul_depth, $ol_depth, $sl_depth, $dl_depth, $table_depth, $stable_depth))"/>
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
    <xsl:variable name="max_depth" select="max(($ul_depth, $ol_depth, $sl_depth, $dl_depth, $table_depth, $stable_depth))"/>
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
      <xsl:when test="$flaggingRules or not(parent::*)">
        <xsl:copy-of select="$flaggingRules"/>
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
  
  <xsl:function name="dita-ot:getColor" as="xs:string">
    <xsl:param name="color"/>
    <xsl:choose>
      <xsl:when test="$color = 'aliceblue'">#f0f8ff</xsl:when>
      <xsl:when test="$color = 'antiquewhite'">#faebd7</xsl:when>
      <xsl:when test="$color = 'aqua'">#00ffff</xsl:when>
      <xsl:when test="$color = 'aquamarine'">#7fffd4</xsl:when>
      <xsl:when test="$color = 'azure'">#f0ffff</xsl:when>
      <xsl:when test="$color = 'beige'">#f5f5dc</xsl:when>
      <xsl:when test="$color = 'bisque'">#ffe4c4</xsl:when>
      <xsl:when test="$color = 'black'">#000000</xsl:when>
      <xsl:when test="$color = 'blanchedalmond'">#ffebcd</xsl:when>
      <xsl:when test="$color = 'blue'">#0000ff</xsl:when>
      <xsl:when test="$color = 'blueviolet'">#8a2be2</xsl:when>
      <xsl:when test="$color = 'brown'">#a52a2a</xsl:when>
      <xsl:when test="$color = 'burlywood'">#deb887</xsl:when>
      <xsl:when test="$color = 'cadetblue'">#5f9ea0</xsl:when>
      <xsl:when test="$color = 'chartreuse'">#7fff00</xsl:when>
      <xsl:when test="$color = 'chocolate'">#d2691e</xsl:when>
      <xsl:when test="$color = 'coral'">#ff7f50</xsl:when>
      <xsl:when test="$color = 'cornflowerblue'">#6495ed</xsl:when>
      <xsl:when test="$color = 'cornsilk'">#fff8dc</xsl:when>
      <xsl:when test="$color = 'crimson'">#dc143c</xsl:when>
      <xsl:when test="$color = 'cyan'">#00ffff</xsl:when>
      <xsl:when test="$color = 'darkblue'">#00008b</xsl:when>
      <xsl:when test="$color = 'darkcyan'">#008b8b</xsl:when>
      <xsl:when test="$color = 'darkgoldenrod'">#b8860b</xsl:when>
      <xsl:when test="$color = 'darkgray'">#a9a9a9</xsl:when>
      <xsl:when test="$color = 'darkgreen'">#006400</xsl:when>
      <xsl:when test="$color = 'darkkhaki'">#bdb76b</xsl:when>
      <xsl:when test="$color = 'darkmagenta'">#8b008b</xsl:when>
      <xsl:when test="$color = 'darkolivegreen'">#556b2f</xsl:when>
      <xsl:when test="$color = 'darkorange'">#ff8c00</xsl:when>
      <xsl:when test="$color = 'darkorchid'">#9932cc</xsl:when>
      <xsl:when test="$color = 'darkred'">#8b0000</xsl:when>
      <xsl:when test="$color = 'darksalmon'">#e9967a</xsl:when>
      <xsl:when test="$color = 'darkseagreen'">#8fbc8f</xsl:when>
      <xsl:when test="$color = 'darkslateblue'">#483d8b</xsl:when>
      <xsl:when test="$color = 'darkslategray'">#2f4f4f</xsl:when>
      <xsl:when test="$color = 'darkturquoise'">#00ced1</xsl:when>
      <xsl:when test="$color = 'darkviolet'">#9400d3</xsl:when>
      <xsl:when test="$color = 'deeppink'">#ff1493</xsl:when>
      <xsl:when test="$color = 'deepskyblue'">#00bfff</xsl:when>
      <xsl:when test="$color = 'dimgray'">#696969</xsl:when>
      <xsl:when test="$color = 'dodgerblue'">#1e90ff</xsl:when>
      <xsl:when test="$color = 'firebrick'">#b22222</xsl:when>
      <xsl:when test="$color = 'floralwhite'">#fffaf0</xsl:when>
      <xsl:when test="$color = 'forestgreen'">#228b22</xsl:when>
      <xsl:when test="$color = 'fuchsia'">#ff00ff</xsl:when>
      <xsl:when test="$color = 'gainsboro'">#dcdcdc</xsl:when>
      <xsl:when test="$color = 'ghostwhite'">#f8f8ff</xsl:when>
      <xsl:when test="$color = 'gold'">#ffd700</xsl:when>
      <xsl:when test="$color = 'goldenrod'">#daa520</xsl:when>
      <xsl:when test="$color = 'gray'">#808080</xsl:when>
      <xsl:when test="$color = 'green'">#008000</xsl:when>
      <xsl:when test="$color = 'greenyellow'">#adff2f</xsl:when>
      <xsl:when test="$color = 'honeydew'">#f0fff0</xsl:when>
      <xsl:when test="$color = 'hotpink'">#ff69b4</xsl:when>
      <xsl:when test="$color = 'indianred'">#cd5c5c</xsl:when>
      <xsl:when test="$color = 'indigo'">#4b0082</xsl:when>
      <xsl:when test="$color = 'ivory'">#fffff0</xsl:when>
      <xsl:when test="$color = 'khaki'">#f0e68c</xsl:when>
      <xsl:when test="$color = 'lavender'">#e6e6fa</xsl:when>
      <xsl:when test="$color = 'lavenderblush'">#fff0f5</xsl:when>
      <xsl:when test="$color = 'lawngreen'">#7cfc00</xsl:when>
      <xsl:when test="$color = 'lemonchiffon'">#fffacd</xsl:when>
      <xsl:when test="$color = 'lightblue'">#add8e6</xsl:when>
      <xsl:when test="$color = 'lightcoral'">#f08080</xsl:when>
      <xsl:when test="$color = 'lightcyan'">#e0ffff</xsl:when>
      <xsl:when test="$color = 'lightgoldenrodyellow'">#fafad2</xsl:when>
      <xsl:when test="$color = 'lightgrey'">#d3d3d3</xsl:when>
      <xsl:when test="$color = 'lightgreen'">#90ee90</xsl:when>
      <xsl:when test="$color = 'lightpink'">#ffb6c1</xsl:when>
      <xsl:when test="$color = 'lightsalmon'">#ffa07a</xsl:when>
      <xsl:when test="$color = 'lightseagreen'">#20b2aa</xsl:when>
      <xsl:when test="$color = 'lightskyblue'">#87cefa</xsl:when>
      <xsl:when test="$color = 'lightslategray'">#778899</xsl:when>
      <xsl:when test="$color = 'lightsteelblue'">#b0c4de</xsl:when>
      <xsl:when test="$color = 'lightyellow'">#ffffe0</xsl:when>
      <xsl:when test="$color = 'lime'">#00ff00</xsl:when>
      <xsl:when test="$color = 'limegreen'">#32cd32</xsl:when>
      <xsl:when test="$color = 'linen'">#faf0e6</xsl:when>
      <xsl:when test="$color = 'magenta'">#ff00ff</xsl:when>
      <xsl:when test="$color = 'maroon'">#800000</xsl:when>
      <xsl:when test="$color = 'mediumaquamarine'">#66cdaa</xsl:when>
      <xsl:when test="$color = 'mediumblue'">#0000cd</xsl:when>
      <xsl:when test="$color = 'mediumorchid'">#ba55d3</xsl:when>
      <xsl:when test="$color = 'mediumpurple'">#9370d8</xsl:when>
      <xsl:when test="$color = 'mediumseagreen'">#3cb371</xsl:when>
      <xsl:when test="$color = 'mediumslateblue'">#7b68ee</xsl:when>
      <xsl:when test="$color = 'mediumspringgreen'">#00fa9a</xsl:when>
      <xsl:when test="$color = 'mediumturquoise'">#48d1cc</xsl:when>
      <xsl:when test="$color = 'mediumvioletred'">#c71585</xsl:when>
      <xsl:when test="$color = 'midnightblue'">#191970</xsl:when>
      <xsl:when test="$color = 'mintcream'">#f5fffa</xsl:when>
      <xsl:when test="$color = 'mistyrose'">#ffe4e1</xsl:when>
      <xsl:when test="$color = 'moccasin'">#ffe4b5</xsl:when>
      <xsl:when test="$color = 'navajowhite'">#ffdead</xsl:when>
      <xsl:when test="$color = 'navy'">#000080</xsl:when>
      <xsl:when test="$color = 'oldlace'">#fdf5e6</xsl:when>
      <xsl:when test="$color = 'olive'">#808000</xsl:when>
      <xsl:when test="$color = 'olivedrab'">#6b8e23</xsl:when>
      <xsl:when test="$color = 'orange'">#ffa500</xsl:when>
      <xsl:when test="$color = 'orangered'">#ff4500</xsl:when>
      <xsl:when test="$color = 'orchid'">#da70d6</xsl:when>
      <xsl:when test="$color = 'palegoldenrod'">#eee8aa</xsl:when>
      <xsl:when test="$color = 'palegreen'">#98fb98</xsl:when>
      <xsl:when test="$color = 'paleturquoise'">#afeeee</xsl:when>
      <xsl:when test="$color = 'palevioletred'">#d87093</xsl:when>
      <xsl:when test="$color = 'papayawhip'">#ffefd5</xsl:when>
      <xsl:when test="$color = 'peachpuff'">#ffdab9</xsl:when>
      <xsl:when test="$color = 'peru'">#cd853f</xsl:when>
      <xsl:when test="$color = 'pink'">#ffc0cb</xsl:when>
      <xsl:when test="$color = 'plum'">#dda0dd</xsl:when>
      <xsl:when test="$color = 'powderblue'">#b0e0e6</xsl:when>
      <xsl:when test="$color = 'purple'">#800080</xsl:when>
      <xsl:when test="$color = 'red'">#ff0000</xsl:when>
      <xsl:when test="$color = 'rosybrown'">#bc8f8f</xsl:when>
      <xsl:when test="$color = 'royalblue'">#4169e1</xsl:when>
      <xsl:when test="$color = 'saddlebrown'">#8b4513</xsl:when>
      <xsl:when test="$color = 'salmon'">#fa8072</xsl:when>
      <xsl:when test="$color = 'sandybrown'">#f4a460</xsl:when>
      <xsl:when test="$color = 'seagreen'">#2e8b57</xsl:when>
      <xsl:when test="$color = 'seashell'">#fff5ee</xsl:when>
      <xsl:when test="$color = 'sienna'">#a0522d</xsl:when>
      <xsl:when test="$color = 'silver'">#c0c0c0</xsl:when>
      <xsl:when test="$color = 'skyblue'">#87ceeb</xsl:when>
      <xsl:when test="$color = 'slateblue'">#6a5acd</xsl:when>
      <xsl:when test="$color = 'slategray'">#708090</xsl:when>
      <xsl:when test="$color = 'snow'">#fffafa</xsl:when>
      <xsl:when test="$color = 'springgreen'">#00ff7f</xsl:when>
      <xsl:when test="$color = 'steelblue'">#4682b4</xsl:when>
      <xsl:when test="$color = 'tan'">#d2b48c</xsl:when>
      <xsl:when test="$color = 'teal'">#008080</xsl:when>
      <xsl:when test="$color = 'thistle'">#d8bfd8</xsl:when>
      <xsl:when test="$color = 'tomato'">#ff6347</xsl:when>
      <xsl:when test="$color = 'turquoise'">#40e0d0</xsl:when>
      <xsl:when test="$color = 'violet'">#ee82ee</xsl:when>
      <xsl:when test="$color = 'wheat'">#f5deb3</xsl:when>
      <xsl:when test="$color = 'white'">#ffffff</xsl:when>
      <xsl:when test="$color = 'whitesmoke'">#f5f5f5</xsl:when>
      <xsl:when test="$color = 'yellow'">#ffff00</xsl:when>
      <xsl:when test="$color = 'yellowgreen'">#9acd32</xsl:when>
      <xsl:when test="starts-with($color, '#')"><xsl:value-of select="$color"/></xsl:when>
      <xsl:otherwise><xsl:text></xsl:text></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
  
</xsl:stylesheet>