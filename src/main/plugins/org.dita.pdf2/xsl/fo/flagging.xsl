<?xml version='1.0' encoding="UTF-8"?>

<!--
Copyright © 2009 by Suite Solutions, Ltd. All rights reserved.
All other trademarks are the property of their respective owners.

Suite Solutions, Ltd. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF Suite Solutions, Ltd.
HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Suite Solutions, Ltd. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Suite Solutions, Ltd.'s
liability for any damages hereunder exceed the amounts received by Suite Solutions, Ltd, Inc. 
as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net. 
See the accompanying license.txt file for applicable licenses.
-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
    xmlns:suitesol="http://suite-sol.com/namespaces/mapcounts"
    exclude-result-prefixes="suitesol opentopic-func dita2xslfo">
   
  <xsl:template match="suitesol:flagging-inside">
      <xsl:call-template name="parseFlagStyle">
         <xsl:with-param name="value">
            <xsl:value-of select="@style"/>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>
   
   <xsl:template match="suitesol:changebar-start">
      
      <fo:change-bar-begin>
         <xsl:attribute name="change-bar-class">
            <xsl:text>dv</xsl:text>
            <xsl:value-of select="@id" />
         </xsl:attribute>

         <!--             
            change-bar-color 
      change-bar-offset
      change-bar-placement= start | end | left | right | inside | outside | alternate 
      change-bar-style = none | hidden | dotted | dashed | solid | double | groove | ridge | inset | outset
      change-bar-width
      -->

         <xsl:call-template name="parseChangeBarStyle">
            <xsl:with-param name="value">
               <xsl:value-of select="@changebar"/>
            </xsl:with-param>
         </xsl:call-template>

      </fo:change-bar-begin>

   </xsl:template>
   
   <xsl:template name="parseChangeBarStyle">
      <xsl:param name="value"/>

      <xsl:choose>
         <xsl:when test="$value = ''"/>
         <xsl:when test="contains($value,';')">
            <xsl:variable name="firstValue" select="substring-before($value,';')"/>
            <xsl:call-template name="outputChangeBarStyle">
               <xsl:with-param name="value">
                  <xsl:value-of select="$firstValue"/>
               </xsl:with-param>
            </xsl:call-template>


            <xsl:call-template name="parseChangeBarStyle">
               <xsl:with-param name="value" select="substring-after($value,';')"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="outputChangeBarStyle">
               <xsl:with-param name="value">
                  <xsl:value-of select="$value"/>
               </xsl:with-param>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template name="outputChangeBarStyle">
      <xsl:param name="value"/>

      <xsl:choose>
         <xsl:when test="$value = ''"/>
         <xsl:when test="contains($value,':')">
            <xsl:variable name="attr" select="substring-before($value,':')"/>
            <xsl:variable name="val" select="substring-after($value,':')"/>

            <!-- 
            change-bar-color 
      change-bar-offset
      change-bar-placement= start | end | left | right | inside | outside | alternate 
      change-bar-style = none | hidden | dotted | dashed | solid | double | groove | ridge | inset | outset
      change-bar-width
      -->

            <xsl:choose>
               <xsl:when test="$attr='color'">
                  <xsl:attribute name="change-bar-color">
                     <xsl:value-of select="$val"/>
                  </xsl:attribute>
               </xsl:when>
               <xsl:when test="$attr='offset'">
                  <xsl:attribute name="change-bar-offset">
                     <xsl:value-of select="$val"/>
                  </xsl:attribute>
               </xsl:when>
               <xsl:when test="$attr='placement'">
                  <xsl:attribute name="change-bar-placement">
                     <xsl:value-of select="$val"/>
                  </xsl:attribute>
               </xsl:when>
               <xsl:when test="$attr='style'">
                  <xsl:attribute name="change-bar-style">
                     <xsl:value-of select="$val"/>
                  </xsl:attribute>
               </xsl:when>
               <xsl:when test="$attr='width'">
                  <xsl:attribute name="change-bar-width">
                     <xsl:value-of select="$val"/>
                  </xsl:attribute>
               </xsl:when>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
            <!-- do nothing -->
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template match="suitesol:changebar-end">      
      <fo:change-bar-end>
         <xsl:attribute name="change-bar-class">
            <xsl:text>dv</xsl:text>
            <xsl:value-of select="@id" />
         </xsl:attribute>
      </fo:change-bar-end>

   </xsl:template>
   
   <xsl:template name="parseFlagStyle">
      <xsl:param name="value"/>

      <xsl:choose>
         <xsl:when test="$value = ''"/>
         <xsl:when test="contains($value,';')">
            <xsl:variable name="firstValue" select="substring-before($value,';')"/>
            <xsl:call-template name="outputFlagStyle">
               <xsl:with-param name="value">
                  <xsl:value-of select="$firstValue"/>
               </xsl:with-param>
            </xsl:call-template>

            <xsl:call-template name="parseFlagStyle">
               <xsl:with-param name="value" select="substring-after($value,';')"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="outputFlagStyle">
               <xsl:with-param name="value">
                  <xsl:value-of select="$value"/>
               </xsl:with-param>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template name="outputFlagStyle">
      <xsl:param name="value"/>

      <xsl:choose>
         <xsl:when test="$value = ''"/>
         <xsl:when test="contains($value,':')">
            <xsl:variable name="attr" select="substring-before($value,':')"/>
            <xsl:variable name="val" select="substring-after($value,':')"/>
            
            <xsl:attribute name="{$attr}">
               <xsl:value-of select="$val"/>
            </xsl:attribute>
            
         </xsl:when>
         <xsl:otherwise>
            <!-- do nothing -->
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <xsl:template match="*" mode="placeImage">
      <xsl:param name="imageAlign"/>
      <xsl:param name="href"/>
      <xsl:param name="height"/>
      <xsl:param name="width"/>
      <!--Using align attribute set according to image @align attribute-->
      <xsl:call-template name="processAttrSetReflection">
         <xsl:with-param name="attrSet" select="concat('__align__', $imageAlign)"/>
         <xsl:with-param name="path" select="'../../cfg/fo/attrs/commons-attr.xsl'"/>
      </xsl:call-template>
      <fo:external-graphic src="url({$href})" xsl:use-attribute-sets="flag.image">
         <xsl:apply-templates select="suitesol:flagging-inside"/>
         <!--Setting image height if defined-->
         <xsl:if test="$height">
            <xsl:attribute name="content-height">
               <!--The following test was commented out because most people found the behavior
                 surprising.  It used to force images with a number specified for the dimensions
                 *but no units* to act as a measure of pixels, *if* you were printing at 72 DPI.
                 Uncomment if you really want it. -->
              <xsl:choose>
                <!--xsl:when test="not(string(number($height)) = 'NaN')">
                  <xsl:value-of select="concat($height div 72,'in')"/>
                </xsl:when-->
                <xsl:when test="not(string(number($height)) = 'NaN')">
                  <xsl:value-of select="concat($height, 'px')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$height"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
         </xsl:if>
         <!--Setting image width if defined-->
         <xsl:if test="$width">
            <xsl:attribute name="content-width">
              <xsl:choose>
                <!--xsl:when test="not(string(number($width)) = 'NaN')">
                  <xsl:value-of select="concat($width div 72,'in')"/>
                </xsl:when-->
                <xsl:when test="not(string(number($width)) = 'NaN')">
                  <xsl:value-of select="concat($width, 'px')"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:value-of select="$width"/>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:attribute>
         </xsl:if>
         <xsl:if test="not($width) and not($height) and @scale">
            <xsl:attribute name="content-width">
               <xsl:value-of select="concat(@scale,'%')"/>
            </xsl:attribute>
         </xsl:if>
      </fo:external-graphic>
   </xsl:template>

   <!--xsl:template match="*" mode="placeNoteContent">
      <fo:block xsl:use-attribute-sets="note" id="{@id}">
         <xsl:apply-templates select="suitesol:flagging-inside"/>
         <fo:inline xsl:use-attribute-sets="note__label">
            <xsl:choose>
               <xsl:when test="@type='note' or not(@type)">
                  <fo:inline xsl:use-attribute-sets="note__label__note">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Note'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='tip'">
                  <fo:inline xsl:use-attribute-sets="note__label__tip">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Tip'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='fastpath'">
                  <fo:inline xsl:use-attribute-sets="note__label__fastpath">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Fastpath'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='restriction'">
                  <fo:inline xsl:use-attribute-sets="note__label__restriction">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Restriction'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='important'">
                  <fo:inline xsl:use-attribute-sets="note__label__important">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Important'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='remember'">
                  <fo:inline xsl:use-attribute-sets="note__label__remember">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Remember'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='attention'">
                  <fo:inline xsl:use-attribute-sets="note__label__attention">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Attention'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='caution'">
                  <fo:inline xsl:use-attribute-sets="note__label__caution">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Caution'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='danger'">
                  <fo:inline xsl:use-attribute-sets="note__label__danger">
                     <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Danger'"/>
                     </xsl:call-template>
                  </fo:inline>
               </xsl:when>
               <xsl:when test="@type='other'">
                  <fo:inline xsl:use-attribute-sets="note__label__other">
                     <xsl:choose>
                        <xsl:when test="@othertype">
                           <xsl:value-of select="@othertype"/>
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:text>[</xsl:text>
                           <xsl:value-of select="@type"/>
                           <xsl:text>]</xsl:text>
                        </xsl:otherwise>
                     </xsl:choose>
                  </fo:inline>
               </xsl:when>
            </xsl:choose>
            <xsl:text>: </xsl:text>
         </fo:inline>
         <xsl:text>  </xsl:text>
         <xsl:apply-templates select="node()[not(name()='suitesol:flagging-inside')]"/>
      </fo:block>
   </xsl:template-->
   
   <xsl:template match="suitesol:flagging-outside">         
      <fo:block>
         <xsl:call-template name="parseFlagStyle">
            <xsl:with-param name="value">
               <xsl:value-of select="@style"/>
            </xsl:with-param>
         </xsl:call-template>
         <xsl:apply-templates />
      </fo:block>
   </xsl:template>
   
   <xsl:template match="suitesol:flagging-outside-inline" priority="10">
     <fo:inline>
         <xsl:call-template name="parseFlagStyle">
            <xsl:with-param name="value">
               <xsl:value-of select="@style"/>
            </xsl:with-param>
         </xsl:call-template>
         <xsl:apply-templates />
      </fo:inline>
   </xsl:template>
</xsl:stylesheet>

