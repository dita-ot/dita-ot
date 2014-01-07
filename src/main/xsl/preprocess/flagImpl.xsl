<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
 See the accompanying license.txt file for applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2007, 2012 -->
<!-- PURPOSE: Replace the XHTML based flagging routines with a common routine.
     Logic for determining what to flag is the same.
     When flags are active:
     * Create element <ditaval-startprop> as the first child, and <ditaval-endprop> as the last.
       These are each pseudo-specializations of <foreign>, with class values
       "+ topic/foreign ditaot-d/ditaval-startprop "
       and
       "+ topic/foreign ditaot-d/ditaval-endprop "
     * Properties / revisions that are set to "flag" in the ditaval are copied as-is into each
       element, so that rendering steps can directly access all active flags and revisions.
     * Relative paths for flagging images are adjusted to be valid from the current topic
     * If styling is active, only one style can be set per element; the CSS style is calculated
       in this step and placed on @outputclass of <ditaval-startprop>. 
     * If there is a style conflict, and the file has <style-conflict>, then a copy of
       <style-conflict> is included in <ditaval-startprop>.
LOOK FOR FIXME TO FIX SCHEMEDEF STUFF
              -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="1.0" 
  xmlns:exsl="http://exslt.org/common" 
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  exclude-result-prefixes="exsl ditamsg">

 <!-- ========== Flagging with flags & revisions ========== -->

  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>

  <xsl:output method="xml"
              encoding="utf-8"
              indent="no"
              byte-order-mark="no"
  />

  <xsl:param name="DBG" select="'no'"/>
  <xsl:param name="DRAFT" select="'no'"/>
  <xsl:param name="DITAEXT" select="'.dita'"/>
  <xsl:param name="FILTERFILEURL"/>
  <xsl:param name="PATH2PROJ">
      <xsl:apply-templates select="/processing-instruction('path2project-uri')[1]" mode="get-path2project"/>
  </xsl:param>
  <xsl:param name="WORKDIR">
    <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
  </xsl:param>
  <xsl:param name="FILENAME"/>
  <xsl:param name="FILEDIR"/>
  <xsl:param name="CURRENTFILE" select="concat($FILEDIR, '/', $FILENAME)"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:variable name="FILTERDOC" select="document($FILTERFILEURL,/)"/>

  <xsl:variable name="GLOBAL-DOMAINS">
    <xsl:choose>
      <xsl:when test="/dita">
        <xsl:value-of select="normalize-space(/dita/*[1]/@domains)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="normalize-space(/*[1]/@domains)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:variable name="allPropsAndRev">
    <!-- First include defaults we always check for (not specialized) -->
    <xsl:text>,audience,platform,product,otherprops,rev</xsl:text>
    <xsl:call-template name="getExtProps">
      <xsl:with-param name="domains" select="$GLOBAL-DOMAINS"/>
    </xsl:call-template>
  </xsl:variable>

  <xsl:variable name="collectPropsExtensions">
    <xsl:call-template name="getExtProps">
      <xsl:with-param name="domains" select="$GLOBAL-DOMAINS"/>
    </xsl:call-template>
  </xsl:variable>
  <!-- Specialized attributes for analysis by flagging templates. Format is:
       props attr1,props attr2,props attr3 -->
  <xsl:variable name="propsExtensions">
    <xsl:value-of select="substring-after($collectPropsExtensions, ',')"/>
  </xsl:variable>


  <xsl:template match="/">
    <!-- Avoid all later checks by adding test here - if no filter file, copy full tree? -->
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*">
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="exsl:node-set($flagrules)/*">
        <xsl:variable name="conflictexist">
         <xsl:call-template name="conflict-check">
          <xsl:with-param name="flagrules" select="$flagrules"/>
         </xsl:call-template>
        </xsl:variable>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <ditaval-startprop class="+ topic/foreign ditaot-d/ditaval-startprop ">
            <xsl:apply-templates select="." mode="gen-style">
              <xsl:with-param name="flagrules" select="$flagrules"/>
              <xsl:with-param name="conflictexist" select="$conflictexist"/>
            </xsl:apply-templates>
            <xsl:if test="$conflictexist='true' and $FILTERDOC/val/style-conflict">
              <xsl:copy-of select="$FILTERDOC/val/style-conflict"/>
            </xsl:if>
            <xsl:apply-templates select="." mode="dita-start-flagit">
              <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="." mode="dita-start-revflag">
              <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:apply-templates>
          </ditaval-startprop>
          <xsl:apply-templates select="*|processing-instruction()|comment()|text()"/>
          <ditaval-endprop class="+ topic/foreign ditaot-d/ditaval-endprop ">
            <xsl:apply-templates select="." mode="dita-end-revflag">
              <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="." mode="dita-end-flagit">
              <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:apply-templates>
          </ditaval-endprop>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*|*|processing-instruction()|comment()|text()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|processing-instruction()|comment()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates select="*|processing-instruction()|comment()|text()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*" mode="dita-start-flagit">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:apply-templates select="exsl:node-set($flagrules)/prop[1]" mode="start-flagit"/>
  </xsl:template>

  <xsl:template match="prop" mode="start-flagit">  
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="startflag">
        <startflag>
          <xsl:copy-of select="startflag/@*"/>
          <xsl:apply-templates select="startflag/@imageref" mode="adjust-imageref"/>
          <xsl:copy-of select="startflag/*"/>
        </startflag>
      </xsl:if>
    </xsl:copy>
   <xsl:apply-templates select="following-sibling::prop[1]" mode="start-flagit"/>
  </xsl:template>

  <xsl:template match="*" mode="dita-end-flagit">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:apply-templates select="exsl:node-set($flagrules)/prop[last()]" mode="end-flagit"/>
  </xsl:template>

  <xsl:template match="prop" mode="end-flagit">  
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="endflag">
        <endflag>
          <xsl:copy-of select="endflag/@*"/>
          <xsl:apply-templates select="endflag/@imageref" mode="adjust-imageref"/>
          <xsl:copy-of select="endflag/*"/>
        </endflag>
      </xsl:if>
    </xsl:copy>
   <xsl:apply-templates select="preceding-sibling::prop[1]" mode="end-flagit"/>
  </xsl:template>


  <!-- Output starting flag only -->
  <xsl:template match="*" mode="dita-start-revflag">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:if test="@rev and not($FILTERFILEURL='')">
    <xsl:call-template name="start-mark-rev">
     <xsl:with-param name="revvalue" select="@rev"/>
     <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
   </xsl:if>
  </xsl:template>

  <!-- Output ending flag only -->
  <xsl:template match="*" mode="dita-end-revflag">
   <xsl:param name="flagrules">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:if test="@rev and not($FILTERFILEURL='')">
    <xsl:call-template name="end-mark-rev">
     <xsl:with-param name="revvalue" select="@rev"/>
     <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
   </xsl:if>
  </xsl:template>

  <!-- This revision is active for this element -->
  <xsl:template match="revprop" mode="start-revflagit">
    <xsl:param name="lang"/>
    <xsl:param name="biditest"/>
   <xsl:copy>
     <xsl:copy-of select="@*"/>
     <xsl:choose>
       <xsl:when test="startflag">
         <startflag>
           <xsl:copy-of select="startflag/@*"/>
           <xsl:apply-templates select="startflag/@imageref" mode="adjust-imageref"/>
           <xsl:copy-of select="startflag/*"/>
         </startflag>
       </xsl:when>
       <xsl:otherwise>
         <!-- Create default start revision reference? -->
         <xsl:call-template name="default-rev-start">
           <xsl:with-param name="lang" select="$lang"/>
           <xsl:with-param name="biditest" select="$biditest"/>
         </xsl:call-template>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:copy>
   <xsl:apply-templates select="following-sibling::revprop[1]" mode="start-revflagit">
     <xsl:with-param name="lang" select="$lang"/>
     <xsl:with-param name="biditest" select="$biditest"/>
   </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="revprop" mode="end-revflagit">
    <xsl:param name="lang"/>
    <xsl:param name="biditest"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
        <xsl:when test="endflag">
          <endflag>
            <xsl:copy-of select="endflag/@*"/>
            <xsl:apply-templates select="endflag/@imageref" mode="adjust-imageref"/>
            <xsl:copy-of select="endflag/*"/>
          </endflag>
        </xsl:when>
        <xsl:otherwise>
          <!-- Create default end revision reference? -->
          <xsl:call-template name="default-rev-end">
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="biditest" select="$biditest"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
   <xsl:apply-templates select="preceding-sibling::revprop[1]" mode="end-revflagit">
     <xsl:with-param name="lang" select="$lang"/>
     <xsl:with-param name="biditest" select="$biditest"/>
   </xsl:apply-templates>
  </xsl:template>

  <!-- output the DEFAULT beginning revision graphic & ALT text.
       Commented out section shows how to insert a default start flag of "delta.gif"
       by creating the proper ditaval syntax -->
  <xsl:template name="default-rev-start">
    <xsl:param name="lang"/>
    <xsl:param name="biditest"/>
    <!--
    <xsl:param name="startRevImage">
      <xsl:choose>
        <xsl:when test="$biditest='bidi'">deltaend.gif</xsl:when>
        <xsl:otherwise>delta.gif</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <startflag imageref="{$PATH2PROJ}{$startRevImage}">
      <alt-text>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'Start of change'"/>
        </xsl:call-template>
      </alt-text>
    </startflag>-->
  </xsl:template>
  <!-- output the DEFAULT ending revision graphic & ALT text -->
  <xsl:template name="default-rev-end">
    <xsl:param name="lang"/>
    <xsl:param name="biditest"/>
    <!--
    <xsl:param name="endRevImage">
      <xsl:choose>
        <xsl:when test="$biditest='bidi'">delta.gif</xsl:when>
        <xsl:otherwise>deltaend.gif</xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <endflag imageref="{$PATH2PROJ}{$endRevImage}">
      <alt-text>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'End of change'"/>
        </xsl:call-template>
      </alt-text>
    </endflag>-->
  </xsl:template>

  <xsl:template match="@imageref" mode="adjust-imageref">
    <xsl:if test="string-length($PATH2PROJ) > 0 and
                  not(contains(.,'://'))">
      <xsl:attribute name="imageref">
        <xsl:value-of select="$PATH2PROJ"/>
        <xsl:value-of select="."/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>


  <!-- ========== COPIED FROM XHTML CODE ========== -->
  <!-- Test for in BIDI area: returns "bidi" when parent's @xml:lang is a bidi language; otherwise, leave blank -->
  <xsl:template name="bidi-area">
   <xsl:param name="parentlang">
    <xsl:call-template name="getLowerCaseLang"/>
   </xsl:param>
   <xsl:variable name="direction">
     <xsl:apply-templates select="." mode="get-render-direction">
       <xsl:with-param name="lang" select="$parentlang"/>
     </xsl:apply-templates>
   </xsl:variable>
   <xsl:choose>
    <xsl:when test="$direction='rtl'">bidi</xsl:when>
    <xsl:otherwise/>
   </xsl:choose>
  </xsl:template>







 
 <!-- Flags - based on audience, product, platform, and otherprops in the source
  AND prop elements in the val file:
  Flag the text with the artwork from the val file & insert the ALT text from the val file.
  For multiple attr values, output each flag in turn.
 -->

<xsl:template name="getrules">
 <!-- Test for the flagging attributes. If found, call 'gen-prop' with the values to use. Otherwise return -->
  <xsl:if test="normalize-space($FILTERFILEURL)!=''">
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
 <!-- Test for the flagging attributes on the parent.
   If found and if the filterfile name was passed in,
      call 'gen-prop' with the values to use. Otherwise return -->
  <xsl:if test="normalize-space($FILTERFILEURL)!=''">
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
   <xsl:when test="$flag-att='rev' and $FILTERDOC/val/revprop[@val=$firstflag][1][@action='flag']">
    <xsl:copy-of select="$FILTERDOC/val/revprop[@val=$firstflag][1][@action='flag']"/>
   </xsl:when>
   <xsl:when test="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][1][@action='flag']">
    <xsl:copy-of select="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][1][@action='flag']"/>
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
     <xsl:with-param name="flag-att" select="$flag-att"/>
     <xsl:with-param name="flag-att-val" select="$moreflags"/>
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
 <xsl:if test="@rev and not($FILTERFILEURL='')">
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
 <xsl:if test="@rev and not($FILTERFILEURL='')">
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
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILEURL='')">
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
 <xsl:if test="../@rev and not(@rev) and not($FILTERFILEURL='')">
  <xsl:call-template name="end-mark-rev">
   <xsl:with-param name="revvalue" select="../@rev"/>
   <xsl:with-param name="flagrules" select="$flagrules"/> 
  </xsl:call-template>
 </xsl:if>
</xsl:template>

<!-- Output starting & ending flag for "blocked" text.
     Use instead of 'apply-templates' for block areas (P, Note, DD, etc) -->
<xsl:template name="revblock">
 <xsl:param name="flagrules"><xsl:call-template name="getrules"/></xsl:param>
 <xsl:variable name="revtest"><xsl:apply-templates select="." mode="mark-revisions-for-draft"/></xsl:variable>
 <xsl:choose>
   <xsl:when test="$revtest=1"> <!-- rev mode with draft -->
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
   <xsl:when test="@rev and not($FILTERFILEURL='')">    <!-- normal rev mode -->
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
   <xsl:otherwise>
     <xsl:apply-templates/>
   </xsl:otherwise>
 </xsl:choose>
</xsl:template>

<!-- Output starting & ending flag & color for phrase text.
     Use instead of 'apply-templates' for phrase areas (PH, B, DT, etc) -->
<xsl:template name="revtext">
 <xsl:param name="flagrules"><xsl:call-template name="getrules"/></xsl:param>
 <xsl:variable name="revtest"><xsl:apply-templates select="." mode="mark-revisions-for-draft"/></xsl:variable>

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
  <xsl:when test="@rev and not($FILTERFILEURL='')">         <!-- normal rev mode -->
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
  <xsl:when test="exsl:node-set($flagrules)/revprop[@color or @backcolor]">
   <font>
    <xsl:apply-templates select="." mode="gen-style">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:apply-templates>
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
 <!--<xsl:variable name="biditest"> 
  <xsl:call-template name="bidi-area"/>
 </xsl:variable>
 <xsl:choose>
  <xsl:when test="$biditest='bidi'">
    <xsl:call-template name="end-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>-->
    <xsl:call-template name="start-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  <!--</xsl:otherwise>
 </xsl:choose>-->
</xsl:template>

 <xsl:template name="start-revflagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:param name="lang">
    <xsl:call-template name="getLowerCaseLang"/>
  </xsl:param>
  <xsl:param name="biditest">
    <xsl:call-template name="bidi-area">
      <xsl:with-param name="parentlang" select="$lang"/>
    </xsl:call-template>
  </xsl:param>
  <xsl:if test="exsl:node-set($flagrules)/revprop[1]">
    <xsl:apply-templates select="exsl:node-set($flagrules)/revprop[1]" mode="start-revflagit">
      <xsl:with-param name="lang" select="$lang"/>
      <xsl:with-param name="biditest" select="$biditest"/>
    </xsl:apply-templates>
  </xsl:if>
 </xsl:template>
 
 <xsl:template name="end-revflagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:param name="lang">
    <xsl:call-template name="getLowerCaseLang"/>
  </xsl:param>
  <xsl:param name="biditest">
    <xsl:call-template name="bidi-area">
      <xsl:with-param name="parentlang" select="$lang"/>
    </xsl:call-template>
  </xsl:param>
  <xsl:if test="exsl:node-set($flagrules)/revprop[1]">
    <xsl:apply-templates select="exsl:node-set($flagrules)/revprop[last()]" mode="end-revflagit">
      <xsl:with-param name="lang" select="$lang"/>
      <xsl:with-param name="biditest" select="$biditest"/>
    </xsl:apply-templates>
  </xsl:if>
 </xsl:template>
 
<!-- output the ending revision graphic & ALT text -->
<!-- Reverse the artwork for BIDI languages -->
<xsl:template name="end-revision-flag">
 <xsl:param name="flagrules">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <!--<xsl:variable name="biditest">
  <xsl:call-template name="bidi-area"/>
 </xsl:variable>
 <xsl:choose>
  <xsl:when test="$biditest='bidi'">
    <xsl:call-template name="start-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
     </xsl:call-template>
  </xsl:when>
  <xsl:otherwise>-->
    <xsl:call-template name="end-revflagit">
      <xsl:with-param name="flagrules" select="$flagrules"/>
    </xsl:call-template>
  <!--</xsl:otherwise>
 </xsl:choose>-->
</xsl:template>

<!-- Shortcut for old multi-line calls to find-active-rev-flag.
     Return 1 for active revision when draft is on, return 0 otherwise. -->
<xsl:template match="*" mode="mark-revisions-for-draft">
  <xsl:choose>
    <xsl:when test="@rev and not($FILTERFILEURL='') and ($DRAFT='yes')">
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
       <xsl:with-param name="allrevs" select="$morerevs"/>
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
       <xsl:with-param name="allrevs" select="$morerevs"/>
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
      <xsl:when test="normalize-space($FILTERFILEURL)=''">
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

 <!-- In earlier versions of the DITA-OT, conflictexist was always passed in as
      a parameter. Seems it would be better to make it a variable and move into
      the "if filterfile" section. Leaving alone now in case of any legacy overrides,
      and only trivial improvement from moving.  -->
  <xsl:template match="*" mode="gen-style">
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:param name="conflictexist">
     <xsl:call-template name="conflict-check">
        <xsl:with-param name="flagrules" select="$flagrules"/>
      </xsl:call-template>
    </xsl:param>

    <!-- Skip all further checking if there is no filter file -->
    <xsl:if test="normalize-space($FILTERFILEURL)!=''">
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
          <xsl:attribute name="outputclass">     
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
          <xsl:attribute name="outputclass">     
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
    </xsl:if>
  </xsl:template>
 
  <xsl:template name="start-flagit">
    <xsl:param name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:apply-templates select="exsl:node-set($flagrules)/prop[1]" mode="start-flagit"/>
  </xsl:template>
 
 <xsl:template name="end-flagit">
  <xsl:param name="flagrules">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:apply-templates select="exsl:node-set($flagrules)/prop[last()]" mode="end-flagit"/>
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