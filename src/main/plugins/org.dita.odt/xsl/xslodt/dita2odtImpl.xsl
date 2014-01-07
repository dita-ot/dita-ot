<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
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
  xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
  xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
  xmlns:prodtools="http://www.ibm.com/xmlns/prodtools" 
  xmlns:styleUtils="org.dita.dost.util.StyleUtils"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  exclude-result-prefixes="styleUtils ditamsg"
  version="1.0">
  
  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
  <!-- =========== "GLOBAL" DECLARATIONS =========== -->
  
  <!-- The document tree of filterfile returned by document($FILTERFILE,/)-->
  <xsl:variable name="FILTERFILEURL">
    <xsl:choose>
      <xsl:when test="not($FILTERFILE)"/> <!-- If no filterfile leave empty -->
      <xsl:when test="starts-with($FILTERFILE,'file:')">
        <xsl:value-of select="$FILTERFILE"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($FILTERFILE,'/')">
            <xsl:text>file://</xsl:text><xsl:value-of select="$FILTERFILE"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>file:/</xsl:text><xsl:value-of select="$FILTERFILE"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="FILTERDOC" select="document($FILTERFILEURL,/)"/>
  
  <!-- Define a newline character -->
  <xsl:variable name="newline"><xsl:text>
  </xsl:text></xsl:variable>
  
  <!--Check the file Url Definition of HDF HDR FTR-->
  <!-- 
  <xsl:variable name="HDFFILE">
    <xsl:choose>
      <xsl:when test="not($HDF)"/>
      <xsl:when test="starts-with($HDF,'file:')">
        <xsl:value-of select="$HDF"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($HDF,'/')">
            <xsl:text>file://</xsl:text><xsl:value-of select="$HDF"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>file:/</xsl:text><xsl:value-of select="$HDF"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="HDRFILE">
    <xsl:choose>
      <xsl:when test="not($HDR)"/> 
      <xsl:when test="starts-with($HDR,'file:')">
        <xsl:value-of select="$HDR"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($HDR,'/')">
            <xsl:text>file://</xsl:text><xsl:value-of select="$HDR"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>file:/</xsl:text><xsl:value-of select="$HDR"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="FTRFILE">
    <xsl:choose>
      <xsl:when test="not($FTR)"/> 
      <xsl:when test="starts-with($FTR,'file:')">
        <xsl:value-of select="$FTR"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with($FTR,'/')">
            <xsl:text>file://</xsl:text><xsl:value-of select="$FTR"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>file:/</xsl:text><xsl:value-of select="$FTR"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  -->
  <!-- Filler for A-name anchors  - was &nbsp;-->
  <xsl:variable name="afill"></xsl:variable>
  
  <!-- these elements are never processed in a conventional presentation. can be overridden. -->
  <xsl:template match="*[contains(@class,' topic/no-topic-nesting ')]"/>
  
  <xsl:template match="*[contains(@class,' topic/topic ')]">
    <!-- 
      <xsl:apply-templates/>
    -->
    <xsl:variable name="topicType">
      <xsl:call-template name="determineTopicType"/>
    </xsl:variable>
    
    <xsl:choose>
      <xsl:when test="$topicType = 'topicChapter'">
        <xsl:call-template name="processTopicChapter"/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicAppendix'">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicPart'">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicPreface'">
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicNotices'">
        <!-- Suppressed in normal processing, since it goes at the beginning of the book. -->
        <!-- <xsl:call-template name="processTopicNotices"/> -->
      </xsl:when>
      <xsl:when test="$topicType = 'topicSimple'">
        <xsl:apply-templates/>
      </xsl:when>
      <!--BS: skipp abstract (copyright) from usual content. It will be processed from the front-matter-->
      <xsl:when test="$topicType = 'topicAbstract'"/>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/body ')]">
    <!-- Added for DITA 1.1 "Shortdesc proposal" -->
    <!-- get the abstract para -->
    <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/abstract ')]" mode="outofline"/>
    
    <!-- get the shortdesc para -->
    <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]" mode="outofline"/>
    
    <!-- Insert pre-req links - after shortdesc - unless there is a prereq section about -->
    <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/related-links ')]" mode="prereqs"/>
    
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/bodydiv ')]">
    <xsl:choose>
      <!-- direct child of body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <xsl:element name="text:p">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
    <xsl:variable name="depth">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')])"/>
    </xsl:variable>
    
    <xsl:call-template name="block-title">
      <xsl:with-param name="depth" select="$depth"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="synsect"/>

  <xsl:template match="*[contains(@class,' topic/section ')]">
    
    <xsl:choose>
      <!-- nested by body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <xsl:element name="text:p">
          <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
          <xsl:element name="text:line-break"/>
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <!-- if has title tag -->
            <xsl:if test="child::*[contains(@class, ' topic/title ')]">
              <xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="render_section_title"/>
            </xsl:if>
            <xsl:apply-templates select="*[not(contains(@class,' topic/title '))] | text() | comment() | processing-instruction()"/>
            
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </xsl:element>
        </xsl:element> 
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:line-break"/>
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/sectiondiv ')]">
    <xsl:element name="text:span">
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/title ')]" mode="render_section_title">
    
    <xsl:variable name="headCount">
      <xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')])+1"/>
    </xsl:variable>
    <!-- Heading_20_2 -->
    <xsl:element name="text:span">
      <!-- 
      <xsl:attribute name="text:style-name"><xsl:value-of select="concat('Heading_20_' , $headCount)"/></xsl:attribute>
      -->
      <xsl:attribute name="text:style-name">bold</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
    <xsl:element name="text:line-break"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/section ')]/*[contains(@class,' topic/note ')]" priority="1">
    <xsl:element name="text:span">
      <xsl:apply-templates/>
    </xsl:element>
    <xsl:element name="text:line-break"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/example ')]">
    
    <xsl:choose>
      <!-- parent is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>		
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags. -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>        
        </xsl:element>        
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]">
    <xsl:element name="text:span">
      <xsl:attribute name="text:style-name">bold</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
    <xsl:element name="text:line-break"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/fig ')]">
    
    <xsl:choose>
      <!-- parent is body, li... -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
        parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>
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
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                  
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                
              </xsl:element>
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
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                  
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/figgroup ')]">
    
    <xsl:element name="text:span">
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>
      </xsl:element>
    </xsl:element>
    <text:line-break/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]">
    <xsl:variable name="ancestorlang">
      <xsl:call-template name="getLowerCaseLang"/>
    </xsl:variable>
    <xsl:variable name="fig-count-actual" select="count(preceding::*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')])+1"><!-- Number of fig/title's including this one --></xsl:variable>
   
    <text:line-break/>
    <text:span text:style-name="center">
      <text:span text:style-name="bold">
        <xsl:choose>
          <!-- Hungarian: "1. Figure " -->
          <xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) 
            or (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )">
            <xsl:value-of select="$fig-count-actual"/><xsl:text>. </xsl:text>
            <xsl:call-template name="getStringODT">
              <xsl:with-param name="stringName" select="'Figure'"/>
            </xsl:call-template><xsl:text> </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="getStringODT">
              <xsl:with-param name="stringName" select="'Figure'"/>
            </xsl:call-template>
            <xsl:text> </xsl:text>
            <xsl:value-of select="$fig-count-actual"/>
            <xsl:text>. </xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:value-of select="."/>
      </text:span>
    </text:span>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/desc ')]" priority="2">
    
    <xsl:element name="text:span">
      <xsl:apply-templates/>
    </xsl:element>
    
    <xsl:element name="text:line-break"/>
  </xsl:template>  

  <!-- =========== block things ============ -->

  <xsl:template match="*[contains(@class,' topic/p ')]">
    
    
    <xsl:choose>
      <!-- nested by body or list -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
                      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
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
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
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
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <xsl:element name="text:p">
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
              parent::*[contains(@class, ' topic/sthead ')]">
                <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
              </xsl:element>	
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <!-- imitate a paragraph -->
        <xsl:element name="text:line-break"/>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/ph ')]">
    
    <xsl:choose>
      <!-- nested by list -->
      <xsl:when test=" parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
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
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
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
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <xsl:element name="text:p">
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
              parent::*[contains(@class, ' topic/sthead ')]">
              <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
              <xsl:element name="text:span">
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:apply-templates/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>	
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </xsl:element>	
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:apply-templates/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/keyword ')]">
    
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
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
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates> 
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'keyword'"/>
          </xsl:apply-templates>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'keyword'"/>
          </xsl:apply-templates>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <!-- lines tag -->
  <xsl:template match="*[contains(@class,' topic/lines ')]">
    
    <xsl:choose>
      <!-- nested by body, li -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
        parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <!-- add flagging styles -->
            <xsl:apply-templates select="." mode="add-odt-flagging"/>
            <xsl:apply-templates/>
          </xsl:element>
        </xsl:element>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
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
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="add-odt-flagging"/>
                <xsl:apply-templates/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <xsl:element name="text:p">
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
              parent::*[contains(@class, ' topic/sthead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- add flagging styles -->
                <xsl:apply-templates select="." mode="add-odt-flagging"/>
                <xsl:apply-templates/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- other tags -->      
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
          <xsl:element name="text:line-break"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/pre ')]">

    <xsl:choose>
      
      <!-- nested by body, li -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
                      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <xsl:element name="text:p">
          <xsl:attribute name="text:style-name">Code_Style_Paragraph</xsl:attribute>
          <xsl:element name="text:span">
            <!-- add flagging styles -->
            <xsl:apply-templates select="." mode="add-odt-flagging"/>
            <xsl:apply-templates/>
          </xsl:element>
        </xsl:element>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
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
                <xsl:element name="text:span">
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- add flagging styles -->
                <xsl:apply-templates select="." mode="add-odt-flagging"/>
                <xsl:apply-templates/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <xsl:element name="text:p">
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
              parent::*[contains(@class, ' topic/sthead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
            <xsl:attribute name="text:style-name">Code_Text</xsl:attribute>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
        </xsl:element>
        <xsl:element name="text:line-break"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- phrases -->
  <xsl:template match="*[contains(@class,' hi-d/tt ')]">
    
    <xsl:element name="text:span">
      <xsl:attribute name="text:style-name">bold</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
    
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/q ')]">
    
    
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:call-template name="create_q_content"/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
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
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:call-template name="create_q_content"/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:call-template name="create_q_content"/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
              </xsl:element>
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
                <xsl:element name="text:span">
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:call-template name="create_q_content"/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:call-template name="create_q_content"/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:call-template name="create_q_content"/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template name="create_q_content">
    
    <xsl:variable name="style_name">
      <xsl:call-template name="get_style_name"/> 
    </xsl:variable>
    
    <xsl:variable name="trueStyleName">
      <xsl:value-of select="styleUtils:getHiStyleName($style_name)"/>
    </xsl:variable>
    
    <xsl:element name="text:span">
      <xsl:if test="$trueStyleName!=''">
        <xsl:attribute name="text:style-name">
          <xsl:value-of select="$trueStyleName"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="getStringODT">
        <xsl:with-param name="stringName" select="'OpenQuote'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
      <xsl:call-template name="getStringODT">
        <xsl:with-param name="stringName" select="'CloseQuote'"/>
      </xsl:call-template>
    </xsl:element>
  </xsl:template>


  <!-- named template library -->

  <xsl:template name="br-replace">
    <xsl:param name="word"/>
    <!-- </xsl:text> on next line on purpose to get newline -->
    <xsl:variable name="cr">
      <xsl:text>\n
</xsl:text>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="contains($word,$cr)">
        <xsl:value-of select="substring-before($word,$cr)"/>
        <br/>
        <xsl:call-template name="br-replace">
          <xsl:with-param name="word" select="substring-after($word,$cr)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$word"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

<!-- standard RTF library mapped to formatting objects -->

<!--e.g  
  <text:h text:style-name="Heading_20_1" text:outline-level="1">
    <text:bookmark text:name="aaa"/>Topic 01
  </text:h>
-->
<xsl:template name="block-title">
  <xsl:param name="depth"/>
  
  <xsl:element name="text:h">
    <xsl:attribute name="text:outline-level">
      <xsl:value-of select="$depth"/>
    </xsl:attribute>
    <xsl:choose>
      <xsl:when test="$depth='1'">
        <xsl:attribute name="text:style-name">Heading_20_1</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth='2'">
        <xsl:attribute name="text:style-name">Heading_20_2</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth='3'">
        <xsl:attribute name="text:style-name">Heading_20_3</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth='4'">
        <xsl:attribute name="text:style-name">Heading_20_4</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth='5'">
        <xsl:attribute name="text:style-name">Heading_20_5</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:attribute name="text:style-name">Heading_20_6</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
    <!-- create end bookmark -->
    <xsl:call-template name="gen-bookmark">
      <xsl:with-param name="flag" select="1"/>
    </xsl:call-template>
    
  </xsl:element>
</xsl:template>
  
  
<!-- font-weight="bold" -->
<xsl:template name="inline-em">
  <xsl:element name="text:span">
    <xsl:attribute name="text:style-name">bold</xsl:attribute>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- font-size="24pt"
   font-weight="bold"
   space-before.optimum="16pt"
   space-after.optimum="12pt" -->
<xsl:template name="block-title-h1">
  <xsl:attribute name="text:style-name">Heading_20_1</xsl:attribute>
  <xsl:apply-templates/>
</xsl:template>

<!-- font-size="16pt"
   font-weight="bold"
   space-before.optimum="14pt"
   space-after.optimum="14pt" \pard\li720\fi-360-->
<xsl:template name="block-title-h2">
  <xsl:attribute name="text:style-name">Heading_20_2</xsl:attribute>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template name="block-title-h3">
  <xsl:attribute name="text:style-name">Heading_20_3</xsl:attribute>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template name="block-title-h4">
  <xsl:attribute name="text:style-name">Heading_20_4</xsl:attribute>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template name="block-title-h5">
  <xsl:attribute name="text:style-name">Heading_20_5</xsl:attribute>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template name="block-title-h6">
  <xsl:attribute name="text:style-name">Heading_20_6</xsl:attribute>
  <xsl:apply-templates/>
</xsl:template>


<xsl:template name="block-pre">

  <xsl:if
    test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]"/>
  <!-- Tagsmiths: make the inserted space conditional, suppressing it for first p in li -->
  <xsl:if test="parent::*[not(contains(@class,' topic/li '))] or position() != 1"/>
<xsl:choose>
  <xsl:when test="parent::*[contains(@class, ' topic/linkinfo ')]">
    <!-- 
    <xsl:element name="text:p">
      <xsl:apply-templates/>
    </xsl:element>
    -->
    <xsl:apply-templates/>
  </xsl:when>
  <xsl:otherwise>
    <xsl:apply-templates/>
  </xsl:otherwise>
</xsl:choose>
  
  <!-- Tagsmiths: make the next rtf string conditional, suppressing it for first p in li -->
  <xsl:if test="parent::*[not(contains(@class,' topic/li '))] or position() != 1"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/lq ')]" name="topic.lq">
  
  <xsl:variable name="samefile">
    <xsl:choose>
      <xsl:when test="@href and starts-with(@href,'#')">
        <xsl:value-of select="'true'"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'false'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="href-value">
    <xsl:choose>
      <xsl:when test="@href and starts-with(@href,'#')">
        <xsl:choose>
          <xsl:when test="contains(@href,'/')">
            <xsl:value-of select="concat('#', substring-after(@href,'/'))"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="@href and contains(@href,'#')">
        <xsl:value-of select="substring-before(@href,'#')"/>
      </xsl:when>
      <xsl:when test="@href and not(@href='')">
        <xsl:value-of select="@href"/>
      </xsl:when>
    </xsl:choose>
  </xsl:variable>
  
  <xsl:choose>
    <!-- parent is body & li -->
    <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
                    parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <xsl:call-template name="create_lq_content">
            <xsl:with-param name="samefile" select="$samefile"/>
            <xsl:with-param name="href-value" select="$href-value"/>
          </xsl:call-template>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>
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
        <!-- cell belongs to thead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
            /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <xsl:call-template name="create_lq_content">
                  <xsl:with-param name="samefile" select="$samefile"/>
                  <xsl:with-param name="href-value" select="$href-value"/>
                </xsl:call-template>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </xsl:element>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags"/>
              <xsl:apply-templates/>
              <xsl:call-template name="create_lq_content">
                <xsl:with-param name="samefile" select="$samefile"/>
                <xsl:with-param name="href-value" select="$href-value"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </xsl:element>	
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
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <xsl:call-template name="create_lq_content">
                  <xsl:with-param name="samefile" select="$samefile"/>
                  <xsl:with-param name="href-value" select="$href-value"/>
                </xsl:call-template>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </xsl:element>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags"/>
              <xsl:apply-templates/>
              <xsl:call-template name="create_lq_content">
                <xsl:with-param name="samefile" select="$samefile"/>
                <xsl:with-param name="href-value" select="$href-value"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:element>
    </xsl:when>
    <!-- other tags -->
    <xsl:otherwise>
      <xsl:element name="text:span">
        <xsl:element name="text:span">
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <xsl:call-template name="create_lq_content">
            <xsl:with-param name="samefile" select="$samefile"/>
            <xsl:with-param name="href-value" select="$href-value"/>
          </xsl:call-template>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>
        </xsl:element>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<xsl:template name="create_lq_content">
  <xsl:param name="samefile" select="''"/>
  <xsl:param name="href-value" select="''"/>
  
  <xsl:choose>
    <xsl:when test="@href and not(@href='')"> 
      <xsl:element name="text:line-break"/>
      <!-- Insert citation as link, use @href as-is -->
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">right</xsl:attribute>
        <xsl:element name="text:a">
          <xsl:choose>
            <xsl:when test="$samefile='true'">
              <xsl:attribute name="xlink:href">
                <xsl:value-of select="$href-value"/>
              </xsl:attribute>
            </xsl:when>
            <xsl:otherwise>
              <xsl:variable name="NORMAMLIZEDOUTPUT" select="translate($OUTPUTDIR, '\', '/')"/>
              <xsl:attribute name="xlink:href">
                <xsl:value-of select="concat($FILEREF, $NORMAMLIZEDOUTPUT, '/', $href-value)"/>
              </xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:choose>
            <xsl:when
              test="@reftitle">
              <xsl:call-template name="get-ascii">
                <xsl:with-param name="txt">
                  <xsl:value-of select="@reftitle"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template
                name="get-ascii">
                <xsl:with-param name="txt">
                  <xsl:value-of select="@href"/>
                </xsl:with-param>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@reftitle and not(@reftitle='')">
      <xsl:element name="text:line-break"/>
      <!-- Insert citation text -->
      <xsl:element name="text:span">
        <xsl:call-template
          name="get-ascii">
          <xsl:with-param name="txt">
            <xsl:value-of select="@reftitle"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:element>
    </xsl:when>
    <xsl:otherwise><!--nop - do nothing--></xsl:otherwise>
  </xsl:choose>
</xsl:template>
<!-- generate bookmark with parent's topic id -->
<xsl:template name="gen-bookmark">
  <xsl:param name="flag"/>
  <xsl:if test="parent::*[contains(@class, ' topic/topic ')]/@id">
    <xsl:variable name="id" select="parent::*[contains(@class, ' topic/topic ')]/@id"/>
    <xsl:choose>
      <!-- if $flag is 0 create bookmark-start -->
      <xsl:when test="$flag = 0">
        <xsl:element name="text:bookmark-start">
          <xsl:attribute name="text:name">
            <xsl:value-of select="$id"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:when>
      <!-- otherwise create bookmark-end -->
      <xsl:otherwise>
        <xsl:element name="text:bookmark-end">
          <xsl:attribute name="text:name">
            <xsl:value-of select="$id"/>
          </xsl:attribute>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:if>
  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/cite ')]">
  
  <xsl:choose>
    <!-- parent is list -->
    <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">italic</xsl:attribute>
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
        <!-- cell belongs to thead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
            /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">italic</xsl:attribute>
                <xsl:apply-templates/>
              </xsl:element>  
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">italic</xsl:attribute>
              <xsl:apply-templates/>
            </xsl:element>  
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
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">italic</xsl:attribute>
                <xsl:apply-templates/>
              </xsl:element>  
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">italic</xsl:attribute>
              <xsl:apply-templates/>
            </xsl:element>  
          </xsl:otherwise>
        </xsl:choose>
      </xsl:element>
    </xsl:when>
    <!-- parent is other tags -->
    <xsl:otherwise>
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">italic</xsl:attribute>
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/desc ')]" priority="0">
  <xsl:element name="text:span">
    <!-- 
    <xsl:apply-templates select="text()"/>
    -->
    <xsl:apply-templates/>
  </xsl:element>
  <!-- 
  <xsl:apply-templates select="child::*[@class]"/>
  -->
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]"/>
<xsl:template match="*[contains(@class,' topic/titlealts ')]"/>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]" mode="outofline">
  
  <xsl:element name="text:p">
    <xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="outofline">
  
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' topic/abstract ')]">
      <xsl:element name="text:span">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:element name="text:p">
          <xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
          <xsl:apply-templates/>
      </xsl:element>
    </xsl:otherwise>
    
  </xsl:choose>
