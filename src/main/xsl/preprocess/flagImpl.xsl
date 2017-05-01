<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2007, 2012 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
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
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0"  
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  exclude-result-prefixes="xs ditamsg">

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

  <!-- Deprecated since 2.3 -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:variable name="FILTERDOC" select="document($FILTERFILEURL,/)" as="document-node()"/>

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

  <!-- Specialized attributes for analysis by flagging templates.
       Value is a sequence of attribute names.
  -->
  <xsl:variable name="propsExtensions" as="xs:string*">
    
    <xsl:variable name="propsAttrs" as="xs:string*">
      <xsl:analyze-string select="$GLOBAL-DOMAINS" regex="a\(props\s+(\w+)\.*?\)">
        <xsl:matching-substring>
          <xsl:sequence select="regex-group(1)"/>
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
<!--    <xsl:message> + [DEBUG] propsExtensions="<xsl:value-of select="$propsAttrs"/>"</xsl:message>-->
    <xsl:sequence select="$propsAttrs"/>
  </xsl:variable>
  

  <xsl:template match="/">
    <!-- Avoid all later checks by adding test here - if no filter file, copy full tree? -->
    <xsl:apply-templates/>    
  </xsl:template>

  <xsl:template match="*">
    <xsl:param name="flagrules" as="element()*">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="exists($flagrules/*)">
        <xsl:variable name="conflictexist" as="xs:boolean">
         <xsl:call-template name="conflict-check">
           <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
         </xsl:call-template>
        </xsl:variable>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <ditaval-startprop class="+ topic/foreign ditaot-d/ditaval-startprop ">
            <xsl:apply-templates select="." mode="gen-style">
              <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
              <xsl:with-param name="conflictexist" select="$conflictexist"/>
            </xsl:apply-templates>
            <xsl:if test="$conflictexist and $FILTERDOC/val/style-conflict">
              <xsl:sequence select="$FILTERDOC/val/style-conflict"/>
            </xsl:if>
            <xsl:apply-templates select="." mode="dita-start-flagit">
              <xsl:with-param name="flagrules"  as="element()*" select="$flagrules"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="." mode="dita-start-revflag">
              <xsl:with-param name="flagrules"  as="element()*" select="$flagrules"/>
            </xsl:apply-templates>
          </ditaval-startprop>
          <xsl:apply-templates select="node()"/>
          <ditaval-endprop class="+ topic/foreign ditaot-d/ditaval-endprop ">
            <xsl:apply-templates select="." mode="dita-end-revflag">
              <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="." mode="dita-end-flagit">
              <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
            </xsl:apply-templates>
          </ditaval-endprop>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy>
          <xsl:apply-templates select="@*,node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="@*|processing-instruction()|comment()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*" mode="dita-start-flagit">
   <xsl:param name="flagrules" as="element()*">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:apply-templates select="$flagrules/prop[1]" mode="start-flagit"/>
  </xsl:template>

  <xsl:template match="prop" mode="start-flagit">  
    <xsl:copy>
      <xsl:sequence select="@*"/>
      <xsl:if test="startflag">
        <startflag>
          <xsl:sequence select="startflag/@*"/>
          <xsl:apply-templates select="startflag/@imageref" mode="adjust-imageref"/>
          <xsl:sequence select="startflag/*"/>
        </startflag>
      </xsl:if>
    </xsl:copy>
   <xsl:apply-templates select="following-sibling::prop[1]" mode="start-flagit"/>
  </xsl:template>

  <xsl:template match="*" mode="dita-end-flagit">
   <xsl:param name="flagrules" as="element()*">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:apply-templates select="$flagrules/prop[last()]" mode="end-flagit"/>
  </xsl:template>

  <xsl:template match="prop" mode="end-flagit">  
    <xsl:copy>
      <xsl:sequence select="@*"/>
      <xsl:if test="endflag">
        <endflag>
          <xsl:sequence select="endflag/@*"/>
          <xsl:apply-templates select="endflag/@imageref" mode="adjust-imageref"/>
          <xsl:sequence select="endflag/*"/>
        </endflag>
      </xsl:if>
    </xsl:copy>
   <xsl:apply-templates select="preceding-sibling::prop[1]" mode="end-flagit"/>
  </xsl:template>


  <!-- Output starting flag only -->
  <xsl:template match="*" mode="dita-start-revflag">
   <xsl:param name="flagrules" as="element()*">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:if test="@rev and not($FILTERFILEURL='')">
    <xsl:call-template name="start-mark-rev">
     <xsl:with-param name="revvalue" select="@rev"/>
      <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
    </xsl:call-template>
   </xsl:if>
  </xsl:template>

  <!-- Output ending flag only -->
  <xsl:template match="*" mode="dita-end-revflag">
   <xsl:param name="flagrules" as="element()*">
     <xsl:call-template name="getrules"/>
   </xsl:param>
   <xsl:if test="@rev and not($FILTERFILEURL='')">
    <xsl:call-template name="end-mark-rev">
     <xsl:with-param name="revvalue" select="@rev"/>
      <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
    </xsl:call-template>
   </xsl:if>
  </xsl:template>

  <!-- This revision is active for this element -->
  <xsl:template match="revprop" mode="start-revflagit">
    <xsl:param name="lang"/>
    <xsl:param name="biditest"/>
   <xsl:copy>
     <xsl:sequence select="@*"/>
     <xsl:choose>
       <xsl:when test="startflag">
         <startflag>
           <xsl:sequence select="startflag/@*"/>
           <xsl:apply-templates select="startflag/@imageref" mode="adjust-imageref"/>
           <xsl:sequence select="startflag/*"/>
         </startflag>
       </xsl:when>
       <xsl:otherwise>
         <!-- Create default start revision reference? -->
         <xsl:call-template name="default-rev-start"/>         
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
      <xsl:sequence select="@*"/>
      <xsl:choose>
        <xsl:when test="endflag">
          <endflag>
            <xsl:sequence select="endflag/@*"/>
            <xsl:apply-templates select="endflag/@imageref" mode="adjust-imageref"/>
            <xsl:sequence select="endflag/*"/>
          </endflag>
        </xsl:when>
        <xsl:otherwise>
          <!-- Create default end revision reference? -->
          <xsl:call-template name="default-rev-end">
            <!--
            <xsl:with-param name="lang" select="$lang"/>
            <xsl:with-param name="biditest" select="$biditest"/>
            -->
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
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Start of change'"/>
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
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'End of change'"/>
        </xsl:call-template>
      </alt-text>
    </endflag>-->
  </xsl:template>

  <xsl:template match="@imageref" mode="adjust-imageref">
    <xsl:if test="string-length($PATH2PROJ) > 0 and
                  not(contains(.,'://'))">
      <xsl:attribute name="imageref" select="concat($PATH2PROJ,.)"/>
    </xsl:if>
  </xsl:template>


  <!-- ========== COPIED FROM XHTML CODE ========== -->
  <!-- Test for in BIDI area: returns "bidi" when parent's @xml:lang is a bidi language; otherwise, leave blank -->
  <xsl:template name="bidi-area" as="xs:string?">
   <xsl:param name="parentlang" as="xs:string">
    <xsl:call-template name="getLowerCaseLang"/>
   </xsl:param>
   <xsl:variable name="direction" as="xs:string">
     <xsl:apply-templates select="." mode="get-render-direction">
       <xsl:with-param name="lang" select="$parentlang"/>
     </xsl:apply-templates>
   </xsl:variable>
   <xsl:sequence select="if ($direction = 'rtl') then 'bidi' else ()"/>
  </xsl:template>
 
 <!-- Flags - based on audience, product, platform, and otherprops in the source
  AND prop elements in the val file:
  Flag the text with the artwork from the val file & insert the ALT text from the val file.
  For multiple attr values, output each flag in turn.
 -->

  <xsl:template name="getrules" as="element()*">
    <xsl:param name="current" select="." as="element()"/>
    
    <val>
      <xsl:if test="normalize-space($FILTERFILEURL)!=''">
        <!-- 
         First gather the set of property name/value pairs then remove duplicates
         and get <prop> specifications from the filter file. 
         
         For @rev the value is a sequence of zero or more revision names. @rev does not
         allow groups.
         
         For @props, @audience, @platform, @product, @otherprops, and specializations of @props, the value is zero or
         more value tokens or groups.
         
         Because of the groups it's necessary to first gather a mapping of condition names to values and then use
         that to find any corresponding flag specifications in the filter file. A group can have any name, so there's
         no requirement that there be any relationship between the attribute name and the group names used in the
         attribute value.
    
         Because we're still in XSLT 2 land we can't use XPath3 maps, which would be the ideal solution for this
         task, so generating an intermediate XML structure to represent the map of condition names to values.
         
         One challenge here is we need to reduce duplicates as it's possible for the same condition/value pair to
         be specified multiple times on a single element and you don't want multiple flag indicators in that case.
         -->
        <xsl:variable name="conditions" as="element()">
          <conditions>
            <xsl:apply-templates mode="getconditions"
              select="@audience, @platform, @product, @otherprops, @props, @rev, @*[name(.) = $propsExtensions]"
            />
          </conditions>
        </xsl:variable>        
        <xsl:message> + [DEBUG] getrules: conditions:
          <xsl:sequence select="$conditions"/></xsl:message>
        
        <!-- Now use the conditions to generate flagging specifications -->
        
        <!-- Because the same condition can occur multiple times, we need to
         group by condition and then get the distinct values for each
         condition to ensure we don't generate duplication flag items.
      -->
        
        <xsl:for-each-group select="$conditions/condition" group-by="@name">
          <xsl:variable name="conditionName" as="xs:string" select="current-grouping-key()"/>
          <xsl:variable name="conditionValues" as="xs:string*"
            select="distinct-values(for $e in ./value return string ($e))"
          />
          
          <!-- Now find any flagging actions for the name/value pair: -->
          <xsl:for-each select="$conditionValues">
            <xsl:variable name="value" select="." as="xs:string"/>
            <xsl:choose>
              <xsl:when test="$conditionName = ('rev')">
                <xsl:apply-templates mode="gen-prop"
                  select="($FILTERDOC/val/revprop[@action = ('flag')][@val = $value])[1]"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates mode="gen-prop"
                  select="($FILTERDOC/val/prop[@action = ('flag')][@att = $conditionName][@val = $value])[1]"/>
              </xsl:otherwise>
            </xsl:choose>
            
          </xsl:for-each>
          
          <!-- Now get any default specification for the condition: -->
          <xsl:sequence select="$FILTERDOC/val/prop[@action = ('flag')][@att = $conditionName][not(@val)]"/>
        </xsl:for-each-group>    
      </xsl:if>
    </val>
  </xsl:template>
  
  <!-- Get the conditions specified in an selection attribute.
    
       The result contributes to the <conditions> element that
       is then used to get the flagging <prop> elements.
    -->
  <xsl:template mode="getconditions" match="@*">
    <!-- Handle any bare values: -->
    <xsl:variable name="bareValues" as="xs:string*">
      <xsl:analyze-string select="." regex="[\w\.-_]+\(.*?\)">
        <xsl:non-matching-substring>
          <xsl:if test=" not(matches(., '^\s*$'))">
            <xsl:sequence select="tokenize(normalize-space(.), ' ')"/>
          </xsl:if>
        </xsl:non-matching-substring>
      </xsl:analyze-string>
    </xsl:variable>
