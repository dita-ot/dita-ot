<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
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
  exclude-result-prefixes="xs styleUtils ditamsg"
  version="2.0">
  
  <xsl:output method="xml"/>
  <xsl:output indent="yes"/>
  <xsl:strip-space elements="*"/>

  <!-- Deprecated since 2.3 -->
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
  <xsl:variable name="afill"/>
  
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
        <text:p>
          <xsl:apply-templates/>
        </text:p>
      </xsl:when>
      <xsl:otherwise>
        <text:span>
          <xsl:apply-templates/>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
    <xsl:variable name="depth" select="count(ancestor::*[contains(@class,' topic/topic ')])" as="xs:integer"/>
    <xsl:call-template name="block-title">
      <xsl:with-param name="depth" select="$depth"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="synsect"/>

  <xsl:template match="*[contains(@class,' topic/section ')]">
    <xsl:choose>
      <!-- nested by body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <text:p text:style-name="indent_paragraph_style">
          <text:line-break/>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <!-- if has title tag -->
            <xsl:if test="child::*[contains(@class, ' topic/title ')]">
              <xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="render_section_title"/>
            </xsl:if>
            <xsl:apply-templates select="*[not(contains(@class,' topic/title '))] | text() | comment() | processing-instruction()"/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </text:span>
        </text:p> 
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <text:span>
          <text:line-break/>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/sectiondiv ')]">
    <text:span>
      <xsl:apply-templates/>
    </text:span>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/title ')]" mode="render_section_title">
    <xsl:variable name="headCount" select="count(ancestor::*[contains(@class,' topic/topic ')])+1"/>
    <!-- Heading_20_2 -->
    <text:span text:style-name="bold">
      <!-- 
      <xsl:attribute name="text:style-name"><xsl:value-of select="concat('Heading_20_' , $headCount)"/></xsl:attribute>
      -->
      <xsl:apply-templates/>
    </text:span>
    <text:line-break/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/section ')]/*[contains(@class,' topic/note ')]" priority="1">
    <text:span>
      <xsl:apply-templates/>
    </text:span>
    <text:line-break/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/example ')]">
    <xsl:choose>
      <!-- parent is body -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')]">
        <text:p>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>		
        </text:p>
      </xsl:when>
      <!-- nested by other tags. -->
      <xsl:otherwise>
        <text:span>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>        
        </text:span>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]">
    <text:span text:style-name="bold">
      <xsl:apply-templates/>
    </text:span>
    <text:line-break/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/fig ')]">
    <xsl:choose>
      <!-- parent is body, li... -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or 
        parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <text:span>
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]
              /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/
              parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- other tags -->
      <xsl:otherwise>
        <text:span>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/figgroup ')]">
    <text:span>
      <text:span>
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>
      </text:span>
    </text:span>
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
          <xsl:when test="((string-length($ancestorlang) = 5 and contains($ancestorlang,'hu-hu')) or
                           (string-length($ancestorlang) = 2 and contains($ancestorlang,'hu')) )">
            <xsl:value-of select="$fig-count-actual"/>
            <xsl:text>. </xsl:text>
            <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Figure'"/>
            </xsl:call-template><xsl:text> </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Figure'"/>
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
    <text:span>
      <xsl:apply-templates/>
    </text:span>
    <text:line-break/>
  </xsl:template>  

  <!-- =========== block things ============ -->

  <xsl:template match="*[contains(@class,' topic/p ')]">
    <xsl:choose>
      <!-- nested by body or list -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <text:p text:style-name="indent_paragraph_style">
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </text:span>            
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
              <!-- cell belongs to thead -->
              <xsl:choose>
                <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                  <text:span text:style-name="bold">
                    <xsl:apply-templates/>
                  </text:span>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:apply-templates/>
                </xsl:otherwise>
              </xsl:choose>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
                <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
              </text:span>	
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <!-- imitate a paragraph -->
        <text:line-break/>
        <text:span>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/ph ')]">
    <xsl:choose>
      <!-- nested by list -->
      <xsl:when test=" parent::*[contains(@class, ' topic/li ')] or
                       parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>            
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <!-- cell belongs to thead -->
            <xsl:choose>
              <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                <text:span text:style-name="bold">
                  <xsl:apply-templates/>
                </text:span>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates/>
              </xsl:otherwise>
            </xsl:choose>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
              <text:span>
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:apply-templates/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>	
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>	
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:apply-templates/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <text:span>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/keyword ')]">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates> 
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags">
              <xsl:with-param name="type" select="'keyword'"/>
            </xsl:apply-templates>
          </text:span>
        </text:p>
      </xsl:when>
      <xsl:otherwise>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags">
            <xsl:with-param name="type" select="'keyword'"/>
          </xsl:apply-templates>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags">
            <xsl:with-param name="type" select="'keyword'"/>
          </xsl:apply-templates>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- lines tag -->
  <xsl:template match="*[contains(@class,' topic/lines ')]">
    <xsl:choose>
      <!-- nested by body, li -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <text:p>
          <text:span>
            <!-- add flagging styles -->
            <xsl:apply-templates select="." mode="add-odt-flagging"/>
            <xsl:apply-templates/>
          </text:span>
        </text:p>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="add-odt-flagging"/>
                <xsl:apply-templates/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- add flagging styles -->
                <xsl:apply-templates select="." mode="add-odt-flagging"/>
                <xsl:apply-templates/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- other tags -->      
      <xsl:otherwise>
        <text:span>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
          <text:line-break/>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/pre ')]">

    <xsl:choose>
      <!-- nested by body, li -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <text:p text:style-name="Code_Style_Paragraph">
          <text:span>
            <!-- add flagging styles -->
            <xsl:apply-templates select="." mode="add-odt-flagging"/>
            <xsl:apply-templates/>
          </text:span>
        </text:p>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- add flagging styles -->
                <xsl:apply-templates select="." mode="add-odt-flagging"/>
                <xsl:apply-templates/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <!-- start add flagging images -->  
        <xsl:apply-templates select="." mode="start-add-odt-imgrevflags"/>
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                  <!-- add flagging styles -->
                  <xsl:apply-templates select="." mode="add-odt-flagging"/>
                  <xsl:apply-templates/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
        <!-- end add flagging images -->
        <xsl:apply-templates select="." mode="end-add-odt-imgrevflags"/>
      </xsl:when>
      <!-- other tags -->
      <xsl:otherwise>
        <text:span text:style-name="Code_Text">
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
        </text:span>
        <text:line-break/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- phrases -->
  <xsl:template match="*[contains(@class,' hi-d/tt ')]">
    <text:span text:style-name="bold">
      <xsl:apply-templates/>
    </text:span>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/q ')]">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:call-template name="create_q_content"/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:call-template name="create_q_content"/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:call-template name="create_q_content"/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:call-template name="create_q_content"/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add rev flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                <xsl:call-template name="create_q_content"/>
                <!-- end add rev flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:call-template name="create_q_content"/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="create_q_content">
    <xsl:variable name="style_name">
      <xsl:call-template name="get_style_name"/> 
    </xsl:variable>
    <xsl:variable name="trueStyleName" select="styleUtils:getHiStyleName($style_name)"/>
    <text:span>
      <xsl:if test="$trueStyleName!=''">
        <xsl:attribute name="text:style-name">
          <xsl:value-of select="$trueStyleName"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'OpenQuote'"/>
      </xsl:call-template>
      <xsl:apply-templates/>
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'CloseQuote'"/>
      </xsl:call-template>
    </text:span>
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
  <xsl:param name="depth" as="xs:integer"/>
  <text:h text:outline-level="{$depth}">
    <xsl:choose>
      <xsl:when test="$depth = 1">
        <xsl:attribute name="text:style-name">Heading_20_1</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth = 2 ">
        <xsl:attribute name="text:style-name">Heading_20_2</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth = 3">
        <xsl:attribute name="text:style-name">Heading_20_3</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth = 4">
        <xsl:attribute name="text:style-name">Heading_20_4</xsl:attribute>
        <!-- create start bookmark -->
        <xsl:call-template name="gen-bookmark">
          <xsl:with-param name="flag" select="0"/>
        </xsl:call-template>
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:when test="$depth = 5">
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
  </text:h>
