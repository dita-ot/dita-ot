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
  xmlns:exsl="http://exslt.org/common" 
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:styleUtils="org.dita.dost.util.StyleUtils"
  xmlns:imgUtils="org.dita.dost.util.ImgUtils"
  exclude-result-prefixes="exsl ditamsg styleUtils imgUtils">
  
  <!-- =========== TEMPLATES FOR ODT FLAGGING =========== -->
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
    <xsl:call-template name="start-add-odt-revflags">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="*" mode="end-add-odt-flags">
    <!-- get style rules -->
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <!-- add rev style -->
    <xsl:call-template name="end-add-odt-revflags">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
    
    <!-- add images -->
    <xsl:call-template name="end-flagit">
      <xsl:with-param name="flagrules" select="$flagrules"/> 
    </xsl:call-template>
    
  </xsl:template>
  
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
  
  
</xsl:stylesheet>