<!--    <xsl:message> + [DEBUG] getconditions: @<xsl:value-of select="name(.)"/>: "<xsl:value-of select="."/>"</xsl:message>-->
    <xsl:if test="count($bareValues) gt 0">
      <condition name="{name(.)}">
        <xsl:for-each select="distinct-values($bareValues)">
          <value><xsl:sequence select="."/></value>
        </xsl:for-each>
      </condition>
    </xsl:if>
    
    <!-- Making this check so we don't accidently support
         groups in @rev, which is not allowed but probably
         isn't validated by any tools, or at least we
         can't depend on it.
      -->
    <xsl:if test="not(name(.) = ('rev'))">
      <!-- Now handle any groups -->
      <xsl:analyze-string select="." regex="([\w\.-_]+)\((.*?)\)">
        <xsl:matching-substring>
          <condition name="{regex-group(1)}">
            <xsl:for-each select="distinct-values(tokenize(regex-group(2), ' '))">
              <value><xsl:sequence select="."/></value>
            </xsl:for-each>
          </condition>
        </xsl:matching-substring>
      </xsl:analyze-string>
    </xsl:if>
    
  </xsl:template>
  
  <xsl:template name="gen-prop" as="element()*">
    <xsl:param name="flag-att" as="xs:string"/>     <!-- attribute name -->
    <xsl:param name="flag-att-val" as="xs:string?"/> <!-- content of attribute -->
    
  <!-- Determine the first flag value, which is the value before the first space -->
   <xsl:variable name="flagTokens" as="xs:string+" 
     select="tokenize($flag-att-val, ' ')"
   />
  <xsl:variable name="firstflag" as="xs:string" 
    select="$flagTokens[1]"
  />
  
  
  <xsl:choose> <!-- Ensure there's an image to get, otherwise don't insert anything -->
   <xsl:when test="$flag-att='rev' and $FILTERDOC/val/revprop[@val=$firstflag][1][@action='flag']">
    <xsl:sequence select="$FILTERDOC/val/revprop[@val=$firstflag][1][@action='flag']"/>
   </xsl:when>
   <xsl:when test="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][1][@action='flag']">
    <xsl:sequence select="$FILTERDOC/val/prop[@att=$flag-att][@val=$firstflag][1][@action='flag']"/>
   </xsl:when>
   <xsl:when test="$FILTERDOC/val/prop[@att=$flag-att][not(@val=$firstflag)][@action='flag']">
    
    <!-- FIXME: WEK: This should be done with apply-templates. -->
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
   <xsl:for-each select="$flagTokens[position() > 1]">
     <xsl:call-template name="gen-prop">
       <xsl:with-param name="flag-att" select="$flag-att"/>
       <xsl:with-param name="flag-att-val" select="."/>
     </xsl:call-template>
   </xsl:for-each>
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
          <xsl:variable name="cvffilepath" select="concat($WORKDIR,$PATH2PROJ,$submfile)" as="xs:string"/>
     <xsl:if test="document($cvffilepath,/)//*[@keys=$value]//*[@keys=$flag]">
         <!-- copy the child node for flag and just copy the first element whose keys=$flag-->
      <!--xsl:for-each select="document($cvffilepath,/)//*[@keys=$value]/*"-->
      <xsl:for-each select="document($cvffilepath,/)//*[@keys=$value]//*[@keys=$flag][1]">
            <xsl:element name="prop">
             <xsl:attribute name="att" select="$att"/>
             <xsl:attribute name="val" select="@keys"/>
             <xsl:attribute name="action" select="'flag'"/>
             <xsl:attribute name="backcolor" select="$bgcolor"/>
             <xsl:attribute name="color" select="$fcolor"/>
             <xsl:attribute name="style" select="$style"/>
             <xsl:sequence select="$childnodes"/>
            </xsl:element>
           </xsl:for-each>
     </xsl:if>
  </xsl:if>
 </xsl:template>
 <!-- check CURRENT File -->
 <xsl:template name="checkFile" as="xs:string">
    <xsl:param name="in" as="xs:string"/>
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
 <xsl:template match="*" mode="check" as="xs:string">
  <xsl:value-of select="."/>
 </xsl:template>
 <xsl:template match="*" mode="getVal" as="xs:string">
     <xsl:value-of select="@val"/>
 </xsl:template>
 <!-- get background color -->
 <xsl:template match="*" mode="getBgcolor" as="xs:string">
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
 <xsl:template match="*" mode="getColor" as="xs:string">
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
 <xsl:template match="*" mode="getStyle" as="xs:string">
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
        <xsl:sequence select="node()"/>
  </xsl:template>