</xsl:template>
  
  
<!-- font-weight="bold" -->
<xsl:template name="inline-em">
  <text:span text:style-name="bold">
    <xsl:apply-templates/>
  </text:span>
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

  <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]"/>
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
    <xsl:when test="parent::*[contains(@class, ' topic/body ')] or                      parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <text:p>
        <text:span>
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <xsl:call-template name="create_lq_content">
            <xsl:with-param name="samefile" select="$samefile"/>
            <xsl:with-param name="href-value" select="$href-value"/>
          </xsl:call-template>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>
        </text:span>
      </text:p>
    </xsl:when>
    <!-- nested by entry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
      <!-- create p tag -->
      <text:p>
        <!-- alignment styles -->
        <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
          <xsl:call-template name="set_align_value"/>
        </xsl:if>
        <!-- cell belongs to thead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]             /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
            <text:span text:style-name="bold">
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <xsl:call-template name="create_lq_content">
                  <xsl:with-param name="samefile" select="$samefile"/>
                  <xsl:with-param name="href-value" select="$href-value"/>
                </xsl:call-template>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span>
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags"/>
              <xsl:apply-templates/>
              <xsl:call-template name="create_lq_content">
                <xsl:with-param name="samefile" select="$samefile"/>
                <xsl:with-param name="href-value" select="$href-value"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </text:span>	
          </xsl:otherwise>
        </xsl:choose>
      </text:p>
    </xsl:when>
    <!-- nested by stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
      <text:p>
        <!-- cell belongs to sthead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/             parent::*[contains(@class, ' topic/sthead ')]">
            <text:span text:style-name="bold">
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <xsl:apply-templates/>
                <xsl:call-template name="create_lq_content">
                  <xsl:with-param name="samefile" select="$samefile"/>
                  <xsl:with-param name="href-value" select="$href-value"/>
                </xsl:call-template>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>
              </text:span>
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span>
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags"/>
              <xsl:apply-templates/>
              <xsl:call-template name="create_lq_content">
                <xsl:with-param name="samefile" select="$samefile"/>
                <xsl:with-param name="href-value" select="$href-value"/>
              </xsl:call-template>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>
            </text:span>
          </xsl:otherwise>
        </xsl:choose>
      </text:p>
    </xsl:when>
    <!-- other tags -->
    <xsl:otherwise>
      <text:span>
        <text:span>
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <xsl:call-template name="create_lq_content">
            <xsl:with-param name="samefile" select="$samefile"/>
            <xsl:with-param name="href-value" select="$href-value"/>
          </xsl:call-template>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>
        </text:span>
      </text:span>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<xsl:template name="create_lq_content">
  <xsl:param name="samefile" select="''"/>
  <xsl:param name="href-value" select="''"/>
  
  <xsl:choose>
    <xsl:when test="@href and not(@href='')"> 
      <text:line-break/>
      <!-- Insert citation as link, use @href as-is -->
      <text:span text:style-name="right">
        <text:a>
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
            <xsl:when test="@reftitle">
              <xsl:value-of select="@reftitle"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@href"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:a>
      </text:span>
    </xsl:when>
    <xsl:when test="@reftitle and not(@reftitle='')">
      <text:line-break/>
      <!-- Insert citation text -->
      <text:span>
        <xsl:value-of select="@reftitle"/>
      </text:span>
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
        <text:bookmark-start text:name="{$id}">
        </text:bookmark-start>
      </xsl:when>
      <!-- otherwise create bookmark-end -->
      <xsl:otherwise>
        <text:bookmark-end text:name="{$id}">
        </text:bookmark-end>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/cite ')]">
  
  <xsl:choose>
    <!-- parent is list -->
    <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <text:p>
        <text:span text:style-name="italic">
          <xsl:apply-templates/>
        </text:span>  
      </text:p>
    </xsl:when>
    <!-- nested by entry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
      <!-- create p tag -->
      <text:p>
        <!-- alignment styles -->
        <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
          <xsl:call-template name="set_align_value"/>
        </xsl:if>
        <!-- cell belongs to thead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]             /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
            <text:span text:style-name="bold">
              <text:span text:style-name="italic">
                <xsl:apply-templates/>
              </text:span>  
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span text:style-name="italic">
              <xsl:apply-templates/>
            </text:span>  
          </xsl:otherwise>
        </xsl:choose>
      </text:p>
    </xsl:when>
    <!-- nested by stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
      <text:p>
        <!-- cell belongs to sthead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/             parent::*[contains(@class, ' topic/sthead ')]">
            <text:span text:style-name="bold">
              <text:span text:style-name="italic">
                <xsl:apply-templates/>
              </text:span>  
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span text:style-name="italic">
              <xsl:apply-templates/>
            </text:span>  
          </xsl:otherwise>
        </xsl:choose>
      </text:p>
    </xsl:when>
    <!-- parent is other tags -->
    <xsl:otherwise>
      <text:span text:style-name="italic">
        <xsl:apply-templates/>
      </text:span>
    </xsl:otherwise>
  </xsl:choose>
  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/desc ')]" priority="0">
  <text:span>
    <!-- 
    <xsl:apply-templates select="text()"/>
    -->
    <xsl:apply-templates/>
  </text:span>
  <!-- 
  <xsl:apply-templates select="child::*[@class]"/>
  -->
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]"/>
<xsl:template match="*[contains(@class,' topic/titlealts ')]"/>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]" mode="outofline">
  
  <text:p text:style-name="Default_20_Text">
    <xsl:apply-templates/>
  </text:p>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="outofline">
  
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' topic/abstract ')]">
      <text:span>
        <xsl:apply-templates/>
      </text:span>
    </xsl:when>
    <xsl:otherwise>
      <text:p text:style-name="Default_20_Text">
          <xsl:apply-templates/>
      </text:p>
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
    <xsl:otherwise/>
  </xsl:choose>
