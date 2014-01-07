<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
 Sourceforge.net. See the accompanying license.txt file for 
 applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2007, 2009 All Rights Reserved. -->
<!-- Updates:
     20090421 robander: Updated so that "flagrules" in all templates
              specifies a default. Can simplify calls from other XSL
              to these templates, with a slight trade-off in processing
              time. Default for "conflictexist" simplifies XSL
              elsewhere with no processing trade-off.
              -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0" 
  xmlns:exsl="http://exslt.org/common" 
  xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  xmlns:styleUtils="org.dita.dost.util.StyleUtils"
  xmlns:imgUtils="org.dita.dost.util.ImgUtils"
  exclude-result-prefixes="exsl dita2html ditamsg styleUtils imgUtils">

 <!-- ========== Flagging with flags & revisions ========== -->
 
 <!-- Single template to set flag variables, generate props and revision flagging, and output
  contents. Can be used by any element that does not use any markup between flags and contents. -->
 <xsl:template match="*" mode="outputContentsWithFlags">
  
  <xsl:variable name="flagrules">
   <xsl:call-template name="getrules"/>
  </xsl:variable>
  <xsl:call-template name="start-flagit">
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
  </xsl:call-template>
  <xsl:call-template name="revblock">
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param> 
  </xsl:call-template>
  <xsl:call-template name="end-flagit">
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param> 
  </xsl:call-template>
 </xsl:template>
 
 <!-- Single template to set the background style, flag based on props and revisions, and output
  contents. Can be used by any element that does not use any markup between flags and contents. -->
 <xsl:template match="*" mode="outputContentsWithFlagsAndStyle">
  <xsl:variable name="flagrules">
   <xsl:call-template name="getrules"/>
  </xsl:variable>
  <xsl:variable name="conflictexist">
   <xsl:call-template name="conflict-check">
    <xsl:with-param name="flagrules" select="$flagrules"/>
   </xsl:call-template>
  </xsl:variable>
  <xsl:call-template name="gen-style">
   <xsl:with-param name="conflictexist" select="$conflictexist"></xsl:with-param> 
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="start-flagit">
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
  </xsl:call-template>
  <xsl:call-template name="revblock">
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param> 
  </xsl:call-template>
  <xsl:call-template name="end-flagit">
   <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param> 
  </xsl:call-template>
 </xsl:template>
 
 <!-- Flags - based on audience, product, platform, and otherprops in the source
  AND prop elements in the val file:
  Flag the text with the artwork from the val file & insert the ALT text from the val file.
  For multiple attr values, output each flag in turn.
 -->

<xsl:template name="getrules">
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
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'audience'"/>
   <xsl:with-param name="flag-att-val" select="@audience"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="@platform and not($FILTERFILE='')">
   
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'platform'"/>
   <xsl:with-param name="flag-att-val" select="@platform"/>
  </xsl:call-template>
   
 </xsl:if>
  <xsl:if test="@product and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'product'"/>
   <xsl:with-param name="flag-att-val" select="@product"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="@otherprops and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'otherprops'"/>
   <xsl:with-param name="flag-att-val" select="@otherprops"/>
  </xsl:call-template>
 </xsl:if>
 
 <xsl:if test="@rev and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'rev'"/>
   <xsl:with-param name="flag-att-val" select="@rev"/>
  </xsl:call-template>
 </xsl:if>
 
  <xsl:if test="not($props='') and not($FILTERFILE='')">
    <xsl:call-template name="ext-getrules">
      <xsl:with-param name="props" select="$props"/>
    </xsl:call-template>
  </xsl:if>
</xsl:template>

  <xsl:template name="getExtProps">
    <xsl:param name="domains"/>
    <xsl:choose>
      <xsl:when test="contains($domains, 'a(props')">
        <xsl:text>,</xsl:text><xsl:value-of select="normalize-space(concat('props',substring-before(substring-after($domains,'a(props'), ')')))"/>
        <xsl:call-template name="getExtProps">
          <xsl:with-param name="domains" select="substring-after(substring-after($domains,'a(props'), ')')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="ext-getrules">
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
          <xsl:call-template name="ext-gen-prop">
            <xsl:with-param name="flag-att-path" select="substring-before($props,',')"/>
            <xsl:with-param name="flag-att-val" select="$propsValue"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:call-template name="ext-getrules">
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
          <xsl:call-template name="ext-gen-prop">
            <xsl:with-param name="flag-att-path" select="$props"/>
            <xsl:with-param name="flag-att-val" select="$propsValue"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="getPropsValue">
    <xsl:param name="propsPath"/>
    <xsl:variable name="propName">
      <xsl:call-template name="getLastPropName">
        <xsl:with-param name="propsPath" select="$propsPath"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="@*[name()=$propName]">
        <xsl:value-of select="@*[name()=$propName]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="getGeneralValue">
          <xsl:with-param name="propName" select="$propName"/>
          <xsl:with-param name="propsPath" select="normalize-space(substring-before($propsPath, $propName))"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="getGeneralValue">
    <xsl:param name="propsPath"/>
    <xsl:param name="propName"/>
    <xsl:variable name="propParentName">
      <xsl:call-template name="getLastPropName">
        <xsl:with-param name="propsPath" select="$propsPath"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="contains(@*[name()=$propParentName],concat($propName,'('))">
        <xsl:value-of select="substring-before(substring-after(@*[name()=$propParentName],concat($propName,'(')),')')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="contains($propsPath,' ')">
            <xsl:call-template name="getGeneralValue">
              <xsl:with-param name="propName" select="$propName"/>
              <xsl:with-param name="propsPath" select="normalize-space(substring-before($propsPath, $propParentName))"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template name="getLastPropName">
    <xsl:param name="propsPath"/>
    <xsl:choose>
      <xsl:when test="contains($propsPath,' ')">
        <xsl:call-template name="getLastPropName">
          <xsl:with-param name="propsPath" select="substring-after($propsPath,' ')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$propsPath"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!-- No flagging attrs allowed to process in phrases - output a message when in debug mode. -->