<!-- There's a rev attr - test for active rev values -->
<xsl:template name="start-mark-rev">
  <xsl:param name="flagrules" as="element()*">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:param name="revvalue"  as="xs:string"/>
  <xsl:variable name="revtest" as="xs:integer">
    <xsl:call-template name="find-active-rev-flag">
      <xsl:with-param name="allrevs" as="xs:string" select="$revvalue"/>
    </xsl:call-template>
  </xsl:variable>
  <xsl:if test="$revtest=1">
    <xsl:call-template name="start-revision-flag">
    <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/> 
    </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- There's a rev attr - test for active rev values -->
<xsl:template name="end-mark-rev">
 <xsl:param name="flagrules" as="element()*">
   <xsl:call-template name="getrules"/>
 </xsl:param>
 <xsl:param name="revvalue"/>
 <xsl:variable name="revtest" as="xs:integer">
  <xsl:call-template name="find-active-rev-flag">
   <xsl:with-param name="allrevs" select="$revvalue"/>
  </xsl:call-template>
 </xsl:variable>
  <xsl:if test="$revtest=1">
   <xsl:call-template name="end-revision-flag">
     <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/> 
   </xsl:call-template>
  </xsl:if>
</xsl:template>

<!-- output the beginning revision graphic & ALT text -->
<!-- Reverse the artwork for BIDI languages -->
<xsl:template name="start-revision-flag">
 <xsl:param name="flagrules" as="element()*">
   <xsl:call-template name="getrules"/>
 </xsl:param>
  <xsl:call-template name="start-revflagit">
    <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
  </xsl:call-template>
