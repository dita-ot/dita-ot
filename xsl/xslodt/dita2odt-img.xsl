<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<!-- 
  <draw:frame text:anchor-type="as-char"
  svg:width="6in" svg:height="3.2736in" draw:z-index="0">
  <draw:image xlink:href="Pictures/100000000000027A0000015A35A01F85.jpg"
  xlink:type="simple" xlink:show="embed" xlink:actuate="onLoad"/>
  </draw:frame>

-->
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
  version="1.0" 
  xmlns:java="org.dita.dost.util.ImgUtils" exclude-result-prefixes="java">

<xsl:output method="xml"/>
<xsl:output indent="yes"/>
<xsl:strip-space elements="*"/>

<xsl:template match="*[contains(@class,' topic/image ')]">
<xsl:if test="@href and not(@href='')">
  <!-- break p tag -->
  <!-- 
  <xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
  <text:p text:style-name="Default_20_Text">
  -->
    <xsl:variable name="type">
      <xsl:choose>
        <xsl:when test="not(contains(@href,'://'))">
          <xsl:value-of select="java:getType(string(@href))"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="height">
      <xsl:choose>
        <xsl:when test="@height">
          <xsl:choose>
            <xsl:when test="contains(@height, 'in')">
              <xsl:value-of select="substring-before(@height, 'in')"/>
            </xsl:when>
            <xsl:when test="contains(@height, 'cm')">
              <xsl:value-of select="number(substring-before(@height, 'cm')) div 2.54"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="number(@height div 100)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="not(contains(@href,'://'))">
          <xsl:value-of select="number(java:getHeight($OUTPUTDIR, string(@href)) div 100)"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="width">
      <xsl:choose>
        <xsl:when test="@width">
          <xsl:choose>
            <xsl:when test="contains(@width, 'in')">
              <xsl:value-of select="substring-before(@width, 'in')"/>
            </xsl:when>
            <xsl:when test="contains(@width, 'cm')">
              <xsl:value-of select="number(substring-before(@width, 'cm')) div 2.54"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="number(@width div 100)"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:when test="not(contains(@href,'://'))">
          <xsl:value-of select="number(java:getWidth($OUTPUTDIR, string(@href)) div 100)"/>
        </xsl:when>
        <xsl:otherwise/>
      </xsl:choose>
    </xsl:variable>
  
  <xsl:choose>
    <!-- nested by body or list -->
    <xsl:when test="parent::*[contains(@class, ' topic/body')] or 
      parent::*[contains(@class, ' topic/li ')]">
      <xsl:element name="text:p">
        <xsl:call-template name="draw_image">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
        </xsl:call-template>
      </xsl:element>
    </xsl:when>
    <!-- nested by entry -->
    <xsl:when test="parent::*[contains(@class, ' topic/entry ')]">
      <!-- create p tag -->
      <xsl:element name="text:p">
          <xsl:call-template name="draw_image">
            <xsl:with-param name="height" select="$height"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="width" select="$width"/>
          </xsl:call-template>
      </xsl:element>
    </xsl:when>
    <!-- nested by stentry -->
    <xsl:when test="parent::*[contains(@class, ' topic/stentry ')]">
      <xsl:element name="text:p">
          <xsl:call-template name="draw_image">
            <xsl:with-param name="height" select="$height"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="width" select="$width"/>
          </xsl:call-template>
      </xsl:element>
    </xsl:when>
    <!-- nested by other tags -->
    <xsl:otherwise>
        <xsl:call-template name="draw_image">
          <xsl:with-param name="height" select="$height"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="width" select="$width"/>
        </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
    
  <!-- 
  </text:p>
  -->
  <!-- start p tag again -->
  <!-- 
  <xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
  -->
</xsl:if>
</xsl:template>
  <xsl:template name="draw_image">
    <xsl:param name="type"/>
    <xsl:param name="height"/>
    <xsl:param name="width"/>
    
    <xsl:choose>
      <xsl:when test="not(contains(@href,'://')) and $type and not($type='other') and ($height &gt; 0) and ($width &gt; 0)">
        <xsl:element name="draw:frame">  
          <xsl:attribute name="text:anchor-type">as-char</xsl:attribute>
          <xsl:attribute name="svg:y">-0.18in</xsl:attribute>
          <xsl:attribute name="svg:width">
            <xsl:choose>
              <xsl:when test="$width &gt; 6">6</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$width"/>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="'in'"/>
          </xsl:attribute>
          <xsl:attribute name="svg:height"><xsl:value-of select="$height"/>in</xsl:attribute>       
          <xsl:element name="draw:image">  
            <xsl:element name="office:binary-data">
              <xsl:value-of select="java:getBASE64($OUTPUTDIR, string(@href))" disable-output-escaping="yes"/>
            </xsl:element>
          </xsl:element>
        </xsl:element>
      </xsl:when>
      <xsl:otherwise>
        <xsl:element name="text:a">
          <xsl:attribute name="xlink:href">
            <xsl:value-of select="@href"/>
          </xsl:attribute>
          <xsl:attribute name="xlink:type">simple</xsl:attribute>
          <xsl:call-template name="gen-img-txt"/>
        </xsl:element>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  

<xsl:template name="gen-img-txt">
    <xsl:choose>
    <xsl:when test="*[contains(@class,' topic/alt ')]">
    <xsl:text>[PIC]</xsl:text><xsl:value-of select="*[contains(@class,' topic/alt ')]"/>
    </xsl:when>
    <xsl:when test="@alt and not(@alt='')"><xsl:text>[PIC]</xsl:text><xsl:value-of select="@alt"/></xsl:when>
    <xsl:when test="text() or *"><xsl:text>[PIC]</xsl:text><xsl:apply-templates/></xsl:when>
    <xsl:otherwise><xsl:text>[PIC]</xsl:text><xsl:value-of select="@href"/></xsl:otherwise>
    </xsl:choose>
  
</xsl:template>

</xsl:stylesheet>