<xsl:template name="flagcheck">
  
  <xsl:variable name="domains">
    <xsl:value-of select="normalize-space(ancestor-or-self::*[contains(@class,' topic/topic ')][1]/@domains)"/>
  </xsl:variable>
  <xsl:variable name="props">
    <xsl:if test="contains($domains, 'a(props')">
      <xsl:value-of select="normalize-space(substring-before(substring-after($domains,'a(props'), ')'))"/>
    </xsl:if>
  </xsl:variable>
  
 <xsl:if test="$DBG='yes' and not($FILTERFILE='')">
  <xsl:if test="@audience">
    <xsl:apply-templates select="." mode="ditamsg:cannot-flag-inline-element">
      <xsl:with-param name="attr-name" select="'audience'"/>
    </xsl:apply-templates>
  </xsl:if>
  <xsl:if test="@platform">
    <xsl:apply-templates select="." mode="ditamsg:cannot-flag-inline-element">
      <xsl:with-param name="attr-name" select="'platform'"/>
    </xsl:apply-templates>
  </xsl:if>
  <xsl:if test="@product">
    <xsl:apply-templates select="." mode="ditamsg:cannot-flag-inline-element">
      <xsl:with-param name="attr-name" select="'product'"/>
    </xsl:apply-templates>
  </xsl:if>
  <xsl:if test="@otherprops">
    <xsl:apply-templates select="." mode="ditamsg:cannot-flag-inline-element">
      <xsl:with-param name="attr-name" select="'otherprops'"/>
    </xsl:apply-templates>
  </xsl:if>
   <xsl:if test="not($props='')">
     <xsl:call-template name="ext-flagcheck">
       <xsl:with-param name="props" select="$props"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:if>
</xsl:template>

  <xsl:template name="ext-flagcheck">
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
          <xsl:apply-templates select="." mode="ditamsg:cannot-flag-inline-element">
            <xsl:with-param name="attr-name" select="$propName"/>
          </xsl:apply-templates>
        </xsl:if>
        
        <xsl:call-template name="ext-flagcheck">
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
          <xsl:apply-templates select="." mode="ditamsg:cannot-flag-inline-element">
            <xsl:with-param name="attr-name" select="$propName"/>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<xsl:template name="getrules-parent">
  <xsl:variable name="domains">
    <xsl:value-of select="normalize-space(ancestor::*[contains(@class,' topic/topic ')][1]/@domains)"/>
  </xsl:variable>
  <xsl:variable name="props">
    <xsl:if test="contains($domains, 'a(props')">
      <xsl:value-of select="normalize-space(substring-before(substring-after($domains,'a(props'), ')'))"/>
    </xsl:if>
  </xsl:variable>
  
 <!-- Test for the flagging attributes on the parent.
   If found and if the filterfile name was passed in,
      call 'gen-prop' with the values to use. Otherwise return -->
  <xsl:if test="../@audience and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'audience'"/>
   <xsl:with-param name="flag-att-val" select="../@audience"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="../@platform and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'platform'"/>
   <xsl:with-param name="flag-att-val" select="../@platform"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="../@product and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'product'"/>
   <xsl:with-param name="flag-att-val" select="../@product"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="../@otherprops and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'otherprops'"/>
   <xsl:with-param name="flag-att-val" select="../@otherprops"/>
  </xsl:call-template>
 </xsl:if>
 
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILE='')">
  <xsl:call-template name="gen-prop">
   <xsl:with-param name="flag-att" select="'rev'"/>
   <xsl:with-param name="flag-att-val" select="../@rev"/>
  </xsl:call-template>
 </xsl:if>
 
  <xsl:if test="not($props='') and not($FILTERFILE='')">
    <xsl:call-template name="ext-getrules-parent">
      <xsl:with-param name="props" select="$props"/>
    </xsl:call-template>
  </xsl:if>
 