</xsl:template>
  
<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]">
  <xsl:if test="not(following-sibling::*[contains(@class,' topic/body ')])">
    <xsl:apply-templates select="." mode="outofline"/>
    <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/related-links ')]" mode="prereqs"/>
  </xsl:if>
</xsl:template>
  
<!-- Updated for DITA 1.1 "Shortdesc proposal" -->
<!-- Added for SF 1363055: Shortdesc disappears when optional body is removed -->
<xsl:template match="*[contains(@class,' topic/shortdesc ')]">
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' topic/abstract ')]">
      <xsl:apply-templates select="." mode="outofline.abstract"/>
    </xsl:when>
    <xsl:when test="not(following-sibling::*[contains(@class,' topic/body ')])">    
      <xsl:apply-templates select="." mode="outofline"/>
      <xsl:apply-templates select="following-sibling::*[contains(@class,' topic/related-links ')]" mode="prereqs"/>
    </xsl:when>
    <xsl:otherwise></xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
  <!-- called shortdesc processing when it is in abstract -->
  <xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="outofline.abstract">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
  </xsl:template>
  
<xsl:template match="*[contains(@class,' topic/note ')]" name="topic.note" priority="0">
  
  <xsl:choose>
    <!-- has child tags -->
    <xsl:when test="child::*|text()">
      <xsl:choose>
        <!-- if the parent tag is body or li-->
        <xsl:when test="parent::*[contains(@class, ' topic/body ')] 
          or parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
          <xsl:element name="text:p">
            <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
            <xsl:element name="text:span">
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags">
                <xsl:with-param name="type" select="'note'"/>
              </xsl:apply-templates>
              <xsl:choose>
                <!-- for hazardstatement -->
                <xsl:when test="contains(@class,' hazard-d/hazardstatement ')">
                      <xsl:call-template name="create_hazards_content"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="create_note_content"/>
                </xsl:otherwise>
              </xsl:choose>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags">
                <xsl:with-param name="type" select="'note'"/>
              </xsl:apply-templates>
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
            <!-- cell belongs to thead -->
            <xsl:choose>
              <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
                /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                <xsl:element name="text:span">
                  <xsl:attribute name="text:style-name">bold</xsl:attribute>
                  <xsl:element name="text:span">
                    <!-- start add flagging styles -->
                    <xsl:apply-templates  select="." mode="start-add-odt-flags">
                      <xsl:with-param name="type" select="'note'"/>
                    </xsl:apply-templates>
                    <xsl:choose>
                      <!-- for hazardstatement -->
                      <xsl:when test="contains(@class,' hazard-d/hazardstatement ')">
                          <xsl:call-template name="create_hazards_content"/>
                      </xsl:when>
                      <xsl:otherwise>
                          <xsl:call-template name="create_note_content"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <!-- end add flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-flags">
                      <xsl:with-param name="type" select="'note'"/>
                    </xsl:apply-templates>
                   </xsl:element>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags">
                    <xsl:with-param name="type" select="'note'"/>
                  </xsl:apply-templates>
                  <xsl:choose>
                    <!-- for hazardstatement -->
                    <xsl:when test="contains(@class,' hazard-d/hazardstatement ')">
                      <xsl:call-template name="create_hazards_content"/>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:call-template name="create_note_content"/>
                    </xsl:otherwise>
                  </xsl:choose>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags">
                    <xsl:with-param name="type" select="'note'"/>
                  </xsl:apply-templates>
                </xsl:element>
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
                  <xsl:element name="text:span">
                    <!-- start add flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-flags">
                      <xsl:with-param name="type" select="'note'"/>
                    </xsl:apply-templates>
                      <xsl:choose>
                        <!-- for hazardstatement -->
                        <xsl:when test="contains(@class,' hazard-d/hazardstatement ')">
                          <xsl:call-template name="create_hazards_content"/>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:call-template name="create_note_content"/>
                        </xsl:otherwise>
                      </xsl:choose>
                      <!-- end add flagging styles -->
                      <xsl:apply-templates select="." mode="end-add-odt-flags">
                        <xsl:with-param name="type" select="'note'"/>
                      </xsl:apply-templates>
                  </xsl:element>
                </xsl:element>
              </xsl:when>
              <xsl:otherwise>
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags">
                    <xsl:with-param name="type" select="'note'"/>
                  </xsl:apply-templates>
                    <xsl:choose>
                      <!-- for hazardstatement -->
                      <xsl:when test="contains(@class,' hazard-d/hazardstatement ')">
                        <xsl:call-template name="create_hazards_content"/>
                      </xsl:when>
                      <xsl:otherwise>
                        <xsl:call-template name="create_note_content"/>
                      </xsl:otherwise>
                    </xsl:choose>
                    <!-- end add flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-flags">
                      <xsl:with-param name="type" select="'note'"/>
                    </xsl:apply-templates>
                </xsl:element>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:element>
        </xsl:when>
        <!-- for other tags -->
        <xsl:otherwise>
          <xsl:element name="text:span">
            <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'note'"/>
            </xsl:apply-templates>
            <xsl:choose>
              <!-- for hazardstatement -->
              <xsl:when test="contains(@class,' hazard-d/hazardstatement ')">
                <xsl:call-template name="create_hazards_content"/>
              </xsl:when>
              <xsl:otherwise>
                  <xsl:call-template name="create_note_content"/>
              </xsl:otherwise>
            </xsl:choose>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'note'"/>
            </xsl:apply-templates>
            </xsl:element>
          </xsl:element>
          <xsl:element name="text:line-break"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="create_note_content">
  <xsl:choose>
    <xsl:when test="@type='note' or not(@type)">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Note'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:if
        test="ancestor::*[contains(@class,' topic/table ') 
      or contains(@class,' topic/simpletable ')]">
        <xsl:element name="text:tab"/>
      </xsl:if>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='tip'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Tip'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='fastpath'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Fastpath'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='important'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Important'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='remember'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Remember'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='restriction'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Restriction'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='attention'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Attention'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='caution'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Caution'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='danger'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Danger'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:when test="@type='other'">
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:choose>
          <xsl:when test="@othertype and
          not(@othertype='')">
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="@othertype"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>[other]</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        
      </xsl:element>
      <xsl:element name="text:span">
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="create_hazards_content">

  <xsl:choose>
    <xsl:when test="@type='note'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Note'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:if
          test="ancestor::*[contains(@class,' topic/table ') 
          or contains(@class,' topic/simpletable ')]">
          <xsl:element name="text:tab"/>
        </xsl:if>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='tip'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Tip'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='fastpath'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Fastpath'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='important'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Important'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='remember'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Remember'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='restriction'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Restriction'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='attention'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Attention'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='caution'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Caution'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    
    <xsl:when test="@type='danger'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'Danger'"/>
        </xsl:call-template>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>

    <xsl:when test="@type='other'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:choose>
          <xsl:when test="@othertype and
          not(@othertype='')">
            <xsl:call-template name="get-ascii">
              <xsl:with-param name="txt">
                <xsl:value-of select="@othertype"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>[other]</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="getStringODT">
          <xsl:with-param name="stringName" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:element name="text:span">
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:apply-templates/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="gen-txt1">
  <xsl:param name="txt"/>
  <xsl:choose>
    <xsl:when test="not(contains($txt,'\'))">
      <xsl:call-template name="gen-txt2">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="gen-txt2">
        <xsl:with-param name="txt" select="substring-before($txt,'\')"/>
      </xsl:call-template>
      <xsl:text>\</xsl:text>
      <xsl:call-template name="gen-txt1">
        <xsl:with-param name="txt" select="substring-after($txt,'\')"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="gen-txt2">
  <xsl:param name="txt"/>
  <xsl:choose>
    <xsl:when test="not(contains($txt,'{'))">
      <xsl:call-template name="gen-txt3">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="gen-txt3">
        <xsl:with-param name="txt" select="substring-before($txt,'{')"/>
      </xsl:call-template>
      <xsl:text>{</xsl:text>
      <xsl:call-template name="gen-txt2">
        <xsl:with-param name="txt" select="substring-after($txt,'{')"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="gen-txt3">
  <xsl:param name="txt"/>
  <xsl:choose>
    <xsl:when test="not(contains($txt,'}'))">
      <xsl:call-template name="gen-txt">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="gen-txt">
        <xsl:with-param name="txt" select="substring-before($txt,'}')"/>
      </xsl:call-template>
      <xsl:text>}</xsl:text>
      <xsl:call-template name="gen-txt3">
        <xsl:with-param name="txt" select="substring-after($txt,'}')"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template name="gen-txt">
  <xsl:param name="txt"/>
  <!-- avoid duplication. -->
  <xsl:param name="isFirst">true</xsl:param>
  <xsl:variable name="newline">
    <xsl:text>
