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
     20121003 robander: Flag logic has moved into preprocess, and is now
              handled using simpler htmlflag.xsl process. All functions in
              this file are deprecated.
              -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0" 
  xmlns:exsl="http://exslt.org/common" 
  xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  exclude-result-prefixes="exsl dita2html ditamsg">

 <!-- ========== Flagging with flags & revisions ========== -->

  <xsl:variable name="documentDomains">
    <xsl:value-of select="normalize-space(/*[contains(@class,' topic/topic ')]/@domains |
                                          /dita/*[contains(@class,' topic/topic ')][1]/@domains)"/>
  </xsl:variable>
  <xsl:variable name="collectPropsExtensions">
    <xsl:call-template name="getExtProps">
      <xsl:with-param name="domains" select="$documentDomains"/>
    </xsl:call-template>
  </xsl:variable>
  <!-- Specialized attributes for analysis by flagging templates. Format is:
       props attr1,props attr2,props attr3 -->
  <xsl:variable name="propsExtensions">
    <xsl:value-of select="substring-after($collectPropsExtensions, ',')"/>
  </xsl:variable>
 
 <!-- Single template to set flag variables, generate props and revision flagging, and output
  contents. Can be used by any element that does not use any markup between flags and contents. -->
 <xsl:template match="*" mode="outputContentsWithFlags">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS -->
   <xsl:apply-templates/>
 </xsl:template>
 
 <!-- Single template to set the background style, flag based on props and revisions, and output
  contents. Can be used by any element that does not use any markup between flags and contents. -->
 <xsl:template match="*" mode="outputContentsWithFlagsAndStyle">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS -->
   <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/@outputclass" mode="add-ditaval-style"/>
   <xsl:apply-templates/>
 </xsl:template>
 
 <!-- Flags - based on audience, product, platform, and otherprops in the source
  AND prop elements in the val file:
  Flag the text with the artwork from the val file & insert the ALT text from the val file.
  For multiple attr values, output each flag in turn.
 -->

<xsl:template name="getrules">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS.
       "getrules" is no longer needed; active flags are already specified as children. -->
 <!-- Test for the flagging attributes. If found, call 'gen-prop' with the values to use. Otherwise return -->
  <xsl:if test="normalize-space($FILTERFILE)!=''">
    <xsl:if test="@audience">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'audience'"/>
        <xsl:with-param name="flag-att-val" select="@audience"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="@platform">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'platform'"/>
        <xsl:with-param name="flag-att-val" select="@platform"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="@product">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'product'"/>
        <xsl:with-param name="flag-att-val" select="@product"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="@otherprops">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'otherprops'"/>
        <xsl:with-param name="flag-att-val" select="@otherprops"/>
      </xsl:call-template>
    </xsl:if>
 
    <xsl:if test="@rev">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'rev'"/>
        <xsl:with-param name="flag-att-val" select="@rev"/>
      </xsl:call-template>
    </xsl:if>
 
    <xsl:if test="$propsExtensions!=''">
      <xsl:call-template name="ext-getrules">
        <xsl:with-param name="props" select="$propsExtensions"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:if>
</xsl:template>

  <!-- Determine what attributes are defined as extensions of @props. 
       No longer used by flagging but could be useful as a general utility. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS.
         "ext-getrules" is no longer needed; active flags are already specified as children. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS.
       The new process allows flagging on phrases; this check is no longer needed. -->
</xsl:template>

  <xsl:template name="ext-flagcheck">
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
 <!-- Test for the flagging attributes on the parent.
   If found and if the filterfile name was passed in,
      call 'gen-prop' with the values to use. Otherwise return -->
  <xsl:if test="normalize-space($FILTERFILE)!=''">
    <xsl:if test="../@audience">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'audience'"/>
        <xsl:with-param name="flag-att-val" select="../@audience"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="../@platform">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'platform'"/>
        <xsl:with-param name="flag-att-val" select="../@platform"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="../@product">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'product'"/>
        <xsl:with-param name="flag-att-val" select="../@product"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="../@otherprops">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'otherprops'"/>
        <xsl:with-param name="flag-att-val" select="../@otherprops"/>
      </xsl:call-template>
    </xsl:if>
 
    <xsl:if test="../@rev and not(@rev)">
      <xsl:call-template name="gen-prop">
        <xsl:with-param name="flag-att" select="'rev'"/>
        <xsl:with-param name="flag-att-val" select="../@rev"/>
      </xsl:call-template>
    </xsl:if>
 
    <xsl:if test="$propsExtensions!=''">
      <xsl:call-template name="ext-getrules-parent">
        <xsl:with-param name="props" select="$propsExtensions"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:if>
</xsl:template>

  <xsl:template name="ext-getrules-parent">
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <xsl:when test="$FILTERDOC/val/prop[@att=$flag-att][not(@val=$firstflag)][@action='flag']">
    
    <xsl:for-each select="$FILTERDOC/val/prop[@att=$flag-att][not(@val=$firstflag)][@action='flag']">
           <!-- get the val -->
           <xsl:variable name="val">
                    <xsl:apply-templates select="." mode="getVal"/>
           </xsl:variable>
           <!-- get the backcolor -->
           <xsl:variable name="backcolor">
                     <xsl:apply-templates select="." mode="getBgcolor"/>
           </xsl:variable>
           <!-- get the color -->
           <xsl:variable name="color">
                  <xsl:apply-templates select="." mode="getColor"/>
           </xsl:variable>
           <!-- get the style -->
           <xsl:variable name="style">
            <xsl:apply-templates select="." mode="getStyle"/>
           </xsl:variable>
           <!-- get child node -->
           <xsl:variable name="childnode">
                   <xsl:apply-templates select="." mode="getChildNode"/>
           </xsl:variable>
           <!-- get the location of schemekeydef.xml -->
           <xsl:variable name="KEYDEF-FILE" select="concat($WORKDIR,$PATH2PROJ,'schemekeydef.xml')"/>
          <!--keydef.xml contains the val  -->
          <xsl:if test="(document($KEYDEF-FILE, /)//*[@keys=$val])">
            <!-- copy needed elements -->
              <xsl:apply-templates select="(document($KEYDEF-FILE, /)//*[@keys=$val])" mode="copy-element">
                 <xsl:with-param name="att" select="$flag-att"/>
                 <xsl:with-param name="bgcolor" select="$backcolor"/>
                 <xsl:with-param name="fcolor" select="$color"/>
                 <xsl:with-param name="style" select="$style"/>
                 <xsl:with-param name="value" select="$val"/>
                 <xsl:with-param name="flag" select="$firstflag"/>
                 <xsl:with-param name="childnodes" select="$childnode"/>
              </xsl:apply-templates>
           </xsl:if>
        </xsl:for-each>
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
     <xsl:param name="att"/>
     <xsl:param name="bgcolor"/>
     <xsl:param name="fcolor"/>
     <xsl:param name="style"/>
     <xsl:param name="value"/>
     <xsl:param name="flag"/>
     <xsl:param name="cvffilename" select="@source"/>
     <xsl:param name="childnodes"/>
    <!--get the location of subject_scheme.dictionary-->
    <xsl:variable name="INITIAL-PROPERTIES-FILE" select="concat($WORKDIR , $PATH2PROJ , 'subject_scheme.dictionary')"/>
    <xsl:variable name="PROPERTIES-FILE" select="$INITIAL-PROPERTIES-FILE"/>
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:value-of select="."/>
 </xsl:template>
 <xsl:template match="*" mode="getVal">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
     <xsl:value-of select="@val"/>
 </xsl:template>
 <!-- get background color -->
 <xsl:template match="*" mode="getBgcolor">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
        <xsl:copy-of select="node()"/>
  </xsl:template>
 
 <!-- Shortcuts for generating both rev flags and property flags -->
 <xsl:template name="start-flags-and-rev">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
   <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
 </xsl:template>
 <xsl:template name="end-flags-and-rev">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
   <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
 </xsl:template>

<!-- Output starting flag only -->
<xsl:template name="start-revflag">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>
</xsl:template>

<!-- Output ending flag only -->
<xsl:template name="end-revflag">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>
</xsl:template>

<!-- for table entries - if the parent (row) has a rev but the cell does not - output the rev -->
<xsl:template name="start-revflag-parent">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>
</xsl:template>
<xsl:template name="end-revflag-parent">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>
</xsl:template>

<!-- Output starting & ending flag for "blocked" text.
     Use instead of 'apply-templates' for block areas (P, Note, DD, etc) -->
<xsl:template name="revblock">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <!--<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>-->
  <xsl:apply-templates/>
  <!--<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>-->
</xsl:template>

<!-- Output starting & ending flag & color for phrase text.
     Use instead of 'apply-templates' for phrase areas (PH, B, DT, etc) -->
<xsl:template name="revtext">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <!--<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>-->
  <xsl:apply-templates/>
  <!--<xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>-->
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="start-mark-rev">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="end-mark-rev">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>
</xsl:template>

<!-- output the revision color & apply further templates-->
<xsl:template name="revstyle">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <span>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/@outputclass" mode="add-ditaval-style"/>
    <xsl:apply-templates/>
  </span>
</xsl:template>

<!-- output the beginning revision graphic & ALT text -->
<!-- Reverse the artwork for BIDI languages -->
<xsl:template name="start-revision-flag">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
 <xsl:variable name="biditest"> 
  <xsl:call-template name="bidi-area"/>
 </xsl:variable>
 <xsl:choose>
  <xsl:when test="$biditest='bidi'">
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

 <xsl:template name="start-revflagit">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
   <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>
 </xsl:template>
 
 <xsl:template name="end-revflagit">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
   <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>
 </xsl:template>
 
 <xsl:template match="revprop" mode="start-revflagit">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
 <xsl:variable name="biditest">
  <xsl:call-template name="bidi-area"/>
 </xsl:variable>
 <xsl:choose>
  <xsl:when test="$biditest='bidi'">
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]/revprop/startflag" mode="ditaval-outputflag"/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]/revprop/endflag" mode="ditaval-outputflag"/>
  </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- output the beginning revision graphic & ALT text -->
<xsl:template name="start-rev-art">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
 <xsl:param name="deltaname"/>
 <img src="{$PATH2PROJ}{$deltaname}">
  <xsl:attribute name='alt'>
   <xsl:call-template name="getString">
    <xsl:with-param name="stringName" select="'End of change'"/>
   </xsl:call-template>
  </xsl:attribute>
 </img>
</xsl:template>

<!-- Shortcut for old multi-line calls to find-active-rev-flag.
     Return 1 for active revision when draft is on, return 0 otherwise. -->
<xsl:template match="*" mode="mark-revisions-for-draft">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:choose>
    <xsl:when test="@rev and not($FILTERFILE='') and ($DRAFT='yes')">
      <xsl:call-template name="find-active-rev-flag"/>
    </xsl:when>
    <xsl:otherwise>0</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Use @rev to find the first active flagged revision.
     Return 1 for active.
     Return 0 for non-active. 
     NOTE: this template is only called when a filter file is available and
     when there is a revision to evaluate. -->
<xsl:template name="find-active-rev-flag">
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:param name="allrevs" select="@rev"/>

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
  <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="normalize-space($FILTERFILE)=''">
        <xsl:value-of select="'false'"/>
      </xsl:when>
      <xsl:when test="exsl:node-set($flagrules)/*">
        <xsl:apply-templates select="exsl:node-set($flagrules)/*[1]" mode="conflict-check"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>  
  </xsl:template>
 
 <xsl:template match="prop|revprop" mode="conflict-check">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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

  <xsl:template name="gen-style">
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS.
         Style is now determined in pre-process, and added with commonattributes. -->
  </xsl:template>
 
  <xsl:template name="start-flagit">
    <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:apply-templates select="exsl:node-set($flagrules)/prop[1]" mode="start-flagit"/>
  </xsl:template>
 
 <xsl:template match="prop" mode="start-flagit">  
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <xsl:when test="@img">
    <!-- output the flag -->
    <xsl:variable name="imgsrc" select="@img"/>    
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
      <xsl:value-of select="@alt"/>
     </xsl:attribute>
    </img>
   </xsl:when>
   <xsl:otherwise/> <!-- that flag not active -->
  </xsl:choose>
  <xsl:apply-templates select="following-sibling::prop[1]" mode="start-flagit"/>
 </xsl:template>

 <xsl:template name="end-flagit">
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:apply-templates select="exsl:node-set($flagrules)/prop[last()]" mode="end-flagit"/>
 </xsl:template>
 
 <xsl:template match="prop" mode="end-flagit">  
   <!-- DEPRECATED IN FAVOR OF FALLTHROUGH SUPPORT WITH NEW FLAGGING PREPROCESS. -->
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
   <!-- not necessary to add logic for @img. original ditaval does not support end flag. -->
   <xsl:otherwise/> <!-- that flag not active -->
  </xsl:choose>
  <xsl:apply-templates select="preceding-sibling::prop[1]" mode="end-flagit"/>
 </xsl:template>

 <xsl:template match="*" mode="ditamsg:cannot-flag-inline-element">
   <xsl:param name="attr-name"/>
   <xsl:call-template name="output-message">
     <xsl:with-param name="msgnum">042</xsl:with-param>
     <xsl:with-param name="msgsev">I</xsl:with-param>
     <xsl:with-param name="msgparams">%1=<xsl:value-of select="$attr-name"/></xsl:with-param>
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