</xsl:template>

  <xsl:template name="ext-getrules-parent">
    <xsl:param name="props"/>
    <xsl:choose>
      <xsl:when test="contains($props,',')">
        <xsl:variable name="propsValue">
          <xsl:call-template name="getPropsValue-parent">
            <xsl:with-param name="propsPath" select="substring-before($props,',')"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="propName">
          <xsl:call-template name="getLastPropName">
            <xsl:with-param name="propsPath" select="substring-before($props,',')"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="not($propsValue='')">
          <xsl:call-template name="ext-gen-prop">
            <xsl:with-param name="flag-att-path" select="substring-before($props,',')"/>
            <xsl:with-param name="flag-att-val" select="$propsValue"/>
          </xsl:call-template>
        </xsl:if>        
        <xsl:call-template name="ext-getrules-parent">
          <xsl:with-param name="props" select="substring-after($props,',')"/>
        </xsl:call-template>
      </xsl:when>
      
      <xsl:otherwise>
        <xsl:variable name="propsValue">
          <xsl:call-template name="getPropsValue-parent">
            <xsl:with-param name="propsPath" select="$props"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="propName">
          <xsl:call-template name="getLastPropName">
            <xsl:with-param name="propsPath" select="$props"/>
          </xsl:call-template>
        </xsl:variable>
        <xsl:if test="not($propsValue='')">
          <xsl:call-template name="ext-gen-prop">
            <xsl:with-param name="flag-att-path" select="$props"/>
            <xsl:with-param name="flag-att-val" select="$propsValue"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template name="getPropsValue-parent">
    <xsl:param name="propsPath"/>
    <xsl:variable name="propName">
      <xsl:call-template name="getLastPropName">
        <xsl:with-param name="propsPath" select="$propsPath"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="../@*[name()=$propName]">
        <xsl:value-of select="../@*[name()=$propName]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="getGeneralValue-parent">
          <xsl:with-param name="propName" select="$propName"/>
          <xsl:with-param name="propsPath" select="normalize-space(substring-before($propsPath, $propName))"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="getGeneralValue-parent">
    <xsl:param name="propsPath"/>
    <xsl:param name="propName"/>
    <xsl:variable name="propParentName">
      <xsl:call-template name="getLastPropName">
        <xsl:with-param name="propsPath" select="$propsPath"/>
      </xsl:call-template>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="contains(../@*[name()=$propParentName],concat($propName,'('))">
        <xsl:value-of select="substring-before(substring-after(../@*[name()=$propParentName],concat($propName,'(')),')')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="contains($propsPath,' ')">
            <xsl:call-template name="getGeneralValue-parent">
              <xsl:with-param name="propName" select="$propName"/>
              <xsl:with-param name="propsPath" select="normalize-space(substring-before($propsPath, $propParentName))"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise/>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  