</xsl:template>

 <xsl:template name="start-revflagit">
  <xsl:param name="flagrules" as="element()*">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:param name="lang" as="xs:string">
    <xsl:call-template name="getLowerCaseLang"/>
  </xsl:param>
  <xsl:param name="biditest" as="xs:string">
    <xsl:call-template name="bidi-area">
      <xsl:with-param name="parentlang" select="$lang"/>
    </xsl:call-template>
  </xsl:param>
  <xsl:if test="$flagrules/revprop[1]">
    <xsl:apply-templates select="$flagrules/revprop[1]" mode="start-revflagit">
      <xsl:with-param name="lang" select="$lang"/>
      <xsl:with-param name="biditest" select="$biditest"/>
    </xsl:apply-templates>
  </xsl:if>
 </xsl:template>
 
 <xsl:template name="end-revflagit">
  <xsl:param name="flagrules" as="element()*">
    <xsl:call-template name="getrules"/>
  </xsl:param>
  <xsl:param name="lang" as="xs:string">
    <xsl:call-template name="getLowerCaseLang"/>
  </xsl:param>
  <xsl:param name="biditest" as="xs:string">
    <xsl:call-template name="bidi-area">
      <xsl:with-param name="parentlang" select="$lang"/>
    </xsl:call-template>
  </xsl:param>
  <xsl:if test="$flagrules/revprop[1]">
    <xsl:apply-templates select="$flagrules/revprop[last()]" mode="end-revflagit">
      <xsl:with-param name="lang" select="$lang"/>
      <xsl:with-param name="biditest" select="$biditest"/>
    </xsl:apply-templates>
  </xsl:if>
 </xsl:template>
 