</xsl:text>
  </xsl:variable>
  <xsl:choose>
    <xsl:when test="contains($txt,$newline)">
      <xsl:value-of select="substring-before($txt,$newline)"/>
      <xsl:element name="text:line-break"/>
      <xsl:call-template name="gen-txt">
        <xsl:with-param name="txt" select="substring-after($txt,$newline)"/>
        <xsl:with-param name="isFirst">false</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:call-template name="get-ascii">
        <xsl:with-param name="txt" select="$txt"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()">
  
  <xsl:variable name="style_name">
    <xsl:call-template name="get_style_name"/> 
  </xsl:variable>
  
  <xsl:variable name="trueStyleName">
    <xsl:value-of select="styleUtils:getHiStyleName($style_name)"/>
  </xsl:variable>
  
  <xsl:choose>
    <!-- parent is entry, stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')] or
                    parent::*[contains(@class, ' topic/stentry ')]">
  
        <xsl:choose>
          <xsl:when test="ancestor::*[contains(@class, ' topic/thead ')] or 
            ancestor::*[contains(@class, ' topic/sthead')]">
            <xsl:element name="text:span">
              <!-- 
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              -->
              <xsl:call-template name="start_flagging_text_of_table_or_list"/>
              <xsl:element name="text:span">
                <xsl:if test="$trueStyleName!=''">
                  <xsl:attribute name="text:style-name">
                    <xsl:value-of select="$trueStyleName"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="gen_txt_content"/>
              </xsl:element>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <xsl:call-template name="start_flagging_text_of_table_or_list"/>
              
              <xsl:element name="text:span">
                <xsl:if test="$trueStyleName!=''">
                  <xsl:attribute name="text:style-name">
                    <xsl:value-of select="$trueStyleName"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="gen_txt_content"/>
              </xsl:element>
              
              <xsl:call-template name="end_flagging_text_of_table_or_list"/>
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
     
    </xsl:when>
    <!-- parent is li, sli add p tag otherwise text is invaild. -->
    <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')] or 
      parent::*[contains(@class, ' topic/sli ')]">
      <xsl:element name="text:p">
        <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>

          <xsl:element name="text:span">
            <xsl:call-template name="start_flagging_text_of_table_or_list"/>
            
            <xsl:element name="text:span">
              <xsl:if test="$trueStyleName!=''">
                <xsl:attribute name="text:style-name">
                  <xsl:value-of select="$trueStyleName"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:call-template name="gen_txt_content"/>
            </xsl:element>
            
            <xsl:call-template name="end_flagging_text_of_table_or_list"/>
          </xsl:element>
      </xsl:element>
      
    </xsl:when>
    <!-- text is not allowed under these tags -->
    <xsl:when test="parent::*[contains(@class,' topic/ul ')] | parent::*[contains(@class,' topic/ol ')]
      | parent::*[contains(@class,' topic/sl ')] | parent::*[contains(@class,' topic/dl ')]"/>
    <!-- for other tags -->
    <xsl:otherwise>
      <xsl:element name="text:span">
        <xsl:if test="$trueStyleName!=''">
          <xsl:choose>
            <!-- title case -->
            <xsl:when test="ancestor::*[contains(@class, ' topic/title ')][1]
              /parent::*[contains(@class, ' topic/topic ')]">
              <xsl:choose>
                <!-- keep font size with the title -->
                <!-- 
                <xsl:when test="contains($trueStyleName, 'italic')">
                  <xsl:variable name="depth" select="count(ancestor::*[contains(@class, ' topic/topic ')])"/>
                  <xsl:attribute name="text:style-name">
                    <xsl:value-of select="concat('italic', '_heading_', $depth)"/>
                  </xsl:attribute>
                </xsl:when>
                -->
                <xsl:when test="$trueStyleName = 'bold'"/>
                <!-- other style is okay -->
                <xsl:otherwise>
                  <xsl:attribute name="text:style-name">
                    <xsl:value-of select="$trueStyleName"/>
                  </xsl:attribute>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:when>
            <!-- other case -->
            <xsl:otherwise>
              <xsl:attribute name="text:style-name">
                <xsl:value-of select="$trueStyleName"/>
              </xsl:attribute>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
        <xsl:call-template name="gen_txt_content"/>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>
  