<!-- Use passed attr value to mark each active flag. -->
  <xsl:template name="ext-gen-prop">
    <xsl:param name="flag-att-path"/>
    <xsl:param name="flag-att-val"/>
    <xsl:variable name="propName">
      <xsl:call-template name="getLastPropName">
        <xsl:with-param name="propsPath" select="$flag-att-path"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="flag-result">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="$propName"/>
        <xsl:with-param name="flag-att-val" select="$flag-att-val"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="exsl:node-set($flag-result)/prop">
        <xsl:copy-of select="$flag-result"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="contains($flag-att-path,' ')">
          <xsl:call-template name="ext-gen-prop">
            <xsl:with-param name="flag-att-path" select="normalize-space(substring-before($flag-att-path, $propName))"/>
            <xsl:with-param name="flag-att-val" select="$flag-att-val"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

 <xsl:template name="gen-prop">
  <xsl:param name="flag-att"/>     <!-- attribute name -->
  <xsl:param name="flag-att-val"/> <!-- content of attribute -->
  
  <!-- Determine the first flag value, which is the value before the first space -->
  <xsl:variable name="firstflag">
   <xsl:choose>
    <xsl:when test="contains($flag-att-val,' ')">
     <xsl:value-of select="substring-before($flag-att-val,' ')"/>
    </xsl:when>
    <xsl:otherwise> <!-- no space, one value -->
     <xsl:value-of select="$flag-att-val"/>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>
  
  <!-- Determine the other flag values, after the first space -->
  <xsl:variable name="moreflags">
   <xsl:choose>
    <xsl:when test="contains($flag-att-val,' ')">
     <xsl:value-of select="substring-after($flag-att-val,' ')"/>
    </xsl:when>
    <xsl:otherwise/> <!-- no space, one value -->
   </xsl:choose>
  </xsl:variable>
  
  <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
   <xsl:when test="$flag-att='rev' and $FILTERDOC/val/revprop[@val=$firstflag][@action='flag']">
    <xsl:copy-of select="$FILTERDOC/val/revprop[@val=$firstflag][@action='flag']"/>
   </xsl:when>
   <xsl:when test="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][@action='flag']">
    <xsl:copy-of select="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][@action='flag']"/>
   </xsl:when>
   <xsl:otherwise/> <!-- that flag not active -->
  </xsl:choose>
  
  <!-- keep testing other values -->
  <xsl:choose>
   <xsl:when test="string-length($moreflags)>0">
    <!-- more values - call it again with remaining values -->
    <xsl:call-template name="gen-prop">
     <xsl:with-param name="flag-att"><xsl:value-of select="$flag-att"/></xsl:with-param>
     <xsl:with-param name="flag-att-val"><xsl:value-of select="$moreflags"/></xsl:with-param>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise/> <!-- no more values -->
  </xsl:choose>
 </xsl:template>
 
 <!-- copy needed elements -->
 <xsl:template match="*" mode="copy-element">
     <xsl:param name="att"/>
     <xsl:param name="bgcolor"/>
     <xsl:param name="fcolor"/>
     <xsl:param name="style"/>
     <xsl:param name="value"/>
     <xsl:param name="flag"/>
     <xsl:param name="cvffilename" select="@source"/>
     <xsl:param name="childnodes"/>
    <!--get the location of dita.xml.properties-->
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
  <!-- scheme list contains the scheme file -->
    <xsl:if test="contains($schemeList, $cvffilename)">
          <!-- get the path of scheme file -->
          <xsl:variable name="submfile">
              <xsl:value-of select="$cvffilename"/><xsl:text>.subm</xsl:text>
          </xsl:variable>
          <xsl:variable name="cvffilepath">
               <xsl:value-of select="concat($WORKDIR,$PATH2PROJ,$submfile)"/>
          </xsl:variable>
     <xsl:if test="document($cvffilepath,/)//*[@keys=$value]//*[@keys=$flag]">
         <!-- copy the child node for flag and just copy the first element whose keys=$flag-->
      <!--xsl:for-each select="document($cvffilepath,/)//*[@keys=$value]/*"-->
      <xsl:for-each select="document($cvffilepath,/)//*[@keys=$value]//*[@keys=$flag][1]">
            <xsl:element name="prop">
             <xsl:attribute name="att">
              <xsl:value-of select="$att"/>
             </xsl:attribute>
             <xsl:attribute name="val">
              <xsl:value-of select="@keys"/>
             </xsl:attribute>
             <xsl:attribute name="action">
              <xsl:value-of select="'flag'"/>
             </xsl:attribute>
             <xsl:attribute name="backcolor">
              <xsl:value-of select="$bgcolor"/>
             </xsl:attribute>
             <xsl:attribute name="color">
              <xsl:value-of select="$fcolor"/>
             </xsl:attribute>
             <xsl:attribute name="style">
              <xsl:value-of select="$style"/>
             </xsl:attribute>
             <xsl:copy-of select="$childnodes"/>
            </xsl:element>
           </xsl:for-each>
     </xsl:if>
  </xsl:if>
 </xsl:template>
 <!-- check CURRENT File -->
 <xsl:template name="checkFile">
    <xsl:param name="in"/>
  <xsl:choose>
   <xsl:when test="starts-with($in, '.\')">
    <xsl:value-of select="substring-after($in, '.\')"/>
   </xsl:when>
   <!-- The file dir passed in by ant cannot by none -->
   <xsl:when test="starts-with($in, './')">
   	<xsl:value-of select="substring-after($in, './')"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="$in"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 <!-- get the scheme list -->
 <xsl:template match="*" mode="check">
  <xsl:value-of select="."/>
 </xsl:template>
 <xsl:template match="*" mode="getVal">
     <xsl:value-of select="@val"/>
 </xsl:template>
 <!-- get background color -->
 <xsl:template match="*" mode="getBgcolor">
  <xsl:choose>
   <xsl:when test="@backcolor">
    <xsl:value-of select="@backcolor"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="''"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 <!-- get font color -->
 <xsl:template match="*" mode="getColor">
       <xsl:choose>
           <xsl:when test="@color">
            <xsl:value-of select="@color"/>
           </xsl:when>
           <xsl:otherwise>
            <xsl:value-of select="''"/>
           </xsl:otherwise>
       </xsl:choose>
 </xsl:template>
 <!-- get font style -->
 <xsl:template match="*" mode="getStyle">
  <xsl:choose>
   <xsl:when test="@style">
    <xsl:value-of select="@style"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="''"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>
 <!-- get child nodes -->
 <xsl:template match="*" mode="getChildNode">
        <xsl:copy-of select="node()"/>
  </xsl:template>
 
 <!-- Shortcuts for generating both rev flags and property flags -->
 <xsl:template name="start-flags-and-rev">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:call-template name="start-flagit">
     <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
   </xsl:call-template>
   <xsl:call-template name="start-revflag">
     <xsl:with-param name="flagrules" select="$flagrules"/>
   </xsl:call-template>
 </xsl:template>
 <xsl:template name="end-flags-and-rev">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:call-template name="end-revflag">
     <xsl:with-param name="flagrules" select="$flagrules"/>
   </xsl:call-template>
   <xsl:call-template name="end-flagit">
     <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param> 
   </xsl:call-template>
 </xsl:template>