</xsl:template>
  
  <!-- called shortdesc processing when it is in abstract -->
  <xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="outofline.abstract">
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
  </xsl:template>
  
<xsl:template match="*[contains(@class,' topic/note ')]" name="topic.note" priority="0">
  
  <xsl:choose>
    <!-- has child tags -->
    <xsl:when test="child::*|text()">
      <xsl:choose>
        <!-- if the parent tag is body or li-->
        <xsl:when test="parent::*[contains(@class, ' topic/body ')]            or parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
          <text:p text:style-name="indent_paragraph_style">
            <text:span>
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
            </text:span>
          </text:p>
        </xsl:when>
        <!-- nested by entry -->
        <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
          <!-- create p tag -->
          <text:p>
            <!-- alignment styles -->
            <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
              <xsl:call-template name="set_align_value"/>
            </xsl:if>
            <!-- cell belongs to thead -->
            <xsl:choose>
              <xsl:when test="parent::*[contains(@class, ' topic/entry ')]                 /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
                <text:span text:style-name="bold">
                  <text:span>
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
                   </text:span>
                </text:span>
              </xsl:when>
              <xsl:otherwise>
                <text:span>
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
                </text:span>
              </xsl:otherwise>
            </xsl:choose>
          </text:p>
        </xsl:when>
        <!-- nested by stentry -->
        <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
          <text:p>
            <!-- cell belongs to sthead -->
            <xsl:choose>
              <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/                 parent::*[contains(@class, ' topic/sthead ')]">
                <text:span text:style-name="bold">
                  <text:span>
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
                  </text:span>
                </text:span>
              </xsl:when>
              <xsl:otherwise>
                <text:span>
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
                </text:span>
              </xsl:otherwise>
            </xsl:choose>
          </text:p>
        </xsl:when>
        <!-- for other tags -->
        <xsl:otherwise>
          <text:span>
            <text:span>
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
            </text:span>
          </text:span>
          <text:line-break/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="create_note_content">
  <xsl:choose>
    <xsl:when test="@type='note' or not(@type)">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Note'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <xsl:if test="ancestor::*[contains(@class,' topic/table ')        or contains(@class,' topic/simpletable ')]">
        <text:tab/>
      </xsl:if>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='tip'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Tip'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='fastpath'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Fastpath'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='important'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Important'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='remember'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Remember'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='restriction'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Restriction'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='attention'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Attention'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='caution'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Caution'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='danger'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Danger'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='trouble'">
      <text:span text:style-name="bold">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Trouble'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='other'">
      <text:span text:style-name="bold">
        <xsl:choose>
          <xsl:when test="@othertype and           not(@othertype='')">
            <xsl:value-of select="@othertype"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>[other]</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
      </text:span>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
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
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Note'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <xsl:if test="ancestor::*[contains(@class,' topic/table ')            or contains(@class,' topic/simpletable ')]">
          <text:tab/>
        </xsl:if>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='tip'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Tip'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='fastpath'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Fastpath'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='important'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Important'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='remember'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Remember'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='restriction'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Restriction'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='attention'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Attention'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='caution'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Caution'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='danger'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'Danger'"/>
        </xsl:call-template>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
    </xsl:when>
    <xsl:when test="@type='trouble'">
      <xsl:attribute name="text:style-name">bold</xsl:attribute>
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'Trouble'"/>
      </xsl:call-template>
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'ColonSymbol'"/>
      </xsl:call-template>
      <xsl:text> </xsl:text>
      <text:span>
        <!-- start add rev flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
        <xsl:apply-templates/>
        <!-- end add rev flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
      </text:span>
    </xsl:when>
    <xsl:when test="@type='other'">
        <xsl:attribute name="text:style-name">bold</xsl:attribute>
        <xsl:choose>
          <xsl:when test="@othertype and not(@othertype='')">
            <xsl:value-of select="@othertype"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>[other]</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'ColonSymbol'"/>
        </xsl:call-template>
        <xsl:text> </xsl:text>
        <text:span>
          <!-- start add rev flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
          <xsl:apply-templates/>
          <!-- end add rev flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
        </text:span>
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
      <text:line-break/>
      <xsl:call-template name="gen-txt">
        <xsl:with-param name="txt" select="substring-after($txt,$newline)"/>
        <xsl:with-param name="isFirst">false</xsl:with-param>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
        <xsl:value-of select="$txt"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="text()">
  <xsl:variable name="style_name">
    <xsl:call-template name="get_style_name"/> 
  </xsl:variable>
  <xsl:variable name="trueStyleName" select="styleUtils:getHiStyleName($style_name)"/>
  <xsl:choose>
    <!-- parent is entry, stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')] or                     parent::*[contains(@class, ' topic/stentry ')]">
        <xsl:choose>
          <xsl:when test="ancestor::*[contains(@class, ' topic/thead ')] or              ancestor::*[contains(@class, ' topic/sthead')]">
            <text:span>
              <!-- 
              <xsl:attribute name="text:style-name">bold</xsl:attribute>
              -->
              <xsl:call-template name="start_flagging_text_of_table_or_list"/>
              <text:span>
                <xsl:if test="$trueStyleName!=''">
                  <xsl:attribute name="text:style-name">
                    <xsl:value-of select="$trueStyleName"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="gen_txt_content"/>
              </text:span>
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span>
              <xsl:call-template name="start_flagging_text_of_table_or_list"/>
              <text:span>
                <xsl:if test="$trueStyleName!=''">
                  <xsl:attribute name="text:style-name">
                    <xsl:value-of select="$trueStyleName"/>
                  </xsl:attribute>
                </xsl:if>
                <xsl:call-template name="gen_txt_content"/>
              </text:span>
              <xsl:call-template name="end_flagging_text_of_table_or_list"/>
            </text:span>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:when>
    <!-- parent is li, sli add p tag otherwise text is invaild. -->
    <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')] or        parent::*[contains(@class, ' topic/sli ')]">
      <text:p text:style-name="indent_paragraph_style">
          <text:span>
            <xsl:call-template name="start_flagging_text_of_table_or_list"/>            
            <text:span>
              <xsl:if test="$trueStyleName!=''">
                <xsl:attribute name="text:style-name">
                  <xsl:value-of select="$trueStyleName"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:call-template name="gen_txt_content"/>
            </text:span>
            <xsl:call-template name="end_flagging_text_of_table_or_list"/>
          </text:span>
      </text:p>
    </xsl:when>
    <!-- text is not allowed under these tags -->
    <xsl:when test="parent::*[contains(@class,' topic/ul ')] | parent::*[contains(@class,' topic/ol ')]       | parent::*[contains(@class,' topic/sl ')] | parent::*[contains(@class,' topic/dl ')]"/>
    <!-- for other tags -->
    <xsl:otherwise>
      <text:span>
        <xsl:if test="$trueStyleName!=''">
          <xsl:choose>
            <!-- title case -->
            <xsl:when test="ancestor::*[contains(@class, ' topic/title ')][1]/parent::*[contains(@class, ' topic/topic ')]">
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
      </text:span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
