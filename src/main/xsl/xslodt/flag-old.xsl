<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
 Sourceforge.net. See the accompanying license.txt file for 
 applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2007 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!-- ========== Flagging with flags & revisions ========== -->

<!-- Flags - based on audience, product, platform, and otherprops in the source
               AND prop elements in the val file:
             Flag the text with the artwork from the val file & insert the ALT text from the val file.
             For multiple attr values, output each flag in turn.
     -->
<xsl:template name="flagit">
  <xsl:call-template name="output-message">
  <xsl:with-param name="msgnum">055</xsl:with-param>
  <xsl:with-param name="msgsev">W</xsl:with-param>
  </xsl:call-template>
  <xsl:call-template name="flagit-deprecated"/>
</xsl:template>
<xsl:template name="flagit-deprecated"> 
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
 <!-- Test for the flagging attributes. If found, call 'mark-prop' with the values to use. Otherwise return -->
  <xsl:if test="@audience and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'audience'"/>
   <xsl:with-param name="flag-att-val" select="@audience"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="@platform and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'platform'"/>
   <xsl:with-param name="flag-att-val" select="@platform"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="@product and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'product'"/>
   <xsl:with-param name="flag-att-val" select="@product"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="@otherprops and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'otherprops'"/>
   <xsl:with-param name="flag-att-val" select="@otherprops"/>
  </xsl:call-template>
 </xsl:if>
 
  <xsl:if test="not($props='') and not($FILTERFILE='')">
    <xsl:call-template name="ext-flagit">
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

  <xsl:template name="ext-flagit">
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
          <xsl:call-template name="ext-mark-prop">
            <xsl:with-param name="flag-att-path" select="substring-before($props,',')"/>
            <xsl:with-param name="flag-att-val" select="$propsValue"/>
          </xsl:call-template>
        </xsl:if>
        <xsl:call-template name="ext-flagit">
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
          <xsl:call-template name="ext-mark-prop">
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
   <xsl:call-template name="output-message">
    <xsl:with-param name="msgnum">042</xsl:with-param>
    <xsl:with-param name="msgsev">I</xsl:with-param>
    <xsl:with-param name="msgparams">%1=audience</xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="@platform">
   <xsl:call-template name="output-message">
    <xsl:with-param name="msgnum">042</xsl:with-param>
    <xsl:with-param name="msgsev">I</xsl:with-param>
    <xsl:with-param name="msgparams">%1=platform</xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="@product">
   <xsl:call-template name="output-message">
    <xsl:with-param name="msgnum">042</xsl:with-param>
    <xsl:with-param name="msgsev">I</xsl:with-param>
    <xsl:with-param name="msgparams">%1=product</xsl:with-param>
   </xsl:call-template>
  </xsl:if>
  <xsl:if test="@otherprops">
   <xsl:call-template name="output-message">
    <xsl:with-param name="msgnum">042</xsl:with-param>
    <xsl:with-param name="msgsev">I</xsl:with-param>
    <xsl:with-param name="msgparams">%1=otherprops</xsl:with-param>
   </xsl:call-template>
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
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">042</xsl:with-param>
            <xsl:with-param name="msgsev">I</xsl:with-param>
            <xsl:with-param name="msgparams">%1=<xsl:value-of select="$propName"/></xsl:with-param>
          </xsl:call-template>
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
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">042</xsl:with-param>
            <xsl:with-param name="msgsev">I</xsl:with-param>
            <xsl:with-param name="msgparams">%1=<xsl:value-of select="$propName"/></xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<xsl:template name="flagit-parent">
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
      call 'mark-prop' with the values to use. Otherwise return -->
  <xsl:if test="../@audience and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'audience'"/>
   <xsl:with-param name="flag-att-val" select="../@audience"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="../@platform and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'platform'"/>
   <xsl:with-param name="flag-att-val" select="../@platform"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="../@product and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'product'"/>
   <xsl:with-param name="flag-att-val" select="../@product"/>
  </xsl:call-template>
 </xsl:if>
  <xsl:if test="../@otherprops and not($FILTERFILE='')">
  <xsl:call-template name="mark-prop">
   <xsl:with-param name="flag-att" select="'otherprops'"/>
   <xsl:with-param name="flag-att-val" select="../@otherprops"/>
  </xsl:call-template>
 </xsl:if>
 
  <xsl:if test="not($props='') and not($FILTERFILE='')">
    <xsl:call-template name="ext-flagit-parent">
      <xsl:with-param name="props" select="$props"/>
    </xsl:call-template>
  </xsl:if>
 