<!-- Output starting flag only -->
<xsl:template name="start-revflag">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:if test="@rev and not($FILTERFILE='')">
  <xsl:call-template name="start-mark-rev">
   <xsl:with-param name="revvalue" select="@rev"/>
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- Output ending flag only -->
<xsl:template name="end-revflag">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:if test="@rev and not($FILTERFILE='')">
  <xsl:call-template name="end-mark-rev">
   <xsl:with-param name="revvalue" select="@rev"/>
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- for table entries - if the parent (row) has a rev but the cell does not - output the rev -->
<xsl:template name="start-revflag-parent">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules-parent"/>
 </xsl:param>
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILE='')">
  <xsl:call-template name="start-mark-rev">
   <xsl:with-param name="revvalue" select="../@rev"/>
   <xsl:with-param name="flagrules" select="$flagrules"/>   
  </xsl:call-template>
 </xsl:if>
</xsl:template>
<xsl:template name="end-revflag-parent">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules-parent"/>
 </xsl:param>
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILE='')">
  <xsl:call-template name="end-mark-rev">
   <xsl:with-param name="revvalue" select="../@rev"/>
   <xsl:with-param name="flagrules" select="$flagrules"/> 
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- Output starting & ending flag for "blocked" text.
     Use instead of 'apply-templates' for block areas (P, Note, DD, etc) -->
<xsl:template name="revblock">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:choose>
  <xsl:when test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- draft rev mode, add div w/ rev attr value -->
    <xsl:variable name="revtest"> 
     <xsl:call-template name="find-active-rev-flag">
      <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
     <xsl:when test="$revtest=1">
      <div class="{@rev}">
      <xsl:call-template name="start-mark-rev">
       <xsl:with-param name="revvalue" select="@rev"/>
       <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
      <xsl:apply-templates/>
      <xsl:call-template name="end-mark-rev">
       <xsl:with-param name="revvalue" select="@rev"/>
       <xsl:with-param name="flagrules" select="$flagrules"/> 
      </xsl:call-template>
      </div>
     </xsl:when>
     <xsl:otherwise>
      <xsl:apply-templates/>
     </xsl:otherwise>
    </xsl:choose>
  </xsl:when>
  <xsl:when test="@rev and not($FILTERFILE='')">    <!-- normal rev mode -->
   <xsl:call-template name="start-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
   <xsl:apply-templates/>
   <xsl:call-template name="end-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
  </xsl:when>
  <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>  <!-- rev mode -->
 </xsl:choose>
</xsl:template>

<!-- Output starting & ending flag & color for phrase text.
     Use instead of 'apply-templates' for phrase areas (PH, B, DT, etc) -->
<xsl:template name="revtext">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:variable name="revtest">
   <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> 
     <xsl:call-template name="find-active-rev-flag">               
       <xsl:with-param name="allrevs" select="@rev"/>
     </xsl:call-template>
   </xsl:if>
 </xsl:variable>
<xsl:choose>
  <xsl:when test="$revtest=1">   <!-- Rev is active - add the SPAN -->
   <span class="{@rev}">
   <xsl:call-template name="start-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
   <xsl:call-template name="revstyle">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
   <xsl:call-template name="end-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
   </span>
  </xsl:when>
  <xsl:when test="@rev and not($FILTERFILE='')">         <!-- normal rev mode -->
   <xsl:call-template name="start-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
   <xsl:call-template name="revstyle">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
   <xsl:call-template name="end-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
  </xsl:when>
  <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>  <!-- no rev mode -->
 </xsl:choose>
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="start-mark-rev">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:param name="revvalue"/>
 <xsl:variable name="revtest">
  <xsl:call-template name="find-active-rev-flag">
   <xsl:with-param name="allrevs" select="$revvalue"/>
  </xsl:call-template>
 </xsl:variable>
  <xsl:if test="$revtest=1">
   <xsl:call-template name="start-revision-flag">
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="end-mark-rev">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:param name="revvalue"/>
 <xsl:variable name="revtest">
  <xsl:call-template name="find-active-rev-flag">
   <xsl:with-param name="allrevs" select="$revvalue"/>
  </xsl:call-template>
 </xsl:variable>
  <xsl:if test="$revtest=1">
   <xsl:call-template name="end-revision-flag">
    <xsl:with-param name="flagrules" select="$flagrules"/> 
   </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- output the revision color & apply further templates-->