<xsl:template name="gen_txt_content">
  <xsl:choose>
    <xsl:when test="ancestor::*[contains(@class,' topic/pre ')]">
      <xsl:call-template name="gen-txt">
        <xsl:with-param name="txt" select="."/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="starts-with(.,' ') and not(normalize-space(.)='')">
        <xsl:text> </xsl:text>
      </xsl:if>
      <xsl:value-of select="normalize-space(.)"/>
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
  
  <xsl:variable name="trueStyleName" select="styleUtils:getHiStyleName($style_name)"/>
  
  <xsl:choose>
    <!-- parent is li -->
    <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
      <text:p>
        <text:span text:style-name="boolean_style">
          <xsl:text>boolean:</xsl:text>
          <xsl:value-of select="@state"/>
        </text:span>
      </text:p>
    </xsl:when>
    <!-- nested by entry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
      <!-- create p tag -->
      <text:p>
        <!-- alignment styles -->
        <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
          <xsl:call-template name="set_align_value"/>
        </xsl:if>
        <!-- cell belongs to thead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/entry ')]             /parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
            <text:span text:style-name="bold">
              <text:span text:style-name="boolean_style">
                <xsl:text>boolean:</xsl:text>
                <xsl:value-of select="@state"/>
              </text:span>
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span text:style-name="boolean_style">
              <xsl:text>boolean:</xsl:text>
              <xsl:value-of select="@state"/>
            </text:span>
          </xsl:otherwise>
        </xsl:choose>
      </text:p>
    </xsl:when>
    <!-- nested by stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
      <text:p>
        <!-- cell belongs to sthead -->
        <xsl:choose>
          <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/             parent::*[contains(@class, ' topic/sthead ')]">
            <text:span text:style-name="bold">
              <text:span text:style-name="boolean_style">
                <xsl:text>boolean:</xsl:text>
                <xsl:value-of select="@state"/>
              </text:span>
            </text:span>
          </xsl:when>
          <xsl:otherwise>
            <text:span text:style-name="boolean_style">
              <xsl:text>boolean:</xsl:text>
              <xsl:value-of select="@state"/>
            </text:span>
          </xsl:otherwise>
        </xsl:choose>
      </text:p>
    </xsl:when>
    <!-- nested by other tags -->
    <xsl:otherwise>
      <text:span>
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
      </text:span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/state ')]">  
    <xsl:variable name="style_name">
      <xsl:call-template name="get_style_name"/> 
    </xsl:variable>
    <xsl:variable name="trueStyleName" select="styleUtils:getHiStyleName($style_name)"/>
    <text:span>
      <xsl:if test="$trueStyleName!=''">
        <xsl:attribute name="text:style-name">
          <xsl:value-of select="$trueStyleName"/>
        </xsl:attribute>
      </xsl:if>
      <text:span text:style-name="state_style">
        <xsl:value-of select="name()"/>
        <xsl:text>: </xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text>=</xsl:text>
        <xsl:value-of select="@value"/>
      </text:span>
    </text:span>
  </xsl:template>
  