<xsl:template name="gen_txt_content">
  <xsl:choose>
    <xsl:when
      test="ancestor::*[contains(@class,' topic/pre ')]">
      <xsl:call-template name="gen-txt">
        <xsl:with-param name="txt" select="."/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="starts-with(.,' ') and not(normalize-space(.)='')">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:call-template name="get-ascii">
        <xsl:with-param name="txt">
          <xsl:value-of select="normalize-space(.)"/>
        </xsl:with-param>
      </xsl:call-template>
      <xsl:if test="substring(.,string-length(.))=' ' and not(normalize-space(.)='')">
        <xsl:text> </xsl:text>
      </xsl:if>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/boolean ')]">
  
  <xsl:variable name="style_name">
    <xsl:call-template name="get_style_name"/> 
  </xsl:variable>
  
  <xsl:variable name="trueStyleName">
    <xsl:value-of select="styleUtils:getHiStyleName($style_name)"/>
  </xsl:variable>
  
  <xsl:choose>
    <!-- parent is li -->
    <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">boolean_style</xsl:attribute>
          <xsl:text>boolean:</xsl:text>
          <xsl:value-of select="@state"/>
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
        
        <!-- cell belongs to thead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
            /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">boolean_style</xsl:attribute>
                <xsl:text>boolean:</xsl:text>
                <xsl:value-of select="@state"/>
              </xsl:element>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">boolean_style</xsl:attribute>
              <xsl:text>boolean:</xsl:text>
              <xsl:value-of select="@state"/>
            </xsl:element>
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
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">boolean_style</xsl:attribute>
                <xsl:text>boolean:</xsl:text>
                <xsl:value-of select="@state"/>
              </xsl:element>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">boolean_style</xsl:attribute>
              <xsl:text>boolean:</xsl:text>
              <xsl:value-of select="@state"/>
            </xsl:element>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:element>
    </xsl:when>
    <!-- nested by other tags -->
    <xsl:otherwise>
      <xsl:element name="text:span">
        <xsl:choose>
          <xsl:when test="$trueStyleName!=''">
            <xsl:attribute name="text:style-name">
              <xsl:value-of select="concat('boolean_', $trueStyleName)"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="text:style-name">boolean_style</xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>boolean:</xsl:text>
        <xsl:value-of select="@state"/>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  

  <xsl:template match="*[contains(@class,' topic/state ')]">
    
    <xsl:variable name="style_name">
      <xsl:call-template name="get_style_name"/> 
    </xsl:variable>
    
    <xsl:variable name="trueStyleName">
      <xsl:value-of select="styleUtils:getHiStyleName($style_name)"/>
    </xsl:variable>
    
    <xsl:element name="text:span">
      <xsl:if test="$trueStyleName!=''">
        <xsl:attribute name="text:style-name">
          <xsl:value-of select="$trueStyleName"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:element name="text:span">
        <xsl:attribute name="text:style-name">state_style</xsl:attribute>
        <xsl:value-of select="name()"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>=</xsl:text>
        <xsl:value-of select="@value"/>
      </xsl:element>
    </xsl:element>
    
  </xsl:template>
  