<xsl:template name="revstyle">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:param name="revvalue"/>
 <xsl:choose>
  <xsl:when test="not($flagrules)">
    <!-- $flagrules was not passed in, so the call must be looking for the deprecated template -->
    <xsl:call-template name="revstyle-deprecated"/>
  </xsl:when>
  <xsl:when test="exsl:node-set($flagrules)/revprop[@color or @backcolor]">
   <xsl:variable name="conflictexist">
    <xsl:call-template name="conflict-check">
     <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
   </xsl:variable>
   <font>
    <xsl:call-template name="gen-style"/>
    <xsl:apply-templates/>
   </font>
  </xsl:when>
  <xsl:otherwise>
   <xsl:variable name="revcolor">
    <xsl:call-template name="find-active-rev-style"> <!-- get 1st active rev color -->
     <xsl:with-param name="allrevs" select="$revvalue"/>
    </xsl:call-template>
   </xsl:variable>
   <xsl:choose>
    <xsl:when test="string-length($revcolor)>0"> <!-- if there's a value, there's an active color -->
     <font>
      <xsl:attribute name="color">
       <xsl:value-of select="$revcolor"/>
      </xsl:attribute>
      <xsl:apply-templates/>
     </font>
    </xsl:when>
    <xsl:otherwise>
     <xsl:apply-templates/> <!-- no active rev color - just apply templates -->
    </xsl:otherwise>
   </xsl:choose>
  </xsl:otherwise>
 </xsl:choose> 
</xsl:template>

<!-- output the beginning revision graphic & ALT text -->
<!-- Reverse the artwork for BIDI languages -->
<xsl:template name="start-revision-flag">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:variable name="biditest"> 
  <xsl:call-template name="bidi-area"/>
 </xsl:variable>
 <xsl:choose>
  <xsl:when test="not($flagrules)">
    <!-- $flagrules was not passed in, so the call must be looking for the deprecated template -->
    <xsl:call-template name="start-revision-flag-deprecated"/>
  </xsl:when>
  <xsl:when test="$biditest='bidi'">
    <xsl:call-template name="end-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="start-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

 <xsl:template name="start-revflagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:apply-templates select="exsl:node-set($flagrules)/revprop[1]" mode="start-revflagit"/>
 </xsl:template>
 
 <xsl:template name="end-revflagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:apply-templates select="exsl:node-set($flagrules)/revprop[last()]" mode="end-revflagit"/>
 </xsl:template>
 
 <xsl:template match="revprop" mode="start-revflagit">
  <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
   <xsl:when test="startflag/@imageref">
    <xsl:variable name="imgsrc" select="startflag/@imageref"/>
    <img>
     <xsl:attribute name="src">
      <xsl:if test="string-length($PATH2PROJ) > 0"><xsl:value-of select="$PATH2PROJ"/></xsl:if>
      <!--
      <xsl:call-template name="get-file-name">
       <xsl:with-param name="file-path" select="$imgsrc"/>
      </xsl:call-template>
      -->
      <xsl:value-of select="$imgsrc"/>
     </xsl:attribute>
     <xsl:if test="startflag/alt-text">
      <xsl:attribute name="alt">
       <xsl:value-of select="startflag/alt-text"/>
      </xsl:attribute>
     </xsl:if>     
    </img>
   </xsl:when>
   <xsl:when test="startflag/alt-text">
    <xsl:value-of select="startflag/alt-text"/>
   </xsl:when>
   <xsl:otherwise/> <!-- that flag not active -->
  </xsl:choose>
  <xsl:apply-templates select="following-sibling::revprop[1]" mode="start-revflagit"/>
 </xsl:template>
 
 <xsl:template match="revprop" mode="end-revflagit">
  <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
   <xsl:when test="endflag/@imageref">
    <xsl:variable name="imgsrc" select="endflag/@imageref"/>
    <img>
     <xsl:attribute name="src">
      <xsl:if test="string-length($PATH2PROJ) > 0"><xsl:value-of select="$PATH2PROJ"/></xsl:if>
      <!--
      <xsl:call-template name="get-file-name">
       <xsl:with-param name="file-path" select="$imgsrc"/>
      </xsl:call-template>
      -->
      <xsl:value-of select="$imgsrc"/>
     </xsl:attribute>
     <xsl:if test="endflag/alt-text">
      <xsl:attribute name="alt">
       <xsl:value-of select="endflag/alt-text"/>
      </xsl:attribute>
     </xsl:if>     
    </img>
   </xsl:when>
   <xsl:when test="endflag/alt-text">
    <xsl:value-of select="endflag/alt-text"/>
   </xsl:when>
   <xsl:otherwise/> <!-- that flag not active -->
  </xsl:choose>
  <xsl:apply-templates select="preceding-sibling::revprop[1]" mode="end-revflagit"/>
 </xsl:template>

<!-- output the ending revision graphic & ALT text -->
<!-- Reverse the artwork for BIDI languages -->
<xsl:template name="end-revision-flag">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:variable name="biditest">
  <xsl:call-template name="bidi-area"/>
 </xsl:variable>
 <xsl:choose>
   <xsl:when test="not($flagrules)">
     <!-- $flagrules was not passed in, so the call must be looking for the deprecated template -->
     <xsl:call-template name="end-revision-flag-deprecated"/>
   </xsl:when>
  <xsl:when test="$biditest='bidi'">
    <xsl:call-template name="start-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>
    <xsl:call-template name="end-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- output the beginning revision graphic & ALT text -->