<!-- output the ending revision graphic & ALT text -->
<!-- Reverse the artwork for BIDI languages -->
<xsl:template name="end-revision-flag">
 <xsl:param name="flagrules" as="element()*">
   <xsl:call-template name="getrules"/>
 </xsl:param>
  <xsl:call-template name="end-revflagit">
    <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
  </xsl:call-template>
</xsl:template>

<!-- Use @rev to find the first active flagged revision.
     Return 1 for active.
     Return 0 for non-active. 
     NOTE: this template is only called when a filter file is available and
     when there is a revision to evaluate. -->
<xsl:template name="find-active-rev-flag" as="xs:integer">
  <xsl:param name="allrevs" select="@rev" as="xs:string"/>

  <!-- Determine the first rev value, which is the value before the first space -->
  <xsl:variable name="firstrev" as="xs:string">
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
  <xsl:variable name="morerevs" as="xs:string">
   <xsl:choose>
    <xsl:when test="contains($allrevs,' ')">
     <xsl:value-of select="substring-after($allrevs,' ')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="''"/>
    </xsl:otherwise> <!-- no space, one value -->
   </xsl:choose>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$FILTERDOC/val/revprop[@val=$firstrev][@action='flag']">
     <xsl:value-of select="1"/> <!-- rev active -->
   </xsl:when>
   <xsl:when test="string-length($morerevs) > 0">
    <!-- more values - call it again with remaining values -->
    <xsl:call-template name="find-active-rev-flag">
     <xsl:with-param name="allrevs" select="$morerevs"/>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise> <!-- no more values - none found -->
    <xsl:value-of select="0"/>
   </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Use @rev to find the first active styled revision.
     Return color setting when active.
     Return null for non-active. -->
