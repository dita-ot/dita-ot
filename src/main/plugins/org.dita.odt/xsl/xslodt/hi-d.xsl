<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

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
     xmlns:styleUtils="org.dita.dost.util.StyleUtils" exclude-result-prefixes="styleUtils"
     version="1.0">

<xsl:output method="xml"/>
<xsl:output indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="*[contains(@class,' hi-d/b ')]">
     
     <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
               <xsl:element name="text:p">
                     <xsl:apply-templates/>
               </xsl:element>
          </xsl:when>
          <!-- nested by entry -->
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
              
               
                    <!-- alignment styles -->
                    <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
                         <xsl:call-template name="set_align_value"/>
                    </xsl:if>
                    <xsl:apply-templates/>
              
          </xsl:when>
          <!-- nested by stentry -->
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
               <xsl:element name="text:p">
                    <xsl:apply-templates/>
               </xsl:element>
          </xsl:when>
          <!-- nested by other tags -->
          <xsl:otherwise>
               <!-- 
               <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">bold</xsl:attribute>
               -->     
                    <xsl:apply-templates/>
               <!-- 
               </xsl:element>
               -->
          </xsl:otherwise>
     </xsl:choose>
     
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/i ')]">
     
     <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
               <xsl:element name="text:p">
                         <xsl:apply-templates/>
               </xsl:element>
          </xsl:when>
          <!-- nested by entry -->
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
               <!-- create p tag -->
               <xsl:element name="text:p">
                    <!-- alignment styles -->
                    <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
                         <xsl:call-template name="set_align_value"/>
                    </xsl:if>
                    <!-- cell belongs to thead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
                              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by stentry -->
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
               <xsl:element name="text:p">
                    <!-- cell belongs to sthead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
                              parent::*[contains(@class, ' topic/sthead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by other tags -->
          <xsl:otherwise>
               <!-- 
               <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">italic</xsl:attribute>
                    <xsl:apply-templates/>
               </xsl:element>
               -->
               <xsl:apply-templates/>
          </xsl:otherwise>
     </xsl:choose>
     
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/u ')]">
     
     <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
               <xsl:element name="text:p">
                    <xsl:apply-templates/>
               </xsl:element>
          </xsl:when>
          <!-- nested by entry -->
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
               <!-- create p tag -->
               <xsl:element name="text:p">
                    <!-- alignment styles -->
                    <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
                         <xsl:call-template name="set_align_value"/>
                    </xsl:if>
                    <!-- cell belongs to thead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
                              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by stentry -->
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
               <xsl:element name="text:p">
                    <!-- cell belongs to sthead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
                              parent::*[contains(@class, ' topic/sthead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by other tags -->
          <xsl:otherwise>
               <!-- 
               <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">underline</xsl:attribute>
                    <xsl:apply-templates/>
               </xsl:element>
               -->
               <xsl:apply-templates/>
          </xsl:otherwise>
     </xsl:choose>
     
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/tt ')]">
     
     <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
               <xsl:element name="text:p">
                    <xsl:element name="text:span">
                         <xsl:attribute name="text:style-name">Courier</xsl:attribute>
                         <xsl:apply-templates/>
                    </xsl:element>
               </xsl:element>
          </xsl:when>
          <!-- nested by entry -->
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
               <!-- create p tag -->
               <xsl:element name="text:p">
                    <!-- alignment styles -->
                    <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
                         <xsl:call-template name="set_align_value"/>
                    </xsl:if>
                    <xsl:element name="text:span">
                         <xsl:attribute name="text:style-name">Courier</xsl:attribute>
                         <xsl:apply-templates/>
                    </xsl:element>
               </xsl:element>
          </xsl:when>
          <!-- nested by stentry -->
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
               <xsl:element name="text:p">
                    <xsl:element name="text:span">
                         <xsl:attribute name="text:style-name">Courier</xsl:attribute>
                         <xsl:apply-templates/>
                    </xsl:element>
               </xsl:element>
          </xsl:when>
          <!-- nested by other tags -->
          <xsl:otherwise>
               <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">Courier</xsl:attribute>
                    <xsl:apply-templates/>
               </xsl:element>
          </xsl:otherwise>
     </xsl:choose>
     
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sup ')]">
     
     <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
               <xsl:element name="text:p">
                     <xsl:apply-templates/>
               </xsl:element>
          </xsl:when>
          <!-- nested by entry -->
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
               <!-- create p tag -->
               <xsl:element name="text:p">
                    <!-- alignment styles -->
                    <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
                         <xsl:call-template name="set_align_value"/>
                    </xsl:if>
                    <!-- cell belongs to thead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
                              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by stentry -->
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
               <xsl:element name="text:p">
                    <!-- cell belongs to sthead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
                              parent::*[contains(@class, ' topic/sthead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by other tags -->
          <xsl:otherwise>
               <!-- 
               <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">sup</xsl:attribute>
                    <xsl:apply-templates/>
               </xsl:element>
               -->
               <xsl:apply-templates/>
          </xsl:otherwise>
     </xsl:choose>
     
</xsl:template>

<xsl:template match="*[contains(@class,' hi-d/sub ')]">
     
     <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
               <xsl:element name="text:p">
                    <xsl:apply-templates/>
               </xsl:element>
          </xsl:when>
          <!-- nested by entry -->
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
               <!-- create p tag -->
               <xsl:element name="text:p">
                    <!-- alignment styles -->
                    <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
                         <xsl:call-template name="set_align_value"/>
                    </xsl:if>
                    <!-- cell belongs to thead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
                              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by stentry -->
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
               <xsl:element name="text:p">
                    <!-- cell belongs to sthead -->
                    <xsl:choose>
                         <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
                              parent::*[contains(@class, ' topic/sthead ')]">
                              <xsl:element name="text:span">
                                   <xsl:attribute name="text:style-name">bold</xsl:attribute>
                                        <xsl:apply-templates/>
                              </xsl:element>
                         </xsl:when>
                         <xsl:otherwise>
                                   <xsl:apply-templates/>
                         </xsl:otherwise>
                    </xsl:choose>
               </xsl:element>
          </xsl:when>
          <!-- nested by other tags -->
          <xsl:otherwise>
               <!-- 
               <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">sub</xsl:attribute>
                    <xsl:apply-templates/>
               </xsl:element>
               -->
               <xsl:apply-templates/>
          </xsl:otherwise>
     </xsl:choose>    
</xsl:template>
     
<xsl:template match="text()|*[contains(@class, ' topic/state ')]" mode="create_hi_style">
     <!-- generating style name based on the styles used on the text. -->
     <xsl:variable name="style_name">
          <xsl:call-template name="get_style_name"/> 
     </xsl:variable>
     
     <xsl:variable name="hasStyleName">
          <xsl:value-of select="styleUtils:insertHiStyleName($style_name)"/>
     </xsl:variable>
     
     
     
     <xsl:if test="$style_name != '' and $hasStyleName = 'false'">
          <!-- common hi style -->
          <xsl:element name="style:style">
               <xsl:attribute name="style:name">
                    <xsl:value-of select="$style_name"/>
               </xsl:attribute>
               <xsl:attribute name="style:family">text</xsl:attribute>
               <xsl:element name="style:text-properties">
                    <!-- bold-->
                    <xsl:if test="contains($style_name, 'bold')">
                         <xsl:attribute name="fo:font-weight">bold</xsl:attribute>
                         <xsl:attribute name="style:font-weight-asian">bold</xsl:attribute>
                         <xsl:attribute name="style:font-weight-complex">bold</xsl:attribute>
                    </xsl:if>                    
                    <!-- italic -->
                    <xsl:if test="contains($style_name, 'italic')">
                         <xsl:attribute name="fo:font-style">italic</xsl:attribute>
                         <xsl:attribute name="style:font-style-asian">italic</xsl:attribute>
                         <xsl:attribute name="style:font-style-complex">italic</xsl:attribute>
                    </xsl:if>
                    <!-- underline -->
                    <xsl:if test="contains($style_name, 'underline')">
                         <xsl:attribute name="style:text-underline-style">solid</xsl:attribute>
                         <xsl:attribute name="style:text-underline-type">single</xsl:attribute>
                         <xsl:attribute name="style:text-underline-width">auto</xsl:attribute>
                         <xsl:attribute name="style:text-underline-color">font-color</xsl:attribute>
                    </xsl:if>
                    <!-- sub -->
                    <xsl:if test="contains($style_name, 'sub')">
                         <xsl:choose>
                              <xsl:when test="contains($style_name ,'sub1')">
                                   <xsl:attribute name="style:text-position">-10% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub2')">
                                   <xsl:attribute name="style:text-position">-20% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub3')">
                                   <xsl:attribute name="style:text-position">-30% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub4')">
                                   <xsl:attribute name="style:text-position">-40% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub5')">
                                   <xsl:attribute name="style:text-position">-50% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:otherwise>
                                   <xsl:attribute name="style:text-position">-60% 58%</xsl:attribute>
                              </xsl:otherwise>
                         </xsl:choose>
                    </xsl:if>
                    <!-- sup -->
                    <xsl:if test="contains($style_name, 'sup')">
                         <xsl:choose>
                              <xsl:when test="contains($style_name ,'sup1')">
                                   <xsl:attribute name="style:text-position">40% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup2')">
                                   <xsl:attribute name="style:text-position">50% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup3')">
                                   <xsl:attribute name="style:text-position">60% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup4')">
                                   <xsl:attribute name="style:text-position">70% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup5')">
                                   <xsl:attribute name="style:text-position">80% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:otherwise>
                                   <xsl:attribute name="style:text-position">90% 58%</xsl:attribute>
                              </xsl:otherwise>
                         </xsl:choose>
                    </xsl:if>
                    <!-- codeblock/screen -->
                    <xsl:if test="contains($style_name, 'code')">
                         <xsl:attribute name="fo:background-color">#d9d9d9</xsl:attribute>
                    </xsl:if>
               </xsl:element>
          </xsl:element>
          <!-- boolean hi style -->
          <xsl:element name="style:style">
               <xsl:attribute name="style:name">
                    <xsl:value-of select="concat('boolean_', $style_name)"/>
               </xsl:attribute>
               <xsl:attribute name="style:family">text</xsl:attribute>
               <xsl:attribute name="style:parent-style-name">boolean_style</xsl:attribute>
               <xsl:element name="style:text-properties">
                    <!-- bold-->
                    <xsl:if test="contains($style_name, 'bold')">
                         <xsl:attribute name="fo:font-weight">bold</xsl:attribute>
                         <xsl:attribute name="style:font-weight-asian">bold</xsl:attribute>
                         <xsl:attribute name="style:font-weight-complex">bold</xsl:attribute>
                    </xsl:if>
                    <!-- italic -->
                    <xsl:if test="contains($style_name, 'italic')">
                         <xsl:attribute name="fo:font-style">italic</xsl:attribute>
                         <xsl:attribute name="style:font-style-asian">italic</xsl:attribute>
                         <xsl:attribute name="style:font-style-complex">italic</xsl:attribute>
                    </xsl:if>
                    <!-- underline -->
                    <xsl:if test="contains($style_name, 'underline')">
                         <xsl:attribute name="style:text-underline-style">solid</xsl:attribute>
                         <xsl:attribute name="style:text-underline-type">single</xsl:attribute>
                         <xsl:attribute name="style:text-underline-width">auto</xsl:attribute>
                         <xsl:attribute name="style:text-underline-color">font-color</xsl:attribute>
                    </xsl:if>
                    <!-- sub -->
                    <xsl:if test="contains($style_name, 'sub')">
                         <xsl:choose>
                              <xsl:when test="contains($style_name ,'sub1')">
                                   <xsl:attribute name="style:text-position">-10% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub2')">
                                   <xsl:attribute name="style:text-position">-20% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub3')">
                                   <xsl:attribute name="style:text-position">-30% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub4')">
                                   <xsl:attribute name="style:text-position">-40% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sub5')">
                                   <xsl:attribute name="style:text-position">-50% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:otherwise>
                                   <xsl:attribute name="style:text-position">-60% 58%</xsl:attribute>
                              </xsl:otherwise>
                         </xsl:choose>
                    </xsl:if>
                    <!-- sup -->
                    <xsl:if test="contains($style_name, 'sup')">
                         <xsl:choose>
                              <xsl:when test="contains($style_name ,'sup1')">
                                   <xsl:attribute name="style:text-position">40% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup2')">
                                   <xsl:attribute name="style:text-position">50% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup3')">
                                   <xsl:attribute name="style:text-position">60% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup4')">
                                   <xsl:attribute name="style:text-position">70% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:when test="contains($style_name ,'sup5')">
                                   <xsl:attribute name="style:text-position">80% 58%</xsl:attribute>
                              </xsl:when>
                              <xsl:otherwise>
                                   <xsl:attribute name="style:text-position">90% 58%</xsl:attribute>
                              </xsl:otherwise>
                         </xsl:choose>
                    </xsl:if>
                    <!-- codeblock/screen -->
                    <xsl:if test="contains($style_name, 'code')">
                         <xsl:attribute name="fo:background-color">#d9d9d9</xsl:attribute>
                    </xsl:if>
               </xsl:element>
          </xsl:element>
     </xsl:if>
     
</xsl:template>

<xsl:template name="get_style_name">
     
     <xsl:if test="ancestor::*[contains(@class, ' hi-d/i ')] | ancestor::*[contains(@class, ' topic/term ')] | 
          ancestor::*[contains(@class, ' topic/cite ')]">
         <xsl:value-of select="'italic'"/> 
     </xsl:if>
     
     <xsl:if test="ancestor::*[contains(@class, ' hi-d/u ')]">
          <xsl:value-of select="'underline'"/>
     </xsl:if>
     
     <xsl:if test="ancestor::*[contains(@class, ' hi-d/b ')]">
          <xsl:value-of select="'bold'"/>
     </xsl:if>
     
     <xsl:if test="ancestor::*[contains(@class, ' hi-d/sub ')]">
          <xsl:value-of select="'sub'"/>
          <xsl:variable name="depth" select="count(ancestor::*[contains(@class, ' hi-d/sub ')])"/>
          <xsl:choose>
               <xsl:when test="$depth &gt; 5">
                    <xsl:value-of select="6"/>
               </xsl:when>
               <xsl:otherwise>
                    <xsl:value-of select="$depth"/>
               </xsl:otherwise>
          </xsl:choose>
     </xsl:if>
     
     <xsl:if test="ancestor::*[contains(@class, ' hi-d/sup ')]">
          <xsl:value-of select="'sup'"/>
          <xsl:variable name="depth" select="count(ancestor::*[contains(@class, ' hi-d/sup ')])"/>
          <xsl:choose>
               <xsl:when test="$depth &gt; 5">
                    <xsl:value-of select="6"/>
               </xsl:when>
               <xsl:otherwise>
                    <xsl:value-of select="$depth"/>
               </xsl:otherwise>
          </xsl:choose>
     </xsl:if>
     
     <xsl:if test="ancestor::*[contains(@class, ' ui-d/screen ')] | ancestor::*[contains(@class, ' pr-d/codeblock ')]">
          <xsl:value-of select="'code'"/>
     </xsl:if>
     
</xsl:template>

</xsl:stylesheet>
