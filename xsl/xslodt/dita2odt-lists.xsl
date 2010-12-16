<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
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
  xmlns:random="org.dita.dost.util.RandomUtils" exclude-result-prefixes="random"
  version="1.0">

<xsl:template match="*[contains(@class,' topic/ul ')]">

        <!-- render list -->
        <xsl:call-template name="render_list">
          <xsl:with-param name="list_style" select="'list_style'"/>
        </xsl:call-template>
        
</xsl:template>
  
<xsl:template match="*[contains(@class,' topic/ol ')]">
  
  <!-- render list -->
  <xsl:call-template name="render_list">
    <xsl:with-param name="list_style" select="'ordered_list_style'"/>
  </xsl:call-template>
  
</xsl:template>
  
<xsl:template match="*[contains(@class,' topic/sl ')]">
  
  <!-- render list -->
  <xsl:call-template name="render_list">
    <xsl:with-param name="list_style" select="'list_style'"/>
  </xsl:call-template>
  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/li ')]">
  
  <xsl:element name="text:list-item">
    <xsl:apply-templates/>
  </xsl:element>
  
</xsl:template>

<xsl:template match="*[contains(@class,' topic/sli ')]">

  <xsl:call-template name="block-sli"/>
  
</xsl:template>

<xsl:template name="block-li">
  <xsl:variable name="depth"><xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')])"/></xsl:variable>
  <xsl:variable name="li-num" select="420 + ($depth * 420)"/>
  <xsl:variable name="listnum" select="count(preceding::*[contains(@class,' topic/ol ') 
  or contains(@class,' topic/ul ')][not(ancestor::*[contains(@class,' topic/li ')])]) + 1"/>
  
  <xsl:element name="text:list-item">
    <xsl:apply-templates mode="create_list_item"/>
  </xsl:element>
</xsl:template>

<xsl:template match="*|text()" mode="create_list_item">
  <xsl:apply-templates select="."/>
</xsl:template>
  
<xsl:template name="block-sli">
  <xsl:element name="text:list-item">
      <xsl:apply-templates/>
  </xsl:element>
</xsl:template>
  

<!-- definition list -->
<xsl:template match="*[contains(@class,' topic/dl ')]">
  
  <!-- render list -->
  <xsl:call-template name="render_list">
    <xsl:with-param name="list_style" select="'list_style'"/>
  </xsl:call-template>

</xsl:template>
  
<!-- definition list -->
<!-- for dl tag -->
<xsl:template name="block-lq">
  <xsl:choose>
    <!-- nested by p -->
    <xsl:when test="parent::*[contains(@class, ' topic/p ')]">
      <!-- break p tag -->
      <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
      <!-- start render dl -->
      <xsl:element name="text:p">
        <xsl:apply-templates/>
      </xsl:element>
      <!-- start p tag again -->
      <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
    </xsl:when>
    <!-- nested by note -->
    <xsl:when test="parent::*[contains(@class, ' topic/note ')]">
      <!-- break p tag -->
      <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
      <!-- start render dl -->
      <xsl:element name="text:p">
        <xsl:apply-templates/>
      </xsl:element>
      <!-- start p tag again -->
      <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
    </xsl:when>
    <!-- nested by lq -->
    <xsl:when test="parent::*[contains(@class, ' topic/lq ')]">
      <xsl:apply-templates/>
    </xsl:when>
    <!-- nested by itemgroup -->
    <xsl:when test="parent::*[contains(@class, ' topic/itemgroup ')]">
      <xsl:apply-templates/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:element name="text:p">
        <xsl:apply-templates/>
      </xsl:element>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
<!-- dlhead tag-->
<xsl:template match="*[contains(@class, ' topic/dlhead ')]" name="topic.dlhead">
  
  <xsl:element name="text:list-item">
      <xsl:apply-templates/>
  </xsl:element>
  
</xsl:template>

<!-- DL heading, term -->
<xsl:template match="*[contains(@class,' topic/dthd ')]" name="topic.dthd">
  
  <xsl:element name="text:p">
    <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
    <xsl:element name="text:span">
      <!-- start add flagging styles -->
      <xsl:apply-templates select="." mode="start-add-odt-flags"/>
      
      <xsl:apply-templates/>
      
      <!-- end add flagging styles -->
      <xsl:apply-templates select="." mode="end-add-odt-flags"/>
    </xsl:element>
  </xsl:element>
  
</xsl:template>
  
<!-- DL heading, description -->
<xsl:template match="*[contains(@class,' topic/ddhd ')]" name="topic.ddhd">
  
  
  <xsl:element name="text:p">
    <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        
        <xsl:element name="text:tab"/>
        <xsl:apply-templates/>
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>
      </xsl:element>
  </xsl:element>
</xsl:template>

<!-- dlentry tag-->
<xsl:template match="*[contains(@class,' topic/dlentry ')]" name="topic.dlentry">
  
  <xsl:element name="text:list-item">
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<!-- for dt tag -->
<xsl:template match="*[contains(@class, ' topic/dt ')]">

  
  <xsl:element name="text:p">
      <xsl:attribute name="text:style-name">bold_paragraph</xsl:attribute>
      <xsl:element name="text:span">
        <!-- start add flagging styles -->
        <xsl:apply-templates select="." mode="start-add-odt-flags"/>
        
        <xsl:apply-templates/>
        
        <!-- end add flagging styles -->
        <xsl:apply-templates select="." mode="end-add-odt-flags"/>	
        
      </xsl:element>
  </xsl:element>
</xsl:template>

<!-- for dd tag -->
<xsl:template match="*[contains(@class, ' topic/dd ')]">
  
  <xsl:element name="text:p">
    <xsl:element name="text:span">
      
      <!-- start add flagging styles -->
      <xsl:apply-templates select="." mode="start-add-odt-flags"/>
      
      <xsl:element name="text:tab"/>
        <xsl:apply-templates/>
      
      <!-- end add flagging styles -->
      <xsl:apply-templates select="." mode="end-add-odt-flags"/>
      
    </xsl:element>
  </xsl:element>
  
</xsl:template>

<!-- parameter list -->

<xsl:template match="parml"> <!-- not found -->
  <xsl:call-template name="block-lq"/>
</xsl:template>

<xsl:template match="plentry/synph">  <!-- plentry not found -->
  <xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="plentry/li">  <!-- plentry not found -->
  <xsl:call-template name="block-lq"/>
</xsl:template>
	
<!-- block-list -->
<xsl:template name="block-list"/>


<xsl:template name="block-ol"/>



<xsl:template name="gen-list-table"/>

	
<xsl:template match="*[contains(@class,' topic/ol ')]" mode="gen-list-table"/>
	
<xsl:template match="*[contains(@class,' topic/ul ')]" mode="gen-list-table"/>

	
</xsl:stylesheet>