<xsl:template name="start-rev-art">
 <xsl:param name="deltaname"/>
 <!-- zdihua -->
 <!-- 
  <img src="{$PATH2PROJ}{$deltaname}">
  <xsl:attribute name='alt'>
   <xsl:call-template name="getString">
    <xsl:with-param name="stringName" select="'Start of change'"/>
   </xsl:call-template>
  </xsl:attribute>
 </img>
 -->
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

<!-- Use @rev to find the first active flagged revision.
     Return 1 for active.
     Return 0 for non-active. -->
<xsl:template name="find-active-rev-flag">
  <xsl:param name="allrevs"/>

  <!-- Determine the first rev value, which is the value before the first space -->
  <xsl:variable name="firstrev">
   <xsl:choose>
    <xsl:when test="contains($allrevs,' ')">
     <xsl:value-of select="substring-before($allrevs,' ')"/>
    </xsl:when>
    <xsl:otherwise> <!-- no space, one value -->
     <xsl:value-of select="$allrevs"/>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>

  <!-- Determine the other rev value, after the first space -->
  <xsl:variable name="morerevs">
   <xsl:choose>
    <xsl:when test="contains($allrevs,' ')">
     <xsl:value-of select="substring-after($allrevs,' ')"/>
    </xsl:when>
    <xsl:otherwise/> <!-- no space, one value -->
   </xsl:choose>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$FILTERDOC/val/revprop[@val=$firstrev][@action='flag']">
     <xsl:value-of select="1"/> <!-- rev active -->
   </xsl:when>
   <xsl:otherwise>              <!-- rev not active -->

    <!-- keep testing other values -->
    <xsl:choose>
     <xsl:when test="string-length($morerevs)>0">
      <!-- more values - call it again with remaining values -->
      <xsl:call-template name="find-active-rev-flag">
       <xsl:with-param name="allrevs"><xsl:value-of select="$morerevs"/></xsl:with-param>
      </xsl:call-template>
     </xsl:when>
     <xsl:otherwise> <!-- no more values - none found -->
      <xsl:value-of select="0"/>
     </xsl:otherwise>
    </xsl:choose>

   </xsl:otherwise>
  </xsl:choose>

</xsl:template>

<!-- Use @rev to find the first active styled revision.
     Return color setting when active.
     Return null for non-active. -->
<xsl:template name="find-active-rev-style">
  <xsl:param name="allrevs"/>

  <!-- Determine the first rev value, which is the value before the first space -->
  <xsl:variable name="firstrev">
   <xsl:choose>
    <xsl:when test="contains($allrevs,' ')">
     <xsl:value-of select="substring-before($allrevs,' ')"/>
    </xsl:when>
    <xsl:otherwise> <!-- no space, one value -->
     <xsl:value-of select="$allrevs"/>
    </xsl:otherwise>
   </xsl:choose>
  </xsl:variable>

  <!-- Determine the other rev value, after the first space -->
  <xsl:variable name="morerevs">
   <xsl:choose>
    <xsl:when test="contains($allrevs,' ')">
     <xsl:value-of select="substring-after($allrevs,' ')"/>
    </xsl:when>
    <xsl:otherwise/> <!-- no space, one value -->
   </xsl:choose>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$FILTERDOC/val/revprop[@val=$firstrev]/@style">
     <!-- rev active -->
     <xsl:value-of select="$FILTERDOC/val/revprop[@val=$firstrev]/@style"/>
   </xsl:when>
   <xsl:otherwise>              <!-- rev not active -->

    <!-- keep testing other values -->
    <xsl:choose>
     <xsl:when test="string-length($morerevs)>0">
      <!-- more values - call it again with remaining values -->
      <xsl:call-template name="find-active-rev-style">
       <xsl:with-param name="allrevs"><xsl:value-of select="$morerevs"/></xsl:with-param>
      </xsl:call-template>
     </xsl:when>
     <xsl:otherwise/> <!-- no more values - none found -->
    </xsl:choose>

   </xsl:otherwise>
  </xsl:choose>