<!-- itemgroup tag -->
<xsl:template match="*[contains(@class, ' topic/itemgroup ')]">
  
  
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' topic/dd ')]">
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
      </xsl:element>
    </xsl:when>
    
    <xsl:when test="parent::*[contains(@class, ' topic/li')]">
      <xsl:element name="text:p">
        <xsl:element name="text:span">
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
        </xsl:element>
      </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<!-- Indexterm tag -->
<xsl:template match="*[contains(@class, ' topic/indexterm ')]">
  
  <xsl:if test="$INDEXSHOW='yes'">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:call-template name="create_indexterm_content"/>
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
                  <xsl:call-template name="create_indexterm_content"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_indexterm_content"/>
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
                <xsl:call-template name="create_indexterm_content"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="create_indexterm_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:call-template name="create_indexterm_content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<xsl:template name="create_indexterm_content">
  <xsl:variable name="indexId">
    <xsl:value-of select="concat('IMark', generate-id(.))"/>
  </xsl:variable>
  <xsl:variable name="depth">
    <xsl:value-of select="count(ancestor-or-self::*[contains(@class,' topic/indexterm ')])"/>
  </xsl:variable>
  <xsl:element name="text:user-index-mark-start">
    <xsl:attribute name="text:id">
      <xsl:value-of select="$indexId"/>
    </xsl:attribute>
    <xsl:attribute name="text:index-name">
      <xsl:value-of select="'user-defined'"/>
    </xsl:attribute>
    <xsl:attribute name="text:outline-level">
      <xsl:value-of select="$depth"/>
    </xsl:attribute>
  </xsl:element>
  <xsl:apply-templates/>
  <xsl:element name="text:user-index-mark-end">
    <xsl:attribute name="text:id">
      <xsl:value-of select="$indexId"/>
    </xsl:attribute>
  </xsl:element>