<!-- itemgroup tag -->
<xsl:template match="*[contains(@class, ' topic/itemgroup ')]">
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class, ' topic/dd ')]">
      <text:span>
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
      </text:span>
    </xsl:when>
    <xsl:when test="parent::*[contains(@class, ' topic/li')]">
      <text:p>
        <text:span>
          <!-- start add flagging styles -->
          <xsl:apply-templates select="." mode="start-add-odt-flags"/>
          <xsl:apply-templates/>
          <!-- end add flagging styles -->
          <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
        </text:span>
      </text:p>
    </xsl:when>
    <xsl:otherwise>
      <text:span>
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
      </text:span>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Indexterm tag -->
<xsl:template match="*[contains(@class, ' topic/indexterm ')]">
  <xsl:if test="$INDEXSHOW='yes'">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <xsl:call-template name="create_indexterm_content"/>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                  <xsl:call-template name="create_indexterm_content"/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_indexterm_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <xsl:call-template name="create_indexterm_content"/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="create_indexterm_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:call-template name="create_indexterm_content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<xsl:template name="create_indexterm_content">
  <xsl:variable name="indexId" select="concat('IMark', generate-id(.))"/>
  <xsl:variable name="depth" select="count(ancestor-or-self::*[contains(@class,' topic/indexterm ')])"/>
  <text:user-index-mark-start text:id="{$indexId}" text:index-name="{'user-defined'}" text:outline-level="{$depth}"/>
  <xsl:apply-templates/>
  <text:user-index-mark-end text:id="{$indexId}"/>    