</xsl:template>

  <xsl:template name="ext-flagit-parent">
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
          <xsl:call-template name="ext-mark-prop">
            <xsl:with-param name="flag-att-path" select="substring-before($props,',')"/>
            <xsl:with-param name="flag-att-val" select="$propsValue"/>
          </xsl:call-template>
        </xsl:if>        
        <xsl:call-template name="ext-flagit-parent">
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
          <xsl:call-template name="ext-mark-prop">
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
  <xsl:template name="ext-mark-prop">
    <xsl:param name="flag-att-path"/>
    <xsl:param name="flag-att-val"/>
    <xsl:variable name="propName">
      <xsl:call-template name="getLastPropName">
        <xsl:with-param name="propsPath" select="$flag-att-path"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="flag-result">
      <xsl:call-template name="mark-prop">
        <xsl:with-param name="flag-att" select="$propName"/>
        <xsl:with-param name="flag-att-val" select="$flag-att-val"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="exsl:node-set($flag-result)/img" xmlns:exsl="http://exslt.org/common">
        <xsl:copy-of select="$flag-result"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="contains($flag-att-path,' ')">
          <xsl:call-template name="ext-mark-prop">
            <xsl:with-param name="flag-att-path" select="normalize-space(substring-before($flag-att-path, $propName))"/>
            <xsl:with-param name="flag-att-val" select="$flag-att-val"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<xsl:template name="mark-prop">
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
   <xsl:when test="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][@action='flag'][@img]">
    <!-- output the flag -->
    <xsl:variable name="imgsrc" select="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][@action='flag']/@img"/>
    
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
     <xsl:attribute name="alt"> <!-- always insert an ALT - if it's blank, assume the user didn't want to fill it. -->
      <xsl:value-of select="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][@action='flag']/@alt"/>
     </xsl:attribute>
    </img>
   </xsl:when>
   <xsl:otherwise/> <!-- that flag not active -->
  </xsl:choose>

  <!-- keep testing other values -->
  <xsl:choose>
   <xsl:when test="string-length($moreflags)>0">
    <!-- more values - call it again with remaining values -->
    <xsl:call-template name="mark-prop">
     <xsl:with-param name="flag-att"><xsl:value-of select="$flag-att"/></xsl:with-param>
     <xsl:with-param name="flag-att-val"><xsl:value-of select="$moreflags"/></xsl:with-param>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise/> <!-- no more values -->
  </xsl:choose>

</xsl:template>

<!-- Output starting flag only -->
<xsl:template name="start-revflag">
 <xsl:if test="@rev and not($FILTERFILE='')">
  <xsl:call-template name="start-mark-rev">
   <xsl:with-param name="revvalue" select="@rev"/>
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- Output ending flag only -->
<xsl:template name="end-revflag">
 <xsl:if test="@rev and not($FILTERFILE='')">
  <xsl:call-template name="end-mark-rev">
   <xsl:with-param name="revvalue" select="@rev"/>
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- for table entries - if the parent (row) has a rev but the cell does not - output the rev -->
<xsl:template name="start-revflag-parent">
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILE='')">
  <xsl:call-template name="start-mark-rev">
   <xsl:with-param name="revvalue" select="../@rev"/>
  </xsl:call-template>
 </xsl:if>
</xsl:template>
<xsl:template name="end-revflag-parent">
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILE='')">
  <xsl:call-template name="end-mark-rev">
   <xsl:with-param name="revvalue" select="../@rev"/>
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- Output starting & ending flag & color for phrase text.
     Use instead of 'apply-templates' for phrase areas (PH, B, DT, etc) -->
<xsl:template name="revtext">
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
   </xsl:call-template>
   <xsl:call-template name="revstyle-deprecated">
    <xsl:with-param name="revvalue" select="@rev"/>
   </xsl:call-template>
   <xsl:call-template name="end-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
   </xsl:call-template>
   </span>
  </xsl:when>
  <xsl:when test="@rev and not($FILTERFILE='')">         <!-- normal rev mode -->
   <xsl:call-template name="start-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
   </xsl:call-template>
   <xsl:call-template name="revstyle-deprecated">
    <xsl:with-param name="revvalue" select="@rev"/>
   </xsl:call-template>
   <xsl:call-template name="end-mark-rev">
    <xsl:with-param name="revvalue" select="@rev"/>
   </xsl:call-template>
  </xsl:when>
  <xsl:otherwise><xsl:apply-templates/></xsl:otherwise>  <!-- no rev mode -->
 </xsl:choose>
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="start-mark-rev">
 <xsl:param name="revvalue"/>
 <xsl:variable name="revtest">
  <xsl:call-template name="find-active-rev-flag">
   <xsl:with-param name="allrevs" select="$revvalue"/>
  </xsl:call-template>
 </xsl:variable>
  <xsl:if test="$revtest=1">
   <xsl:call-template name="start-revision-flag-deprecated"/>
  </xsl:if>
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="end-mark-rev">
 <xsl:param name="revvalue"/>
 <xsl:variable name="revtest">
  <xsl:call-template name="find-active-rev-flag">
   <xsl:with-param name="allrevs" select="$revvalue"/>
  </xsl:call-template>
 </xsl:variable>
  <xsl:if test="$revtest=1">
   <xsl:call-template name="end-revision-flag-deprecated"/>
  </xsl:if>
</xsl:template>

<!-- output the beginning revision graphic & ALT text -->
<xsl:template name="start-rev-art">
 <xsl:param name="deltaname"/>
  <img src="{$PATH2PROJ}{$deltaname}">
  <xsl:attribute name='alt'>
   <xsl:call-template name="getString">
    <xsl:with-param name="stringName" select="'Start of change'"/>
   </xsl:call-template>
  </xsl:attribute>
 </img>
</xsl:template>
<!-- output the ending revision graphic & ALT text -->
<xsl:template name="end-rev-art">
 <xsl:param name="deltaname"/>
 <img src="{$PATH2PROJ}{$deltaname}">
  <xsl:attribute name='alt'>
   <xsl:call-template name="getString">
    <xsl:with-param name="stringName" select="'End of change'"/>
   </xsl:call-template>
  </xsl:attribute>
 </img>
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

<!-- ===================================================================== -->

</xsl:stylesheet>