</xsl:template>

  <xsl:template match="*[contains(@class,' topic/tm ')]">
    
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:call-template name="create_tm_content"/>
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
                <xsl:call-template name="create_tm_content"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_tm_content"/>
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
                <xsl:call-template name="create_tm_content"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_tm_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:call-template name="create_tm_content"/>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template name="create_tm_content">
    
    <xsl:element name="text:span">
      <xsl:apply-templates/>
    </xsl:element>
    <xsl:choose>
      <xsl:when test="@tmtype='tm'">
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">sup</xsl:attribute>
          <xsl:text>(TM)</xsl:text>
        </xsl:element>
      </xsl:when>
      <xsl:when test="@tmtype='service'">
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">sup</xsl:attribute>
          <xsl:text>(SM)</xsl:text>
        </xsl:element>
      </xsl:when>
      <xsl:when test="@tmtype='reg'">
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">sup</xsl:attribute>
          <xsl:text>(R)</xsl:text>
        </xsl:element>
      </xsl:when>
    </xsl:choose>
    
  </xsl:template>

<xsl:template match="*[contains(@class,' topic/draft-comment ')]">
  <xsl:if test="$DRAFT='yes'">
    <xsl:apply-templates select="." mode="ditamsg:draft-comment-in-content"/>
    <xsl:choose>
      <!-- parent is p or list -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:attribute name="text:style-name">draftcomment_paragraph</xsl:attribute>
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Draft comment'"/>
              </xsl:call-template>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'ColonSymbol'"/>
              </xsl:call-template>
              <xsl:if test="@author">
                <xsl:value-of select="@author"/><xsl:text> </xsl:text>
              </xsl:if>
              <xsl:if test="@disposition">
                <xsl:value-of select="@disposition"/><xsl:text> </xsl:text>
              </xsl:if>
              <xsl:if test="@time">
                <xsl:value-of select="@time"/>
              </xsl:if>
              <xsl:element name="text:line-break"/>
            </xsl:element>
            <xsl:apply-templates/>
            
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          
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
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  
                  <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">bold</xsl:attribute>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'Draft comment'"/>
                    </xsl:call-template>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                    </xsl:call-template>
                    <xsl:if test="@author">
                      <xsl:value-of select="@author"/><xsl:text> </xsl:text>
                    </xsl:if>
                    <xsl:if test="@disposition">
                      <xsl:value-of select="@disposition"/><xsl:text> </xsl:text>
                    </xsl:if>
                    <xsl:if test="@time">
                      <xsl:value-of select="@time"/>
                    </xsl:if>
                    <xsl:element name="text:line-break"/>
                  </xsl:element>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>  
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                
                <xsl:element name="text:span">
                  <xsl:attribute name="text:style-name">bold</xsl:attribute>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'Draft comment'"/>
                  </xsl:call-template>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                  </xsl:call-template>
                  <xsl:if test="@author">
                    <xsl:value-of select="@author"/><xsl:text> </xsl:text>
                  </xsl:if>
                  <xsl:if test="@disposition">
                    <xsl:value-of select="@disposition"/><xsl:text> </xsl:text>
                  </xsl:if>
                  <xsl:if test="@time">
                    <xsl:value-of select="@time"/>
                  </xsl:if>
                  <xsl:element name="text:line-break"/>
                </xsl:element>
                <xsl:apply-templates/>
                
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	  
              </xsl:element>
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
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  
                  <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">bold</xsl:attribute>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'Draft comment'"/>
                    </xsl:call-template>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                    </xsl:call-template>
                    <xsl:if test="@author">
                      <xsl:value-of select="@author"/><xsl:text> </xsl:text>
                    </xsl:if>
                    <xsl:if test="@disposition">
                      <xsl:value-of select="@disposition"/><xsl:text> </xsl:text>
                    </xsl:if>
                    <xsl:if test="@time">
                      <xsl:value-of select="@time"/>
                    </xsl:if>
                    <xsl:element name="text:line-break"/>
                  </xsl:element>
                  <xsl:apply-templates/>
                  
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/> 
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                
                <xsl:element name="text:span">
                  <xsl:attribute name="text:style-name">bold</xsl:attribute>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'Draft comment'"/>
                  </xsl:call-template>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                  </xsl:call-template>
                  <xsl:if test="@author">
                    <xsl:value-of select="@author"/><xsl:text> </xsl:text>
                  </xsl:if>
                  <xsl:if test="@disposition">
                    <xsl:value-of select="@disposition"/><xsl:text> </xsl:text>
                  </xsl:if>
                  <xsl:if test="@time">
                    <xsl:value-of select="@time"/>
                  </xsl:if>
                  <xsl:element name="text:line-break"/>
                </xsl:element>
                <xsl:apply-templates/>
                
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	  
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- parent is other tag -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'Draft comment'"/>
              </xsl:call-template>
              <xsl:call-template name="getStringODT">
                <xsl:with-param name="stringName" select="'ColonSymbol'"/>
              </xsl:call-template>
              <xsl:if test="@author">
                <xsl:value-of select="@author"/><xsl:text> </xsl:text>
              </xsl:if>
              <xsl:if test="@disposition">
                <xsl:value-of select="@disposition"/><xsl:text> </xsl:text>
              </xsl:if>
              <xsl:if test="@time">
                <xsl:value-of select="@time"/>
              </xsl:if>
              <xsl:element name="text:line-break"/>
            </xsl:element>
            <xsl:apply-templates/>
            
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </xsl:element>
          <xsl:element name="text:line-break"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>
  