</xsl:template>

  <xsl:template match="*[contains(@class,' topic/tm ')]">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <xsl:call-template name="create_tm_content"/>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <xsl:call-template name="create_tm_content"/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_tm_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <xsl:call-template name="create_tm_content"/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_tm_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <xsl:call-template name="create_tm_content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="create_tm_content">
    <text:span>
      <xsl:apply-templates/>
    </text:span>
    <xsl:choose>
      <xsl:when test="@tmtype='tm'">
        <text:span text:style-name="sup">
          <xsl:text>(TM)</xsl:text>
        </text:span>
      </xsl:when>
      <xsl:when test="@tmtype='service'">
        <text:span text:style-name="sup">
          <xsl:text>(SM)</xsl:text>
        </text:span>
      </xsl:when>
      <xsl:when test="@tmtype='reg'">
        <text:span text:style-name="sup">
          <xsl:text>(R)</xsl:text>
        </text:span>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

<xsl:template match="*[contains(@class,' topic/draft-comment ')]">
  <xsl:if test="$DRAFT='yes'">
    <xsl:apply-templates select="." mode="ditamsg:draft-comment-in-content"/>
    <xsl:choose>
      <!-- parent is p or list -->
      <xsl:when test="parent::*[contains(@class, ' topic/body ')] or
                      parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <text:p text:style-name="draftcomment_paragraph">
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <text:span text:style-name="bold">
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="'Draft comment'"/>
              </xsl:call-template>
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="'ColonSymbol'"/>
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
              <text:line-break/>
            </text:span>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <text:span text:style-name="bold">
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'Draft comment'"/>
                    </xsl:call-template>
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'ColonSymbol'"/>
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
                    <text:line-break/>
                  </text:span>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>  
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <text:span text:style-name="bold">
                  <xsl:call-template name="getVariable">
                    <xsl:with-param name="id" select="'Draft comment'"/>
                  </xsl:call-template>
                  <xsl:call-template name="getVariable">
                    <xsl:with-param name="id" select="'ColonSymbol'"/>
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
                  <text:line-break/>
                </text:span>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	  
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <text:span text:style-name="bold">
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'Draft comment'"/>
                    </xsl:call-template>
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'ColonSymbol'"/>
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
                    <text:line-break/>
                  </text:span>
                  <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/> 
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <text:span text:style-name="bold">
                  <xsl:call-template name="getVariable">
                    <xsl:with-param name="id" select="'Draft comment'"/>
                  </xsl:call-template>
                  <xsl:call-template name="getVariable">
                    <xsl:with-param name="id" select="'ColonSymbol'"/>
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
                  <text:line-break/>
                </text:span>
                <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	  
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- parent is other tag -->
      <xsl:otherwise>
        <text:span>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <text:span text:style-name="bold">
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="'Draft comment'"/>
              </xsl:call-template>
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="'ColonSymbol'"/>
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
              <text:line-break/>
            </text:span>
            <xsl:apply-templates/>
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>
          </text:span>
          <text:line-break/>
        </text:span>
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
                      parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
            <text:span>
              <!-- start add flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                <text:span text:style-name="bold">
                  <xsl:text>[</xsl:text>
                  <xsl:call-template name="getVariable">
                    <xsl:with-param name="id" select="'Required cleanup'"/>
                  </xsl:call-template>
                  <xsl:text>]</xsl:text>
                  <xsl:if test="string(@remap)">
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="@remap"/>
                    <xsl:text>)</xsl:text>
                  </xsl:if>
                  <xsl:call-template name="getVariable">
                    <xsl:with-param name="id" select="'ColonSymbol'"/>
                  </xsl:call-template>
                  <xsl:text> </xsl:text>
                </text:span>
                <xsl:apply-templates/>
              <!-- end add flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
            </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    <text:span text:style-name="bold">
                      <xsl:text>[</xsl:text>
                      <xsl:call-template name="getVariable">
                        <xsl:with-param name="id" select="'Required cleanup'"/>
                      </xsl:call-template>
                      <xsl:text>]</xsl:text>
                      <xsl:if test="string(@remap)">
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="@remap"/>
                        <xsl:text>)</xsl:text>
                      </xsl:if>
                      <xsl:call-template name="getVariable">
                        <xsl:with-param name="id" select="'ColonSymbol'"/>
                      </xsl:call-template>
                      <xsl:text> </xsl:text>
                    </text:span>
                    <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <text:span text:style-name="bold">
                    <xsl:text>[</xsl:text>
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'Required cleanup'"/>
                    </xsl:call-template>
                    <xsl:text>]</xsl:text>
                    <xsl:if test="string(@remap)">
                      <xsl:text>(</xsl:text>
                      <xsl:value-of select="@remap"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'ColonSymbol'"/>
                    </xsl:call-template>
                    <xsl:text> </xsl:text>
                  </text:span>
                  <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span>
                  <!-- start add flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    <text:span text:style-name="bold">
                      <xsl:text>[</xsl:text>
                      <xsl:call-template name="getVariable">
                        <xsl:with-param name="id" select="'Required cleanup'"/>
                      </xsl:call-template>
                      <xsl:text>]</xsl:text>
                      <xsl:if test="string(@remap)">
                        <xsl:text>(</xsl:text>
                        <xsl:value-of select="@remap"/>
                        <xsl:text>)</xsl:text>
                      </xsl:if>
                      <xsl:call-template name="getVariable">
                        <xsl:with-param name="id" select="'ColonSymbol'"/>
                      </xsl:call-template>
                      <xsl:text> </xsl:text>
                    </text:span>
                    <xsl:apply-templates/>
                  <!-- end add flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span>
                <!-- start add flagging styles -->
                <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                  <text:span text:style-name="bold">
                    <xsl:text>[</xsl:text>
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'Required cleanup'"/>
                    </xsl:call-template>
                    <xsl:text>]</xsl:text>
                    <xsl:if test="string(@remap)">
                      <xsl:text>(</xsl:text>
                      <xsl:value-of select="@remap"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'ColonSymbol'"/>
                    </xsl:call-template>
                    <xsl:text> </xsl:text>
                  </text:span>
                  <xsl:apply-templates/>
                <!-- end add flagging styles -->
                <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- parent is other tag -->
      <xsl:otherwise>
        <text:span>
          <text:span>
            <!-- start add flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-flags"/>
            <!-- 
            <xsl:element name="text:span">
              <xsl:attribute name="text:style-name">required_cleanup_style</xsl:attribute>
            -->
              <text:span text:style-name="bold">
                <xsl:text>[</xsl:text>
                <xsl:call-template name="getVariable">
                  <xsl:with-param name="id" select="'Required cleanup'"/>
                </xsl:call-template>
                <xsl:text>]</xsl:text>
                <xsl:if test="string(@remap)">
                  <xsl:text>(</xsl:text>
                  <xsl:value-of select="@remap"/>
                  <xsl:text>)</xsl:text>
                </xsl:if>
                <xsl:call-template name="getVariable">
                  <xsl:with-param name="id" select="'ColonSymbol'"/>
                </xsl:call-template>
                <xsl:text> </xsl:text>
              </text:span>
              <xsl:apply-templates/>
            <!-- 
            </xsl:element>
            -->
            <!-- end add flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
          </text:span>
          <text:line-break/>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

  <xsl:template match="*[contains(@class, ' topic/keywords ')]">
    <text:p text:style-name="indent_paragraph_style">
      <xsl:apply-templates/>
    </text:p>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/term ')]">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <text:span text:style-name="italic">
            <text:span>
              <!-- start add rev flagging styles -->
              <xsl:apply-templates select="." mode="start-add-odt-flags">
                <xsl:with-param name="type" select="'keyword'"/>
              </xsl:apply-templates>
              <xsl:apply-templates/>
              <!-- end add rev flagging styles -->
              <xsl:apply-templates select="." mode="end-add-odt-flags">
                <xsl:with-param name="type" select="'keyword'"/>
              </xsl:apply-templates>
            </text:span>
          </text:span>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <text:span text:style-name="italic">
                  <text:span>
                    <!-- start add rev flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                    <xsl:apply-templates/>
                    <!-- end add rev flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                  </text:span>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span text:style-name="italic">
                <text:span>
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:apply-templates/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </text:span>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <text:span text:style-name="italic">
                  <text:span>
                    <!-- start add rev flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                    <xsl:apply-templates/>
                    <!-- end add rev flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                  </text:span>
                </text:span>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <text:span text:style-name="italic">
                <text:span>
                  <!-- start add rev flagging styles -->
                  <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
                  <xsl:apply-templates/>
                  <!-- end add rev flagging styles -->
                  <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
                </text:span>
              </text:span>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by other tags -->
      <xsl:otherwise>
        <text:span text:style-name="italic">
          <text:span>
            <!-- start add rev flagging styles -->
            <xsl:apply-templates select="." mode="start-add-odt-revflags"/>
            <xsl:apply-templates/>
            <!-- end add rev flagging styles -->
            <xsl:apply-templates select="." mode="end-add-odt-revflags"/>
          </text:span>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' topic/fn ')]">
    <xsl:choose>
      <xsl:when test="parent::*[contains(@class, ' topic/li ')] or parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <xsl:call-template name="create_fn_content"/>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <xsl:call-template name="create_fn_content"/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_fn_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <xsl:call-template name="create_fn_content"/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="create_fn_content"/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
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
            <text:note text:note-class="footnote">
              <text:note-citation text:label="{@callout}">
                <xsl:value-of select="@callout"/>
              </text:note-citation>
              <text:note-body>
                <text:p text:style-name="footnote">
                  <text:span>
                    <!-- start add flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    <xsl:apply-templates/>
                    <!-- end add flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                  </text:span>
                </text:p>
              </text:note-body>
            </text:note>
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
            <text:note text:note-class="footnote">
              <text:note-citation>
                <xsl:value-of select="$fnNumber"/>
              </text:note-citation>
              <text:note-body>
                <text:p text:style-name="footnote">
                  <text:span>
                    <!-- start add flagging styles -->
                    <xsl:apply-templates select="." mode="start-add-odt-flags"/>
                    <xsl:apply-templates/>
                    <!-- end add flagging styles -->
                    <xsl:apply-templates select="." mode="end-add-odt-flags"/>
                  </text:span>
                </text:p>
              </text:note-body>
            </text:note>  
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
                      parent::*[contains(@class, ' topic/li ')] or
                      parent::*[contains(@class, ' topic/sli ')]">
        <text:p>
          <xsl:apply-templates/>
        </text:p>
      </xsl:when>
      <!-- nested by entry -->
      <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
        <!-- create p tag -->
        <text:p>
          <!-- alignment styles -->
          <xsl:if test="parent::*[contains(@class, ' topic/entry ')]/@align">
            <xsl:call-template name="set_align_value"/>
          </xsl:if>
          <!-- cell belongs to thead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/entry ')]/parent::*[contains(@class, ' topic/row ')]/parent::*[contains(@class, ' topic/thead ')]">
              <text:span text:style-name="bold">
                <xsl:apply-templates/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- nested by stentry -->
      <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
        <text:p>
          <!-- cell belongs to sthead -->
          <xsl:choose>
            <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]/parent::*[contains(@class, ' topic/sthead ')]">
              <text:span text:style-name="bold">
                <xsl:apply-templates/>
              </text:span>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates/>
            </xsl:otherwise>
          </xsl:choose>
        </text:p>
      </xsl:when>
      <!-- for other tags -->
      <xsl:otherwise>
        <text:span>
          <xsl:apply-templates/>
        </text:span>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Add for "Support foreign content vocabularies such as 
  MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]"/>

</xsl:stylesheet>