<xsl:template name="find-active-rev-style">
  <xsl:param name="allrevs" as="xs:string"/>

  <!-- Determine the first rev value, which is the value before the first space -->
  <xsl:variable name="firstrev" as="xs:string">
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
  <xsl:variable name="morerevs" as="xs:string">
   <xsl:choose>
    <xsl:when test="contains($allrevs,' ')">
     <xsl:value-of select="substring-after($allrevs,' ')"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:value-of select="''"/>
    </xsl:otherwise> <!-- no space, one value -->
   </xsl:choose>
  </xsl:variable>

  <xsl:choose>
   <xsl:when test="$FILTERDOC/val/revprop[@val=$firstrev]/@style">
     <!-- rev active -->
     <xsl:value-of select="$FILTERDOC/val/revprop[@val=$firstrev]/@style"/>
   </xsl:when>
   <xsl:when test="string-length($morerevs)>0">
    <!-- more values - call it again with remaining values -->
    <xsl:call-template name="find-active-rev-style">
     <xsl:with-param name="allrevs" select="$morerevs"/>
    </xsl:call-template>
   </xsl:when>
   <xsl:otherwise/> <!-- no more values - none found -->
  </xsl:choose>

</xsl:template>

 <xsl:template name="conflict-check" as="xs:boolean">
    <xsl:param name="flagrules" as="element()*">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="normalize-space($FILTERFILEURL)=''">
        <xsl:sequence select="false()"/>
      </xsl:when>
      <xsl:when test="$flagrules/*">
        <xsl:apply-templates select="$flagrules/*[1]" mode="conflict-check"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="false()"/>
      </xsl:otherwise>
    </xsl:choose>  
  </xsl:template>
 
 <xsl:template match="prop|revprop" mode="conflict-check" as="xs:boolean">
  <xsl:param name="color"/>
  <xsl:param name="backcolor"/>
  
  <xsl:choose>   
   <xsl:when test="(@color and @color!='' and $color!='' and $color!=@color)or(@backcolor and @backcolor!='' and $backcolor!='' and $backcolor!=@backcolor)">
    <xsl:sequence select="true()"/>
   </xsl:when>
   <xsl:when test="following-sibling::*">
    <xsl:apply-templates select="following-sibling::*[1]" mode="conflict-check">
     <xsl:with-param name="color" select="@color"/>
     <xsl:with-param name="backcolor" select="@backcolor"/>
    </xsl:apply-templates>
   </xsl:when>
   <xsl:otherwise>
    <xsl:sequence select="false()"/>
   </xsl:otherwise>
  </xsl:choose>
 </xsl:template>

 <!-- In earlier versions of the DITA-OT, conflictexist was always passed in as
      a parameter. Seems it would be better to make it a variable and move into
      the "if filterfile" section. Leaving alone now in case of any legacy overrides,
      and only trivial improvement from moving.  -->
  <xsl:template match="*" mode="gen-style" as="attribute(outputclass)?">
    <xsl:param name="flagrules" as="element()*">
      <xsl:call-template name="getrules"/>
    </xsl:param>
    <xsl:param name="conflictexist" as="xs:boolean">
     <xsl:call-template name="conflict-check">
       <xsl:with-param name="flagrules" as="element()*" select="$flagrules"/>
      </xsl:call-template>
    </xsl:param>

    <!-- Skip all further checking if there is no filter file -->
    <xsl:if test="normalize-space($FILTERFILEURL)!=''">
      <xsl:variable name="validstyle" as="xs:boolean">
        <!-- This variable is used to prevent using pre-OASIS or unrecognized ditaval styles -->
        <xsl:choose>
          <xsl:when test="not($conflictexist) and $flagrules/*[@style]">
            <xsl:choose>
              <xsl:when test="$flagrules/*/@style='italics'">true</xsl:when>
              <xsl:when test="$flagrules/*/@style='bold'">true</xsl:when>
              <xsl:when test="$flagrules/*/@style='underline'">true</xsl:when>
              <xsl:when test="$flagrules/*/@style='double-underline'">true</xsl:when>
              <xsl:when test="$flagrules/*/@style='overline'">true</xsl:when>
              <xsl:otherwise>false</xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <xsl:otherwise>false</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:choose>  
        <xsl:when test="$conflictexist and $FILTERDOC/val/style-conflict[@foreground-conflict-color or @background-conflict-color]">
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
        <xsl:when test="($flagrules/*[@color or @backcolor] or $validstyle)"><!--not($conflictexist) and -->
          <xsl:attribute name="outputclass">     
            <xsl:if test="$flagrules/*[@color]">
              <xsl:text>color:</xsl:text>
              <xsl:value-of select="($flagrules/*[@color])[1]/@color"/>
              <xsl:text>;</xsl:text>
            </xsl:if>
            <xsl:if test="$flagrules/*[@backcolor]">
              <xsl:text>background-color:</xsl:text>
              <xsl:value-of select="($flagrules/*[@backcolor])[1]/@backcolor"/>
              <xsl:text>;</xsl:text>
            </xsl:if>     
            <xsl:if test="$flagrules/*/@style='italics'">
              <xsl:text>font-style:italic;</xsl:text>
            </xsl:if>     
            <xsl:if test="$flagrules/*/@style='bold'">
              <xsl:text>font-weight:bold;</xsl:text>
            </xsl:if>     
            <xsl:if test="$flagrules/*/@style='underline' or 
                          $flagrules/*/@style='double-underline'">
              <!-- For double-underline, style="border-bottom: 3px double;" seems to work
                   in some cases, but not in all. For now, treat it as underline. -->
              <xsl:text>text-decoration:underline;</xsl:text>
            </xsl:if>     
            <xsl:if test="$flagrules/*/@style='overline'">
              <xsl:text>text-decoration:overline;</xsl:text>
            </xsl:if>     
          </xsl:attribute>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
 
 <xsl:template match="*" mode="ditamsg:cannot-flag-inline-element">
   <xsl:param name="attr-name"/>
   <xsl:call-template name="output-message">
     <xsl:with-param name="id" select="'DOTX042I'"/>
     <xsl:with-param name="msgparams">%1=<xsl:value-of select="$attr-name"/></xsl:with-param>
   </xsl:call-template>
 </xsl:template>
 <xsl:template match="*" mode="ditamsg:conflict-text-style-applied">
   <xsl:call-template name="output-message">
    <xsl:with-param name="id" select="'DOTX054W'"/>
   </xsl:call-template>
 </xsl:template>

<!-- ===================================================================== -->
</xsl:stylesheet>