<xsl:template match="*[contains(@class,' topic/required-cleanup ')]">
  <xsl:if test="$DRAFT='yes'">
    <xsl:apply-templates select="." mode="ditamsg:required-cleanup-in-content"/>
    <xsl:choose>
      <!-- parent is p or list -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
            <xsl:element name="text:span">
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags"/>
              
                <xsl:element name="text:span">
                  <xsl:attribute name="text:style-name">bold</xsl:attribute>
                  <xsl:text>[</xsl:text>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'Required cleanup'"/>
                  </xsl:call-template>
                  <xsl:text>]</xsl:text>
                  <xsl:if test="string(@remap)">
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="@remap"/>
                    <xsl:text>)</xsl:text>
                  </xsl:if>
                  <xsl:call-template name="getStringODT">
                    <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                  </xsl:call-template>
                  <xsl:text> </xsl:text>
                </xsl:element>
                <xsl:apply-templates/>
              
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
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
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  
                    <xsl:element name="text:span">
                      <xsl:attribute name="text:style-name">bold</xsl:attribute>
                      <xsl:text>[</xsl:text>
                      <xsl:call-template name="getStringODT">
                        <xsl:with-param name="stringName" select="'Required cleanup'"/>
                      </xsl:call-template>
                      <xsl:text>]</xsl:text>
                      <xsl:if test="string(@remap)">
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="@remap"/>
                        <xsl:text>)</xsl:text>
                      </xsl:if>
                      <xsl:call-template name="getStringODT">
                        <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                      </xsl:call-template>
                      <xsl:text> </xsl:text>
                    </xsl:element>
                    <xsl:apply-templates/>
                  
                  
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                
                  <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">bold</xsl:attribute>
                    <xsl:text>[</xsl:text>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'Required cleanup'"/>
                    </xsl:call-template>
                    <xsl:text>]</xsl:text>
                    <xsl:if test="string(@remap)">
                      <xsl:text>(</xsl:text>
                      <xsl:value-of select="@remap"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                    </xsl:call-template>
                    <xsl:text> </xsl:text>
                  </xsl:element>
                  <xsl:apply-templates/>
                
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
              </xsl:element>
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
                <xsl:element name="text:span">
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  
                    <xsl:element name="text:span">
                      <xsl:attribute name="text:style-name">bold</xsl:attribute>
                      <xsl:text>[</xsl:text>
                      <xsl:call-template name="getStringODT">
                        <xsl:with-param name="stringName" select="'Required cleanup'"/>
                      </xsl:call-template>
                      <xsl:text>]</xsl:text>
                      <xsl:if test="string(@remap)">
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="@remap"/>
                        <xsl:text>)</xsl:text>
                      </xsl:if>
                      <xsl:call-template name="getStringODT">
                        <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                      </xsl:call-template>
                      <xsl:text> </xsl:text>
                    </xsl:element>
                    <xsl:apply-templates/>
                  
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                
                  <xsl:element name="text:span">
                    <xsl:attribute name="text:style-name">bold</xsl:attribute>
                    <xsl:text>[</xsl:text>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'Required cleanup'"/>
                    </xsl:call-template>
                    <xsl:text>]</xsl:text>
                    <xsl:if test="string(@remap)">
                      <xsl:text>(</xsl:text>
                      <xsl:value-of select="@remap"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                    <xsl:call-template name="getStringODT">
                      <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                    </xsl:call-template>
                    <xsl:text> </xsl:text>
                  </xsl:element>
                  <xsl:apply-templates/>
                
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- parent is other tag -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:element name="text:span">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <!-- 
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">required_cleanup_style</xsl:attribute>
            -->
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:text>[</xsl:text>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'Required cleanup'"/>
                </xsl:call-template>
                <xsl:text>]</xsl:text>
                <xsl:if test="string(@remap)">
                  <xsl:text>(</xsl:text>
                  <xsl:value-of select="@remap"/>
                  <xsl:text>)</xsl:text>
                </xsl:if>
                <xsl:call-template name="getStringODT">
                  <xsl:with-param name="stringName" select="'ColonSymbol'"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
              </xsl:element>
              <xsl:apply-templates/>
            <!-- 
            </xsl:element>
            -->
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </xsl:element>
          <xsl:element name="text:line-break"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

  <xsl:template match="*[contains(@class, ' topic/keywords ')]">
    
    <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">indent_paragraph_style</xsl:attribute>
      <xsl:apply-templates/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/term ')]">
    
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:element name="text:span">
            <xsl:attribute name="text:style-name">italic</xsl:attribute>
            <xsl:element name="text:span">
              <!-- start add rev flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags">
                <xsl:with-param name="type" select="'keyword'"/>
              </xsl:apply-templates>
              <xsl:apply-templates/>
              <!-- end add rev flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags">
                <xsl:with-param name="type" select="'keyword'"/>
              </xsl:apply-templates>
            </xsl:element>
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
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">bold</xsl:attribute>
                <xsl:element name="text:span">
                  <xsl:attribute name="text:style-name">italic</xsl:attribute>
                  <xsl:element name="text:span">
                    <!-- start add rev flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                    <xsl:apply-templates/>
                    <!-- end add rev flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                  </xsl:element>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">italic</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:apply-templates/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </xsl:element>
              </xsl:element>
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
                <xsl:element name="text:span">
                  <xsl:attribute name="text:style-name">italic</xsl:attribute>
                  <xsl:element name="text:span">
                    <!-- start add rev flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                    <xsl:apply-templates/>
                    <!-- end add rev flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                  </xsl:element>
                </xsl:element>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:element name="text:span">
                <xsl:attribute name="text:style-name">italic</xsl:attribute>
                <xsl:element name="text:span">
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:apply-templates/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </xsl:element>
              </xsl:element>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:attribute name="text:style-name">italic</xsl:attribute>
          <xsl:element name="text:span">
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
          </xsl:element>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/fn ')]">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <xsl:element name="text:p">
          <xsl:call-template name="create_fn_content"/>
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
                <xsl:call-template name="create_fn_content"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_fn_content"/>
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
                <xsl:call-template name="create_fn_content"/>
              </xsl:element>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_fn_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:element>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:call-template name="create_fn_content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="create_fn_content">
    
    <xsl:choose>
      <xsl:when test="not(@id)">
        <xsl:choose>
          <xsl:when test="@callout and not(@callout = '')">
            <xsl:element name="text:note">
              <xsl:attribute name="text:note-class">footnote</xsl:attribute>
              <xsl:element name="text:note-citation">
                <xsl:attribute name="text:label">
                  <xsl:value-of select="@callout"/>
                </xsl:attribute>
                <xsl:value-of select="@callout"/>
              </xsl:element>
              <xsl:element name="text:note-body">
                <xsl:element name="text:p">
                  <xsl:attribute name="text:style-name">footnote</xsl:attribute>
                  <xsl:element name="text:span">
                    <!-- start add flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    <xsl:apply-templates/>
                    <!-- end add flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                  </xsl:element>
                </xsl:element>
              </xsl:element>
            </xsl:element>
          </xsl:when>
          <xsl:otherwise>
            <!-- should be updated -->
            <!-- 
              <xsl:variable name="fnNumber" 
              select="count(preceding::*[contains(@class, ' topic/fn ')]) + 1"/>
            -->
            <xsl:variable name="fnNumber">
              <xsl:number from="/" level="any"/>
            </xsl:variable>
            <xsl:element name="text:note">
              <xsl:attribute name="text:note-class">footnote</xsl:attribute>
              <xsl:element name="text:note-citation">
                <xsl:value-of select="$fnNumber"/>
              </xsl:element>
              <xsl:element name="text:note-body">
                <xsl:element name="text:p">
                  <xsl:attribute name="text:style-name">footnote</xsl:attribute>
                  <xsl:element name="text:span">
                    <!-- start add flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    
                    <xsl:apply-templates/>
                    
                    <!-- end add flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                  </xsl:element>
                </xsl:element>
              </xsl:element>
            </xsl:element>  
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>  
  </xsl:template>

  <!-- Add for "New <data> element (#9)" in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/data ')]"/>

  <!-- data-about tag -->
  <xsl:template match="*[contains(@class, ' topic/data-about ')]">
    <xsl:choose>
      <!-- nested by body or list -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
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
      <!-- for other tags -->
      <xsl:otherwise>
        <xsl:element name="text:span">
          <xsl:apply-templates/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- Add for "Support foreign content vocabularies such as 
  MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]"/>
  
  

</xsl:stylesheet>