</xsl:template>

 <xsl:template name="conflict-check">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:choose>
   <xsl:when test="exsl:node-set($flagrules)/*">
    <xsl:apply-templates select="exsl:node-set($flagrules)/*[1]" mode="conflict-check"/>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="'false'"/>
   </xsl:otherwise>
  </xsl:choose>  
 </xsl:template>
 
 <xsl:template match="prop|revprop" mode="conflict-check">
  <xsl:param name="color"/>
  <xsl:param name="backcolor"/>
  
  <xsl:choose>   
   <xsl:when test="(@color and @color!='' and $color!='' and $color!=@color)or(@backcolor and @backcolor!='' and $backcolor!='' and $backcolor!=@backcolor)">
    <xsl:value-of select="'true'"/>
   </xsl:when>
   <xsl:when test="following-sibling::*">
    <xsl:apply-templates select="following-sibling::*[1]" mode="conflict-check">
     <xsl:with-param name="color" select="@color"/>
     <xsl:with-param name="backcolor" select="@backcolor"/>
    </xsl:apply-templates>
   </xsl:when>
   <xsl:otherwise>
    <xsl:value-of select="'false'"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <!-- Currently, gen-style is never called without conflictexist in the OT
      code, so the default is never used. If we replace the default with the
      default code used elsewhere, then most or all calls to gen-style can be
      simplified. -->
 <xsl:template name="gen-style">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
  <xsl:param name="conflictexist">
    <xsl:call-template name="conflict-check">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:param>
  <xsl:variable name="validstyle">
    <!-- This variable is used to prevent using pre-OASIS or unrecognized ditaval styles -->
    <xsl:if test="$conflictexist='false' and exsl:node-set($flagrules)/*[@style]">
      <xsl:choose>
        <xsl:when test="exsl:node-set($flagrules)/*/@style='italics'">YES</xsl:when>
        <xsl:when test="exsl:node-set($flagrules)/*/@style='bold'">YES</xsl:when>
        <xsl:when test="exsl:node-set($flagrules)/*/@style='underline'">YES</xsl:when>
        <xsl:when test="exsl:node-set($flagrules)/*/@style='double-underline'">YES</xsl:when>
        <xsl:when test="exsl:node-set($flagrules)/*/@style='overline'">YES</xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:variable>
  <xsl:choose>  
   <xsl:when test="$conflictexist='true' and $FILTERDOC/val/style-conflict[@foreground-conflict-color or @background-conflict-color]">
     <xsl:apply-templates select="." mode="ditamsg:conflict-text-style-applied"/>
    <xsl:attribute name="style">     
     <xsl:if test="$FILTERDOC/val/style-conflict[@foreground-conflict-color]">
      <xsl:text>color:</xsl:text>
      <xsl:value-of select="$FILTERDOC/val/style-conflict/@foreground-conflict-color"/>
      <xsl:text>;</xsl:text>
     </xsl:if>
     <xsl:if test="$FILTERDOC/val/style-conflict[@background-conflict-color]">
      <xsl:text>background-color:</xsl:text>
      <xsl:value-of select="$FILTERDOC/val/style-conflict/@background-conflict-color"/>
      <xsl:text>;</xsl:text>
     </xsl:if>     
    </xsl:attribute>
   </xsl:when>
   <xsl:when test="$conflictexist='false' and 
                   (exsl:node-set($flagrules)/*[@color or @backcolor] or $validstyle='YES')">
    <xsl:attribute name="style">     
     <xsl:if test="exsl:node-set($flagrules)/*[@color]">
      <xsl:text>color:</xsl:text>
      <xsl:value-of select="exsl:node-set($flagrules)/*[@color]/@color"/>
      <xsl:text>;</xsl:text>
     </xsl:if>
     <xsl:if test="exsl:node-set($flagrules)/*[@backcolor]">
      <xsl:text>background-color:</xsl:text>
      <xsl:value-of select="exsl:node-set($flagrules)/*[@backcolor]/@backcolor"/>
      <xsl:text>;</xsl:text>
     </xsl:if>     
     <xsl:if test="exsl:node-set($flagrules)/*/@style='italics'">
      <xsl:text>font-style:italic;</xsl:text>
     </xsl:if>     
     <xsl:if test="exsl:node-set($flagrules)/*/@style='bold'">
      <xsl:text>font-weight:bold;</xsl:text>
     </xsl:if>     
     <xsl:if test="exsl:node-set($flagrules)/*/@style='underline' or 
                   exsl:node-set($flagrules)/*/@style='double-underline'">
      <!-- For double-underline, style="border-bottom: 3px double;" seems to work
           in some cases, but not in all. For now, treat it as underline. -->
      <xsl:text>text-decoration:underline;</xsl:text>
     </xsl:if>     
     <xsl:if test="exsl:node-set($flagrules)/*/@style='overline'">
      <xsl:text>text-decoration:overline;</xsl:text>
     </xsl:if>     
    </xsl:attribute>
   </xsl:when>
  </xsl:choose>
 </xsl:template>
 
 <xsl:template name="start-flagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:apply-templates select="exsl:node-set($flagrules)/prop[1]" mode="start-flagit"/>
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

 <xsl:template name="end-flagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:apply-templates select="exsl:node-set($flagrules)/prop[last()]" mode="end-flagit"/>
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

 <xsl:template match="*" mode="ditamsg:cannot-flag-inline-element">
   <xsl:param name="attr-name"/>
   <xsl:call-template name="output-message">
     <xsl:with-param name="msgnum">042</xsl:with-param>
     <xsl:with-param name="msgsev">W</xsl:with-param>
     <xsl:with-param name="msgparams">%i=<xsl:value-of select="$attr-name"/></xsl:with-param>
   </xsl:call-template>
 </xsl:template>
 <xsl:template match="*" mode="ditamsg:conflict-text-style-applied">
   <xsl:call-template name="output-message">
    <xsl:with-param name="msgnum">054</xsl:with-param>
    <xsl:with-param name="msgsev">W</xsl:with-param>
   </xsl:call-template>
 </xsl:template>
 
 


<!-- ===================================================================== -->
</xsl:stylesheet>